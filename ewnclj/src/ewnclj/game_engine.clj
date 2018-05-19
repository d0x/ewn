(ns ewnclj.game-engine
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.ki :as ki]
            [ewnclj.ki-random :as ki-rnd]
            [ewnclj.parser :as p]
            [ewnclj.communication :as net]
            [ewnclj.board :as b]
            [clojure.core.match :refer (match)]
            [ewnclj.utils :as u]
            [ewnclj.utils :as u]))

; ----- Acting to responses
(defn do-own-startaufstellung [game-state network]
  (let [own-side (if (= (game-state :opponent-side) "↘️") "↖️" "↘️")
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

(defn apply-opponent-startaufstellung [game-state aufstellung]
  (let [steine (p/parse-aufstellung aufstellung)
        opponent-side (if (b/is-top-half (get steine 0)) "↘️" "↖️")
        new-board (reduce
                    (fn [board stein]
                      (b/bset board (stein :x) (stein :y) (str "o" (stein :augen))))
                    (game-state :board) steine)]
    (assoc game-state
      :opponent-side opponent-side
      :board new-board)))

(defn send-quit-when-winner-found [game-state network]
  (let [potential-winner (b/get-winner (game-state :board) (game-state :own-side) (game-state :opponent-side))
        winners-name (if (some? potential-winner)
                       (if (= potential-winner "b")
                         (game-state :botname)
                         (game-state :opponent-name))
                       nil)
        winners-key (keyword winners-name)
        wins (game-state :wins {})]
    (if (some? potential-winner)
      (do
        (println winners-name "hat gewonnen!")
        (when (= "b" potential-winner)
          (net/send-command network "quit"))
        (assoc game-state
          :winner potential-winner
          :wins (assoc wins
                  winners-key (inc (winners-key wins 0)))
          ))
      game-state)))

(defn do-opp-move [game-state stein wuerfel network]
  ; TODO use wuerfel to prevent cheating!
  (send-quit-when-winner-found
    (assoc game-state :board (b/place-stein (game-state :board) "o" (stein :augen) (stein :x) (stein :y)))
    network))

(defn do-own-move [game-state wuerfel network ki]
  (let [stein ((ki :choose-move) (game-state :board) "b" (game-state :own-side) wuerfel)]
    (net/send-command network (str (stein :augen) (inc (stein :x)) (inc (stein :y))))
    (let [new-board (b/place-stein (game-state :board) "b" (stein :augen) (stein :x) (stein :y))]
      (send-quit-when-winner-found (assoc game-state :board new-board) network))))

(defn do-shutdown [game-state network]
  (net/send-command network "logout") (net/shutdown-network network)
  game-state)

(defn handle-Z-command [response game-state network ki]
  (if-not (some? (game-state :winner))
    (if (= (response :sender) "Server")
      (cond
        (= (response :message) "Sie sind am Zug") game-state
        (str/starts-with? (response :message) "Zug an ") game-state
        (str/starts-with? (response :message) "Würfel: ") (if (game-state :own-side)
                                                            (do-own-move game-state (Integer/parseInt (str/replace (response :message) "Würfel: " "")) network ki)
                                                            (do-own-startaufstellung game-state network))
        :else (do (println "Unhandled Response: " (response :raw)) game-state))
      ; Messages from opponent
      (cond
        (nil? (game-state :opponent-side)) (apply-opponent-startaufstellung game-state (response :message))
        (some? (re-matches #"\d{3}" (response :message))) (do-opp-move game-state (p/parse-stein (response :message)) (response :wuerfel) network)
        :else (do (println "Unhandled Response: " (response :raw)) game-state)))
    game-state))

(defn challenge-opponent-if-present [response game-state network]
  (when-let [opponent-to-challenge (game-state :opponent-to-challenge)]
    (if (some #{opponent-to-challenge} (p/parse-player-list response))
      (net/send-command network (str "spiel " opponent-to-challenge))
      (do
        (Thread/sleep 1000)
        (net/send-command network "liste"))))
  game-state)

(defn clean-state [game-state]
  (assoc game-state
    :board c/initial-board
    :own-side nil
    :opponent-side nil
    :opponent-name nil
    :winner nil))

(defn handle-B-command [response game-state network]
  "B - success"
  (if (= (:sender response) "Server")
    (cond
      (= (response :message) "Verbindung zum Server erstellt") (do (net/send-command network (str "login " (game-state :botname))) game-state)
      (= (response :message) "Spiel startet") game-state
      (= (response :message) "disconnect") (do (net/shutdown-network network) game-state)
      (= (response :message) (str (game-state :botname) ", Sie sind angemeldet")) (do (net/send-command network "liste") (println "Waiting for game-state requests") game-state)
      (= (response :message) (str (game-state :opponent-to-challenge) " akzeptiert")) (assoc (clean-state game-state) :opponent-name (game-state :opponent-to-challenge))
      (str/starts-with? (response :message) "Folgende Spieler waeren bereit zu spielen:") (challenge-opponent-if-present response game-state network)
      :else (do (println "Unhandled Response: " response :raw) (do-shutdown game-state network)))
    (do (println "Unhandled Response: " (response :raw)) game-state)))

(defn handle-Q-command [response game-state network]
  "Akzeptiert jeden game-state Request"
  (let [opponent-name (p/parse-opponent-name response)]
    (net/send-command network "Ja")
    (assoc (clean-state game-state) :opponent-name opponent-name)))

(defn handle-E102-nick-in-used-command [response game-state network]
  (let [new-name (ki/choose-next-nick-name (game-state :botname))]
    (net/send-command network (str "login " new-name))
    (assoc game-state :botname new-name)))

(defn handle-M-command [response game-state network]
  (when (and (= (response :message) "Spielende") (some? (game-state :opponent-to-challenge)))
    (if (game-state :auto-rematch)
      (do
        (println "Waiting to rechallange.")
        (Thread/sleep 100)
        (net/send-command network "liste"))
      (do-shutdown game-state network)))
  game-state)

(defn handle-E001-unkown-command [response game-state network]
  game-state)

(defn handle-E201-game-request-rejected-command [response game-state network]
  (do-shutdown game-state network))

(defn handle-E301-move-timeout-command [response game-state network]
  (println "Disqualifiziert weil die Antwort zu lange gedauert hat")
  game-state)

(defn handle-E302-idle-timeout-command [response game-state network]
  (net/shutdown-network network)
  game-state)

(defn handle-response [raw-response game-state network ki]
  (let [response (p/parse-response raw-response)]
    (case (response :code)
      "B" (handle-B-command response game-state network)
      "Q" (handle-Q-command response game-state network)
      "Z" (handle-Z-command response game-state network ki)
      "M" (handle-M-command response game-state network)
      "E001" (handle-E001-unkown-command response game-state network)
      "E201" (handle-E201-game-request-rejected-command response game-state network)
      "E301" (handle-E301-move-timeout-command response game-state network)
      "E302" (handle-E302-idle-timeout-command response game-state network)
      "E102" (handle-E102-nick-in-used-command response game-state network)
      :else (do (println "Unkown code" (response :code) "in Response:" (response :raw)) game-state))))

(defn print-state-changes [game-state new-game-state]
  (when (some? (new-game-state :winner))
    (when-not (= (new-game-state :board) (game-state :board))
      ;(when (some? new-game-state)
      (println "/============================================================\\")
      (println "| Wins: " (new-game-state :wins))
      (println "| Me :" (u/side-to-icon (new-game-state :own-side)) " 🔴 " (u/force-size (new-game-state :botname) 5) "|" (str/join (map #(str "🔴 (" (% :augen) ") ") (b/get-steine (new-game-state :board) "b"))))
      (println "| Opp:" (u/side-to-icon (new-game-state :opponent-side)) " 🔵 " (u/force-size (new-game-state :opponent-name) 5) "|" (str/join (map #(str "🔵 (" (% :augen) ") ") (b/get-steine (new-game-state :board) "o"))))
      (println "|------------------------------------------------------------|")
      (b/print-board (new-game-state :board))
      (println "\\============================================================/")
      )))

(defn game-loop [root-game-state network sleep ki]
  (loop [game-state root-game-state]
    (when (net/network-connected network)
      (let [raw-response (net/read-response network)
            new-game-state (handle-response raw-response game-state network ki)]
        (print-state-changes game-state new-game-state)
        (Thread/sleep sleep)
        (recur new-game-state)))))

(defn add-shutdown-hook [network]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. #(when (net/network-connected network) (net/send-command network "logout")))))

(defn start-engine
  [name opponent-to-challenge sleep host ki]
  (let [network (net/setup-network host)
        root-game-state (assoc c/initial-game-state
                          :opponent-to-challenge opponent-to-challenge
                          :botname name)]
    (add-shutdown-hook network)
    (game-loop root-game-state network sleep ki)))
