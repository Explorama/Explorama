(ns de.explorama.frontend.map.map.impl.openlayers.feature-layers.area
  (:require [clojure.string :as str]
            [de.explorama.frontend.map.utils :refer [rgb-hex-parser]]))

(def ol-geojson (aget js/ol "format" "EsriJSON"))
(def ol-map (aget js/ol "Map"))
(def ol-layer-vector (aget js/ol "layer" "Vector"))
(def ol-layer-vector-image (aget js/ol "layer" "VectorImage"))
(def ol-source-vector (aget js/ol "source" "Vector"))
(def ol-view (aget js/ol "View"))
(def ol-style-style  (aget js/ol "style" "Style"))
(def ol-style-fill  (aget js/ol "style" "Fill"))
(def ol-style-stroke  (aget js/ol "style" "Stroke"))
(def ol-format-geojson (aget js/ol "format" "GeoJSON"))

(def default-color [238 238 238 0.25])

(def default-stlye (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "rgba(75, 79, 83, 0.5)"
                                                                      :width 1})
                                        :fill (ol-style-fill. #js{:color (str "rgba(" (str/join "," default-color) ")")})}))

(defn area-color [area-data]
  (if-let [c (:color area-data)]
    (str "rgba(" (str/join "," (rgb-hex-parser c)) "," (:opacity area-data) ")")
    (str "rgba(" (str/join "," default-color) ")")))

(defn- feature-properties->data-key [feature-properties feature]
  (select-keys (js->clj (.getProperties feature))
               (keys feature-properties)))

(defn- query-map->url-encode [query-map]
  (str/join "&"
            (reduce (fn [acc [k v]]
                      (conj acc
                            (str k "=" (if (not= k "where")
                                         (js/encodeURIComponent v)
                                         v))))
                    []
                    query-map)))

(defn- where-string [static-query-properties main-feature-property values]
  (let [static-query (str/join "AND" (reduce (fn [acc [k v]]
                                               (conj acc (str k "=" v)))
                                             []
                                             static-query-properties))
        property-query (str main-feature-property
                            " IN ("
                            (str/join "," (map #(str "%27"
                                                     (str/replace % "'" "''")
                                                     "%27")
                                               values))
                            ")")]
    (if (seq static-query)
      (str static-query " AND " property-query)
      (str property-query))))

(def ^:private query-base-map {"returnGeometry" true
                               "outSR" 4326
                               "outFields" "*"
                               "inSR" 4326
                               "geometryType" "esriGeometryEnvelope"
                               "spatialRel" "esriSpatialRelIntersects"
                               "geometryPrecision" 6
                               "resultType" "tile"
                               "f" "json"})

(defn- load-from-url [url vector-source]
  (.then (js/fetch url)
         (fn [response]
           (.then (.json response)
                  (fn [result]
                    (.addFeatures vector-source
                                  (.readFeatures (ol-geojson.)
                                                 result
                                                 #js{:dataProjection "EPSG:4326",
                                                     :featureProjection "EPSG:3857"})))))))

(defn create-layer [{:keys [feature-layer-config geojson-object]}
                    {:keys [data-set feature-layer-id]}]
  (let [{:keys [main-feature-property
                server-url
                file-path
                static-query-properties
                feature-properties
                type]} (feature-layer-config feature-layer-id)
        main-prop-values (->> data-set
                              keys
                              (mapv #(get % main-feature-property)))
        geojson-obj (when (seq file-path)
                      (geojson-object file-path))
        vector-source (case type
                        "esri" (ol-source-vector.)
                        "geojson" (ol-source-vector. #js{:features (.readFeatures (ol-format-geojson.)
                                                                                  geojson-obj
                                                                                  #js{:dataProjection "EPSG:4326"
                                                                                      :featureProjection "EPSG:3857"})})
                        (ol-source-vector.))
        vector-layer (ol-layer-vector-image.
                      #js{:source vector-source
                          :style (fn [feature]
                                   (let [data-key (feature-properties->data-key feature-properties
                                                                                feature)
                                         data (get data-set data-key)]
                                     (when (seq data)
                                       (->> (area-color data)
                                            (.setColor (.getFill default-stlye)))
                                       default-stlye)))})]
    (when (seq server-url)
      (doseq [main-prop-values (partition-all 50 main-prop-values)]
        (let [url (when (seq server-url)
                    (str server-url
                         "/query?"
                         (query-map->url-encode
                          (assoc query-base-map
                                 "where"
                                 (where-string static-query-properties
                                               main-feature-property
                                               main-prop-values)))))]
          (load-from-url url vector-source))))
    {:vector-source vector-source
     :vector-layer vector-layer}))

(defn- destroy-and-hide [map-instance {:keys [vector-layer vector-source]}]
  (.removeLayer map-instance vector-layer)
  (.clear vector-source))

(defn display-layer [map-instance {:keys [vector-layer vector-source]}]
  (.addLayer map-instance vector-layer))

(defn destroy-layer [map-instance layer-obj extra-fns]
  (destroy-and-hide map-instance layer-obj))

(defn hide-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))