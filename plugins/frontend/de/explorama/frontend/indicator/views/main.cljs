(ns de.explorama.frontend.indicator.views.main
  "Defines the main part where a selected Indicator can be defined."
  (:require [clojure.string :as str]
            [data-format-lib.dates :as dfdates]
            [data-format-lib.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                   input-field select
                                                                   textarea]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.select :as select-util]
            [de.explorama.frontend.indicator.components.dialog :as dialog]
            [de.explorama.frontend.indicator.components.direct-visualization :refer [direct-visualization]]
            [de.explorama.frontend.indicator.views.elements.component :refer [comp-wrapper]]
            [de.explorama.frontend.indicator.views.elements.select-aggregation :as select-aggregation]
            [de.explorama.frontend.indicator.views.management :as management]
            [de.explorama.frontend.indicator.views.result-preview :as result-preview]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defn- i18n-translate [label]
  (if (keyword? label)
    @(re-frame/subscribe [::i18n/translate label])
    (str label)))

(defn- addon-row-header [label]
  (let [label (i18n-translate label)]
    [:label.explorama__form__label.input--w14
     label]))

(defn- value-in-options? [options value]
  (let [option-set (reduce
                    (fn [acc option]
                      (if-let [sub-options (get option :options)]
                        (into acc (mapv :value sub-options))
                        (conj acc (get option :value))))
                    #{}
                    options)]
    (or (nil? value)
        (contains? option-set (get value :value)))))

(defn- addon-row- [indicator-id idx {:keys [id type content depends-on]}]
  (let [addon-row @(re-frame/subscribe [::management/indicator-addon-row-value indicator-id idx id])
        options (case content
                  :all-attributes
                  @(re-frame/subscribe [::management/additional-attributes indicator-id nil])
                  :aggregations
                  @(re-frame/subscribe [::select-aggregation/aggregation-options indicator-id depends-on idx])
                  :calc-attributes
                  @(re-frame/subscribe [::management/calc-attributes indicator-id nil]))
        is-grouped? (not= content :aggregations)]
    (when-not (value-in-options? options addon-row)
      (re-frame/dispatch [::management/indicator-addon-row-value indicator-id idx id nil]))
    (case type
      :select
      [select
       {:is-clearable? false
        :options options
        :is-grouped? is-grouped?
        :group-value-key :di
        :extra-class "input--w14"
        :values addon-row
        :mark-invalid? true
        :on-change (fn [e]
                     (re-frame/dispatch [::management/indicator-addon-row-value indicator-id idx id e]))}])))

(defn- addon-ctn-rows [indicator-id comps-template]
  (let [added-rows @(re-frame/subscribe [::management/indicator-addon-rows indicator-id])]
    (into [:<>]
          (for [[idx _] (map-indexed vector added-rows)]
            (with-meta
              [:div.explorama__form__select.explorama__form__input.mb-0
               (for [{id :id :as comp-template} comps-template]
                 (with-meta
                   [addon-row- indicator-id idx comp-template]
                   {:key (str "additional-attrs-added-row-" idx "-" id)}))
               [button {:start-icon :trash
                        :aria-label :delete-label
                        :variant :secondary
                        :on-click #(re-frame/dispatch [::management/indicator-addon-remove-row indicator-id idx])}]]
              {:key (str "additional-attrs-added-row-" idx)})))))

(defn- addon-row [indicator-id {:keys [label]
                                comps-template :comps}]
  (let [label (if (keyword? label)
                @(re-frame/subscribe [::i18n/translate label])
                (str label))
        hint-label  @(re-frame/subscribe [::i18n/translate :indicator-addon-row-hint])]
    [:div.indicator__addon.row
     [:div.indicator__section__title label
      [icon {:icon :info-circle
             :tooltip hint-label}]]
     [:div.label__header
      (for [{:keys [id label]} comps-template]
        (with-meta
          [addon-row-header label]
          {:key (str "additional-attrs-added-row-header-" id)}))]
     [addon-ctn-rows indicator-id comps-template]
     [button {:start-icon :plus
              :aria-label :aria-add-attribute
              :variant :secondary
              :on-click #(re-frame/dispatch [::management/indicator-addon-add-row indicator-id])}]]))

(defn- col-x-row [indicator-id col-class comps]
  (reduce (fn [parent comp-desc]
            (conj parent
                  [comp-wrapper indicator-id comp-desc]))
          [:div {:class col-class}]
          comps))

(defn- definition-row [indicator-id row-desc]
  (let [{:keys [label comps]
         comp-partition :partition} row-desc
        label (if (keyword? label)
                @(re-frame/subscribe [::i18n/translate label])
                (str label))
        partitioned? (not (nil? comp-partition))
        partition-size (if partitioned?
                         comp-partition
                         (count comps))
        partitioned-comp (partition-all partition-size
                                        comps)
        col-class (if partitioned?
                    "col-6"
                    "col-12")]
    (reduce (fn [parent part-comps]
              (conj parent
                    [col-x-row indicator-id col-class part-comps]))
            [:<>
             [:div.indicator__section__title label]]
            partitioned-comp)))

(defn- definition-rows [indicator-id description]
  [:div.indicator__definition.row
   (for [{:keys [label] :as row} description]
     (with-meta
       [definition-row indicator-id row]
       {:key (str "indicator-definition-" label)}))])

(defn- loc-num [attribute value]
  (cond-> value
    (and (number? value)
         (not (#{::dfdates/year ::dfdates/month}
               attribute)))
    (i18n/localized-number)))

(defn- info-block
  ([label attribute tooltip parent-class]
   (let [label (cond
                 (= label ::dfdates/full-date) "date"
                 (= label ::dfdates/year) "year"
                 (= label ::dfdates/month) "month"
                 :else label)]
     [:div {:class parent-class}
      [:dt (i18n/attribute-label label)]
      [:dd (cond-> {}
             tooltip
             (assoc :title tooltip))
       attribute]]))
  ([label attribute tooltip]
   [info-block label attribute tooltip nil])
  ([label attribute]
   [info-block label attribute (i18n/attribute-label attribute)]))

(defn- more-data-row [{:data-format-lib.filter/keys [prop value]
                       op ::dflsv/op}]
  (let [lang @(re-frame/subscribe [::i18n/current-language])
        value (if (= prop :data-format-lib.dates/month)
                (if (vector? value)
                  (mapv #(i18n/month-name % lang) value)
                  (i18n/month-name value lang))
                value)
        {:keys [legend-range-separator
                legend-not-operator]}
        @(re-frame/subscribe [::i18n/translate-multi
                          :legend-range-separator
                          :legend-not-operator])
        to legend-range-separator
        v (case op
            :in (str/join "," value)
            (:in-range :not-in-range) (str
                                       (loc-num prop (first value))
                                       to
                                       (loc-num prop (second value)))
            (str (loc-num prop value)))
        o (case op
            (:in := :in-range) nil
            (:not= :not-in-range) legend-not-operator
            (name op))
        text (if o (str " " v) v)]

    [info-block
     prop
     [:<>
      (when o [:span.operator o])
      text]
     (str (or o "")
          text)]))

(defn- dataset-description [{:keys [title di indicator-id]
                             {[years] :years
                              [countries] :countries
                              [datasources] :datasources} :di-desc
                             :as dataset}]
  (let [{incomplete-desc? :incomplete?
         di-filter-primitives :di-filter-primitives} (dflsv/simplified-filter di nil)
        legend-icomplete-filter-explanation @(re-frame/subscribe [::i18n/translate :legend-icomplete-filter-explanation])]
    [:div.card {:title (str datasources "," years "," countries)}
     [:div {:class ["flex"
                    "align-items-center"
                    "gap-4"
                    "-mt-4"
                    "-mr-4"
                    "mb-4"]}
      [icon {:icon :database}]
      [:span {:class "text-bold"} title]
      [button {:start-icon :close
               :variant :tertiary
               :size :small
               :extra-class "ml-auto"
               :on-click (fn []
                           (re-frame/dispatch [::management/remove-dataset indicator-id di]))}]]
     [:dl.data-desc-list
      [info-block "datasource" datasources]
      [info-block "year" years]
      [info-block "country" countries]
      (when (seq di-filter-primitives)
        (map (fn [p] [more-data-row p]) di-filter-primitives))]
     (when incomplete-desc?
       [:div.data__hint
        [icon {:icon :info-circle}]
        legend-icomplete-filter-explanation])]))

(defn- dataset-container [indicator-id]
  (let [loaded-datasets @(re-frame/subscribe [::management/datasets-descriptions indicator-id])]
    [:div.dataset__container
     (for [dataset loaded-datasets]
       (with-meta
         [dataset-description dataset]
         {:key (str "indicator-dataset-desc-" (get dataset :di))}))]))

(def indicator-drop-area-id (str ::drop-area))

(defn- handle-drop [drag-entered-state _ _]
  (reset! drag-entered-state false))

(defn- handle-drag-enter [drag-entered-state _ _]
  (reset! drag-entered-state true))

(defn- handle-drag-leave [drag-entered-state _ _]
  (reset! drag-entered-state false))

(re-frame/reg-event-fx
 ::register-drop-handler
 (fn [{db :db} [_ drag-entered-state frame-id]]
   (when-let [service-register-db-update (fi/api-definition :service-register-db-update)]
     {:db (-> db
              (service-register-db-update :frame-drop-hitbox
                                          ::hitboxes
                                          {:dom-ids [indicator-drop-area-id]
                                           :id frame-id
                                           :global-context? true
                                           :default-connect? true
                                           :on-drag-enter (partial handle-drag-enter drag-entered-state)
                                           :on-drag-leave (partial handle-drag-leave drag-entered-state)
                                           :on-drop (partial handle-drop drag-entered-state)}))})))

(re-frame/reg-event-fx
 ::unregister-drop-handler
 (fn [{db :db}]
   (when-let [service-deregister-db-update (fi/api-definition :service-deregister-db-update)]
     {:db (-> db
              (service-deregister-db-update :frame-drop-hitbox
                                            ::hitboxes))})))

(defn- drop-area [frame-id _ _]
  (let [drag-entered-state (reagent/atom false)]
    (reagent/create-class
     {:component-did-mount #(re-frame/dispatch [::register-drop-handler drag-entered-state frame-id])
      :component-will-unmount #(re-frame/dispatch [::unregister-drop-handler])
      :reagent-render
      (fn [_ indicator-id _]
        (let [drop-area-hint @(re-frame/subscribe [::i18n/translate :drop-area-text])
              no-datasets? (empty? @(re-frame/subscribe [::management/datasets-descriptions indicator-id]))]
          [:div {:id indicator-drop-area-id
                 :class ["drag-drop-area"
                         (when no-datasets?
                           "drag-drop-area--empty")
                         (when @drag-entered-state
                           "drop-target")]}
           (when no-datasets? [:span drop-area-hint])
           [dataset-container indicator-id]]))})))

(defn- type-comp [indicator-id]
  (let [indicator-type @(re-frame/subscribe [::management/indicator-prop
                                         indicator-id
                                         :indicator-type])
        drop-down-label @(re-frame/subscribe [::i18n/translate :indicator-type])
        hint-label @(re-frame/subscribe [::i18n/translate :indicator-type-hint])
        template-options @(re-frame/subscribe [::management/indicator-template-options])
        loaded-datasets @(re-frame/subscribe [::management/datasets-descriptions indicator-id])]
    [select
     {:label drop-down-label
      :is-clearable? false
      :hint hint-label
      :disabled? (empty? loaded-datasets)
      :options template-options
      :extra-class "input--w14"
      :values (select-util/selected-option :value template-options indicator-type)
      :on-change (fn [{:keys [value]}]
                   (re-frame/dispatch [::management/update-indiactor-type
                                   indicator-id value]))}]))

(defn- type-info-comp [indicator-id]
  (let [type-info @(re-frame/subscribe [::management/indicator-type-info indicator-id])
        info-str (if (keyword? type-info)
                   @(re-frame/subscribe [::i18n/translate type-info])
                   (str type-info))]
    (when type-info
      [:p info-str])))

(defn- basic-row [frame-id indicator-id drop-area-props]
  (let [{desc-label :indicator-desc
         desc-placeholder :indicator-desc-placeholder
         name-label :indicator-name}
        @(re-frame/subscribe [::i18n/translate-multi :indicator-desc :indicator-desc-placeholder :indicator-name])
        {:keys [name description]} @(re-frame/subscribe [::management/indicator-props
                                                     indicator-id
                                                     [:name :description]])]
    [:div.indicator__basics.row
     [:div.col-12
      [:h1 name]
      [input-field {:label name-label
                    :extra-class "input--w14"
                    :value name
                    :max-length 25
                    :on-change (fn [val]
                                 (re-frame/dispatch [::management/update-indicator-prop
                                                 indicator-id :name val]))}]
      [textarea {:label desc-label
                 :extra-class "input--w14"
                 :value (or description "")
                 :placeholder desc-placeholder
                 :max-length 255
                 :on-change (fn [val]
                              (re-frame/dispatch [::management/update-indicator-prop
                                              indicator-id :description val]))}]
      [drop-area frame-id indicator-id drop-area-props]
      [type-comp indicator-id]
      [type-info-comp indicator-id]]]))

(defn- translate-ui-desc [indicator-id]
  (let [{:keys [additional-attributes]
         definition-rows-desc :definition-rows
         :as ui-desc-template}
        @(re-frame/subscribe [::management/indicator-template-ui-desc indicator-id])]
    [:<>
     (when (seq definition-rows-desc)
       [definition-rows indicator-id definition-rows-desc])
     (when (seq additional-attributes)
       [addon-row indicator-id additional-attributes])]))

(defn- editor-view [frame-id indicator-id drop-area-props]
  (let [is-changed? @(re-frame/subscribe [::management/changed? indicator-id true])
        indicator-type @(re-frame/subscribe [::management/indicator-prop
                                         indicator-id
                                         :indicator-type])
        loaded-datasets @(re-frame/subscribe [::management/datasets-descriptions indicator-id])]
    [:div.wrapper__main
     [basic-row frame-id indicator-id drop-area-props]
     (when (and indicator-type
                (not-empty loaded-datasets))
       [:<>
        [translate-ui-desc indicator-id]
        [result-preview/view indicator-id]])]))

(defn- actions [indicator-id project?]
  (let [new-indicator-id @(re-frame/subscribe [::management/new-indicator-id])
        is-new? (= new-indicator-id indicator-id)
        is-changed? @(re-frame/subscribe [::management/changed? indicator-id])
        {is-valid? :valid?
         non-valid-reasons :reasons} @(re-frame/subscribe [::management/valid? indicator-id])
        reasons-msg (->> non-valid-reasons
                         (filter identity)
                         (map #(deref (re-frame/subscribe %)))
                         (str/join "\n"))]
    [:div.settings__actions
     (when-not project?
       [:<>
        [button (cond-> {:start-icon :save
                         :extra-style {:pointer-events :auto}
                         :label (re-frame/subscribe [::i18n/translate :indicator-save])
                         :disabled? (or (not is-valid?)
                                        (not is-changed?))
                         :on-click #(re-frame/dispatch [::management/save-indicator indicator-id])}
                  (not is-valid?)
                  (assoc :title reasons-msg))]
        [button {:start-icon :cross
                 :variant :secondary
                 :disabled? (or is-new?
                                (not is-changed?))
                 :label (re-frame/subscribe [::i18n/translate :indicator-discard-changes])
                 :on-click #(re-frame/dispatch [::management/discard-changes indicator-id])}]
        #_[button {:start-icon :trash
                   :variant :secondary
                   :label (re-frame/subscribe [::i18n/translate :delete-tooltip-title])
                   :on-click #(re-frame/dispatch [::dialog/set-show "delete" indicator-id true])}]])
     [direct-visualization indicator-id project? (or (not is-valid?)
                                                     is-new?
                                                     is-changed?)]]))

(defn view [frame-id drop-area-props]
  (let [{current-indicator-id :id
         project? :project?} @(re-frame/subscribe [::management/active-indicator])
        new-indicator-id @(re-frame/subscribe [::management/new-indicator-id])]
    (when current-indicator-id
      [:<>
       [button {:start-icon :previous
                :variant :back
                :label (re-frame/subscribe [::i18n/translate :back-to-overview-label])
                :on-click (fn [e]
                            (let [is-changed? @(re-frame/subscribe [::management/changed? current-indicator-id false])
                                  shift? (aget e "shiftKey")
                                  is-new? (= new-indicator-id current-indicator-id)]
                              (cond
                                is-changed? (re-frame/dispatch [::dialog/set-show "back-confirm" new-indicator-id true])
                                :else (re-frame/dispatch [::management/change-active-indicator nil]))))}]
       [:div.indicator__wrapper
        [editor-view frame-id current-indicator-id drop-area-props]]
       [actions current-indicator-id project?]])))
