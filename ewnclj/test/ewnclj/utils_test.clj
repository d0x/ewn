(ns ewnclj.utils-test
  (:require [clojure.test :refer :all])
  (:require [ewnclj.utils :refer :all]))

(deftest augen-test
  (testing "augen"
    (is (= (augen '()) ()))
    (is (= (augen '({:owner "b", :x 0, :y 0, :augen 3}
                     {:owner "b", :x 1, :y 0, :augen 2}
                     {:owner "b", :x 2, :y 0, :augen 6}
                     {:owner "b", :x 0, :y 1, :augen 5}))
           '(3 2 6 5)))))

(deftest moegliche-augen-test
  (testing "moegliche augen"
    (is (= (moegliche-augen 4 '(1 2 3 4 5 6)) '(4)))
    (is (= (moegliche-augen 4 '(1 2 3 4)) '(4)))
    (is (= (moegliche-augen 4 '(4 5 6)) '(4)))
    (is (= (moegliche-augen 4 '(1 2 3)) '(3)))
    (is (= (moegliche-augen 4 '(5 6)) '(5)))
    (is (= (moegliche-augen 4 '(1 2 3 5 6)) '(5 3)))
    (is (= (moegliche-augen 4 '()) '()))))
