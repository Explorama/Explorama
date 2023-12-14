(def
 desc
 {:meta-data
  {:file-format :csv, :csv {:separator ";", :quote "\"", :limit 1000}},
  :mapping
  {:datasource
   {:name [:value "Placeholder"],
    :global-id [:value "source-placeholder"]},
   :items
   [{:global-id [:field "ID"],
     :features
     [{:facts
       [{:value [:field "prio"],
         :name [:value "prio"],
         :type [:value "integer"]}
        {:value [:field "title"],
         :name [:value "title"]
         :type [:value "string"]}],
       :locations [{:point [:position [:field "location"]]}],
       :contexts
       [{:name [:field "country"],
         :global-id [:id-generate ["country" :text] :name],
         :type [:value "country"]}
        {:name [:field "plugin"],
         :global-id [:id-generate ["plugin" :text] :name],
         :type [:value "plugin"]}],
       :dates
       [{:value [:date-schema "YYYY-MM-dd" [:field "date"]],
         :type [:value "occured-at"]}],
       :texts [[:field "desc" ""]]}]}]}})


desc
