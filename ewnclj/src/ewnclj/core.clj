(ns ewnclj.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [ewnclj.game-engine :as g])
  (:require [ewnclj.ki-random :as ki-rnd])
  (:require [ewnclj.ki-proper :as ki-proper])
  (:gen-class))


(def cli-options
  [["-s" "--sleep MILLIS" "Timeout between moves"
    :default 1000
    :parse-fn #(Integer/parseInt %)]
   ["-o" "--opponent NAME" "Opponent that should be challanged"
    :default nil
    :parse-fn #(str %)]
   ["-n" "--name NAME" "Bot name"
    :default "cb"
    :parse-fn #(str %)]
   ["-k" "--ki NAME" "KI-Name [rnd|proper]"
    :default "rnd"
    :parse-fn #(str %)]
   ["-h" "--host HOSTNAME" "Hostname to connect"
    :default "localhost"
    :parse-fn #(str %)]
   ])

(defn -main [& args]
  (let [options ((parse-opts args cli-options) :options)]
    (println "Config" options)
    (g/start-engine
      (options :name)
      (options :opponent)
      (options :sleep)
      (options :host)
      (case (options :ki)
        "rnd" ki-rnd/ki
        "proper" ki-proper/ki
        ))
    )
  )
