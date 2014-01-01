(ns launchtone.core
  (:use [overtone.live :only [midi-note-on midi-mk-full-device-key on-event]]
        [overtone.at-at :only [mk-pool]]
        [subwatch.core :only [add-sub-watch]]
        [launchtone.board :only [empty-board]]
        [launchtone.buffering :only [send-flip-buffer! send-buffer-msg! buffer-0 buffer-none]]
        [launchtone.colors :only [brightness-off brightness-min brightness-mid brightness-max
                                  color-map board->colors]]
        [launchtone.devices :only [select-transmitter-and-receiver]]
        [launchtone.launchpad :only [point->note note->point colors->velocity]]
        [launchtone.utils :only [enumerate debug set-level!]]))

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

(defn send-colors!
  [receiver colors]
  (doseq [[r row-colors] (enumerate colors)
          [c [red green]] (enumerate row-colors)]
    (send-redgreen! receiver r c red green)))

(defn send-board!
  "Render the given board to the given reciever."
  [receiver board]
  (send-colors! receiver (board->colors board)))

(defn buffering-on?
  [app]
  (not= (app :buffer) buffer-none))

;; To make this work with subwatch I would need to modify subwatch to also pass
;; in the full old/new values instead of just the sub parts, right?
(defn- render-board
  [_k ref old-board new-board]
  ;; I'm not sure that it's a good idea to deref
  ;; new-app here, I should probably arrange for
  ;; sub-watches to receive the whole thing as an additional
  ;; parameter but for now...
  (let [app @ref]
    (let [new-receiver (app :receiver)
          cur-buffer (app :buffer)]
      (when (not= old-board new-board)
        (send-board! new-receiver new-board)
        (when (buffering-on? app)
          (send-flip-buffer! new-receiver cur-buffer))))))

(defn- render-buffer
  [[old-app new-app]]
  (let [old-buffer (old-app :buffer)
        new-buffer (new-app :buffer)]
    (when (not= old-buffer new-buffer)
      (send-buffer-msg! (new-app :receiver) new-buffer))))

(defn buffering-on! [app]
  (swap! app #(assoc % :buffer buffer-0)))

(defn buffering-off! [app]
  (swap! app #(assoc % :buffer buffer-none)))

(defn- extend-key [key extra]
  (let [key (name key)
        extra (name extra)]
    (keyword (str key "|" extra))))

(defn make-app
  ([]
     (make-app :app))
  ([key]
     (let [[transmitter receiver] (select-transmitter-and-receiver)]
       (make-app key transmitter receiver)))
  ([key transmitter receiver]
     (when (nil? transmitter)
       (throw (Exception. (str "invalid transmitter " transmitter))))
     (when (nil? receiver)
       (throw (Exception. (str "invalid receiver " receiver))))
     (let [app (atom {:transmitter transmitter
                      :receiver receiver
                      :line-in (midi-mk-full-device-key transmitter)
                      :line-out (midi-mk-full-device-key receiver)
                      :worker-pool (mk-pool)
                      :buffer buffer-none
                      :board empty-board})
           buffer-key (extend-key key :buffer)
           board-key (extend-key key :board)]
       (add-sub-watch app buffer-key [:buffer] render-buffer)
       (add-sub-watch app board-key [:board] render-board)
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

(defn swap-board! [app f]
  (debug "wtf!!")
  (dosync
   (let [old-board (@app :board)
         new-board (f old-board)]
     (swap! app assoc-board new-board))))

(defn set-spot-color! [app r c color]
  (let [board (@app :board)
        row (board r)
        new-row (assoc row c color)
        new-board (assoc board r new-row)]
    (swap! app assoc-board new-board)))

(defn handle-button-event [app f event-type]
  (debug "installing handle-button-event")
  (fn [m]
    (debug "firing handle-button-event")
    (let [[row col] (note->point (m :note))]
      (f row col event-type m))))

(defn on-button-down [app f key]
  (debug "attached on-button-down for key " key)
  (on-event [:midi :note-on]
            (handle-button-event app f :down)
            key))

(defn on-button-up [app f key]
  (debug "attached on-button-up")
  (on-event [:midi :note-off]
            (handle-button-event app f :up)
            key))

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

