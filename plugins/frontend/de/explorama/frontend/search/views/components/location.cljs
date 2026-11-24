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

;; ---------------------------------------------------------------------------
;; Tailwind class stacks — migrated from styles/src/scss/components/_search.scss
;; "Map integration (geographical filtering)" family (.map-container/
;; .map-actions/.map-hint/.map-input/.hint-text/.ol-zoom). This is the single
;; owner of this rule family; nothing else in the app selects on these
;; classes (verified by repo-wide grep before deleting the SCSS block).
;;
;; `.map-input` always carried the literal "unselected" class in every
;; historical revision of this file (`git log -p` shows the two class names
;; hardcoded together back to the ol-map's introduction) -- the class does
;; not toggle at runtime. It marks the *branch* of `location-input` where the
;; widget is in its passive/collapsed display (the `if-not @alter-state` arm,
;; as opposed to the `.map-container` editing overlay), and that branch IS
;; the condition. So the `.unselected`-driven declarations
;; (`.hint-text` opacity, the `::before` overlay tint) are baked in as
;; unconditional utilities on this element rather than re-implemented behind
;; a synthetic always-true flag -- inventing a toggle nothing sets would not
;; be "mirroring the current condition", it would be adding one. If a future
;; change makes `unselected` genuinely conditional, `hint-text-classes` and
;; the `before:bg-[...]`/`before:z-[1]` bit of `map-input-classes` are the
;; two spots to gate.
(def ^:private map-container-classes
  "w-full h-full relative")

(def ^:private map-actions-classes
  "absolute bottom-0 left-0 right-0 m-2.5 flex flex-row justify-between")

(def ^:private map-actions-group-classes
  "flex gap-1")

(def ^:private map-hint-classes
  (str "absolute top-2.5 right-16 bottom-auto left-16 py-[0.5em] px-[0.75em] "
       "rounded-sm text-white bg-[rgba(13.1,14.6,15.3,0.8)] text-xs text-center"))

;; `map-input` marker kept as a bare literal alongside the utility stack
;; (matches no CSS rule after this migration, harmless hook -- see brief
;; calibration). `.map-input > div` (the OL map's mounted target div,
;; rendered by the shared `location-react-comp` also used inside
;; `.map-container`) is expressed as an `[&>div]:` arbitrary child variant
;; here rather than threading a class prop through that shared component, so
;; the `.map-container` usage is untouched. `.ol-zoom` (OpenLayers' own zoom
;; control, rendered into the mounted map div by the ol library, not by our
;; hiccup) is hidden the same way via an `[&_.ol-zoom]:` descendant variant.
;; `ease-[ease]` on every transition below: the SCSS source's `transition:
;; <props> 120ms;` never names a timing-function, so it defaults to the CSS
;; initial `ease` -- Tailwind's bare `duration-[120ms]` instead defaults to
;; `--default-transition-timing-function` (a different cubic-bezier curve),
;; so `ease-[ease]` is required to match. This mirrors the established
;; pattern for the same situation elsewhere in this codebase (e.g.
;; woco/presentation/sidebar.cljs, ui_base/formular/collapsible_list.cljs,
;; configuration/views/data_management/geographic_attributes.cljs all use
;; `transition-[prop] duration-[Nms] ease-[ease]` for the same reason).
(def ^:private map-input-classes
  (str "map-input relative border border-gray-300 rounded-xs overflow-hidden shadow-xs "
       "bg-white cursor-pointer transition-[border-color,box-shadow] duration-[120ms] ease-[ease] "
       "group hover:border-gray-400 hover:shadow-sm "
       "before:content-[''] before:absolute before:inset-0 "
       "before:bg-[rgba(255,255,255,0.5)] before:z-[1] "
       "before:transition-[background-color] before:duration-[120ms] before:ease-[ease] "
       "[&>div]:m-auto [&>div]:p-0.5 [&>div]:rounded-lg [&>div]:overflow-hidden "
       "[&_.ol-zoom]:hidden"))

;; opacity-100 baked in (see note above); group-hover:text-gray-900 mirrors
;; `.map-input:hover .hint-text { color: gray-900 }` -- a genuine
;; parent-hover-affects-child rule, translated as group/group-hover per
;; calibration, never a bare `hover:` on this element.
;; `hint-text` marker kept as a bare literal alongside the utility stack, same
;; as `map-input` above: `styles/src/scss/base/_themes.scss:316` still selects
;; `.map-input .hint-text` under `@media (forced-colors: active)` to force
;; Windows High-Contrast-Mode text color, so the literal token must stay in
;; the DOM even though normal-mode styling is now fully expressed as utilities.
(def ^:private hint-text-classes
  (str "hint-text flex items-center justify-center absolute inset-0 z-[2] opacity-100 "
       "text-gray-800 transition-[color,opacity] duration-[120ms] ease-[ease] group-hover:text-gray-900"))

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
           [:div {:class map-input-classes
                  :on-click (fn []
                              (reset! alter-state true))}
            [:span {:class hint-text-classes} passive-label]
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
            [:div {:class map-container-classes}
             [location-react-comp
              dom-id instance alter-state
              rect-state internal-state
              (or @internal-state
                  @ui-selection)
              woco-zoom]
             [:div {:class map-actions-classes}
              [:div {:class map-actions-group-classes}
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
              [:div {:class map-actions-group-classes}
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
               [:div {:class map-hint-classes} hint])]]))
       child])))
