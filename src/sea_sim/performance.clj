(ns sea-sim.performance
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

(defn in-bounds
  [v m]
  (min (max v 0) (dec m)))

(defn cell-neighbour-coords
  [x y max-width max-height]
  (set 
   (for [x-diff (range -1 2)
         y-diff (range -1 2)
         :when (not (and (zero? x-diff) 
                         (zero? y-diff)))]
     [(in-bounds (+ x x-diff) max-width)
      (in-bounds (+ y y-diff) max-height)])))

(defn cell
  [x y state single-cell-pixel-width single-cell-pixel-height cells-wide cells-high]
  {:x x
   :y y
   :state state
   :neighbours (cell-neighbour-coords x y cells-wide cells-high)
   :draw-fn (fn [state] 
              (q/fill (* state 255)) 
              (q/rect (* x single-cell-pixel-width) 
                      (* y single-cell-pixel-height) 
                      single-cell-pixel-width 
                      single-cell-pixel-height))})

(defn initial-state
  [pixels-wide pixels-high cells-wide cells-high]
  (apply merge 
         (for [x (range cells-wide)
               y (range cells-high)]
           {[x y] (cell x 
                        y 
                        (rand-initial-state) 
                        (/ pixels-wide cells-wide) 
                        (/ pixels-high cells-high)
                        cells-wide
                        cells-high)})))

(defn setup [cells-wide cells-high]
  (q/frame-rate 60)
  (q/color-mode :hsb)
  (q/no-stroke)
  {:grid (initial-state 640 480 cells-wide cells-high)})

(defn alive-neighbours
  [cell-neighbours grid]
  (reduce + (map (comp :state grid) cell-neighbours)))

(defn calculate-new-state
  [neighbour-counts {:keys [state] :as cell}]
  (if (or (and (alive? state) (= neighbour-counts 2))
          (= neighbour-counts 3))
    1
    0))

(defn new-cell-state
  [cell grid]
  (-> cell
      :neighbours
      (alive-neighbours grid)
      (calculate-new-state cell)))

(defn update-cell
  [[coords cell] grid]
  (let [new-state (new-cell-state cell grid)]
    (if (= (:state cell) new-state)
      [coords cell]
      [coords (assoc cell :state new-state)])))

(defn update-state
  [state]
  (assoc state :grid
         (apply hash-map
                (apply concat 
                       (for [cell (:grid state)]
                         (update-cell cell (:grid state)))))))

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
    :update update-state
    :draw draw-state
    :middleware [m/fun-mode]))
