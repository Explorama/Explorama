(ns de.explorama.backend.expdb.temp-import.csv-parser
  (:require [clojure.data.csv :as csv]))

(def ^:private default-desc {:separator ","
                             :quote "\""
                             :limit 200})

(defn parse [{{csv :csv} :meta-data} result]
  (let [{:keys [separator quote]} (or csv default-desc)
        edn (csv/read-csv result :separator (first separator) :quote (first quote) :encoding "UTF-8")]
    (->> (map #(zipmap (first edn) %) (rest edn))
         (map-indexed (fn [idx row]
                        (assoc row :row-number idx))))))
