(ns de.explorama.backend.reporting.persistence.expdb.store
  (:require [de.explorama.backend.reporting.persistence.expdb.path :as persistence-path]
            [de.explorama.backend.reporting.persistence.store :as store-api]
            [de.explorama.backend.expdb.middleware.db :as expdb]
            [taoensso.timbre :refer [debug error]]))

(def dashboard-bucket "reporting/dashboards")
(defonce ^:private dashboard-path (str persistence-path/dashboards-root-folder "index"))
(def reports-bucket "reporting/reports")
(defonce ^:private report-path (str persistence-path/reports-root-folder "index"))

(def ^:private index-keys [:id :creator :name :description :shared-by])

(deftype GenStore [type-name bucket folder-path file-path-fn]
  store-api/Store
  (store-api/init [_])

  (store-api/read-files [_ _]
    (->>
     (expdb/get+ bucket)
     vals vec))

  (store-api/read-user-files [_ {:keys [username]}]
    (->
     (expdb/get+ bucket)
     (get username)
     vals vec))

  (store-api/read-file [_ username file-id]
    (expdb/get bucket (file-path-fn username file-id)))

  (store-api/write-file [_ {:keys [username]} file-id {:keys [id] :as content}]
    (let [path (file-path-fn username file-id)]
      (try
        (expdb/set bucket path content)
        true
        (catch #?(:clj Throwable :cljs :default) e
          (error "Failed to write" path e)
          false))))

  (store-api/delete-file [_ {:keys [username]} file-id]
    (let [path (file-path-fn username file-id)]
      (try
        (expdb/del bucket path)
        true
        (catch #?(:clj Throwable :cljs :default) e
          (error "Failed to delete" path e)
          false)))))

(defn init-stores []
  (let [dashboards (GenStore. "Dashboard"
                              dashboard-bucket
                              persistence-path/dashboards-root-folder
                              persistence-path/dashboard-file-path)
        reports (GenStore. "Report"
                           reports-bucket
                           persistence-path/reports-root-folder
                           persistence-path/report-file-path)]
    (debug "init dashboards store" persistence-path/dashboards-root-folder)
    (store-api/init dashboards)
    (debug "init reports store" persistence-path/reports-root-folder)
    (store-api/init reports)
    [dashboards reports]))