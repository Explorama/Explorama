(ns de.explorama.shared.reporting.ws-api)

(def update-user-info ::update-user-info)

;;--------- Dashboards ---------

(def all-dashboards-doc "Fetch all dashboards with read/write access for user and send to client")
(def all-dashboards-route :reporting.persistence.api/all-dashboards)
(def all-dashboards-result :reporting.shared.data.dashboards/set-dashboards)

(def save-dashboard-doc "Saves an dashboard")
(def save-dashboard-route :reporting.persistence.api/save-dashboard)
(def save-dashboard-result :reporting.shared.data.dashboards/save-dashboard-response)

(def delete-dashboard-doc "Delete an dashboard")
(def delete-dashboard-route :reporting.persistence.api/delete-dashboard)
(def delete-dashboard-result :reporting.shared.data.dashboards/delete-dashboard-response)

;;--------- Reports ---------

(def all-reports-doc "Fetch all reports with read/write access for user and send to client")
(def all-reports-route :reporting.persistence.api/all-reports)
(def all-reports-result :reporting.shared.data.reports/set-reports)

(def save-report-doc "Saves an report")
(def save-report-route :reporting.persistence.api/save-report)
(def save-report-result :reporting.shared.data.reports/save-report-response)

(def delete-report-doc "Delete an report")
(def delete-report-route :reporting.persistence.api/delete-report)
(def delete-report-result :reporting.shared.data.reports/delete-report-response)

;;--------- Rights & Roles---------

(def roles-and-user-doc "Fetch all user and roles and send to client")
(def roles-and-users-route ::roles-and-users)
(def roles-and-users-result :reporting.configs.core/set-user-and-roles)

(def update-dr-rights-doc "Updates usage/writes/public rights of an report or dashboard")
(def update-dr-rights-route ::update-dr-rights)
(def update-dr-rights-result :reporting.shared.views.share-dr/submit-rights-response)