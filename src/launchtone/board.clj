(ns launchtone.board
  (:use [clojure.math.combinatorics :only [cartesian-product]]))

(def all-spots (cartesian-product
                (range 8)
                (range 8)))

(def empty-row (into [] (take 8 (repeat :e))))
(def empty-board (into [] (take 8 (repeat empty-row))))

