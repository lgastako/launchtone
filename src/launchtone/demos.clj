(ns launchtone.demos
  (:use [launchtone.core :only [make-app set-board! get-redgreen set-spot-color! buffering-on!]]
        [launchtone.board :only [all-spots]]
        [launchtone.cron :only [every after]]
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
    ;;              (after app interval #(finish-cycle! remaining))
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

(defn scrolled-row
  [row]
;; TODO figure out idiom
;;  (conj (rest row) (first row))
  (concat (into [] (rest row)) [(first row)])
  )

(assert (= [:b :c :a] (scrolled-row [:a :b :c])))
(scrolled-row [:a :b :c])

(defn scrolled-board
  [board]
  (debug "scrolled-board..." board)
  (let [res (map scrolled-row board)]
    (debug "sb.res " res)
    res))

(defn scroll!
  [app]
  (debug "scroll! " app)
  (let [board (@app :board)]
    (debug "board " board))
  (letfn [(updater [old-app]
            (debug "in updater")
            (let [res (update-in old-app [:board] scrolled-board)]
              (debug "res " res)
              res))]
    (debug "in let")
    (swap! app updater)))

(defn scroll-smiley!
  ([app]
     ;; replace this with #(scroll! app) except that works
     (letfn [(tmp-update []
               (debug "tmp-update")
               (scroll! app))]
       (smiley! app)
       (every app 100 #(scroll! app))))
  ([]
     (with-app scroll-smiley!)))

(defn -main []
  (debug "main beginning...")
  (xmas-tree!)
  (debug "main ending..."))
