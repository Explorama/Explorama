(ns de.explorama.backend.projects.persistence.project.expdb
  (:require [de.explorama.backend.expdb.middleware.db :as expdb]
            [de.explorama.backend.projects.persistence.project.repository :as repo]))

(def ^:private bucket "/projects/project/")

;TODO r1/projects introduce logging for multi user?

(deftype Expdb-Repo [project-id]
  repo/Project
  (project-id [_]
    project-id)
  (read [_]
    (expdb/get bucket project-id))
  (update [_ desc]
    (expdb/set bucket
               project-id
               desc))
  (delete [_]
    (expdb/del bucket project-id)))

(defn new-instance [project-id]
  (Expdb-Repo. project-id))
