(ns de.explorama.frontend.map.map.impl.openlayers.feature-layers.movement
  (:require ["ol/style/Style" :as StyleModule]
            ["ol/style/Stroke" :as StrokeModule]
            ["ol-ext/style/FlowLine" :as FlowLineModule]
            ["ol/layer/VectorImage" :as VectorImageLayerModule]
            ["ol/source/Vector" :as VectorSourceModule]
            ["ol-ext/overlay/Popup" :as PopupModule]
            ["ol-ext/interaction/Hover" :as HoverModule]
            ["ol/Feature" :as FeatureModule]
            ["ol/geom/LineString" :as LineStringModule]
            ["ol/proj" :as proj]))

(def Style (.-default StyleModule))
(def Stroke (.-default StrokeModule))
(def FlowLine (.-default FlowLineModule))
(def VectorImageLayer (.-default VectorImageLayerModule))
(def VectorSource (.-default VectorSourceModule))
(def Popup (.-default PopupModule))
(def Hover (.-default HoverModule))
(def Feature (.-default FeatureModule))
(def LineString (.-default LineStringModule))

(defonce ^:private default-style
  (new Style
       #js{:stroke (new Stroke #js{:color #js[255 255 255 0.1]
                                           :width 1})}))
(def arrow-color "rgba(27, 28, 30, 0.4)")
(def hover-color "rgb(16, 163, 163)")

(defn- get-style [color feature _]
  (let [weight (.get feature "weight")
        start-width (* 1 weight) ; arrow-point
        end-width 2 ; arrow-end
        geometry (.getGeometry feature)
        z-index 5 #_(-> feature
                        (.getGeometry)
                        (.getLastCoordinate)
                        (aget 1)
                        -)
        flow-style (new FlowLine
                        #js{:color color
                            :color2 color
                            :width start-width
                            :width2 end-width
                            :arrow -1
                            :geometry geometry
                            :zIndex z-index})]
    #js[default-style
        flow-style]))

(defn create-layer [{localize-num-fn :localize-number
                     attribute-label-fn :attribute-label}
                    _]
  (let [vector-source (new VectorSource)
        vector-layer (new VectorImageLayer #js{:source vector-source
                                                       :style (partial get-style arrow-color)})
        popup-layer (new Popup #js{:className "tooltips"
                                           :offsetBox 5})
        hover-interaction (new Hover
                               #js{:cursor "pointer"
                                   :layers #js[vector-layer]
                                   :hitTolerance 2})
        hovered-feature (atom nil)]
    (.on hover-interaction
         "hover"
         (fn [e]
           (when-let [old-feature @hovered-feature]
             (.setStyle old-feature (get-style arrow-color old-feature nil)))
           (let [feature (aget e "feature")
                 feature-style (get-style hover-color feature nil)
                 attribute-name (.get feature "attribute")
                 value (.get feature "original")]
             (.setStyle feature feature-style)
             (reset! hovered-feature feature)
             (.show popup-layer
                    (aget e "coordinate")
                    (str (attribute-label-fn attribute-name)
                         ": "
                         (if (number? value)
                           (localize-num-fn value)
                           value))))))
    (.on hover-interaction
         "leave"
         (fn [e]
           (let [feature @hovered-feature
                 feature-style (get-style arrow-color feature nil)]
             (.setStyle feature feature-style)
             (reset! hovered-feature nil)
             (.hide popup-layer))))
    {:vector-source vector-source
     :vector-layer vector-layer
     :popup-layer popup-layer
     :hover-interaction hover-interaction}))

(defn create-arrow-feature [{:keys [from to weight original attribute]}]
  (let [[from-lat from-lon] from
        [to-lat to-lon] to
        line-string (new LineString
                         #js[(proj/fromLonLat #js[to-lon to-lat])
                             (proj/fromLonLat #js[from-lon from-lat])])
        feature-obj (new Feature line-string)]
    (.set feature-obj "weight" weight)
    (.set feature-obj "original" original)
    (.set feature-obj "attribute" attribute)
    feature-obj))

(defn add-arrows [{:keys [vector-source]} arrow-objs]
  (.addFeatures vector-source
                (clj->js arrow-objs)))

(defn display-layer [map-instance {:keys [vector-layer
                                          popup-layer
                                          hover-interaction]}]
  (.addOverlay map-instance popup-layer)
  (.addLayer map-instance vector-layer)
  (.addInteraction map-instance hover-interaction))

(defn- destroy-and-hide [map-instance {:keys [vector-layer
                                              popup-layer
                                              hover-interaction
                                              vector-source]}]
  (.removeOverlay map-instance popup-layer)
  (.removeLayer map-instance vector-layer)
  (.removeInteraction map-instance hover-interaction)
  (.clear vector-source))

(defn destroy-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))

(defn hide-layer [map-instance layer-obj]
  (destroy-and-hide map-instance layer-obj))