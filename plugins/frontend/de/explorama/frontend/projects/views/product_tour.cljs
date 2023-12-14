(ns de.explorama.frontend.projects.views.product-tour
  (:require [de.explorama.frontend.ui-base.components.misc.core :as comp-misc]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]))

(defn product-tour-step [params]
  (let [current-step (fi/call-api [:product-tour :current-sub])
        current-language (re-frame/subscribe [::i18n/current-language])
        steps-label (re-frame/subscribe [::i18n/translate :product-tour-steps-label])
        next-button-label (re-frame/subscribe [::i18n/translate :product-tour-next-button-label])
        back-button-label (re-frame/subscribe [::i18n/translate :product-tour-back-button-label])
        max-steps (fi/call-api [:product-tour :max-steps-sub])]
    [comp-misc/product-tour-step (assoc params
                                        :current-step current-step
                                        :language current-language
                                        :steps-label steps-label
                                        :next-button-label next-button-label
                                        :back-button-label back-button-label
                                        :val-sub-fn (fn [keyword-vector]
                                                      @(re-frame/subscribe (if (vector? keyword-vector)
                                                                             keyword-vector
                                                                             [::i18n/translate keyword-vector])))
                                        :prev-fn (fn [_]
                                                   (re-frame/dispatch (fi/call-api [:product-tour :previous-event-vec])))
                                        :next-fn (fn [{:keys [component additional-info]}]
                                                   (re-frame/dispatch (fi/call-api [:product-tour :next-event-vec]
                                                                                   component additional-info)))
                                        :cancel-fn (fn [_]
                                                     (re-frame/dispatch (fi/call-api [:product-tour :cancel-event-vec])))
                                        :max-steps max-steps)]))