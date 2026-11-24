#!/usr/bin/env bb
(require '[clojure.edn :as edn])

;; All four reports gate the build on errors; warnings stay advisory.
(def ^:private checks
  [["Browser" "browser_check.edn"]
   ["Electron" "electron_check.edn"]
   ["Server" "server_check.edn"]
   ["Plugins" "plugins_check.edn"]])

(let [counts (keep (fn [[label file]]
                     (let [n (get-in (edn/read-string (slurp file))
                                     [:summary :error]
                                     0)]
                       (when (pos? n)
                         [label n])))
                   checks)]
  (if (seq counts)
    (do
      (doseq [[label n] counts]
        (println (format "%s: %d clj-kondo error(s)" label n)))
      (println)
      (println "clj-kondo errors fail the build. Warnings stay advisory -")
      (println "see the job summary for the full report.")
      (System/exit 1))
    (println "No clj-kondo errors.")))
