# launchtone

A Clojure library for interacting with the Launchpad S from Overtone.

## Installation

Add the following to the dependencies in your project.clj:

```clojure
[launchtone "0.1.0"]
```

## Usage

```clojure
(ns example
   (:use [launchtone.core :only [make-app set-color!]]
         [launchtone.demos :only [xmas-tree!]))

(def app (make-app))

(set-color! app 0 0 :r)

(xmas-tree! app)
```

## License

Public Domain.