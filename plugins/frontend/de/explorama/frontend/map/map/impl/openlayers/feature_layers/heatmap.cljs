(ns de.explorama.frontend.map.map.impl.openlayers.feature-layers.heatmap
  (:require ["ol" :refer [Feature proj]]
            ["ol/layer" :refer [Heatmap]]
            ["ol/source" :refer [Vector]]
            ["ol/geom" :refer [Point]]))

(defn- get-weight [layer-desc-fn feature]
  (let [{:keys [extrema]} (layer-desc-fn)]
    (if (= extrema :local) ; Weighted by attribute
      (.get feature "weight")
      1)))

(defn create-layer [{current-desc :feature-layer-desc} 
                    {:keys [layer-id]}]
  (let [layer-desc-fn (partial current-desc layer-id)
        vector-source (Vector.)
        heatmap-obj (Heatmap.
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
        point (Point. (.fromLonLat proj #js[lng lat]))
        feature-obj (Feature. point)]
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