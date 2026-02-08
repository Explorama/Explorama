(ns de.explorama.shared.cache.data-tiles.tiling-test
  (:require [clojure.test :refer [deftest testing is]]
            [de.explorama.shared.common.data.data-tiles :as tiles]
            [de.explorama.shared.cache.data-tile.retrieval :as retrieval]))

(def tile-data-tiles #'retrieval/tile-data-tiles)

(def year-dim (tiles/access-key "year"))
(def country-dim (tiles/access-key "country"))
(def bucket-dim (tiles/access-key "bucket"))
(def datasource-dim (tiles/access-key "datasource"))
(def identifier-dim (tiles/access-key "identifier"))

(def data-tiles-1
  (into
   (vec (for [year (range 1997 2000)
              datasource ["DS-A20190827" "DS-B" "DS-C" "DS-D20200113" "DS-E20200113" "DS-F20200113"]
              country ["Algeria" "Angloa"]]
          {year-dim year
           datasource-dim datasource
           country-dim country
           identifier-dim "search"
           bucket-dim "default"}))
   (for [id (range 0 6)]
     {:id id
      identifier-dim "dummy"})))

(def data-tiles-2
  (for [year (range 1997 2020)
        datasource ["DS-A" "DS-B" "DS-C"]
        country ["Afghanistan" "Akrotiri and Dhekelia" "Albania" "Algeria" "Angola"
                 "Azerbaijan" "Bahrain" "Belarus" "Benin" "Bosnia and Herzegovina"
                 "Botswana" "Bulgaria" "Burkina Faso" "Burundi" "Cameroon"
                 "Caspian Sea" "Central African Republic" "Chad" "Croatia"
                 "Cyprus" "Democratic Republic of Congo" "Djibouti" "Egypt"
                 "Equatorial Guinea" "Eritrea" "Ethiopia" "Gabon" "Gambia"
                 "Ghana" "Guinea" "Guinea-Bissau" "India" "Indonesia" "Iraq"
                 "Israel" "Ivory Coast" "Jordan" "Kenya" "Kosovo" "Kuwait"
                 "Laos" "Lebanon" "Lesotho" "Liberia" "Libya" "Macedonia"
                 "Madagascar" "Malawi" "Malaysia" "Mali" "Mauritania" "Moldova"
                 "Montenegro" "Morocco" "Mozambique" "Namibia" "Niger" "Nigeria"
                 "Northern Cyprus" "Oman" "Palestina" "Qatar" "Republic of Congo"
                 "Romania" "Russia" "Rwanda" "Saudi Arabia" "Senegal" "Serbia"
                 "Sierra Leone" "Somalia" "South Africa" "South Sudan" "Sudan"
                 "Swaziland" "Syria" "Tanzania" "Thailand" "Togo" "Tunisia" "Turkey"
                 "Uganda" "Ukraine" "United Arab Emirates" "Vietnam" "Yemen" "Zambia"
                 "Zimbabwe"]]
    {year-dim year
     datasource-dim datasource
     country-dim country
     identifier-dim "search"
     bucket-dim "default"}))

(def data-tiles-3
  (for [year (range 2000 2004)
        datasource ["DS-A" "DS-B" "DS-C"]
        country ["Afghanistan" "Akrotiri and Dhekelia" "Albania" "Algeria" "Angola"
                 "Zimbabwe"]]
    {year-dim year
     datasource-dim datasource
     country-dim country
     identifier-dim "search"
     bucket-dim "default"}))

(defn result-set-1 [identifier dss cs ys]
  (set (for [c cs
             ds dss
             y ys]
         {year-dim       y
          datasource-dim ds
          country-dim    c
          identifier-dim identifier
          bucket-dim "default"})))

(defn result-set-2 [identifier ids]
  (set (for [id ids]
         {:id id
          identifier-dim identifier})))

(deftest testing-data-tile-tiling
  (testing "testing data-tile tiling on default configuration"
    (let [config
          {:query-partition
           {"search" {:big {:partition 1
                            :keys [datasource-dim year-dim identifier-dim]}
                      :medium {:partition 2
                               :keys [datasource-dim country-dim identifier-dim]}
                      :small {:partition 1000
                              :keys [datasource-dim identifier-dim]}}
            "dummy" {:dummy {:partition 15
                             :keys [identifier-dim]}
                     :small {:partition 4
                             :keys [identifier-dim]}}}
           :workaround-data-tile-classification
           {:classification [{:match {identifier-dim "search"
                                      datasource-dim "DS-C"}
                              :=> :big}
                             {:match {identifier-dim "search"}
                              :regex {datasource-dim "DS-A[0-9]+"}
                              :=> :medium}
                             {:match {identifier-dim "search"}
                              :regex {datasource-dim "DS-D[0-9]+"}
                              :=> :medium}
                             {:match {identifier-dim "search"}
                              :regex {datasource-dim "DS-E[0-9]+"}
                              :=> :medium}
                             {:match {identifier-dim "search"}
                              :regex {datasource-dim "DS-F[0-9]+"}
                              :=> :medium}
                             {:match {identifier-dim "dummy"
                                      :id 5}
                              :=> :dummy}]
            :default :small}}]
      (is  (= (set (mapv set (tile-data-tiles config data-tiles-1)))
              #{(result-set-1 "search" ["DS-A20190827"] ["Angloa"] [1997 1998])
                (result-set-1 "search" ["DS-A20190827"] ["Angloa"] [1999])
                (result-set-1 "search" ["DS-A20190827"] ["Algeria"] [1997 1998])
                (result-set-1 "search" ["DS-A20190827"] ["Algeria"] [1999])
                (result-set-1 "search" ["DS-D20200113"] ["Angloa"] [1997 1998])
                (result-set-1 "search" ["DS-D20200113"] ["Angloa"] [1999])
                (result-set-1 "search" ["DS-D20200113"] ["Algeria"] [1997 1998])
                (result-set-1 "search" ["DS-D20200113"] ["Algeria"] [1999])
                (result-set-1 "search" ["DS-E20200113"] ["Angloa"] [1997 1998])
                (result-set-1 "search" ["DS-E20200113"] ["Angloa"] [1999])
                (result-set-1 "search" ["DS-E20200113"] ["Algeria"] [1997 1998])
                (result-set-1 "search" ["DS-E20200113"] ["Algeria"] [1999])
                (result-set-1 "search" ["DS-F20200113"] ["Angloa"] [1997 1998])
                (result-set-1 "search" ["DS-F20200113"] ["Angloa"] [1999])
                (result-set-1 "search" ["DS-F20200113"] ["Algeria"] [1997 1998])
                (result-set-1 "search" ["DS-F20200113"] ["Algeria"] [1999])
                (result-set-1 "search" ["DS-C"] ["Algeria"] [1997])
                (result-set-1 "search" ["DS-C"] ["Angloa"] [1997])
                (result-set-1 "search" ["DS-C"] ["Angloa"] [1998])
                (result-set-1 "search" ["DS-C"] ["Algeria"] [1998])
                (result-set-1 "search" ["DS-C"] ["Angloa"] [1999])
                (result-set-1 "search" ["DS-C"] ["Algeria"] [1999])
                (result-set-1 "search" ["DS-B"] ["Angloa" "Algeria"] [1997 1998 1999])
                (result-set-2 "dummy" [0 1 2 3])
                (result-set-2 "dummy" [4])
                (result-set-2 "dummy" [5])})))
    (testing "testing data-tile tiling on default configuration - size check"
      (let [config
            {:query-partition
             {"search" {:small {:partition 25
                                :keys [datasource-dim identifier-dim]}}
              "dummy" {:small {:partition 25
                               :keys [identifier-dim]}}}
             :workaround-data-tile-classification
             {:default :small}}]
        (is (=
             (reduce (fn [acc tiles]
                       (max acc (count tiles)))
                     0
                     (tile-data-tiles config data-tiles-2))
             25))))))
