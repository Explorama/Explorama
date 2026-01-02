(ns de.explorama.frontend.map.map.impl.openlayers.overlayer
  (:require ["ol/style/Style" :as StyleModule]
            ["ol/style/Circle" :as CircleStyleModule]
            ["ol/style/Fill" :as FillModule]
            ["ol/style/Stroke" :as StrokeModule]
            ["ol/style/Icon" :as IconModule]
            ["ol/format/GeoJSON" :as GeoJSONModule]
            ["ol/source/Vector" :as VectorSourceModule]
            ["ol/layer/Vector" :as VectorLayerModule]
            [clojure.string :as str]
            [taoensso.timbre :refer [warn]]))

(def Style (.-default StyleModule))
(def CircleStyle (.-default CircleStyleModule))
(def Fill (.-default FillModule))
(def Stroke (.-default StrokeModule))
(def Icon (.-default IconModule))
(def GeoJSON (.-default GeoJSONModule))
(def VectorSource (.-default VectorSourceModule))
(def VectorLayer (.-default VectorLayerModule))

(defonce ^:private point-circle-style
  (new CircleStyle #js{:radius 3
                               :fill (new Fill #js{:color "magenta"})
                               :stroke (new Stroke #js{:color "red"
                                                               :width 1})}))

(def ^:private marker-scale 0.12)
(def ^:private marker-image-height 250)
(def ^:private y-anchor (* marker-image-height marker-scale))

(defonce ^:private marker-icon
  (new Icon #js{:scale marker-scale
                        :anchor #js[0.5, y-anchor],
                        :anchorXUnits "fraction"
                        :anchorYUnits "pixel"
                        :src "img/marker.png"}))

(defonce ^:private styles
  {"Point" (new Style #js{:image marker-icon})
   "LineString" (new Style #js{:stroke (new Stroke #js{:color "green"
                                                                       :width 3})})
   "MultiLineString" (new Style #js{:stroke (new Stroke #js{:color "green"
                                                                            :width 3})})
   "MultiPoint" (new Style #js{:image marker-icon})
   "MultiPolygon" (new Style #js{:stroke (new Stroke #js{:color "rgb(75, 79, 83)"
                                                                         :width 2})
                                         :fill (new Fill #js{:color "rgba(54, 70, 85, 0.15)"})})
   "Polygon" (new Style #js{:stroke (new Stroke #js{:color "rgb(75, 79, 83)"
                                                                    :lineDash #js[4]
                                                                    :width 3})
                                    :fill (new Fill #js{:color "rgba(54, 70, 85, 0.15)"})})
   "GeometryCollection" (new Style #js{:stroke (new Stroke #js{:color "magenta"
                                                                               :width 2})
                                               :fill (new Fill #js{:color "magenta"})
                                               :image (new CircleStyle #js{:radius 10
                                                                                   :fill nil
                                                                                   :stroke (new Stroke #js{:color "magenta"})})})
   "Circle" (new Style #js{:stroke (new Stroke #js{:color "red"
                                                                   :width 2})
                                   :fill (new Fill #js{:color "rgba(255, 0, 0, 0.2)"})})})

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
  (let [vector-source (new VectorSource #js{:features (.readFeatures (new GeoJSON)
                                                                             geojson-obj
                                                                             #js{:dataProjection "EPSG:4326"
                                                                                 :featureProjection "EPSG:3857"})})
        vector-layer (new VectorLayer #js{:source vector-source
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
        vector-source (new VectorSource #js{:format (new GeoJSON)
                                                    :url complete-url})
        vector-layer (new VectorLayer #js{:source vector-source
                                                  :style styling-function
                                                  :zIndex 4})]
    vector-layer))