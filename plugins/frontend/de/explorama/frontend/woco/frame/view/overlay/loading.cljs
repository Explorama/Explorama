(ns de.explorama.frontend.woco.frame.view.overlay.loading
  (:require [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.frame.events :as evts]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.plugin-api :as papi]))

(defn frame-loading-screen [frame-id]
  (when-let [loading-desc @(re-frame/subscribe [::papi/loading-screen frame-id])]
    (let [{:keys [show? cancellable? cancel-fn
                  loading-screen-message-sub
                  loading-screen-tip-sub
                  loading-screen-tip-titel-sub]}
          loading-desc
          {is-minimized? :is-minimized?}
          @(re-frame/subscribe [::evts/frame frame-id])]
      [loading-screen (cond-> {:show? (and @(show? frame-id)
                                           (not is-minimized?))
                               :message (loading-screen-message-sub frame-id)
                               :tip (loading-screen-tip-sub frame-id)
                               :tip-title (loading-screen-tip-titel-sub frame-id)}
                        @(cancellable? frame-id)
                        (assoc :buttons
                               [{:label @(re-frame/subscribe [::i18n/translate :cancel-label])
                                 :on-click #(cancel-fn frame-id %)}]))])))
