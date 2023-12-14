(ns de.explorama.frontend.search.backend.traffic-lights
  (:require [de.explorama.shared.search.ws-api :as ws-api]
            [de.explorama.frontend.search.path :as path]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.frontend.search.backend.util :refer [build-options-request-params]]
            [taoensso.timbre :refer-macros [debug]]
            [re-frame.core :refer [reg-event-db reg-event-fx
                                   reg-sub]]))

(reg-event-fx
 ws-api/recalc-traffic-lights
 (fn [_ [_ datasources frame-id formdata token]]
   {:backend-tube [ws-api/recalc-traffic-lights
                   {:client-callback [ws-api/recalc-traffic-lights-result]
                    :failed-callback [ws-api/failed-handler :recalc-traffic-lights]}
                   datasources frame-id formdata token]}))

(reg-event-fx
 ws-api/recalc-traffic-lights-result
 (fn [_ [_ frame-id tf-status token]]
   {:dispatch [::update-traffic-lights
               frame-id
               tf-status
               token]}))

(defn get-traffic-lights [db frame-id]
  (get-in db (path/traffic-light-status frame-id)))

(defn update-traffic-lights [db frame-id traffic-light-status]
  (assoc-in db (path/traffic-light-status frame-id) traffic-light-status))

(reg-event-db
 ::update-traffic-lights
 (fn [db [_
          frame-id
          traffic-light
          returned-token]]
   (let [frame-desc (get-in db (path/frame-desc frame-id))]
     (if (and frame-desc
              (or (= returned-token
                     (:token (get-traffic-lights db frame-id)))
                  (nil? returned-token)))
       (update-traffic-lights db frame-id (assoc traffic-light
                                                 :token returned-token))
       db))))

(reg-event-fx
 ::request-traffic-lights
 (fn [{db :db} [_ frame-id force?]]
   ;force? is used in replay-create-datainstance
   (let [search-rows (get-in db (path/frame-search-rows frame-id) {})
         {formdata :formdata} (build-options-request-params db frame-id nil search-rows false)]
     (debug "REQUEST Traffic lights" {:frame-id frame-id
                                      :formdata formdata
                                      :force? force?})
     (when (or (and (seq formdata)
                    (validation/search-formdata-valid? db frame-id))
               force?)
       (let [token (str (random-uuid))
             datasources (get-in db path/search-enabled-datasources)]
         {:dispatch [ws-api/recalc-traffic-lights datasources frame-id formdata token]
          :db (update-traffic-lights db frame-id {:status :pending
                                                  :token token})})))))

(reg-sub
 ::traffic-light
 (fn [db [_ frame-id]]
   (get-traffic-lights db frame-id)))