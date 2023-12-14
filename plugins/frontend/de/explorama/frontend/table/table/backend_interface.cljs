(ns de.explorama.frontend.table.table.backend-interface
  (:require [de.explorama.frontend.common.calculations.data-acs-client :as data-acs]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.shared.table.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as timbre :refer-macros [debug warn]]))

(re-frame/reg-event-fx
 ::apply-filter
 (fn [{db :db} [_ frame-id local-filter task-id]]
   (table-data/set-frame-table-config frame-id ws-api/current-page-key 1)
   (table-data/set-frame-table-config frame-id ws-api/filter-key local-filter)

   {:db (-> db
            (assoc-in (path/applied-filter frame-id) local-filter)
            (path/reset-stop-views frame-id))
    :backend-tube [ws-api/table-data {:client-callback [ws-api/table-data-result frame-id task-id]
                                      :failed-callback [ws-api/table-data-error frame-id task-id]}
                   (-> (table-data/frame-request-params frame-id)
                       (assoc :calc {:di-desc? true}
                              :task-id task-id))]}))

(re-frame/reg-event-fx
 ::connect-to-datainstance
 (fn [{db :db} [_
                {:keys [di frame-target-id source-table-state reset-state? state-params]}
                [source-local-filter]
                task-id]]
   (let [{ndb :db :keys [dispatch-n]} (fi/call-api [:details-view :remove-frame-events-from-details-view-db-update]
                                                   db frame-target-id)
         request-params (cond-> (or source-table-state {})
                          (map? state-params)
                          (merge state-params)
                          :always
                          (assoc ws-api/di-key di
                                 ws-api/vis-type-key :table
                                 ws-api/filter-key source-local-filter))]
     (if reset-state?
       (table-data/init-frame-table-config frame-target-id request-params)
       (table-data/merge-frame-table-config frame-target-id request-params))

     (cond-> {:backend-tube [ws-api/table-data {:client-callback [ws-api/table-data-result frame-target-id task-id]
                                                :failed-callback [ws-api/table-data-error frame-target-id task-id]}
                             (-> (table-data/frame-request-params frame-target-id)
                                 (assoc :calc {:data-keys? true
                                               :di-desc? true
                                               :data-acs? true}
                                        :task-id task-id))]
              :db (-> (or ndb db)
                      (path/reset-stop-views frame-target-id))}
       dispatch-n (update :dispatch-n (fn [o] (apply conj (or o [])
                                                     dispatch-n)))))))

(re-frame/reg-event-fx
 ws-api/table-data
 (fn [{db :db} [_ frame-id request-params task-id]]
   (let [request-params (merge (table-data/frame-request-params frame-id)
                               request-params)
         di? (boolean (get-in db (path/table-datasource frame-id)))
         same-request-params? (= request-params (get-in db (path/last-request-params frame-id)))]
     ;prevent from multiple request which makes no sense
     (if (and di? (not same-request-params?))
       (do (debug "Requesting table-data" (assoc request-params
                                                 :task-id task-id))
           {:db (-> db
                    (assoc-in (path/last-request-params frame-id) request-params)
                    (path/reset-stop-views frame-id))
            :backend-tube [ws-api/table-data {:client-callback [ws-api/table-data-result frame-id task-id]
                                              :failed-callback [ws-api/table-data-error frame-id task-id]}
                           request-params]})
       (do (warn "Ignore table-data request: No Datasource or same request-params as before" {:frame-id frame-id
                                                                                              :di? di?
                                                                                              :same-request-params? same-request-params?})
           {:fx [[:dispatch [::ddq/finish-task frame-id task-id ::table-data]]
                 [:dispatch [::ddq/execute-callback-vec frame-id ::table-data]]]})))))


(defn- handle-connected-result [db frame-id {:keys [di filtered-data-keys
                                                    data-acs external-refs di-desc
                                                    data-count filtered-count local-filter
                                                    stop-filterview? warn-filterview?] :as res}]
  (when filtered-data-keys
    (table-data/save-frame-columns frame-id filtered-data-keys))
  (let [project-loading? (fi/call-api :project-loading-db-get db)]
    {:db (cond-> (-> db
                     (assoc-in (path/table-datasource frame-id) di)
                     (assoc-in (path/applied-filter frame-id) local-filter)
                     (assoc-in (path/di-desc frame-id) di-desc))

           external-refs
           (assoc-in (path/frame-external-refs frame-id) external-refs)
           data-acs
           (assoc-in (conj (path/frame-filter frame-id) :data-acs)
                     (data-acs/post-process data-acs))
           (boolean? warn-filterview?)
           (assoc-in (path/filter-warn-limit-reached frame-id)
                     warn-filterview?)
           (boolean? stop-filterview?)
           (assoc-in (path/filter-stop-limit-reached frame-id)
                     stop-filterview?))
     :fx (cond-> []
           di
           (conj [:dispatch [:de.explorama.frontend.table.components.frame-header/set-counts
                             frame-id data-count filtered-count]]))}))

(re-frame/reg-event-fx
 ws-api/table-data-result
 (fn [{db :db} [_ frame-id task-id {:keys [di-desc]
                                    data ws-api/data-key
                                    :as result}]]
   (let [project-loading? (fi/call-api :project-loading-db-get db)]
     (if-not (get-in db (path/table-frame frame-id))
       (do (warn "Ignoring server result: frame not exists" {:frame-id frame-id})
           {:fx [[:dispatch [::ddq/finish-task frame-id task-id ::table-data-result]]
                 [:dispatch [::ddq/execute-callback-vec frame-id ::table-data-result]]]})
       (do (debug "Result from server arrived" (-> result
                                                   (assoc :task-id task-id)
                                                   (dissoc ws-api/data-key)))
           (table-data/save-frame-data frame-id data)
           (table-data/merge-frame-table-config frame-id
                                                (select-keys result
                                                             [ws-api/di-key
                                                              ws-api/filter-key
                                                              ws-api/focus-row-idx-key
                                                              ws-api/current-page-key
                                                              ws-api/data-count-key
                                                              ws-api/row-count-key
                                                              ws-api/data-range-key
                                                              ws-api/last-page-key]))
           (let [{:keys [fx] ndb :db} (when di-desc (handle-connected-result db frame-id result))]
             {:db (or ndb db)
              :fx (cond-> (or fx [])
                    (not project-loading?)
                    (conj [:dispatch (table-data/logging-event-vec frame-id)])
                    :always
                    (conj
                     [:dispatch [::ddq/finish-task frame-id task-id ::table-data-result]]
                     [:dispatch [::ddq/execute-callback-vec frame-id ::table-data-result]]))}))))))

(re-frame/reg-event-fx
 ::cancel-loading
 (fn [{_db :db} [_ frame-id]]
   (table-data/remove-frame-data frame-id)
   {:backend-tube [ws-api/set-backend-canceled frame-id]}))

(re-frame/reg-event-fx
 ::close-delete-data
 (fn [{db :db} [_ frame-id]]
   (table-data/remove-frame-data frame-id)
   {}))

(re-frame/reg-event-fx
 ws-api/table-data-error
 (fn [{db :db} [_ frame-id task-id {{:keys [error] :as error-desc} :error-desc}]]
   (warn "ERROR while backend request" {:frame-id frame-id
                                        :task-id task-id
                                        :error-desc error-desc})
   (let [render? (fi/call-api [:interaction-mode :render-db-get?] db)
         stop-view-label (case error
                           :too-much-data :stop-view-too-much-data
                           :unknown :stop-view-unknown)]

     {:fx (cond-> [(when render?
                     [:dispatch [:de.explorama.frontend.table.components.stop-screen/stop-view-display frame-id stop-view-label error-desc]])]
            (and frame-id task-id)
            (conj [:dispatch [::ddq/finish-task frame-id task-id ::table-data-result]]))})))