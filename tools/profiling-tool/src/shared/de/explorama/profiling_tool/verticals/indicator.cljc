(ns de.explorama.profiling-tool.verticals.indicator)

#_

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save (benchmark-connect-to-di))
         (report->save (benchmark-create-delete))
         (report->save (benchmark-create-di))
         (report->save (benchmark-indicator-data)))
     (report->save
      (benchmark-all benchmark-connect-to-di
                     benchmark-create-delete
                     benchmark-create-di
                     benchmark-indicator-data)))))
