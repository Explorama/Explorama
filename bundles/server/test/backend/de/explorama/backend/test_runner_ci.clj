(ns de.explorama.backend.test-runner-ci
  (:require [clojure.test :as test]
            [clojure.test.junit :refer [with-junit-output]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.namespace.find :as find])
  (:import [java.io StringWriter]))

(defn -main [& _args]
  (let [test-dirs ["test/backend" "../../plugins/backend_test" "../../plugins/shared_test"]
        test-nses (->> test-dirs
                       (map io/file)
                       (mapcat #(find/find-namespaces-in-dir % find/clj))
                       (filter #(re-matches #".*-test$" (name %))))]
    (doseq [ns test-nses]
      (require ns))
    (let [sw (StringWriter.)
          summary (binding [test/*test-out* sw]
                    (with-junit-output
                      (apply test/run-tests test-nses)))
          xml-output (str sw)
          modified-xml (str/replace xml-output
                                    "<testsuites>"
                                    "<testsuites name=\"Explorama Server Tests\">")]
      (spit "server-report.xml" modified-xml)
      (System/exit (if (test/successful? summary) 0 1)))))

