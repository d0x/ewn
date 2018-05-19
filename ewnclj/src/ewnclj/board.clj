(ns ewnclj.board
  (:require [clojure.string :as str]
            [ewnclj.config :as c]
            [ewnclj.parser :as p]
            [clojure.set :refer [map-invert]]))

(defn has-players [board]
  (some? (first (filter #(not= ewnclj.config/blank %) (flatten board)))))

(defn is-top-half
  ([x y]
   "Gibt an ob der Punkt sich oben links befindet (die Diagonale zählt als top mit)"
   (<= (+ x y) 3))
  ([{:keys [x y]}]
   (is-top-half x y)))

(defn get-startaufstellung-side [{erster-stein 0}]
  "Prüft ob die übergebene Startaufstellung oben (↘️) oder unten (↖️) angeordnet ist"
  (if (is-top-half (erster-stein :x) (erster-stein :y)) "↘️" "↖️"))

(def board-player-mapping {:bot      "b"
                           :opponent "o"})
(def board-value-mapping (map-invert board-player-mapping))

(defn bget-field-value [board x y]
  (get-in board [y x]))

(defn bset-field-value [board x y val]
  (assoc-in board [y x] val))

(defn parse-field-value [field-value]
  (if (= field-value c/blank)
    nil
    (let [[owner augen] (str/split field-value #"")]
      {:owner (board-value-mapping owner)
       :augen (Integer/parseInt augen)})))


(defn bset
  ([board owner augen x y] (bset-field-value board x y (str (board-player-mapping owner) augen)))
  ([board owner {:keys [x y augen] :as stein}] (bset board owner augen x y)))

(defn bget [board x y]
  (let [feld (parse-field-value (bget-field-value board x y))]
    (if feld
      (assoc feld :x x :y y)
      nil)))

(defn get-steine
  ([board] "Liefert die Steine in der Form {:owner \"b\" :x 2 :y 2 :augen 4} auf dem Board"
   (->> (map-indexed (fn [y row]
                       (map-indexed (fn [x feld]
                                      (assoc (parse-field-value feld) :x x :y y))
                                    row))
                     board)
        (flatten)
        (filter #(some? (% :owner)))))
  ([board owner] "Liefert die Steine eines Spielers"
   (filter #(= (% :owner) owner) (get-steine board))))

(defn find-stein [board owner augen]
  (some #(when (= (% :augen) augen) %) (get-steine board owner)))

(defn remove-punkt [board {:keys [x y] :as punkt}]
  (if (some? punkt)
    (bset-field-value board x y c/blank)
    board))

(defn move-stein [board from to]
  "Setzt den wert von from auf to"
  (let [from-value (bget-field-value board (from :x) (from :y))
        temp-board (bset-field-value board (to :x) (to :y) from-value)
        new-board (remove-punkt temp-board from)]
    new-board
    ))

(defn place-stein [board owner augen x y]
  "Entfernt den stein von der alten position und setzt ihn auf die neue"
  (let [alter-stein (find-stein board owner augen)
        board-ohne-alten-stein (remove-punkt board alter-stein)]
    (bset board-ohne-alten-stein owner augen x y)))

(defn has-steine [board owner]
  (not (empty? (get-steine board owner))))

(defn other-corner-reached [board who start-side]
  (if (= start-side "↘️")
    (let [stein (bget board 4 4)]
      (if (some? stein)
        (= (stein :owner) who)
        false))
    (let [stein (bget board 0 0)]
      (if stein
        (= (stein :owner) who)
        false))
    ))

(defn get-winner [board bot-side opp-side]
  (cond
    (not (has-steine board :bot)) :opponent
    (not (has-steine board :opponent)) :bot
    (other-corner-reached board :bot bot-side) :bot
    (other-corner-reached board :opponent opp-side) :opponent))

(def replacements {#"b"  "🔴"
                   #"o"  "🔵"
                   #"__" "⚪ (_)"
                   #"\d" " ($0)"
                   })

(defn print-board [board]
  (doseq [row board]
    (print "| ")
    (doseq [field row]
      (print (reduce #(apply str/replace %1 %2) field replacements) " "))
    (println)))
