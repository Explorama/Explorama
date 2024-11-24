(ns de.explorama.frontend.map.map.impl.openlayers.util
  (:require ["ol" :refer [coordinate proj extent Feature]]
            ["ol/interaction" :refer [Interaction]]
            ["ol/style" :refer [Circle Fill Stroke Style Chart Text]]
            ["ol/geom" :refer [Polygon]]
            [clojure.string :as str]
            [de.explorama.frontend.map.utils :refer [rgb-hex-parser font-color]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]))

(def ^:private invisible-stroke (Stroke. #js{:color (str "rgba(1,1,1,0)")
                                                      :width 1}))
(def ^:private style-border (Style. #js{:image (Circle. #js{:fill (Fill. #js{:color "#fff"})
                                                                              :radius 25
                                                                              :stroke invisible-stroke})}))
(def ^:private white-circle-16 (Circle. #js{:fill (Fill. #js{:color "#fff"})
                                                     :radius 16
                                                     :stroke invisible-stroke}))

(defonce fit-view-opts (clj->js {:padding [50, 50, 50, 50]}))

(defonce ^:private marker-style-cache (atom {}))

(defn marker-data->circle-style [marker-highlighted-fn?
                                 stroke-color-fn
                                 highlighted-marker-stroke-color-fn
                                 [_ _ {:keys [fillColor radius fillOpacity]}]]
  (let [is-highlighted? (marker-highlighted-fn?)
        style-key [fillColor is-highlighted?]]
    (if-let [cached-style (get @marker-style-cache style-key)]
      cached-style
      (let [fill-color (str "rgb(" (str/join "," (rgb-hex-parser fillColor)) ", " fillOpacity ")")
            stroke-color (str "rgb(" (str/join ","
                                               (if is-highlighted?
                                                 (highlighted-marker-stroke-color-fn)
                                                 (stroke-color-fn))) ")")
            z-index (if is-highlighted? 3 2)
            circle-style (Circle. #js{:fill (Fill. #js{:color fill-color})
                                               :radius radius
                                               :stroke (Stroke. #js{:color stroke-color
                                                                             :width 2})})
            result-style (Style. #js{:image circle-style
                                              :zIndex z-index})]
        (swap! marker-style-cache assoc style-key result-style)
        result-style))))

(defn cluster->cluster-style [localize-number-fn cluster _]
  (let [features (.get cluster "features")]
    (if-let [cluster-style (aget features "cachedStyle")]
      cluster-style
      (let [features (.get cluster "features")
            num-features (aget features "length")
            color-counts (reduce (fn [acc feature]
                                   (let [[_ _ {color :fillColor}] (.get feature "data")]
                                     (update acc color (fnil inc 0))))
                                 {}
                                 (array-seq features))
            colors-vec (clj->js (map (fn [c] (str "rgb(" (str/join "," (rgb-hex-parser c)) ")"))
                                     (keys color-counts)))
            data-vec (clj->js (vals color-counts))
            style-outer (Style. #js{:image (Chart. #js{:type "donut"
                                                                         :radius 25
                                                                         :data data-vec
                                                                         :colors colors-vec
                                                                         :stroke invisible-stroke})})
            style-inner (Style. #js{:image white-circle-16
                                             :text (Text. #js{:font "bold 12px 'Arial'"
                                                                       :text (localize-number-fn num-features)})})
            cluster-style (if (> num-features 1)
                            #js[style-border style-outer style-inner]
                            #js[(.getStyle (aget features "0"))])]
        (aset features "cachedStyle" cluster-style)
        cluster-style))))

(defn cluster-interaction-style [localize-number-fn instance f res]
  (let [cluster (.get f "features")
        cluster-size (aget cluster "length")]
    (if-let [coords-extent (aget cluster "coordsCached")]
      (let [area-size (.getArea extent coords-extent)]
        (when (> area-size 0)
          (.fit (.getView instance)
                coords-extent
                fit-view-opts)))
      (let [all-coords (clj->js (map (fn [f]
                                       (.getFirstCoordinate
                                        (.getGeometry f)))
                                     (array-seq cluster)))
            coords-extent (.boundingExtent extent
                                           all-coords)
            area-size (.getArea extent coords-extent)]
        (aset cluster "coordsCached" coords-extent)
        (when (> area-size 0)
          (.fit (.getView instance)
                coords-extent
                fit-view-opts))))
    (if-let [cluster-style (aget cluster "cachedStyle")]
      cluster-style
      (let [all-coords (clj->js (map (fn [f]
                                       (.getFirstCoordinate
                                        (.getGeometry f)))
                                     (array-seq cluster)))
            coords-extent (.boundingExtent extent
                                           all-coords)
            area-size (.getArea extent coords-extent)
            cluster-style (if (> cluster-size 1)
                            (cluster->cluster-style localize-number-fn f res)
                            #js[(.getStyle (aget cluster "0"))])]
        (when (> area-size 0)
          (.fit (.getView instance)
                coords-extent
                fit-view-opts))

        (aset cluster "cachedStyle" cluster-style)
        (aset cluster "coordsCached" coords-extent)
        cluster-style))))

(defn cluster-interaction-feature [marker-highlighted-fn? stroke-color-fn highlighted-marker-stroke-color-fn f]
  (let [sel (.get f "features")
        feature (when sel (aget sel "0"))
        style (when feature (marker-data->circle-style (partial marker-highlighted-fn? (.get feature "id"))
                                                       stroke-color-fn
                                                       highlighted-marker-stroke-color-fn
                                                       (.get feature "data")))]
    (when feature
      style)))

(defn find-cluster-for-feature
  "Finds the cluster-feature which contains the given feature-obj.
   Clusters are always based on the current zoom level."
  [cluster-layer feature-obj]
  (some (fn [c]
          (when (and (.get c "features")
                     (some #(= feature-obj %)
                           (array-seq (.get c "features"))))
            c))
        (array-seq (.getFeatures (.getSource cluster-layer)))))

(defn add-mouse-leave-handler [map-instance track-view-position-fn]
  (.addEventListener (.getViewport map-instance)
                     "mouseleave"
                     (fn [_]
                       (track-view-position-fn true))))

(defn get-view-port [map-instance]
  (let [view-obj (.getView map-instance)
        [lon lat] (js->clj (.toLonLat proj (.getCenter view-obj)))]
    {:center [lat lon]
     :zoom (.getZoom view-obj)}))

(defn map-click-handler [current-objs-fn active-feature-layer-fn marker-clicked-fn overlayer-feature-clicked-fn
                         area-feature-clicked-fn highlight-event-fn hide-popup-fn e]
  (let [{:keys [cluster-interaction
                cluster-layer
                marker-layer
                feature-layer
                overlayer]
         map-obj :map} (current-objs-fn)
        event-pixel (aget e "pixel")
        active-feature-layer (active-feature-layer-fn)
        active-feature-layers (into {}
                                    (comp (filter (fn [[k _]]
                                                    (active-feature-layer k)))
                                          (map (fn [[k v]]
                                                 {k (get-in v [:layer :vector-layer])})))
                                    feature-layer)
        active-overlayers (into {}
                                (filterv (fn [[_ v]]
                                           (aget v "addedToMap"))
                                         overlayer))
        overlayers-count (count active-overlayers)
        overlayers-names (keys active-overlayers)
        feature-layers-count (count active-feature-layers)
        feature-layers-names (keys active-feature-layers)
        open-cluster-features (when cluster-interaction
                                (.getFeatures (.getLayer cluster-interaction)
                                              event-pixel))
        marker-features (when marker-layer
                          (.getFeatures marker-layer event-pixel))
        cluster-features (when cluster-layer
                           (.getFeatures cluster-layer event-pixel))
        [lon lat] (.toLonLat proj (.getCoordinateFromPixel map-obj event-pixel))
        event-coords [lat lon]
        all-promises ((comp vec flatten conj) [open-cluster-features marker-features cluster-features]
                                              (mapv (fn [[_ v]]
                                                      (.getFeatures v event-pixel))
                                                    active-overlayers)
                                              (mapv (fn [[_ v]]
                                                      (when v
                                                        (.getFeatures v event-pixel)))
                                                    active-feature-layers))]
    (.then (.all js/Promise (clj->js all-promises))
           (fn [result]
             (let [vector-result (vec (array-seq result))
                   [open-cluster-features
                    marker-features
                    cluster-features] vector-result
                   overlayers-results (zipmap overlayers-names
                                              (subvec vector-result
                                                      3
                                                      (+ 3 overlayers-count)))
                   feature-layers-results (zipmap feature-layers-names
                                                  (subvec vector-result
                                                          (+ 3 overlayers-count)
                                                          (+ 3 overlayers-count feature-layers-count)))
                   clicked-feature-obj (cond (and open-cluster-features
                                                  (= (aget open-cluster-features "length") 1))
                                             (-> open-cluster-features
                                                 (aget "0")
                                                 (.get "features")
                                                 (aget "0"))
                                             (and cluster-features
                                                  (= (aget cluster-features "length") 1)
                                                  (= (-> cluster-features
                                                         (aget "0")
                                                         (.get "features")
                                                         (aget "length")) 1))
                                             (-> cluster-features
                                                 (aget "0")
                                                 (.get "features")
                                                 (aget "0"))
                                             (and marker-features
                                                  (= (aget marker-features "length") 1))
                                             (aget marker-features "0"))
                   ;Marker Features takes priority over generic feature overlayer
                   clicked-overlayer-feature (when (and (not clicked-feature-obj)
                                                        (or (not cluster-features)
                                                            (= (aget cluster-features "length") 0)))
                                               ;Find the first overlayer with a feature
                                               (some (fn [[k v]]
                                                       (when (> (aget v "length") 0)
                                                         [k (-> v
                                                                (aget 0)
                                                                .getProperties
                                                                js->clj
                                                                (dissoc "geometry"))]))
                                                     overlayers-results))
                   clicked-feature-layer-feature (when (and (not clicked-feature-obj)
                                                            cluster-features
                                                            (= (aget cluster-features "length") 0))
                                               ;Find the first feature-layer with a feature
                                                   (some (fn [[k v]]
                                                           (when (and v (> (aget v "length") 0))
                                                             [k (-> v
                                                                    (aget 0)
                                                                    .getProperties
                                                                    js->clj
                                                                    (dissoc "geometry"))]))
                                                         feature-layers-results))
                   [event-id location {fillColor :fillColor}]
                   (when clicked-feature-obj
                     (.get clicked-feature-obj "data"))
                   highlight-id (when clicked-feature-obj (.get clicked-feature-obj "id"))
                   highlight? (aget e "originalEvent" "ctrlKey")]
               (cond
                 (and (seq event-id) highlight?) (highlight-event-fn highlight-id location)
                 (seq event-id) (marker-clicked-fn event-id
                                                   fillColor
                                                   event-coords
                                                   (get-view-port map-obj))
                 (seq clicked-overlayer-feature) (overlayer-feature-clicked-fn (first clicked-overlayer-feature)
                                                                               (second clicked-overlayer-feature)
                                                                               event-coords
                                                                               (get-view-port map-obj))
                 (seq clicked-feature-layer-feature) (area-feature-clicked-fn (first clicked-feature-layer-feature)
                                                                              (second clicked-feature-layer-feature)
                                                                              event-coords
                                                                              (get-view-port map-obj))
                 (or cluster-interaction
                     cluster-layer
                     marker-layer
                     (seq overlayers-results)
                     (seq feature-layers-results)) (hide-popup-fn)))))))

(defn fake-double-click-zoom [e]
  (let [map-instance (aget e "map")
        browser-event (aget e "originalEvent")
        anchor (aget e "coordinate")
        delta (if (aget browser-event "shiftKey")
                -1 1)
        view (.getView map-instance)]
    (.zoomByDelta Interaction view delta anchor 250)
    (.preventDefault browser-event)))

(defn map-double-click-handler [current-objs-fn marker-dbl-clicked-fn e]
  (let [{:keys [cluster-interaction
                cluster-layer
                marker-layer]} (current-objs-fn)
        event-pixel (aget e "pixel")
        open-cluster-features (.getFeatures (.getLayer cluster-interaction)
                                            event-pixel)
        clusters (.getFeatures cluster-layer event-pixel)
        markers (.getFeatures marker-layer event-pixel)]
    (.then (.all js/Promise #js[open-cluster-features clusters markers])
           (fn [result]
             (let [[open-cluster-features
                    cluster-features
                    marker-features] (array-seq result)
                   clicked-feature-obj (cond (= (aget open-cluster-features "length") 1)
                                             (-> open-cluster-features
                                                 (aget "0")
                                                 (.get "features")
                                                 (aget "0"))
                                             (and (= (aget cluster-features "length") 1)
                                                  (= (-> cluster-features
                                                         (aget "0")
                                                         (.get "features")
                                                         (aget "length")) 1))
                                             (-> cluster-features
                                                 (aget "0")
                                                 (.get "features")
                                                 (aget "0"))
                                             (= (aget marker-features "length") 1)
                                             (aget marker-features "0"))
                   [event-id] (when clicked-feature-obj
                                (.get clicked-feature-obj "data"))]
               (if (and (or (nil? open-cluster-features)
                            (= (aget open-cluster-features "length") 0))
                        (or (nil? cluster-features)
                            (= (aget cluster-features "length") 0))
                        (or (nil? marker-features)
                            (= (aget marker-features "length") 0))) ;Only zoom when nothing was clicked
                 (fake-double-click-zoom e)
                 (marker-dbl-clicked-fn (aget e "originalEvent") event-id)))))))

(defn map-hover-enter-handler [hover-source max-marker-hover e]
  (let [hull (.get (aget e "feature") "convexHull")
        cluster (.get (aget e "feature") "features")
        hull (if-not hull
               (let [all-coords (clj->js (map (fn [f]
                                                (.getFirstCoordinate
                                                 (.getGeometry f)))
                                              (array-seq cluster)))
                     hull (.convexHull coordinate all-coords)]
                 (.set (aget e "feature") "convexHull" hull)
                 hull)
               hull)]
    (.clear hover-source)
    (if (<= (aget cluster "length") @(max-marker-hover))
      (do (.addFeature hover-source
                       (Feature. (Polygon. #js[hull])))
          (.addFeatures hover-source
                        cluster))
      (.addFeature hover-source
                   (Feature. (Polygon. #js[hull]))))))

(defn map-hover-leave-handler [hover-source _]
  (.clear hover-source))

(defn- date-attr? [attribute-label]
  (#{"month" "year" "day" "date"} attribute-label))

(defn- attribute-desc [localize-num-fn attribute-label-fn [attribute attribute-value]]
  (let [attribute-label (attribute-label-fn attribute)
        value-text (cond
                     (and (vector? attribute-value)
                          (number? (first attribute-value))) (str/join ", "
                                                                       (map localize-num-fn attribute-value))
                     (vector? attribute-value) (str/join ", " attribute-value)
                     (and (number? attribute-value)
                          (not (date-attr? attribute))) (localize-num-fn attribute-value)
                     :else attribute-value)]
    (str "<dt>" attribute-label "</dt>"
         "<dd>" value-text "</dd>")))


(defn gen-popup-content [localize-num-fn
                         attribute-label-fn
                         color event
                         title-attributes
                         display-attributes]
  (let [selected-attributes (cond (and (= display-attributes :all)
                                       (object? event)) ;special case configuration for feature-layer
                                  (array-seq (js-keys event))

                                  (= display-attributes :all)
                                  (keys event)

                                  (vector? (first display-attributes)) ;field-assignment from marker-layouts
                                  (mapv second display-attributes)

                                  :else display-attributes) ;configured feature-layer propertie-list
        title-attributes-set (set title-attributes)
        attribute-desc-fn (partial attribute-desc localize-num-fn attribute-label-fn)]
    (when (and (seq event)
               (or (seq title-attributes)
                   (seq selected-attributes)))
      (format "<div class=\"popup-content\" style=\"width: 350px;\"> %s %s </div>"
              (if (and (seq color) title-attributes)
                (str "<dl class=\"colored-bg\" style=\"background-color: " color "; color: " (font-color color) ";\">"
                     (str/join (mapv attribute-desc-fn
                                     (sort-by first
                                              (filter identity
                                                      (select-keys event title-attributes)))))
                     "</dl>")
                "")
              (if (seq selected-attributes)
                (str "<dl>"
                     (str/join (mapv attribute-desc-fn
                                     (sort-by first
                                              (select-keys event
                                                           (filterv #(not (title-attributes-set %))
                                                                    selected-attributes)))))
                     "</dl>")
                "")))))

(comment
  (let [content-with-no-title {:event {"test" "test-title"
                                       "attr-1" 0
                                       "attr-2" "fooo"
                                       "attr-3" "bar"}
                               :display-attributes ["attr-1" "attr-2" "attr-3" "test"]}
        localize-num-fn (fn [num]
                          (let [lang "en-GB"]
                            (.toLocaleString num lang)))
        attribute-label-fn (fn [attr]
                             attr)
        {:keys [color event
                title-attributes
                display-attributes]} content-with-no-title]

    (gen-popup-content localize-num-fn
                       attribute-label-fn
                       color
                       event
                       title-attributes
                       display-attributes))

  (let [content-with-no-title-all-attrs {:event {"test" "test-title"
                                                 "attr-1" 0
                                                 "attr-2" "fooo"
                                                 "attr-3" "bar"
                                                 "attr-4" 20}
                                         :display-attributes :all}
        localize-num-fn (fn [num]
                          (let [lang "en-GB"]
                            (.toLocaleString num lang)))
        attribute-label-fn (fn [attr]
                             attr)
        {:keys [color event
                title-attributes
                display-attributes]} content-with-no-title-all-attrs]
    (gen-popup-content localize-num-fn
                       attribute-label-fn
                       color
                       event
                       title-attributes
                       display-attributes)))