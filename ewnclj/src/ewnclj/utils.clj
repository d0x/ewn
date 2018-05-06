(ns ewnclj.utils)

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
