(ns de.explorama.frontend.knowledge-editor.plugin-impl
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::cancellable?
 (fn [_ _]
   false))

(def toolbar-impl
  {})

(def frame-header-impl
  {:frame-icon :atlas
   :frame-title-sub (fn [frame-id] (atom nil))
   :frame-title-prefix-sub (fn [frame-id vertical-count-number]
                             (re-frame/subscribe [::i18n/translate :vertical-label-data-atlas]))
   :can-change-title? false
   :on-minimize-event (fn [frame-id] [::no-event])
   :on-maximize-event (fn [frame-id] [::no-event])
   :on-normalize-event (fn [frame-id] [::no-event])
   :on-close-fn (fn [frame-id done-fn]
                  (done-fn))})

(def product-tour-impl
  {:component :knowledge-editor})

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
                      (atom false))
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
