(ns de.explorama.profiling-tool.benchmark
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]
            [de.explorama.profiling-tool.config :as config]
            [de.explorama.profiling-tool.env :refer [get-software-version]]
            [taoensso.timbre :refer [info]]
            [taoensso.tufte :as tufte]
            [de.explorama.profiling-tool.resources :refer [save-test-result resource-available?]]
            [de.explorama.profiling-tool.env :refer [thread-wait force-gc]]))

(defonce stats-accumulator (tufte/add-accumulating-handler! {:ns-pattern "*"}))

(defn bench-report
  "Generate a map with all benchmark ran from the last report till now.
   
   Contains for each ran benchmark the raw stats.
   Also has one :formatted-string which is human-readable."
  ([]
   (bench-report true))
  ([thread-sleep?]
   (when thread-sleep?
     (thread-wait config/bench-report-wait))
   (let [m (loop [a @stats-accumulator]
             (if (empty? a) (recur @stats-accumulator) a))
         group-keys (keys m)]
     (assoc {}
            :results (reduce (fn [acc group-id]
                               (assoc acc group-id @(get m group-id)))
                             {}
                             group-keys)
            :formatted-string (tufte/format-grouped-pstats m)
            :release-infos (get-software-version)))))

(defn bench
  "ID => Bench id or name, grouping the profile results by it
   
   fn-to-execute => function that can be executed without any args
   
   opts => option map with the keys (num-of-executions, create-report?)
   
   :preexecute-fn => fn to prepare for the actual benchmark, like importing or creating something (not profiled for the bench result)
   :cleanup-fn => fn which gets execute after each run (not profiled for the bench result)
   :num-of-executions => defaults to 50, means the fn gets executed 50 times
   :create-report? => directly returns a report map, otherwise use (bench-report)
   :print-run-progress? => print what number of run is currently running"
  ([id resource-ids fn-to-execute]
   (bench id resource-ids fn-to-execute {}))
  ([id resource-ids fn-to-execute {:keys [num-of-executions create-report?
                                          cleanup-fn preexecute-fn print-run-progress?]
                                   :or {num-of-executions 50}}]
   #?(:cljs (js/console.log "resource-ids" resource-ids))
   (if (every? resource-available? resource-ids)
     (do
       (info "Start benchmark with" num-of-executions "runs for" id)
       (tufte/profile
        {:id id}
        (doseq [run-index (range num-of-executions)]
          (let [run-number (inc run-index)]
            (when (fn? preexecute-fn)
              (when print-run-progress?
                (info "Executing pre-execute fn for run" run-number))
              (tufte/profile
               {:id (str id ", preexecute-fn")}
               (preexecute-fn)))
            (force-gc)
            (when print-run-progress?
              (info "Executing benchmark fn for run" run-number))
            (tufte/p
             :fn-execute
             (fn-to-execute))
            (when (fn? cleanup-fn)
              (when print-run-progress?
                (info "Executing cleanup fn for run" run-number))
              (tufte/profile
               {:id (str id ", cleanup-fn")}
               (cleanup-fn))))))
       (info "Finished benchmark for" id)
       (when create-report?
         (info "Create benchmark report")
         (bench-report false)))
     (do
       (info "Create skipping" id)
       #_(bench-report false)))))

(defn benchmark-all
  "Executes every given benchmark-fn and merges them to one result map.
   Each benchmark-fn needs to return a map and the :formatted-string key are getting merged by str newline.
   Any duplicate keys are getting merged by the same rule as merge (second map wins)."
  [& benchmark-fns]
  (dissoc (apply merge-with
                 (fn [v1 v2]
                   (if (and (string? v1)
                            (str/includes? v1 "pId"))
                     (str v1 "\n" v2)
                     v2))
                 (mapv (fn [benchmark-fn]
                         (benchmark-fn))
                       benchmark-fns))
          :name))

(defn report->save [{:keys [name service formatted-string]
                     {release-version "release"} :release-infos
                     :as bench-report}]
  (info "Saving benchmark reports.")
  (let [base-filename (if name
                        (str/replace name #" " "_")
                        service)
        formatted-string-path (if name
                                ["reports" "formatted-string" release-version service base-filename]
                                ["reports" "formatted-string" release-version base-filename])
        complete-report-path (if name
                               ["reports" "complete-report" release-version service base-filename]
                               ["reports" "complete-report" release-version base-filename])]
    (save-test-result formatted-string-path formatted-string)
    (save-test-result complete-report-path bench-report)))
