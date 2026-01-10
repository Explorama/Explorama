(ns de.explorama.test-runner
  (:require [de.explorama.frontend.algorithms.components.parameter-test]
            [de.explorama.frontend.algorithms.components.helper-test]
            [de.explorama.frontend.algorithms.operations.redo-test]
            [de.explorama.frontend.data-atlas.db-utils-test]
            [de.explorama.shared.indicator.transform-test]
            [de.explorama.frontend.indicator.management-test]
            [de.explorama.frontend.map.operations.redo-test]
            [de.explorama.frontend.map.impl.openlayers.util-test]
            [de.explorama.frontend.mosaic.data-structure.impl-tests]
            [de.explorama.frontend.mosaic.data-structure.data-format-test]
            [de.explorama.frontend.mosaic.data-structure.nested-test]
            [de.explorama.frontend.mosaic.operations.nested-filter-test]
            [de.explorama.shared.mosaic.group-by-layout-test]
            [de.explorama.frontend.projects.projects-test]
            [de.explorama.frontend.search.core-test]
            [de.explorama.shared.search.date-utils-test]
            [de.explorama.frontend.woco.details-view-test]
            [de.explorama.frontend.woco.notifications-test]
            [de.explorama.frontend.woco.filter-test]
            [de.explorama.frontend.woco.operations-test]
            [de.explorama.shared.interval.validation-test]
            [de.explorama.shared.data-format.core-test]
            [de.explorama.shared.data-format.data-test]
            [de.explorama.shared.data-format.date-filter-test]
            [de.explorama.shared.data-format.operations-indicator-test]
            [de.explorama.shared.data-format.operations-mosaic-test]
            [de.explorama.shared.data-format.operations-test]
            [de.explorama.shared.data-format.simplified-view-test]
            [de.explorama.shared.data-format.standard-filter-test]
            [de.explorama.backend.algorithms.data.future-data-test]
            [de.explorama.backend.algorithms.data.redo-test]
            [de.explorama.backend.algorithms.data.train-data-test]
            [de.explorama.backend.algorithms.prediction-registry.expdb-backend-test]
            [de.explorama.backend.charts.components.bar-chart-test]
            [de.explorama.backend.charts.components.base-charts-test]
            [de.explorama.backend.charts.components.bubble-chart-test]
            [de.explorama.backend.charts.components.line-chart-test]
            [de.explorama.backend.charts.components.pie-chart-test]
            [de.explorama.backend.charts.components.scatter-chart-test]
            [de.explorama.backend.charts.components.wordcloud-chart-test]
            [de.explorama.backend.charts.error-test]
            [de.explorama.backend.charts.data.redo-test]
            [de.explorama.backend.common.aggregation-test]
            [de.explorama.backend.common.calculations.data-acs-test]
            [de.explorama.backend.common.data.descriptions-test]
            [de.explorama.backend.expdb.ac-api-test]
            [de.explorama.backend.expdb.indexed-db-test]
            [de.explorama.backend.expdb.mapping-test]
            [de.explorama.backend.expdb.suggestions-test]
            [de.explorama.backend.expdb.simple-db-test]
            [de.explorama.backend.indicator.calculate-test]
            [de.explorama.backend.indicator.persistence-test]
            [de.explorama.backend.indicator.sample-test]
            [de.explorama.backend.map.overlayers-test]
            [de.explorama.backend.projects.core-test]
            [de.explorama.backend.projects.direct-search-test]
            [de.explorama.backend.projects.event-log-test]
            [de.explorama.backend.projects.projects-test]
            [de.explorama.backend.projects.queue-test]
            [de.explorama.backend.search.core-test]
            [de.explorama.backend.search.data-tile-test]
            [de.explorama.backend.search.filter-test]
            [de.explorama.backend.table.error-test]
            [de.explorama.backend.table.table-test]
            [de.explorama.backend.algorithms.test-env]
            [de.explorama.backend.expdb.middleware.indexed-db-test]
            [cljs.test :refer [report]]
            [clojure.string :as str]
            [figwheel.main.testing :refer [run-tests-async]]))

(defonce test-results (atom {:test-cases [] :current-ns nil :current-test nil}))

(defn- escape-xml [s]
  (when s
    (-> (str s)
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;")
        (str/replace "'" "&apos;"))))

(defn- format-test-case-xml [{:keys [ns name type message expected actual file line]}]
  (let [test-name name
        class-name ns]
    (str "    <testcase name=\"" (escape-xml test-name) "\" "
         "classname=\"" (escape-xml class-name) "\">\n"
         (case type
           :fail
           (str "      <failure message=\"" (escape-xml (or message "Assertion failed")) "\" "
                "type=\"AssertionError\">"
                (escape-xml
                 (str "Expected: " (pr-str expected) "\n"
                      "  Actual: " (pr-str actual)
                      (when file (str "\nLocation: " file (when line (str ":" line))))))
                "</failure>\n")
           :error
           (str "      <error message=\"" (escape-xml (or message "Test error")) "\" "
                "type=\"Error\">"
                (escape-xml
                 (str (pr-str actual)
                      (when file (str "\nLocation: " file (when line (str ":" line))))))
                "</error>\n")
           "")
         "    </testcase>\n")))

(defn- generate-junit-xml [summary]
  (let [{:keys [test fail error]} summary
        test-cases (:test-cases @test-results)
        grouped-by-ns (group-by :ns test-cases)
        timestamp (.toISOString (js/Date.))]
    (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
         "<testsuites tests=\"" test "\" failures=\"" fail "\" errors=\"" error "\" time=\"0\">\n"
         (apply str
                (for [[ns-name tests] grouped-by-ns]
                  (let [ns-tests (count tests)
                        ns-fail (count (filter #(= :fail (:type %)) tests))
                        ns-error (count (filter #(= :error (:type %)) tests))]
                    (str "  <testsuite name=\"" (escape-xml ns-name) "\" "
                         "tests=\"" ns-tests "\" "
                         "failures=\"" ns-fail "\" "
                         "errors=\"" ns-error "\" "
                         "skipped=\"0\" "
                         "time=\"0\" "
                         "timestamp=\"" timestamp "\">\n"
                         (apply str (map format-test-case-xml tests))
                         "  </testsuite>\n"))))
         "</testsuites>\n")))

(defn- write-junit-xml [xml-content]
  (try
    (let [fs (js/require "fs")
          output-dir "target/test-results"
          output-file (str output-dir "/junit.xml")]
      (.mkdirSync fs output-dir #js {:recursive true})
      (.writeFileSync fs output-file xml-content)
      (println (str "\nJUnit XML report written to: " output-file)))
    (catch :default e
      (println "\nFailed to write JUnit XML file:")
      (println (str "Error: " (.-message e))))))

;; Report methods
(defmethod report [:cljs.test/default :pass] [m]
  (swap! test-results update :test-cases conj
         (assoc m :type :pass
                :name (:current-test @test-results)
                :ns (:current-ns @test-results))))

(defmethod report [:cljs.test/default :fail] [m]
  (swap! test-results update :test-cases conj
         (assoc m :type :fail
                :name (:current-test @test-results)
                :ns (:current-ns @test-results)))
  (let [{:keys [expected actual message file line]} m]
    (println (str "  FAIL: " (or message "Assertion failed")))
    (println (str "    Expected: " (pr-str expected)))
    (println (str "      Actual: " (pr-str actual)))
    (when file
      (println (str "    Location: " file (when line (str ":" line)))))))

(defmethod report [:cljs.test/default :error] [m]
  (swap! test-results update :test-cases conj
         (assoc m :type :error
                :name (:current-test @test-results)
                :ns (:current-ns @test-results)))
  (let [{:keys [actual message file line]} m]
    (println (str "  ERROR: " (or message "Test error")))
    (println (str "    " (pr-str actual)))
    (when file
      (println (str "    Location: " file (when line (str ":" line)))))))

(defmethod report [:cljs.test/default :summary] [m]
  (let [tests (group-by :type (:test-cases @test-results))
        pass (count (:pass tests))
        fail (count (:fail tests))
        error (count (:error tests))
        junit-xml (generate-junit-xml m)]
    (println "\n================================================================================")
    (println "                             TEST SUMMARY")
    (println "================================================================================")
    (println (str "Tests run:  " (+ pass fail error)))
    (println (str "  Passed:   " pass))
    (println (str "  Failed:   " fail))
    (println (str "  Errors:   " error))
    (println "================================================================================")
    (if (and (zero? fail) (zero? error))
      (println "\n✓ ALL TESTS PASSED\n")
      (println (str "\n✗ TESTS FAILED (" (+ fail error) " issues)\n")))
    (write-junit-xml junit-xml)
    {:pass pass :failed fail :error error}))

(defmethod report [:cljs.test/default :begin-test-ns] [m]
  (swap! test-results assoc :current-ns (:ns m))
  (println (str "\nTesting " (name (:ns m)))))

(defmethod report [:cljs.test/default :begin-test-var] [m]
  (swap! test-results assoc :current-test (:var m)))

(defn -main [& _args]
  (try
    (run-tests-async 10000)
    (catch :default e
      (println e))))

