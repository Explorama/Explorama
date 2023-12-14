(ns de.explorama.backend.reporting.websocket
  (:require [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.reporting.persistence.api :as persistence-api]
            [de.explorama.shared.reporting.ws-api :as ws-api]
            [taoensso.timbre :refer [warn]]))

(defn- default-fn [& _]
  (warn "Not yet implemented"))

(def endpoints
 {ws-api/update-user-info default-fn

  ws-api/all-dashboards-route persistence-api/all-dashboards
  ws-api/save-dashboard-route persistence-api/save-dashboard
  ws-api/delete-dashboard-route persistence-api/delete-dashboard

  ws-api/all-reports-route persistence-api/all-reports
  ws-api/save-report-route persistence-api/save-report
  ws-api/delete-report-route persistence-api/delete-report})
