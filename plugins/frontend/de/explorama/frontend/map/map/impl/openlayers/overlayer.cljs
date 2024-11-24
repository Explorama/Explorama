(ns de.explorama.frontend.map.map.impl.openlayers.overlayer
  (:require ["ol/style" :refer [Circle Fill Stroke Style Icon]]
            ["ol/format" :refer [GeoJSON]]
            ["ol/source" :rename {Vector ol-source-vector}]
            ["ol/layer" :rename {Vector ol-layer-vector}]
            [clojure.string :as str]
            [taoensso.timbre :refer [warn]]))

(defonce ^:private point-circle-style
  (Circle. #js{:radius 3
                        :fill (Fill. #js{:color "magenta"})
                        :stroke (Stroke. #js{:color "red"
                                                      :width 1})}))

(def ^:private marker-scale 0.12)
(def ^:private marker-image-height 250)
(def ^:private y-anchor (* marker-image-height marker-scale))

(defonce ^:private marker-icon
  (Icon. #js{:scale marker-scale
                      :anchor #js[0.5, y-anchor],
                      :anchorXUnits "fraction"
                      :anchorYUnits "pixel"
                      :src "img/marker.png"}))

(defonce ^:private styles
  {"Point" (Style. #js{:image marker-icon})
   "LineString" (Style. #js{:stroke (Stroke. #js{:color "green"
                                                                   :width 3})})
   "MultiLineString" (Style. #js{:stroke (Stroke. #js{:color "green"
                                                                        :width 3})})
   "MultiPoint" (Style. #js{:image marker-icon})
   "MultiPolygon" (Style. #js{:stroke (Stroke. #js{:color "rgb(75, 79, 83)"
                                                                     :width 2})
                                       :fill (Fill. #js{:color "rgba(54, 70, 85, 0.15)"})})
   "Polygon" (Style. #js{:stroke (Stroke. #js{:color "rgb(75, 79, 83)"
                                                                :lineDash #js[4]
                                                                :width 3})
                                  :fill (Fill. #js{:color "rgba(54, 70, 85, 0.15)"})})
   "GeometryCollection" (Style. #js{:stroke (Stroke. #js{:color "magenta"
                                                                           :width 2})
                                             :fill (Fill. #js{:color "magenta"})
                                             :image (Circle. #js{:radius 10
                                                                          :fill nil
                                                                          :stroke (Stroke. #js{:color "magenta"})})})
   "Circle" (Style. #js{:stroke (Stroke. #js{:color "red"
                                                               :width 2})
                                 :fill (Fill. #js{:color "rgba(255, 0, 0, 0.2)"})})})

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

(defn create-geojson [_ geojson-obj]
  (let [vector-source (ol-source-vector. #js{:features (.readFeatures (GeoJSON.)
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
        vector-source (ol-source-vector. #js{:format (GeoJSON.)
                                             :url complete-url})
        vector-layer (ol-layer-vector. #js{:source vector-source
                                           :style styling-function
                                           :zIndex 4})]
    vector-layer))