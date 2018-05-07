(ns ewnclj.communication
  (:require [ewnclj.config :as c])
  (:import (java.io InputStreamReader PrintWriter BufferedReader)
           (java.net Socket)))

(def socket (new Socket c/hostname 1078))
(def out (new PrintWriter (.getOutputStream socket) true))
(def br (new BufferedReader (new InputStreamReader (.getInputStream socket))))

(defn read-response []
  (let [response (.readLine br)]
    (println "RECV: " response)
    response))

(defn send-command [message]
  (println "SEND: " message)
  (.println out message))

(defn shutdown-network []
  (println "Closing Socket...")
  (.close socket))

(defn network-connected []
  (not (.isClosed socket)))

