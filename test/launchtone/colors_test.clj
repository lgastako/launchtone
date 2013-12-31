(ns launchtone.colors-test
  (:use [clojure.test :only [deftest testing is]]
        [launchtone.colors :only [board->colors]]))

(deftest test-board->colors
  (testing "Color selection of board->colors"
    (is (= [[[0 0] [3 0]]
            [[0 3] [0 0]]]
           (board->colors [[:e :r]
                           [:g :e]])))))
