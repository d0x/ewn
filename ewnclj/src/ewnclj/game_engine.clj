(ns ewnclj.game-engine
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.ki :as ki]
            [ewnclj.parser :as p]
            [ewnclj.communication :as net]
            [ewnclj.board :as b]
            [clojure.core.match :refer (match)]))

; ----- Acting to responses
(defn do-own-startaufstellung [game-state network]
  (let [own-side (if (= (game-state :opponent-side) "t") "b" "t")
        aufstellung (ki/choose-startaufstellung game-state own-side)
        steine (p/parse-aufstellung aufstellung)
        new-board (reduce
                    (fn [board stein]
                      (b/bset board (stein :x) (stein :y) (str "b" (stein :augen))))
                    (game-state :board) steine)]
    (net/send-command network aufstellung)
    (assoc game-state
      :own-side own-side
      :board new-board)))

(defn do-opponent-startaufstellung [game-state aufstellung]
  (let [steine (p/parse-aufstellung aufstellung)
        opponent-side (if (b/is-top-half (get steine 0)) "t" "b")
        new-board (reduce
                    (fn [board stein]
                      (b/bset board (stein :x) (stein :y) (str "o" (stein :augen))))
                    (game-state :board) steine)]
    (assoc game-state
      :opponent-side opponent-side
      :board new-board)))

(defn send-quit-when-winner-found [game-state network]
  (let [potential-winner (b/get-winner (game-state :board) (game-state :own-side) (game-state :opponent-side))]
    (when (some? potential-winner)
      (do
        (println potential-winner "hat gewonnen!")
        (net/send-command network "quit"))
      )
    game-state))

(defn do-opp-move [game-state stein wuerfel network]
  (send-quit-when-winner-found game-state network)
  ; TODO use wuerfel to prevent cheating!
  (assoc game-state :board (b/move-stein (game-state :board) "o" stein)))

(defn do-own-move [game-state wuerfel network]
  (let [stein (ki/choose-move game-state wuerfel)]
    (net/send-command network (str (stein :augen) (inc (stein :x)) (inc (stein :y))))
    (let [new-board (b/move-stein (game-state :board) "b" stein)]
      (send-quit-when-winner-found (assoc game-state :board new-board) network))))

(defn handle-Z-command [response game-state network]
  (if (= (response :sender) "Server")
    (cond
      (= (response :message) "Sie sind am Zug") game-state
      (str/starts-with? (response :message) "Zug an ") game-state
      (str/starts-with? (response :message) "Würfel: ") (if (:own-side game-state)
                                                          (do-own-move game-state (Integer/parseInt (str/replace (response :message) "Würfel: " "")) network)
                                                          (do-own-startaufstellung game-state network))
      :else (do (println "Unhandled Response: " (response :raw)) game-state))
    ; Messages from opponent
    (cond
      (nil? (game-state :opponent-side)) (do-opponent-startaufstellung game-state (response :message))
      (some? (re-matches #"\d{3}" (response :message))) (do-opp-move game-state (p/parse-stein (response :message)) (response :wuerfel) network)
      :else (do (println "Unhandled Response: " (response :raw)) game-state))))

(defn challenge-opponent-if-present [response game-state network]
  (let [opponent-to-challenge (game-state :opponent-to-challenge)]
    (when opponent-to-challenge
      (if (some #{opponent-to-challenge} (p/parse-player-list response))
        (net/send-command network (str "spiel " opponent-to-challenge))
        (do
          (Thread/sleep 1000)
          (net/send-command network "liste")
          )))
    game-state))

(defn handle-B-command [response game-state network]
  "B - success"
  (if (= (:sender response) "Server")
    (cond
      (= (response :message) "Verbindung zum Server erstellt") (do (net/send-command network (str "login " (game-state :botname))) game-state)
      (= (response :message) "Spiel startet") game-state
      (= (response :message) "disconnect") (do (net/shutdown-network network) game-state)
      (= (response :message) (str (game-state :botname) ", Sie sind angemeldet")) (do (net/send-command network "liste") (println "Waiting for game-state requests") game-state)
      (= (response :message) (str (game-state :opponent-to-challenge) " akzeptiert")) game-state
      (str/starts-with? (response :message) "Folgende Spieler waeren bereit zu spielen:") (challenge-opponent-if-present response game-state network)
      :else (do (println "Unhandled Response: " response :raw) (net/send-command network "logout") game-state))
    (do (println "Unhandled Response: " (response :raw)) game-state)))

(defn handle-Q-command [response game-state network]
  "Akzeptiert jeden game-state Request"
  (let [opponent-name (p/parse-opponent-name response)]
    (net/send-command network "Ja")
    (assoc c/initial-game-state :botname (game-state :botname) :opponent-name opponent-name)))

(defn handle-E102-nick-in-used [response game-state network]
  (let [new-name (ki/choose-next-nick-name (game-state :botname))]
    (net/send-command network (str "login " new-name))
    (assoc game-state :botname new-name)))

(defn handle-M-command [response game-state network] game-state)

(defn handle-E001-unkown-command [response game-state] game-state)

(defn handle-response [raw-response game-state network]
  (let [response (p/parse-response raw-response)]
    (case (response :code)
      "B" (handle-B-command response game-state network)
      "Q" (handle-Q-command response game-state network)
      "Z" (handle-Z-command response game-state network)
      "M" (handle-M-command response game-state network)
      "E001" (handle-E001-unkown-command response game-state)
      "E201" (do (net/send-command network "logout") (net/shutdown-network network) game-state)
      "E102" (handle-E102-nick-in-used response game-state network)
      "E302" (do (net/shutdown-network network) game-state)
      :else (do (println "Unkown code" (response :code) "in Response:" (response :raw)) game-state))))

(defn start-engine
  [name opponent-to-challenge sleep host]
  (let [network (net/connect host)]
    (do
      (.addShutdownHook (Runtime/getRuntime) (Thread. #(when (net/network-connected network) (net/send-command network "logout"))))
      (loop [game-state (assoc c/initial-game-state
                          :opponent-to-challenge opponent-to-challenge
                          :botname name)]
        (if (net/network-connected network)
          (let [raw-response (net/read-response network)
                new-game-state (handle-response raw-response game-state network)]
            (when (not=
                    (new-game-state :board)
                    (game-state :board))
              (b/print-board (new-game-state :board)))
            (Thread/sleep sleep)
            (recur new-game-state)))))))
