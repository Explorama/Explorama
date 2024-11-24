(ns de.explorama.frontend.mosaic.render.draw.scatter.cards
  (:require [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.interaction.state :as tooltip]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.draw.color :as color]
            [de.explorama.frontend.mosaic.render.draw.common-cards :as grdcc]
            [de.explorama.frontend.mosaic.render.draw.text-handler :as text-handler]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.parameter :as grp]
            [de.explorama.frontend.mosaic.render.pixi.common :as common]
            ["pixi.js" :refer [Container] ]))

(defn coords
  ([offset-x offset-y index {{cpl-ctn :cpl-ctn} :params :as ctx} constraints x-relative y-relative]
   (coords offset-x
           offset-y
           (mod index cpl-ctn)
           (Math/floor (/ index
                          cpl-ctn))
           ctx
           constraints
           x-relative
           y-relative))
  ([offset-x offset-y x y {:keys []} {:keys [card-width card-height card-margin]} x-relative y-relative]
   [(+ offset-x
       x-relative
       (* -0.5 (+ card-width (* 2 card-margin)))
       (+ card-margin (* x (+ card-width (* 2 card-margin)))))
    (+ offset-y
       y-relative
       (* -0.5 (+ card-height (* 2 card-margin)))
       (+ card-margin (* y (+ card-height (* 2 card-margin)))))]))

(defn most-frequent-layout [idxs]
  (->> (map #(gdal/get % 0) idxs)
       (frequencies)
       (sort-by (fn [[_ val]] val) #(compare %2 %1))
       (ffirst)))

(defn get-data
  ([path data-path idx]
   [(gdal/get (get-data path data-path)
              idx)])
  ([path data-path idx _ {{:keys [mapping]} :optional-desc}]
   (let [data (get-data path data-path)
         idx-map (get mapping idx)]
     (mapv (fn [[idx x-relative y-relative]]
             [(gdal/get data idx) idx x-relative y-relative])
           idx-map)))
  ([path data-path]
   (gdal/get-in (gdb/get-events path)
                data-path)))

(defn draw-cluster-static- [coords meta {:keys [card-margin card-width card-height] :as constraints}
                            zoom-level instance _root-container-idx stage color offset-x offset-y
                            row-major-index _path _grouped? x-relative y-relative highlights counter data]
  (let [[x y] (coords offset-x offset-y row-major-index meta constraints x-relative y-relative)]
    (when (= 1 zoom-level)
      (gre/interaction-primitive instance
                                 stage
                                 "dblclick"
                                 (fn [_ _ _e]
                                   (when-not (:inspector? (gre/state instance))
                                     (let [main-stage (.-stage (gre/app instance))
                                           frame-id (gre/frame-id instance)
                                           {:keys [width height]} (gre/args instance)
                                           inspector-margin-x 50
                                           inspector-margin-y 50
                                           inspector-header-y 0
                                           size-x (* 0.8 width)
                                           size-y (* 0.8 height)
                                           inspector-stage (Container.)
                                           main-container (Container.)
                                           axes-container (Container.)
                                           background-container (Container.)
                                           contexts
                                           (grp/grp-contexts data
                                                             nil
                                                             {:type :raster}
                                                             (select-keys (gre/state instance)
                                                                          [:constraints :di :path])
                                                             {:width size-x :height (- size-y inspector-header-y)
                                                              :optional-desc {:inspector-width size-x
                                                                              :inspector-height (- size-y inspector-header-y)
                                                                              :inspector-margin-x (* width 0.1)
                                                                              :inspector-margin-y (* height 0.1)
                                                                              :inspector-header-y inspector-header-y}}
                                                             [nil nil]
                                                             nil
                                                             nil
                                                             nil)
                                           max-zoom (get-in contexts [[] :params :max-zoom])]
                                       (gre/assoc-state! instance [:inspector-idx common/inspector-stage-index
                                                                   :inspector? true
                                                                   :inspector-init {common/inspector-stage-index false}
                                                                   :custom-data {common/inspector-stage-index data}])
                                       (gre/assoc-state! instance
                                                         [[:pos common/inspector-stage-index]
                                                          {:x 0
                                                           :y 0
                                                           :z 0
                                                           :zoom 0
                                                           :next-zoom 0}])
                                       (gre/assoc-in-state! instance
                                                            [:contexts common/inspector-stage-index]
                                                            contexts)
                                       (.addChildAt main-stage
                                                    inspector-stage
                                                    common/inspector-stage-index),
                                       (aset inspector-stage "name" "inspector-stage")
                                       (aset main-container "name" "inspector-main-container")
                                       (aset axes-container "name" "inspector-axes-container")
                                       (aset background-container "name" "inspector-background-container")
                                       (.addChildAt inspector-stage
                                                    background-container
                                                    (common/background-container common/inspector-stage-index))
                                       (.addChildAt inspector-stage
                                                    main-container
                                                    (common/main-container common/inspector-stage-index))
                                       (.addChildAt inspector-stage
                                                    axes-container
                                                    (common/axes-container common/inspector-stage-index))
                                       (.addChildAt axes-container
                                                    (Container.)
                                                    (common/axes-background-container common/inspector-stage-index))
                                       (.addChildAt axes-container
                                                    (Container.)
                                                    (common/axes-text-container common/inspector-stage-index))
                                       (gre/move-to! instance common/inspector-stage-index
                                                     (+ (/ inspector-margin-x max-zoom))
                                                     (+ (/ inspector-margin-y max-zoom))
                                                     max-zoom (if (< (count data) 17) 1 0))
                                       (gre/update-zoom instance common/inspector-stage-index)
                                       (tooltip/block-tooltips frame-id))))
                                 nil
                                 0)
      (let [[soft hard] (color/font-color color [255 255 255])]
        (when (and highlights (some (fn [event]
                                      (highlights (grc/get-id event)))
                                    data))
          (gre/rect instance
                    stage
                    (- x 15)
                    (- y 15)
                    (+ card-width (* 1.3 card-margin))
                    (+ card-height (* 1.3 card-margin))
                    grdcc/hightlight-color
                    {:a 0.2}))
        (gre/rect instance
                  stage
                  (+ x 15)
                  (+ y 15)
                  card-width
                  card-height
                  color
                  {:interactive? true
                   :stage 0})
        (gre/rect instance
                  stage
                  (+ x 5)
                  (+ y 5)
                  card-width
                  card-height
                  soft
                  {:interactive? true
                   :stage 0
                   :a 0.4})
        (gre/rect instance
                  stage
                  x
                  y
                  card-width
                  card-height
                  color
                  {:interactive? true
                   :stage 0})
        (gre/polygon instance
                     stage
                     [(gre/point instance
                                 (+ x 465)
                                 (+ y card-height))
                      (gre/point instance
                                 (+ x 465)
                                 (+ y 465))
                      (gre/point instance
                                 (+ x card-width)
                                 (+ y 465))]
                     soft
                     {:a 0.4})
        (text-handler/draw-text-new instance
                                    stage
                                    (+ x (/ card-width 2))
                                    (+ y (/ card-height 2))
                                    (* card-width 0.9)
                                    (* card-height 0.9)
                                    nil
                                    nil
                                    counter
                                    {:size (cond (< counter 100)
                                                 280
                                                 (< counter 10000)
                                                 140
                                                 :else
                                                 70)
                                     :color hard
                                     :horizontal-align :center
                                     :vertical-align :center})))))

(defn draw-card-static [zoom instance root-container-idx row-major-index
                        {[offset-x offset-y] :offset-absolute
                         {:keys [header]} :params
                         :as ctx}
                        [data-struct cluster containers]
                        constraints
                        render-path
                        highlights
                        grouped?]
  (when-not (empty? data-struct)
    (doseq [[[x-relative y-relative] idxs] cluster]
      (if (< 1 (count idxs))
        (let [color (most-frequent-layout (map #(-> (get data-struct %)
                                                    second
                                                    first)
                                               idxs))]
          (draw-cluster-static- coords
                                ctx
                                constraints
                                zoom
                                instance
                                root-container-idx
                                (get containers [x-relative y-relative])
                                color
                                offset-x
                                (+ offset-y header)
                                row-major-index
                                grouped?
                                render-path
                                x-relative
                                y-relative
                                highlights
                                (count idxs)
                                (reduce (fn [acc event]
                                          (gdal/conj acc (-> event second first)))
                                        (gdal/->g [])
                                        (map #(get data-struct %) idxs))))
        (let [[data [key data-idx] card-container] (get data-struct (idxs 0))
              layouts (:layouts (gre/state instance))
              field-assignments (get layouts (grc/get-layout-id key))
              layout-desc (->> (get grdcc/field-count->layout-desc (count field-assignments))
                               (get grdcc/layout-desc))
              color (grc/get-color key)]
          (when layout-desc
            (grdcc/draw-card-static- coords
                                     data
                                     key
                                     ctx
                                     constraints
                                     zoom
                                     instance
                                     root-container-idx
                                     card-container
                                     field-assignments
                                     layout-desc
                                     color
                                     offset-x
                                     (+ offset-y header)
                                     render-path
                                     row-major-index
                                     data-idx
                                     (gre/frame-id instance)
                                     grouped?
                                     x-relative
                                     y-relative
                                     highlights)))))))

(defn draw-cluster
  [[card-x card-y] [offset-x offset-y] color width height margin instance stage factor-absolute x-relative y-relative]
  (let [x (* factor-absolute
             (+ offset-x
                (* (+ width (* 2 margin)) -0.5)
                x-relative
                (* card-x (+ width (* 2 margin)))))
        y (* factor-absolute
             (+ offset-y
                (* (+ height (* 2 margin)) -0.5)
                y-relative
                (* card-y (+ height (* 2 margin)))))
        add-x (* 465 factor-absolute)
        add-y (* 465 factor-absolute)
        scaled-width (* width factor-absolute)
        scaled-height (* height factor-absolute)
        color (if (= "#ffffff" color)
                config/white-replacement
                color)
        [soft] (color/font-color color [255 255 255])]
    (gre/rect instance
              stage
              (+ x (* factor-absolute 15))
              (+ y (* factor-absolute 15))
              (* factor-absolute width)
              (* factor-absolute height)
              color)
    (gre/rect instance
              stage
              (+ x (* factor-absolute 5))
              (+ y (* factor-absolute 5))
              (* factor-absolute width)
              (* factor-absolute height)
              soft
              {:a 0.4})
    (gre/rect instance
              stage
              x
              y
              (* factor-absolute width)
              (* factor-absolute height)
              color)
    (gre/polygon instance
                 stage
                 [(gre/point instance
                             (+ x add-x)
                             (+ y scaled-height))
                  (gre/point instance
                             (+ x add-x)
                             (+ y add-y))
                  (gre/point instance
                             (+ x scaled-width)
                             (+ y add-y))]
                 soft
                 {:a 0.4})))

(defn draw-card-loadscreen [instance stage-key row-major-index
                            {[offset-x offset-y] :offset-absolute {:keys [header grouped?]} :params :as ctx}
                            [data-struct cluster _layouts _containers] constraints _render-path highlights]
  (doseq [[[x-relative y-relative] _idxs] cluster]
    (let [[[data] card-container] (data-struct 0)]
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
                                     x-relative
                                     y-relative
                                     highlights)))))

(defn draw-base
  [[card-x card-y] [offset-x offset-y] data width height margin instance stage overview-factor x-relative y-relative]
  (let [color (grc/get-color data)
        x
        (* overview-factor
           (+ offset-x
              (* (+ width (* 2 margin)) -0.5)
              x-relative
              (* card-x (+ width (* 2 margin)))))
        y (* overview-factor
             (+ offset-y
                (* (+ height (* 2 margin)) -0.5)
                y-relative
                (* card-y (+ height (* 2 margin)))))]
    (gre/rect instance
              stage
              x
              y
              (* overview-factor width)
              (* overview-factor height)
              (if (= "#ffffff" color)
                config/white-replacement
                color))))

(defn render-base-static [instance _stage-key render-path stage contraints ctx highlights _parent-grouped? cluster-fn]
  (let [{:keys [path]}
        (gre/state instance)
        {:keys [card-width card-height card-margin]}
        contraints
        {{:keys [cpl-ctn count-ctn header]} :params :keys [factor-overview offset-absolute]
         {:keys [mapping]} :optional-desc}
        ctx
        data-path (common/data-path render-path)
        data (get-data path data-path)
        [offset-x offset-y] offset-absolute
        relevant-annotations (atom [])
        relevant-highlights (atom [])
        annotations (gdb/get-annotations (gre/frame-id instance))]
    (loop [n 0]
      (if (< n count-ctn)
        (let [idx-map (get mapping n)
              idx-mapping (mapv
                           (fn [[idx x-relative y-relative]]
                             [(gdal/get data idx) n x-relative y-relative])
                           idx-map)
              [cluster] (cluster-fn idx-mapping)]
          (doseq [[[x-relative y-relative] idxs] cluster
                  :let [datapoints (->> (map idx-mapping idxs)
                                        (map first))]]
            (if (< 1 (count idxs))
              (let [pos [(mod n cpl-ctn)
                         (Math/floor (/ n cpl-ctn))]
                    offsets [offset-x (+ offset-y header)]]
                (when (some (fn [datapoint]
                              (get annotations [(grc/get-id datapoint)
                                                (grc/get-bucket datapoint)]))
                            datapoints)
                  (swap! relevant-annotations conj [pos offsets x-relative y-relative]))
                (when (and highlights
                           (some (fn [datapoint]
                                   (highlights (grc/get-id datapoint)))
                                 datapoints))
                  (swap! relevant-highlights conj [[pos offsets x-relative y-relative]]))
                (draw-cluster pos
                              offsets
                              (most-frequent-layout datapoints)
                              card-width
                              card-height
                              card-margin
                              instance
                              stage
                              factor-overview
                              x-relative
                              y-relative))
              (when (seq idxs)
                (let [data (ffirst idx-mapping)
                      pos [(mod n cpl-ctn)
                           (Math/floor (/ n cpl-ctn))]
                      offsets [offset-x (+ offset-y header)]]
                  (when (get annotations [(grc/get-id data)
                                          (grc/get-bucket data)])
                    (swap! relevant-annotations conj [pos offsets x-relative y-relative]))
                  (when (and highlights (highlights (grc/get-id data)))
                    (swap! relevant-highlights conj [[pos offsets x-relative y-relative]]))
                  (draw-base pos
                             offsets
                             data
                             card-width
                             card-height
                             card-margin
                             instance
                             stage
                             factor-overview
                             x-relative
                             y-relative)))))
          (recur (inc n)))
        [@relevant-annotations
         @relevant-highlights]))))

(defn relevant-annotations [instance _stage-key render-path ctx _parent-grouped? cluster-fn]
  (let [{:keys [path]}
        (gre/state instance)
        {{:keys [cpl-ctn count-ctn header]} :params :keys [offset-absolute]
         {:keys [mapping]} :optional-desc}
        ctx
        data-path (common/data-path render-path)
        data (get-data path data-path)
        [offset-x offset-y] offset-absolute
        relevant-annotations (atom [])
        annotations (gdb/get-annotations (gre/frame-id instance))]
    (loop [n 0]
      (if (< n count-ctn)
        (let [idx-map (get mapping n)
              idx-mapping (mapv
                           (fn [[idx x-relative y-relative]]
                             [(gdal/get data idx) n x-relative y-relative])
                           idx-map)
              [cluster] (cluster-fn idx-mapping)]
          (doseq [[[x-relative y-relative] idxs] cluster
                  :let [datapoints (->> (map idx-mapping idxs)
                                        (map first))]]
            (if (< 1 (count idxs))
              (let [pos [(mod n cpl-ctn)
                         (Math/floor (/ n cpl-ctn))]
                    offsets [offset-x (+ offset-y header)]]
                (when (some (fn [datapoint]
                              (get annotations [(grc/get-id datapoint)
                                                (grc/get-bucket datapoint)]))
                            datapoints)
                  (swap! relevant-annotations conj [pos offsets x-relative y-relative])))
              (when (seq idxs)
                (let [data (ffirst idx-mapping)
                      pos [(mod n cpl-ctn)
                           (Math/floor (/ n cpl-ctn))]
                      offsets [offset-x (+ offset-y header)]]
                  (when (get annotations [(grc/get-id data)
                                          (grc/get-bucket data)])
                    (swap! relevant-annotations conj [pos offsets x-relative y-relative]))))))
          (recur (inc n)))
        @relevant-annotations))))

(defn relevant-highlights [instance _stage-key render-path ctx highlights _parent-grouped? cluster-fn]
  (let [{:keys [path]}
        (gre/state instance)
        {{:keys [cpl-ctn count-ctn header]} :params :keys [offset-absolute]
         {:keys [mapping]} :optional-desc}
        ctx
        data-path (common/data-path render-path)
        data (get-data path data-path)
        [offset-x offset-y] offset-absolute
        relevant-highlights (atom [])]
    (loop [n 0]
      (if (< n count-ctn)
        (let [idx-map (get mapping n)
              idx-mapping (mapv
                           (fn [[idx x-relative y-relative]]
                             [(gdal/get data idx) n x-relative y-relative])
                           idx-map)
              [cluster] (cluster-fn idx-mapping)]
          (doseq [[[x-relative y-relative] idxs] cluster
                  :let [datapoints (->> (map idx-mapping idxs)
                                        (map first))]]
            (if (< 1 (count idxs))
              (let [pos [(mod n cpl-ctn)
                         (Math/floor (/ n cpl-ctn))]
                    offsets [offset-x (+ offset-y header)]]
                (when (and highlights
                           (some (fn [datapoint]
                                   (highlights (grc/get-id datapoint)))
                                 datapoints))
                  (swap! relevant-highlights conj [[pos offsets x-relative y-relative]
                                                   [n (->> datapoints
                                                           (map grc/get-id)
                                                           (into #{}))]])))
              (when (seq idxs)
                (let [data (ffirst idx-mapping)
                      pos [(mod n cpl-ctn)
                           (Math/floor (/ n cpl-ctn))]
                      offsets [offset-x (+ offset-y header)]]
                  (when (and highlights (highlights (grc/get-id data)))
                    (swap! relevant-highlights conj [[pos offsets x-relative y-relative]
                                                     [n #{(grc/get-id data)}]]))))))
          (recur (inc n)))
        @relevant-highlights))))

(defn- draw-annotation
  [[[card-x card-y] [offset-x offset-y] x-relative y-relative] width height margin instance stage overview-factor]
  (let [x
        (* overview-factor
           (+ offset-x
              (* (+ width (* 2 margin)) -0.5)
              x-relative
              (* card-x (+ width (* 2 margin)))
              (* 0.25 width)))
        y (* overview-factor
             (+ offset-y
                (* (+ height (* 2 margin)) -0.5)
                y-relative
                (* card-y (+ height (* 2 margin)))
                (* 0.25 height)))]
    (gre/img instance
             stage
             "speech-bubble"
             x
             y
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

(defn coords-highlight [[[card-x card-y] [offset-x offset-y] x-relative y-relative] width height margin]
  [(+ offset-x
      (* (+ width (* 2 margin)) -0.5)
      x-relative
      (* card-x (+ width (* 2 margin))))
   (+ offset-y
      (* (+ height (* 2 margin)) -0.5)
      y-relative
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

(defn index [ctx idx]
  (let [mapping (get-in ctx [:optional-desc :mapping])]
    (loop [mapping mapping]
      (let [[c-idx content] (first mapping)
            found (or (some #{idx} content)
                      (first (filter #(some #{idx} %) content)))] ;! Not sure if this is totaly correct
        (if (or found
                (empty? mapping))
          c-idx
          (recur (rest mapping)))))))
