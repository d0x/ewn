(ns ewnclj.communication
  (:require [ewnclj.config :as c])
  (:import (java.io InputStreamReader PrintWriter BufferedReader)
           (java.net Socket)))

(defn read-response [network]
  (let [response (.readLine (network :br))]
    (println "RECV: " response)
    response))

(defn send-command [network message]
  (println "SEND: " message)
  (.println (network :out) message))

(defn shutdown-network [network]
  (println "Closing Socket...")
  (.close (network :socket)))

(defn network-connected [network]
  (not (.isClosed (network :socket))))

(defn setup-network [host]
  (let [socket (new Socket host 1078)
        out (new PrintWriter (.getOutputStream socket) true)
        br (new BufferedReader (new InputStreamReader (.getInputStream socket)))]
    {:socket socket
     :out    out
     :br     br}
    )
  )
