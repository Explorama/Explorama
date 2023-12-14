(ns de.explorama.frontend.reporting.data.reports
  (:require [re-frame.core :refer [reg-sub reg-event-db reg-event-fx]]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [taoensso.timbre :refer [debug]]
            [de.explorama.shared.reporting.ws-api :as ws-api]))
(reg-sub
 ::report
 (fn [db [_ r-id]]
   (or (get-in db (dr-path/report r-id))
       (get-in db (dr-path/shared-report r-id)))))

(reg-sub
 ::reports
 (fn [db]
   (get-in db dr-path/reports)))

(reg-event-fx
 ws-api/all-reports-result
 (fn [{db :db} [_ init-with-report-id reports]]
   (debug "reports arrived from server" reports)
   (when init-with-report-id
     (debug "load report" init-with-report-id))
   (let [load? (and init-with-report-id
                    (or (get-in reports [:shared init-with-report-id])
                        (get-in reports [:created init-with-report-id])))]
     {:db (assoc-in db dr-path/reports reports)
      :fx [(when load?
             [:dispatch [:de.explorama.frontend.reporting.views.reports.overview/show-report init-with-report-id]])
           (when load?
             [:dispatch [:de.explorama.frontend.reporting.views.builder/init-new]])]})))

(reg-event-fx
 ::request-reports
 (fn [{db :db} [_ init-with-report-id]]
   (debug "Requesting reports")
   {:backend-tube [ws-api/all-reports-route
                  {:client-callback [ws-api/all-reports-result init-with-report-id]}
                  (fi/call-api :user-info-db-get db)]}))

(reg-event-fx
 ws-api/save-report-result
 (fn [{db :db} [_ init-with-report-id & response]]
   {:db (-> db
            (assoc-in dr-path/creation-save-pending? false)
            (assoc-in dr-path/creation-save-response response))
    :dispatch [::request-reports init-with-report-id]}))

(reg-event-fx
 ::save-report
 (fn [{db :db} [_ {:keys [id] :as report-desc}]]
   {:backend-tube [ws-api/save-report-route
                  {:client-callback [ws-api/save-report-result id]
                   :broadcast-callback [::request-reports]}
                  (fi/call-api :user-info-db-get db)
                  report-desc]}))

(reg-event-fx
 ws-api/delete-report-result
 (fn [{db :db} [_ response code]]
   (debug "delete-result" response code)
   {:dispatch [::request-reports]}))

(reg-event-fx
 ::delete-report
 (fn [{db :db} [_ report-id]]
   {:backend-tube [ws-api/delete-report-route
                  {:client-callback ws-api/delete-report-result
                   :broadcast-callback [::request-reports]}
                  (fi/call-api :user-info-db-get db)
                  report-id]}))