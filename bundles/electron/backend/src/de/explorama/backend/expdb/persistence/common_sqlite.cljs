(ns de.explorama.backend.expdb.persistence.common-sqlite
  (:require ["better-sqlite3" :as database]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [taoensso.timbre :refer [error debug]]))

(defn table-name [schema]
  (let [schema (str "indexed_" schema)
        result (str/replace (if (str/ends-with? schema "/")
                              (subs schema 0 (dec (count schema)))
                              schema)
                            #"[_\/-]+" "_")]
    result))

(defn create-db [db-key schema]
  (let [table-name (table-name schema)
        stm (str "CREATE TABLE IF NOT EXISTS " table-name
                 " (key TEXT PRIMARY KEY, value TEXT)")]
    (try
      (let [db (database db-key #js{#_#_"verbose" js/console.log})]
        (.pragma db "journal_mode = WAL")
        (when schema
          (.run (.prepare db stm)))
        db)
      (catch :default e
        (error e "create-db" stm)
        ;TODO handle this in every function
        {:success false
         :message "create-db - see logs for details"
         :error-reason (ex-message e)}))))

(defn db-close [db]
  (.close db))

(defn dump [db-key bucket]
  (let [db (create-db db-key bucket)]
    (try
      (let [stm (str "SELECT key, value FROM " (table-name bucket))
            result (.all (.prepare db stm))
            result (into {}
                         (map (fn [entry]
                                [(edn/read-string (aget entry "key"))
                                 (edn/read-string (aget entry "value"))]))
                         result)]
        result)
      (catch :default e
        (error "dump" e)
        {:success false
         :message "dump - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn set-dump [db-key bucket data]
  (let [db (create-db db-key nil)]
    (try
      (let [stm (str "INSERT OR REPLACE INTO " (table-name bucket) "(key,value) VALUES ($key,$value)")
            dbstm (.prepare db stm)
            insert-multi (.transaction db (fn [value-sets]
                                            (doall (for [value-set value-sets]
                                                     (.run dbstm value-set)))))
            result (insert-multi (clj->js (map (fn [[entry-key entry-value]]
                                                 #js{"key" (pr-str entry-key)
                                                     "value" (pr-str entry-value)})
                                               data)))]
        result)
      (catch :default e
        (error "set-dump" e)
        {:success false
         :message "Set dump - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-set+ [db-key bucket data]
  (let [db (create-db db-key bucket)
        stm (str "INSERT OR REPLACE INTO " (table-name bucket) "(key,value) VALUES ($key,$value)")]
    (try
      (let [dbstm (.prepare db stm)
            insert-multi (.transaction db (fn [value-sets]
                                            (doall (for [value-set value-sets]
                                                     (.run dbstm value-set)))))
            data (clj->js (map (fn [[entry-key entry-value]]
                                 #js{"key" (pr-str entry-key)
                                     "value" (pr-str entry-value)})
                               data))
            result (insert-multi data)]
        result)
      (catch :default e
        (error "set+" e)
        {:success false
         :message "db-set+ - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-get+ [db-key bucket keys]
  (let [stm (str "SELECT key, value FROM " (table-name bucket) " WHERE key IN ( " (str/join " , " (map (fn [_] "?") (range (count keys)))) " )")
        db (create-db db-key bucket)]
    (try
      (let [result (.all (.prepare db stm)
                         (clj->js (map pr-str keys)))
            result (into {}
                         (map (fn [entry]
                                [(edn/read-string (aget entry "key"))
                                 (edn/read-string (aget entry "value"))]))
                         result)]
        result)
      (catch :default e
        (error "get+" e stm)
        {:success false
         :message "db-get+ - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-del+ [db-key bucket keys]
  (let [stm (str "DELETE FROM " (table-name bucket) " WHERE key = ?")
        db (create-db db-key bucket)]
    (try
      (let [dbstm (.prepare db stm)
            del-multi (.transaction db (fn [value-sets]
                                         (doall (for [value-set value-sets]
                                                  (.run dbstm value-set)))))
            result (del-multi (clj->js (map pr-str keys)))]
        result)
      (catch :default e
        (error "del" e stm)
        {:success false
         :message "db-del+ - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-drop-table [db-key bucket]
  (let [stm (str "DROP TABLE " (table-name bucket))
        db (create-db db-key bucket)]
    (try
      (let [result (.run (.prepare db stm))]
        result)
      (catch :default e
        (error "del-bucket" e stm)
        {:success false
         :message "db-drop-table - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))
