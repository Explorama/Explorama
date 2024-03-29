(ns de.explorama.backend.expdb.middleware.simple-db-test
  (:require [clojure.java.io :as io]
            [de.explorama.backend.expdb.persistence.backend-simple :as simple-db]))

(def db (atom (simple-db/new-instance nil "test-db")))

(def ^:private db-key "de.explorama.backend.expdb.simple-test.sqlite3")

(defn test-setup [test-func]
  (with-redefs [de.explorama.backend.expdb.persistence.backend-simple/db-key db-key]
    (reset! db (simple-db/new-instance nil "test-db"))
    (test-func)
    (io/delete-file db-key)))
