(ns de.explorama.backend.expdb.persistence.backend-simple
  (:require [de.explorama.backend.expdb.persistence.common-sqlite
             :refer [collect-result create-db db-close db-del+ db-drop-table
                     db-get+ db-set+ dump set-dump table-name]]
            [de.explorama.backend.expdb.persistence.simple :as itf]
            [taoensso.timbre :refer [error]]))

(def ^:private db-key "de.explorama.backend.expdb.simple.sqlite3")

(deftype Backend [bucket config]
  itf/Simple

  (schema [_]
    bucket)

  (dump [_]
    (dump db-key bucket))

  (set-dump [_ data]
    (set-dump db-key bucket data)
    {:success true
     :pairs (count data)})

  (del [_ key]
    (db-del+ db-key bucket [key])
    {:success true
     :pairs -1})
  (del-bucket [_]
    (db-drop-table db-key bucket)
    {:success true
     :dropped-bucket? true})

  (get [_ key]
    (-> (db-get+ db-key bucket [key])
        (get key)))
  (get+ [_]
    (let [stm (str "SELECT key, value FROM " (table-name bucket))
          db (create-db db-key bucket)]
      (try
        (let [result (.executeQuery (.prepareStatement db stm))
              result (collect-result result)]
          result)
        (catch Throwable e
          (error "get+ all" e stm))
        (finally (db-close db)))))
  (get+ [_ keys]
    (db-get+ db-key bucket keys))

  (set [_ key value]
    (db-set+ db-key bucket {key value})
    {:success true
     :pairs 1})

  (set+ [_ data]
    (db-set+ db-key bucket data)
    {:success true
     :pairs (count data)}))

(defn new-instance [config bucket]
  (Backend. bucket config))

(defn instances []
  (let [stm "SELECT name FROM sqlite_master WHERE type='table';"
        db (create-db db-key nil)
        ^java.sql.ResultSet tables (.executeQuery (.prepareStatement db stm))]
    (loop [result []]
      (if (.next tables)
        (recur (conj result
                     (.getString "name")))
        result))))
