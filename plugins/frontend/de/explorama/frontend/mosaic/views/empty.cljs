(ns de.explorama.frontend.mosaic.views.empty
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(defn empty-component
  "If the data in the mosaic window is not loaded or empty,
   a hint and a rectangular area are displayed,
   which shows where the window should be dragged into."
  [frame-id _]
  (reagent/create-class
   {:display-name "empty-component"
    :component-did-mount #(re-frame/dispatch (fi/call-api :render-done-event-vec
                                                      frame-id (str config/default-namespace
                                                                    " - mosaics core")))
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