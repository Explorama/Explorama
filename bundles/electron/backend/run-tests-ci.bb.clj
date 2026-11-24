(require '[babashka.process :as p]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

;; Streams `clj -M:test-ci` output, captures the JUnit block between the
;; runner's markers into report.xml, then destroys the process tree: after
;; -main returns, cljs.main lingers in a REPL that never exits on its own
;; (see issue #6), so waiting for process exit would hang forever. The exit
;; code is derived from the report's failure/error counts.
(let [proc (p/process ["clj" "-M:test-ci"] {:err :inherit :out :stream})
      rdr (io/reader (:out proc))]
  (loop [captured nil]
    (if-let [line (.readLine rdr)]
      (cond
        (= line "### report start ###") (recur [])
        (= line "### report end ###")
        (let [report (str/join "\n" captured)]
          (spit "report.xml" report :encoding "UTF-8")
          (p/destroy-tree proc)
          (let [[_ f e] (re-find #"failures=\"(\d+)\" errors=\"(\d+)\"" report)
                ok? (and (= "0" f) (= "0" e))]
            (println (str "report.xml written; failures=" f " errors=" e))
            (System/exit (if ok? 0 1))))
        (some? captured) (recur (conj captured line))
        :else (do (println line) (recur captured)))
      (do (println "test run ended without a report block")
          (System/exit 1)))))
