(ns de.explorama.test-runner
  (:require [cljs.test :refer-macros [run-tests]]
            [de.explorama.backend.algorithms.test-env]
            [de.explorama.backend.expdb.middleware.indexed-db-test]
            [de.explorama.backend.expdb.middleware.simple-db-test]))

(defn ^:export run []
  (run-tests
   'de.explorama.backend.algorithms.test-env
   'de.explorama.backend.expdb.middleware.indexed-db-test
   'de.explorama.backend.expdb.middleware.simple-db-test))

;; Auto-run tests when loaded
(run)
