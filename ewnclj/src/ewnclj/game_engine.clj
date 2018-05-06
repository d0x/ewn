(ns ewnclj.game-engine
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.ki :as ki]
            [ewnclj.parser :as p]
            [ewnclj.communication :as net]
            [ewnclj.board :as b]))

; ----- Acting to responses
(defn apply-startaufstellung [game-state]
  game-state)

(defn do-own-startaufstellung [game-state]
  (let [own-side (if (= (game-state :opponent-side) "t") "b" "t")
        aufstellung (ki/choose-startaufstellung game-state own-side)
        steine (p/parse-aufstellung aufstellung)
        new-board (reduce
                    (fn [board stein]
                      (b/bset board (stein :x) (stein :y) (str "b" (stein :augen))))
                    (game-state :board) steine)]
    (net/send-command aufstellung)
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

(defn move [game-state]
  game-state)

(defn handle-Z-command [response game-state]
  (if (= (response :sender) "Server")
    (cond
      (= (response :message) "Sie sind am Zug") game-state
      (str/starts-with? (response :message) "Zug an ") game-state
      (str/starts-with? (response :message) "Würfel:") (if (:own-side game-state)
                                                         (move game-state)
                                                         (do-own-startaufstellung game-state))
      :else (println "Unhandled Response: " (response :raw)))
    (if (game-state :opponent-side)
      (move game-state)
      (do-opponent-startaufstellung game-state (response :message)))))

(defn handle-B-command [response game-state]
  "B - success"
  (if (= (:sender response) "Server")
    (do
      (cond
        (= (:message response) "Verbindung zum Server erstellt") (net/send-command (str "login " (game-state :botname)))
        (= (:message response) (str (game-state :botname) ", Sie sind angemeldet")) (println "Waiting for game-state requests")
        (= (:message response) "Spiel startet") game-state
        (= (:message response) "disconnect") (net/shutdown-network)
        :else (net/send-command "logout"))
      game-state)
    (throw (IllegalStateException. "Not implemented"))))

(defn handle-Q-command [response game-state]
  "Akzeptiert jeden game-state Request"
  (let [opponent-name (p/parse-opponent-name (response :message))]
    (net/send-command "Ja")
    (assoc game-state :opponent-name opponent-name)))

(defn handle-E102-nick-in-used [response game-state]
  (let [new-name (ki/choose-next-nick-name (game-state :botname))]
    (net/send-command (str "login " new-name))
    (assoc game-state :botname new-name)))

(defn handle-M-command [response game-state] game-state)

(defn handle-response [raw-response game-state]
  (let [response (p/parse-response raw-response)]
    ;(println "response: " response)
    (cond
      (= (response :code) "B") (handle-B-command response game-state)
      (= (response :code) "Q") (handle-Q-command response game-state)
      (= (response :code) "Z") (handle-Z-command response game-state))
    (= (response :code) "M") (handle-M-command response game-state)
    (= (response :code) "E102") (handle-E102-nick-in-used response game-state)
    (= (response :code) "E302") (net/shutdown-network)
    ))

(defn start-engine []
  (loop [game-state c/initial-game-state]
    (if (net/network-connected)
      (let [new-game-state (handle-response (net/read-response) game-state)]
        (pp/pprint (new-game-state :board))
        (Thread/sleep 2000)
        (recur new-game-state)))))
