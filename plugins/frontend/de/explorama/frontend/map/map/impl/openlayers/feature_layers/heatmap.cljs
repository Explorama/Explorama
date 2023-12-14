(ns de.explorama.frontend.map.map.impl.openlayers.feature-layers.heatmap
  (:require [cljsjs.openlayers]
            [cljsjs.openlayers-ol-ext]))

(def ol-layer-heatmap (aget js/ol "layer" "Heatmap"))

(def ol-source-vector (aget js/ol "source" "Vector"))
(def ol-feature (aget js/ol "Feature"))
 (def ol-geom-point (aget js/ol "geom" "Point"))
(def ol-proj (aget js/ol "proj"))

(defn- get-weight [layer-desc-fn feature]
  (let [{:keys [extrema]} (layer-desc-fn)]
    (if (= extrema :local) ; Weighted by attribute
      (.get feature "weight")
      1)))

(defn create-layer [{current-desc :feature-layer-desc} 
                    {:keys [layer-id]}]
  (let [layer-desc-fn (partial current-desc layer-id)
        vector-source (ol-source-vector.)
        heatmap-obj (ol-layer-heatmap.
                     #js{:source vector-source
                         :blur 15
                         :radius 5
                         :weight (partial get-weight layer-desc-fn)})]
    {:heatmap heatmap-obj
     :vector-source vector-source}))

(defn display-layer [map-instance {:keys [heatmap]}]
  (.addLayer map-instance heatmap))

(defn create-feature [{:keys [lat lng] :as desc}] 
  (let [attr-key (first (keys (dissoc desc :lat :lng)))
        attr-val (when (seq attr-key) (get desc attr-key))
        point (ol-geom-point. (.fromLonLat ol-proj #js[lng lat]))
        feature-obj (ol-feature. point)]
    (when attr-val
      (.set feature-obj attr-key attr-val)
      (.set feature-obj "weight" attr-val))
    feature-obj))

(defn add-features [{:keys [vector-source]} heatmap-features]
  (when vector-source
    (.addFeatures vector-source
                  (clj->js heatmap-features))))

(defn- destroy-and-hide [map-instance {:keys [heatmap
                                              vector-source]}]
  (when heatmap
    (.removeLayer map-instance heatmap))
  (when vector-source
    (.clear vector-source)))

(defn destroy-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))

(defn hide-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))