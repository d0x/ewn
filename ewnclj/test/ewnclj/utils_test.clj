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

(deftest moegliche-steine-test
  (testing "moegliche steine"
    (is (= (moegliche-steine 4 [{:augen 3}
                                {:augen 5}
                                ])
           '({:augen 5}
              {:augen 3}
              )))))

(deftest moegliche-zuege-test
  (testing "moegliche zuege für einen stein"
    (is (= (moegliche-zuege "t" {:x 4 :y 4}) '()))
    (is (= (moegliche-zuege "t" {:x 4 :y 0}) '({:x 4 :y 1})))
    (is (= (moegliche-zuege "t" {:x 0 :y 4}) '({:x 1 :y 4})))
    (is (= (moegliche-zuege "t" {:x 0 :y 0}) '({:x 1 :y 0} {:x 0 :y 1} {:x 1 :y 1})))

    (is (= (moegliche-zuege "b" {:x 0 :y 0}) '()))
    (is (= (moegliche-zuege "b" {:x 0 :y 4}) '({:x 0 :y 3})))
    (is (= (moegliche-zuege "b" {:x 4 :y 0}) '({:x 3 :y 0})))
    (is (= (moegliche-zuege "b" {:x 4 :y 4}) '({:x 3 :y 4} {:x 4 :y 3} {:x 3 :y 3})))))
