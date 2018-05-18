(ns ewnclj.ki-random
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.utils :as u]
            [ewnclj.board :as b]))

(defn choose-move [board player player-side wuerfel]
  "Returns a new Stein {:augen 5 :x 3 :y 2 }"
  (->> (u/moegliche-steine wuerfel (b/get-steine board player))
       (map #(u/moegliche-zuege-for-stein player-side %))
       (flatten)
       (map #(into (% :to)))
       (shuffle)
       (first)
       ))

(def ki {:choose-move choose-move})
