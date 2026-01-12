(ns de.explorama.backend.expdb.persistence.backend-simple
  (:require [clojure.edn :as edn]
            [de.explorama.backend.electron.config :refer [app-data-path]]
            [de.explorama.backend.electron.file :refer [add-to-path]]
            [de.explorama.backend.expdb.persistence.common-sqlite :refer [create-db db-close db-del+ db-drop-table db-get+ db-set+ dump set-dump table-name]]
            [de.explorama.backend.expdb.persistence.simple :as itf]))

(def ^:private db-key (add-to-path app-data-path
                                   "de.explorama.backend.expdb.simple.sqlite3"))

(def ^:private root-key "/de.explorama.backend.expdb/")

(defn- base-key [schema]
  (str root-key
       "simple/"
       schema
       "/"))

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
        (let [result (.all (.prepare db stm))
              result (into {}
                           (map (fn [entry]
                                  [(edn/read-string (aget entry "key"))
                                   (edn/read-string (aget entry "value"))]))
                           result)]
          result)
        (catch :default e
          (println "get+ all" e stm))
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
        tables (.all (.prepare db stm))]
    (js->clj tables)))
