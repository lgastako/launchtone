(ns launchtone.buffering
  (:use [launchtone.midi :only [send-midi-msg!]]
        [launchtone.utils :only [debug]]))

(def buffer-none 0x30)
(def buffer-0 0x31)
(def buffer-1 0x34)

(defn send-buffer-msg!
  [sink data]
  (debug "Sending buffer msg!: " data)
  (send-midi-msg! sink 0xB0 0x00 data))

(defn send-flip-buffer!
  [sink cur-buffer]
  (debug "Sending flip buffer!")
  (let [new-buffer (if (= buffer-0 cur-buffer) buffer-1 buffer-0)]
    (send-buffer-msg! sink new-buffer)))

(defn send-buffer-on-msg!
  [sink]
  (debug "Sending buffer-on msg!")
  (send-buffer-msg! sink buffer-0))

(defn send-switch-to-buffer-0-msg!
  [sink]
  (debug "Sending buffer-0 msg!")
  (send-buffer-msg! sink buffer-1))

(defn send-buffer-off-msg!
  [sink]
  (debug "Sending buffer-1 msg!")
  (send-buffer-msg! sink buffer-none))
