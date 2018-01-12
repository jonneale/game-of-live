(ns sea-sim.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn dead?
  [state]
  (= 0 state))

(defn alive?
  [state]
  (= 1 state))

(defn rand-initial-state
  []
  (rand-nth (range 2)))

(defn cell
  [x y state cell-width cell-height]
  {:x x
   :y y
   :state state
   :draw-fn (fn [state] 
              (q/fill (* state 255)) 
              (q/rect (* x cell-width) (* y cell-height) cell-width cell-height))})

(defn initial-state
  [width height grid-width grid-height]
  (apply merge 
         (for [x (range grid-width)
               y (range grid-height)]
           {[x y] (cell x y (rand-initial-state) (/ width grid-width) (/ height grid-height))})))

(defn setup [cells-wide cells-high]
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (q/no-stroke)
  {:grid (initial-state 640 480 cells-wide cells-high)})

(defn in-bounds
  [v m]
  (min (max v 0) (dec m)))

(defn cell-neighbour-coords
  [{:keys [x y] :as cell} max-width max-height]
  (set 
   (for [x-diff (range -1 2)
         y-diff (range -1 2)
         :when (not (and (zero? x-diff) 
                         (zero? y-diff)))]
     [(in-bounds (+ x x-diff) max-width)
      (in-bounds (+ y y-diff) max-height)])))

(defn alive-neighbours
  [cell-neighbours grid]
  (reduce + (map (comp :state grid) cell-neighbours)))

(defn calculate-new-state
  [neighbour-counts {:keys [state] :as cell}]
  (if (or (and (alive? state) (= neighbour-counts 2))
          (= neighbour-counts 3))
    1
    0))

(defn update-cell
  [cell grid cells-wide cells-high]
  (assoc cell :state
         (-> cell
             (cell-neighbour-coords cells-wide cells-high)
             (alive-neighbours grid)
             (calculate-new-state cell))))

(defn update-grid
  [grid cells-wide cells-high]
  (reduce-kv (fn [agg k v]
               (assoc agg k (update-cell v grid cells-wide cells-high)))
             {} grid))

(defn update-state
  [cells-wide cells-high state]
  (update-in state [:grid] #(update-grid % cells-wide cells-high)))

(defn draw-state
  [{:keys [grid] :as state}]
  (q/background 128)
  (doseq [cell (vals grid)]
    ((:draw-fn cell) (:state cell))))

(defn run
  [cells-wide cells-high]
  (q/defsketch game-of-life
    :host "host"
    :size [640 480]
    :setup (partial setup cells-wide cells-high)
    :update (partial update-state cells-wide cells-high)
    :draw draw-state
    :middleware [m/fun-mode]))
