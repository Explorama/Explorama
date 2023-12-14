(ns de.explorama.backend.projects.persistence.event-log
  (:require [de.explorama.backend.projects.persistence.event-log.expdb :as expdb-impl]
            [de.explorama.backend.projects.persistence.event-log.repository :as repo]))

(defn new-instance [project-id]
  (expdb-impl/new-instance project-id))

(defn read-lines
  ([instance] (repo/read-lines instance))
  ([instance from to] (repo/read-lines instance from to)))

(defn append-lines [instance lines]
  (repo/append-lines instance lines))

(defn delete [instance]
  (repo/delete instance))

(defn last-modified [instance]
  (repo/last-modified instance))
