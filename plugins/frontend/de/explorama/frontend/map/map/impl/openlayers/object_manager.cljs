(ns de.explorama.frontend.map.map.impl.openlayers.object-manager
  (:require [cljsjs.openlayers]
            [cljsjs.openlayers-ol-ext]
            [de.explorama.frontend.map.map.impl.openlayers.feature-layers.area :as area]
            [de.explorama.frontend.map.map.impl.openlayers.feature-layers.heatmap :as heatmap]
            [de.explorama.frontend.map.map.impl.openlayers.feature-layers.movement :as movement]
            [de.explorama.frontend.map.map.impl.openlayers.overlayer :as overlayer]
            [de.explorama.frontend.map.map.impl.openlayers.util :refer [add-mouse-leave-handler
                                                                        cluster->cluster-style
                                                                        cluster-interaction-feature cluster-interaction-style map-click-handler
                                                                        map-double-click-handler map-hover-enter-handler map-hover-leave-handler
                                                                        marker-data->circle-style]]
            [de.explorama.frontend.map.map.protocol.object-manager :as proto]
            [de.explorama.frontend.map.map.util :as util]
            [taoensso.timbre :refer [warn]]
            [clojure.string :as str]))

(def ol-proj (aget js/ol "proj"))

(defonce ^:private db-state (atom {}))

(def ol-map (aget js/ol "Map"))
(def ol-tile-layer (aget js/ol "layer" "Tile"))
(def ol-layer-vectortile (aget js/ol "layer" "VectorTile"))
(def ol-source-vectortile (aget js/ol "source" "VectorTile"))
(def ol-view (aget js/ol "View"))

(def ol-feature (aget js/ol "Feature"))

(def ol-source-xyz (aget js/ol "source" "XYZ"))
(def ol-source-arcgis-rest (aget js/ol "source" "TileArcGISRest"))
(def ol-source-wms (aget js/ol "source" "TileWMS"))

(def ol-source-vector (aget js/ol "source" "Vector"))
(def ol-source-cluster (aget js/ol "source" "Cluster"))
(def ol-layer-animated-cluster (aget js/ol "layer" "AnimatedCluster"))
(def ol-layer-vector (aget js/ol "layer" "Vector"))

(def ol-geom-point (aget js/ol "geom" "Point"))

(def ol-interaction (aget js/ol "interaction"))
(def ol-interaction-select-cluster (aget ol-interaction "SelectCluster"))
(def ol-interaction-hover (aget ol-interaction "Hover"))

(def ol-overlaye-popup (aget js/ol "Overlay" "Popup"))

(def ol-events-condition-no-modifier (aget js/ol "events" "condition" "noModifierKeys"))

(defn- is-touch? [e]
  (= "touch" (aget e "pointerType")))

(defn- check-panning-cond [do-panning? map-browser-event]
  (when-let [pointer-event (aget map-browser-event "originalEvent")]
    (and (ol-events-condition-no-modifier map-browser-event)
         (aget pointer-event "isPrimary")
         (or (do-panning? (inc (aget pointer-event "button")))
             (is-touch? pointer-event)))))

(defn- custom-default-interactions [do-panning?]
  #js[(new (aget ol-interaction "DragPan")
           (clj->js {:condition (partial check-panning-cond do-panning?)}))
      (new (aget ol-interaction "PinchZoom"))
      (new (aget ol-interaction "KeyboardPan"))
      (new (aget ol-interaction "KeyboardZoom"))
      (new (aget ol-interaction "MouseWheelZoom"))])

(def ol-geojson (aget js/ol "format" "GeoJSON"))
(def ol-mvt (aget js/ol "format" "MVT"))
(def ol-overlay (aget js/ol "Overlay"))
(def ol-style-style  (aget js/ol "style" "Style"))
(def ol-style-fill  (aget js/ol "style" "Fill"))
(def ol-style-stroke  (aget js/ol "style" "Stroke"))
;TODO r1/attribution
(def ol-control-defaults (aget js/ol "control" "defaults"))
(def ol-control-attribution (aget js/ol "control" "Attribution"))


(defn- create-map-instance [frame-id headless? {marker-clicked-fn :marker-clicked
                                                hide-popup-fn :hide-popup
                                                highlight-event-fn :highlight-event
                                                marker-dbl-clicked-fn :marker-dbl-clicked
                                                track-view-position-fn :track-view-position-change
                                                overlayer-feature-clicked-fn :overlayer-feature-clicked
                                                area-feature-clicked-fn :area-feature-clicked
                                                active-feature-layer-fn :active-feature-layers
                                                do-panning? :do-panning?}]
  (let [popup-overlay (ol-overlaye-popup. #js{:popupClass "default"
                                              :closeBox true
                                              :onshow (fn [])
                                              :onclose (fn [] (hide-popup-fn))
                                              :positioning "auto"
                                              :autoPan true})
        obj-instance (ol-map.
                      (clj->js {:layers #js[]
                                :overlays #js[popup-overlay]
                                :controls (.extend (ol-control-defaults. #js{:attribution false})
                                                   #js[(ol-control-attribution. #js{:collapsible false})])
                                :interactions (custom-default-interactions do-panning?)
                                :target (when-not headless?
                                          (util/map-canvas-id frame-id))
                                :view (ol-view. #js{:center #js[0 0]
                                                    :zoom 2})}))
        current-objs-fn (fn [] (get @db-state frame-id))]
    (when-not headless?
      (add-mouse-leave-handler obj-instance track-view-position-fn))

    (.on obj-instance
         "click"
         (partial map-click-handler current-objs-fn active-feature-layer-fn marker-clicked-fn
                  overlayer-feature-clicked-fn area-feature-clicked-fn highlight-event-fn hide-popup-fn))
    (.on obj-instance
         "dblclick"
         (partial map-double-click-handler current-objs-fn marker-dbl-clicked-fn))
    (swap! db-state update frame-id assoc
           :map obj-instance
           :popup popup-overlay)
    obj-instance))

(defn- map-instance [frame-id]
  (get-in @db-state [frame-id :map]))

(defn- create-base-layer [frame-id {:keys [tilemap-server-url type
                                           wms-layers attribution
                                           max-zoom min-zoom name]
                                    :or {min-zoom 1
                                         type "default"}}]
  (let [tile-source (case type
                      "esri" (ol-source-arcgis-rest.
                              #js{:url tilemap-server-url
                                  :projection "EPSG:3857"
                                  :attributions attribution
                                  :crossOrigin ""})
                      "wms" (ol-source-wms.
                             (clj->js
                              {:url tilemap-server-url
                               :params {"LAYERS" (str/split wms-layers #",")
                                        "TILED" true}
                               :serverType "geoserver"
                               :projection "EPSG:3857"
                               :crossOrigin ""
                               :attributions attribution
                               :transition 0}))
                      ("default" "tms")
                      (ol-source-xyz.
                       #js{:url tilemap-server-url
                           :projection "EPSG:3857"
                           :attributions attribution
                           :crossOrigin ""}))
        obj-base-layer (ol-tile-layer.
                        (clj->js {:source tile-source
                                  :maxZoom max-zoom
                                  :minZoom min-zoom}))]
    (.set obj-base-layer "base" true)
    (swap! db-state assoc-in [frame-id :base-layer name] obj-base-layer)))

(defn- get-base-layer-obj [frame-id base-layer-id]
  (get-in @db-state [frame-id :base-layer base-layer-id]))

(defn- create-marker-layer [frame-id {max-marker-hover :max-hover-marker
                                      hide-popup-fn :hide-popup
                                      marker-stroke-color-fn :marker-stroke-rgb-color
                                      highlighted-marker-stroke-color-fn :highlighted-marker-stroke-rgb-color
                                      marker-highlighted-fn? :event-highlighted?
                                      localize-number-fn :localize-number}]
  (when-not (get-in @db-state [frame-id :marker-layer])
    (let [vector-source (ol-source-vector. #js{:projection "EPSG:900913"})
          vector-layer (ol-layer-vector. #js{:source vector-source})

          cluster-source (ol-source-cluster. #js{:distance 80 ;;TODO r1/config
                                                 :source vector-source})
          cluster-layer (ol-layer-animated-cluster. #js{:name "Cluster"
                                                        :source cluster-source
                                                        :animationDuration 700
                                                        :style (partial cluster->cluster-style localize-number-fn)})
          cluster-interaction (ol-interaction-select-cluster.
                               #js{:pointRadius 35
                                   :selectCluster true
                                   :spiral true
                                   :layers #js[cluster-layer]
                                   :featureStyle (partial cluster-interaction-feature
                                                          marker-highlighted-fn?
                                                          marker-stroke-color-fn
                                                          highlighted-marker-stroke-color-fn)
                                   :condition (aget js/ol "events" "condition" "click")
                                   ;:toggleCondition (aget js/ol "events" "condition" "click")
                                   ;:toggle true
                                   ;:multi true
                                   :style (partial cluster-interaction-style
                                                   localize-number-fn
                                                   (map-instance frame-id))})
          hover-source (ol-source-vector. #js{:projection "EPSG:900913"})
          hover-layer (ol-layer-vector. #js{:source hover-source})
          hover-interaction (ol-interaction-hover.
                             #js{:cursor "pointer"
                                 :layerFilter (fn [l]
                                                (= l cluster-layer))})]
      (.setZIndex hover-layer 1)
      (.setZIndex (.getLayer cluster-interaction) 2)
      (.setZIndex cluster-layer 3)

      (.on hover-interaction "enter"
           (partial map-hover-enter-handler hover-source max-marker-hover))

      (.on hover-interaction "leave"
           (partial map-hover-leave-handler hover-source))

      (.on (.getFeatures cluster-interaction)
           "remove"
           (fn [_]
             (hide-popup-fn)))

      (swap! db-state update frame-id assoc
             :marker-source vector-source
             :marker-layer vector-layer
             :cluster-layer cluster-layer
             :cluster-interaction cluster-interaction
             :cluster-hover hover-interaction
             :hover-layer hover-layer))))

(defn- remove-marker-layer [frame-id _]
  (let [{:keys [marker-layer
                cluster-layer
                cluster-interaction
                cluster-hover
                hover-layer]} (get @db-state frame-id)
        geo-obj (map-instance frame-id)]
    (if geo-obj
      (do
        (.removeLayer geo-obj marker-layer)
        (.removeLayer geo-obj cluster-layer)
        (.removeLayer geo-obj hover-layer)
        (.removeInteraction geo-obj cluster-interaction)
        (.removeInteraction geo-obj cluster-hover)
        (swap! db-state update frame-id dissoc
               :marker-source
               :marker-layer
               :cluster-layer
               :cluster-interaction
               :cluster-hover))
      (do
        (warn "No map instance exist. Removing all objects for this frame." frame-id)
        (swap! db-state dissoc frame-id)
        nil))))

(defn- get-cluster-layer [frame-id]
  [(get-in @db-state [frame-id :cluster-layer])
   (get-in @db-state [frame-id :cluster-interaction])
   (get-in @db-state [frame-id :cluster-hover])
   (get-in @db-state [frame-id :hover-layer])])

(defn- get-marker-layer [frame-id]
  (get-in @db-state [frame-id :marker-layer]))

(defn- marker-layer-created? [frame-id]
  (boolean (get-in @db-state [frame-id :marker-source])))

(defn- marker-created? [frame-id marker-id]
  (boolean (get-in @db-state [frame-id :markers marker-id])))

(defn- get-marker-ids [frame-id]
  (let [all-markers (get-in @db-state [frame-id :markers] {})]
    (vec (keys all-markers))))

(defn- marker-objs [frame-id marker-ids]
  (-> (get-in @db-state [frame-id :markers] {})
      (select-keys marker-ids)))

(defn- remove-marker [frame-id marker-ids {hide-popup :hide-popup
                                           get-popup-desc :get-popup-desc}]
  (let [all-markers (vals (marker-objs frame-id marker-ids))
        marker-layer (get-in @db-state [frame-id :marker-source])]
    (if marker-layer
      (do
        (when (and (:event-id (get-popup-desc))
                   (:event-color (get-popup-desc)))
          (hide-popup))
        (doseq [m all-markers]
          (.removeFeature marker-layer m))
        (swap! db-state update-in [frame-id :markers]
               (fn [markers]
                 (apply dissoc markers marker-ids))))
      (do
        (warn "No marker-layer exist to remove the markers from. Removing marker-objs from internal state.")
        (swap! db-state update frame-id dissoc :markers)
        nil))))

(defn- clear-markers [frame-id {hide-popup :hide-popup
                                get-popup-desc :get-popup-desc}]
  (when-let [marker-layer (get-in @db-state [frame-id :marker-source])]
    (when (and (:event-id (get-popup-desc))
               (:event-color (get-popup-desc)))
      (hide-popup))
    (.clear marker-layer)
    (swap! db-state assoc-in [frame-id :markers] {})))

(defn- create-markers [frame-id
                       {marker-stroke-color-fn :marker-stroke-rgb-color
                        highlighted-marker-stroke-color-fn :highlighted-marker-stroke-rgb-color
                        marker-highlighted-fn? :event-highlighted?}
                       markers-data]
  (let [created-markers (into {}
                              (map (fn [[marker-id [_
                                                    location
                                                    :as marker-data]]]
                                     (let [[lat lon] (first location)
                                           style-desc (marker-data->circle-style (partial marker-highlighted-fn? marker-id)
                                                                                 marker-stroke-color-fn
                                                                                 highlighted-marker-stroke-color-fn
                                                                                 marker-data)
                                           feature (ol-feature. (ol-geom-point. (.fromLonLat ol-proj #js[lon lat])))]
                                       (.setStyle feature style-desc)
                                       (.set feature "id" marker-id)
                                       (.set feature "data" marker-data)
                                       [marker-id feature])))
                              markers-data)
        marker-source (get-in @db-state [frame-id :marker-source])]
    (.addFeatures marker-source (clj->js (vec (vals created-markers))))
    (swap! db-state update-in [frame-id :markers] merge created-markers)))

(defn- destroy-instance [frame-id extra-fns]
  (let [geo-obj (map-instance frame-id)]
    (remove-marker-layer frame-id extra-fns)
    (if geo-obj
      (.setTarget geo-obj nil)
      (warn "No map-instance exist. Removing all objects from internal-state."))
    (swap! db-state dissoc frame-id)))

(defn- get-popup [frame-id]
  (get-in @db-state [frame-id :popup]))

(defn- create-overlayer [frame-id {get-geojson-object :geojson-object} {:keys [file-path type name]
                                                                        :as overlayer}]
  (let [created-overlayer (case type
                            "geojson" (overlayer/create-geojson overlayer (get-geojson-object file-path))
                            (overlayer/create-esri overlayer))]
    (swap! db-state assoc-in [frame-id :overlayer name] created-overlayer)))

(defn- get-overlayer-obj [frame-id overlayer-id]
  (get-in @db-state [frame-id :overlayer overlayer-id]))

(defn- feature-layer-created? [frame-id feature-layer-id]
  (boolean (get-in @db-state [frame-id :feature-layer feature-layer-id])))

(defn- get-feature-layer-obj [frame-id feature-layer-id]
  (get-in @db-state [frame-id :feature-layer feature-layer-id]))

(defn- all-feature-layers [frame-id]
  (get-in @db-state [frame-id :feature-layer]))

(defn- create-feature-layer [frame-id
                             extra-fns
                             {:keys [layer-id type] :as feature-layer}]
  (let [created-layer (case type
                        :movement (movement/create-layer extra-fns feature-layer)
                        :feature (area/create-layer extra-fns feature-layer)
                        :heatmap (heatmap/create-layer extra-fns feature-layer)
                        (warn "Unknown layer-type used" {:layer-desc feature-layer
                                                         :type type}))]
    (swap! db-state assoc-in [frame-id :feature-layer layer-id]
           {:type type
            :layer created-layer})
    created-layer))

(defn- create-arrow-features [frame-id feature-layer-id arrow-descs]
  (when arrow-descs
    (let [{feature-layer-obj :layer} (get-feature-layer-obj frame-id feature-layer-id)
          created-arrows (into {}
                               (map (fn [{:keys [id] :as desc}]
                                      [id (movement/create-arrow-feature desc)]))
                               arrow-descs)]
      (swap! db-state update-in [frame-id :arrows feature-layer-id]
             merge created-arrows)
      (movement/add-arrows feature-layer-obj (vals created-arrows))
      created-arrows)))

(defn- clear-arrow-features [frame-id feature-layer-id]
  (swap! db-state update-in [frame-id :arrows] dissoc feature-layer-id))


(defn- create-heatmap-features [frame-id feature-layer-id heatmap-data]
  (let [{feature-layer-obj :layer} (get-feature-layer-obj frame-id feature-layer-id)
        created-features (mapv heatmap/create-feature heatmap-data)]
    (swap! db-state assoc-in [frame-id :heatmap-features feature-layer-id] created-features)
    (heatmap/add-features feature-layer-obj created-features)
    created-features))

(defn- clear-heatmap-features [frame-id feature-layer-id]
  (swap! db-state update-in [frame-id :heatmap-features] dissoc feature-layer-id))

(defn- remove-feature-layer [frame-id extra-fns feature-layer-id]
  (let [{layer-type :type
         feature-layer-obj :layer} (get-feature-layer-obj frame-id feature-layer-id)
        map-instance (map-instance frame-id)]
    (case layer-type
      :movement (do
                  (clear-arrow-features frame-id feature-layer-id)
                  (movement/destroy-layer map-instance feature-layer-obj))
      :feature (area/destroy-layer map-instance feature-layer-obj extra-fns)
      :heatmap (heatmap/destroy-layer map-instance feature-layer-obj)
      (warn "Unknown layer-type used" {:layer-id feature-layer-id
                                       :type layer-type}))
    (swap! db-state update-in [frame-id :feature-layer] dissoc feature-layer-id)))

(deftype OpenlayersObjectManager [frame-id extra-fns]
  proto/mapObjectManager
  (create-map-instance [_ headless?]
    (create-map-instance frame-id headless? extra-fns))
  (map-instance [_]
    (map-instance frame-id))

  (create-marker-layer [_]
    (create-marker-layer frame-id extra-fns))
  (remove-marker-layer [_]
    (remove-marker-layer frame-id extra-fns))
  (marker-layer-created? [_]
    (marker-layer-created? frame-id))
  (get-cluster-layer [_]
    (get-cluster-layer frame-id))
  (get-marker-layer [_]
    (get-marker-layer frame-id))

  (create-markers [_ markers-data]
    (create-markers frame-id extra-fns markers-data))
  (remove-markers [_ marker-ids]
    (remove-marker frame-id marker-ids extra-fns))
  (clear-markers [_]
    (clear-markers frame-id extra-fns))
  (marker-created? [_ marker-id]
    (marker-created? frame-id marker-id))
  (marker-ids [_]
    (get-marker-ids frame-id))
  (get-marker-objs [_ marker-ids]
    (marker-objs frame-id marker-ids))

  (create-feature-layer [_ feature-layer]
    (create-feature-layer frame-id extra-fns feature-layer))
  (remove-feature-layer [_ feature-layer-id]
    (remove-feature-layer frame-id extra-fns feature-layer-id))
  (feature-layer-created? [_ feature-layer-id]
    (feature-layer-created? frame-id feature-layer-id))
  (get-feature-layer-obj [_ feature-layer-id]
    (get-feature-layer-obj frame-id feature-layer-id))
  (all-feature-layers [_]
    (all-feature-layers frame-id))

  (create-arrow-features [_ feature-layer-id arrow-descs]
    (create-arrow-features frame-id feature-layer-id arrow-descs))
  (clear-arrow-features [_ feature-layer-id]
    (clear-arrow-features frame-id feature-layer-id))

  (create-heatmap-features [_ feature-layer-id heatmap-data]
    (create-heatmap-features frame-id feature-layer-id heatmap-data))
  (clear-heatmap-features [_ feature-layer-id]
    (clear-heatmap-features frame-id feature-layer-id))

  (create-overlayer [_ overlayer]
    (create-overlayer frame-id extra-fns overlayer))
  (remove-overlayer [instance overlayer-id])
  (overlayer-created? [instance overlayer-id])
  (get-overlayer-obj [_ overlayer-id]
    (get-overlayer-obj frame-id overlayer-id))

  (create-base-layer [_ base-layer]
    (create-base-layer frame-id base-layer))
  (remove-base-layer [instance base-layer-id])
  (base-layer-created? [instance base-layer-id])
  (get-base-layer-obj [_ base-layer-id]
    (get-base-layer-obj frame-id base-layer-id))

  (get-popup [_]
    (get-popup frame-id))

  (destroy-instance [_]
    (destroy-instance frame-id extra-fns)))

(defn create-instance [frame-id extra-fns]
  (->OpenlayersObjectManager frame-id extra-fns))