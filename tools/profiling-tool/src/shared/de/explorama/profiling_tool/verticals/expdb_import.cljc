(ns de.explorama.profiling-tool.verticals.expdb-import
  (:require [de.explorama.backend.expdb.middleware.indexed :as mwi]
            [de.explorama.profiling-tool.resources :refer [load-test-resource preload-resources]]
            [de.explorama.profiling-tool.env :refer [bench-bucket]]
            [de.explorama.profiling-tool.benchmark :refer [bench
                                                           bench-report
                                                           benchmark-all report->save]]
            [de.explorama.profiling-tool.data.core :refer [data-a-1k data-a-10k
                                                           data-a-100k data-a-1m
                                                           data-b-1k data-b-10k
                                                           data-b-100k data-b-1m
                                                           data-a-ds-id data-b-ds-id]]
            [taoensso.timbre :refer [info]]
            [taoensso.tufte :as tufte]))

(preload-resources data-a-1k data-a-10k
                   data-a-100k data-a-1m
                   data-b-1k data-b-10k
                   data-b-100k data-b-1m
                   data-a-ds-id data-b-ds-id)

(defn- import-one-file [file]
  (let [data (load-test-resource file)]
    (try
      (let [{:keys [success]}
            (tufte/p
             :import-one-file
             (mwi/import-data data
                              {}
                              (name bench-bucket)))]
        success)
      (catch #?(:clj Exception :cljs :default) e
        (throw (ex-info (str "Failed to import file")
                        {:msg (ex-message e)
                         :exception e}))))))

(defn- import-ds [ds-files]
  (tufte/p
   :import-ds-files
   (doseq [ds-file (if (string? ds-files)
                     [ds-files]
                     ds-files)]
     (import-one-file ds-file))))

(defn- get-data-sources []
  (tufte/p
   :get-data-sources
   (mwi/get-datasources (name bench-bucket))))

(defn- delete-guard []
  (pos? (count (get-data-sources))))

(defn- delete-data-source [datasource-id]
  (when (delete-guard)
    (try
      (let [{{:keys [success]} :body} ;data
            (tufte/p
             :delete-ds
             (mwi/delete-data-source (name bench-bucket) datasource-id))]
        success)
      (catch #?(:clj Exception :cljs :default) e
        (throw (ex-info "Deletion failed." {:msg (ex-message e)
                                            :exception e}))))))

(defn- delete-all []
  (when (delete-guard)
    (try
      (let [{{:keys [success]} :body} ;data
            (tufte/p
             :delete-bucket
             (mwi/delete-all (name bench-bucket)))]
        success)
      (catch #?(:clj Exception :cljs :default) e
        (throw (ex-info "Deletion failed. Exception:"
                        {}
                        e))))))

(defn benchmark-import-empty-bucket []
  (delete-all) ; Make sure nothing is imported 
  (let [bench-1k-data-a "Empty graph, 1k data-a, import"
        bench-10k-data-a "Empty graph, 10k data-a, import"
        bench-100k-data-a "Empty graph, 100k data-a, import"
        bench-1m-data-a "Empty graph, 1m data-a, import"
        bench-1k-data-b "Empty graph, 1k data-b, import"
        bench-10k-data-b "Empty graph, 10k data-b, import"
        bench-100k-data-b "Empty graph, 100k data-b, import"
        bench-1m-data-b "Empty graph, 1m data-b, import"]
    (bench bench-1k-data-a
           [data-a-1k]
           (partial import-ds data-a-1k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-10k-data-a
           [data-a-10k]
           (partial import-ds data-a-10k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-100k-data-a
           [data-a-100k]
           (partial import-ds data-a-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-1m-data-a
           [data-a-1m]
           (partial import-ds data-a-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})

    (bench bench-1k-data-b
           [data-b-1k]
           (partial import-ds data-b-1k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-10k-data-b
           [data-b-10k]
           (partial import-ds data-b-10k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-100k-data-b
           [data-b-100k]
           (partial import-ds data-b-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-1m-data-b
           [data-a-1k data-b-1m]
           (partial import-ds data-b-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :print-run-progress? true})

    (info "Create benchmark report for import into empty bucket.")

    (assoc (bench-report)
           :name "import into empty graph"
           :service "importer")))

(defn benchmark-update-imported-ds
  "There is no guarentee that each ds size increase has completly new events.
   Which is why i always go to a bigger collection so i know atleast some have to be new."
  []
  (delete-all)
  (let [bench-1k-10k-data-a "1k data-a, 10k data-a, update"
        bench-1k-1m-data-a "1k data-a, 1m data-a, update"
        bench-10k-100k-data-a "10k data-a, 100k data-a, update"
        bench-10k-1m-data-a "10k data-a, 1m data-a, update"

        bench-1k-10k-data-b "1k data-b, 10k data-b, update"
        bench-1k-1m-data-b "1k data-b, 1m data-b, update"
        bench-10k-100k-data-b "10k data-b, 100k data-b, update"
        bench-10k-1m-data-b "10k data-b, 1m data-b, update"]

    (bench bench-1k-10k-data-a
           [data-a-1k data-a-10k]
           (partial import-ds data-a-10k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-1k)
            :print-run-progress? true})
    (bench bench-1k-1m-data-a
           [data-a-1k data-a-1m]
           (partial import-ds data-a-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-1k)
            :print-run-progress? true})
    (bench bench-10k-100k-data-a
           [data-a-10k data-a-100k]
           (partial import-ds data-a-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-10k)
            :print-run-progress? true})
    (bench bench-10k-1m-data-a
           [data-a-10k data-a-1m]
           (partial import-ds data-a-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-10k)
            :print-run-progress? true})

    (bench bench-1k-10k-data-b
           [data-b-1k data-b-10k]
           (partial import-ds data-b-10k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-1k)
            :print-run-progress? true})
    (bench bench-1k-1m-data-b
           [data-b-1k data-b-1m]
           (partial import-ds data-b-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-1k)
            :print-run-progress? true})
    (bench bench-10k-100k-data-b
           [data-b-10k data-b-100k]
           (partial import-ds data-b-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-10k)
            :print-run-progress? true})
    (bench bench-10k-1m-data-b
           [data-b-10k data-b-1m]
           (partial import-ds data-b-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-10k)
            :print-run-progress? true})

    (info "Create benchmark report for updating existing ds.")

    (assoc (bench-report)
           :name "update ds graph"
           :service "importer")))

(defn benchmark-import-extra-ds []
  (delete-all)
  (let [bench-1k-data-a-1k-data-b "1k data-a, 1k data-b, add ds"
        bench-1k-data-a-100k-data-b "1k data-a, 100k data-b, add ds"
        bench-100k-data-a-1k-data-b "100k data-a, 1k data-b, add ds"
        bench-100k-data-a-100k-data-b "100k data-a, 100k data-b, add ds"
        bench-100k-data-a-1m-data-b "100k data-a, 1m data-b, add ds"

        bench-1k-data-b-1k-data-a "1k data-b, 1k data-a, add ds"
        bench-1k-data-b-100k-data-a "1k data-b, 100k data-a, add ds"
        bench-100k-data-b-1k-data-a "100k data-b, 1k data-a, add ds"
        bench-100k-data-b-100k-data-a "100k data-b, 100k data-a, add ds"
        bench-100k-data-b-1m-data-a "100k data-b, 1m data-a, add ds"]
    (bench bench-1k-data-a-1k-data-b
           [data-a-1k data-b-1k]
           (partial import-ds data-b-1k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-1k)
            :print-run-progress? true})
    (bench bench-1k-data-a-100k-data-b
           [data-a-1k data-b-100k]
           (partial import-ds data-b-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-1k)
            :print-run-progress? true})
    (bench bench-100k-data-a-1k-data-b
           [data-a-100k data-b-1k]
           (partial import-ds data-b-1k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-100k)
            :print-run-progress? true})
    (bench bench-100k-data-a-100k-data-b
           [data-b-100k data-a-100k]
           (partial import-ds data-b-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-100k)
            :print-run-progress? true})
    (bench bench-100k-data-a-1m-data-b
           [data-b-1m data-a-100k]
           (partial import-ds data-b-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-a-100k)
            :print-run-progress? true})

    (bench bench-1k-data-b-1k-data-a
           [data-a-1k data-b-1k]
           (partial import-ds data-a-1k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-1k)
            :print-run-progress? true})
    (bench bench-1k-data-b-100k-data-a
           [data-a-100k data-b-1k]
           (partial import-ds data-a-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-1k)
            :print-run-progress? true})
    (bench bench-100k-data-b-1k-data-a
           [data-a-1k data-b-100k]
           (partial import-ds data-a-1k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-100k)
            :print-run-progress? true})
    (bench bench-100k-data-b-100k-data-a
           [data-a-100k data-b-100k]
           (partial import-ds data-a-100k)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-100k)
            :print-run-progress? true})
    (bench bench-100k-data-b-1m-data-a
           [data-a-1m data-b-100k]
           (partial import-ds data-a-1m)
           {:num-of-executions 2
            :cleanup-fn delete-all
            :preexecute-fn (partial import-ds data-b-100k)
            :print-run-progress? true})

    (info "Create benchmark report for adding new ds.")

    (assoc (bench-report)
           :name "add ds to existing graph"
           :service "importer")))

(defn benchmark-drop-one-ds []
  (delete-all)
  (let [bench-1k-data-a-1k-data-b "1k data-a, 1k data-b, delete data-a"
        bench-100k-data-a-100k-data-b "100k data-a, 100k data-b, delete data-a"
        bench-100k-data-a-1k-data-b "100k data-a, 1k data-b, delete data-a"
        bench-1m-data-a-100k-data-b "1m data-a, 100k data-b, delete data-a"

        bench-1k-data-b-1k-data-a "1k data-b, 1k data-a, delete data-b"
        bench-100k-data-b-100k-data-a "100k data-b, 100k data-a, delete data-b"
        bench-100k-data-b-1k-data-a "100k data-b, 1k data-a, delete data-b"
        bench-1m-data-b-100k-data-a "1m data-b, 100k data-a, delete data-b"]
    (bench bench-1k-data-a-1k-data-b
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-a-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-a-1k)
                             (import-ds data-b-1k))
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-100k-data-a-100k-data-b
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-a-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-a-100k)
                             (import-ds data-b-100k))
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-100k-data-a-1k-data-b
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-a-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-a-100k)
                             (import-ds data-b-1k))
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-1m-data-a-100k-data-b
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-a-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-a-1m)
                             (import-ds data-b-100k))
            :cleanup-fn delete-all
            :print-run-progress? true})

    (bench bench-1k-data-b-1k-data-a
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-b-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-b-1k)
                             (import-ds data-a-1k))
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-100k-data-b-100k-data-a
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-b-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-b-100k)
                             (import-ds data-a-100k))
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-100k-data-b-1k-data-a
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-b-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-b-100k)
                             (import-ds data-a-1k))
            :cleanup-fn delete-all
            :print-run-progress? true})
    (bench bench-1m-data-b-100k-data-a
           [data-a-ds-id data-a-100k data-b-1k]
           (partial delete-data-source data-b-ds-id)
           {:num-of-executions 2
            :preexecute-fn (fn []
                             (import-ds data-b-1m)
                             (import-ds data-a-100k))
            :cleanup-fn delete-all
            :print-run-progress? true})

    (info "Create benchmark report for deleting one ds.")
    #_
    (assoc (bench-report)
           :name "deleting one ds from graph"
           :service "importer")))

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save benchmark-import-empty-bucket)
         (report->save benchmark-update-imported-ds)
         (report->save benchmark-import-extra-ds)
         (report->save benchmark-drop-one-ds))
     (report->save
      (benchmark-all benchmark-import-empty-bucket
                     benchmark-update-imported-ds
                     benchmark-import-extra-ds
                     benchmark-drop-one-ds)))))
