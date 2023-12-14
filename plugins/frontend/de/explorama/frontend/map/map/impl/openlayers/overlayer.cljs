(ns de.explorama.frontend.map.map.impl.openlayers.overlayer
  (:require [cljsjs.openlayers]
            [cljsjs.openlayers-ol-ext]
            [clojure.string :as str]
            [taoensso.timbre :refer [warn]]))

(def ol-style (aget js/ol "style"))
(def ol-style-circle (aget ol-style "Circle"))
(def ol-style-fill (aget ol-style "Fill"))
(def ol-style-stroke (aget ol-style "Stroke"))
(def ol-style-style (aget ol-style "Style"))
(def ol-style-icon (aget ol-style "Icon"))

(defonce ^:private point-circle-style
  (ol-style-circle. #js{:radius 3
                        :fill (ol-style-fill. #js{:color "magenta"})
                        :stroke (ol-style-stroke. #js{:color "red"
                                                      :width 1})}))

(def ^:private marker-scale 0.12)
(def ^:private marker-image-height 250)
(def ^:private y-anchor (* marker-image-height marker-scale))

(defonce ^:private marker-icon
  (ol-style-icon. #js{:scale marker-scale
                      :anchor #js[0.5, y-anchor],
                      :anchorXUnits "fraction"
                      :anchorYUnits "pixel"
                      :src "img/marker.png"}))

(defonce ^:private styles
  {"Point" (ol-style-style. #js{:image marker-icon})
   "LineString" (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "green"
                                                                   :width 3})})
   "MultiLineString" (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "green"
                                                                        :width 3})})
   "MultiPoint" (ol-style-style. #js{:image marker-icon})
   "MultiPolygon" (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "rgb(75, 79, 83)"
                                                                     :width 2})
                                       :fill (ol-style-fill. #js{:color "rgba(54, 70, 85, 0.15)"})})
   "Polygon" (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "rgb(75, 79, 83)"
                                                                :lineDash #js[4]
                                                                :width 3})
                                  :fill (ol-style-fill. #js{:color "rgba(54, 70, 85, 0.15)"})})
   "GeometryCollection" (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "magenta"
                                                                           :width 2})
                                             :fill (ol-style-fill. #js{:color "magenta"})
                                             :image (ol-style-circle. #js{:radius 10
                                                                          :fill nil
                                                                          :stroke (ol-style-stroke. #js{:color "magenta"})})})
   "Circle" (ol-style-style. #js{:stroke (ol-style-stroke. #js{:color "red"
                                                               :width 2})
                                 :fill (ol-style-fill. #js{:color "rgba(255, 0, 0, 0.2)"})})})

(defn- styling-function [feature]
  (let [feature-type (.getType (.getGeometry feature))]
    (if-let [feature-style (get styles feature-type)]
      feature-style
      (do
        (warn "No style defined for given feature type. Using fallback Point."
              {:feature feature
               :feature-type feature-type
               :possible-types (keys styles)})
        (get styles "Point")))))

(def ol-format-geojson (aget js/ol "format" "GeoJSON"))

(def ol-source-vector (aget js/ol "source" "Vector"))

(def ol-layer-vector (aget js/ol "layer" "Vector"))

(defn create-geojson [_ geojson-obj]
  (let [vector-source (ol-source-vector. #js{:features (.readFeatures (ol-format-geojson.)
                                                                      geojson-obj
                                                                      #js{:dataProjection "EPSG:4326"
                                                                          :featureProjection "EPSG:3857"})})
        vector-layer (ol-layer-vector. #js{:source vector-source
                                           :style styling-function
                                           :zIndex 4})]
    vector-layer))

(def ^:private default-url-query "query?where=1%3D1&outFields=*&returnGeometry=true&f=geojson")

(defn create-esri [{:keys [server-url query]}]
  (let [server-url (if (str/ends-with? server-url "/")
                     server-url
                     (str server-url "/"))
        complete-url (if (seq query)
                       (str server-url query)
                       (str server-url default-url-query))
        vector-source (ol-source-vector. #js{:format (ol-format-geojson.)
                                             :url complete-url})
        vector-layer (ol-layer-vector. #js{:source vector-source
                                           :style styling-function
                                           :zIndex 4})]
    vector-layer))