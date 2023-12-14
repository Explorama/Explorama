(ns de.explorama.frontend.charts.components.frame-header
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]))

(re-frame/reg-event-db
 ::set-counts
 (fn [db [_ frame-id global-count local-count]]
   (cond-> db
     global-count
     (assoc-in (conj (path/frame-filter frame-id) :counts)
               {:global global-count :local global-count})
     local-count
     (assoc-in (conj (path/frame-filter frame-id) :counts :local)
               local-count))))

(re-frame/reg-event-db
 ::reset-counts
 (fn [db [_ frame-id]]
   (update-in db (path/frame-filter frame-id) dissoc :counts)))

(re-frame/reg-sub
 ::get-counts
 (fn [db [_ frame-id]]
   (get-in db (conj (path/frame-filter frame-id) :counts))))

(re-frame/reg-event-db
 ::set-custom-title
 [(fi/ui-interceptor)]
 (fn [db [_ frame-id title]]
   (assoc-in db (path/custom-title frame-id) title)))

(re-frame/reg-sub
 ::custom-title
 (fn [db [_ frame-id]]
   (get-in db (path/custom-title frame-id))))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (get-in db (path/di-desc frame-id))))

(re-frame/reg-sub
 ::event-count
 (fn [[_ frame-id]]
   (re-frame/subscribe [::get-counts frame-id]))
 (fn [counts _]
   (:local counts)))

(re-frame/reg-sub
 ::title-prefix
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::event-count frame-id])
    (re-frame/subscribe [::i18n/translate :chart-component-label])])
 (fn [[event-count
       vertical-label] [_ _ vertical-count-number]]
   (cond-> (str vertical-label " " vertical-count-number)
     (zero? event-count)
     (str " - 0 Events")
     (pos? event-count)
     (str " - "
          (i18n/localized-number event-count)
          " Events"))))

(re-frame/reg-sub
 ::title
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::di-desc frame-id])])
 (fn [[di-desc]]
   (let [{:keys [years countries datasources]} di-desc]
     (when (and (and datasources years countries)
                (not-empty datasources)
                (not-empty years)
                (not-empty countries))
       (str/join " " [" -" datasources years countries])))))

(def frame-header-impl
  {:frame-icon
   (fn [frame-id]
     :charts)

   :frame-title-sub
   (fn [frame-id]
     (re-frame/subscribe [::title frame-id]))

   :frame-title-prefix-sub
   (fn [frame-id vertical-count-number]
     (re-frame/subscribe [::title-prefix frame-id vertical-count-number]))

   :on-minimize-event (fn [frame-id])
   :on-maximize-event (fn [frame-id])
   :on-normalize-event (fn [frame-id])
   :on-close-fn (fn [_ done-fn]
                  (done-fn))
   :can-change-title? true})

(defn loading-impl [frame-id]
  (re-frame/subscribe [::queue-util/loading? frame-id]))