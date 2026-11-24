(ns de.explorama.benchmarks.expdb.runner
  (:require [de.explorama.backend.expdb.persistence.common-sqlite :as sqlite]
            [de.explorama.benchmarks.expdb.e2e :as e2e]
            [de.explorama.benchmarks.expdb.harness :as harness]
            [de.explorama.benchmarks.expdb.scenarios :as scenarios]))

(def ^:private node-fs (js/require "fs"))

(def ^:private scratch-db "target/bench-kv.sqlite3")
(def ^:private results-dir "../../../benchmarks/results")

(def ^:private storage
  {:set+ (partial sqlite/db-set+ scratch-db)
   :get+ (partial sqlite/db-get+ scratch-db)
   :del+ (partial sqlite/db-del+ scratch-db)
   :dump (partial sqlite/dump scratch-db)
   :set-dump (partial sqlite/set-dump scratch-db)
   :drop (partial sqlite/db-drop-table scratch-db)
   :reset! (fn []
             (.rmSync node-fs scratch-db #js{:force true})
             (.rmSync node-fs (str scratch-db "-wal") #js{:force true})
             (.rmSync node-fs (str scratch-db "-shm") #js{:force true}))})

(defn -main [& _]
  (harness/assert-deterministic!)
  (.mkdirSync node-fs "target" #js{:recursive true})
  (let [results {:timestamp (harness/timestamp)
                 :git-sha (harness/git-sha)
                 :bundle :electron
                 :backend :sqlite
                 :scenarios (merge (scenarios/run-all storage {:warmup 1 :iterations 5})
                                   (e2e/run-all {}))}]
    ((:reset! storage))
    (harness/print-report results)
    (println "results written to" (harness/write-results! results-dir results))
    (js/process.exit 0)))
