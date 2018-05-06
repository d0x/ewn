(ns ewnclj.board-test
  (:require [clojure.test :refer :all])
  (:require [ewnclj.board :refer :all]
            [ewnclj.config :as c]))

(def board [["b3" "b2" "b6" "__" "__"]
            ["b5" "b4" "__" "__" "__"]
            ["b1" "__" "__" "__" "o1"]
            ["__" "__" "__" "o4" "o5"]
            ["__" "__" "o6" "o2" "o3"]])

(def empty-board c/initial-board)

(deftest move-test
  (testing "Move stein"
    (is (= (move-stein board "b" {:augen 4 :x 2 :y 2}) [["b3" "b2" "b6" "__" "__"]
                                                        ["b5" "__" "__" "__" "__"]
                                                        ["b1" "__" "b4" "__" "o1"]
                                                        ["__" "__" "__" "o4" "o5"]
                                                        ["__" "__" "o6" "o2" "o3"]]))))

(deftest bset-test
  (testing "set board raw"
    (is (= (bset empty-board 1 0 "test") [["__" "test" "__" "__" "__"]
                                          ["__" "__" "__" "__" "__"]
                                          ["__" "__" "__" "__" "__"]
                                          ["__" "__" "__" "__" "__"]
                                          ["__" "__" "__" "__" "__"]])))

  (testing "set board with stein"
    (is (= (bset empty-board "b" {:augen 4 :x 2 :y 2}) [["__" "__" "__" "__" "__"]
                                                        ["__" "__" "__" "__" "__"]
                                                        ["__" "__" "b4" "__" "__"]
                                                        ["__" "__" "__" "__" "__"]
                                                        ["__" "__" "__" "__" "__"]]))))

(deftest parse-feld-test
  (testing "parse feld"
    (is (nil? (parse-feld c/blank)))
    (is (= (parse-feld "b5") {:owner "b" :augen 5}))))


(deftest get-steine-test
  (testing "steine aus dem feld auslesen"
    (is (= (get-steine empty-board) ()))
    (is (= (get-steine [["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "b4" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]])
           '({:owner "b" :x 2 :y 2 :augen 4})))))

(deftest find-stein-test
  (testing "einen bestimmten stein suchen"
    (is (= (get-steine empty-board) ()))
    (is (= (find-stein [["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "b4" "__" "__"]
                        ["__" "__" "__" "__" "__"]
                        ["__" "__" "__" "__" "__"]],
                       "b" 4)
           {:owner "b" :x 2 :y 2 :augen 4}))))

(deftest remove-stein-test
  (testing "einen stein vom spielfeld nehmen"
    (is (= (remove-stein board nil) board))
    (is (= (remove-stein empty-board {:x 2 :y 2 :augen 4}) empty-board))
    (is (= (remove-stein [["__" "__" "__" "__" "__"]
                          ["__" "__" "__" "__" "__"]
                          ["__" "__" "b4" "__" "__"]
                          ["__" "__" "__" "__" "__"]
                          ["__" "__" "__" "__" "__"]],
                         {:x 2 :y 2 :augen 4})
           empty-board))))

(deftest has-steine-test
  (testing "has steine"
    (is (= (has-steine board "b") true))
    (is (= (has-steine board "o") true))
    (is (= (has-steine empty-board "b") false))
    (is (= (has-steine empty-board "o") false))))

(deftest bget-stein-test
  (testing "get stein"
    (is (= (bget-stein board 0 0) {:augen 3 :owner "b" :x 0 :y 0}))
    (is (= (bget-stein board 4 4) {:augen 3 :owner "o" :x 4 :y 4}))
    (is (= (bget-stein board 2 2) nil))))

(deftest other-corner-reached-test
  (testing "other-corner"
    (is (= (other-corner-reached board "b" "t") false))
    (is (= (other-corner-reached board "b" "b") true))
    (is (= (other-corner-reached board "o" "b") false))
    (is (= (other-corner-reached board "o" "t") true))))

