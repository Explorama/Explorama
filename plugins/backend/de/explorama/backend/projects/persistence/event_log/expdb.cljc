(ns de.explorama.backend.projects.persistence.event-log.expdb
  (:require [de.explorama.backend.expdb.middleware.db :as expdb]
            [de.explorama.backend.projects.persistence.event-log.repository :as repo]
            [de.explorama.shared.common.unification.time :refer [current-ms]]))

;TODO r1/projects introduce logging for multi user?

(def ^:private bucket "/projects/event-log/")

(defn- get-content [expdb-key]
  (let [content (expdb/get bucket expdb-key)]
    (if (empty? content)
      {:last-modified (current-ms)
       :content []}
      content)))

(deftype Expdb-Repo [expdb-key]
  repo/EventLogReader
  (read-lines [_]
    (:content (get-content expdb-key)))
  (read-lines [_ from to]
    (keep-indexed #(when (and (<= from %1)
                              (>= to %1))
                     %2)
                  (:content (get-content expdb-key))))
  (append-lines [_ lines]
    (let [current-content (:content (get-content expdb-key))
          new-lines (into current-content lines)]
      (expdb/set bucket
                 expdb-key
                 {:last-modified (current-ms)
                  :content new-lines})))
  (delete [_]
    (expdb/del bucket expdb-key))
  (last-modified [_]
    (:last-modified (get-content  expdb-key))))

(defn new-instance [project-id]
  (Expdb-Repo. project-id))
