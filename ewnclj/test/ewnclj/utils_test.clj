(ns ewnclj.utils-test
  (:require [clojure.test :refer :all])
  (:require [ewnclj.utils :refer :all]))


(def board [["b3" "b2" "b6" "__" "__"]
            ["b5" "b4" "__" "__" "__"]
            ["b1" "__" "__" "__" "o1"]
            ["__" "__" "__" "o4" "o5"]
            ["__" "__" "o6" "o2" "o3"]])

(def close-board [["__" "b2" "b6" "__" "__"]
                  ["__" "b5" "b4" "b3" "__"]
                  ["__" "b1" "o1" "__" "__"]
                  ["__" "__" "o4" "o5" "__"]
                  ["__" "__" "o6" "o2" "o3"]])

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
    (is (= (moegliche-steine 4 [{:augen 3} {:augen 5}]) '({:augen 5} {:augen 3})))))

(deftest moegliche-zug-ziele-test
  (testing "moegliche zuege für einen stein"
    (is (= (moegliche-zug-ziele "t" {:x 4 :y 4}) '()))
    (is (= (moegliche-zug-ziele "t" {:x 4 :y 0}) '({:x 4 :y 1})))
    (is (= (moegliche-zug-ziele "t" {:x 0 :y 4}) '({:x 1 :y 4})))
    (is (= (moegliche-zug-ziele "t" {:x 0 :y 0}) '({:x 1 :y 0} {:x 0 :y 1} {:x 1 :y 1})))

    (is (= (moegliche-zug-ziele "b" {:x 0 :y 0}) '()))
    (is (= (moegliche-zug-ziele "b" {:x 0 :y 4}) '({:x 0 :y 3})))
    (is (= (moegliche-zug-ziele "b" {:x 4 :y 0}) '({:x 3 :y 0})))
    (is (= (moegliche-zug-ziele "b" {:x 4 :y 4}) '({:x 3 :y 4} {:x 4 :y 3} {:x 3 :y 3})))))

(deftest moegliche-zuege-test
  (testing "moegliche zuege für einen stein"
    (is (= (moegliche-zuege "t" [{:x 3, :y 3} {:x 2, :y 2}]) '({:from {:x 3, :y 3}, :to {:x 4, :y 3}}
                                                                {:from {:x 3, :y 3}, :to {:x 3, :y 4}}
                                                                {:from {:x 3, :y 3}, :to {:x 4, :y 4}}
                                                                {:from {:x 2, :y 2}, :to {:x 3, :y 2}}
                                                                {:from {:x 2, :y 2}, :to {:x 2, :y 3}}
                                                                {:from {:x 2, :y 2}, :to {:x 3, :y 3}})))))

(deftest zug-is-diagonal-test
  (testing "Testes ob ein Zug diagonal ist"
    (is (= (zug-is-diagonal {:x 4 :y 4} {:x 3 :y 3}) true))
    (is (= (zug-is-diagonal {:x 2 :y 2} {:x 3 :y 3}) true))
    (is (= (zug-is-diagonal {:x 4 :y 4} {:x 3 :y 4}) false))
    (is (= (zug-is-diagonal {:x 4 :y 4} {:x 4 :y 3}) false))
    (is (= (zug-is-diagonal {:x 4 :y 4} {:x 4 :y 4}) false))))

(deftest zug-is-kanibalisch-test
  (testing
    (is (= (zug-is-kanibalisch close-board {:x 2 :y 2} {:x 1 :y 2}) false))
    (is (= (zug-is-kanibalisch close-board {:x 2 :y 1} {:x 2 :y 2}) false))
    (is (= (zug-is-kanibalisch board {:x 0 :y 0} {:x 1 :y 1}) true))
    (is (= (zug-is-kanibalisch board {:x 0 :y 0} {:x 4 :y 4}) false))
    (is (= (zug-is-kanibalisch board {:x 0 :y 0} {:x 2 :y 2}) false))
    ))
(deftest zug-is-kill-test
  (testing
    (is (= (zug-is-kill close-board {:x 2 :y 2} {:x 1 :y 2}) true))
    (is (= (zug-is-kill close-board {:x 2 :y 3} {:x 3 :y 3}) false))
    (is (= (zug-is-kill board {:x 0 :y 0} {:x 1 :y 1}) false))
    (is (= (zug-is-kill board {:x 0 :y 0} {:x 4 :y 4}) true))
    (is (= (zug-is-kill board {:x 0 :y 0} {:x 2 :y 2}) false))))

(deftest zug-is-win-test
  (testing
    (is (= (zug-is-win {:x 0 :y 0}) true))
    (is (= (zug-is-win {:x 4 :y 4}) true))
    (is (= (zug-is-win {:x 2 :y 4}) false))))

(deftest zug-is-shortes-path-test
  (testing
    (is (= (zug-is-shortes-path "b" {:x 4 :y 4} {:x 3 :y 3}) true))
    (is (= (zug-is-shortes-path "b" {:x 4 :y 0} {:x 3 :y 0}) true))
    (is (= (zug-is-shortes-path "b" {:x 4 :y 4} {:x 3 :y 4}) false))
    (is (= (zug-is-shortes-path "b" {:x 4 :y 4} {:x 4 :y 3}) false))
    (is (= (zug-is-shortes-path "b" {:x 3 :y 4} {:x 2 :y 3}) true))
    (is (= (zug-is-shortes-path "b" {:x 3 :y 4} {:x 3 :y 3}) true))
    (is (= (zug-is-shortes-path "b" {:x 2 :y 2} {:x 2 :y 1}) false))
    (is (= (zug-is-shortes-path "b" {:x 4 :y 2} {:x 4 :y 1}) false))
    (is (= (zug-is-shortes-path "b" {:x 0 :y 2} {:x 0 :y 1}) true))
    (is (= (zug-is-shortes-path "b" {:x 2 :y 0} {:x 1 :y 0}) true))
    (is (= (zug-is-shortes-path "t" {:x 2 :y 2} {:x 3 :y 3}) true))

    (is (= (zug-is-shortes-path "t" {:x 1 :y 2} {:x 2 :y 2}) true))
    (is (= (zug-is-shortes-path "t" {:x 1 :y 2} {:x 1 :y 3}) false))
    (is (= (zug-is-shortes-path "t" {:x 1 :y 4} {:x 2 :y 4}) true))

    (is (= (zug-is-shortes-path "t" {:x 2 :y 4} {:x 3 :y 4}) true))))

(deftest stein-is-obere-haelfte-test
  (testing
    (is (= (stein-is-obere-haelfte {:x 4 :y 4}) false))
    (is (= (stein-is-obere-haelfte {:x 4 :y 2}) true))
    (is (= (stein-is-obere-haelfte {:x 4 :y 0}) true))
    (is (= (stein-is-obere-haelfte {:x 3 :y 4}) false))))

(deftest stein-is-untere-haelfte-test
  (testing
    (is (= (stein-is-untere-haelfte {:x 4 :y 4}) false))
    (is (= (stein-is-untere-haelfte {:x 4 :y 2}) false))
    (is (= (stein-is-untere-haelfte {:x 4 :y 0}) false))
    (is (= (stein-is-untere-haelfte {:x 3 :y 4}) true))))

(deftest stein-direct-enemy-count-test
  (testing
    (is (= (stein-direct-enemy-count board "b" "o" {:x 1 :y 1}) 3))
    (is (= (stein-direct-enemy-count board "b" "o" {:x 0 :y 1}) 1))
    (is (= (stein-direct-enemy-count board "b" "o" {:x 1 :y 0}) 1))
    (is (= (stein-direct-enemy-count board "b" "o" {:x 4 :y 4}) 0))
    ))

(deftest side-to-icon-test
  (testing
    (is (= (side-to-icon nil) " "))))
