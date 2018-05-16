(ns ewnclj.utils
  (:require [ewnclj.board :as b]
            [clojure.string :as str]))

(defn augen [steine]
  (map #(% :augen) steine))

(defn moegliche-augen [wuerfel augen]
  (let [res (some #{wuerfel} augen)]
    (if (some? res)
      [res]
      ; TODO: Statt sort und filter besser in eine Map gruppiern
      (let [sorted-augen (sort < augen)
            next-higher (last (filter #(> wuerfel %) sorted-augen))
            next-smaller (first (filter #(< wuerfel %) sorted-augen))]
        (filter some? [next-smaller next-higher])
        ))))

(defn find-first [f coll]
  (first (filter f coll)))

(defn find-stein [augen steine]
  (find-first #(= augen (% :augen)) steine))

(defn moegliche-steine [wuerfel steine]
  (let [moegliche-augen (moegliche-augen wuerfel (augen steine))]
    (map #(find-stein % steine) moegliche-augen)))

(defn- moegliche-zuege-top-to-bottom [punkt]
  (let [x (punkt :x)
        y (punkt :y)
        right-ok (< x 4)
        bottom-ok (< y 4)
        diagonal-ok (and right-ok bottom-ok)]
    (filter some?
            [(if right-ok (assoc punkt :x (inc x)) nil)
             (if bottom-ok (assoc punkt :y (inc y)) nil)
             (if diagonal-ok (assoc punkt :x (inc x) :y (inc y)) nil)]
            )
    )
  )

(defn- moegliche-zuege-bottom-to-top [punkt]
  (let [x (punkt :x)
        y (punkt :y)
        left-ok (> x 0)
        up-ok (> y 0)
        diagonal-ok (and left-ok up-ok)]
    (filter some?
            [(if left-ok (assoc punkt :x (dec x)) nil)
             (if up-ok (assoc punkt :y (dec y)) nil)
             (if diagonal-ok (assoc punkt :x (dec x) :y (dec y)) nil)]
            )))

(defn moegliche-zug-ziele [root punkt]
  (if (= root "t")
    (moegliche-zuege-top-to-bottom punkt)
    (moegliche-zuege-bottom-to-top punkt)))

(defn moegliche-zuege-for-stein [root stein]
  "Berechnet die Zuege in der Form: '({:from {:x 3, :y 3}, :to {:x 4, :y 3}}...)"
  (->> (moegliche-zug-ziele root stein)
       (map #(into {:from stein :to %}))))

(defn moegliche-zuege [root steine]
  "Berechnet die Zuege in der Form: '({:from {:x 3, :y 3}, :to {:x 4, :y 3}}...)"
  (->> steine
       (map (fn [stein] (->> (moegliche-zug-ziele root stein)
                             (map #(into {:from stein :to %})))))
       flatten))

(defn zug-is-kanibalisch [board from to]
  (->> [from to]
       (map #(b/bget-stein board (% :x) (% :y)))
       (map #(if (= nil %) "none" (% :owner)))
       (apply =)
       ))

(defn zug-is-win [to]
  (or (= (to :x) (to :y) 0)
      (= (to :x) (to :y) 4)))

(defn zug-is-kill [board from to]
  (let [owners (->> [from to]
                    (map #(b/bget-stein board (% :x) (% :y)))
                    (map #(if (= nil %) nil (% :owner)))
                    )]
    (if (some nil? owners)
      (apply not= owners)
      false)
    ))

(defn stein-direct-enemy-count [board root player punkt]
  (->> (moegliche-zug-ziele root punkt)
       (map #(b/bget-stein board (% :x) (% :y)))
       (filter some?)
       (map #(% :owner))
       (map #(not= % player))
       (filter true?)
       (count)
       ))

(defn zug-is-diagonal [from to]
  (and (not= (from :x) (to :x))
       (not= (from :y) (to :y))))

(defn stein-is-on-diagonale [{:keys [x y]}] (= x y))

(defn stein-is-obere-haelfte [{:keys [x y]}] (> x y))

(defn stein-is-untere-haelfte [{:keys [x y]}] (< x y))

(defn stein-is-spielfeld-rand [root {:keys [x y]}]
  (case root
    "b" (or (= x 0) (= y 0))
    "t" (or (= x 4) (= y 4))))

(defn zug-is-shortes-path [root from to]
  (if (zug-is-diagonal from to)
    true
    (if (stein-is-on-diagonale from)
      false
      (if (stein-is-obere-haelfte from)
        (case root
          "b" (> (from :x) (to :x))
          "t" (< (from :y) (to :y))
          )
        (case root
          "b" (> (from :y) (to :y))
          "t" (< (from :x) (to :x))
          )
        ))
    ))

(defn side-to-icon [side]
  (case side
    nil " "
    "t" "↘️"
    "b" "↖️"))

(defn trunc [s n]
  (subs s 0 (min (count s) n)))

(defn force-size [string max]
  (let [trunced (trunc string max)]
    (str trunced (str/join (map (fn [x] " ") (range (count trunced) max))))))
