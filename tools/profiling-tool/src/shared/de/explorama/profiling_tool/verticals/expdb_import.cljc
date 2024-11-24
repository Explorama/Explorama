(ns de.explorama.profiling-tool.verticals.expdb-import
  (:require
   [clojure.test :refer [deftest is testing]]
   [de.explorama.backend.frontend-api :as frontend-api]
   [de.explorama.profiling-tool.benchmark :refer [bench-report]]
   [de.explorama.profiling-tool.data :refer [data-a-1k-csv]]
   [de.explorama.profiling-tool.resources :refer [load-test-resource]]
   [de.explorama.shared.expdb.ws-api :as ws-api]
   [taoensso.tufte :as tufte]
   [taoensso.timbre :refer [info]]))

(defn bench-import [name data]
  (info "benchmarking" name)
  (tufte/profile {}
                 (let [_ (info ["bla"])
                       result
                       (tufte/p
                        :csv-upload
                        (frontend-api/request-listener [ws-api/upload-file {:extention "csv" :name (str "test")} (load-test-resource data)]))
                       _ (info "result" result)
                       #_#_#_#__ (bench "request expdb import"
                                        (send-fn [ws-api/upload-file {:extention "csv" :name (str "test")} (load-test-resource data)])
                                        {:num-of-executions 25})
                           _ (bench "request expdb commit"
                                    (send-fn [ws-api/upload-file {:extention "csv" :name (str "test")} (load-test-resource data)])
                                    {:num-of-executions 25})]
                   (assoc (bench-report)
                          :name name
                          :service "expdb-import")))
  (info "benchmarked" name)
  true)

(bench-import "test" data-a-1k-csv)
(deftest a-test
  (testing "Benchmarking expdb-import" (is (= true (bench-import "test" data-a-1k-csv)))))