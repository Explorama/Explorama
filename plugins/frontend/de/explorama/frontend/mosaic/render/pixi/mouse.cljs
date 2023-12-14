(ns de.explorama.frontend.mosaic.render.pixi.mouse
  (:require [de.explorama.frontend.mosaic.render.pixi.interaction :as pi]
            [de.explorama.frontend.mosaic.render.config :refer [panning-event?]]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as grpc]))

(def panning? (atom false))
(def ^:private touch-ev-cache (atom {}))
(def ^:private pinch-diff (atom nil))

(defn- is-panning-wheel? [e]
  (not= (aget e "wheelDeltaX") 0))

(defn- is-touch? [e]
  (= "touch" (aget e "pointerType")))

(defn wheel [instance]
  (fn [e]
    (if (is-panning-wheel? e)
      (pi/pan instance e true)
      (do
        (pi/zoom instance e)
        (.stopPropagation e)
        (.preventDefault e)))))

(defn- reset-touch-states []
  (reset! touch-ev-cache {})
  (reset! pinch-diff nil))

(defn- handle-touch-start [e]
  (when (is-touch? e)
    (swap! touch-ev-cache
           assoc
           (aget e "pointerId")
           {:x (aget e "clientX")
            :y (aget e "clientY")
            :offset-x (aget e "offsetX")
            :offset-y (aget e "offsetY")})))

(defn mousedown [instance]
  (fn [e]
    (when (and (or (panning-event? e)
                   (is-touch? e))
               (not @grpc/drag-interaction))
      (gre/assoc-state! instance [:pmx nil
                                  :pmy nil])
      (handle-touch-start e)
      (reset! panning? true))))

(defn- handle-touch-pinch-zoom
  "Detect and execute pinch zoom on mobile devices"
  [e instance]
  (when (is-touch? e)
    (let [evc @touch-ev-cache]
      (when (= 2 (count evc))
        (let [pointer-id (aget e "pointerId")
              {:keys [x y offset-x offset-y]}
              (get evc (first (filter #(not= % pointer-id)
                                      (keys evc))))
              curr-x (aget e "clientX")
              curr-y (aget e "clientY")
              curr-offset-x (aget e "offsetX")
              curr-offset-y (aget e "offsetY")
              ;; Distance between two touch points
              new-diff (Math/sqrt
                        (+ (Math/pow (- x curr-x) 2)
                           (Math/pow (- y curr-y) 2)))
              offset-x (+ (min curr-offset-x offset-x)
                          (Math/abs (- curr-offset-x offset-x)))
              offset-y (+ (min curr-offset-y offset-y)
                          (Math/abs (- curr-offset-y offset-y)))
              old-diff @pinch-diff]
          (reset! pinch-diff new-diff)
          (swap! touch-ev-cache
                 assoc
                 pointer-id
                 {:x curr-x
                  :y curr-y
                  :offset-x curr-offset-x
                  :offset-y curr-offset-y})
          (when old-diff
            (let [d (if (> new-diff old-diff)
                      #js{"offsetX" offset-x
                          "offsetY" offset-y
                          "wheelDelta" 40}
                      #js{"offsetX" offset-x
                          "offsetY" offset-y
                          "wheelDelta" -40})]
              (pi/zoom instance d))
            true))))))

(defn pointermove [instance]
  (fn [e]
    (let [pinch? (handle-touch-pinch-zoom e instance)]
      (when (and @panning?
                 (not pinch?))
        (pi/pan instance e false)))))

(defn mouseup [instance]
  (fn [e]
    (when (or (panning-event? e)
              (is-touch? e))
      (gre/assoc-state! instance [:pmx nil
                                  :pmy nil])
      (reset! panning? false)
      (reset-touch-states))))

(defn mouseleave [instance]
  (fn [e]
    (pi/leave instance)
    (when (or (panning-event? e)
              @panning?
              (is-touch? e))
      (gre/assoc-state! instance [:pmx nil
                                  :pmy nil])
      (reset! panning? false)
      (reset-touch-states))))