(ns de.explorama.frontend.indicator.views.core
  "Connects main and sidebar to one view.
   Also defines the frame definition and content."
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.components.dialog :as dialog]
            [de.explorama.frontend.indicator.db-utils :as db-utils]
            [de.explorama.frontend.indicator.path :as db-path]
            [de.explorama.frontend.indicator.views.main :as main-view]
            [de.explorama.frontend.indicator.views.management :as management]
            [de.explorama.frontend.indicator.views.overview :as overview]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::title-prefix
 (fn [db [_ _frame-id _]]
   (i18n/translate db :vertical-label-indicator)))

(re-frame/reg-event-fx
 ::no-event
 (fn [_ _]
   {}))

(def frame-header-impl
  {:frame-icon :indicator
   :frame-title-sub (fn [_frame-id]
                      (atom "")) ;! workaround otherwise boom! 
   :frame-title-prefix-sub (fn [frame-id vertical-count-number]
                             (re-frame/subscribe [::title-prefix frame-id vertical-count-number]))
   :can-change-title? false
   :on-minimize-event (fn [_frame-id] [::no-event])
   :on-maximize-event (fn [_frame-id] [::no-event])
   :on-normalize-event (fn [_frame-id] [::no-event])
   :on-close-fn (fn [_frame-id done-fn]
                  (done-fn))})

(def toolbar-impl
  {})

(defn loading-impl [_]
  (re-frame/subscribe [::is-loading?]))

(def loading-screen-impl
  {:show?
   (fn [_frame-id]
     (re-frame/subscribe [::is-loading?]))
   :cancellable?
   (fn [_]
     (atom false))
   :cancel-fn
   (fn [_frame-id _])
   :loading-screen-message-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-message]))
   :loading-screen-tip-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip]))
   :loading-screen-tip-titel-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip-titel]))})

(defn main-panel [frame-id drop-area-props]
  (let [{:keys [is-minimized?]} @(fi/call-api :frame-sub frame-id)
        receive-sync-events? @(fi/call-api :project-receive-sync?-sub)
        current-indicator-id @(re-frame/subscribe [::management/active-indicator])
        no-sync-hint @(re-frame/subscribe [::i18n/translate :no-sync-hint])]
    [:div.window__body {:style {:display
                                (when is-minimized?
                                  "none")
                                :height "100%"}}
     (cond
       receive-sync-events? [:div.no-data-placeholder
                             [:span
                              [:div.loader-sm.pr-8
                               [:span]]
                              [:div no-sync-hint]]]
       current-indicator-id [main-view/view frame-id drop-area-props]
       :else [overview/view frame-id])
     [dialog/view]]))

(re-frame/reg-event-db
 ::set-loading
 (fn [db [_ loading?]]
   (db-utils/frame-exist-guard
    db
    (assoc-in db db-path/loading? loading?))))

(re-frame/reg-sub
 ::is-loading?
 (fn [db _]
   (get-in db db-path/loading? false)))
