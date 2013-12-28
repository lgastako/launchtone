(ns launchtone.devices
  (:use [overtone.live :only [midi-find-connected-device
                              midi-find-connected-devices
                              midi-find-connected-receiver]]
        [overtone.midi :only [midi-in midi-out]]
        [launchtone.utils :only [debug]])
  (:require [overtone.config.log :as log])
  (:import (javax.sound.midi ShortMessage)))

(def ^:private lp-regex "Launchpad")

;; (defn no-transmitter-and-receiver []
;;   (throw (Exception. "No Launchpads found.")))

(defn only-transmitter-and-receiver []
  ((juxt midi-find-connected-device
         midi-find-connected-receiver) lp-regex))

;; (defn choose-transmitter-and-receiver []
;;   ((juxt midi-in midi-out)))

;; (defn select-transmitter-and-receiver
;;   []
;;   (log/debug "select-transmitter-and-receiver")
;;   (let [num-launchpads (midi-find-connected-devices lp-regex)]
;;     (log/debug "Found " num-launchpads " Launchpad devices.")
;;     (cond (= 0 num-launchpads) (no-transmitter-and-receiver)
;;           (= 1 num-launchpads) (only-transmitter-and-receiver)
;;           :else (choose-transmitter-and-receiver))))


(def select-transmitter-and-receiver only-transmitter-and-receiver)
