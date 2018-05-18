(ns ewnclj.ki-proper
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.utils :as u]
            [ewnclj.board :as b]))

(def config {:schachmatt   10
             :greedyness   3
             :shortest     2
             :kanibalismus 1
             })

(defn score-move [move]
  (assoc move
    :score
    (+ (* (config :greedyness) (if (move :kill) 1 0))
       (* (config :schachmatt) (if (move :schachmatt) 1 0))
       (* (config :shortest) (if (move :shortest) 1 0))
       (* (config :kanibalismus) (if (move :kanibalisch) 1 0))
       )))

(defn enrich-move [board player-side {:keys [from to] :as move}]
  (assoc move
    :kill (u/zug-is-kill board from to)
    :shortest (u/zug-is-shortes-path player-side from to)
    :schachmatt (u/zug-is-win to)
    :kanibalisch (u/zug-is-kanibalisch board from to)))

(defn choose-move [board player player-side wuerfel]
  "Returns a new Stein {:augen 5 :x 3 :y 2 }"
  (->> (u/moegliche-steine board player wuerfel)
       (map #(u/moegliche-zuege-for-stein player-side %))
       (flatten)
       (map #(enrich-move board player-side %))
       (map #(score-move %))
       (sort-by #(- (% :score)))
       (map #(into (% :to)))
       (first)
       ))

(def ki {:choose-move choose-move})
