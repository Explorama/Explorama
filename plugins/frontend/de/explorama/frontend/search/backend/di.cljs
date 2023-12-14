(ns de.explorama.frontend.search.backend.di
  (:require [de.explorama.shared.search.ws-api :as ws-api]
            [de.explorama.frontend.search.path :as path]
            [de.explorama.frontend.search.backend.util :refer [build-options-request-params event-log-keys]]
            [de.explorama.frontend.search.data.acs :as acs]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.search.backend.traffic-lights :as traffic-lights-backend]
            [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 ws-api/create-di
 (fn [_ [_ datasources frame-id formdata callback-event]]
   {:backend-tube [ws-api/create-di
                   {:client-callback [ws-api/create-di-result]
                    :failed-callback [ws-api/failed-handler :create-di]}
                   datasources frame-id formdata callback-event]}))

(reg-event-fx
 ws-api/create-di-result
 (fn [_ [_ frame-id di callback-event]]
   {:dispatch [:de.explorama.frontend.search.data.di/create-datainstance-success frame-id di callback-event]}))

(reg-event-fx
 ::replay-create-data-instance
 (fn [{db :db} [_ event-formdata frame-id diid]]
   (let [search-path (path/frame-search-rows frame-id)
         new-db (-> db
                    (assoc-in search-path event-formdata)
                    (assoc-in (path/frame-wait-callback-keys frame-id)
                              #{:create :search-options :request-attributes}))
         {formdata :formdata} (build-options-request-params db frame-id nil (get-in new-db search-path) false)
         datasources (get-in db path/search-enabled-datasources)]
     {:db new-db
      :dispatch [::traffic-lights-backend/request-traffic-lights frame-id true]
      :fx [[:dispatch [ws-api/search-options datasources frame-id (vec (keys event-formdata)) event-formdata
                       [::wait-callback frame-id :search-options]]]
           [:dispatch [ws-api/request-attributes datasources frame-id (vec (keys event-formdata)) event-formdata
                       [::wait-callback frame-id :request-attributes]]]
           [:dispatch [ws-api/create-di datasources frame-id formdata
                       [::wait-callback frame-id :create]]]]})))

(reg-event-fx
 ::wait-callback
 (fn [{db :db} [_ frame-id done-key]]
   (let [callback-event (get-in db (path/frame-wait-callback frame-id))
         callback-keys-path (path/frame-wait-callback-keys frame-id)
         new-callback-keys (disj (get-in db callback-keys-path) done-key)
         work-remains? (seq new-callback-keys)]
     (if work-remains?
       {:db (assoc-in db callback-keys-path new-callback-keys)}
       {:db (-> db
                (update-in path/wait-callback-keys dissoc frame-id)
                (update-in path/wait-callback dissoc frame-id))
        :dispatch-n [callback-event]}))))

(reg-event-fx
 ::submit-search-form
 (fn [{db :db} [_ frame-id]]
   (let [path (path/frame-search-rows frame-id)
         {formdata :formdata} (build-options-request-params db frame-id nil (get-in db path) false)
         {event-log-formdata :formdata} (build-options-request-params db frame-id nil (get-in db path) event-log-keys {:translate-topics? false})
         datasources (get-in db path/search-enabled-datasources)]
     {:db (assoc-in db (path/di-creation-pending frame-id) true)
      :dispatch-n [(fi/call-api :reset-selections-event-vec frame-id)
                   [:de.explorama.frontend.search.event-logging/log-event frame-id "create-data-instance" {:formdata (into {} event-log-formdata)}]
                   [ws-api/create-di datasources frame-id formdata]]})))

(reg-event-fx
 ::submit-form
 [acs/requesting-acs-interceptor]
 (fn [_ [_ frame-id]]
   {:dispatch-n [[::submit-search-form frame-id]]}))