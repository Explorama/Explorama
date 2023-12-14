(ns de.explorama.frontend.mosaic.views.legend
  (:require [clojure.string :as str :refer [join]]
            [data-format-lib.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button select]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [goog.string.format]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.frontend.mosaic.css :as gcss]
            [de.explorama.frontend.mosaic.data.graph-acs :refer [attr->display-name]]
            [de.explorama.frontend.mosaic.interaction.context-menu.shared :refer [aggregate-functions]]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::read-only?
 (fn [db [_ frame-id]]
   (let [current-mode
         (fi/call-api [:interaction-mode :current-db-get?] db {:frame-id frame-id})
         pending-read-only?
         (fi/call-api [:interaction-mode :pending-read-only-db-get?] db)]
     (or (= :read-only current-mode)
         pending-read-only?))))

(re-frame/reg-sub
 ::applied-filter
 (fn [db [_ frame-id]]
   (get-in db (gp/applied-filter frame-id))))

(re-frame/reg-event-fx
 ::toggle-legend
 (fn [{db :db} [_ frame-id active?]]
   (let [default-flag (fi/call-api :flags-db-get db frame-id :legend-default-open?)
         opened-and-closed? (= active? (not default-flag))]
     {:fx [(when opened-and-closed?
             [:dispatch (fi/call-api [:product-tour :next-event-vec]
                                     :mosaic :settings-info)])]})))

(def ^:private i18n-sub #(re-frame/subscribe [::i18n/translate %]))

(re-frame/reg-sub
 ::get-layout-details
 (fn [db [_ path]]
   (get-in db (gp/layout-details path))))

(re-frame/reg-sub
 ::selected-layouts
 (fn [db [_ path]]
   (get-in db (gp/selected-layouts path))))

(defn- layout-change-event [_ frame-id selected-layouts]
  {:dispatch [::tasks/execute-wrapper
              (gp/top-level frame-id)
              :layout
              {:layouts selected-layouts}]})

(re-frame/reg-event-fx
 ::change-layout
 (fn [{db :db} [_ frame-id selected-layouts]]
   (when (vector? selected-layouts)
     (layout-change-event db frame-id selected-layouts))))

(re-frame/reg-event-fx
 ::remove-layout
 (fn [{db :db} [_ frame-id layer-id]]
   (let [prev-selected (get-in db (gp/selected-layouts frame-id))
         selected (filterv #(not= layer-id %) prev-selected)]
     (layout-change-event db frame-id selected))))

(defn- swap-items [a-vec elem delta]
  (let [i (.indexOf a-vec elem)
        j (+ i delta)]
    (if (and (< -1 i)
             (< -1 j (count a-vec)))
      (assoc a-vec i (a-vec j) j (a-vec i))
      a-vec)))

(re-frame/reg-event-fx
 ::shift-layout
 (fn [{db :db} [_ frame-id layer-id delta]]
   (let [prev-selected (get-in db (gp/selected-layouts frame-id))
         selected (swap-items prev-selected layer-id delta)]
     (layout-change-event db frame-id selected))))

(re-frame/reg-event-db
 ::add-overlayer-active ;; TODO remove overlayer sutff, why is this here?
 (fn [db [_ frame-id active?]]
   (assoc-in db (gp/add-layout-active? frame-id) active?)))

(re-frame/reg-sub
 ::add-layout-active?
 (fn [db [_ frame-id]]
   (get-in db (gp/add-layout-active? frame-id) false)))

(re-frame/reg-sub
 ::selected-layout-ids
 (fn [db [_ frame-id]]
   (mapv :id (get-in db (gp/selected-layouts frame-id) []))))

(re-frame/reg-sub
 ::unused-layouts
 (fn [[_ frame-id]]
   (let [path (gp/top-level frame-id)]
     [(re-frame/subscribe [::gcss/raw-layouts])
      (re-frame/subscribe [::selected-layout-ids path])
      (re-frame/subscribe [::gcss/usable-layouts path])]))
 (fn [[raw-layouts selected-layer-ids usable-layouts]]
   (-> (apply dissoc raw-layouts selected-layer-ids)
       (select-keys usable-layouts)
       vals
       vec)))

(re-frame/reg-sub
 ::data-display-count
 (fn [db [_ frame-id]]
   (let [{:keys [local-count global-count]} (get-in db (gp/top-level frame-id))]
     {:all-data global-count
      :local-data local-count})))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (let [frame (:frame (get-in db (gp/container-path frame-id)))]
     {:years (:selected-years frame)
      :countries (:selected-countries frame)
      :datasources (:selected-datasources frame)
      :filtered-data-info (get-in db (gp/filtered-data-info frame-id))})))

(re-frame/reg-sub
 ::frame-datasource
 (fn [db [_ path]]
   (get-in db (gp/data-instance path))))

(re-frame/reg-sub
 ::simplified-di-desc
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::frame-datasource frame-id])
    (re-frame/subscribe [::di-desc frame-id])
    (re-frame/subscribe [::applied-filter frame-id])])
 (fn [[di-filter {:keys [filtered-data-info] :as di-desc} local-filter] _]
   (let [base (if (and local-filter (seq filtered-data-info))
                filtered-data-info
                di-desc)]
     {:base base
      :local-filter? (boolean local-filter)
      :additional (dflsv/simplified-filter di-filter local-filter)})))

(re-frame/reg-sub
 ::scatter-plot-ignored-events
 (fn [db [_ path]]
   (get-in db (gp/scatter-plot-ignored-events path))))

(defn- scatterplot-warning [path]
  (let [ignored @(re-frame/subscribe [::scatter-plot-ignored-events path])]
    (when (pos-int? ignored)
      [:p.legend__warning
       (str ignored @(i18n-sub :scatter-plot-missing-values))])))

(def ^:private axis-name-key
  {:x :x-axis-attribute-label
   :y :y-axis-attribute-label})

(def ^:private axis-placeholder-key
  {:x :scatter-plot-settings-placeholder-x-axis
   :y :scatter-plot-settings-placeholder-y-axis})

(re-frame/reg-sub
 ::scatter-axis-options
 (fn [[_ path] _]
   (re-frame/subscribe [:de.explorama.frontend.mosaic.data.graph-acs/scatter-axis path]))
 (fn [scatter-axis-options _]
   (mapv (fn [{label :name key :key}]
           {:label label
            :value key})
         (sort-by (comp str/lower-case :name)
                  scatter-axis-options))))

(re-frame/reg-sub
 ::scatter-axis-selected
 (fn [db [_ axis path]]
   (let [operations-desc (get-in db (gp/operation-desc path))
         value (get operations-desc axis)
         attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)]
     {:label (i18n/attribute-label attribute-labels value)
      :value value})))

(defn- scatterplot-axis [frame-id axis]
  (let [path (gp/top-level frame-id)
        axis-str (name axis)
        axis-name @(i18n-sub (axis-name-key axis))
        axis-placeholder @(i18n-sub (axis-placeholder-key axis))]
    [:div.explorama__form__select.explorama__form--flex
     [:label.explorama__form__label {:for (str "scatter-input-text-" axis-str)}
      axis-name]
     (with-meta
       [select {:placeholder axis-placeholder
                :is-multi? false
                :is-clearable? false
                :extra-class "input--w100"
                :autofocus? true
                :values (re-frame/subscribe [::scatter-axis-selected axis path])
                :options (re-frame/subscribe [::scatter-axis-options path])
                :disabled? @(re-frame/subscribe [::read-only? {:frame-id frame-id}])
                :on-change (fn [{value :value}]
                             (re-frame/dispatch [:de.explorama.frontend.mosaic.operations.tasks/execute-wrapper
                                                 frame-id :scatter-axis
                                                 {:path axis
                                                  :axis value}]))}]
       {:key (str path "legend-scatter-input-text-" axis-str)})]))

(defn- scatterplot-params [frame-id]
  [:div.subsection__control
   [scatterplot-axis frame-id gcp/scatter-x]
   [scatterplot-axis frame-id gcp/scatter-y]])

(defn- scatterplot-infos [{:keys [frame-id]}]
  (when (= gcp/render-mode-key-scatter
           (get @(re-frame/subscribe [:de.explorama.frontend.mosaic.operations.tasks/operations frame-id])
                gcp/render-mode-key))
    [:div.panel__subsection>div.subsection__content
     [scatterplot-warning frame-id]
     [scatterplot-params frame-id]]))

(re-frame/reg-sub
 ::sort-events
 (fn [db [_ path]]
   (get-in db (conj path :sort-data))))

(def aggregate-method-i18n-keys
  (reduce (fn [agg {:keys [name value]}] (assoc agg value name)) {} aggregate-functions))

(defn groups-sorted-by->display-name [{:keys [by attr method]} labels]
  (cond
    (nil? by) nil
    (= :aggregate by) (str
                       (-> attr name (attr->display-name labels))
                       " (" @(re-frame/subscribe [::i18n/translate (aggregate-method-i18n-keys method)]) ")")
    (= "layout" by) (attr->display-name "layout" labels)
    (= :event-count by) @(re-frame/subscribe [::i18n/translate :number-of-events])
    :else (name by)))

(defn operation->display-name [desc labels]
  (when (and desc (:by desc)) (-> (:by desc) name (attr->display-name labels))))

(defn operation->display-name-simple [desc labels]
  (when desc (-> desc name (attr->display-name labels))))

(defn- info-block [label attribute]
  [:div
   [:dt label]
   [:dd attribute]])

(defn operation-infos [{:keys [frame-id]}]
  (let [labels @(fi/call-api [:i18n :get-labels-sub])
        operation-infos @(re-frame/subscribe [::tasks/operations frame-id])
        grouped-by (operation->display-name-simple (get operation-infos gcp/grp-by-key) labels)
        grouped? (boolean grouped-by)
        sorted-by (operation->display-name (get operation-infos gcp/sort-key) labels)
        groups-sorted-by (groups-sorted-by->display-name (get operation-infos gcp/sort-grp-key) labels)
        subgrouped-by (operation->display-name-simple (get operation-infos gcp/sub-grp-by-key) labels)
        sub-grp-sorted-by (groups-sorted-by->display-name (get operation-infos gcp/sort-sub-grp-key) labels)
        {:keys [info-grouped-by
                info-sorted-by
                info-groups-sorted-by
                info-subgrouped-by
                info-treemap-type
                info-subgroups-sorted-by
                contextmenu-operations-group-label]}
        @(re-frame/subscribe
          [::i18n/translate-multi
           :info-grouped-by
           :info-sorted-by
           :info-groups-sorted-by
           :info-subgrouped-by
           :info-treemap-type
           :info-subgroups-sorted-by
           :contextmenu-operations-group-label])
        raster? (= (get operation-infos gcp/render-mode-key) gcp/render-mode-key-raster)
        tree? (= (get operation-infos gcp/render-mode-key) gcp/render-mode-key-treemap)]
    (when (or sorted-by grouped? subgrouped-by)
      [:div.panel__section
       [:div.section__content>div.panel__subsection
        [:div.section__title contextmenu-operations-group-label]
        [:div.subsection__content>dl
         (cond-> [:<>]
           (and sorted-by
                raster?)
           (conj [info-block info-sorted-by sorted-by])
           (and sorted-by
                raster?
                (or (and grouped? grouped-by)
                    subgrouped-by))
           (conj [:p])
           (and grouped? grouped-by (or raster? tree?))
           (conj [info-block info-grouped-by grouped-by]
                 (when groups-sorted-by
                   [info-block info-groups-sorted-by groups-sorted-by]))
           (and grouped? grouped-by subgrouped-by (or raster? tree?))
           (conj [:p])
           (and subgrouped-by
                (or raster? tree?))
           (conj [info-block info-subgrouped-by subgrouped-by]
                 (when (and sub-grp-sorted-by
                            raster?)
                   [info-block info-subgroups-sorted-by sub-grp-sorted-by]))
           tree?
           (conj [info-block
                  info-treemap-type
                  @(re-frame/subscribe [::i18n/translate (keyword (str "info-treemap-")
                                                                  (get operation-infos gcp/treemap-algorithm))])]))]]])))

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
        [scatterplot-infos props]]]]]))

(defn- general-information [{:keys [frame-id is-maximized? on-maximize on-normalize show-actions?] :as props}]
  (if-let [_scatter? (= gcp/render-mode-key-scatter
                        (get @(re-frame/subscribe [:de.explorama.frontend.mosaic.operations.tasks/operations frame-id])
                             gcp/render-mode-key))]
    (if is-maximized?
      [general-edit props]
      (let [general-label @(re-frame/subscribe [::i18n/translate :legend-general])
            x-axis @(re-frame/subscribe [::scatter-axis-selected gcp/scatter-x frame-id])
            y-axis @(re-frame/subscribe [::scatter-axis-selected gcp/scatter-y frame-id])
            {:keys [x-axis-attribute-label y-axis-attribute-label]}
            @(re-frame/subscribe
              [::i18n/translate-multi
               :x-axis-attribute-label :y-axis-attribute-label])]
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
         [:div.subsection__content>div.subsection__element>div.draggable__content>dl
          [general-elem true x-axis-attribute-label x-axis]
          [general-elem true y-axis-attribute-label y-axis]]]))
    (when (fn? on-normalize)
      (on-normalize))))

(defn layout-warnings [{:keys [frame-id]}]
  (let [fallback-layout? @(re-frame/subscribe [::gcss/fallback-layout? frame-id])
        {:keys [info-fallback-layout-hint]}
        @(re-frame/subscribe
          [::i18n/translate
           :info-fallback-layout-hint])]
    (when fallback-layout?
      [:div.panel__section.legend
       [:div.section__content>div.panel__subsection {:style {:padding "2px 10px"}}
        info-fallback-layout-hint]])))

#_; debugging purpose
  (defn highlights-view [{:keys [frame-id]}]
    (let [highlights (first @(fi/call-api :connection-data-for-frame-sub frame-id :selections :source-main))]
      [:div.panel__section.legend
       [:div.section__content>div.panel__subsection {:style {:padding "2px 10px"
                                                             :margin-top 12}}
        [:div.subsection__title "Highlights"]
        (str (:current highlights))]]))

(def legend-impl
  {:visible? true
   :disabled? (fn [_frame-id]
                false)
   :on-toggle-fn (fn [frame-id active?]
                   (re-frame/dispatch [::toggle-legend frame-id active?]))

   :data-acs-path
   (fn [frame-id]
     (gp/data-acs frame-id))

   :data-display-count
   (fn [frame-id]
     (re-frame/subscribe [::data-display-count frame-id]))

   :di-desc-sub
   (fn [frame-id]
     (re-frame/subscribe [::simplified-di-desc frame-id]))

   :coloring-info-subs
   (fn [frame-id]
     [{:title (re-frame/subscribe [::i18n/translate :layout-section-title])
       :coloring-sub (re-frame/subscribe [::selected-layouts frame-id])}])

   :configuration [{:module general-information}
                   {:module :layout-picker
                    :add-layout-options (fn [frame-id] (re-frame/subscribe [::unused-layouts frame-id]))
                    :selected-layouts (fn [frame-id] (re-frame/subscribe [::selected-layouts frame-id]))
                    :on-change (fn [frame-id selected-layouts] (re-frame/dispatch [::change-layout frame-id selected-layouts]))
                    :data-acs-path (fn [frame-id] (gp/data-acs frame-id))}
                   {:module layout-warnings}
                   {:module operation-infos}
                   #_{:module highlights-view}]})