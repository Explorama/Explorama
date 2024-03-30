(ns de.explorama.backend.expdb.persistence.common-sqlite
  (:require [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [taoensso.timbre :refer [error]])
  (:import [java.sql Connection ResultSet Statement]))

;TODO r1/expdb - use the clojure.java.jdbc api

(defn table-name [schema]
  (let [schema (str "indexed_" schema)
        result (str/replace (if (str/ends-with? schema "/")
                              (subs schema 0 (dec (count schema)))
                              schema)
                            #"[_\/-]+" "_")]
    result))

(defn collect-result [^ResultSet result-set]
  (if result-set
    (loop [result []]
      (if (.next result-set)
        (recur (conj result
                     [(edn/read-string (.getString result-set "key"))
                      (edn/read-string (.getString result-set "value"))]))
        (into {} result)))
    []))

(defn create-db [db-key schema]
  (let [table-name (table-name schema)
        stm (str "CREATE TABLE IF NOT EXISTS " table-name
                 " (key TEXT PRIMARY KEY, value TEXT)")]
    (try
      (let [^Connection db (jdbc/get-connection {:classname "org.sqlite.JDBC"
                                                 :subprotocol "sqlite"
                                                 :subname db-key})]
        (when schema
          (.executeUpdate (jdbc/prepare-statement db stm)))
        db)
      (catch Throwable e
        (error e "create-db" stm)
        {:success false
         :message "create-db - see logs for details"
         :error-reason (ex-message e)}))))

(defn db-close [^Connection db]
  (.close db))

(defn dump [db-key bucket]
  (let [^Connection db (create-db db-key bucket)]
    (try
      (let [stm (str "SELECT key, value FROM " (table-name bucket))
            result (.executeQuery (jdbc/prepare-statement db stm))
            result (collect-result result)]
        result)
      (catch Throwable e
        (error "dump" e)
        {:success false
         :message "dump - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn set-dump [db-key bucket data]
  (let [^Connection db (create-db db-key nil)]
    (try
      (let [stm (str "INSERT OR REPLACE INTO " (table-name bucket) "(key,value) VALUES ($key,$value)")
            _ (.setAutoCommit db false)
            ^Statement dbstm (jdbc/prepare-statement db stm)
            _ (doseq [[entry-key entry-value] data]
                (.setString dbstm 1 (pr-str entry-key))
                (.setString dbstm 2 (pr-str entry-value))
                (.executeUpdate dbstm))
            result (.commit db)]
        (.commit db)
        result)
      (catch Throwable e
        (error "set-dump" e)
        {:success false
         :message "Set dump - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-set+ [db-key bucket data]
  (let [^Connection db (create-db db-key bucket)
        stm (str "INSERT OR REPLACE INTO " (table-name bucket) "(key,value) VALUES (?, ?)")]
    (try
      (let [_ (.setAutoCommit db false)
            ^Statement dbstm (jdbc/prepare-statement db stm)
            _ (doseq [[entry-key entry-value] data]
                (.setString dbstm 1 (pr-str entry-key))
                (.setString dbstm 2 (pr-str entry-value))
                (.executeUpdate dbstm))
            result (.commit db)]
        result)
      (catch Throwable e
        (error "set+" e)
        {:success false
         :message "db-set+ - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-get+ [db-key bucket keys]
  (let [stm (str "SELECT key, value FROM " (table-name bucket) " WHERE key IN ( " (str/join " , " (map (fn [_] "?") (range (count keys)))) " )")
        ^Connection db (create-db db-key bucket)]
    (try
      (let [^Statement dbstm (jdbc/prepare-statement db stm)
            _ (doseq [[idx key] (map-indexed vector keys)]
                (.setString dbstm (inc idx) (pr-str key)))
            result (.executeQuery dbstm)
            result (collect-result result)]
        result)
      (catch Throwable e
        (error "get+" e stm)
        {:success false
         :message "db-get+ - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-del+ [db-key bucket keys]
  (let [stm (str "DELETE FROM " (table-name bucket) " WHERE key IN ( " (str/join " , " (map (fn [_] "?") (range (count keys)))) " )")
        ^Connection db (create-db db-key bucket)]
    (try
      (let [_ (.setAutoCommit db false)
            dbstm (jdbc/prepare-statement db stm)
            _ (doseq [[idx key] (map-indexed vector keys)]
                (.setString dbstm (inc idx) (pr-str key)))
            _ (.executeUpdate dbstm)
            result (.commit db)]
        result)
      (catch Throwable e
        (error "del" e stm)
        {:success false
         :message "db-del+ - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))

(defn db-drop-table [db-key bucket]
  (let [stm (str "DROP TABLE " (table-name bucket))
        ^Connection db (create-db db-key bucket)]
    (try
      (let [result (.executeUpdate (jdbc/prepare-statement db stm))]
        result)
      (catch Throwable e
        (error "del-bucket" e stm)
        {:success false
         :message "db-drop-table - see logs for details"
         :error-reason (ex-message e)})
      (finally (db-close db)))))
