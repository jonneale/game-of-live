(ns sea-sim.state)


(defn current-view
  [state]
  (first (:current-view state)))

(defn current-draw-fn
  [state]
  (-> state
      current-view
      state
      :draw-fn))

(defn current-update-fn
  [state]
  (-> state
      current-view
      state
      :update-fn))

(defn pop-current-view
  [state]
  (update-in state [:current-view] rest))

(defn push-view
  [new-view state]
  (update-in state [:current-view] (partial cons new-view)))

(defn last-key-pressed
  [state]
  (:key-pressed state))

(defn add-keypress
  [state key]
  (assoc state :key-pressed key))
