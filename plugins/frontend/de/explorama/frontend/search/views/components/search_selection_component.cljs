(ns de.explorama.frontend.search.views.components.search-selection-component
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :refer [chip]]
            [de.explorama.frontend.ui-base.utils.select :refer [to-option]]
            [re-frame.core :as re-frame :refer [dispatch subscribe]]
            [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.data.acs :as acs]
            [de.explorama.frontend.search.data.topics :as topics :refer [is-topic-attr-desc?
                                                                         topic-attr-desc topic-values]]
            [de.explorama.frontend.search.views.components.advanced-mode-components :as adv-comps]
            [de.explorama.frontend.search.views.components.elements :as elements]
            [de.explorama.frontend.search.views.components.location :as loc]
            [de.explorama.frontend.search.views.components.row-message :refer [row-message]]
            [de.explorama.frontend.search.views.components.simple-mode-components :as simple-comps]))

(defn range-select-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/range-select props]
      [simple-comps/range-select props])))

(defn range-number-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/range-number-input props]
      [simple-comps/range-number-input props])))

(defn string-selection-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/string-selection-input props]
      [simple-comps/string-selection-input props])))

(defn year-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/year-select props]
      [simple-comps/range-select props])))

(defn month-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/month-input props]
      [simple-comps/string-selection-input props])))

(defn date-range-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/date-range-input props]
      [simple-comps/date-range-input props])))

(defn simple-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/simple-input props]
      [simple-comps/simple-input props])))

(defn contains-input [{:keys [path] :as props}]
  (let [advanced? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/adv-mode path])]
    (if advanced?
      [adv-comps/contains-input props]
      [simple-comps/contains-input props])))

(defn invalid-row [{:keys [path] :as props}]
  (let [invalid-search-row-message @(subscribe [::i18n/translate :invalid-search-row-message])]
    invalid-search-row-message))

(defn topic-datasource-switch [{:keys [frame-id attr-desc path disabled?]}]
  (when (is-topic-attr-desc? attr-desc)
    (let [topic-selection? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/topic-selection? path])
          {:keys [switch-to-datasource switch-to-topic]}
          @(subscribe [::i18n/translate-multi :switch-to-datasource :switch-to-topic])]
      [:span.attribute__toggle {:on-click #(when-not disabled?
                                             (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/reset-values-from-attr path])
                                             (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :topic-selection? (not topic-selection?)])
                                             (when topic-selection?
                                               (let [topic-selections @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])
                                                     datasources (reduce (fn [acc {:keys [datasources]}]
                                                                           (if (seq datasources)
                                                                             (apply conj acc (map to-option datasources))
                                                                             acc))
                                                                         #{}
                                                                         topic-selections)]
                                                 (dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection (vec datasources)])
                                                 (dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values (mapv :value datasources)]))
                                               (dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :topic-selection? false]))
                                             (options-backend/start-timeout-request frame-id topic-attr-desc)
                                             (dispatch (fi/call-api [:user-preferences :save-event-vec]
                                                                    "search-topic-selection?"
                                                                    (not topic-selection?))))}

       (if topic-selection?
         switch-to-datasource
         switch-to-topic)])))

(defn- topic-select [{:keys [path frame-id disabled? on-change on-blur class autofocus] :as props}]
  (let [{:keys [select-placeholder included-datasources]}
        @(subscribe [::i18n/translate-multi :select-placeholder :included-datasources])
        ui-selection @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])]
    [:<>
     [elements/selection-dropdown
      {:placeholder select-placeholder
       :all-group? true
       :autofocus   autofocus
       :on-change   (fn [selections]
                      (let [selections (elements/reduce-selections selections)]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection selections])
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values (mapv :value selections)])
                        (when on-change
                          (on-change))))
       :on-blur     on-blur
       :values      ui-selection
       :tooltip-key :tooltip
       :options     (re-frame/subscribe [::topics/topics frame-id])
       :classname   class
       :disabled? disabled?}]
     [:div.flex.flex-wrap.gap-2.mt-4
      [:div.text-xxs.text-secondary.px-4
       (when (seq ui-selection)
         included-datasources)]
      (reduce (fn [acc datasource]
                (conj acc
                      [chip {:label datasource
                             :size :extra-small}]))
              [:<>]
              (topic-values ui-selection))]]))

(defn- datasource-select [{:keys [path frame-id disabled? on-change on-blur class autofocus]}]
  (let [select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        ui-selection @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])
        options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/datasource-options frame-id])]
    [:<>
     [elements/selection-dropdown
      {:placeholder select-placeholder
       :is-grouped? true
       :autofocus   autofocus
       :on-change   (fn [selections]
                      (let [selections (elements/reduce-selections selections)]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection selections])
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values (mapv :value selections)])
                        (when on-change
                          (on-change))))
       :on-blur     on-blur
       :values      ui-selection
       :options     options
       :classname   class
       :disabled? disabled?}]]))

(defn topic-datasource-input [{:keys [path] :as props}]
  (if-let [topic-selection? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/topic-selection? path])]
    [topic-select props]
    [datasource-select props]))

(def location-input loc/location-input)

(defn search-selection-component [{:keys [frame-id path attr-desc is-last? read-only? on-change extra-style]}]
  (let [at-type (cond
                  (is-topic-attr-desc? attr-desc)
                  :topic
                  :else @(subscribe [::acs/attr-type attr-desc]))
        options (cond
                  (= at-type :month)
                  (subscribe [:de.explorama.frontend.search.views.formdata/month-ops path])
                  (= at-type :day)
                  (subscribe [:de.explorama.frontend.search.views.formdata/day-ops path])
                  :else
                  (subscribe [:de.explorama.frontend.search.views.formdata/search-attribute-options frame-id attr-desc {:transform? true :is-int? false}]))
        onchangefunc (fn []
                       (dispatch [:de.explorama.frontend.search.views.formdata/search-changed frame-id true])
                       (options-backend/start-timeout-request frame-id attr-desc)
                       (when (fn? on-change)
                         (on-change)))
        onblurfunc (fn []
                     (options-backend/clear-timeout-request frame-id)
                     (dispatch [::options-backend/request-options frame-id attr-desc true]))
        props (cond-> {:path          path
                       :frame-id      frame-id
                       :options       options
                       :attr          attr-desc
                       :on-change     onchangefunc
                       :on-blur-range onblurfunc
                       :on-blur       onblurfunc
                       :advanced-mode-enableable? true
                       :disabled? read-only?
                       :child [row-message frame-id path attr-desc]
                       :extra-style extra-style}
                is-last? (assoc :autofocus true))]
    (case at-type
      :topic [topic-datasource-input (assoc props :class "input--w100")]
      :string [string-selection-input (assoc props :class "input--w100")]
      :year [year-input props]
      :month [month-input (assoc props :class "input--w100")]
      :day [date-range-input props]
      :integer [range-number-input (assoc props :is-int? true)]
      (:double :decimal) [range-number-input (assoc props :is-int? false)]
      :location [location-input (assoc props :class "input--w100")]
      :notes [contains-input (assoc props :class "input--w100")]
      :external-ref [contains-input (assoc props
                                           :class "input--w100"
                                           :advanced-mode-enableable? false)]
      [invalid-row (assoc props :class "input--w100")])))