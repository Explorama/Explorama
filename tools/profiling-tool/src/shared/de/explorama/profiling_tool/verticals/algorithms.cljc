(ns de.explorama.profiling-tool.verticals.algorithms)

#_

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->file (benchmark-training-data))
         (report->file (benchmark-predict))
         (report->file (benchmark-prediction-di-data)))
     (report->file
      (benchmark-all benchmark-training-data
                     benchmark-predict
                     benchmark-prediction-di-data)))))
