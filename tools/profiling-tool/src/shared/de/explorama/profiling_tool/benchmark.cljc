(ns de.explorama.profiling-tool.benchmark
  (:require [clojure.string :as str]
            [taoensso.timbre :refer [info]]
            [taoensso.tufte :as tufte]
            [de.explorama.profiling-tool.resources :refer [save-test-result]]))

(defonce stats-accumulator (tufte/add-accumulating-handler! {:ns-pattern "*"}))

(defn bench-report []
  (let [m (loop [a @stats-accumulator]
            (if (empty? a) (recur @stats-accumulator) a))
        group-keys (keys m)]
    (assoc {}
           :results (reduce (fn [acc group-id]
                              (assoc acc group-id @(get m group-id)))
                            {}
                            group-keys)
           :formatted-string (tufte/format-grouped-pstats m))))

(defn report->save [{:keys [name service formatted-string]
                     {release-version "release"} :release-infos
                     :as bench-report}]
  (info "Saving benchmark reports.")
  (let [base-filename (if name
                        (str/replace name #" " "_")
                        service)
        formatted-string-path (if name
                                ["reports" "formatted-string" release-version service base-filename]
                                ["reports" "formatted-string" release-version base-filename])
        complete-report-path (if name
                               ["reports" "complete-report" release-version service base-filename]
                               ["reports" "complete-report" release-version base-filename])]
    (save-test-result formatted-string-path formatted-string)
    (save-test-result complete-report-path bench-report)))
