(ns de.explorama.frontend.algorithms.plugin-impl
  (:require [de.explorama.frontend.algorithms.components.main :refer [frame-header-impl
                                        loading-screen-impl
                                        product-tour-impl
                                        loading-impl
                                        stop-screen-impl]]
            [de.explorama.frontend.algorithms.components.legend :refer [legend-impl]]
            [de.explorama.frontend.algorithms.components.toolbar :refer [toolbar-impl]]
            [de.explorama.frontend.algorithms.components.frame-notifications :refer [frame-notifications]]))

(def frame-desc
  {:component-did-mount (fn [_] nil)
   :clean-up-event-vec (fn [follow-event frame-ids]
                         [:de.explorama.frontend.algorithms.core/clean-up follow-event frame-ids])
   :loading? loading-impl
   ;:settings settings-impl
   :burger {:show-button?
            (fn [_] true)}
   :frame-header frame-header-impl
   :legend legend-impl
   :toolbar toolbar-impl

   :frame-header-context-menu nil
   :loading-screen loading-screen-impl
   :warn-screen nil
   :stop-screen stop-screen-impl

   :product-tour product-tour-impl

   :filter nil
   :notifications nil})


