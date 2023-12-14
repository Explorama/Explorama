(ns de.explorama.backend.projects.persistence.event-log.repository)

(defprotocol EventLogReader
  (read-lines [instance] [instance from to])
  (append-lines [instance lines])
  (delete [instance])
  (last-modified [instance]))
