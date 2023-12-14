(ns de.explorama.frontend.indicator.views.elements.custom-textarea
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [textarea]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.views.management :as management]
            [re-frame.core :as re-frame]))

(defn element [indicator-id {:keys [label id]
                             :as comp-desc}]
  (let [label (if (keyword? label)
                @(re-frame/subscribe [::i18n/translate label])
                (str label))]
    [textarea {:label label
               :extra-class "custom-indicator"
               :max-length nil
               :value (re-frame/subscribe [::management/indicator-ui-desc-comp-value indicator-id id])
               :on-change
               (fn [changed-val]
                 (re-frame/dispatch [::management/update-indicator-ui-desc
                                 indicator-id id changed-val]))}]))