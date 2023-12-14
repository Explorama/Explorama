(ns de.explorama.frontend.map.plugin-impl
  (:require
   [de.explorama.frontend.map.components.frame-notifications :refer [frame-notifications-impl]]
   [de.explorama.frontend.map.views.filter.core :refer [filter-impl]]
   [de.explorama.frontend.map.views.frame-header :refer [frame-header-impl loading-impl]]
   [de.explorama.frontend.map.views.legend :refer [legend-impl]]
   [de.explorama.frontend.map.views.map :refer [loading-screen-impl product-tour-impl stop-screen-impl
                                                warn-screen-impl]]
   [de.explorama.frontend.map.views.toolbar :refer [toolbar-impl]]
   [de.explorama.frontend.map.map.core :as map]))

(def desc
  {:component-did-mount (fn [frame-id])
   :clean-up-event-vec (fn [follow-event frame-ids]
                         [:de.explorama.frontend.map.core/clean-up follow-event frame-ids])

   :loading? loading-impl
   :toolbar toolbar-impl
   :burger {:show-button?
            (fn [_] true)}
   :legend legend-impl
   :frame-header frame-header-impl

   :frame-header-context-menu nil
   :loading-screen loading-screen-impl
   :warn-screen warn-screen-impl
   :stop-screen stop-screen-impl

   :product-tour product-tour-impl

   :filter filter-impl
   :notifications frame-notifications-impl

   ;! hack because reporting does not use the normal way to interact with windows
   :resize-listener (fn [params]
                      [::map/resize-listener params])})
