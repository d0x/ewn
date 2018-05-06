(ns ewnclj.board
  (:require [clojure.string :as str]
            [ewnclj.config :as c]))

(defn has-players [board]
  (some? (first (filter #(not= ewnclj.config/blank %) (flatten board)))))

(defn parse-stein [stein]
  "Parses 513 to {:augen 5, :x 0, :y 2}"
  (let [[augen x y] (mapv #(Integer/parseInt %) (str/split stein #""))]
    {:augen augen :x (dec x) :y (dec y)}))

(defn is-top-half
  ([x y]
   "Gibt an ob der Punkt sich oben links befindet (die Diagonale zählt als top mit)"
   (<= (+ x y) 3))
  ([stein]
   (is-top-half (stein :x) (stein :y))))

(defn get-startaufstellung-side [steine]
  "Prüft ob die übergebene Startaufstellung oben (t) oder unten (b) angeordnet ist"
  (let [ersterStein (get steine 0)]
    (if (is-top-half (ersterStein :x) (ersterStein :y)) "t" "b")))

(defn bset
  ([board x y val] (let [new-row (assoc (get board y) x val)]
                     (assoc board y new-row)))
  ([board player stein] (bset board (stein :x) (stein :y) (stein :augen))))

(defn bget [board x y]
  (get-in board [x y]))
