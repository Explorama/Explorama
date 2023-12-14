(ns de.explorama.frontend.data-atlas.views.plugin-impl
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.data-atlas.views.core :as view-core :refer [toolbar-impl frame-header-impl product-tour-impl]]))

(re-frame/reg-sub
 ::cancellable?
 (fn [_ _]
   false))

(def desc
  {:component-did-mount (fn [frame-id])
   :loading? nil
   :toolbar toolbar-impl
   ;:settings settings-impl
   ;:burger burger-impl
   :frame-header frame-header-impl
   :custom-items nil

   :frame-header-context-menu nil
   :loading-screen {:show?
                    (fn [frame-id]
                      (re-frame/subscribe [::view-core/is-loading? frame-id]))
                    :cancellable?
                    (fn [_]
                      (re-frame/subscribe [::cancellable?]))
                    :cancel-fn
                    (fn [_ _])
                    :loading-screen-message-sub
                    (fn [_]
                      (re-frame/subscribe [::i18n/translate :loading-screen-message]))
                    :loading-screen-tip-sub
                    (fn [_]
                      (re-frame/subscribe [::i18n/translate :loading-screen-tip]))
                    :loading-screen-tip-titel-sub
                    (fn [_]
                      (re-frame/subscribe [::i18n/translate :loading-screen-tip-titel]))}
   :warn-screen nil
   :stop-screen nil

   :product-tour product-tour-impl

  ;;  :filter filter-impl
   :notifications nil})
