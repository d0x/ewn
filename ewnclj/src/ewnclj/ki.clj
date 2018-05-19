(ns ewnclj.ki
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.utils :as u]
            [ewnclj.board :as b]))

(defn choose-next-nick-name [name]
  "Zählt am Namen die hintere Zahl hoch. Also cb, cb1, cb2, cb3, ..."
  (str c/initial-bot-name
       (inc (Integer/parseInt (or (re-find #"\d+" name) "0")))))

(defn choose-startaufstellung [game-state own-side]
  (if (= own-side "↘️")
    c/top-player-setup
    c/bot-player-setup))

;(defn choose-move [game-state wuerfel]
;  "Returns a new Stein {:augen 5 :x 3 :y 2 }"
;  (let [board (game-state :board)
;        own-steine (b/get-steine board "b")
;        opp-steine (b/get-steine board "o")
;        moegliche-steine (u/moegliche-steine wuerfel own-steine)
;        moegliche-zuege (flatten (map #(u/moegliche-zuege (game-state :own-side) %) moegliche-steine))]
;    (first moegliche-zuege)))
