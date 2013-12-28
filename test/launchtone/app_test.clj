(ns launchtone.app-test
  (:use [clojure.test :only [deftest testing is]]
        [launchtone.app :only [assoc-board
                               board->colors
                               get-color
                               get-redgreen
                               make-app
                               set-spot-color!]]))

(deftest test-make-app
  (testing "Creation of an application.ck"
    (let [app @(make-app)]
      (is (contains? app :transmitter))
      (is (contains? app :receiver))
      (is (contains? app :line-in))
      (is (contains? app :line-out))
      (is (contains? app :worker-pool))
      (is (contains? app :buffer))
      (is (contains? app :board)))))

(deftest test-board->colors
  (testing "Color selection of board->colors"
    (is (= [[[0 0] [3 0]]
            [[0 3] [0 0]]]
           (board->colors [[:e :r]
                           [:g :e]])))))

(deftest test-assoc-board
  (testing "Association of a new board to an app."
    (let [app-atom (make-app)
          app-guts @app-atom
          new-board (take 8 (repeat [:e :r :g :y :e :r :g :y]))
          new-app (assoc-board app-guts new-board)]
      (is (= new-board (new-app :board))))))

(deftest test-set-spot-color!
  (testing "Setting of spot colors"
    (let [mock-app (atom {:board [[:e :e] [:e :e]]})]
      (set-spot-color! mock-app 0 0 :r)
      (is (= [[:r :e] [:e :e]]
             (@mock-app :board))))))

(deftest test-get-color
  (testing "Getting colors"
    (let [mock-app (atom {:board [[:r :g] [:e :y]]})]
      (is (= :r (get-color mock-app 0 0)))
      (is (= :g (get-color mock-app 0 1)))
      (is (= :e (get-color mock-app 1 0)))
      (is (= :y (get-color mock-app 1 1))))))


(deftest test-get-redgreen
  (testing "Getting redgreens"
    (let [mock-app (atom {:board [[:r :g] [:e :y]]})]
      (is (= [3 0] (get-redgreen mock-app 0 0)))
      (is (= [0 3] (get-redgreen mock-app 0 1)))
      (is (= [0 0] (get-redgreen mock-app 1 0)))
      (is (= [3 3] (get-redgreen mock-app 1 1))))))
