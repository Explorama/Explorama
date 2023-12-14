(ns de.explorama.frontend.reporting.data.dashboards
  (:require [re-frame.core :refer [reg-sub reg-event-db reg-event-fx]]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [taoensso.timbre :refer [debug]]
            [de.explorama.shared.reporting.ws-api :as ws-api]))
(reg-sub
 ::dashboard
 (fn [db [_ d-id]]
   (or (get-in db (dr-path/dashboard d-id))
       (get-in db (dr-path/shared-dashboard d-id)))))

(reg-sub
 ::dashboards
 (fn [db]
   (get-in db dr-path/dashboards)))

(reg-event-fx
 ws-api/all-dashboards-result
 (fn [{db :db} [_ init-with-dashboard-id dashboards]]
   (debug "dashboards arrived from server" dashboards)
   (when init-with-dashboard-id
     (debug "load dashboard" init-with-dashboard-id))
   (let [load? (and init-with-dashboard-id
                    (or (get-in dashboards [:shared init-with-dashboard-id])
                        (get-in dashboards [:created init-with-dashboard-id])))]
     {:db (assoc-in db dr-path/dashboards dashboards)
      :fx [(when load?
             [:dispatch [:de.explorama.frontend.reporting.views.dashboards.overview/show-dashboard init-with-dashboard-id]])
           (when load?
             [:dispatch-later {:ms 1000 :dispatch [:de.explorama.frontend.reporting.views.builder/init-new]}])]})))

(reg-event-fx
 ::request-dashboards
 (fn [{db :db} [_ init-with-dashboard-id]]
   (debug "Requesting dashboards" init-with-dashboard-id ws-api/all-dashboards-result init-with-dashboard-id)
   {:backend-tube [ws-api/all-dashboards-route
                  {:client-callback [ws-api/all-dashboards-result init-with-dashboard-id]}
                  (fi/call-api :user-info-db-get db)]}))

(reg-event-fx
 ws-api/save-dashboard-result
 (fn [{db :db} [_ init-with-dashboard-id & response]]
   {:db (-> db
            (assoc-in dr-path/creation-save-pending? false)
            (assoc-in dr-path/creation-save-response response))
    :dispatch [::request-dashboards init-with-dashboard-id]}))

(reg-event-fx
 ::save-dashboard
 (fn [{db :db} [_ {:keys [id] :as dashboard-desc}]]
   {:backend-tube [ws-api/save-dashboard-route
                  {:client-callback [ws-api/save-dashboard-result id]
                   :broadcast-callback [::request-dashboards]}
                  (fi/call-api :user-info-db-get db)
                  dashboard-desc]}))

(reg-event-fx
 ws-api/delete-dashboard-result
 (fn [{db :db} [_ response code]]
   (debug "delete-result" response code)
   {:dispatch [::request-dashboards]}))

(reg-event-fx
 ::delete-dashboard
 (fn [{db :db} [_ dashboard-id]]
   {:backend-tube [ws-api/delete-dashboard-route
                  {:client-callback ws-api/delete-dashboard-result
                   :broadcast-callback [::request-dashboards]}
                  (fi/call-api :user-info-db-get db)
                  dashboard-id]}))