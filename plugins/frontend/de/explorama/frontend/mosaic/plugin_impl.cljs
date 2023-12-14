(ns de.explorama.frontend.mosaic.plugin-impl
  (:require [de.explorama.frontend.mosaic.views.frame-header :refer [frame-header-impl
                                                                     loading-impl]]
            [de.explorama.frontend.mosaic.views.frame :refer [loading-screen-impl product-tour-impl
                                                              stop-screen-impl warn-screen-impl]]
            [de.explorama.frontend.mosaic.views.toolbar :refer [toolbar-impl]]
            [de.explorama.frontend.mosaic.views.filter.core :refer [filter-impl]]
            [de.explorama.frontend.mosaic.views.legend :refer [legend-impl]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.mosaic.views.frame-notifications :refer [frame-notifications-impl]]))

(re-frame/reg-sub
 ::parent-container-id
 (fn [_ [_ frame-id]]
   (str "mosaic_" (:frame-id frame-id))))

(def desc
  {:parent-container-id (fn [frame-id]
                          (re-frame/subscribe [::parent-container-id frame-id]))
   :component-did-mount (fn [frame-id]
                          (re-frame/dispatch [:de.explorama.frontend.mosaic.core/register-render-wait frame-id]))
   :clean-up-event-vec (fn [follow-event frame-ids]
                         [:de.explorama.frontend.mosaic.core/clean-up follow-event frame-ids])

   :loading? loading-impl
   :legend legend-impl
   :toolbar toolbar-impl
   :frame-header frame-header-impl

   :frame-header-context-menu nil
   :loading-screen loading-screen-impl
   :warn-screen warn-screen-impl
   :stop-screen stop-screen-impl

   :product-tour product-tour-impl

   :filter filter-impl
   :notifications frame-notifications-impl

   ;! hack because reporting does not use the normal way to interact with windows
   ;TODO r1/window-handling this could be solved over the new api - frame-info-api-register-event-vec
   :resize-listener (fn [params]
                      [:de.explorama.frontend.mosaic.interaction.resize/resize-listener params])})
