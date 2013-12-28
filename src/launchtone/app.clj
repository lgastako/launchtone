(ns launchtone.app
  (:use [overtone.live :only [midi-note-on midi-mk-full-device-key on-event]]
        [overtone.at-at :only [mk-pool]]
        [clojure.math.combinatorics :only [cartesian-product]]
        [launchtone.launchpad :only [brightness-off brightness-min brightness-mid brightness-max
                                     point->note note->point colors->velocity]]
        [launchtone.devices :only [select-transmitter-and-receiver]]
        [launchtone.buffering :only [buffering-on?]]
        [launchtone.utils :only [enumerate debug set-level!]]))

(def all-spots (cartesian-product
                (range 8)
                (range 8)))

(def empty-row (into [] (take 8 (repeat :e))))
(def empty-board (into [] (take 8 (repeat empty-row))))

(def color-map {:e [brightness-off brightness-off]
                :r [brightness-max brightness-off]
                :g [brightness-off brightness-max]
                :y [brightness-max brightness-max]
                :r0g0 [brightness-off brightness-off]
                :r0g1 [brightness-off brightness-min]
                :r0g2 [brightness-off brightness-mid]
                :r0g3 [brightness-off brightness-max]
                :r1g0 [brightness-min brightness-off]
                :r1g1 [brightness-min brightness-min]
                :r1g2 [brightness-min brightness-mid]
                :r1g3 [brightness-min brightness-max]
                :r2g0 [brightness-mid brightness-off]
                :r2g1 [brightness-mid brightness-min]
                :r2g2 [brightness-mid brightness-mid]
                :r2g3 [brightness-mid brightness-max]
                :r3g0 [brightness-max brightness-off]
                :r3g1 [brightness-max brightness-min]
                :r3g2 [brightness-max brightness-mid]
                :r3g3 [brightness-max brightness-max]
                })

(defn board->colors [board]
  (into [] (map #(into [] (map color-map %)) board)))

(defn send-redgreen!
  "Render the given red/green values to the row/col of receiver."
  [receiver row col red green]
  (let [note (point->note col row)
        velocity (colors->velocity red green)]
    (midi-note-on receiver note velocity)))

(defn send-color!
  "Render the given color value to the row/col of receiver."
  [receiver row col color]
  (let [[red green] (color-map color)]
    (send-redgreen! receiver row col red green)))

(defn send-board!
  "Render the given board to the given reciever."
  [receiver board]
  (debug "Sending board: " board)
  (let [board-colors (board->colors board)]
    (debug "board colors " board-colors)
    (doseq [[r row-colors] (enumerate board-colors)
            [c [red green]] (enumerate row-colors)]
      (debug "board.send: r=" r ", c=" c ", red=" red ", green=" green ", receiver=" receiver)
      (send-redgreen! receiver r c red green))))

(defn- render-board
  [[old-app new-app]]
  (debug "rendering board")
  (let [old-board (old-app :board)
        new-board (new-app :board)
        new-receiver (new-app :receiver)]
    (when (not= old-board new-board)
      (send-board! new-receiver new-board)
      (when (buffering-on? old-app)
        (debug "sending flip buffer for old app!")
        (send-flip-buffer! xyzzy)))))

(defn- render-buffer
  [[old-app new-app]]
  (debug "rendering buffer")
  (let [old-buffer (old-app :buffer)
        new-buffer (new-app :buffer)]
    (when (not= old-buffer new-buffer)
      (debug "changing buffer from " old-buffer " to " new-buffer)
      (send-buffer-msg! (new-app :receiver) new-buffer))))

(defn- render-app
  [key ref old-app new-app]
  (debug "Rendering app.")
  ;; I think we always want to render first then update buffer status but there
  ;; may be some undesirable interplay here.
  (-> [old-app new-app]
      (render-board)
      (render-buffer)))

(defn buffering-on! [app]
  (swap! app #(assoc % :buffer buffer-0)))

(defn buffering-off! [app]
  (swap! app #(assoc % :buffer buffer-none)))

(defn make-app
  ([] (make-app :app))
  ([key] (let [[transmitter receiver] (select-transmitter-and-receiver)]
           (make-app key transmitter receiver)))
  ([key transmitter receiver]
     (let [app (atom {:transmitter transmitter
                      :receiver receiver
                      :line-in (midi-mk-full-device-key transmitter)
                      :line-out (midi-mk-full-device-key receiver)
                      :worker-pool (mk-pool)
                      :buffer buffer-none
                      :board empty-board})]
       (add-watch app key render-app)
       app)))

(def ^:private primary-app (atom nil))

(defn get-app
  []
  (if @primary-app
    @primary-app
    (let [new-app (make-app)]
      (reset! primary-app new-app)
      new-app)))

(defn assoc-board [app board]
  (assoc app :board board))

(defn set-board! [app board]
  (swap! app assoc-board board))

(defn set-spot-color! [app r c color]
  (let [board (@app :board)
        row (board r)
        new-row (assoc row c color)
        new-board (assoc board r new-row)]
    (swap! app assoc-board new-board)))

(defn handle-button-event [app f event-type]
  (fn [m]
    (let [[row col] (note->point (m :note))]
      (f row col event-type m))))

(defn on-button-down [app f]
  (on-event [:midi :note-on] (handle-button-event app f :down)))

(defn on-button-up [app f]
  (on-event [:midi :note-off] (handle-button-event app f :up)))

(def on-button on-button-up)

(defn get-color [app r c]
  (let [board (@app :board)
        row (board r)]
    (row c)))

(defn get-redgreen [app r c]
  (color-map (get-color app r c)))

(defn off!
  ([app]
     (set-board! app empty-board)))
