(ns de.explorama.frontend.mosaic.views.filter.core
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]))

(re-frame/reg-sub
 ::filter-desc
 (fn [db [_ path]]
   (get-in db (gp/filter-desc path))))

(re-frame/reg-sub
 ::filter-limit-reached?
 (fn [db [_ path]]
   (get-in db (gp/stop-filterview path))))

(re-frame/reg-sub
 ::deactivated?
 (fn [_ [_ _]]
   false))

(re-frame/reg-sub
 ::filter-disabled?
 (fn [[_ frame-id]]
   [(re-frame/subscribe [:de.explorama.frontend.mosaic.views.top-level/top-level (gp/top-level frame-id)])
    (re-frame/subscribe [:de.explorama.frontend.mosaic.views.frame-header/loading? frame-id])
    (re-frame/subscribe [:de.explorama.frontend.mosaic.views.frame-header/request-pending? frame-id])
    (re-frame/subscribe [:de.explorama.frontend.mosaic.data.di-acs/data-acs-available? frame-id])
    (re-frame/subscribe [::deactivated? frame-id])])
 (fn [[top-level loading? request-pending? data-acs-available? deactivated?] _]
   (let [{:keys [all-count]} top-level]
     (or (= 0 (or all-count 0))
         (not data-acs-available?)
         loading?
         request-pending?
         deactivated?))))

(re-frame/reg-sub
 ::true-sub?
 (fn [_ _]
   true))

(re-frame/reg-event-fx
 ::check-before-show
 [(.-uiInterceptor js/window)]
 (fn [{db :db} [_ frame-id callback-event]]
   (let [path (gp/top-level frame-id)
         show-warn? (get-in db (gp/warn-filterview path))
         show-stop? (get-in db (gp/stop-filterview path))
         frame-id (gp/frame-id path)]
     (cond-> {:db (cond
                    show-stop?
                    (assoc-in db (gp/stop-view frame-id) :stop-view-display)
                    show-warn?
                    (-> (assoc-in db (gp/warn-view frame-id) :filter-warn-view)
                        (assoc-in (gp/warn-view-callback frame-id) callback-event))
                    :else db)}
       (not (or show-warn? show-stop?))
       (assoc :dispatch callback-event)))))

(re-frame/reg-event-fx
 ::submit-filter
 (fn [_ [_ frame-id filter-desc]]
   (let [path (gp/top-level frame-id)]
     {:dispatch
      [::tasks/execute-wrapper
       path
       :filter
       {:filter-desc filter-desc}]})))

(def filter-impl
  {:show-button?
   (fn [frame-id]
     (re-frame/subscribe [::true-sub? frame-id]))

   :check-before-open
   (fn [frame-id callback-event]
     (re-frame/dispatch [::check-before-show frame-id callback-event]))

   :data-acs-path
   (fn [frame-id]
     (gp/data-acs frame-id))

   :submit-event
   (fn [frame-id local-filter]
     [::submit-filter frame-id local-filter])

   :disabled?
   (fn [frame-id]
     (re-frame/subscribe [::filter-disabled? frame-id]))

   :tooltip-fn
   (fn [frame-id loading? invalid-filter? filter-active?]
     (let [path (gp/top-level frame-id)
           all-filter-tooltip-text @(re-frame/subscribe [::i18n/translate :all-filter-tooltip-text])
           filtered-tooltip-text @(re-frame/subscribe [::i18n/translate :filtered-tooltip-text])
           constraints-limit-reached-tooltip @(re-frame/subscribe [::i18n/translate :constraints-limit-reached-tooltip])
           filter-limit-reached? @(re-frame/subscribe [::filter-limit-reached? path])
           {:keys [local-count all-count]} @(re-frame/subscribe [:de.explorama.frontend.mosaic.views.top-level/top-level path])
           deactivated? @(re-frame/subscribe [::deactivated? path])]
       (cond loading?
             @(re-frame/subscribe [::i18n/translate :loading-label])
             deactivated?
             @(re-frame/subscribe [::i18n/translate :constraints-inactive-tooltip])
             invalid-filter?
             @(re-frame/subscribe [::i18n/translate :constraints-invalid-tooltip])
             :else
             (cond-> (str all-filter-tooltip-text ": " (i18n/localized-number all-count))
               (and local-count
                    (not loading?)
                    filter-active?
                    (not filter-limit-reached?))
               (str "\n "
                    filtered-tooltip-text
                    ": "
                    (i18n/localized-number local-count))
               (and (not loading?)
                    filter-limit-reached?)
               (str "\n" constraints-limit-reached-tooltip)))))})
