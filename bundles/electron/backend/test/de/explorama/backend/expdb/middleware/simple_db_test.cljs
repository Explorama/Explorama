(ns de.explorama.backend.expdb.middleware.simple-db-test
  (:require [de.explorama.backend.expdb.persistence.backend-simple :as simple-db]
            [fs :as fs]))

(def db (atom (simple-db/new-instance nil "test-db")))

(def ^:private db-key "de.explorama.backend.expdb.simple-test.sqlite3")

(defn test-setup [test-func]
  (with-redefs [de.explorama.backend.expdb.persistence.backend-simple/db-key db-key]
    (reset! db (simple-db/new-instance nil "test-db"))
    (test-func)
    (fs/rmSync db-key)))
