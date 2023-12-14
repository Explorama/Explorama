(ns de.explorama.frontend.mosaic.render.draw.normal.cards
  (:require [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.draw.common-cards :as grdcc]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]))

(defn coords
  ([offset-x offset-y index {{cpl-ctn :cpl-ctn} :params :as ctx} constraints relative-x relative-y]
   (coords offset-x
           offset-y
           (mod index cpl-ctn)
           (Math/floor (/ index
                          cpl-ctn))
           ctx
           constraints
           relative-x
           relative-y))
  ([offset-x offset-y x y {:keys []} {:keys [card-width card-height card-margin]} _ _]
   [(+ offset-x
       (* 1
          (+ card-margin (* x (+ card-width (* 2 card-margin))))))
    (+ offset-y
       (* 1
          (+ card-margin (* y (+ card-height (* 2 card-margin))))))]))

(defn get-data
  ([path data-path idx]
   [(gdal/get (get-data path data-path)
              idx)])
  ([path data-path idx grouped-parent? _]
   (if grouped-parent?
     [(gdal/get (gdal/second (get-data path data-path))
                idx)]
     [(gdal/get (get-data path data-path)
                idx)]))
  ([path data-path]
   (gdal/get-in (gdb/get-events path)
                data-path)))

(defn draw-card-static [zoom instance stage-key row-major-index
                        {[offset-x offset-y] :offset-absolute {:keys [header]} :params :as ctx}
                        [data] constraints render-path highlights grouped?]
  (let [[data data-key card-container] (data 0)
        layouts (:layouts (gre/state instance))
        field-assignments (get layouts (grc/get-layout-id data-key))
        layout-desc (->> (get grdcc/field-count->layout-desc (count field-assignments))
                         (get grdcc/layout-desc))
        color (grc/get-color data-key)]
    (grdcc/draw-card-static- coords
                             data
                             data-key
                             ctx
                             constraints
                             zoom
                             instance
                             stage-key
                             card-container
                             field-assignments
                             layout-desc
                             color
                             offset-x
                             (+ offset-y header)
                             render-path
                             row-major-index
                             nil
                             (gre/frame-id instance)
                             grouped?
                             0
                             0
                             highlights)))

(defn draw-card-loadscreen [instance stage-key row-major-index
                            {[offset-x offset-y] :offset-absolute {:keys [header grouped?]} :params :as ctx}
                            [data] constraints _render-path highlights]
  (let [[data card-container] (data 0)]
    (when (and data card-container)
      (grdcc/draw-card-loadscreen- coords
                                   data
                                   ctx
                                   constraints
                                   instance
                                   stage-key
                                   card-container
                                   offset-x
                                   (+ offset-y header)
                                   row-major-index
                                   nil
                                   nil
                                   grouped?
                                   0
                                   0
                                   highlights))))

(defn draw-base
  [[card-x card-y] [offset-x offset-y] data width height margin _state _zoom-level instance stage overview-factor factor-absolute]
  (let [color (grc/get-color data)
        x (+ (* offset-x overview-factor) (* card-x (+ width (* 2 margin)) factor-absolute))
        y (+ (* offset-y overview-factor) (* card-y (+ height (* 2 margin)) factor-absolute))]
    (gre/rect instance
              stage
              x
              y
              (* factor-absolute width)
              (* factor-absolute height)
              (if (= "#ffffff" color)
                config/white-replacement
                color))))

(defn- resolve-custom-data [instance stage-key path data-path parent-grouped?]
  (cond (= pc/main-stage-index stage-key)
        (if parent-grouped?
          (when-let [data (get-data path data-path)]
            (gdal/second data))
          (get-data path data-path))
        (map? (get-in (gre/state instance) [:custom-data stage-key]))
        (gdal/second (get-data path (get-in (gre/state instance) [:custom-data stage-key :path])))
        :else
        (get-in (gre/state instance) [:custom-data stage-key])))

(defn relevant-annotations [instance stage-key render-path ctx parent-grouped? _]
  (let [{:keys [path]}
        (gre/state instance)
        {{:keys [cpl-ctn header]} :params
         :keys [offset-absolute]}
        ctx
        data-path (pc/data-path render-path)
        data (resolve-custom-data instance stage-key path data-path parent-grouped?)
        [offset-x offset-y] offset-absolute
        ct (if (or (nil? data)
                   (map? data))
             0
             (gdal/count data))
        relevant-annotations (atom [])
        annotations (gdb/get-annotations (gre/frame-id instance))]
    (loop [n 0]
      (if (< n ct)
        (let [event (gdal/get data n)
              pos [(mod n cpl-ctn)
                   (Math/floor (/ n cpl-ctn))]
              offsets [offset-x (+ offset-y header)]]
          (when (get annotations [(grc/get-id event)
                                  (grc/get-bucket event)])
            (swap! relevant-annotations conj [pos offsets]))
          (recur (inc n)))
        @relevant-annotations))))

(defn relevant-highlights [instance stage-key render-path ctx highlights parent-grouped? _]
  (let [{:keys [path]}
        (gre/state instance)
        {{:keys [cpl-ctn header]} :params
         :keys [offset-absolute]}
        ctx
        data-path (pc/data-path render-path)
        data (resolve-custom-data instance stage-key path data-path parent-grouped?)
        [offset-x offset-y] offset-absolute
        ct (if (or (nil? data)
                   (map? data))
             0
             (gdal/count data))
        relevant-highlights (atom [])]
    (loop [n 0]
      (if (< n ct)
        (let [event (gdal/get data n)
              pos [(mod n cpl-ctn)
                   (Math/floor (/ n cpl-ctn))]
              offsets [offset-x (+ offset-y header)]]
          (when (highlights (grc/get-id event))
            (swap! relevant-highlights conj [[pos offsets] [n #{(grc/get-id event)}]]))
          (recur (inc n)))
        @relevant-highlights))))

(defn render-base-static [instance stage-key render-path stage contraints ctx highlights parent-grouped? _]
  (let [{:keys [path]}
        (gre/state instance)
        {:keys [card-width card-height card-margin]}
        contraints
        {{:keys [cpl-ctn header]} :params
         :keys [factor-overview offset-absolute]}
        ctx
        data-path (pc/data-path render-path)
        data (resolve-custom-data instance stage-key path data-path parent-grouped?)
        [offset-x offset-y] offset-absolute
        ct (if (or (nil? data)
                   (map? data)) ; Can happen sometimes when loading the same project multiple times (timing issue i guess)
             0
             (gdal/count data))
        relevant-annotations (atom [])
        relevant-highlights (atom [])
        annotations (gdb/get-annotations (gre/frame-id instance))]
    (loop [n 0]
      (if (< n ct)
        (let [event (gdal/get data n)
              pos [(mod n cpl-ctn)
                   (Math/floor (/ n cpl-ctn))]
              offsets [offset-x (+ offset-y header)]]
          (when (get annotations [(grc/get-id event)
                                  (grc/get-bucket event)])
            (swap! relevant-annotations conj [pos offsets]))
          (when (and highlights (highlights (grc/get-id event)))
            (swap! relevant-highlights conj [[pos offsets]]))
          (draw-base pos
                     offsets
                     event
                     card-width
                     card-height
                     card-margin
                     {}
                     0
                     instance
                     stage
                     factor-overview
                     factor-overview)
          (recur (inc n)))
        [@relevant-annotations
         @relevant-highlights]))))

(defn- draw-annotation
  [[[card-x card-y] [offset-x offset-y]] width height margin instance stage overview-factor]
  (let [x (+ offset-x
             (* card-x (+ width (* 2 margin))))
        y (+ offset-y
             (* card-y (+ height (* 2 margin))))]
    (gre/img instance
             stage
             "speech-bubble"
             (* (+ x (* 0.25 width)) overview-factor)
             (* (+ y (* 0.25 height)) overview-factor)
             (* 0.5 width overview-factor)
             (* 0.5 height overview-factor))))

(defn render-annotations-0 [instance stage contraints ctx relevant-annotations]
  (let [{:keys [card-width card-height card-margin]}
        contraints
        {:keys [factor-overview]}
        ctx]
    (loop [n 0]
      (if (< n (count relevant-annotations))
        (do
          (draw-annotation (get relevant-annotations n)
                           card-width
                           card-height
                           card-margin
                           instance
                           stage
                           factor-overview)
          (recur (inc n)))
        :done))))

(defn coords-highlight [[[card-x card-y] [offset-x offset-y]] width height margin]
  [(+ offset-x
      (* card-x (+ width (* 2 margin))))
   (+ offset-y
      (* card-y (+ height (* 2 margin))))])

(defn- draw-highlight
  [info width height margin instance stage overview-factor]
  (let [[x y] (coords-highlight info width height margin)]
    (gre/rect instance
              stage
              (* (- x (* 0.05 width)) overview-factor)
              (* (- y (* 0.05 height)) overview-factor)
              (* 1.1 width overview-factor)
              (* 1.1 height overview-factor)
              grdcc/hightlight-color
              {:a 0.2})))

(defn render-highlights-0 [instance stage contraints ctx relevant-highlights]
  (let [{:keys [card-width card-height card-margin]}
        contraints
        {:keys [factor-overview]}
        ctx]
    (loop [n 0]
      (if (< n (count relevant-highlights))
        (do
          (draw-highlight (first (get relevant-highlights n))
                          card-width
                          card-height
                          card-margin
                          instance
                          stage
                          factor-overview)
          (recur (inc n)))
        :done))))

(defn index [_ idx]
  idx)
