(ns data.dummy-data-roadmap)

(def data
  {:contexts   [{:global-id "plugin-hints"
                 :type      "plugin"
                 :name      "hints"}
                {:global-id "plugin-search"
                 :type      "plugin"
                 :name      "search"}
                {:global-id "country-Germany"
                 :type      "country"
                 :name      "Germany"}
                {:global-id "plugin-indicator"
                 :type      "plugin"
                 :name      "indicator"}
                {:global-id "plugin-map"
                 :type      "plugin"
                 :name      "map"}
                {:global-id "plugin-electron"
                 :type      "plugin"
                 :name      "electron"}
                {:global-id "plugin-woco"
                 :type      "plugin"
                 :name      "woco"}
                {:global-id "plugin-projects"
                 :type      "plugin"
                 :name      "projects"}
                {:global-id "plugin-table"
                 :type      "plugin"
                 :name      "table"}
                {:global-id "plugin-charts"
                 :type      "plugin"
                 :name      "charts"}
                {:global-id "plugin-expdb"
                 :type      "plugin"
                 :name      "expdb"}]
   :datasource {:global-id "source-roadmap"
                :name      "Roadmap"}
   :items      [{:global-id "1"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 4} {:name  "title"
                                                         :type  "string"
                                                         :value "Support for video, pictures and sound"}]
                              :locations    [{:lat 90.0
                                              :lon 0.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-expdb"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Add support for video, pictures and sound fact to the import and visualization plugins."]}]}
                {:global-id "2"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 2} {:name  "title"
                                                         :type  "string"
                                                         :value "Encryption of data for Electron"}]
                              :locations    [{:lat 70.0
                                              :lon 30.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-electron"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Add encryption support for the database to the Electron version. There should be a configuration option, so that the use can decide whats gets encrypted"]}]}
                {:global-id "3"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 1} {:name  "title"
                                                         :type  "string"
                                                         :value "Undo"}]
                              :locations    [{:lat 70.0
                                              :lon -30.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-woco"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Add a simple undo function for the workspace - it should work indenpent of the protocol"]}]}
                {:global-id "4"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 3} {:name  "title"
                                                         :type  "string"
                                                         :value "Formal Concept Analysis Visualization"}]
                              :locations    [{:lat 70.0
                                              :lon 25.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-woco"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Graph visualization of rules and implications."]}]}
                {:global-id "5"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 3} {:name  "title"
                                                         :type  "string"
                                                         :value "Hexagon heatmap"}]
                              :locations    [{:lat 70.0
                                              :lon -25.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-map"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Add a hexagon heatmap to the map plugin."]}]}
                {:global-id "6"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 2} {:name  "title"
                                                         :type  "string"
                                                         :value "Summary and improved statistics of data"}]
                              :locations    [{:lat 45.0
                                              :lon 40.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-expdb"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Add a summary and improved statistics of data to expdb and use it in the right places, e.g. data-atlas."]}]}
                {:global-id "7"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 4} {:name  "title"
                                                         :type  "string"
                                                         :value "Embed wikipedia for searching"}]
                              :locations    [{:lat 45.0
                                              :lon -40.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-search"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Wikipedia embedding for faster and better additional data."]}]}
                {:global-id "8"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 4} {:name  "title"
                                                         :type  "string"
                                                         :value "Hints and suggestions for next steps"}]
                              :locations    [{:lat 45.0
                                              :lon 28.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-hints"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["To empower the user, the system should give hints and suggestions for next steps."]}]}
                {:global-id "9"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 1} {:name  "title"
                                                         :type  "string"
                                                         :value "Notifications"}]
                              :locations    [{:lat 45.0
                                              :lon -28.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-woco"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["More transparent error messages and notifications for the user."]}]}
                {:global-id "10"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 1} {:name  "title"
                                                         :type  "string"
                                                         :value "Migration"}]
                              :locations    [{:lat 15.0
                                              :lon 50.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-expdb"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Implement a migration strategy for the user data and events."]}]}
                {:global-id "11"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 2} {:name  "title"
                                                         :type  "string"
                                                         :value "Sharing"}]
                              :locations    [{:lat 15.0
                                              :lon -50.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-projects"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Adds a sharing function that allows users to share their data and projects without a shared server."]}]}
                {:global-id "12"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 1} {:name  "title"
                                                         :type  "string"
                                                         :value "Performance"}]
                              :locations    [{:lat 15.0
                                              :lon 32.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-expdb"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Improve the performance, mainly import and read performance for the data."]}]}
                {:global-id "13"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 2} {:name  "title"
                                                         :type  "string"
                                                         :value "Units"}]
                              :locations    [{:lat 15.0
                                              :lon -32.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-expdb"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Allow the user to specify units for facts and allow operations based on the units, e.g. tranforming units into different ones."]}]}
                {:global-id "14"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 4} {:name  "title"
                                                         :type  "string"
                                                         :value "Better Charts"}]
                              :locations    [{:lat 15.0
                                              :lon 5.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-charts"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Check out ECharts and test them compared to Chartjs."]}]}
                {:global-id "15"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 4} {:name  "title"
                                                         :type  "string"
                                                         :value "Better Map"}]
                              :locations    [{:lat 15.0
                                              :lon -5.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-map"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Look into performance improvements for the map plugin. The goal is to have a similar performance as mosaic."]}]}
                {:global-id "16"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 3} {:name  "title"
                                                         :type  "string"
                                                         :value "Improve Table"}]
                              :locations    [{:lat -15.0
                                              :lon 5.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-table"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Add some common features to the table plugin."]}]}
                {:global-id "17"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 2} {:name  "title"
                                                         :type  "string"
                                                         :value "Aggregations"}]
                              :locations    [{:lat -15.0
                                              :lon -5.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-indicator"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Improve the indicator plugin to allow more complex aggregations, e.g. by introducing a VPL."]}]}
                {:global-id "18"
                 :features  [{:facts        [{:name  "prio"
                                              :type  "integer"
                                              :value 1} {:name  "title"
                                                         :type  "string"
                                                         :value "Time granularities"}]
                              :locations    [{:lat 90.0
                                              :lon 0.0}]
                              :context-refs [{:global-id "country-Germany"} {:global-id "plugin-expdb"}]
                              :dates        [{:type  "occured-at"
                                              :value "2023-12-14"}]
                              :texts        ["Allow more flexibility for time attributes."]}]}]})