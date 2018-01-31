(ns sea-sim.interrupt
  (:require [sea-sim.state :as s]))

(defmulti handle-new-keypress
  (fn [key state]
    (println key)
    key))

(defmethod handle-new-keypress :p
  [_ state]
  (println "Pause requested")
  (println (s/current-view state))
  (let [new-state (if (= :pause (s/current-view state))
                    (s/pop-current-view state)
                    (s/push-view :pause state))]
    (println (s/current-view new-state))
    new-state))

(defmethod handle-new-keypress (keyword " ")
  [_ state]
  state)

(defmethod handle-new-keypress :default
  [key state]
  (println key " was pressed")
  state)

(defn handle-no-keypress
  [state]
  (s/add-keypress state nil))

(defn handle-keypress
  [key state]
  (if (= key (s/last-key-pressed state))
    ;; key still pressed
    state
    (handle-new-keypress key (s/add-keypress state key))))
