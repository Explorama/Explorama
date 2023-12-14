(ns de.explorama.backend.projects.persistence.project.repository
  (:refer-clojure :exclude [read update]))

(defprotocol Project
  (project-id [instance])
  (read [instance])
  (update [instance desc])
  (delete [instance]))
