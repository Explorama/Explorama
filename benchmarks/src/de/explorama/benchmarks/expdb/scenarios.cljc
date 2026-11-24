(ns de.explorama.benchmarks.expdb.scenarios
  (:require [de.explorama.benchmarks.expdb.data :as data]
            [de.explorama.benchmarks.expdb.harness :as harness]))

(def ^:private bucket "bench")

(defn run-all [{:keys [set+ get+ del+ dump set-dump reset! drop]} opts]
  (let [pairs-100 (data/kv-pairs 1 100 200)
        pairs-1k (data/kv-pairs 2 1000 200)
        pairs-10k (data/kv-pairs 3 10000 200)
        pairs-large (data/kv-pairs 4 1000 5000)
        keys-10k (mapv first pairs-10k)
        keys-large (mapv first pairs-large)
        run (fn [spec] (harness/run-scenario spec opts))
        preload-10k! (fn [] (reset!) (set-dump bucket pairs-10k))]
    (merge
     {:write-batch-100 (run {:key :write-batch-100 :setup! reset!
                             :run! #(set+ bucket pairs-100)})
      :write-batch-1k (run {:key :write-batch-1k :setup! reset!
                            :run! #(set+ bucket pairs-1k)})
      :write-batch-10k (run {:key :write-batch-10k :setup! reset!
                             :run! #(set+ bucket pairs-10k)})
      :write-large-1k (run {:key :write-large-1k :setup! reset!
                            :run! #(set+ bucket pairs-large)})
      :set-dump-10k (run {:key :set-dump-10k :setup! reset!
                          :run! #(set-dump bucket pairs-10k)})
      :delete-batch-1k (run {:key :delete-batch-1k :setup! preload-10k!
                             :run! #(del+ bucket (subvec keys-10k 0 1000))})
      :drop-table-10k (run {:key :drop-table-10k :setup! preload-10k!
                            :run! #(drop bucket)})}
     (do (preload-10k!)
         {:read-point-100 (run {:key :read-point-100
                                :run! (fn []
                                        (doseq [k (subvec keys-10k 0 100)]
                                          (harness/check-failure! :read-point-100
                                                                  (get+ bucket [k]))))})
          :read-batch-100 (run {:key :read-batch-100
                                :run! #(get+ bucket (subvec keys-10k 0 100))})
          :read-batch-1k (run {:key :read-batch-1k
                               :run! #(get+ bucket (subvec keys-10k 0 1000))})
          :dump-all-10k (run {:key :dump-all-10k
                              :run! #(dump bucket)})})
     (do (reset!)
         (set-dump bucket pairs-large)
         (let [res {:read-large-1k (run {:key :read-large-1k
                                         :run! #(get+ bucket keys-large)})}]
           (reset!)
           res)))))
