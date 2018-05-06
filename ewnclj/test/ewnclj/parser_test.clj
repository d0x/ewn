(ns ewnclj.parser-test
  (:require [clojure.test :refer :all])
  (:require [ewnclj.parser :refer [parse-response]]))

(deftest parse-response-test
  (testing "Simple message"
    (is (= (parse-response "Server Z> Würfel: 2") {:code    "Z"
                                                   :message "Würfel: 2"
                                                   :raw     "Server Z> Würfel: 2"
                                                   :sender  "Server"
                                                   :wuerfel nil})))
  (testing "Message with würfel"
    (is (= (parse-response "Server (Würfel: 2) Z> Foobar") {:code    "Z"
                                                            :message "Foobar"
                                                            :raw     "Server (Würfel: 2) Z> Foobar"
                                                            :sender  "Server"
                                                            :wuerfel 2}))))
