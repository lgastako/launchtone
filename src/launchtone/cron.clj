(ns launchtone.cron
  (:require [overtone.at-at :as at-at]))

(defn every [app ms f]
  (println "every - worker pool" (@app :worker-pool))
  (at-at/every ms f (@app :worker-pool)))

(defn after [app ms f]
  (at-at/after ms f (@app :worker-pool)))
