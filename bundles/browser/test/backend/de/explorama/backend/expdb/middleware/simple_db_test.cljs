(ns de.explorama.backend.expdb.middleware.simple-db-test
  (:require [de.explorama.backend.expdb.persistence.backend-simple :as simple-db]))

(def db (atom (simple-db/new-instance nil "test-db")))

(defn test-setup [test-func]
  (reset! @#'simple-db/store {})
  (reset! db (simple-db/new-instance nil "test-db"))
  (test-func))
