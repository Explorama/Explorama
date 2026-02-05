(ns de.explorama.backend.projects.middleware
  (:require [de.explorama.backend.projects.event-log :as pcore]))

(defn log-event [body]
  (pcore/eventlog (select-keys body [:user-info :client-id])
                  body))