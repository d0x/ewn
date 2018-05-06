(ns ewnclj.parser
  (:require [ewnclj.board :as b]
            [clojure.string :as str]))

(defn parse-response [raw-response]
  "Zerlegt den raw-response in sender, code und message"
  (let [[full sender wuerfel code message] (re-matches #"(.*?) (.Würfel:.* )?(.*?)> (.*)" raw-response)]
    {:raw raw-response :sender sender :wuerfel wuerfel :code code :message message}))

(defn parse-aufstellung [aufstellung]
  "Macht aus 311 512 113 221 422 631 ein Vektor aus steinen"
  (mapv b/parse-stein (str/split aufstellung #" ")))

(defn parse-opponent-name [response]
  "Ließt den Namen aus: chris moechte gegen Sie spielen. o.k.? (Ja/Nein)"
  (let [[full opponent rest] (re-matches #"(.*?) (.*)" (response :message))]
    opponent))
