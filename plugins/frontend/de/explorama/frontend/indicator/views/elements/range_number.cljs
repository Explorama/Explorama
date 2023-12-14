(ns de.explorama.frontend.indicator.views.elements.range-number
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [input-field]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.views.management :as management]
            [re-frame.core :as re-frame]))

(defn element [indicator-id {:keys [label hint default id]
                             :as comp-desc}]
  (let [label (if (keyword? label)
                @(re-frame/subscribe [::i18n/translate label])
                (str label))
        thousand-sep @(re-frame/subscribe [::i18n/translate :thousand-separator])
        decimal-sep @(re-frame/subscribe [::i18n/translate :decimal-separator])
        lang @(re-frame/subscribe [::i18n/current-language])
        hint-text (cond
                    (keyword? hint) (re-frame/subscribe [::i18n/translate hint])
                    (not (nil? hint)) (str hint))]
    [input-field (cond-> {:extra-class "input--w14"
                          :thousand-separator thousand-sep
                          :decimal-separator decimal-sep
                          :language (name lang)
                          :label label
                          :type :number
                          :default-value default
                          :value (re-frame/subscribe [::management/indicator-ui-desc-comp-value indicator-id id])
                          :on-change (fn [val]
                                       (re-frame/dispatch [::management/update-indicator-ui-desc
                                                       indicator-id id val]))}
                   hint-text (assoc :hint hint-text))]))