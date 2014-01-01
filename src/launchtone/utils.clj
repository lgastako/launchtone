(ns launchtone.utils
  (:require [overtone.config.log :as log]))

(defn debug [& args]
  (let [print-debugging false]
    (if print-debugging
      (apply println args)
      (apply log/debug args))))

(defn enumerate
  "Emulates python's enumerate."
  [xs]
  (map-indexed vector xs))

(def set-level! log/set-level!)
