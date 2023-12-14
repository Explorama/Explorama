(ns datasource-a-dummy
  (:require [clojure.string :as str]))

(def desc-csv
  {:transformations {:pre {}
                     :post {}}
   :meta-data {:file-format :csv
               :csv {:separator ","
                     :quote "\""
                     :limit 1000}}
   :mapping {:datasource {:name [:value "Datasource A"]
                          :global-id [:value "source-ds-a"]}
             :items [{:global-id [:field "ID"]
                      :features [{:facts [{:value [:field "FACT1"]
                                           :name [:value "fact1"]
                                           :type [:value "integer"]}]
                                  :locations [{:point [:lat-lon
                                                       [:field "LATITUDE"]
                                                       [:field "LONGITUDE"]]}]
                                  :contexts [{:name [:field "COUNTRY"]
                                              :global-id [:id-generate ["country" :text] [:field "COUNTRY"]]
                                              :type [:value "country"]}

                                             {:name [:field "ORG1"]
                                              :global-id [:id-generate ["org" :text] [:field "ORG1"]]
                                              :type [:value "org"]}

                                             {:name [:field "ORG2"]
                                              :global-id [:id-generate ["org" :text] [:field "ORG2"]]
                                              :type [:value "org"]}]
                                  :dates [{:value [:convert (fn [row]
                                                              (let [date (get row "DATE")
                                                                    [d m y] (str/split date #"/")]
                                                                (str y "-" m "-" d)))]
                                           :type [:value "occured-at"]}]
                                  :texts [[:field "NOTES" ""]]}]}]}})

desc-csv