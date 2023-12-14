(ns de.explorama.cli.data-transformer.parser.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [de.explorama.shared.data-transformer.util :refer [string->char]]
            [de.explorama.shared.data-transformer.parser :as parser]))

(deftype CsvParser []
  parser/Parser
  (parser/parse [_
                 {{{:keys [encoding limit separator quote
                           drop-rows offset]
                    :or   {encoding "UTF-8"
                           separator ","
                           quote "\""
                           drop-rows 0
                           offset 0}}
                   :csv}
                  :meta-data}
                 file]
    (with-open [reader (apply io/reader
                              file
                              (when encoding
                                (list :encoding encoding)))]
      (let [rows (csv/read-csv
                  reader
                  :separator (string->char separator)
                  :quote (string->char quote))
            rows (drop drop-rows rows)
            header (first rows)
            rows (drop offset (rest rows))
            rows (if limit
                   (take (inc limit) rows)
                   rows)
            _ (log/debug {:file           file
                          :encoding       encoding
                          :separator (string->char separator)
                          :quote (string->char quote)
                          :reader-options {:separator (string->char separator)
                                           :quote (string->char quote)}
                          :count-limited  (count rows)
                          :drop-rows drop-rows
                          :offset offset})
            data (->> (map #(zipmap header %) rows)
                      (map-indexed #(assoc %2 :row-number %1))
                      vec)]
        data)))
  (parser/header
    [_
     {{{:keys [encoding separator quote drop-rows]
        :or   {encoding "UTF-8"
               separator ","
               quote "\""
               drop-rows 0}}
       :csv}
      :meta-data}
     file]
    (with-open [reader (apply io/reader
                              file
                              (when encoding
                                (list :encoding encoding)))]
      (let [rows (csv/read-csv reader
                               :separator (string->char separator)
                               :quote (string->char quote))
            rows (drop drop-rows rows)
            base-header-row (first rows)]
        base-header-row)))
  (parser/count-rows [_
                      {{{:keys [encoding separator quote drop-rows offset]
                         :or   {encoding "UTF-8"
                                separator ","
                                quote "\""
                                drop-rows 0
                                offset 0}}
                        :csv}
                       :meta-data}
                      file]
    (with-open [reader (apply io/reader
                              file
                              (when encoding
                                (list :encoding encoding)))]
      (let [rows (csv/read-csv reader
                               :separator (string->char separator)
                               :quote (string->char quote))
            rows (drop drop-rows rows)
            rows (rest rows)
            rows (drop offset rows)]
        (count rows)))))

(defn new-instance []
  (CsvParser.))