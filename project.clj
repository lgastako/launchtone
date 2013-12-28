(defproject launchtone "0.1.0-SNAPSHOT"
  :description "Interface with the Launchpad from Overtone"
  :url "http://github.com/lgastako/launchtone"
  :license {:name "Public Domain"
            :url "http://creativecommons.org/publicdomain/zero/1.0/"}
  :main ^:skip-aot launchtone.demos
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [overtone "0.9.1"]
                 [org.clojure/math.combinatorics "0.0.7"]])
