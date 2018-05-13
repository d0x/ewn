(ns ewnclj.ki-random
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.utils :as u]
            [ewnclj.board :as b]))

(defn- choose-move [game-state wuerfel]
  "Returns a new Stein {:augen 5 :x 3 :y 2 }"
  (let [board (game-state :board)
        own-steine (b/get-steine board "b")
        opp-steine (b/get-steine board "o")
        moegliche-steine (u/moegliche-steine wuerfel own-steine)
        moegliche-zuege (flatten (map #(u/moegliche-zuege (game-state :own-side) %) moegliche-steine))]
    (first moegliche-zuege)))

(def ki {:choose-move choose-move})
