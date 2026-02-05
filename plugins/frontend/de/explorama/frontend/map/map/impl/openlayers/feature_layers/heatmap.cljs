(ns de.explorama.frontend.map.map.impl.openlayers.feature-layers.heatmap
  (:require ["ol/layer/Heatmap" :as HeatmapLayerModule]
            ["ol/source/Vector" :as VectorSourceModule]
            ["ol/Feature" :as FeatureModule]
            ["ol/geom/Point" :as PointModule]
            ["ol/proj" :as proj]))

(def HeatmapLayer (.-default HeatmapLayerModule))
(def VectorSource (.-default VectorSourceModule))
(def Feature (.-default FeatureModule))
(def Point (.-default PointModule))

(defn- get-weight [layer-desc-fn feature]
  (let [{:keys [extrema]} (layer-desc-fn)]
    (if (= extrema :local) ; Weighted by attribute
      (.get feature "weight")
      1)))

(defn create-layer [{current-desc :feature-layer-desc}
                    {:keys [layer-id]}]
  (let [layer-desc-fn (partial current-desc layer-id)
        vector-source (new VectorSource)
        heatmap-obj (new HeatmapLayer
                         #js{:source vector-source
                             :blur 15
                             :radius 5
                             :weight (partial get-weight layer-desc-fn)})]
    {:heatmap heatmap-obj
     :vector-source vector-source}))

(defn display-layer [^js map-instance {:keys [heatmap]}]
  (.addLayer map-instance heatmap))

(defn create-feature [{:keys [lat lng] :as desc}]
  (let [attr-key (first (keys (dissoc desc :lat :lng)))
        attr-val (when (seq attr-key) (get desc attr-key))
        point (new Point (proj/fromLonLat #js[lng lat]))
        feature-obj (new Feature point)]
    (when attr-val
      (.set feature-obj attr-key attr-val)
      (.set feature-obj "weight" attr-val))
    feature-obj))

(defn add-features [{:keys [^js vector-source]} heatmap-features]
  (when vector-source
    (.addFeatures vector-source
                  (clj->js heatmap-features))))

(defn- destroy-and-hide [^js map-instance {:keys [^js heatmap
                                                  vector-source]}]
  (when heatmap
    (.removeLayer map-instance heatmap))
  (when vector-source
    (.clear vector-source)))

(defn destroy-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))

(defn hide-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))