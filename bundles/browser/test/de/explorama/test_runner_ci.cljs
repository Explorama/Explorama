(ns de.explorama.test-runner-ci
  (:require [de.explorama.frontend.algorithms.components.parameter-test]
            [de.explorama.frontend.algorithms.components.helper-test]
            [de.explorama.frontend.algorithms.operations.redo-test]
            [de.explorama.frontend.data-atlas.db-utils-test]
            [de.explorama.frontend.charts.charts.chartjs-test]
            [de.explorama.frontend.common.i18n-test]
            [de.explorama.shared.indicator.transform-test]
            [de.explorama.frontend.indicator.management-test]
            [de.explorama.frontend.map.operations.redo-test]
            [de.explorama.frontend.map.impl.openlayers.util-test]
            [de.explorama.frontend.mosaic.data-structure.impl-tests]
            [de.explorama.frontend.mosaic.data-structure.data-format-test]
            [de.explorama.frontend.mosaic.data-structure.nested-test]
            [de.explorama.frontend.mosaic.operations.nested-filter-test]
            #_[de.explorama.shared.mosaic.group-by-layout-test]
            [de.explorama.frontend.projects.projects-test]
            [de.explorama.frontend.search.core-test]
            [de.explorama.shared.search.date-utils-test]
            [de.explorama.frontend.ui-base.utils.virtual-test]
            [de.explorama.frontend.ui-base.utils.floating-test]
            [de.explorama.frontend.woco.details-view-test]
            [de.explorama.frontend.woco.notifications-test]
            [de.explorama.frontend.woco.filter-test]
            [de.explorama.frontend.woco.operations-test]
            [de.explorama.shared.interval.validation-test]
            [de.explorama.shared.data-format.core-test]
            [de.explorama.shared.data-format.data-test]
            #_[de.explorama.shared.data-format.aggregations-test]; break the ci - work locally
            [de.explorama.shared.data-format.date-filter-test]
            [de.explorama.shared.common.unification.time-test]
            #_[de.explorama.shared.data-format.filter-test]; break the ci - work locally
            [de.explorama.shared.data-format.operations-indicator-test]
            [de.explorama.shared.data-format.operations-mosaic-test]
            [de.explorama.shared.data-format.operations-test]
            [de.explorama.shared.data-format.simplified-view-test]
            [de.explorama.shared.data-format.standard-filter-test]
            #_[de.explorama.shared.cache.data-tiles.tiling-test]; break the ci - work locally
            #_[de.explorama.shared.cache.data-tiles.cache-test]; break the ci - work locally
            #_[de.explorama.shared.cache.data-tiles.multi-layer-cache-test]; break the ci - work locally
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
            [de.explorama.backend.expdb.db-api-test]
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
            [figwheel.main.async-result :as async-result]
            [figwheel.main.testing :refer [run-tests-async]]
            [goog.object :as gobj]))

(defonce test-results (atom {:test-cases [] :current-ns nil :current-test nil}))
(defonce test-case-counter (atom {}))

(defn- escape-xml [s]
  (when s
    (-> (str s)
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;")
        (str/replace "'" "&apos;"))))

(defn- format-test-case-xml [{:keys [ns name type message expected actual file line]}]
  (let [class-name ns
        key [class-name name]
        count (get @test-case-counter key 0)
        test-name  (str name "-" count)]
    (swap! test-case-counter update key #(if % (inc %) 1))
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
         "<testsuites name=\"Explorama Browser Tests\" tests=\"" test "\" failures=\"" fail "\" errors=\"" error "\" time=\"0\">\n"
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
                :ns (:current-ns @test-results))))

(defmethod report [:cljs.test/default :error] [m]
  (swap! test-results update :test-cases conj
         (assoc m :type :error
                :name (:current-test @test-results)
                :ns (:current-ns @test-results))))

(defmethod report [:cljs.test/default :summary] [m]
  (let [junit-xml (generate-junit-xml m)]
    (println "### report start ###")
    (println junit-xml)
    (println "### report end ###")))

(defmethod report [:cljs.test/default :begin-test-ns] [m]
  (swap! test-results assoc :current-ns (:ns m)))

(defmethod report [:cljs.test/default :begin-test-var] [m]
  (swap! test-results assoc :current-test (:var m)))

(defn- unloaded-namespaces
  "Explorama namespaces the Closure debug loader wrote a script tag for but that
   never ended up defined in the page. Non-empty means the REPL started driving
   the page before its dependency graph finished loading, and the run would
   otherwise die on an opaque \"Cannot read properties of undefined\" deep
   inside an unrelated fixture. Closure's own namespaces are excluded: the
   goog.module ones never attach to the global object."
  []
  (let [loader (gobj/get js/goog "debugLoader_")
        deps (some-> loader (gobj/get "dependencies_"))
        written (some-> loader (gobj/get "written_"))]
    (if (and deps written)
      (into []
            (comp (keep #(gobj/get deps %))
                  (mapcat #(array-seq (gobj/get % "provides" #js [])))
                  (filter #(str/starts-with? % "de.explorama."))
                  ;; goog.getObjectByName reports absence as nil, never undefined
                  (filter #(nil? (js/goog.getObjectByName %))))
            (array-seq (js/Object.keys written)))
      [])))

(def ^:private graph-timeout-ms 60000)
(def ^:private graph-poll-ms 250)

;; The REPL can start driving the page before the debug loader has finished
;; defining every namespace it wrote a script tag for, and which namespaces are
;; still pending varies between runs of the same commit. Waiting for the graph
;; to drain turns that race into a delay; only a graph that never completes is
;; a genuine failure.
(defn- await-dependency-graph [waited-ms on-ready]
  (let [missing (unloaded-namespaces)]
    (cond
      (empty? missing)
      (on-ready)

      (< waited-ms graph-timeout-ms)
      (js/setTimeout #(await-dependency-graph (+ waited-ms graph-poll-ms) on-ready)
                     graph-poll-ms)

      :else
      (let [msg (str "Aborting test run: " (count missing)
                     " namespace(s) were never loaded into the page after "
                     graph-timeout-ms "ms: "
                     (pr-str (vec (take 20 missing))))]
        (println msg)
        (async-result/send
         (async-result/throw-ex
          (ex-info msg {:missing-count (count missing)
                        :missing (vec (take 20 missing))})))))))

;; run-tests-async arranges the async-result send itself, so deferring it is
;; safe as long as -main returns the wait marker up front.
(defn -main [& _args]
  (await-dependency-graph 0 #(run-tests-async 10000))
  [:figwheel.main.async-result/wait (+ graph-timeout-ms 120000)])

