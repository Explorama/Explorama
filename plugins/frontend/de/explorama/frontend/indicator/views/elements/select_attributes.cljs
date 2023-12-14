(ns de.explorama.frontend.indicator.views.elements.select-attributes
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [select]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.views.management :as management]
            [re-frame.core :as re-frame]))

(defonce grouped-content #{:calc-attributes :all-attributes})

(defn- options-sub [indicator-id content filter-list number-of-events]
  (case content
    :group-attributes (re-frame/subscribe [::management/group-attributes indicator-id])
    :time (re-frame/subscribe [::management/time-attributes indicator-id])
    :calc-attributes (re-frame/subscribe [::management/calc-attributes indicator-id filter-list number-of-events])
    :all-attributes (re-frame/subscribe [::management/additional-attributes indicator-id filter-list])
    []))

(defn element [indicator-id {:keys [label hint content id number-of-events]
                             :as comp-desc}]
  (let [attribute-labels @(fi/call-api [:i18n :get-labels-sub])
        update-attribute-label (fn [option] (update option :label #(get attribute-labels % %)))
        updated-options (mapv
                         (fn [option]
                           (if-let [sub-options (get option :options)]
                             (assoc option :options (mapv update-attribute-label sub-options))
                             (update-attribute-label option)))
                         @(options-sub indicator-id content nil number-of-events))
        value @(re-frame/subscribe [::management/indicator-ui-desc-comp-value indicator-id id])
        updated-value (when value
                        (if (vector? value)
                          (mapv update-attribute-label value)
                          (update-attribute-label value)))
        label (if (keyword? label)
                @(re-frame/subscribe [::i18n/translate label])
                (str label))
        is-multi? (= content :group-attributes)
        is-grouped? (grouped-content content)
        hint-text (cond
                    (keyword? hint) (re-frame/subscribe [::i18n/translate hint])
                    (not (nil? hint)) (str hint))]
    [select (cond-> {:is-clearable? false
                     :extra-class "input--w14"
                     :label label
                     :is-multi? is-multi?
                     :is-grouped? (boolean is-grouped?)
                     :group-value-key :di
                     :mark-invalid? true
                     :options updated-options
                     :values updated-value
                     :on-change (fn [changed-val]
                                  (re-frame/dispatch [::management/update-indicator-ui-desc
                                                  indicator-id id changed-val]))}

              hint-text (assoc :hint hint-text))]))