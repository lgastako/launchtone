(ns launchtone.launchpad)

(def brightness-off 0x00)
(def brightness-min 0x01)
(def brightness-mid 0x02)
(def brightness-max 0x03)

(defn point->note
  "Convert a column-row vector into the appropriate note to send to the Launchpad."
  [col row]
  (+ (* 0x10 row) col))

(defn note->point
  "Convert a note from the Launchpad into a column-row vector."
  [note]
  (let [col (bit-and-not note 0xfff0)
        row (bit-shift-right note 0x04)]
    [col row]))

(defn colors->velocity
  "Convert color to the appropriate velocity for the Launchpad."
  [red green]
  ;; Bit 6 must be 0
  ;; 5..4 are the green values
  ;; 3 clear (ignored for now)
  ;; 2 copy (ignored for now)
  ;; 1..0 red
  (let [flags 0] ;; for now
    (+ (* 0x10 green) red flags)))
