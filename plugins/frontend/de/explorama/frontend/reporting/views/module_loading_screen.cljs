(ns de.explorama.frontend.reporting.views.module-loading-screen
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]))

(defn module-loading-screen [frame-id props]
  (let [{:keys [show? cancellable? cancel-fn
                loading-screen-message-sub
                loading-screen-tip-sub
                loading-screen-tip-titel-sub]}
        @(fi/call-api :papi-loading-screen-sub frame-id)
        show? (when show? @(show? frame-id))]
    (when show?
      [:div.loading-screen-wrapper
       [loading-screen {:show? show?
                        :buttons (when @(cancellable? frame-id)
                                   [{:label @(re-frame/subscribe [::i18n/translate :cancel-label])
                                     :on-click #(cancel-fn frame-id %)}])
                        :message (loading-screen-message-sub frame-id)
                        :tip (loading-screen-tip-sub frame-id)
                        :tip-titel (loading-screen-tip-titel-sub frame-id)}]])))