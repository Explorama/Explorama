(ns de.explorama.frontend.table.components.filter
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.table.backend-interface :as table-backend-interface]
            [de.explorama.frontend.table.util.queue :as queue-util]
            [de.explorama.frontend.common.queue :as ddq]))


(re-frame/reg-sub
 ::filter-desc
 (fn [db [_ frame-id]]
   (get-in db (path/frame-filter frame-id))))

(re-frame/reg-sub
 ::filter-limit-reached?
 (fn [db [_ vis-filter-path]]
   (get-in db (path/filter-stop-limit-reached vis-filter-path))))

(re-frame/reg-sub
 ::filter-disabled?
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::queue-util/loading? frame-id])
    (re-frame/subscribe [:de.explorama.frontend.table.components.frame-header/get-counts frame-id])
    (re-frame/subscribe [:de.explorama.frontend.table.components.filter/filter-desc frame-id])])
 (fn [[loading?
       {:keys [global]}
       {:keys [data-acs deactivated?]}]]
   (or (zero? (or global 0))
       (not (boolean data-acs))
       loading?
       deactivated?)))

(def filter-impl
  {:show-button? (fn [_] true)

   :check-before-open
   (fn [frame-id callback-event]
     (re-frame/dispatch [:de.explorama.frontend.table.operations.filter/check-before-show frame-id callback-event]))

   :data-acs-path
   (fn [frame-id]
     (conj (path/frame-filter frame-id)
           :data-acs))

   :submit-event
   (fn [frame-id local-filter]
     [::ddq/queue frame-id
      [::table-backend-interface/apply-filter frame-id local-filter]])

   :disabled?
   (fn [frame-id]
     (re-frame/subscribe [::filter-disabled? frame-id]))

   :tooltip-fn
   (fn [frame-id loading? invalid-filter? filter-active?]
     (let [{:keys [local global]} @(re-frame/subscribe [:de.explorama.frontend.table.components.frame-header/get-counts frame-id])
           {:keys [data-acs deactivated?]} @(re-frame/subscribe [:de.explorama.frontend.table.components.filter/filter-desc frame-id])
           loading-complete-data-acs? (boolean data-acs)
           all-filter-tooltip-text @(re-frame/subscribe [::i18n/translate :all-filter-tooltip-text])
           filtered-tooltip-text @(re-frame/subscribe [::i18n/translate :filtered-tooltip-text])
           constraints-limit-reached-tooltip @(re-frame/subscribe [::i18n/translate :constraints-limit-reached-tooltip])
           filter-limit-reached? @(re-frame/subscribe [:de.explorama.frontend.table.components.filter/filter-limit-reached? (path/frame-filter frame-id)])
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