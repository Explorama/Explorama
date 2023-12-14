(ns de.explorama.frontend.search.data.di
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.search.data.acs :as acs]
            [taoensso.timbre :refer-macros [debug error warn]]
            [de.explorama.frontend.search.path :as spath]))

(re-frame/reg-sub
 ::di-creation-pending?
 (fn [db [_ frame-id]]
   (get-in db (spath/di-creation-pending frame-id) false)))

(re-frame/reg-sub
 ::di-creation-success?
 (fn [db [_ frame-id]]
   (get-in db (spath/di-creation-success frame-id) true)))

(re-frame/reg-event-db
 ::confirm-failed-di-creation
 (fn [db [_ frame-id]]
   (-> db
       (assoc-in (spath/di-creation-success frame-id) nil))))

(re-frame/reg-sub
 ::di
 (fn [db [_ frame-id]]
   (get-in db (spath/data-instance frame-id))))

(re-frame/reg-event-fx
 ::create-datainstance-success
 (fn [{db :db}
      [_ frame-id di callback-vec]]
   (let [render? (fi/call-api [:interaction-mode :render-db-get?]
                              db)]
     (debug "di created " {:frame-id frame-id
                           :di di
                           :callback-vec callback-vec})
     (let [frame-desc (get-in db (spath/frame-desc frame-id))
           old-di (get-in db (spath/data-instance frame-id))]
       (if frame-desc
         {:db         (-> db
                          (acs/create-data-instance= frame-id false)
                          (assoc-in (spath/data-instance frame-id) di)
                          (assoc-in (spath/di-creation-pending frame-id) false)
                          (assoc-in (spath/di-creation-success frame-id) true))
          :dispatch-n (cond-> [callback-vec]
                        (and render? (nil? old-di))
                        (conj (fi/call-api :frame-header-color-event-vec frame-id)
                              (fi/call-api :frame-set-publishing-event-vec frame-id true))

                        (and render? old-di)
                        (conj (fi/call-api :frame-update-children frame-id {:di di}))

                        render?
                        (conj [:de.explorama.frontend.search.views.components.direct-visualization/open-visualizations frame-id]))}
         {:fx [(when callback-vec
                 [:dispatch callback-vec])]})))))
