(ns de.explorama.frontend.mosaic.render.pixi.common)

(defn modifier [^js event]
  {:ctrl (-> event .-data .-originalEvent .-ctrlKey)
   :alt (-> event .-data .-originalEvent .-altKey)
   :shift (-> event .-data .-originalEvent .-shiftKey)})

(defn coords [event]
  [(-> event .-data .-global .-x)
   (-> event .-data .-global .-y)])

(defn twod->oned [x y cpl]
  (+ x (* y cpl)))

(defn oned->twod [index cpl]
  [(mod index cpl)
   (Math/floor (/ index cpl))])

(defn zoom-level [z render-type]
  (if (= :treemap render-type)
    0
    (cond (< 0.5 z) 3
          (< 0.35 z) 2
          (< 0.20 z) 1
          :else 0)))

(defn new-zoom [stage-key state args [nx ny nz]]
  (let [{:keys [x y z]} (get state [:pos stage-key])
        {:keys [card-margin card-height card-width]} (get state :constraints)
        {:keys [width height]} args
        {{:keys [scale-window-width scale-window-height scale-window-margin
                 inspector-margin-x inspector-margin-y inspector-header-y
                 inspector-width inspector-height]
          :or {scale-window-width 0
               scale-window-height 0
               scale-window-margin 0
               inspector-margin-x 0
               inspector-margin-y 0
               inspector-header-y 0}}
         :optional-desc
         {:keys [max-zoom min-zoom bb-min-x bb-min-y bb-max-x bb-max-y]} :params
         render-type :render-type}
        (get-in state [:contexts stage-key []])
        width (or inspector-width width)
        height (or inspector-height height)
        min-x bb-min-x
        min-y bb-min-y
        max-x bb-max-x
        max-y bb-max-y
        cz (max max-zoom
                (min min-zoom nz))
        min-x (- min-x (/ (+ inspector-margin-x scale-window-width scale-window-margin) cz))
        min-y (- min-y (/ (+ inspector-margin-y inspector-header-y) cz))
        max-x (- max-x (/ (+ inspector-margin-x) cz))
        max-y (- (+ max-y (/ (+ scale-window-height scale-window-margin) cz))
                 (/ (+ inspector-margin-y inspector-header-y) cz))

        x-max-current-bb (* cz (- min-x))
        x-min-current-bb (- (* (- (+ max-x card-margin (if (= :scatter render-type)
                                                         (* 0.5 card-width)
                                                         0))
                                  (/ width cz))
                               cz))
        cx
        (if (and (= :scatter render-type)
                 (<= x-max-current-bb nx)
                 (< (- bb-max-x bb-min-x)
                    (/ (- width scale-window-width) cz)))
          x-max-current-bb
          (min x-max-current-bb
               (max nx x-min-current-bb)))

        y-max-current-bb (* cz (- min-y))
        y-min-current-bb (- (* (- (+ max-y card-margin (if (= :scatter render-type)
                                                         (* 0.25 card-height)
                                                         0))
                                  (/ height cz))
                               cz))

        cy
        (if (and (= :scatter render-type)
                 (<= y-max-current-bb ny)
                 (< (- bb-max-y bb-min-y)
                    (/ (- height scale-window-height) cz)))
          y-min-current-bb
          (min y-max-current-bb
               (max ny y-min-current-bb)))

        [cx cy cz] (if (< min-zoom nz)
                     [x y z]
                     [cx cy cz])]
    [cx cy cz]))

(def drag-interaction (atom nil))
(def drag-interaction-left (atom nil))
(def vis-settings (atom nil))

(defn setting [k]
  (get @vis-settings k))

(defn data-path [render-path]
  (if (= [] render-path)
    render-path
    (into [1]
          (vec (interpose 1 render-path)))))

(def main-stage-index 0)
(def inspector-stage-index 1)
(defn zoom-context-stage [^js app idx]
  (.getChildAt (.-stage app)
               idx))
(defn find-stage-idx [state]
  (if (:inspector? state)
    (:inspector-idx state)
    main-stage-index))

(defn main-container
  ([stage-key]
   (case stage-key
     1 1
     0))
  ([stage stage-key]
   (.getChildAt ^js stage (main-container stage-key))))

(defn axes-container
  ([stage-key]
   (case stage-key
     1 2
     1))
  ([stage stage-key]
   (.getChildAt ^js stage (axes-container stage-key))))

(defn axes-background-container
  ([_]
   0)
  ([stage stage-key]
   (.getChildAt ^js stage (axes-background-container stage-key))))

(defn axes-background-container-direct [stage stage-key]
  (axes-background-container (axes-container stage stage-key)
                             stage-key))

(defn axes-text-container
  ([_]
   1)
  ([^js stage stage-key]
   (.getChildAt stage (axes-text-container stage-key))))

(defn axes-text-container-direct [stage stage-key]
  (axes-text-container (axes-container stage stage-key)
                       stage-key))

(defn background-container
  ([stage-key]
   (case stage-key
     1 0
     0))
  ([stage stage-key]
   (.getChildAt ^js stage (background-container stage-key))))

(defn ui-container
  ([_] 2)
  ([stage stage-key]
   (.getChildAt ^js stage (ui-container stage-key))))
