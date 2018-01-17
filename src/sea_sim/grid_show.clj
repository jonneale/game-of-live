(ns sea-sim.grid-show
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [sea-sim.shanty :as shanty]))

(defn draw-state
  [{:keys [current-view] :as state}]
  ((((first current-view) state) :draw-fn) state))

(defn setup [cells-wide cells-high]
  (q/frame-rate 60)
  (q/color-mode :hsb)
  (q/no-stroke)
  {:grid         (shanty/init 640 480 cells-wide cells-high)
   :current-view [:grid]})

(defn update-state
  [state]
  (when (q/key-pressed?)
    (println (q/key-code) " ---- " (q/key-as-keyword)))
  (let [current-view (-> state :current-view first)
        new-data (((current-view state) :update-fn) state)]
    (assoc-in state [current-view :data] new-data)))

(defn run
  [cells-wide cells-high]
  (q/defsketch game-of-life
    :host "host"
    :size [640 480]
    :setup (partial setup cells-wide cells-high)
    :update update-state
    :draw draw-state
    :middleware [m/fun-mode]))
