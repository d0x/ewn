(ns ewnclj.board
  (:require [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.parser :as p]))

(defn has-players [board]
  (some? (first (filter #(not= ewnclj.config/blank %) (flatten board)))))

(defn parse-feld [feld]
  (if (= feld c/blank)
    nil
    (let [[owner augen] (str/split feld #"")]
      {:owner owner
       :augen (Integer/parseInt augen)})))

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
  ([board owner stein] (bset board (stein :x) (stein :y) (str owner (stein :augen)))))

(defn bget-field [board x y]
  (get-in board [x y]))

(defn bget-stein [board x y]
  (let [feld (parse-feld (bget-field board x y))]
    (if feld
      (assoc feld :x x :y y)
      nil)))

(defn get-steine
  ([board] "Liefert die Steine in der Form {:owner \"b\" :x 2 :y 2 :augen 4} auf dem Board"
   (->> (map-indexed (fn [y row]
                       (map-indexed (fn [x feld]
                                      (let [{owner :owner augen :augen} (parse-feld feld)]
                                        {:owner owner :x x :y y :augen augen}))
                                    row))
                     board)
        (flatten)
        (filter #(some? (% :owner)))))
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

(defn has-steine [board owner]
  (not (empty? (get-steine board owner))))

(defn other-corner-reached [board who start-side]
  (if (= start-side "t")
    (let [stein (bget-stein board 4 4)]
      (if (some? stein)
        (= (stein :owner) who)
        false))
    (let [stein (bget-stein board 0 0)]
      (if stein
        (= (stein :owner) who)
        false))
    ))

(defn get-winner [board bot-side opp-side]
  (cond
    (not (has-steine board "b")) "o"
    (not (has-steine board "o")) "b"
    (other-corner-reached board "b" bot-side) "b"
    (other-corner-reached board "o" opp-side) "o"))

(def replacements {#"b"  "🔴"
                   #"o"  "🔵"
                   #"__" "⚪ (_)"
                   #"\d" " ($0)"
                   })

(defn print-board [board]
  (doseq [row board]
    (doseq [field row]
      (print (reduce #(apply str/replace %1 %2) field replacements) " "))
    (println)))
