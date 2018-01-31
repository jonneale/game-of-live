(ns sea-sim.views.pause
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn draw
  [width height]
  (fn [_]
    (q/background 255)
    (q/text "Paused" 320 240)))

(defn init
  [width height]
  {:data []
   :draw-fn (draw width height)
   :update-fn identity})
