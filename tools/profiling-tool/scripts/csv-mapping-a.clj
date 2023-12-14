(def
 desc
 {:meta-data
  {:file-format :csv, :csv {:separator ",", :quote "\""}},
  :mapping
  {:datasource
   {:name [:value "Data-A"],
    :global-id [:value "source-data-a"]},
   :items
   [{:global-id [:field "id"],
     :features
     [{:facts
       [{:value [:field "fact1"],
         :name [:value "fact1"],
         :type [:value "integer"]}],
       :locations [],
       :contexts
       [{:name [:field "country"],
         :global-id
         [:id-generate ["country" :text] [:field "country"]],
         :type [:value "country"]}
        {:name [:field "context2"],
         :global-id
         [:id-generate ["context2" :text] [:field "context2"]],
         :type [:value "context2"]}
        {:name [:field "context1"],
         :global-id
         [:id-generate ["context1" :text] [:field "context1"]],
         :type [:value "context1"]}
        {:name [:field "fact2"],
         :global-id [:id-generate ["fact2" :text] [:field "fact2"]],
         :type [:value "fact2"]}
        {:name [:field "fact3"],
         :global-id [:id-generate ["fact3" :text] [:field "fact3"]],
         :type [:value "fact3"]}],
       :dates
       [{:value [:date-schema "YYYY-MM-dd" [:field "date"]],
         :type [:value "occured-at"]}],
       :texts [[:field "text" ""]]}]}]}})


desc
