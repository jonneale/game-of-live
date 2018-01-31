(ns sea-sim.views.sea
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [clojure.java.io :as io]))

(def images
  (delay
   {:calm  (q/load-image "images/calm.jpg")
    :rough (q/load-image "images/rough.jpg")
    :ship  (q/load-image "images/ship.jpg")}))

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
  [x y state single-cell-pixel-width single-cell-pixel-height cells-wide cells-high images]
  {:x x
   :y y
   :state state
   :neighbours (cell-neighbour-coords x y cells-wide cells-high)
   :draw-fn (fn [state] 
              (if (dead? state)
                (q/image (@images :calm) (* x single-cell-pixel-width)  (* y single-cell-pixel-height) )
                (q/image (@images :rough) (* x single-cell-pixel-width)  (* y single-cell-pixel-height) )))})

(defn initial-state
  [pixels-wide pixels-high cells-wide cells-high images]
  (apply merge 
         (for [x (range cells-wide)
               y (range cells-high)]
           {[x y] (cell x 
                        y 
                        (rand-initial-state) 
                        (/ pixels-wide cells-wide) 
                        (/ pixels-high cells-high)
                        cells-wide
                        cells-high
                        images)})))

(defn draw-state
  [state]
  (let [grid-data (get-in state [:grid :data])]
    (q/background 128)
    (doseq [cell (vals grid-data)]
      ((:draw-fn cell) (:state cell)))))

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
  (let [grid-data (get-in state [:grid :data])]
    (apply hash-map
           (apply concat 
                  (for [cell grid-data]
                    (update-cell cell grid-data))))))

(defn init
  [width height cells-wide cells-high]
  {:data (initial-state 640 480 cells-wide cells-high images)
   :draw-fn draw-state
   :update-fn update-state})
