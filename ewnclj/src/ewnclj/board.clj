(ns ewnclj.board
  (:require [clojure.string :as str]
            [ewnclj.config :as c]))

(defn has-players [board]
  (some? (first (filter #(not= ewnclj.config/blank %) (flatten board)))))

(defn parse-feld [feld]
  (if (= feld c/blank)
    nil
    (let [[owner augen] (str/split feld #"")]
      {:owner owner
       :augen (Integer/parseInt augen)})))

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
  ([board who stein] (bset board (stein :x) (stein :y) (str who (stein :augen)))))

(defn bget [board x y]
  (get-in board [x y]))

(defn get-steine
  ([board] "Liefert die Steine in der From {:owner \"b\" :x 2 :y 2 :augen 4} auf Board"
   (filter #(some? (% :owner))
           (flatten
             (map-indexed (fn [y row]
                            (map-indexed (fn [x feld]
                                           (let [{owner :owner augen :augen} (parse-feld feld)]
                                             {:owner owner :x x :y y :augen augen}))
                                         row))
                          board))
           ))
  ([board owner] "Liefert die Steine eines Spielers"
   (filter #(= (% :owner) owner) (get-steine board))))

(defn find-stein [board owner augen]
  (some #(when (= (% :augen) augen) %) (get-steine board owner)))

(defn remove-stein [board stein]
  (if (some? stein)
    (bset board (stein :x) (stein :y) c/blank)
    board))

(defn move-stein [board owner stein]
  (bset (remove-stein board (find-stein board owner (stein :augen)))
        owner
        stein))
