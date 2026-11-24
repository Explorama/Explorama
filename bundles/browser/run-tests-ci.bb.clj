(require '[babashka.process :refer [shell]]
         '[clojure.string :as str])

;; :continue so a crashed run still yields its captured output: the JUnit block
;; is written when present and the browser-side diagnostics are echoed, instead
;; of both being discarded by a thrown non-zero exit.
(let [{:keys [out exit]} (shell {:out :string :continue true} "clj -M:test-ci")
      report (loop [lines (str/split (or out "") #"\n")
                    result ""
                    toggle false]
               (if (empty? lines)
                 result
                 (cond (= "### report start ###" (first lines))
                       (recur (rest lines) result true)
                       (= "### report end ###" (first lines))
                       (recur (rest lines) result false)
                       toggle
                       (recur (rest lines) (str result "\n" (first lines)) toggle)
                       :else
                       (recur (rest lines) result toggle))))]
  (when (seq (str/trim report))
    (spit "report.xml" report :encoding "UTF-8"))
  (when-not (zero? exit)
    (println (or out ""))
    (System/exit exit)))
