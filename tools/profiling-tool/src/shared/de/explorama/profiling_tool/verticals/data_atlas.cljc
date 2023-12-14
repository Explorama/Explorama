(ns de.explorama.profiling-tool.verticals.data-atlas)

#_

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (report->save (benchmark-data-elements))
     (report->save
      (benchmark-all benchmark-data-elements)))))
