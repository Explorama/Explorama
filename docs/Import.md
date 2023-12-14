# Import

There is currently only one way to import data into Explorama via CSV files and the import dialog. However the options the UI provides are fairly limited. Thats you can also import data by providing a mapping file. You can create this mapping file by hand or download it after uploading the CSV file in the import dialog (or use the cli-transformation tool).


Here is an example:
```clojure
 {:meta-data
  {:file-format :csv, :csv {:separator ",", :quote "\"", :limit 500}},
  :mapping
  {:datasource
   {:name [:value "Netflix"], ;URL https://www.kaggle.com/datasets/shivamb/netflix-shows/
    :global-id [:value "source-netflix"]},
   :items
   [{:global-id [:field "show_id"],
     :features
     [{:facts
       [{:value [:field "release_year"],
         :name [:value "release_year"],
         :type [:value "integer"]}
        {:value [:field "duration"],
         :name [:value "duration"],
         :type [:value "string"]}
        {:value [:field "title"],
         :name [:value "title"],
         :type [:value "string"]}],
       :locations [],
       :contexts
       [{:name      [:convert ["cast" ", "]]
         :global-id [:id-generate ["cast" :text] :name]
         :type      [:value "cast"]}
        {:name [:convert ["listed_in" ", "]],
         :global-id [:id-generate ["listed_in" :text] :name],
         :type [:value "listed_in"]}
        {:name [:convert ["country" ", "]],
         :global-id [:id-generate ["country" :text] :name],
         :type [:value "country"]}
        {:name [:convert ["director" ", "]],
         :global-id [:id-generate ["director" :text] :name],
         :type [:value "director"]}
        
        {:name [:field "type"],
         :global-id [:id-generate ["type" :text] :name],
         :type [:value "type"]}
        {:name [:field "rating"],
         :global-id [:id-generate ["rating" :text] :name],
         :type [:value "rating"]}],
       :dates
       [{:value [:date-schema "MMM. dd, YYYY" [:field "date_added"]],
         :type [:value "occured-at"]}],
       :texts [[:field "description" ""]]}]}]}})
```

Full schema can be found [here](../libs/data-transformer/src/de/explorama/shared/data_transformer/schema.cljc). A description what everything does will follow soon.

The resulting data for the import is specified [here](../libs/data-transformer/src/de/explorama/shared/data_transformer/spec.cljc).