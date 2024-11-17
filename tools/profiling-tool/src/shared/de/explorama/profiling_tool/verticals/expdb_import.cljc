(ns de.explorama.profiling-tool.verticals.expdb-import
  (:require [de.explorama.profiling-tool.resources :refer [load-test-resource]]
            [de.explorama.profiling-tool.benchmark :refer [bench-report
                                                           report->save]]
            [taoensso.tufte :as tufte]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.expdb.ws-api :as ws-api]
            [de.explorama.profiling-tool.data.core :refer [data-a-1k-csv]]
            [clojure.test :refer [deftest testing is]]))

(defn bench-import [name data]
  (tufte/profile {}
   (let [_ (println ["bla"])
         result
         (tufte/p
          :csv-upload
          (frontend-api/request-listener [ws-api/upload-file {:extention "csv" :name (str "test")} (load-test-resource data)]))
         _ (js/console.log "result" result)
         #_#_#_#__ (bench "request expdb import"
                          (send-fn [ws-api/upload-file {:extention "csv" :name (str "test")} (load-test-resource data)])
                          {:num-of-executions 25})
             _ (bench "request expdb commit"
                      (send-fn [ws-api/upload-file {:extention "csv" :name (str "test")} (load-test-resource data)])
                      {:num-of-executions 25})]
     (assoc (bench-report)
            :name name
            :service "expdb-import"))))

(deftest a-test
  (testing (is (= true (bench-import "test" data-a-1k-csv)))))