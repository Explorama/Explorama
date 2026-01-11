(ns de.explorama.frontend.test-runner-ci
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
            [cljs.test :refer [report]]
            [clojure.string :as str]
            [figwheel.main.testing :refer [run-tests-async]]))

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
         "<testsuites name=\"Explorama Server Frontend Tests\" tests=\"" test "\" failures=\"" fail "\" errors=\"" error "\" time=\"0\">\n"
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

(defn -main [& _args]
  (try
    (run-tests-async 10000)
    (catch :default e
      (println e))))

