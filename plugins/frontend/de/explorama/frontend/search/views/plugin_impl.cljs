(ns de.explorama.frontend.search.views.plugin-impl
  (:require [de.explorama.frontend.search.views.komplexe-suche-view :refer [frame-header-impl product-tour-impl]]
            [de.explorama.frontend.search.views.toolbar :refer [toolbar-impl]]))

(def desc
  {:component-did-mount (fn [frame-id])
   :loading? false
   :toolbar toolbar-impl
   ;:settings settings-impl
   :burger {:show-button?
            (fn [_] true)}
   :frame-header frame-header-impl

   :frame-header-context-menu nil
   :loading-screen nil
   :warn-screen nil
   :stop-screen nil

   :product-tour product-tour-impl

  ;;  :filter filter-impl
   :notifications nil})


