(ns de.explorama.frontend.woco.frame.view.core
  (:require ["re-resizable" :refer [Resizable]]
            [clojure.string :refer [join split]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.components.frames.core :refer [vertical-frame]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :refer [dispatch dispatch-sync reg-event-fx
                                   subscribe]]
            [reagent.core :as r]
            [de.explorama.frontend.woco.api.couple :as couple-api]
            [de.explorama.frontend.woco.api.interaction-mode :as im-api]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.api :as fapi]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.interaction.dnd :as dnd]
            [de.explorama.frontend.woco.frame.interaction.move :as move]
            [de.explorama.frontend.woco.frame.interaction.snapping :as snapping]
            [de.explorama.frontend.woco.frame.interaction.z-order :as z-order]
            [de.explorama.frontend.woco.frame.plugin-api :as papi]
            [de.explorama.frontend.woco.frame.plugin-impl :as plugi]
            [de.explorama.frontend.woco.frame.size-position :refer [frame-position-sub
                                                                    set-frame-position
                                                                    set-frame-full-size set-frame-size
                                                                    set-resize-infos
                                                                    resize-info]]
            [de.explorama.frontend.woco.frame.util :refer [handle-param is-content-frame?
                                                           is-custom-frame?]]
            [de.explorama.frontend.woco.frame.view.header :refer [frame-header full-title]]
            [de.explorama.frontend.woco.frame.view.legend :refer [legend legend-open?]]
            [de.explorama.frontend.woco.frame.view.overlay.filter :refer [frame-filter]]
            [de.explorama.frontend.woco.frame.view.overlay.loading :refer [frame-loading-screen]]
            [de.explorama.frontend.woco.frame.view.overlay.notifications :refer [frame-notifications]]
            [de.explorama.frontend.woco.frame.view.overlay.stop :refer [stop-screen]]
            [de.explorama.frontend.woco.frame.view.overlay.warn :refer [warn-screen]]
            [de.explorama.frontend.woco.frame.view.product-tour :refer [product-tour-step]]
            [de.explorama.frontend.woco.frame.view.toolbar :refer [toolbar-comp]]
            [de.explorama.frontend.woco.navigation.snapping :as wns]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.screenshot.pdf :refer [finalize-pdf]]
            [de.explorama.frontend.woco.screenshot.core :refer [make-frame-screenshot make-screenshot]]
            [de.explorama.frontend.woco.screenshot.util :refer [base64-valid? download-base64-img]]
            [de.explorama.frontend.woco.workspace.states :as wws]))

(def resizable-comp (r/adapt-react-class Resizable))
(defonce optimization-states (atom {}))

(defn- frame-style
  "Creates styles map for frame"
  [{[x y] :coords
    [fw fh] :full-size
    :keys [z-index is-maximized?]}]
  (cond-> {:position :absolute
           ;:content-visibility :auto
           :z-index z-index}
    is-maximized? (assoc :z-index config/maximized-index
                         :transform (str "translate3d(0px, " config/userbar-height "px, 0)"))
    (not is-maximized?) (assoc :transform (str "translate3d(" x "px, " y "px, 0)"))
    (and fw fh) (assoc :height fh
                       :width fw)))

(defn vertical-frame-props
  "Properties for vertical-frame component which will handle dragging states"
  [drag-props]
  {:ignore-child-events? (subscribe [::navigation-control/enable-overlay?])
   :force-on-ignore (cond-> {}
                      (not (move/moving-state))
                      (merge (select-keys drag-props [:on-click :on-mouse-down :on-mouse-move :on-mouse-up :style :on-mouse-leave]))
                      :always
                      (assoc :on-mouse-up (:on-mouse-up drag-props)))
   :drag-props drag-props})

(defn- frame-skeleton
  "Provides the basic skeleton from frame like frame-component, header and overlays"
  [_ {frame-id :id} _ _ _]
  (r/create-class
   {:component-did-mount (fn [_]
                           (when-let [did-mount-fn @(subscribe [::papi/component-did-mount frame-id])]
                             (did-mount-fn frame-id)))
    :reagent-render (fn [focus-props {:keys [is-maximized? is-minimized? full-size selected?] frame-id :id :as frame-desc} module comp-ref frame-type]
                      (let [parent-container-id @(subscribe [::papi/parent-container-id frame-id])
                            drop-area-extra? (when-let [sub-fn @(subscribe [::papi/drop-area-extra? frame-id])]
                                               (val-or-deref (sub-fn frame-id)))
                            ignore-child-events-fn @(subscribe [::papi/ignore-child-events? frame-id])
                            drag-props (dnd/drag-props frame-id frame-type)
                            vertical-frame-props (cond-> (vertical-frame-props drag-props)
                                                   ignore-child-events-fn (assoc :ignore-child-events?
                                                                                 (val-or-deref (ignore-child-events-fn frame-id))))
                            open-legend? (legend-open? frame-id)
                            frame-focus? (and
                                          (not selected?)
                                          (not (@wws/temporary-selection frame-id))
                                          (not (move/moving-data frame-id))
                                          (not is-minimized?)
                                          (or (val-or-deref (:focus-state focus-props))
                                              is-maximized?))
                            {:keys [width height]} (when (and full-size (not is-minimized?))
                                                     (let [[^number width ^number height] full-size
                                                           height (- height config/header-height)]
                                                       {:width width
                                                        :height height}))
                            module-comp @(fi/call-api :service-target-sub :modules module)]
                        [:<>
                         [frame-notifications frame-id]
                         [frame-header frame-desc drag-props open-legend?]
                         [product-tour-step frame-id]
                         [:div.window__body__wrapper {:style (cond-> {}
                                                               ;(number? width)
                                                               ;(assoc :width width)
                                                               ;(number? height)
                                                               ;(assoc :height height
                                                               ;       :min-height height)
                                                               is-maximized?
                                                               (assoc :width "100%"
                                                                      :height "100%")
                                                               is-minimized?
                                                               (assoc :width width
                                                                      :height config/minimized-height))}
                          [vertical-frame (cond-> vertical-frame-props
                                            #_(assoc-in vertical-frame-props
                                                        [:extra-props :style]
                                                        {:content-visibility :auto})
                                            :always (dissoc :drag-props)
                                            parent-container-id (assoc-in [:extra-props :id]
                                                                          (val-or-deref (parent-container-id frame-id)))
                                            drop-area-extra? (dissoc :force-on-ignore))
                           [toolbar-comp {:show? frame-focus?
                                          :focus-props focus-props
                                          :frame-id frame-id
                                          :frame-desc frame-desc
                                          :show-legend? open-legend?}]
                           (if module-comp
                             ^{:key (str "woco-module_" frame-id)}
                             [module-comp frame-id (when drop-area-extra?
                                                     (get vertical-frame-props :force-on-ignore))]
                             [:div.no-module (dnd/drag-props frame-id frame-type)])]
                          (when (and open-legend? (not is-minimized?))
                            [:div.extra-column {:style {:width (str config/legend-width "px")}}
                             [legend frame-id frame-focus?]])]]))}))

(defn- build-frame-classes
  "Builds classes for frame"
  [frame-id {:keys [optional-class is-maximized?]} drop-zone-active? selected?]
  (join " " (cond->  [config/window-class
                      "explorama__window" ;;TODO change this to frame, however currently many classes depend on it
                      "bg-white"
                      optional-class]
              is-maximized?
              (conj "explorama__window--maximized")

              drop-zone-active?
              (conj "drop-target")

              selected?
              (conj "selected")

              (@wws/temporary-selection frame-id)
              (conj "selection"))))

(defn- resize-enable-prop
  "Defines if frame is resizable and in which way"
  [{:keys [is-maximized? is-minimized? resizable]} drop-zone-active? enable-overlay? read-only?]
  (if (or enable-overlay?
          (not resizable)
          read-only?)
    false
    (let [resize? (and (not is-maximized?)
                       (not is-minimized?)
                       (not drop-zone-active?))]
      {:right resize?
       :bottom resize?
       :bottomRight resize?
       :top resize?
       :left resize?
       :topRight resize?
       :bottomLeft resize?
       :topLeft resize?})))

(reg-event-fx
 ::screenshot
 (fn [_ [_ frame-id title type add-export-details?]]
   (cond (and (get-in @optimization-states [frame-id :screenshot-state])
              @(get-in @optimization-states [frame-id :screenshot-state])
              (= type :pdf))
         (finalize-pdf (js/document.getElementById (config/frame-optimization-dom-id frame-id))
                       @(get-in @optimization-states [frame-id :screenshot-state])
                       {:type type
                        :file-name title
                        :frame-ids #{frame-id}
                        :add-export-details? add-export-details?
                        :callback-fn #(dispatch [:de.explorama.frontend.woco.page/global-loadingscreen false])})
         (and (get-in @optimization-states [frame-id :screenshot-state])
              @(get-in @optimization-states [frame-id :screenshot-state])
              (= type :png))
         (do
           (download-base64-img @(get-in @optimization-states [frame-id :screenshot-state])
                                title)
           (dispatch [:de.explorama.frontend.woco.page/global-loadingscreen false]))
         :else
         (make-screenshot {:frame-id frame-id
                           :frame-ids #{frame-id}
                           :type type
                           :file-name title
                           :add-export-details? add-export-details?
                           :callback-fn (fn [& _]
                                          (dispatch [:de.explorama.frontend.woco.page/global-loadingscreen false]))}))
   {}))

(reg-event-fx
 ::resize-stop
 (fn [{db :db} [_ frame-id {:keys [full-width full-height width height recalc-positions? no-event-logging? force?] :as resize-infos}]]
   (let [abort (or (nil? frame-id) (and (nil? width) (nil? height) (nil? full-width) (nil? full-height)))
         frame-exist? (not-empty (get-in db (path/frame-desc frame-id)))
         frame-component (:vertical frame-id)]
     (when frame-exist?
       (im-api/check-inter-mode db
                                (get-in db (path/frame-type frame-id))
                                {:frame-id frame-id
                                 :component frame-component
                                 :additional-info nil}
                                (fn []
                                  {:db (if abort
                                         db
                                         (-> db
                                             (set-resize-infos frame-id resize-infos)
                                             (set-frame-size frame-id width height)
                                             (set-frame-full-size frame-id full-width full-height)))
                                   :dispatch-n [(when recalc-positions? [::couple-api/recalc-positions frame-id no-event-logging?])
                                                (when-not no-event-logging?
                                                  [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id])]})
                                force?)))))

(defn- on-frame-resize
  "Will be triggered when frame is resized by user"
  [{[cw ch] :size
    [fw fh] :full-size
    :keys [couple-infos id]}
   delta]
  (let [new-width (+ cw (aget delta "width"))
        new-height (+ ch (aget delta "height"))
        new-fw (+ fw (aget delta "width"))
        new-fh (+ fh (aget delta "height"))
        coupled-with (:with couple-infos)
        resize-infos (cond-> (resize-info (aget delta "width")
                                          (aget delta "height")
                                          new-width
                                          new-height
                                          new-fw
                                          new-fh)
                       coupled-with (assoc :recalc-positions? true))]
    (dispatch-sync [::resize-stop id resize-infos])))

(defn- trigger-screenshot [frame-id {:keys [screenshot-state show-screenshot-state clicked-in? is-in-frame? creating? data-changed?]}]
  (make-frame-screenshot {:frame-id frame-id
                          :callback-fn (fn [base64-screenshot]
                                         (reset! creating? false)
                                         (when (and (not @is-in-frame?)
                                                    base64-screenshot
                                                    (base64-valid? base64-screenshot))
                                           (z-order/reset-focused-frame)
                                           (reset! data-changed? false)
                                           (reset! clicked-in? false)
                                           (reset! screenshot-state base64-screenshot)
                                           (reset! show-screenshot-state true)))}))

;; logic for taking Screenshots:
;; Case 1: no screenshot exists -> frame leaved, enable overlay (woco zooming out)
;; Case 2: screenshot exists -> frame changed (title, z-index, di, loading-screen) + not in frame
;; Case 3: old screenshot will be displayed, when frame leaved or overlay enabled

(defn trigger-screenshot-ext [frame-id]
  (when-let [optimization-state (get @optimization-states frame-id)]
    (reset! (get optimization-state :screenshot-state) nil)))

(defn- optimization-node-render [{:keys [frame-id frame-type]} _ _ _ _]
  (r/create-class
   {:display-name (str "opti_node_frame_" frame-id)
    :component-did-update (fn [this argv]
                            (when-not (move/moving-state)
                              (let [[_
                                     {{old-z-index :z-index} :frame-desc}
                                     _ old-title old-di old-loading-screen?] argv
                                    [_
                                     {{new-z-index :z-index} :frame-desc {:keys [clicked-in? data-changed? creating? show-screenshot-state screenshot-state is-in-frame?] :as optimization-state} :optimization-state}
                                     enable-overlay? title di loading-screen?] (r/argv this)]
                                (when (and (not enable-overlay?)
                                         ;As indicator for clicked in
                                           (or (= frame-id @z-order/clicked-focused-frame)
                                               (not= old-z-index new-z-index)))
                                  (reset! clicked-in? true))
                                (when (and (not= old-loading-screen? loading-screen?)
                                           loading-screen?)
                                  (reset! screenshot-state nil)
                                  (reset! show-screenshot-state false))
                                (when (or (not= old-title title)
                                          (not= old-loading-screen? loading-screen?)
                                          (not= old-di di))
                                  (reset! data-changed? true))
                                (cond (and
                                       (not @creating?)
                                       (not loading-screen?)
                                       (not @is-in-frame?)
                                       (or @clicked-in?
                                           (not @screenshot-state)
                                           @data-changed?
                                           (and (or (not @screenshot-state)
                                                    @data-changed?)
                                                enable-overlay?)))
                                      (do
                                        (reset! creating? true)
                                        (js/setTimeout #(trigger-screenshot frame-id optimization-state)
                                                       500))
                                      (and
                                       (not @creating?)
                                       (not loading-screen?)
                                       (or (not @is-in-frame?)
                                           enable-overlay?)
                                       @screenshot-state)
                                      (reset! show-screenshot-state true)))))
    :reagent-render (fn [{:keys [frame-id frame-comp style class optimization-state]} enable-overlay? title di loading-screen?]
                      (let [{:keys [show-screenshot-state screenshot-state clicked-in? is-in-frame?]} optimization-state
                            ;; For triggerering rerendering/did-update-check
                            _ @clicked-in?
                            _ @is-in-frame?
                            _ @z-order/clicked-focused-frame
                            show-screenshot? @show-screenshot-state
                            screenshot-str @screenshot-state]
                        (when (and screenshot-str (or show-screenshot? enable-overlay?))
                          (let [parent-container-id @(subscribe [::papi/parent-container-id frame-id])
                                drop-area-extra? (when-let [sub-fn @(subscribe [::papi/drop-area-extra? frame-id])]
                                                   (val-or-deref (sub-fn frame-id)))
                                ignore-child-events-fn @(subscribe [::papi/ignore-child-events? frame-id])
                                drag-props (dnd/drag-props frame-id frame-type)
                                vertical-frame-props (cond-> (vertical-frame-props drag-props)
                                                       ignore-child-events-fn
                                                       (assoc :ignore-child-events?
                                                              (val-or-deref (ignore-child-events-fn frame-id))))]

                            [:div {:id (config/frame-optimization-dom-id frame-id)
                                   :style style
                                   :class class
                                   :on-mouse-enter #(when-not enable-overlay?
                                                      (reset! clicked-in? false)
                                                      (reset! is-in-frame? true)
                                                      (reset! show-screenshot-state false))}
                             [vertical-frame (cond-> vertical-frame-props
                                               parent-container-id (assoc-in [:extra-props :id]
                                                                             (val-or-deref (parent-container-id frame-id)))
                                               drop-area-extra? (dissoc :force-on-ignore))

                              [:img {:src screenshot-str
                                     :on-drag-start #(.preventDefault %)}]]]))))}))

(defn- optimization-node [{:keys [frame-id optimization-state] :as state} enable-overlay?]
  (let [{:keys [loading-sub title-fn di-sub]} optimization-state
        loading-screen? @(loading-sub frame-id)
        title (title-fn)
        di @di-sub]
    [optimization-node-render state enable-overlay? title di loading-screen?]))

(defn- handle-performance-optimization [frame-id
                                        {:keys [show-screenshot-state screenshot-state is-in-frame?]}
                                        enable-overlay?
                                        {:keys [style] :as frame-props}]
  (let [show-screenshot? @show-screenshot-state
        screenshot-str @screenshot-state]
    (assoc frame-props
           :style (cond-> style
                    (and screenshot-str (or enable-overlay? show-screenshot?))
                    (assoc :display :none))
           :on-mouse-leave (fn [_]
                             (when-not (move/moving-state)
                               (reset! is-in-frame? false)))

           :on-mouse-enter (fn [_]
                             (reset! is-in-frame? true)))))

(defn- custom-frame-render
  [frame-comp
   focus-props
   {[fw fh] :full-size
    frame-id :id
    frame-type :type
    :keys [module dom-id selected?
           min-resize-width min-resize-height]
    :as frame-desc}
   optimize-frame-performance?
   optimization-state]
  (let [{:keys [extra-style]} @(subscribe [::papi/frame frame-id])
        drop-zone-active? @(dnd/show-dropzone-sub frame-id)
        enable-overlay? @(subscribe [::navigation-control/enable-overlay?])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})
        enable-prop (resize-enable-prop frame-desc drop-zone-active? enable-overlay? read-only?)
        {workspace-zoom :z} @(subscribe [::navigation-control/position])
        frame-classes (build-frame-classes frame-id frame-desc drop-zone-active? false)
        coords @(frame-position-sub frame-id)
        {move-x :new-x move-y :new-y} (move/moving-data frame-id)
        frame-desc (cond-> (assoc frame-desc :coords coords)
                     (and move-x move-y)
                     (assoc :coords (list move-x move-y)))
        fr-style (-> (frame-style frame-desc)
                     (merge (handle-param extra-style frame-id)))
        gridsnap? (wns/snapping? :grid)
        [grid-size snap-gap] (snapping/get-grid-dim)
        frame-focus? (and
                      (not enable-overlay?)
                      (not selected?)
                      (not (@wws/temporary-selection frame-id))
                      (not (move/moving-data frame-id))
                      (val-or-deref (:focus-state focus-props)))
        module-comp @(fi/call-api :service-target-sub :modules module)]
    ^{:key (str "wocoframe-" dom-id)}
    [:<>
     [resizable-comp (cond-> {:ref #(reset! frame-comp %)
                              :size {:width (or fw 0)
                                     :height (or fh 0)}
                              :on-mouse-down (fn [e]
                                               (.stopPropagation e)
                                               (dispatch [::z-order/bring-to-front frame-id]))
                              ;:on-mouse-up #(.stopPropagation %)
                              :on-click (fn [e]
                                          (.stopPropagation e))
                              :className frame-classes
                              :style fr-style
                              :scale workspace-zoom
                              :min-width min-resize-width
                              :min-height min-resize-height
                              :grid (when gridsnap? [grid-size grid-size])
                              :snap-gap (when gridsnap? snap-gap)
                              :on-resize-start (fn [ev direction _]
                                                 (.stopPropagation ev)
                                                 (dispatch [::z-order/bring-to-front frame-id])
                                                 (when (#{"top" "left" "topLeft" "topRight" "bottomLeft"} direction)
                                                   (move/moving-state true true)
                                                   (move/start-resizing)
                                                   (let [event-x (aget ev "pageX")
                                                         event-y (aget ev "pageY")
                                                         [page-x page-y] @(subscribe [::navigation-control/page->workspace [event-x event-y]])
                                                         [frame-x frame-y] @(frame-position-sub frame-id)
                                                         offset-x (- frame-x page-x)
                                                         offset-y (- frame-y page-y)
                                                         new-x (+ page-x offset-x)
                                                         new-y (+ page-y offset-y)
                                                         max-x (+ (get-in frame-desc [:coords 0])
                                                                  (- fw min-resize-width))
                                                         max-y (+ (get-in frame-desc [:coords 1])
                                                                  (- fh min-resize-height))]
                                                     (move/moving-data frame-id
                                                                       {:new-x (+ page-x offset-x)
                                                                        :new-y (+ page-y offset-y)
                                                                        :max-x max-x
                                                                        :max-y max-y
                                                                        :offset-x offset-x
                                                                        :offset-y offset-y}))))
                              :on-resize (fn [ev direction _ delta]
                                           (let [delta [(aget delta "width") (aget delta "height")]
                                                 [delta-x delta-y] delta
                                                 {:keys [offset-x offset-y max-x max-y] old-delta :delta :as prev-data} (move/moving-data frame-id)
                                                 [old-delta-x old-delta-y] old-delta
                                                 event-x (aget ev "pageX")
                                                 event-y (aget ev "pageY")
                                                 [page-x page-y] @(subscribe [::navigation-control/page->workspace [event-x event-y]])
                                                 new-x (min (+ page-x offset-x)
                                                            max-x)
                                                 new-y (min (+ page-y offset-y)
                                                            max-y)]

                                             (cond
                                               (and (= "topLeft" direction)
                                                    (not= delta-y old-delta-y)
                                                    (not= delta-x old-delta-x))
                                               (move/moving-data frame-id (assoc prev-data :delta delta :new-x new-x :new-y new-y))
                                               (and (#{"topLeft" "top" "topRight"} direction)
                                                    (not= delta-y old-delta-y))
                                               (move/moving-data frame-id (assoc prev-data :delta delta :new-y new-y))
                                               (and (#{"topLeft" "left" "bottomLeft"} direction)
                                                    (not= delta-x old-delta-x))
                                               (move/moving-data frame-id (assoc prev-data :delta delta :new-x new-x)))))
                              :on-resize-stop (fn [ev direction ref delta]
                                                (when (#{"top" "left" "topLeft" "topRight" "bottomLeft"} direction)
                                                  (let [{:keys [new-x new-y]} (move/moving-data frame-id)]
                                                    (dispatch [::fapi/set-frame-coords frame-id new-x new-y])
                                                    ;; Workaround to reduce flickering until state is set in app db
                                                    (js/setTimeout
                                                     (fn []
                                                       (move/moving-state false true)
                                                       (move/stop-resizing))
                                                     100)))
                                                (on-frame-resize frame-desc delta))
                              :id dom-id
                              :enable enable-prop})
      [toolbar-comp {:show? frame-focus?
                     :focus-props focus-props
                     :frame-id frame-id
                     :frame-desc frame-desc}]
      (if module-comp
        ^{:key (str "woco-module_" frame-id)}
        [module-comp frame-id {:drag-props (dnd/drag-props frame-id frame-type)
                               :ignore-child-interactions? enable-overlay?}]
        [:div.no-module (dnd/drag-props frame-id frame-type)])]]))

(defn- default-frame-render
  "Render function of frame"
  [frame-comp
   focus-props
   {[fw fh] :full-size
    frame-id :id
    frame-type :type
    :keys [module dom-id
           minmax-followevent
           min-resize-width min-resize-height
           is-maximized? is-minimized? selected?]
    :as frame-desc}
   optimize-frame-performance?
   optimization-state]
  (let [{:keys [extra-style]} @(subscribe [::papi/frame frame-id])
        drop-zone-active? @(dnd/show-dropzone-sub frame-id)
        {workspace-zoom :z} @(subscribe [::navigation-control/position])
        enable-overlay? @(subscribe [::navigation-control/enable-overlay?])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})
        enable-prop (resize-enable-prop frame-desc drop-zone-active? enable-overlay? read-only?)
        frame-classes (build-frame-classes frame-id frame-desc drop-zone-active? selected?)
        coords @(frame-position-sub frame-id)
        {move-x :new-x move-y :new-y} (move/moving-data frame-id)
        open-legend? (legend-open? frame-id)
        gridsnap? (wns/snapping? :grid)
        [grid-size snap-gap] (snapping/get-grid-dim)
        frame-desc (cond-> (assoc frame-desc :coords coords)
                     (and move-x move-y)
                     (assoc :coords (list move-x move-y)))
        fr-style (-> (frame-style frame-desc)
                     (merge (handle-param extra-style frame-id)))
        min-resize-width (+ min-resize-width (when open-legend? config/legend-width))
        {:keys [width height]} @(subscribe [:de.explorama.frontend.woco.page/workspace-rect])
        height (cond-> height
                 is-maximized? (- config/userbar-height))]
    (when (vector? minmax-followevent)
      (dispatch minmax-followevent)
      (dispatch [::fapi/set-follow-event-minmaximized frame-id nil]))
    [:<>
     (when optimize-frame-performance?
       [optimization-node
        {:frame-id frame-id
         :frame-comp frame-comp
         :frame-desc frame-desc
         :frame-type frame-type
         :class frame-classes
         :style fr-style
         :optimization-state optimization-state}
        enable-overlay?])
     ^{:key (str "wocoframe-" dom-id)}
     [resizable-comp (cond-> {:ref #(reset! frame-comp %)
                              :size (cond is-minimized?
                                          {:width (cond
                                                    fw fw
                                                    :else 0)
                                           :height config/minimized-height}
                                          is-maximized?
                                          {:width width
                                           :height height}
                                          :else
                                          {:width (or fw 0)
                                           :height (or fh 0)})
                              :on-mouse-down (fn [e]
                                               (.stopPropagation e)
                                               (dispatch [::z-order/bring-to-front frame-id]))
                              ;:on-mouse-up #(.stopPropagation %)
                              :on-click (fn [e]
                                          (.stopPropagation e))
                              :className frame-classes
                              :style fr-style
                              :scale workspace-zoom
                              :min-width (when (not is-minimized?)
                                           min-resize-width)
                              :min-height (when (not is-minimized?)
                                            min-resize-height)
                              :grid (when gridsnap? [grid-size grid-size])
                              :snap-gap (when gridsnap? snap-gap)
                              :on-resize-start (fn [ev direction _]
                                                 (.stopPropagation ev)
                                                 (dispatch [::z-order/bring-to-front frame-id])
                                                 (when (#{"top" "left" "topLeft" "topRight" "bottomLeft"} direction)
                                                   (move/moving-state true true)
                                                   (move/start-resizing)
                                                   (let [event-x (aget ev "pageX")
                                                         event-y (aget ev "pageY")
                                                         [page-x page-y] @(subscribe [::navigation-control/page->workspace [event-x event-y]])
                                                         [frame-x frame-y] @(frame-position-sub frame-id)
                                                         offset-x (- frame-x page-x)
                                                         offset-y (- frame-y page-y)
                                                         new-x (+ page-x offset-x)
                                                         new-y (+ page-y offset-y)
                                                         max-x (+ (get-in frame-desc [:coords 0])
                                                                  (- fw min-resize-width))
                                                         max-y (+ (get-in frame-desc [:coords 1])
                                                                  (- fh min-resize-height))]
                                                     (move/moving-data frame-id
                                                                       {:new-x new-x
                                                                        :new-y new-y
                                                                        :max-x max-x
                                                                        :max-y max-y
                                                                        :offset-x offset-x
                                                                        :offset-y offset-y}))))
                              :on-resize (fn [ev direction _ delta]
                                           (let [delta [(aget delta "width") (aget delta "height")]
                                                 [delta-x delta-y] delta
                                                 {:keys [offset-x offset-y max-x max-y] old-delta :delta :as prev-data} (move/moving-data frame-id)
                                                 [old-delta-x old-delta-y] old-delta
                                                 event-x (aget ev "pageX")
                                                 event-y (aget ev "pageY")
                                                 [page-x page-y] @(subscribe [::navigation-control/page->workspace [event-x event-y]])
                                                 new-x (min (+ page-x offset-x)
                                                            max-x)
                                                 new-y (min (+ page-y offset-y)
                                                            max-y)]
                                             (cond
                                               (and (= "topLeft" direction)
                                                    (not= delta-y old-delta-y)
                                                    (not= delta-x old-delta-x))
                                               (move/moving-data frame-id (assoc prev-data :delta delta :new-x new-x :new-y new-y))
                                               (and (#{"topLeft" "top" "topRight"} direction)
                                                    (not= delta-y old-delta-y))
                                               (move/moving-data frame-id (assoc prev-data :delta delta :new-y new-y))
                                               (and (#{"topLeft" "left" "bottomLeft"} direction)
                                                    (not= delta-x old-delta-x))
                                               (move/moving-data frame-id (assoc prev-data :delta delta :new-x new-x)))))
                              :on-resize-stop (fn [ev direction ref delta]
                                                (when (#{"top" "left" "topLeft" "topRight" "bottomLeft"} direction)
                                                  (let [{:keys [new-x new-y]} (move/moving-data frame-id)]
                                                    (dispatch [::fapi/set-frame-coords frame-id new-x new-y])
                                                    ;; Workaround to reduce flickering until state is set in app db
                                                    (js/setTimeout
                                                     (fn []
                                                       (move/moving-state false true)
                                                       (move/stop-resizing))
                                                     100)))
                                                (on-frame-resize frame-desc delta))
                              :id dom-id
                              :enable enable-prop}
                       optimize-frame-performance? (->> (handle-performance-optimization frame-id optimization-state enable-overlay?)))

      (when-not is-minimized?
        [:<>
         [frame-filter frame-id]
         [frame-loading-screen frame-id]
         [warn-screen frame-id (subscribe [::plugi/warn-screen])]
         [stop-screen frame-id (subscribe [::plugi/stop-screen])]
         [warn-screen frame-id (subscribe [::papi/warn-screen frame-id])]
         [stop-screen frame-id (subscribe [::papi/stop-screen frame-id])]])
      [frame-skeleton focus-props frame-desc module frame-comp frame-type]]]))

(defn- frame-did-update
  "Will be triggered, when frame is updating"
  [id
   focus-state
   this
   old-argv]
  (let [[{old-size :size old-maxi :is-maximized? old-mini :is-minimized?}
         _ old-wsp-position]
        (rest old-argv)
        [{new-size :size new-maxi :is-maximized? new-mini :is-minimized? :as new-frame-infos}
         _ new-wsp-position new-toolbar-disabled?]
        (rest (r/argv this))
        resized-infos (get new-frame-infos :resized-infos)]
    (when (and @focus-state
               (or new-toolbar-disabled?
                   (not= old-wsp-position new-wsp-position)))
      (reset! focus-state false))
    (when (or (not= old-size new-size)
              (not= old-maxi new-maxi)
              (not= old-mini new-mini))
      (dispatch [:de.explorama.frontend.woco.frame.api/frame-did-update id]))
    (when resized-infos
      (dispatch [::fapi/set-resize-infos id resized-infos]))))

(defn- init-optimization-state [frame-id]
  {:show-screenshot-state (r/atom false)
   :screenshot-state (r/atom nil)
   :last-enter-timestamp (r/atom nil)
   :data-changed? (r/atom false)
   :is-in-frame? (r/atom false)
   :creating? (atom false)
   :clicked-in? (atom false)
   :loading-sub @(subscribe [::papi/loading? frame-id])
   :title-fn (fn [] (full-title frame-id))
   :di-sub (fi/call-api :connection-data-for-frame-sub frame-id :di)})

(defn- handle-timeout [timeout-state func timeout]
  (when-let [t @timeout-state]
    (js/clearTimeout t))
  (reset! timeout-state
          (js/setTimeout func timeout)))

(def ^:private ignore-focus-classes #{toolbar-ignore-class})

(defn- resizable-frame
  "Frame which is resizable and has header and "
  [{:keys [id dom-id] :as frame-desc} optimize-frame-performance? _ _]
  (let [comp-ref (atom nil)
        optimization-state (when optimize-frame-performance?
                             (let [optimization-state (init-optimization-state id)]
                               (swap! optimization-states assoc id optimization-state)
                               optimization-state))
        focus-state (r/atom false)
        ;; used to support some animation while timeout runs
        extra-style (r/atom nil)
        timeout (atom nil)
        on-show (fn [e]
                  (when-not @(subscribe [::navigation-control/enable-overlay?])
                    (reset! extra-style nil)
                    (handle-timeout timeout
                                    #(reset! focus-state true)
                                    config/show-delay)))
        on-hide (fn [e]
                   ;;ignore select menus/portals
                  (when (or (not (aget e "relatedTarget"))
                            (and (not= js/document.body (aget e "relatedTarget" "parentNode"))
                                 (not (some ignore-focus-classes
                                            (split (aget e "relatedTarget" "className") #" ")))))
                    (reset! extra-style {:opacity 0})
                    (handle-timeout timeout
                                    #(reset! focus-state false)
                                    config/hide-delay)))
        on-focus (fn [e]
                   (when (= dom-id (aget e "target" "id"))
                     (on-show e)))
        on-focus-lost (fn [e]
                        (when (= dom-id (aget e "target" "id"))
                          (on-hide e)))
        focus-props {:focus-state focus-state
                     :extra-style extra-style
                     :on-show on-show
                     :on-hide on-hide}]

    (r/create-class
     {:display-name (str "frame: " id)
      :component-did-update (partial frame-did-update id focus-state)
      :component-did-mount (fn [_]
                             (when dom-id
                               (when-let [dom-node (js/document.getElementById dom-id)]
                                 (.addEventListener dom-node "mouseenter" on-focus true)
                                 (.addEventListener dom-node "mouseleave" on-focus-lost true))))
      :component-will-unmount (fn [_]
                                (when dom-id
                                  (when-let [dom-node (js/document.getElementById dom-id)]
                                    (.removeEventListener dom-node "mouseenter" on-focus true)
                                    (.removeEventListener dom-node "mouseleave" on-focus-lost true))))
      :reagent-render (fn [{frame-type :type :as frame-desc} optimize-frame-performance? _ _]
                        (condp = frame-type
                          evts/custom-type
                          [custom-frame-render comp-ref focus-props frame-desc optimize-frame-performance? optimization-state]
                          ;;TODO r1/woco handle other frame-types?
                          [default-frame-render comp-ref focus-props frame-desc optimize-frame-performance? optimization-state]))})))

(defn frame
  "Root component for a frame"
  [frame-id]
  (let [dom-id (config/frame-dom-id frame-id)
        {fr-mod :module
         [arfl-w arfl-h] :size
         :as frame-desc} @(subscribe [::evts/frame frame-id])
        frame-desc (when frame-desc (assoc frame-desc :dom-id dom-id))
        module-comp @(fi/call-api :service-target-sub :modules fr-mod)]
    (when (and frame-id frame-desc arfl-w arfl-h (fn? module-comp))
      (let [optimize-frame-performance? @(subscribe [::papi/optimize-frame-performance frame-id])
            wsp-position @(subscribe [::navigation-control/position])
            toolbar-disabled? @(subscribe [::navigation-control/enable-overlay?])]
        ^{:key (str "woco-rez-frame" frame-id)}
        [resizable-frame frame-desc optimize-frame-performance? wsp-position toolbar-disabled?]))))