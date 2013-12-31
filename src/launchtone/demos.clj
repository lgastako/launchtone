(ns launchtone.demos
  (:use [overtone.at-at :only [after]]
        [launchtone.app :only [make-app set-board! get-redgreen set-spot-color! buffering-on!]]
        [launchtone.board :only [all-spots]]
        [launchtone.utils :only [debug]]))

(def xmas-tree-board
  [[:r0g0 :r0g0 :r0g0 :r0g3 :r0g3 :r0g0 :r0g0 :r0g0]
   [:r0g0 :r0g0 :r0g2 :r0g3 :r0g3 :r0g2 :r0g0 :r0g0]
   [:r0g0 :r0g0 :r0g3 :r0g3 :r0g3 :r0g3 :r0g0 :r0g0]
   [:r0g0 :r0g2 :r0g3 :r0g3 :r0g3 :r0g3 :r0g2 :r0g0]
   [:r0g0 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g0]
   [:r0g2 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g2]
   [:r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3 :r0g3]
   [:r0g0 :r0g0 :r0g0 :r3g3 :r3g3 :r0g0 :r0g0 :r0g0]])

(def smiley-board
  [[:e :e :y :y :y :y :e :e]
   [:e :y :y :y :y :y :y :e]
   [:y :y :e :y :y :e :y :y]
   [:y :y :y :y :y :y :y :y]
   [:y :e :y :y :y :y :e :y]
   [:y :y :e :y :y :e :y :y]
   [:e :y :y :e :e :y :y :e]
   [:e :e :y :y :y :y :e :e]])

(defn with-app [f]
  (let [app (make-app)]
    (f app)
    app))

(defn xmas-tree!
  ([app]
     (set-board! app xmas-tree-board))
  ([]
     (with-app xmas-tree!)))

(defn start-twinkling! [app [r c]]
  ;; A twinkle is picking red or yellow and then cycling from
  ;; the lowest intensity version of the color to the highest
  ;; then back to the lowest and then to green.  Will have to
  ;; figure out how to do the green part -- should we fade to
  ;; it or must pop it back or what?
  (debug "start-twinkling!")
  (debug "start-twinkling!")
  (let [interval 500]
    (letfn [(finish-cycle! [cycle]
              (debug "finishing cycle: " cycle)
              (if (< 0 (count cycle))
                (let [color (first cycle)
                      remaining (rest cycle)]
                  (debug "work to do, color=" color)
                  (set-spot-color! app r c color)
                  (debug "color set, sched next")
                  (debug "remaining " remaining)
    ;;              (after interval #(finish-cycle! remaining) :blah)
                  (finish-cycle! remaining)
                  ))
              (debug "no more vals in cycle"))]
      (let [cycle (if (> (rand) 0.5)
                    [:r1g3 :r2g3 :r3g3 :r2g3 :r1g3 :r0g3]
                    [:r1g2 :r2g1 :r3g0 :r2g1 :r1g2 :r0g3])]
        (debug "starting twinkle on cycle: " cycle)
  (finish-cycle! cycle)))))

(defn potential-lights
  [app]
  (let [full-green [0 3]]
    (letfn [(is-green-spot [[row col]]
              (= full-green (get-redgreen app row col)))]
      (filter is-green-spot all-spots))))

(defn random-potential-light
  [app]
  (rand-nth (potential-lights app)))

(defn animate-tree!
  ([app]
     (let [min-ms 300
           max-ms 3000
           random-interval (+ min-ms (rand-int (- max-ms min-ms)))]
       (after random-interval (start-twinkling! app (random-potential-light app)))))
  ([]
     (with-app animate-tree!)))

(defn animated-xmas-tree!
  ([app]
     (xmas-tree! app)
     (animate-tree! app)))

(defn smiley!
  ([app]
     (set-board! app smiley-board))
  ([]
     (with-app smiley!)))

(defn both!
  [app]
  (let [scenes (cycle [xmas-tree! smiley!])]
    (loop [scene! (first scenes)]
      (scene! app)
      (Thread/sleep 1000)
      (recur (rest scenes)))))

(defn start-app!
  []
  (let [app (make-app)]
;;    (buffering-on! app)
    (xmas-tree! app)
    app))

(defn -main []
  (debug "main beginning...")
  (xmas-tree!)
  (debug "main ending..."))
