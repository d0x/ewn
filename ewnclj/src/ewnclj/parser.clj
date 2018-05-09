(ns ewnclj.parser
  (:require [clojure.string :as str]))

(defn parse-response [raw-response]
  "Zerlegt den raw-response in sender, code und message"
  (let [[full sender wuerfel code message] (re-matches #"(.*?) (?:\(Würfel: (\d)\) )?(.*?)> (.*)" raw-response)]
    {:raw     raw-response
     :sender  sender
     :wuerfel (if (nil? wuerfel) nil (Integer/parseInt wuerfel))
     :code    code
     :message message}))

(defn parse-stein [message]
  "Macht aus 522 einen Stein {:augen 5 :x 1 :y 1}"
  (let [[augen x y] (map #(Integer/parseInt %) (str/split message #""))]
    {:augen augen :x (dec x) :y (dec y)}))

(defn parse-aufstellung [aufstellung]
  "Macht aus 311 512 113 221 422 631 ein Vektor aus steinen"
  (mapv parse-stein (str/split aufstellung #" ")))

(defn parse-opponent-name [response]
  "Ließt den Namen aus: chris moechte gegen Sie spielen. o.k.? (Ja/Nein)"
  (let [[full opponent rest] (re-matches #"(.*?) (.*)" (response :message))]
    opponent))

(defn parse-player-list [response]
  "Liefert die Spieler [cb cb1] aus Server B> Folgende Spieler waeren bereit zu spielen: cb  cb1"
  (let [[full list] (re-matches #"(?:.*?:) (.*)" (response :message))]
    (if (= list "Sonst keiner da!")
      '()
      (str/split list #" "))))
