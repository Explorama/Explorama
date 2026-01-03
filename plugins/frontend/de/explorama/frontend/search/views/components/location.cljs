(ns de.explorama.frontend.search.views.components.location
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.search.config :refer [geo-config]]
            [de.explorama.frontend.woco.workarounds.map :as workarounds]
            ["ol/layer/Tile" :as Tile]
            ["ol/layer/Vector" :as Vector]
            ["ol/source/Vector" :as SourceVector]
            ["ol/source/XYZ" :as SourceXYZ]
            ["ol/Map" :as Map]
            ["ol/View" :as View]
            ["ol-ext/interaction/DrawRegular" :as RegularInteraction]
            ["ol/events/condition" :as condition]
            ["ol/format/GeoJSON" :as GeoJSON]))

(def ^:private default-proj "EPSG:900913")

(defn- set-event-pixel-fn [workspace-scale-fn]
  ;; issue #60
  #_(when-not @workarounds/initialized?
    ;Based on the given example from this issue
    ;https://github.com/openlayers/openlayers/issues/13283
      (aset (.-ol js/window) "PluggableMap" "prototype" "getEventPixel"
            (fn [event]
              (this-as ^js this
                       (let [scale @(workspace-scale-fn)
                             viewportPosition (.getBoundingClientRect (.getViewport this))
                             size (clj->js
                                   [(aget viewportPosition "width")
                                    (aget viewportPosition "height")])]
                         (clj->js [(/ (/ (* (- (aget event "clientX")
                                               (aget viewportPosition "left"))
                                            (aget size "0"))
                                         (aget viewportPosition "width"))
                                      scale)
                                   (/ (/ (* (- (aget event "clientY")
                                               (aget viewportPosition "top"))
                                            (aget size "1"))
                                         (aget viewportPosition "height"))
                                      scale)])))))
      (reset! workarounds/initialized? true)))

(defn- new-map-instance [target rect-state internal-state init-value woco-zoom]
  (set-event-pixel-fn woco-zoom)
  (let [init-value (filterv number? init-value)
        init-value (if (= 4 (count init-value))
                     init-value
                     [])
        layers #js[(new (.-default Tile)
                        (clj->js (merge {:source (new (.-default SourceXYZ)
                                                      (clj->js (merge {:projection default-proj
                                                                       :crossOrigin ""}
                                                                      (:source geo-config))))}
                                        (dissoc geo-config :source))))]
        view (new (.-default View)
                  #js{:zoom 0
                      :center #js[0 0]})
        map-obj (new (.-default Map)
                     #js{:target target
                         :view view
                         :layers layers})
        vectorsource-obj (new (.-default SourceVector))
        vector-obj (new (.-default Vector)
                        #js{:name "BoundingBox"
                            :source vectorsource-obj})
        interaction (new (.-default RegularInteraction)
                         #js{:source (.getSource vector-obj)
                             :sides 4
                             :canRotate false
                             :condition (fn [e]
                                          (and @rect-state
                                               (= 0 (aget e "originalEvent" "button"))))
                             :centerCondition condition/never
                             :squareCondition condition/never})]
    (when-not (empty? init-value)
      (let [features (.readFeatures (new (.-default GeoJSON))
                                    (clj->js {"type" "LineString",
                                              "coordinates" [[(get init-value 3)
                                                              (get init-value 0)]
                                                             [(get init-value 1)
                                                              (get init-value 0)]
                                                             [(get init-value 1)
                                                              (get init-value 2)]
                                                             [(get init-value 3)
                                                              (get init-value 2)]
                                                             [(get init-value 3)
                                                              (get init-value 0)]]})
                                    #js{:dataProjection "EPSG:4326"
                                        :featureProjection
                                        (get-in geo-config [:source :projection] default-proj)})]
        (.fit view
              (.getExtent (.getGeometry (aget features 0)))
              #js{:padding #js[5 15 5 15]})
        (.addFeatures vectorsource-obj
                      features)))
    (.addLayer map-obj vector-obj)
    (.addInteraction map-obj interaction)
    (.on interaction "drawstart"
         (fn [_]
           (.clear vectorsource-obj)))
    (.on interaction "drawend"
         (fn [_]
           (let [geo (->> (.getFeatures vectorsource-obj)
                          first
                          .getGeometry)
                 geo-clone (.clone geo)
                 coords (-> geo-clone
                            (.transform (get-in geo-config [:source :projection] default-proj)
                                        "EPSG:4326")
                            (.getCoordinates geo)
                            js->clj
                            first)
                 max-values (reduce (fn [[mlat mlng] [lng lat]]
                                      [(max mlat lat)
                                       (max mlng lng)])
                                    [-90 -180]
                                    coords)
                 min-values (reduce (fn [[mlat mlng] [lng lat]]
                                      [(min mlat lat)
                                       (min mlng lng)])
                                    [90 180]
                                    coords)
                 coords (into min-values
                              max-values)]
             (reset! internal-state coords)
             (reset! rect-state false))))
    {:map map-obj
     :vector-source vectorsource-obj}))

(defn- location-react-comp [dom-id instance alter-state rect-state internal-state init-value woco-zoom]
  (reagent/create-class {:display-name dom-id
                         :reagent-render
                         (fn []
                           [:div {:id dom-id
                                  :style (if @alter-state
                                           {:width "100%"
                                            :height "100%"}
                                           {:width 244
                                            :height 50})}])
                         :component-did-mount
                         (fn [_]
                           (reset! instance
                                   (new-map-instance dom-id rect-state internal-state init-value woco-zoom)))
                         :should-component-update
                         (fn [_ _ _]
                           false)
                         :component-did-update
                         (fn [_ _])
                         :component-will-unmount
                         (fn [_]
                           (.setTarget (:map @instance) nil))}))

(defn location-input [{:keys [path frame-id on-change extra-style child]}]
  (let [dom-id (str path frame-id "-loc")
        instance (atom nil)
        rect-state (reagent/atom false)
        alter-state (reagent/atom false)
        internal-state (atom nil)
        ui-selection (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])
        woco-zoom (fi/call-api :workspace-scale-sub)]
    (fn [_]
      [:<>
       (if-not @alter-state
         (let [passive-label @(re-frame/subscribe [::i18n/translate :search-location-select])]
           [:div {:class "map-input unselected"
                  :on-click (fn []
                              (reset! alter-state true))}
            [:span {:class "hint-text"} passive-label]
            [location-react-comp
             dom-id instance alter-state
             rect-state internal-state
             (or @internal-state
                 @ui-selection)
             woco-zoom]])
         (let [{apply :search-location-apply
                cancel :search-location-cancel
                hint :search-location-hint
                select-location-tooltip :search-select-location-tooltip
                reset-selection-tooltip :search-reset-location-selection-tooltip}
               @(re-frame/subscribe [::i18n/translate-multi
                                     :search-location-apply
                                     :search-location-cancel
                                     :search-location-hint
                                     :search-select-location-tooltip
                                     :search-reset-location-selection-tooltip])]
           [:div.overlay {:style extra-style}
            [:div.map-container
             [location-react-comp
              dom-id instance alter-state
              rect-state internal-state
              (or @internal-state
                  @ui-selection)
              woco-zoom]
             [:div.map-actions
              [:div
               [button {:title select-location-tooltip
                        :variant (if @rect-state
                                   :primary
                                   :secondary)
                        :disabled? @rect-state
                        :start-icon :select
                        :on-click #(reset! rect-state true)}]
               [button {:title reset-selection-tooltip
                        :variant :secondary
                        :start-icon :reset
                        :on-click #(do
                                     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection nil])
                                     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values nil])
                                     (reset! rect-state false)
                                     (reset! internal-state nil)
                                     (.clear (:vector-source @instance)))}]]
              [:div
               [button {:label apply
                        :disabled? (not @internal-state)
                        :variant :secondary
                        :start-icon :check
                        :on-click
                        (fn []
                          (let [coords (if-let [internal-state @internal-state]
                                         internal-state
                                         @ui-selection)]
                            (reset! alter-state false)
                            (reset! rect-state false)
                            (when coords
                              (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection coords])
                              (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values coords]))
                            (on-change)))}]
               [button {:label cancel
                        :variant :secondary
                        :start-icon :close
                        :on-click
                        (fn []
                          (reset! rect-state false)
                          (reset! internal-state @ui-selection)
                          (reset! alter-state false)
                          (on-change))}]]]
             (when @rect-state
               [:div.map-hint hint])]]))
       child])))
