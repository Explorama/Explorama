(ns de.explorama.benchmarks.expdb.e2e
  (:require [de.explorama.backend.common.middleware.cache :as idb-cache]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.cache :as ac-cache]
            [de.explorama.backend.expdb.legacy.search.data-tile-ref :as dt-api]
            [de.explorama.backend.expdb.persistence.backend-indexed :as backend-indexed]
            [de.explorama.backend.expdb.persistence.indexed :as indexed]
            [de.explorama.backend.expdb.persistence.shared :as imp]
            [de.explorama.backend.expdb.query.index :as index]
            [de.explorama.benchmarks.expdb.data :as data]
            [de.explorama.benchmarks.expdb.harness :as harness]
            #?(:clj [clojure.java.io :as io])))

#?(:cljs (def ^:private node-fs (js/require "fs")))

(def ^:private config
  {:backend "browser"
   :indexed? true
   :bucket "default"
   :schema "default"
   :data-tile-keys {"year" {:field ["Date" "date" "value"]
                            :date-part :year
                            :type :string}
                    "country" {:field ["Context" "country" "name"]
                               :type :string}
                    "datasource" {:field ["Datasource" "datasource" "name"]}
                    "bucket" {:field :bucket
                              :type :string}
                    "identifier" {:value "search"}}})

(def ^:private scratch-db "target/bench-e2e-db")

#?(:clj
   (defn- delete-recursively! [f]
     (let [f (io/file f)]
       (when (.exists f)
         (doseq [child (reverse (file-seq f))]
           (io/delete-file child true))))))

(defn- delete-scratch! []
  #?(:clj (do (delete-recursively! scratch-db)
              (io/delete-file (str scratch-db "-wal") true)
              (io/delete-file (str scratch-db "-shm") true))
     :cljs (do (.rmSync node-fs scratch-db #js{:recursive true :force true})
               (.rmSync node-fs (str scratch-db "-wal") #js{:recursive true :force true})
               (.rmSync node-fs (str scratch-db "-shm") #js{:recursive true :force true}))))

(defn- single [ms]
  {:iterations 1
   :ms {:min ms :median ms :mean ms :max ms}})

(defn run-all [{:keys [years countries events-per-tile]
                :or {years 10 countries 20 events-per-tile 100}}]
  (with-redefs [de.explorama.backend.expdb.persistence.backend-indexed/db-key scratch-db]
    (delete-scratch!)
    (let [db (backend-indexed/new-instance config)
          {:keys [payload dt-keys]} (data/import-payload 7 years countries events-per-tile)]
      (dt-api/reset-cache)
      (ac-cache/new-ac-cache)
      (idb-cache/reset-states)
      (let [[new-index new-index-inv new-dt-key-index] (indexed/get-index db)]
        (swap! index/expdb-hash->dt-key assoc (indexed/schema db) new-dt-key-index)
        (swap! index/current assoc (indexed/schema db) new-index)
        (swap! index/current-inv assoc (indexed/schema db) new-index-inv))
      (let [t0 (harness/now-ms)
            import-result (imp/transform->import payload {} "default")
            import-ms (- (harness/now-ms) t0)]
        (harness/check-failure! :e2e-import import-result)
        (let [t1 (harness/now-ms)
              _ (doseq [batch (partition-all 50 dt-keys)]
                  (harness/check-failure! :e2e-query
                                          (indexed/data-tiles db (vec batch))))
              query-ms (- (harness/now-ms) t1)
              t2 (harness/now-ms)
              dump-result (indexed/dump db)
              dump-ms (- (harness/now-ms) t2)]
          (harness/check-failure! :e2e-dump dump-result)
          (delete-scratch!)
          {:e2e-import (single import-ms)
           :e2e-query (single query-ms)
           :e2e-dump (single dump-ms)})))))
