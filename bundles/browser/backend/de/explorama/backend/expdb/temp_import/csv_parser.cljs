(ns de.explorama.backend.expdb.temp-import.csv-parser
  (:require [cljsjs.papaparse]))

(def ^:private default-desc {:separator ","
                             :quote "\""
                             :limit 200})

(defn parse [{{csv :csv} :meta-data} result]
  (let [{:keys [separator quote]} (or csv default-desc)]
    ;TODO r1/mapping use 

    (let [results (js/Papa.parse result #js {"delimiter" separator
                                             "quoteChar" quote
                                             "skipEmptyLines" true
                                             "encoding" "UTF-8"})
          edn (get (js->clj results) "data")]
      (->> (map #(zipmap (first edn) %) (rest edn))
           (map-indexed (fn [idx row]
                          (assoc row :row-number idx)))))))
                 