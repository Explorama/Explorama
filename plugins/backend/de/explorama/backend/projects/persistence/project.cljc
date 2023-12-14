(ns de.explorama.backend.projects.persistence.project
  (:require [de.explorama.backend.projects.persistence.project.expdb :as expdb-impl]
            [de.explorama.backend.projects.persistence.project.repository :as repo]
            [de.explorama.backend.expdb.middleware.db :as expdb])
  (:refer-clojure :exclude [read update]))

(def ^:private bucket "/projects/project-list")

(defn new-instance [project-id]
  (let [projects ((fnil conj #{}) (expdb/get bucket "")
                                  project-id)]
    (expdb/set bucket "" projects))

  (expdb-impl/new-instance project-id))

(defn read [instance]
  (repo/read instance))

(defn update [instance desc]
  (repo/update instance desc))

(defn delete [instance]
  (expdb/set bucket ""
             ((fnil disj #{}) (expdb/get bucket "")
                              (repo/project-id instance)))
  (repo/delete instance))

(defn list-all []
  (expdb/get bucket ""))
