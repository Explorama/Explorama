(ns de.explorama.frontend.map.views.frame-header
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.views.legend :as legend]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::close
 (fn [_ [_ frame-id]]
   {:backend-tube [ws-api/remove-data {} frame-id]}))

(re-frame/reg-event-fx
 ::invalidate-size
 (fn [_ [_ frame-id]]
   (map-api/resize-map frame-id)
   {}))

(re-frame/reg-event-db
 ::set-counts
 (fn [db [_ frame-id global local]]
   (assoc-in db (geop/frame-filter-counts frame-id) {:global global :local local})))

(re-frame/reg-event-db
 ::reset-counts
 (fn [db [_ frame-id]]
   (update-in db (geop/frame-filter frame-id) dissoc geop/counts-key)))

(re-frame/reg-sub
 ::get-counts
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-filter-counts frame-id))))

(re-frame/reg-sub
 ::event-count
 (fn [[_ frame-id]]
   (re-frame/subscribe [::get-counts frame-id]))
 (fn [counts _]
   (:local counts)))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-di-desc frame-id))))

(re-frame/reg-sub
 ::title-prefix
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::event-count frame-id])
    (re-frame/subscribe [::i18n/translate :vertical-label-map])])
 (fn [[event-count
       vertical-label] [_ frame-id vertical-count-number]]
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
  {:frame-icon :map
   :frame-title-sub
   (fn [frame-id]
     (re-frame/subscribe [::title frame-id]))

   :frame-title-prefix-sub
   (fn [frame-id vertical-count-number]
     (re-frame/subscribe [::title-prefix frame-id vertical-count-number]))

   :can-change-title? true

   :on-minimize-event (fn [frame-id]
                        [:de.explorama.frontend.map.views.frame-header/invalidate-size frame-id])
   :on-maximize-event (fn [frame-id]
                        [:de.explorama.frontend.map.views.frame-header/invalidate-size frame-id])
   :on-normalize-event (fn [frame-id]
                         [:de.explorama.frontend.map.views.frame-header/invalidate-size frame-id])
   :on-close-fn (fn [frame-id done-fn]
                  (re-frame/dispatch [:de.explorama.frontend.map.views.frame-header/close frame-id])
                  (done-fn))})

(defn loading-impl [frame-id]
  (re-frame/subscribe [:de.explorama.frontend.map.views.map/is-loading? frame-id]))
