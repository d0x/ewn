(ns ewnclj.ki
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.board :as b]))

(defn choose-next-nick-name [name]
  "Zählt am Namen die hintere Zahl hoch. Also cb, cb1, cb2, cb3, ..."
  (str c/initial-bot-name
       (inc (Integer/parseInt (or (re-find #"\d+" name) "0")))))

(defn choose-startaufstellung [game-state own-side]
  (if (= own-side "t")
    c/top-player-setup
    c/bot-player-setup)
  )

