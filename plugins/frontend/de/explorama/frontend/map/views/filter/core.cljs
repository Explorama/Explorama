(ns de.explorama.frontend.map.views.filter.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.map.paths :as gp]
            [de.explorama.frontend.map.views.warning-screen :as warning-screen]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::filter-desc
 (fn [db [_ frame-id]]
   (get-in db (gp/frame-filter frame-id))))

(re-frame/reg-sub
 ::filter-limit-reached?
 (fn [db [_ path]]
   (get-in db (gp/stop-filterview path))))

(re-frame/reg-sub
 ::filter-disabled?
 (fn [[_ frame-id]]
   [(re-frame/subscribe [:de.explorama.frontend.map.views.map/is-loading? frame-id])
    (re-frame/subscribe [:de.explorama.frontend.map.views.frame-header/get-counts frame-id])
    (re-frame/subscribe [:de.explorama.frontend.map.views.filter.core/filter-desc frame-id])])
 (fn [[loading?
       {:keys [global]}
       {:keys [data-acs deactivated?]}]]
   (or (zero? (or global 0))
       (not (boolean data-acs))
       loading?
       deactivated?)))

(re-frame/reg-event-fx
 ::check-before-show
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event]]
   (let [show-warn? (get-in db (gp/warn-filterview frame-id))
         show-stop? (get-in db (gp/stop-filterview frame-id))]
     (cond show-stop?
           {:dispatch-n [[:de.explorama.frontend.map.views.stop-screen/stop-view-display frame-id :stop-view-display]]}
           show-warn?
           {:dispatch [::warning-screen/warning-view-display frame-id event]}
           :else
           {:dispatch event}))))

(re-frame/reg-event-fx
 ::filter-sync
 (fn [_ [_ frame-id local-filter]]
   {:dispatch [:de.explorama.frontend.map.operations.tasks/execute-wrapper
               frame-id
               :filter
               {:filter-desc local-filter}]}))

(def filter-impl
  {:show-button? (fn [_] true)

   :check-before-open
   (fn [frame-id callback-event]
     (re-frame/dispatch [::check-before-show frame-id callback-event]))

   :data-acs-path
   (fn [frame-id]
     (conj (gp/frame-filter frame-id) :data-acs))

   :submit-event
   (fn [frame-id local-filter]
     (let [sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)]
       (sync-event-fn [::filter-sync frame-id local-filter])
       [:de.explorama.frontend.map.operations.tasks/execute-wrapper
        frame-id
        :filter
        {:filter-desc local-filter}]))

   :disabled?
   (fn [frame-id]
     (re-frame/subscribe [::filter-disabled? frame-id]))

   :tooltip-fn
   (fn [frame-id loading? invalid-filter? filter-active?]
     (let [{:keys [local global]} @(re-frame/subscribe [:de.explorama.frontend.map.views.frame-header/get-counts frame-id])
           {:keys [data-acs deactivated?]} @(re-frame/subscribe [:de.explorama.frontend.map.views.filter.core/filter-desc frame-id])
           loading-complete-data-acs? (boolean data-acs)
           all-filter-tooltip-text @(re-frame/subscribe [::i18n/translate :all-filter-tooltip-text])
           filtered-tooltip-text @(re-frame/subscribe [::i18n/translate :filtered-tooltip-text])
           constraints-limit-reached-tooltip @(re-frame/subscribe [::i18n/translate :constraints-limit-reached-tooltip])
           filter-limit-reached? @(re-frame/subscribe [:de.explorama.frontend.map.views.filter.core/filter-limit-reached? frame-id])
           filter-tooltip-text (cond-> (str all-filter-tooltip-text ": " (i18n/localized-number global))
                                 (and local
                                      (not loading?)
                                      filter-active?
                                      (not filter-limit-reached?))
                                 (str "\n "
                                      filtered-tooltip-text
                                      ": "
                                      (i18n/localized-number local))
                                 (and (not loading?)
                                      loading-complete-data-acs?
                                      filter-limit-reached?)
                                 (str "\n" constraints-limit-reached-tooltip))]
       (cond (or (not loading-complete-data-acs?)
                 loading?)
             (re-frame/subscribe [::i18n/translate :loading-label])
             deactivated?
             (re-frame/subscribe [::i18n/translate :constraints-inactive-tooltip])
             invalid-filter?
             (re-frame/subscribe [::i18n/translate :constraints-invalid-tooltip])
             :else filter-tooltip-text)))})