(ns de.explorama.frontend.charts.empty
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.charts.config :as vconfig]))

(defn empty-component
  "This is needed so the frame informs frontend interface of render-done"
  [frame-id _]
  (reagent/create-class
   {:display-name "empty-component"
    :component-did-mount #(re-frame/dispatch (fi/call-api :render-done-event-vec
                                                          frame-id
                                                          vconfig/default-namespace))

    :reagent-render (fn [_ {:keys [counts-sub]}]
                      (let [{data-count :local} (val-or-deref counts-sub)]
                        [:div.no-data-placeholder
                         (if (and data-count (= data-count 0))
                           [:span.enable--linebreaks
                            @(re-frame/subscribe [::i18n/translate :empty-data-hint])
                            [icon {:icon :info-circle}]]
                           [:span.enable--linebreaks
                            @(re-frame/subscribe [::i18n/translate :drag-info])
                            [icon {:icon :drop}]])]))}))