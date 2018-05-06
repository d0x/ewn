(ns ewnclj.utils
  (:require [ewnclj.board :as b]))

(defn augen [steine]
  (map #(% :augen) steine))

(defn moegliche-augen [wuerfel augen]
  (println "Wuerfel: " wuerfel " Augen " augen)
  (let [res (some #{wuerfel} augen)]
    (if (some? res)
      [res]
      ; TODO: Statt sort und filter besser in eine Map gruppiern
      (let [sorted-augen (sort < augen)
            next-higher (last (filter #(> wuerfel %) sorted-augen))
            next-smaller (first (filter #(< wuerfel %) sorted-augen))]
        (filter some? [next-smaller next-higher])
        ))))

(defn find-first
  [f coll]
  (first (filter f coll)))

(defn find-stein [augen steine]
  (find-first #(= augen (% :augen)) steine))

(defn moegliche-steine [wuerfel steine]
  (let [moegliche-augen (moegliche-augen wuerfel (augen steine))]
    (map #(find-stein % steine) moegliche-augen)))

(defn- moegliche-zeuge-top-to-bottom [x y stein]
  (let [right-ok (< x 4)
        bottom-ok (< y 4)
        diagonal-ok (and right-ok bottom-ok)]
    (filter some?
            [(if right-ok (assoc stein :x (inc x)) nil)
             (if bottom-ok (assoc stein :y (inc y)) nil)
             (if diagonal-ok (assoc stein :x (inc x) :y (inc y)) nil)]
            )
    )
  )

(defn- moegliche-zeuge-bottom-to-top [x y stein]
  (let [left-ok (> x 0)
        up-ok (> y 0)
        diagonal-ok (and left-ok up-ok)]
    (filter some?
            [(if left-ok (assoc stein :x (dec x)) nil)
             (if up-ok (assoc stein :y (dec y)) nil)
             (if diagonal-ok (assoc stein :x (dec x) :y (dec y)) nil)]
            )))

(defn moegliche-zuege [root stein]
  (let [{x :x y :y} stein]
    (if (= root "t")
      (moegliche-zeuge-top-to-bottom x y stein)
      (moegliche-zeuge-bottom-to-top x y stein))))
