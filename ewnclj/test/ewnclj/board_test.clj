(ns ewnclj.board-test
  (:require [clojure.test :refer :all])
  (:require [ewnclj.board :refer :all]
            [ewnclj.config :as c]
            [clojure.string :as str]))

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

(def empty-board c/initial-board)

(deftest place-stein-test
  (testing "Move stein"
    (is (= (place-stein board :bot 4 2 2) [["b3" "b2" "b6" "__" "__"]
                                           ["b5" "__" "__" "__" "__"]
                                           ["b1" "__" "b4" "__" "o1"]
                                           ["__" "__" "__" "o4" "o5"]
                                           ["__" "__" "o6" "o2" "o3"]]))))

(deftest bset-test
  (testing "set board raw"
    (is (= (bset-field-value empty-board 1 0 "test") [["__" "test" "__" "__" "__"]
                                                      ["__" "__" "__" "__" "__"]
                                                      ["__" "__" "__" "__" "__"]
                                                      ["__" "__" "__" "__" "__"]
                                                      ["__" "__" "__" "__" "__"]])))
  (is (= (bset empty-board :bot {:augen 4 :x 2 :y 2}) [["__" "__" "__" "__" "__"]
                                                       ["__" "__" "__" "__" "__"]
                                                       ["__" "__" "b4" "__" "__"]
                                                       ["__" "__" "__" "__" "__"]
                                                       ["__" "__" "__" "__" "__"]])))

(deftest parse-feld-test
  (testing "parse feld"
    (is (nil? (parse-field-value c/blank)))
    (is (= (parse-field-value "b5") {:owner :bot :augen 5}))
    (is (= (parse-field-value "o2") {:owner :opponent :augen 2}))))


(deftest get-steine-test
  (testing "steine aus dem feld auslesen"
    (is (= (get-steine empty-board) ()))
    (is (= (get-steine [["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "b4" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]])
           '({:owner :bot :x 2 :y 2 :augen 4})))))

(deftest find-stein-test
  (testing "einen bestimmten stein suchen"
    (is (= (get-steine empty-board) ()))
    (is (= (find-stein [["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "b4" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]],
                       :bot 4)
           {:owner :bot :x 2 :y 2 :augen 4}))))

(deftest remove-stein-test
  (testing "einen stein vom spielfeld nehmen"
    (is (= (remove-punkt board nil) board))
    (is (= (remove-punkt empty-board {:x 2 :y 2 :augen 4}) empty-board))
    (is (= (remove-punkt [["__" "__" "__" "__" "__"]
                          ["__" "__" "__" "__" "__"]
                          ["__" "__" "b4" "__" "__"]
                          ["__" "__" "__" "__" "__"]
                          ["__" "__" "__" "__" "__"]],
                         {:x 2 :y 2 :augen 4})
           empty-board))))

(deftest has-steine-test
  (testing "has steine"
    (is (= (has-steine board :bot) true))
    (is (= (has-steine board :opponent) true))
    (is (= (has-steine empty-board :bot) false))
    (is (= (has-steine empty-board :opponent) false))))

(deftest bget-field-value-test
  (testing
    (is (= (bget-field-value close-board 2 2) "o1"))
    (is (= (bget-field-value close-board 2 3) "o4"))))

(deftest bget-stein-test
  (testing "get stein"
    (is (= (bget close-board 2 3) {:augen 4 :owner :opponent :x 2 :y 3}))
    (is (= (bget board 0 0) {:augen 3 :owner :bot :x 0 :y 0}))
    (is (= (bget board 4 4) {:augen 3 :owner :opponent :x 4 :y 4}))
    (is (= (bget board 2 2) nil))))

(deftest other-corner-reached-test
  (testing "other-corner"
    (is (= (other-corner-reached board :bot "↘️") false))
    (is (= (other-corner-reached board :bot :bot) true))
    (is (= (other-corner-reached board :opponent :bot) false))
    (is (= (other-corner-reached board :opponent "↘️") true))))

(deftest print-board-test
  (testing "printboard"
    (is (= (print-board board)))))


(deftest move-stein-test
  (testing
    (is (= (move-stein board {:x 0 :y 0} {:x 2 :y 2}) [["__" "b2" "b6" "__" "__"]
                                                       ["b5" "b4" "__" "__" "__"]
                                                       ["b1" "__" "b3" "__" "o1"]
                                                       ["__" "__" "__" "o4" "o5"]
                                                       ["__" "__" "o6" "o2" "o3"]]))
    (is (= (move-stein board {:x 0 :y 0} {:x 1 :y 1}) [["__" "b2" "b6" "__" "__"]
                                                       ["b5" "b3" "__" "__" "__"]
                                                       ["b1" "__" "__" "__" "o1"]
                                                       ["__" "__" "__" "o4" "o5"]
                                                       ["__" "__" "o6" "o2" "o3"]]))))

