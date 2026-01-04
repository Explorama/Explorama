(ns de.explorama.test.reporter
  (:require [cljs.test :as test]
            [clojure.string :as str]))

(def test-results (atom {:total 0 :passed 0 :failed 0 :error 0 :suites {}}))

(defn- escape-xml [s]
  (when s
    (-> s
        str
        (str/replace "&" "&amp;")
        (str/replace "<" "&lt;")
        (str/replace ">" "&gt;")
        (str/replace "\"" "&quot;")
        (str/replace "'" "&apos;"))))

(defn- format-time [ms]
  (/ ms 1000.0))

(defn generate-junit-xml []
  (let [results @test-results
        total-time (reduce + (map #(get % :time 0) (vals (:suites results))))]
    (str
     "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
     "<testsuites tests=\"" (:total results) "\" "
     "failures=\"" (:failed results) "\" "
     "errors=\"" (:error results) "\" "
     "time=\"" (format-time total-time) "\">\n"
     (str/join "\n"
               (for [[ns-name suite] (:suites results)]
                 (str
                  "  <testsuite name=\"" (escape-xml ns-name) "\" "
                  "tests=\"" (:tests suite) "\" "
                  "failures=\"" (:failures suite) "\" "
                  "errors=\"" (:errors suite) "\" "
                  "time=\"" (format-time (:time suite)) "\">\n"
                  (str/join "\n"
                            (for [test-case (:test-cases suite)]
                              (str
                               "    <testcase name=\"" (escape-xml (:name test-case)) "\" "
                               "classname=\"" (escape-xml (:classname test-case)) "\" "
                               "time=\"" (:time test-case) "\">\n"
                               (when-let [failure (:failure test-case)]
                                 (str "      <failure message=\"" (:message failure) "\" "
                                      "type=\"" (:type failure) "\">"
                                      (:text failure)
                                      "</failure>\n"))
                               (when-let [error (:error test-case)]
                                 (str "      <error message=\"" (:message error) "\" "
                                      "type=\"" (:type error) "\">"
                                      (:text error)
                                      "</error>\n"))
                               "    </testcase>")))
                  "\n  </testsuite>")))
     "\n</testsuites>")))

(defn save-report [filename]
  (when (exists? js/require)
    (let [fs (js/require "fs")]
      (.writeFileSync fs filename (generate-junit-xml))))
  (when-not (exists? js/require)
    (println "\n=== JUnit XML Report ===")
    (println (generate-junit-xml))))

(defmethod test/report :default [m]
  ((get-method test/report (:type m)) m))

(defmethod test/report [:cljs.test/default :begin-test-ns] [m]
  ((get-method test/report [:cljs.test/default :default]) m)
  (swap! test-results assoc-in [:suites (str (:ns m))]
         {:name (str (:ns m))
          :tests 0
          :failures 0
          :errors 0
          :time 0
          :start-time (.now js/Date)
          :test-cases []}))

(defmethod test/report [:junit :begin-test-ns] [m]
  (swap! test-results assoc-in [:suites (str (:ns m))]
         {:name (str (:ns m))
          :tests 0
          :failures 0
          :errors 0
          :time 0
          :start-time (.now js/Date)
          :test-cases []}))

(defmethod test/report [:junit :end-test-ns] [m]
  (let [ns-name (str (:ns m))
        suite (get-in @test-results [:suites ns-name])
        duration (- (.now js/Date) (:start-time suite))]
    (swap! test-results assoc-in [:suites ns-name :time] duration)))

(defmethod test/report [:junit :pass] [m]
  (swap! test-results update :passed inc)
  (swap! test-results update :total inc)
  (let [ns-name (str test/*current-env* (-> test/*testing-vars* first meta :ns))]
    (swap! test-results update-in [:suites ns-name :tests] inc)))

(defmethod test/report [:junit :fail] [m]
  (swap! test-results update :failed inc)
  (swap! test-results update :total inc)
  (let [ns-name (str (-> test/*testing-vars* first meta :ns))
        test-name (-> test/*testing-vars* first meta :name str)
        message (str "Expected: " (pr-str (:expected m)) "\n"
                     "Actual: " (pr-str (:actual m)))
        context (when (seq test/*testing-contexts*)
                  (str/join " " (reverse test/*testing-contexts*)))]
    (swap! test-results update-in [:suites ns-name :tests] inc)
    (swap! test-results update-in [:suites ns-name :failures] inc)
    (swap! test-results update-in [:suites ns-name :test-cases] conj
           {:name test-name
            :classname ns-name
            :time 0
            :failure {:message (escape-xml (or context "Test failed"))
                      :type "failure"
                      :text (escape-xml message)}})))

(defmethod test/report [:junit :error] [m]
  (swap! test-results update :error inc)
  (swap! test-results update :total inc)
  (let [ns-name (str (-> test/*testing-vars* first meta :ns))
        test-name (-> test/*testing-vars* first meta :name str)
        message (str "Error: " (pr-str (:actual m)))]
    (swap! test-results update-in [:suites ns-name :tests] inc)
    (swap! test-results update-in [:suites ns-name :errors] inc)
    (swap! test-results update-in [:suites ns-name :test-cases] conj
           {:name test-name
            :classname ns-name
            :time 0
            :error {:message (escape-xml "Test error")
                    :type "error"
                    :text (escape-xml message)}})))

(defmethod test/report [:junit :summary] [m]
  (println "\n=== Test Summary ===")
  (println "Total:" (:total @test-results))
  (println "Passed:" (:passed @test-results))
  (println "Failed:" (:failed @test-results))
  (println "Errors:" (:error @test-results))
  (save-report "target/test-results/junit.xml")
  (println "\nTest report saved to: target/test-results/junit.xml"))

