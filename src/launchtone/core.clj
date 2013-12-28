(ns launchtone.core
  (:use [launchtone.app :only [make-app]])
  (:require [overtone.config.log :as log]))

(log/set-level! :debug)
