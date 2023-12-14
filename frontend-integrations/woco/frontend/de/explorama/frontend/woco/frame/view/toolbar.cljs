(ns de.explorama.frontend.woco.frame.view.toolbar
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [context-menu
                                                                        toolbar toolbar-divider]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class export-ignore-class]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :refer [dispatch reg-event-fx reg-sub subscribe]]
            [react-dom :as react-dom]
            [reagent.core :as r]
            [taoensso.timbre :refer [error]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.filter.core :as filter-core]
            [de.explorama.frontend.woco.frame.plugin-api :as papi]
            [de.explorama.frontend.woco.frame.util :refer [handle-param]]
            [de.explorama.frontend.woco.frame.view.overlay.filter :as filter-view]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.api.product-tour :as product-tour]))

(defn- toolbar-item [toolbar-state frame-id
                     {:keys [icon extra-class extra-props title
                             group label active? on-click disabled? icon-props tooltip]}]
  (let [id (str frame-id group label)
        compact? (= config/toolbar-compact-mode-key (val-or-deref (:mode-state toolbar-state)))
        label (if (keyword? label)
                @(subscribe [::i18n/translate label])
                (val-or-deref label))
        icon-props (handle-param icon-props frame-id)
        tooltip (if (keyword? tooltip)
                  @(subscribe [::i18n/translate tooltip])
                  (val-or-deref tooltip))
        close-callback (fn [state] (update toolbar-state :result-state reset! state))]
    (cond-> {:id id
             :title (or title tooltip label)
             :active? (boolean
                       (handle-param active? frame-id))
             :disabled? (boolean
                         (handle-param disabled? frame-id))
             :on-click (fn [e]
                         (let [{curr-id :id} @(:result-state toolbar-state)]
                           (if (and (fn? on-click)
                                    (not= id curr-id))
                             (let [result-action
                                   (on-click e frame-id {:close-callback (partial close-callback nil)})]
                               (if (map? result-action)
                                 (close-callback
                                  (assoc result-action
                                         :id id
                                         :position (-> (calc-menu-position e)
                                                       (update :left - 80)
                                                       (update :top + 20))))
                                 (close-callback nil)))
                             (close-callback nil))))}
      icon-props (assoc :icon-props icon-props)
      extra-class (assoc :extra-class extra-class)
      extra-props (assoc :extra-props extra-props)
      icon
      (assoc :icon icon)
      (or (and label (not compact?))
          (not icon))
      (assoc :label label))))

(defn- filter-item [frame-id]
  (let [{:keys [disabled? tooltip-fn show-button? check-before-open]}
        @(subscribe [::papi/filter frame-id])
        disabled? (and disabled? (val-or-deref (disabled? frame-id)))
        loading-sub @(subscribe [::papi/loading? frame-id])
        loading? (when loading-sub @(loading-sub frame-id))
        filter-active? @(subscribe [::filter-view/filter-active? frame-id])]
    (when (and show-button?
               (val-or-deref (show-button? frame-id)))
      {:icon :filter
       :label (subscribe [::i18n/translate :contextmenu-top-level-filter])
       :visible? #(not @(fi/call-api [:interaction-mode :read-only-sub?]
                                     {:frame-id frame-id}))
       :tooltip (if filter-active?
                  (tooltip-fn frame-id loading? false filter-active?)
                  (subscribe [::i18n/translate :contextmenu-top-level-filter]))
       :active? filter-active?
       :disabled? disabled?
       :on-click #(when-not disabled?
                    (check-before-open frame-id [::filter-core/show frame-id]))})))

(defn- toolbar-group-items [toolbar-state frame-id group-items coupled?]
  (reduce (fn [acc {:keys [visible?] :as item}]
            (cond-> acc
              (and (= item :divider)
                   (seq acc))
              (conj toolbar-divider)
              (and (= item :filter)
                   (not coupled?))
              (conj (toolbar-item toolbar-state frame-id (filter-item frame-id)))
              (and (map? item)
                   (or (nil? visible?)
                       (handle-param visible? frame-id)))
              (conj
               (toolbar-item toolbar-state frame-id item))))
          []
          group-items))

(reg-event-fx
 ::screenshot-later
 (fn [{db :db} [_ frame-id title type]]
   {:db (assoc-in db path/global-loadingscreen true)
    :dispatch-later [{:ms 200
                      :dispatch [:de.explorama.frontend.woco.frame.view.core/screenshot frame-id title type true]}
                     {:ms 8000
                      :dispatch [:de.explorama.frontend.woco.page/global-loadingscreen false]}]}))

(defn- legend-tool [frame-id {:keys [is-minimized?]}]
  (let [{:keys [visible? disabled? on-toggle-fn]}
        @(subscribe [:de.explorama.frontend.woco.frame.plugin-api/legend frame-id])
        open-legend? @(subscribe [:de.explorama.frontend.woco.frame.view.legend/legend-open? frame-id])
        visible? (boolean (handle-param visible? frame-id))
        disabled? (boolean (handle-param disabled? frame-id))
        product-tour-active? @(subscribe [:de.explorama.frontend.woco.product-tour/running?])
        read-only? @(subscribe [:de.explorama.frontend.woco.api.interaction-mode/read-only?
                                {:component (:vertical frame-id)
                                 :additional-info :settings-info}])
        product-tour-read-only? (and product-tour-active?
                                     read-only?)]
    {:icon :info-square
     :label (subscribe [::i18n/translate :legend-section-title])
     :active? open-legend?
     :visible? visible?
     :disabled? (or is-minimized? disabled? product-tour-read-only?)
     :on-click (fn [e frame-id]
                 (dispatch [:de.explorama.frontend.woco.frame.view.legend/toggle-legend frame-id])
                 (when (fn? on-toggle-fn)
                   (on-toggle-fn frame-id (not @(subscribe [:de.explorama.frontend.woco.frame.view.legend/legend-open? frame-id])))))}))

(defn- window-tools [frame-id frame-desc toolbar-desc]
  (let [{:keys [is-minimized? is-maximized?]} frame-desc
        {:keys [on-duplicate-fn]} toolbar-desc
        {:keys [frame-title-sub frame-title-prefix-sub]}
        @(subscribe [::papi/frame-header frame-id])
        is-supported-type? (#{:frame/content-type :frame/custom-type} (:type frame-desc))
        png-label @(subscribe [::i18n/translate :png-export])
        product-tour-active? @(subscribe [::product-tour/product-tour-active?])]
    [(legend-tool frame-id frame-desc)
     {:icon :download
      :label :export-label
      :active? (fn [frame-id])
      :disabled? is-minimized?
      :visible? (not product-tour-active?)
      :on-click (fn [e frame-id]
                  (let [vertical-count-number @(subscribe [:de.explorama.frontend.woco.frame.api/vertical-count frame-id])
                        custom-title (or @(subscribe [:de.explorama.frontend.woco.frame.view.header/custom-title frame-id])
                                         (when frame-title-sub
                                           @(frame-title-sub frame-id)))
                        title-prefix (when frame-title-prefix-sub
                                       @(frame-title-prefix-sub frame-id vertical-count-number))
                        short-title (cond-> title-prefix
                                      custom-title
                                      (str (subs custom-title 0 (min (count custom-title) 50))))]
                    {:items
                     [{:label png-label
                       :icon :image
                       :on-click (fn [_]
                                   (dispatch [::screenshot-later frame-id (str short-title ".png") :png]))}
                      {:label "PDF"
                       :icon :file-empty
                       :on-click (fn [_]
                                   (dispatch [::screenshot-later frame-id (str short-title ".pdf") :pdf]))}]}))}
     {:icon :copy
      :label :copy-label
      :disabled? (or is-minimized? is-maximized?)
      :visible? (boolean
                 (and is-supported-type?
                      (fn? on-duplicate-fn)
                      (not @(fi/call-api [:interaction-mode :read-only-sub?]
                                         {:frame-id frame-id}))
                      (not product-tour-active?)))
      :on-click (fn [_ frame-id]
                  (on-duplicate-fn frame-id))}]))

(defn result-action-comp [toolbar-state]
  [:<>
   (when-let [state @(:result-state toolbar-state)]
     (let [{:keys [position items close-on-select? extra-class]
            :or {close-on-select? true}}
           state]
       [context-menu {:show? true
                      :close-on-select? close-on-select?
                      :on-close #(update toolbar-state :result-state reset! nil)
                      :position position
                      :menu-max-height 250
                      :menu-z-index config/toolbar-context-menu-z-index
                      :extra-class extra-class
                      :items (mapv (fn [{:keys [icon left-icon label] :as item}]
                                     (cond-> item
                                       (keyword? label) (assoc :label (subscribe [::i18n/translate label]))
                                       :always (dissoc :icon)
                                       (or left-icon icon) (assoc :icon (or left-icon icon))))
                                   items)}]))])

(defn- portal-comp [props & childs]
  (r/create-class
   {:display-name "toolbar portal"
    :reagent-render
    (fn [{:keys [left top height menu-z-index focus-props width]} & childs]
      (let [{:keys [on-show on-hide]} focus-props]
        (apply conj
               [:div {:class [toolbar-ignore-class export-ignore-class]
                      :on-mouse-enter on-show
                      :on-mouse-leave on-hide
                      :style (cond-> {:position :absolute
                                      :left left
                                      :height height
                                      :top top
                                      :z-index menu-z-index}
                               width (assoc :width width))}]
               childs)))}))

(defn- portal [{:keys [portal-target] :as props} childs]
  (let [portal-target-elem (if-let [ele (.getElementById js/document portal-target)]
                             ele
                             js/document.body)]
    (react-dom/createPortal
     (r/as-element
      [portal-comp props childs])
     portal-target-elem)))

(reg-sub
 ::toolbar-position
 (fn [db [_ frame-id top-offset]]
   (let [{:keys [is-maximized?]
          [frame-left frame-top] :coords
          [frame-width] :full-size}
         (fi/call-api [:frame-db-get] db frame-id)
         [left top] (fi/call-api [:workspace-coords->page-coords-db-get] db [(+ frame-left (/ frame-width 2))
                                                                             frame-top])]
     {:top (if is-maximized?
             top-offset
             (max (+ top top-offset)
                  config/userbar-height))
      :left (if is-maximized?
              0
              (max left 0))})))

(defn- toolbar-impl [toolbar-state {:keys [show? frame-id frame-desc focus-props]}]
  (let [{:keys [items extra-props apply-extra-style?]
         show-condition :show?
         :as toolbar-desc}
        @(subscribe [::papi/toolbar frame-id])
        show? (or (and show? (not show-condition))
                  (handle-param show-condition frame-id show?))
        coupled? (seq @(fi/call-api :coupled-with-sub frame-id))]
    (when show?
      (let [items (if (fn? items)
                    (-> []
                        (conj (items frame-id))
                        (conj (window-tools frame-id frame-desc toolbar-desc)))
                    [(window-tools frame-id frame-desc toolbar-desc)])
            apply-extra-style? (when apply-extra-style?
                                 (handle-param apply-extra-style? frame-id))
            extra-style (when (or (not show-condition) apply-extra-style?)
                          (val-or-deref (:extra-style focus-props)))
            {:keys [is-maximized?]} frame-desc
            extra-props (cond-> (if (fn? extra-props)
                                  (extra-props frame-id)
                                  extra-props)
                          (and extra-style (not is-maximized?))
                          (update :style (fn [o]
                                           (merge (or o {})
                                                  extra-style)))
                          is-maximized?
                          (update :style (fn [o]
                                           (merge (or o {})
                                                  {:position :absolute
                                                   :top 0
                                                   :left "50%"
                                                   :transform "translate(-50%, 0)"}))))
            mode-state (val-or-deref (fi/call-api [:user-preferences :preference-sub]
                                                  config/toolbar-mode-pref-key
                                                  config/default-toolbar-mode))
            compact? (= config/toolbar-compact-mode-key mode-state)
            toolbar-state (assoc toolbar-state :mode-state mode-state)
            top-offset (cond
                         is-maximized? 0
                         compact? -110
                         :else -130)
            {:keys [top left]} @(subscribe [::toolbar-position frame-id top-offset])]
        [:<>
         [result-action-comp toolbar-state]
         [portal {:portal-target config/workspace-parent-id
                  :focus-props focus-props
                  :menu-z-index config/toolbar-portal-z-index
                  :height 0
                  :top top
                  :left left
                  :width (when is-maximized? "100%")}
          [:<>
           (when-not is-maximized?
           ;; helper to not interupt "hovering" of window because of small space between toolbar and window
             [:div
              {:class (str toolbar-ignore-class " window-toolbar-center-x")
               :style {:height (- (+ top-offset 48))
                       :opacity 0}}])
           [toolbar (cond-> {:orientation :horizontal
                             :tooltip-direction :up
                             :on-click-toolbar-options
                             (fn [e]
                               (if-let [dispatch-vec (fi/call-api [:user-preferences :save-event-vec]
                                                                  config/toolbar-mode-pref-key
                                                                  (if compact?
                                                                    config/toolbar-full-mode-key
                                                                    config/toolbar-compact-mode-key))]
                                 (dispatch dispatch-vec)
                                 (error "Failed to save user-preference" {:pref-key config/toolbar-mode-pref-key})))
                             :offset {:top (if is-maximized?
                                             0
                                             (+ top-offset 48))}
                             :z-index 1
                             :extra-class [toolbar-ignore-class "window-toolbar-center-x" "animation-fade-in" "short-animation"]
                             :items (reduce (fn [acc group-items]
                                              (let [group-items (toolbar-group-items toolbar-state frame-id group-items coupled?)]
                                                (cond-> acc
                                                  (seq group-items)
                                                  (conj group-items))))
                                            []
                                            items)}
                      extra-props (assoc :extra-props extra-props))]]]]))))

(defn toolbar-comp [_]
  (let [toolbar-state {:result-state (r/atom nil)}]
    (r/create-class
     {:component-did-update (fn [this _]
                              (let [[_ {:keys [show?]}] (r/argv this)]
                                (when-not show?
                                  (update toolbar-state :result-state reset! nil))))
      :reagent-render (fn [props]
                        [error-boundary
                         [toolbar-impl toolbar-state props]])})))