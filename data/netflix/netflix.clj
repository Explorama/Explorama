(def
 desc
  
 ;bash builder.sh gen-mapping ../../../dummy/csv.clj ../../../dummy/netflix_titles.csv ../../../dummy/netflix-2.edn
 ;bash builder.sh gen ../../../dummy/netflix-2.edn ../../../dummy/netflix_titles.csv ../../../dummy/netflix_titles.edn
 ;bb tools/replace-things/replace-multi-in-file.clj dummy/replacements.txt dummy/netflix_titles.csv
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


desc
