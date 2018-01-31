(ns sea-sim.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [sea-sim.views.sea :as shanty]
            [sea-sim.state :as state]
            [sea-sim.views.pause :as pause]
            [sea-sim.interrupt :as interrupt]))

(defn draw-state
  [current-state]
  ((state/current-draw-fn current-state) current-state))

(defn setup [cells-wide cells-high]
  (q/frame-rate 1)
  (q/color-mode :hsb)
  (q/no-stroke)
  {:grid         (shanty/init 640 480 cells-wide cells-high)
   :pause        (pause/init 640 480)
   :current-view [:grid]})

(defn update-state
  [current-state]
  (println "states - " (:current-view current-state))
  (let [keyboard-modified-state (if (q/key-pressed?)
                                  (interrupt/handle-keypress (q/key-as-keyword) current-state)
                                  (interrupt/handle-no-keypress current-state))]
    (let [new-data     ((state/current-update-fn keyboard-modified-state) keyboard-modified-state)]
      (assoc-in keyboard-modified-state [(state/current-view keyboard-modified-state) :data] new-data))))

(defn run
  [cells-wide cells-high]
  (q/defsketch game-of-life
    :host "host"
    :size [640 480]
    :setup (partial setup cells-wide cells-high)
    :update update-state
    :draw draw-state
    :middleware [m/fun-mode]))


(defn -main
  [& args]
  (run 5 5))
