(ns de.explorama.frontend.indicator.plugin-impl
  (:require [de.explorama.frontend.indicator.views.core :refer [toolbar-impl frame-header-impl loading-screen-impl loading-impl]]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::parent-container-id
 (fn [_ [_ frame-id]]
   (str "indicator_" (:frame-id frame-id))))

(def desc
  {#_#_:parent-container-id (fn [frame-id]
                              (re-frame/subscribe [::parent-container-id frame-id]))
   :component-did-mount (fn [frame-id]
                          #_(re-frame/dispatch [:mosaic.core/register-render-wait frame-id]))
   :clean-up-event-vec (fn [follow-event frame-ids]
                         [:de.explorama.frontend.indicator.core/clean-up follow-event frame-ids])
   :loading? loading-impl
   :toolbar toolbar-impl
   :loading-screen loading-screen-impl
   :burger {:show-button?
            (fn [_] true)}
   :frame-header frame-header-impl
   :frame-header-context-menu nil
   :product-tour {:component :none}
   :notifications nil})