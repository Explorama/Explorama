(ns de.explorama.frontend.map.views.legend
  (:require [clojure.string :as str :refer [join trim lower-case]]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.shared.data-format.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core
             :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core
             :refer [button checkbox select]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.frontend.ui-base.utils.select :refer [to-option]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.configs.overlayer.core :as overlayer-config]
            [de.explorama.frontend.map.configs.util :refer [translated-layer->raw]]
            [de.explorama.frontend.map.map.core :as map-core]
            [de.explorama.frontend.map.operations.tasks :as tasks]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.utils :refer [marker-layout-id->desc]]
            [goog.string.format]
            [re-frame.core :as re-frame]))

;; =============  Header  =============================================== 
(defn attr->display-name [attr]
  (let [labels @(fi/call-api [:i18n :get-labels-sub])]
    (or (get labels attr)
        (when-let [agg-label (get-in dfl-agg/descs [attr :label])]
          @(re-frame/subscribe [::i18n/translate agg-label]))
        attr)))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-di-desc frame-id))))

(re-frame/reg-event-db
 ::applied-filter
 (fn [db [_ frame-id filter]]
   (assoc-in db (geop/applied-filter frame-id) filter)))

(re-frame/reg-sub
 ::applied-filter
 (fn [db [_ frame-id]]
   (get-in db (geop/applied-filter frame-id))))

(re-frame/reg-sub
 ::simplified-di-desc
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::map-core/frame-datasource frame-id])
    (re-frame/subscribe [::di-desc frame-id])
    (re-frame/subscribe [::applied-filter frame-id])])
 (fn [[di-filter {:keys [filtered-data-infos] :as di-desc} local-filter] _]
   (let [base (if (and local-filter (seq filtered-data-infos))
                filtered-data-infos
                di-desc)]
     {:base base
      :local-filter? (boolean local-filter)
      :additional (dflsv/simplified-filter di-filter local-filter)})))

(re-frame/reg-sub
 ::data-display-count
 (fn [db [_ frame-id]]
   (let [global-data-count (get-in db (geop/frame-count-global frame-id))
         local-data-count (get-in db (geop/frame-count-local frame-id))
         displayable-data (get-in db (geop/frame-displayable-data frame-id))]
     {:all-data global-data-count
      :local-data local-data-count
      :displayed-data displayable-data})))

(re-frame/reg-sub
 ::read-only?
 (fn [db [_ frame-id]]
   (let [current-mode
         (fi/call-api [:interaction-mode :current-db-get?]
                      db
                      {:frame-id frame-id})
         pending-read-only?
         (fi/call-api [:interaction-mode :pending-read-only-db-get?] db)]
     (or (= :read-only current-mode)
         pending-read-only?))))

;; =============  Base Map  =============================================== 

(defn- as-option
  ([value]
   (as-option value value))
  ([value label]
   {:value value, :label label}))

(re-frame/reg-event-db
 ::book-base-layer
 (fn [db [_ frame-id layer-id]]
   (assoc-in db (geop/base-layer frame-id) layer-id)))

(re-frame/reg-sub
 ::base-layer-value
 (fn [db [_ frame-id]]
   (when-let [base-layer (get-in db (geop/base-layer frame-id))]
     (as-option base-layer))))

(re-frame/reg-sub
 ::base-layer-options
 (fn [_]
   (re-frame/subscribe [:de.explorama.frontend.map.core/layer-config]))
 (fn [layer-config _]
   (mapv (comp as-option :name) (:base-layers layer-config))))

(def ^:private i18n-sub #(re-frame/subscribe [::i18n/translate %]))

(defn- base-map-selector [{:keys [frame-id]}]
  ;; [:div.panel__subsection>div.subsection__content>div.subsection__control
  [select {:options (re-frame/subscribe [::base-layer-options])
           :values (re-frame/subscribe [::base-layer-value frame-id])
           :label (i18n-sub :base-map-select-label)
           :on-change #(re-frame/dispatch [::tasks/execute-wrapper
                                           frame-id
                                           :base-layer
                                           {:base-layer (:value %)}])
           :disabled? @(re-frame/subscribe [::read-only? frame-id])}])

;; =============   Shape Files / Overlays  =============================================== 

(re-frame/reg-sub
 ::overlay-values
 (fn [db [_ frame-id]]
   (let [selected (get-in db (geop/selected-overlayers frame-id) #{})
         overlayers (filter (fn [layer] (some #{(get layer :name)} selected))
                            (get-in db geop/overlayers))]
     (mapv #(as-option % (get % :name)) overlayers))))

(re-frame/reg-sub
 ::shape-options
 (fn [db [_ frame-id]]
   (let [selected (get-in db (geop/selected-overlayers frame-id) #{})
         overlayers (filter (fn [layer] (not (some #{(get layer :name)} selected)))
                            (get-in db geop/overlayers))]
     (mapv #(as-option % (get % :name)) overlayers))))

(defn- shape-file-selector [{:keys [frame-id]}]
  (let [options @(re-frame/subscribe [::shape-options frame-id])
        values @(re-frame/subscribe [::overlay-values frame-id])]
    (when (and options (or (seq options)
                           (seq values)))
      ;; [:div.panel__subsection>div.subsection__content>div.subsection__control
      [select {:options options
               :values (re-frame/subscribe [::overlay-values frame-id])
               :on-change (fn [v]
                            (re-frame/dispatch [::tasks/execute-wrapper
                                                frame-id
                                                :overlayer
                                                {:overlayers (set (mapv #(get-in % [:value :name]) v))}]))
               :label (i18n-sub :overlay-label)
               :disabled? @(re-frame/subscribe [::read-only? frame-id])
               :is-multi? true
               :is-clearable? true}])))

;; =============  Marker Settings  =============================================== 

(re-frame/reg-sub
 ::cluster?
 (fn [db [_ frame-id]]
   (get-in db (geop/cluster-marker? frame-id))))

(defn- cluster-toggle [{:keys [frame-id]}]
  (let [local-events @(re-frame/subscribe [:de.explorama.frontend.map.views.frame-header/event-count frame-id])
        too-many-events (> local-events config/marker-cluster-threshold)
        cluster-marker-label @(re-frame/subscribe [::i18n/translate :cluster-marker-label])
        read-only? @(re-frame/subscribe [::read-only? frame-id])
        too-many-events-label @(re-frame/subscribe [::i18n/translate :disabled-cluster-checkbox-hint])
        cluster? @(re-frame/subscribe [::cluster? frame-id])]
    [:div.flex
     [tooltip {:text (when (and too-many-events (not read-only?))
                       (format too-many-events-label (i18n/localized-number config/marker-cluster-threshold)))}
      [checkbox
       {:checked? (if too-many-events
                    true
                    cluster?)
        :on-change #(re-frame/dispatch [::tasks/execute-wrapper
                                        frame-id
                                        :cluster-switch
                                        {:cluster? (not cluster?)}])
        :box-position :right
        :disabled? (or read-only? too-many-events)
        :label cluster-marker-label}]]]))

(defn- marker-settings [frame-id]
  [:div.panel__subsection>div.subsection__content>div.subsection__control
   [cluster-toggle frame-id]])

;; =============  Marker Layer  =============================================== 

(re-frame/reg-sub
 ::marker-layer-options
 (fn [db [_ frame-id]]
   (let [usable-marker-layouts-id (get-in db (geop/usable-marker-layouts-id frame-id))
         selected-ids (set (map :id (get-in db (geop/selected-marker-layouts frame-id))))]
     (mapv (partial marker-layout-id->desc db frame-id)
           (filter #(not (selected-ids %))
                   usable-marker-layouts-id)))))

(re-frame/reg-sub
 ::marker-layer-values
 (fn [db [_ frame-id]]
   (let [temp-layouts (get-in db (geop/temp-raw-marker-layouts frame-id))
         layouts (get-in db geop/raw-marker-layouts)
         selected-layouts (get-in db (geop/selected-marker-layouts frame-id))]
     (mapv (fn [{:keys [id] :as v}]
             (cond
               (contains? temp-layouts id) (get temp-layouts id)
               (contains? layouts id) (get layouts id)
               :else (translated-layer->raw v)))
           selected-layouts))))

;; =============  Overlayer  =============================================== 

(re-frame/reg-sub
 ::shown-overlayers
 (fn [db [_ frame-id]]
   (let [overlayers (merge (get-in db geop/raw-feature-layers)
                           (get-in db (geop/temp-raw-feature-layers frame-id)))
         selected (get-in db (geop/selected-feature-layers frame-id))]
     (mapv (fn [[k v]]
             (get overlayers k
                  (translated-layer->raw v)))
           selected))))

(def ^:private i18n-layer-type-key
  {:country :designer-layer-type-feature
   :feature :designer-layer-type-feature
   :heatmap :designer-layer-type-heatmap
   :movement :designer-layer-type-movement})

(defn- overlay-options [frame-id options-fn]
  (mapv (fn [{:keys [name id]}]
          (to-option id name))
        (sort-by #(lower-case (trim (:name %)))
                 (val-or-deref (options-fn frame-id)))))

(defn- overlayer-title [{:keys [frame-id on-maximize show-actions?] :as props} layer]
  (let [id (:id layer)
        i18n-key (-> layer :type i18n-layer-type-key)
        layer-name (if (:temporary? layer)
                     @(re-frame/subscribe [::i18n/translate :temporary-overlay-name])
                     (:name layer))
        type-name (when (keyword? i18n-key) @(i18n-sub i18n-key))
        read-only? @(re-frame/subscribe [::read-only? frame-id])]
    [:div.subsection__element__title
     [:div.truncate-text
      [:span.layer__name layer-name]
      [:span.layer__type (when type-name
                           (str " (" type-name ")"))]]
     (when show-actions?
       [:div.flex
        [button {:start-icon :edit
                 :label (re-frame/subscribe [::i18n/translate :edit-label])
                 :aria-label :edit-label
                 :variant :tertiary
                 :size :small
                 :on-click #(on-maximize layer)
                 :disabled? read-only?}]
        [button {:start-icon :close
                 :aria-label :close
                 :variant :tertiary
                 :size :small
                 :on-click #(re-frame/dispatch [::tasks/execute-wrapper
                                                frame-id
                                                :hide-feature-layer
                                                {:feature-layer id}])
                 :disabled? read-only?}]])]))

(re-frame/reg-sub
 ::add-overlayer-options
 (fn [db [_ frame-id]]
   (let [usable-feature-layouts-id (get-in db (geop/usable-feature-layouts-id frame-id))
         overlayers (get-in db geop/raw-feature-layers)
         selected (set (keys (get-in db (geop/selected-feature-layers frame-id))))]
     (->> overlayers
          (filterv (fn [[k _]]
                     (and (not (selected k))
                          (some #{k} usable-feature-layouts-id))))
          (mapv (fn [[_ v]] v))))))

(re-frame/reg-event-db
 ::add-overlayer-active
 (fn [db [_ frame-id active?]]
   (assoc-in db (geop/add-overlayer-active? frame-id) active?)))

(re-frame/reg-sub
 ::add-overlayer-active?
 (fn [db [_ frame-id]]
   (get-in db (geop/add-overlayer-active? frame-id) false)))

(defn- add-overlayer [{:keys [add-overlayer-options frame-id
                              show-actions?]}]
  (when show-actions?
    [:div.add-layout
     [button
      {:on-click #(re-frame/dispatch [::add-overlayer-active frame-id true])
       :disabled? @(re-frame/subscribe [::read-only? frame-id])
       :label @(i18n-sub :add-overlayer-button-text)
       :start-icon :plus
       :size :big
       :variant :secondary}]
     (when @(re-frame/subscribe [::add-overlayer-active? frame-id])
       [:div.menu__overlay--map
        (let [close-select #(re-frame/dispatch [::add-overlayer-active frame-id false])]
          [select {:autofocus? true
                   :on-blur close-select
                   :disabled? @(re-frame/subscribe [::read-only? frame-id])
                   :placeholder @(i18n-sub :add-overlayer-button-text)
                   :options (overlay-options frame-id add-overlayer-options)
                   :is-multi? false
                   :is-clearable? false
                   :on-change #(re-frame/dispatch [::tasks/execute-wrapper
                                                   frame-id
                                                   :feature-layer
                                                   {:feature-layer-id (:value %)}])
                   :values []
                   :show-options-tooltip? false}])])]))

(defmulti ^:private overlayer-content (comp keyword :layer-type))


(defn plus-minus-infinity-or-localized [v]
  (let [{:keys [legend-negative-infinity legend-infinity]}
        @(re-frame/subscribe [::i18n/translate-multi :legend-negative-infinity :legend-infinity])]
    (cond (= ##-Inf v)
          legend-negative-infinity
          (= ##Inf v)
          legend-infinity
          :else
          (i18n/localized-number v))))

(defn- loc-num [attributes value]
  (cond-> value
    (and (number? value)
         (not (some #{"year"} attributes)))
    (plus-minus-infinity-or-localized)))

(defn- calc-color-legend [attributes type assignment color]
  {:color color
   :value (cond
            (= type "number") (str (loc-num attributes (first assignment))
                                   " - "
                                   (loc-num attributes (last assignment)))
            (coll? assignment) (apply str (interpose ", " assignment))
            :else assignment)})

(defn- to-map [assignment] (into {} (mapv #(vector (-> %1 str keyword) %2) (range (count assignment)) assignment)))

(defn- info-block [label attribute]
  [:div
   [:dt label]
   [:dd.truncate-text attribute]])

(defn- color-code-list [layer]
  (when-let [attributes (:attributes layer)]
    (let [{need-attribute? :need-attribute?
           method-label :label} (-> layer
                                    :aggregate-method-name
                                    keyword
                                    dfl-agg/descs)
          attributes (if (or (nil? need-attribute?) need-attribute?)
                       attributes
                       [@(re-frame/subscribe [::i18n/translate method-label])])]
      [:<>
       [:dl
        [info-block
         @(i18n-sub :designer-color-coding-attribute)
         (str/join ", " (map attr->display-name attributes))]]
       (into [:ul]
             (comp (map val)
                   (remove (comp str/blank? :value))
                   (map (fn [{:keys [color value]}]
                          [:li
                           [:span.legend__color
                            {:style {:background-color color}}]
                           [:span.legend__value
                            value]])))
             (sort-by key
                      (merge-with (partial calc-color-legend attributes (get layer :attribute-type))
                                  (to-map (get layer :value-assigned))
                                  (get-in layer [:color-scheme :colors]))))])))

(defmethod overlayer-content :country [layer]
  [color-code-list layer])

(defmethod overlayer-content :feature [layer]
  [color-code-list layer])

(def ^:private i18n-heatmap-extrema-key
  {:global :designer-layer-heatmap-point-density
   :local :designer-layer-heatmap-weighted})

(defmethod overlayer-content :heatmap [layer]
  (let [extrema-lang-key (-> layer :extrema i18n-heatmap-extrema-key)
        {:keys [designer-color-coding-attribute
                designer-layer-heatmap-drop-down-type]
         extrema-translation extrema-lang-key}
        @(re-frame/subscribe [::i18n/translate-multi
                              :designer-color-coding-attribute
                              :designer-layer-heatmap-drop-down-type
                              extrema-lang-key])]
    [:dl
     [info-block designer-layer-heatmap-drop-down-type extrema-translation]
     (when-let [attr (:attributes layer)]
       [info-block designer-color-coding-attribute (str/join ", " (map attr->display-name attr))])]))

(defmethod overlayer-content :movement [layer]
  (let [{:keys [designer-color-coding-attribute
                designer-layer-movement-source
                designer-layer-movement-target]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :designer-color-coding-attribute
                              :designer-layer-movement-source
                              :designer-layer-movement-target])]
    [:dl
     [info-block designer-color-coding-attribute (str/join ", " (map attr->display-name (:attributes layer)))]
     [info-block designer-layer-movement-source (attr->display-name (:source layer))]
     [info-block designer-layer-movement-target (attr->display-name (:target layer))]]))

(defn- overlayer [{:keys [frame-id] :as props} layer]
  (let [id (str frame-id "/legend/overlayer/" (:id layer))]
    [:div.subsection__element {:id id}
     [overlayer-title props layer]
     [overlayer-content layer]]))

(defn- overlayers [{:keys [frame-id show-actions? on-maximize]
                    :as props}]
  (let [shown @(re-frame/subscribe [::shown-overlayers frame-id])
        elem-fn (partial overlayer (assoc props :show-actions? false))]
    [:div.panel__subsection.layouts
     [:div.subsection__title
      "Overlayers"
      (when (val-or-deref show-actions?)
        [button {:start-icon :edit
                 :label (re-frame/subscribe [::i18n/translate :edit-label])
                 :aria-label :edit-label
                 :variant :tertiary
                 :size :small
                 :extra-class export-ignore-class
                 :on-click #(on-maximize {:overview? true})}])]
     (into [:div.subsection__content]
           (map elem-fn)
           (sort-by #(lower-case (trim (:name %)))
                    shown))]))

(defn- overlayers-edit-overview [{:keys [frame-id on-normalize]
                                  :as props}]
  (let [shown @(re-frame/subscribe [::shown-overlayers frame-id])
        elem-fn (partial overlayer props)]
    [:div.panel__section
     [:div.panel__header
      [button {:on-click #(on-normalize)
               :label @(re-frame/subscribe [::i18n/translate :back-label])
               :start-icon :previous
               :size :big
               :variant :back}]]
     [:div.section__title
      "Overlayers"]
     [:div.section__content>div.panel__subsection>div.subsection__content
      (into [:<>]
            (map elem-fn)
            (sort-by #(lower-case (trim (:name %)))
                     shown))
      [add-overlayer props]]]))

(defn overlay-picker [{:keys [maximized-data] :as props}]
  (let [{edit-view :component} @(fi/call-api :service-target-sub :config-module :legend-edit)
        {:keys [overview?] :as maximized-data} (val-or-deref maximized-data)]
    [error-boundary
     (cond
       overview? [overlayers-edit-overview props]
       maximized-data [edit-view props overlayer-config/view]
       :else [overlayers props])]))

(defn- general-elem [show? label {val :label :as selection}]
  (let [multi-vals? (and (not val)
                         (coll? selection))]
    (when (and show? label (or val multi-vals?))
      [:div
       [:dt.legend__label label]
       [:dd
        (when val val)
        (when multi-vals?
          (join ", " (map :label selection)))]])))

(defn- general-display [{:keys [frame-id]}]
  (let [base-map @(re-frame/subscribe [::base-layer-value frame-id])
        shapes-values @(re-frame/subscribe [::overlay-values frame-id])
        show-shapes? (seq shapes-values)
        {:keys [overlay-label base-map-select-label]}
        @(re-frame/subscribe [::i18n/translate-multi :overlay-label :base-map-select-label])]
    [:div.subsection__content>div.subsection__element>div.draggable__content>dl
     [general-elem true base-map-select-label base-map]
     [general-elem show-shapes? overlay-label
      (if (= 1 (count shapes-values))
        (first shapes-values)
        shapes-values)]]))

(defn- general-edit [{:keys [on-normalize] :as props}]
  (let [general-label @(re-frame/subscribe [::i18n/translate :legend-general])]
    [:div.panel__section
     [:div.panel__header
      [button {:on-click #(on-normalize)
               :label @(re-frame/subscribe [::i18n/translate :back-label])
               :start-icon :previous
               :size :big
               :variant :back}]]
     [:div.section__title
      general-label]
     [:div.panel__scroll__container
      [:div.section__content>div.panel__subsection>div.subsection__content {:style {:position :relative}}
       [:div.subsection__control
        [base-map-selector props]
        [shape-file-selector props]
        [cluster-toggle props]]]]]))

(defn- general-information [{:keys [frame-id is-maximized? on-maximize show-actions?] :as props}]
  (if is-maximized?
    [general-edit props]
    (let [general-label @(re-frame/subscribe [::i18n/translate :legend-general])]
      [:div.panel__subsection.draggable-content
       [:div.subsection__title
        general-label
        (when (val-or-deref show-actions?)
          [button {:start-icon :edit
                   :label (re-frame/subscribe [::i18n/translate :edit-label])
                   :aria-label :edit-label
                   :variant :tertiary
                   :size :small
                   :extra-class export-ignore-class
                   :on-click #(on-maximize {})}])]
       [general-display props]])))

;; =============  Legend  =============================================== 

(re-frame/reg-sub
 ::too-much-data?
 (fn [db [_ frame-id]]
   (get-in db (geop/too-much-data? frame-id) false)))

(def legend-impl
  {:visible? true
   :disabled? (fn [frame-id]
                false)

   :di-desc-sub
   (fn [frame-id]
     (re-frame/subscribe [::simplified-di-desc frame-id]))

   :data-display-count
   (fn [frame-id]
     (re-frame/subscribe [::data-display-count frame-id]))

   :display-tooltip-info
   (fn []
     (re-frame/subscribe [::i18n/translate :marker-not-displayable-tooltip]))

   :coloring-info-subs
   (fn [frame-id]
     [{:title (re-frame/subscribe [::i18n/translate :marker-section-title])
       :coloring-sub (re-frame/subscribe [::marker-layer-values frame-id])}
      {:title (re-frame/subscribe [::i18n/translate :overlayer-section-title])
       :coloring-sub (re-frame/subscribe [::shown-overlayers frame-id])}])

   :configuration [{:module general-information}
                   {:module :layout-picker
                    :disabled? (fn [frame-id]
                                 (re-frame/subscribe [::too-much-data? frame-id]))
                    :add-layout-options (fn [frame-id] (re-frame/subscribe [::marker-layer-options frame-id]))
                    :selected-layouts (fn [frame-id] (re-frame/subscribe [::marker-layer-values frame-id]))
                    :on-change (fn [frame-id selected-layouts]
                                 (re-frame/dispatch [::tasks/execute-wrapper
                                                     frame-id
                                                     :marker
                                                     {:marker-layouts selected-layouts}]))
                    :data-acs-path (fn [frame-id] (conj (geop/frame-filter frame-id) :data-acs))}
                   {:module overlay-picker
                    :add-overlayer-options (fn [frame-id] (re-frame/subscribe [::add-overlayer-options frame-id]))
                    :selected-layouts (fn [frame-id] (re-frame/subscribe [::shown-overlayers frame-id]))
                    :on-change (fn [frame-id selected-layouts]
                                 (re-frame/dispatch [::tasks/execute-wrapper
                                                     frame-id
                                                     :feature-layer
                                                     {:feature-layers selected-layouts}]))
                    :data-acs-path (fn [frame-id] (conj (geop/frame-filter frame-id) :data-acs))}]})
