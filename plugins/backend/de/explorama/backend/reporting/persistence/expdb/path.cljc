(ns de.explorama.backend.reporting.persistence.expdb.path
  (:require [de.explorama.backend.reporting.util.core :as util]))

(def templates-root-folder "/reporting/templates/")
(def dashboards-root-folder "/reporting/dashboards/")
(def reports-root-folder "/reporting/reports/")

(defn user-folder-path [user-info parent-path]
  (str parent-path
       (if (map? user-info)
         (util/username user-info)
         user-info)))

(defn template-file-path [username template-id]
  (str templates-root-folder "/" template-id))

(defn dashboard-file-path [user-info dashboard-id]
  (str (user-folder-path user-info dashboards-root-folder)
       "/" dashboard-id))

(defn report-file-path [user-info report-id]
  (str (user-folder-path user-info reports-root-folder)
       "/" report-id))
