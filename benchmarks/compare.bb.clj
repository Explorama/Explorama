(require '[clojure.edn :as edn])

(defn- fail [msg]
  (println msg)
  (System/exit 1))

(let [[before-file after-file] *command-line-args*]
  (when-not (and before-file after-file)
    (fail "usage: bb compare.bb.clj <before.edn> <after.edn>"))
  (let [read-results (fn [f]
                       (try (edn/read-string (slurp f))
                            (catch Exception e
                              (fail (str "cannot read " f ": " (ex-message e))))))
        before (read-results before-file)
        after (read-results after-file)]
    (when (not= (:bundle before) (:bundle after))
      (println "WARNING: comparing different bundles:"
               (:bundle before) "vs" (:bundle after)))
    (let [b (:scenarios before)
          a (:scenarios after)
          missing (sort (remove #(and (contains? b %) (contains? a %))
                                (distinct (concat (keys b) (keys a)))))
          common (sort (filter #(contains? a %) (keys b)))]
      (when (seq missing)
        (println "WARNING: scenarios not present in both files:" (vec missing)))
      (println (format "%-22s %18s %18s %9s"
                       "scenario"
                       (str "before ms (" (name (:backend before)) ")")
                       (str "after ms (" (name (:backend after)) ")")
                       "speedup"))
      (doseq [k common]
        (let [bm (get-in b [k :ms :median])
              am (get-in a [k :ms :median])]
          (println (format "%-22s %18.2f %18.2f %8.1fx"
                           (name k) (double bm) (double am)
                           (double (/ bm am)))))))))
