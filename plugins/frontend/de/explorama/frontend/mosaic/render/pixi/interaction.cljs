(ns de.explorama.frontend.mosaic.render.pixi.interaction
  (:require [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [de.explorama.frontend.mosaic.render.pixi.db :refer [instances]]
            [re-frame.core :as re-frame]))

(defn safe-number? [number]
  ;; Ensures that:
  ;;   * Number is not null
  ;;   * Number is not infinity/-infinity (number? is true for infinity)
  ;;   * Number is not NaN
  ;;   * Number is a number
  ;;   See https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Number/isFinite
  (js/Number.isFinite number))

(defn set-transform-main [stage x y z]
  (when stage
    (set! (.-x (.-position stage)) x)
    (set! (.-y (.-position stage)) y)
    (set! (.-x (.-scale stage)) z)
    (set! (.-y (.-scale stage)) z)))

(defn set-transform-main-pos [stage x y]
  (when stage
    (set! (.-x (.-position stage)) x)
    (set! (.-y (.-position stage)) y)))

(def max-wheel-delta 720)
(def min-wheel-delta -720)

(defn- apply-zoom [instance stage-key x y z new-zoom-level]
  (let [app (gre/app instance)
        stage (pc/zoom-context-stage app stage-key)]
    (set-transform-main (pc/main-container stage stage-key)
                        x y z)
    (set-transform-main (pc/axes-background-container-direct stage stage-key)
                        x y z)
    (gre/merge-in-state! instance
                         [:pos stage-key]
                         {:x x
                          :y y
                          :z z
                          :next-zoom new-zoom-level})
    (gre/dirty! instance true)
    (gre/update-zoom instance stage-key)))

(defn- calculate-zoom [path stage-key state args x-offset y-offset wheel-delta]
  (let [{:keys [x y z zoom]} (get state path)
        old-coords [x y z]
        {overview-factor :factor-overview
         render-type :render-type}
        (get-in state [:contexts stage-key []])
        wheel-delta (cond
                      (> wheel-delta max-wheel-delta) max-wheel-delta
                      (< wheel-delta min-wheel-delta) min-wheel-delta
                      :else wheel-delta)
        delta (+ 1
                 (* 0.1
                    (/ wheel-delta 120)))
        new-z (* z delta)
        mx (- x-offset)
        my (- y-offset)
        x (- (* (/ x z)
                new-z)
             (- mx (* mx delta)))
        y (- (* (/ y z)
                new-z)
             (- my (* my delta)))
        [x y z :as new-coord]
        (pc/new-zoom stage-key state args [x y new-z] zoom)
        new-zoom-level (pc/zoom-level z zoom overview-factor render-type)
        z (cond (and (= 0 zoom)
                     (< 0 new-zoom-level))
                (* z overview-factor)
                (and (< 0 zoom)
                     (= 0 new-zoom-level))
                (/ z overview-factor)
                :else z)]
    (when (and (not= old-coords new-coord)
               (safe-number? x)
               (safe-number? y)
               (safe-number? z)
               (safe-number? new-zoom-level))
      [x y z new-zoom-level])))

(defn zoom [this event]
  (let [stage-key (pc/find-stage-idx (gre/state this))
        {:keys [coupled] :as state} (gre/state this)
        [x y z new-zoom-level :as new-coords] (calculate-zoom [:pos stage-key]
                                                              stage-key
                                                              state
                                                              (gre/args this)
                                                              (.-offsetX event)
                                                              (.-offsetY event)
                                                              (.-wheelDelta event))]
    (when new-coords
      (apply-zoom this stage-key x y z new-zoom-level)
      (doseq [other-instance (if-let [with (:with coupled)]
                               (mapv (fn [path] (get @instances path)) with)
                               [])]
        (apply-zoom other-instance stage-key x y z new-zoom-level)))))

(defn- apply-pan [instance stage-key x y mx my]
  (let [app (gre/app instance)
        stage (pc/zoom-context-stage app stage-key)]
    (set-transform-main-pos (pc/main-container stage stage-key)
                            x y)
    (set-transform-main-pos (pc/axes-background-container-direct stage stage-key)
                            x y)
    (gre/merge-in-state! instance
                         [:pos stage-key]
                         {:x x
                          :y y})
    (gre/merge-state! instance
                      {:pmx mx
                       :pmy my})
    (gre/dirty! instance true)
    (gre/update-zoom instance stage-key)))

(defn pan [this event wheel?]
  (let [stage-key (pc/find-stage-idx (gre/state this))
        {:keys [coupled]
         opmx :pmx
         opmy :pmy
         :as state}
        (gre/state this)
        {:keys [zoom x y z]}
        (get state [:pos stage-key])
        old-coords [x y z]
        update-instances (conj (if-let [with (:with coupled)]
                                 (mapv (fn [path] (get @instances path)) with)
                                 [])
                               this)
        args (gre/args this)
        [mx my pmx pmy new-x new-y]
        (if wheel?
          (let [new-x (+ x (.-deltaX event))
                new-y (+ y (.-deltaY event))]
            [(- (.-offsetX event))
             (- (.-offsetY event))
             nil nil
             new-x new-y])
          (let [mx (- (.-offsetX event))
                my (- (.-offsetY event))
                pmx (if opmx opmx mx)
                pmy (if opmy opmy my)
                new-x (+ x (- pmx mx))
                new-y (+ y (- pmy my))]
            [mx my pmx pmy new-x new-y]))
        [x y z :as new-coord]
        (pc/new-zoom stage-key state args [new-x new-y z] zoom)]
    (when (and (or (not= old-coords new-coord)
                   (not= pmx opmx)
                   (not= pmy opmy))
               (safe-number? x)
               (safe-number? y)
               (safe-number? z))
      (apply-pan this stage-key x y mx my)
      (doseq [other-instance update-instances]
        (apply-pan other-instance stage-key x y mx my)))))

(defn log-canvas-state [instance stage-key]
  (let [{:keys [app-state-path]} (gre/args instance)
        {:keys [x y z zoom]} (get (gre/state instance) [:pos stage-key])
        path (gp/top-level app-state-path)
        state {:x x
               :y y
               :z z
               :zoom zoom}]
    (re-frame/dispatch [:de.explorama.frontend.mosaic.event-logging/log-event
                    (gp/frame-id path)
                    "canvas-state"
                    (assoc state :app-state-path app-state-path)])
    (gre/dirty! instance false)))

(defn leave [this]
  (let [{:keys [coupled]} (gre/state this)
        update-instances (conj (if-let [with (:with coupled)]
                                 (mapv (fn [path] (get @instances path)) with)
                                 [])
                               this)]
    (doseq [other-instance update-instances]
      (let [instance (if (vector? other-instance)
                       (get @instances other-instance)
                       other-instance)]
        (when (gre/dirty? instance)
          (log-canvas-state instance pc/main-stage-index))))))

(defn deselect [this idx row-major-index]
  (gre/assoc-in-state! this [:highlights [:render idx row-major-index]] false))

(defn select [this idx row-major-index]
  (gre/assoc-in-state! this [:highlights [:render idx row-major-index]] true))
