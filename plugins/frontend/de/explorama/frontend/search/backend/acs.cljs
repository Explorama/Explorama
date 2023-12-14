(ns de.explorama.frontend.search.backend.acs
  (:require [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.backend.di :as di-backend]
            [de.explorama.frontend.search.path :as path]
            [taoensso.timbre :refer-macros [debug]]
            [re-frame.core :refer [reg-event-fx reg-event-db]]))

(defonce ^:private current-requests? (atom 0))
(defonce ^:private requests-done? (atom 0))

(defn- excecute-request? [inc?]
  (>= (cond-> @requests-done?
        inc? inc)
      @current-requests?))

;Requests for reacting on an import/ac-update
(defn- update-frame-options [db]
  (let [search-frame-ids (set (keys (get-in db path/search-formdata {})))]
    (mapv (fn [frame-id] [::options-backend/request-options frame-id nil true])
          search-frame-ids)))

(reg-event-fx
 ::set-attr-types
 (fn [{db :db} [_ attr-types waiting-done?]]
   (cond (and waiting-done? (excecute-request? true))
         (do
           (reset! current-requests? 0)
           (reset! requests-done? 0)
           (debug "New attribute types arrived")
           {:dispatch-n (update-frame-options db)
            :db (assoc-in db path/attribute-types attr-types)})

         waiting-done? ;an newer update arrived
         (do
           (swap! requests-done? inc)
           (debug "Ignore new attr types due to a newer request")
           {})
         :else
         (do
           (swap! current-requests? inc)
           {:dispatch-later {:ms 300
                             :dispatch [::set-attr-types attr-types true]}}))))

(reg-event-fx
 ::trigger-data-instance-creation
 (fn [{db :db} [_ frame-id]]
   (when (and (not (get-in db (path/requesting frame-id)))
              (get-in db (path/create-data-instance frame-id)))
     {:db       (-> db
                    (assoc-in (path/create-data-instance frame-id) false))
      :dispatch [::di-backend/submit-form frame-id]})))

(reg-event-fx
 ::execute-callback
 (fn [{db :db} [_ frame-id]]
   (if-let [callback-vec (get-in db (path/frame-event-callback frame-id))]
     {:db       (update-in db path/event-callback dissoc frame-id)
      :dispatch callback-vec}
     {})))

(reg-event-db
 ::set-enabled-datasources
 (fn [db [_ datasources]]
   (assoc-in db path/search-enabled-datasources datasources)))

(reg-event-db
 ::set-bucket-datasources
 (fn [db [_ bucket-datasources]]
   (assoc-in db path/search-bucket-datasources bucket-datasources)))

(reg-event-fx
 ::set-acs
 (fn [{db :db} [_ frame-id acs request-id]]
   {:db         (-> db
                    (assoc-in [:search :acs :all-acs] acs)
                     ;(assoc-in [:search :acs frame-id :root_init] acs)
                    (assoc-in (path/requesting frame-id) 0)
                    (assoc-in (path/create-data-instance frame-id) false)
                    (assoc-in [:search :acs frame-id :root_init] {})
                    (update :search dissoc :frame-open-event))
    :dispatch-n [(when (get-in db [:search :frame-open-event])
                   (vec (conj (get-in db [:search :frame-open-event]) frame-id)))
                 [::trigger-data-instance-creation frame-id]
                 [::execute-callback frame-id]]}))