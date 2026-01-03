(ns de.explorama.frontend.map.map.impl.openlayers.state-handler
  (:require ["ol/PluggableMap" :as PluggableMapModule]
            ["ol/extent" :as extent]
            ["ol/proj" :as proj]
            [clojure.set :as set]
            [de.explorama.frontend.ui-base.utils.interop :refer [safe-number?]]
            [de.explorama.frontend.map.map.impl.openlayers.feature-layers.area :as area]
            [de.explorama.frontend.map.map.impl.openlayers.feature-layers.heatmap :as heatmap]
            [de.explorama.frontend.map.map.impl.openlayers.feature-layers.movement :as movement]
            [de.explorama.frontend.map.map.impl.openlayers.util :refer [add-mouse-leave-handler
                                                                        find-cluster-for-feature fit-view-opts gen-popup-content get-view-port
                                                                        marker-data->circle-style]]
            [de.explorama.frontend.map.map.protocol.object-manager :as object-proto]
            [de.explorama.frontend.map.map.protocol.state-handler :as proto]
            [de.explorama.frontend.map.map.util :as util]
            [de.explorama.frontend.woco.workarounds.map :as workarounds]
            [taoensso.timbre :refer [warn]]))

(def PluggableMap (.-default PluggableMapModule))

(defonce ^:private db (atom {}))

(defn- render-map [frame-id {track-view-position-fn :track-view-position-change} obj-manager-instance]
  (let [map-obj (object-proto/map-instance obj-manager-instance)
        [cluster-layer] (object-proto/get-cluster-layer obj-manager-instance)]
    (cond
      (and map-obj
           (not (.getTarget map-obj)))
      (do (.setTarget map-obj (util/map-canvas-id frame-id))
          (.renderSync map-obj)
          (add-mouse-leave-handler map-obj track-view-position-fn))
      map-obj (.renderSync map-obj))
    (when (and cluster-layer
               (.get cluster-layer "addedToMap"))
      (.refresh (.getSource cluster-layer)))))

(defn- update-zoom-layers [obj-manager frame-id]
  (when-let [base-layer-obj (object-proto/get-base-layer-obj obj-manager
                                                             (get-in @db [frame-id :current-base]))]
    (let [new-max-zoom (.getMaxZoom base-layer-obj)
          new-min-zoom (.getMinZoom base-layer-obj)
          map (object-proto/map-instance obj-manager)
          [cluster-layer] (object-proto/get-cluster-layer obj-manager)]

      (when-let [marker-layer (object-proto/get-marker-layer obj-manager)]
        (.setMaxZoom marker-layer new-max-zoom)
        (.setMinZoom marker-layer new-min-zoom))

      (when cluster-layer
        (.setMaxZoom cluster-layer new-max-zoom)
        (.setMinZoom cluster-layer new-min-zoom))

      (when-let [view-obj (when map
                            (.getView map))]
        (.setMaxZoom view-obj new-max-zoom)
        (.setMinZoom view-obj new-min-zoom)))))

(defn- switch-base-layer [obj-manager frame-id base-layer-id]
  (when (seq base-layer-id)
    (let [map-obj (object-proto/map-instance obj-manager)
          base-layer-obj (object-proto/get-base-layer-obj obj-manager base-layer-id)
          layers (.getLayers map-obj)]
      (.forEach layers (fn [layer _ _]
                         (when (and layer
                                    (.get layer "base"))
                           (.removeLayer map-obj layer))))
      (swap! db assoc-in [frame-id :current-base] base-layer-id)
      (update-zoom-layers obj-manager frame-id)
      (when base-layer-obj
        (.insertAt layers 0 base-layer-obj)))))

(defn- resize-map [obj-manager]
  (let [map-obj (object-proto/map-instance obj-manager)]
    (.updateSize map-obj)))

(defn- set-marker-data [frame-id marker-data]
  (swap! db assoc-in [frame-id :marker-data] marker-data))

(defn- get-marker-data [frame-id]
  (get-in @db [frame-id :marker-data]))

(defn- move-to-data [obj-manager max-data-zoom]
  (let [map-obj (object-proto/map-instance obj-manager)
        marker-layer (object-proto/get-marker-layer obj-manager)
        extent (when marker-layer (.getExtent (.getSource marker-layer)))
        area-size (when extent (extent/getArea extent))
        view-obj (.getView map-obj)
        new-zoom (min (.getMaxZoom view-obj)
                      max-data-zoom)
        new-center (when extent (extent/getCenter extent))]
    (cond (> area-size 0)
          (.fit view-obj
                extent
                fit-view-opts)
          (and (safe-number? new-zoom)
               (every? safe-number? (array-seq new-center)))
          (do
            (.setZoom view-obj (min (.getMaxZoom view-obj)
                                    max-data-zoom))
            (.setCenter view-obj (extent/getCenter extent))))))

(defn- display-markers [obj-manager {max-data-zoom :move-data-max-zoom} frame-id]
  (let [map-obj (object-proto/map-instance obj-manager)
        [cluster-layer
         cluster-interaction
         cluster-hover
         hover-layer] (object-proto/get-cluster-layer obj-manager)
        marker-layer (object-proto/get-marker-layer obj-manager)]
    (update-zoom-layers obj-manager frame-id)
    (when (.get cluster-layer "addedToMap")
      (.removeLayer map-obj cluster-layer)
      (.removeLayer map-obj hover-layer)
      (.removeInteraction map-obj cluster-interaction)
      (.removeInteraction map-obj cluster-hover)
      (.set cluster-layer "addedToMap" false))

    (when-not (.get marker-layer "addedToMap")
      (.addLayer map-obj marker-layer)
      (.set marker-layer "addedToMap" true))

    (move-to-data obj-manager @(max-data-zoom))

    (when (.getTarget map-obj)
      (.renderSync map-obj))

    (swap! db assoc-in [frame-id :clustering?] false)))

(defn- display-marker-cluster [obj-manager {max-data-zoom :move-data-max-zoom} frame-id]
  (let [map-obj (object-proto/map-instance obj-manager)
        [cluster-layer
         cluster-interaction
         cluster-hover
         hover-layer] (object-proto/get-cluster-layer obj-manager)
        marker-layer (object-proto/get-marker-layer obj-manager)]
    (update-zoom-layers obj-manager frame-id)

    (when (and marker-layer (.get marker-layer "addedToMap"))
      (.removeLayer map-obj marker-layer)
      (.set marker-layer "addedToMap" false))

    (when (and cluster-layer (not (.get cluster-layer "addedToMap")))
      (.addLayer map-obj cluster-layer)
      (.addLayer map-obj hover-layer)
      (.addInteraction map-obj cluster-interaction)
      (.addInteraction map-obj cluster-hover)
      (.set cluster-layer "addedToMap" true))

    (move-to-data obj-manager @(max-data-zoom))

    (when (.getTarget map-obj)
      (.renderSync map-obj))

    (swap! db assoc-in [frame-id :clustering?] true)))

(defn- marker-highlighted? [frame-id marker-id]
  (let [highlighted-marker (get-in @db [frame-id :highlighted-marker] #{})]
    (boolean (highlighted-marker marker-id))))

(defn- list-highlighted-marker [frame-id]
  (get-in @db [frame-id :highlighted-marker] #{}))

(defn- update-open-cluster-styles
  "Iterates over a current marker-cluster and re-apply the styles."
  [obj-manager
   {marker-stroke-color-fn :marker-stroke-rgb-color
    highlighted-marker-stroke-color-fn :highlighted-marker-stroke-rgb-color
    marker-highlighted-fn? :event-highlighted?}]
  (let [[cluster-layer
         cluster-interaction] (object-proto/get-cluster-layer obj-manager)]
    (when (and cluster-layer
               cluster-interaction
               (.get cluster-layer "addedToMap"))
      (let [source (.getSource (.getLayer cluster-interaction))]
        (doseq [f (array-seq (.getFeatures source))]
          (let [sel (.get f "features")
                feature (when sel (aget sel "0"))]
            (when feature
              (.setStyle f
                         (marker-data->circle-style (partial marker-highlighted-fn? (.get feature "id"))
                                                    marker-stroke-color-fn
                                                    highlighted-marker-stroke-color-fn
                                                    (.get feature "data"))))))))))

(defn- highlight-marker [obj-manager
                         {marker-stroke-color-fn :marker-stroke-rgb-color
                          highlighted-marker-stroke-color-fn :highlighted-marker-stroke-rgb-color
                          :as extra-fns}
                         frame-id
                         marker-id]
  (when-let [marker-obj (get (object-proto/get-marker-objs obj-manager [marker-id]) marker-id)]
    (let [feature-data (.get marker-obj "data")
          style-desc (marker-data->circle-style (constantly true)
                                                marker-stroke-color-fn
                                                highlighted-marker-stroke-color-fn
                                                feature-data)]
      (.setStyle marker-obj style-desc)
      (swap! db update-in [frame-id :highlighted-marker] (fnil conj #{}) marker-id)

      (update-open-cluster-styles obj-manager extra-fns))))

(defn- de-highlight-marker [obj-manager
                            {marker-stroke-color-fn :marker-stroke-rgb-color
                             highlighted-marker-stroke-color-fn :highlighted-marker-stroke-rgb-color
                             :as extra-fns}
                            frame-id
                            marker-id]
  (let [marker-obj (get (object-proto/get-marker-objs obj-manager [marker-id]) marker-id)
        feature-data (.get marker-obj "data")
        style-desc (marker-data->circle-style (constantly false)
                                              marker-stroke-color-fn
                                              highlighted-marker-stroke-color-fn
                                              feature-data)]
    (.setStyle marker-obj style-desc)

    (swap! db update-in [frame-id :highlighted-marker] disj marker-id)

    (update-open-cluster-styles obj-manager extra-fns)))

(defn- update-marker-styles [obj-manager
                             {marker-stroke-color-fn :marker-stroke-rgb-color
                              highlighted-marker-stroke-color-fn :highlighted-marker-stroke-rgb-color
                              :as extra-fns}
                             frame-id
                             to-update-ids]
  (let [marker-data (get-marker-data frame-id)
        all-markers (object-proto/get-marker-objs obj-manager to-update-ids)]

    (doseq [[k m] all-markers]
      (let [feature-data (get marker-data k)
            style-desc (marker-data->circle-style (partial marker-highlighted? k)
                                                  marker-stroke-color-fn
                                                  highlighted-marker-stroke-color-fn
                                                  feature-data)]
        (.setStyle m style-desc)
        (.set m "data" feature-data)))

    (update-open-cluster-styles obj-manager extra-fns)))

(defn- cache-event-data [frame-id event-id event-data]
  (swap! db assoc-in [frame-id :events event-id] event-data))

(defn- cached-event-data [frame-id event-id]
  (get-in @db [frame-id :events event-id]))

(defn- hide-popup [_ obj-manager]
  (let [popup-obj (object-proto/get-popup obj-manager)]
    (.hide popup-obj)))

(defn- display-popup [frame-id
                      {localize-num-fn :localize-number
                       attribute-label-fn :attribute-label
                       :as extra-fns}
                      obj-manager
                      coordinates
                      {:keys [data
                              title-color title-attributes
                              display-attributes
                              area-feature-id]}]
  (let [popup-obj (object-proto/get-popup obj-manager)
        popup-content (gen-popup-content localize-num-fn
                                         attribute-label-fn
                                         title-color
                                         data
                                         title-attributes
                                         display-attributes)
        [lat lon] coordinates
        coords (proj/fromLonLat #js[lon lat])]
    (.setProperty (aget popup-obj "element" "style")
                  "--layoutColor"
                  title-color)
    (if (seq popup-content)
      (.show popup-obj
             coords
             popup-content)
      (hide-popup frame-id obj-manager))
    (update-open-cluster-styles obj-manager extra-fns)))

(defn- move-to [obj-manager zoom position]
  (let [map (object-proto/map-instance obj-manager)
        view (.getView map)
        [lat lon] position]
    (when (and zoom position)
      (.setZoom view zoom)
      (.setCenter view (proj/fromLonLat #js[lon lat]))
      (when (.getTarget map)
        (.renderSync map)))))

(defn- get-view-position [obj-manager]
  (get-view-port (object-proto/map-instance obj-manager)))

(defn- select-cluster-with-marker [obj-manager marker-id]
  (let [feature-obj (get (object-proto/get-marker-objs obj-manager [marker-id]) marker-id)
        [cluster-layer
         cluster-interaction] (object-proto/get-cluster-layer obj-manager)
        cluster-obj (when (and feature-obj cluster-layer)
                      (find-cluster-for-feature cluster-layer
                                                feature-obj))]
    (if cluster-obj
      (do (.selectCluster cluster-interaction #js{:type "select"
                                                  :selected #js[cluster-obj]
                                                  :deselected #js[]})
          (if-let [cluster-obj (find-cluster-for-feature (.getLayer cluster-interaction)
                                                         feature-obj)]
            (do (.selectCluster cluster-interaction #js{:type "select"
                                                        :selected #js[cluster-obj]
                                                        :deselected #js[]})
                (.push (.getFeatures cluster-interaction) cluster-obj))
            (warn "No cluster-obj found in overlayer, can't select" {:feature-obj feature-obj
                                                                     :marker-id marker-id
                                                                     :overlayer-source (.getLayer cluster-interaction)
                                                                     :cluster-layer cluster-layer})))
      (warn "No cluster-obj found can't select." {:feature-obj feature-obj
                                                  :marker-id marker-id
                                                  :cluster-layer cluster-layer}))))

(defn- move-to-marker [obj-manager {move-data-max-zoom :move-data-max-zoom} frame-id marker-id]
  (let [map-obj (object-proto/map-instance obj-manager)
        view-obj (.getView map-obj)
        marker-obj (get (object-proto/get-marker-objs obj-manager [marker-id])
                        marker-id)
        clustering? (get-in @db [frame-id :clustering?])
        [cluster-layer] (object-proto/get-cluster-layer obj-manager)]
    (if clustering?
      (loop []
        (let [cluster-obj (find-cluster-for-feature cluster-layer
                                                    marker-obj)
              cluster (.get cluster-obj "features")
              all-coords (clj->js (map (fn [f]
                                         (.getFirstCoordinate
                                          (.getGeometry f)))
                                       (array-seq cluster)))
              coords-extent (extent/boundingExtent all-coords)
              area-size (extent/getArea coords-extent)]

          (if  (> area-size 0)
            (do (.fit view-obj
                      coords-extent
                      fit-view-opts)
                (.renderSync map-obj)
                (recur))
            (do
              (.setCenter view-obj (extent/getCenter coords-extent))
              (select-cluster-with-marker obj-manager marker-id)))))
      (do
        (.setZoom view-obj @(move-data-max-zoom))
        (.setCenter view-obj (-> marker-obj
                                 (.getGeometry)
                                 (.getFirstCoordinate)))))))

(defn- hide-markers-with-id [obj-manager marker-ids]
  (let [marker-layer (object-proto/get-marker-layer obj-manager)
        marker-source (.getSource marker-layer)
        all-ids (object-proto/marker-ids obj-manager)
        to-display-marker-ids (set/difference (set all-ids)
                                              (set marker-ids))
        to-display-marker-objs (->> to-display-marker-ids
                                    (object-proto/get-marker-objs obj-manager)
                                    vals
                                    clj->js)]
    (if (seq marker-ids)
      (do (.clear marker-source)
          (if (seq to-display-marker-ids)
            (.addFeatures marker-source to-display-marker-objs)
            (warn "All marker ids where used to hide. No data to show anymore." {:given-ids-count (count marker-ids)
                                                                                 :available-ids-count (count all-ids)})))
      (warn "No markern-ids given to hide."))))
(defn- display-all-markers [obj-manager]
  (let [marker-layer (object-proto/get-marker-layer obj-manager)
        marker-source (.getSource marker-layer)
        all-ids (object-proto/marker-ids obj-manager)
        all-marker-objs (->> all-ids
                             (object-proto/get-marker-objs obj-manager)
                             vals
                             clj->js)]
    (when (seq all-marker-objs)
      (.clear marker-source)
      (.addFeatures marker-source all-marker-objs))))

(defn- temp-hide-marker-layer [obj-manager]
  (let [map-obj (object-proto/map-instance obj-manager)
        [cluster-layer
         _
         _
         hover-layer] (object-proto/get-cluster-layer obj-manager)
        marker-layer (object-proto/get-marker-layer obj-manager)
        marker-source (when marker-layer (.getSource marker-layer))]
    (when (and marker-layer (.get marker-layer "addedToMap"))
      (.removeLayer map-obj marker-layer)
      (.set marker-layer "tempHidden" true))

    (when (and cluster-layer (.get cluster-layer "addedToMap"))
      (.removeLayer map-obj cluster-layer)
      (.removeLayer map-obj hover-layer)
      (.set cluster-layer "tempHidden" true))

    (when marker-source
      (.clear marker-source))

    (when (.getTarget map-obj)
      (.renderSync map-obj))))

(defn- restore-temp-hidden-marker-layer [obj-manager]
  (let [map-obj (object-proto/map-instance obj-manager)
        [cluster-layer
         _
         _
         hover-layer] (object-proto/get-cluster-layer obj-manager)
        marker-layer (object-proto/get-marker-layer obj-manager)
        marker-source (when marker-layer (.getSource marker-layer))]
    (when (and marker-source
               (or (.get marker-layer "tempHidden")
                   (.get cluster-layer "tempHidden")))
      (let [all-ids (object-proto/marker-ids obj-manager)
            all-markers (-> (object-proto/get-marker-objs obj-manager all-ids)
                            vals
                            clj->js)]
        (when (and all-markers
                   (seq all-ids)
                   (= (aget (.getFeatures marker-source) "length") 0))
          (.addFeatures marker-source all-markers))))

    (when (and marker-layer
               (.get marker-layer "addedToMap")
               (.get marker-layer "tempHidden"))
      (.addLayer map-obj marker-layer)
      (.set marker-layer "tempHidden" false))

    (when (and cluster-layer
               (.get cluster-layer "addedToMap")
               (.get cluster-layer "tempHidden"))
      (.addLayer map-obj cluster-layer)
      (.addLayer map-obj hover-layer)
      (.set cluster-layer "tempHidden" false))

    (when (.getTarget map-obj)
      (.renderSync map-obj))))

(defn- display-overlayer [obj-manager frame-id overlayer-id]
  (let [map-obj (object-proto/map-instance obj-manager)
        overlayer-obj (object-proto/get-overlayer-obj obj-manager overlayer-id)]
    (when (and map-obj overlayer-obj)
      (.addLayer map-obj overlayer-obj)
      (aset overlayer-obj "addedToMap" true)
      (swap! db update-in [frame-id :overlayer]
             (fnil conj #{}) overlayer-id))))

(defn- list-active-overlayers [frame-id]
  (get-in @db [frame-id :overlayer] #{}))

(defn- hide-overlayer [obj-manager frame-id overlayer-id]
  (let [map-obj (object-proto/map-instance obj-manager)
        overlayer-obj (object-proto/get-overlayer-obj obj-manager overlayer-id)]
    (when (and map-obj overlayer-obj)
      (.removeLayer map-obj overlayer-obj)
      (aset overlayer-obj "addedToMap" false)
      (swap! db update-in [frame-id :overlayer]
             disj overlayer-id))))

(defn- set-feature-data [frame-id feature-data]
  (swap! db assoc-in [frame-id :feature-data] feature-data))

(defn- get-feature-data [frame-id feature-layer-id]
  (get-in @db [frame-id :feature-data feature-layer-id]))

(defn- set-filtered-feature-data [frame-id filtered-feature-data]
  (swap! db assoc-in [frame-id :filtered-feature-data] filtered-feature-data))

(defn- get-filtered-feature-data [frame-id feature-layer-id]
  (get-in @db [frame-id :filtered-feature-data feature-layer-id]))

(defn- display-feature-layer [frame-id obj-manager-instance feature-layer-id]
  (let [{layer-type :type
         feature-layer-obj :layer} (object-proto/get-feature-layer-obj
                                    obj-manager-instance feature-layer-id)
        map-instance (object-proto/map-instance obj-manager-instance)]
    (when (not= true (some #(= feature-layer-id %)
                           (get-in @db [frame-id :active-feature-layers])))
      (case layer-type
        :movement (movement/display-layer map-instance feature-layer-obj)
        :feature (area/display-layer map-instance feature-layer-obj)
        :heatmap (heatmap/display-layer map-instance feature-layer-obj)
        (warn "Unknown layer-type used" {:layer-id feature-layer-id
                                         :type layer-type})))
    (swap! db update-in [frame-id :active-feature-layers] (fnil conj #{}) feature-layer-id)))

(defn- hide-feature-layer [frame-id obj-manager-instance feature-layer-id _]
  (let [{layer-type :type
         feature-layer-obj :layer} (object-proto/get-feature-layer-obj obj-manager-instance
                                                                       feature-layer-id)
        map-instance (object-proto/map-instance obj-manager-instance)]
    (case layer-type
      :movement (do (movement/hide-layer map-instance feature-layer-obj)
                    (object-proto/clear-arrow-features obj-manager-instance feature-layer-id))
      :feature (area/hide-layer map-instance feature-layer-obj)
      :heatmap (heatmap/hide-layer map-instance feature-layer-obj)
      (warn "Unknown layer-type used" {:layer-id feature-layer-id
                                       :type layer-type}))
    (swap! db update-in [frame-id :active-feature-layers] disj feature-layer-id)
    (swap! db update-in [frame-id :feature-data] dissoc feature-layer-id)))

(defn- list-active-feature-layers [frame-id]
  (get-in @db [frame-id :active-feature-layers] #{}))

(defn- clear-feature-layers [frame-id obj-manager-instance]
  (let [all-ids (keys (object-proto/all-feature-layers obj-manager-instance))]
    (doseq [id all-ids]
      (object-proto/remove-feature-layer obj-manager-instance id))
    (swap! db assoc-in [frame-id :active-feature-layers] #{})))

(defn- remove-feature-layer [frame-id obj-manager-instance feature-layer-id _]
  (object-proto/remove-feature-layer obj-manager-instance feature-layer-id)
  (swap! db update-in [frame-id :active-feature-layers] disj feature-layer-id))

(defn- destroy-instance [frame-id]
  (swap! db dissoc frame-id))

(defn- one-time-render-done-listener [obj-manager listener-fn]
  (let [map (object-proto/map-instance obj-manager)]
    (.once map "loadend" listener-fn)))

(deftype OpenlayersStateHandler [frame-id
                                 obj-manager-instance
                                 extra-fns]
  proto/mapStateHandler
  (render-map [_]
    (render-map frame-id extra-fns obj-manager-instance))

  (one-time-render-done-listener [_ listener-fn]
    (one-time-render-done-listener obj-manager-instance listener-fn))

  (move-to-data [_]
    (move-to-data obj-manager-instance
                  @((:move-data-max-zoom extra-fns))))
  (move-to-marker [_ marker-id]
    (move-to-marker obj-manager-instance extra-fns frame-id marker-id))

  (set-marker-data [_ marker-data]
    (set-marker-data frame-id marker-data))

  (get-marker-data [_]
    (get-marker-data frame-id))

  (display-marker-cluster [_]
    (display-marker-cluster obj-manager-instance extra-fns frame-id))
  (display-markers [_]
    (display-markers obj-manager-instance extra-fns frame-id))
  (update-marker-styles [_ to-update-ids]
    (update-marker-styles obj-manager-instance extra-fns frame-id to-update-ids))
  (marker-higlighted? [_ marker-id]
    (marker-highlighted? frame-id marker-id))
  (list-highlighted-marker [_]
    (list-highlighted-marker frame-id))
  (highlight-marker [_ marker-id]
    (highlight-marker obj-manager-instance
                      extra-fns
                      frame-id
                      marker-id))
  (de-highlight-marker [_ marker-id]
    (de-highlight-marker obj-manager-instance
                         extra-fns
                         frame-id
                         marker-id))

  (temp-hide-marker-layer [_]
    (temp-hide-marker-layer obj-manager-instance))

  (restore-temp-hidden-marker-layer [_]
    (restore-temp-hidden-marker-layer obj-manager-instance))

  (hide-markers-with-id [_ marker-ids]
    (hide-markers-with-id obj-manager-instance marker-ids))
  (display-all-markers [_]
    (display-all-markers obj-manager-instance))

  (cache-event-data [_ event-id event-data]
    (cache-event-data frame-id event-id event-data))
  (cached-event-data [_ event-id]
    (cached-event-data frame-id event-id))

  (set-feature-data [_ feature-data]
    (set-feature-data frame-id feature-data))
  (get-feature-data [_ feature-layer-id]
    (get-feature-data frame-id feature-layer-id))
  (set-filtered-feature-data [_ feature-data]
    (set-filtered-feature-data frame-id feature-data))
  (get-filtered-feature-data [_ feature-layer-id]
    (get-filtered-feature-data frame-id feature-layer-id))

  (display-feature-layer [_ feature-layer-id]
    (display-feature-layer frame-id obj-manager-instance feature-layer-id))
  (hide-feature-layer [_ feature-layer-id]
    (hide-feature-layer frame-id obj-manager-instance feature-layer-id extra-fns))
  (remove-feature-layer [_ feature-layer-id]
    (remove-feature-layer frame-id obj-manager-instance feature-layer-id extra-fns))
  (clear-feature-layers [_]
    (clear-feature-layers frame-id obj-manager-instance))
  (list-active-feature-layers [_]
    (list-active-feature-layers frame-id))

  (display-overlayer [_ overlayer-id]
    (display-overlayer obj-manager-instance frame-id overlayer-id))
  (list-active-overlayers [_]
    (list-active-overlayers frame-id))
  (hide-overlayer [_ overlayer-id]
    (hide-overlayer obj-manager-instance frame-id overlayer-id))

  (switch-base-layer [_ base-layer-id]
    (switch-base-layer obj-manager-instance frame-id base-layer-id))

  (resize-map [_]
    (resize-map obj-manager-instance))
  (move-to [_ zoom position]
    (move-to obj-manager-instance zoom position))
  (view-position [_]
    (get-view-position obj-manager-instance))

  (select-cluster-with-marker [_ marker-id]
    (select-cluster-with-marker obj-manager-instance marker-id))

  (display-popup [_ position content-desc]
    (display-popup frame-id
                   extra-fns
                   obj-manager-instance
                   position
                   content-desc))
  (hide-popup [_]
    (hide-popup frame-id obj-manager-instance))

  (destroy-instance [_]
    (destroy-instance frame-id)))

(defn- set-event-pixel-fn [workspace-scale-fn]
  (when-not @workarounds/initialized?
    ;Based on the given example from this issue
    ;https://github.com/openlayers/openlayers/issues/13283
    (aset (.-ol js/window) "PluggableMap" "prototype" "getEventPixel"
          (fn [event]
            (this-as this
                     (let [scale @(workspace-scale-fn)
                           viewportPosition (.getBoundingClientRect (.getViewport this))
                           size (clj->js
                                 [(aget viewportPosition "width")
                                  (aget viewportPosition "height")])]
                       (clj->js [(/ (/ (* (- (aget event "clientX")
                                             (aget viewportPosition "left"))
                                          (aget size "0"))
                                       (aget viewportPosition "width"))
                                    scale)
                                 (/ (/ (* (- (aget event "clientY")
                                             (aget viewportPosition "top"))
                                          (aget size "1"))
                                       (aget viewportPosition "height"))
                                    scale)])))))
    (reset! workarounds/initialized? true)))

(defn create-instance [frame-id obj-manager-instance {:keys [workspace-scale]
                                                      :as extra-fns}]
  (set-event-pixel-fn workspace-scale)
  (->OpenlayersStateHandler frame-id obj-manager-instance extra-fns))
