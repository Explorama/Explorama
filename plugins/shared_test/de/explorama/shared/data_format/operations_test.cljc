(ns de.explorama.shared.data-format.operations-test
  (:require #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            [de.explorama.shared.data-format.operations :as of]
            [de.explorama.shared.common.test-data :as td]))

(t/deftest prepare-data
  (t/testing "Simple test"
    (t/is (= (set (of/prepare-data td/country-b-datasource-a-data))
             (set (flatten (vals td/country-b-datasource-a-data)))))
    (t/is (= (set (of/prepare-data (into td/country-b-datasource-a-data
                                         td/country-a-datasource-a-data)))
             td/all-events))))

(def fact-1=0 [:and {:de.explorama.shared.data-format.filter/op :=, :de.explorama.shared.data-format.filter/prop td/fact-1, :de.explorama.shared.data-format.filter/value 0}])
(def fact-1=1 [:and {:de.explorama.shared.data-format.filter/op :=, :de.explorama.shared.data-format.filter/prop td/fact-1, :de.explorama.shared.data-format.filter/value 1}])
(def org=org-1 [:and {:de.explorama.shared.data-format.filter/op :has, :de.explorama.shared.data-format.filter/prop td/org, :de.explorama.shared.data-format.filter/value (td/org-val 1)}])
(def org=org-3 [:and {:de.explorama.shared.data-format.filter/op :has, :de.explorama.shared.data-format.filter/prop td/org, :de.explorama.shared.data-format.filter/value (td/org-val 3)}])
(def org=org-4 [:and {:de.explorama.shared.data-format.filter/op :has, :de.explorama.shared.data-format.filter/prop td/org, :de.explorama.shared.data-format.filter/value (td/org-val 4)}])
(def category-1=category-A-2 [:and {:de.explorama.shared.data-format.filter/op :has, :de.explorama.shared.data-format.filter/prop td/category-1, :de.explorama.shared.data-format.filter/value (td/category-val 2 "A")}])
(def org=org-6 [:and {:de.explorama.shared.data-format.filter/op :has, :de.explorama.shared.data-format.filter/prop td/org, :de.explorama.shared.data-format.filter/value (td/org-val 6)}])

(t/deftest simple-operations
  (t/testing "Union test"
    (let [operations [:union
                      nil
                      [:filter "fact-1=1" td/country-a]
                      [:filter "fact-1=0" td/country-b]]
          filter-def {"fact-1=0" fact-1=0
                      "fact-1=1" fact-1=1}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)
                          td/country-b (td/prepare-data td/country-b-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               #{(td/get-data "B" 1 {})
                 (td/get-data "B" 2 {})
                 (td/get-data "B" 3 {})
                 (td/get-data "B" 4 {})
                 (td/get-data "B" 5 {})
                 (td/get-data "A" 9 {})
                 (td/get-data "A" 3 {})}))))
  (t/testing "Intersection test"
    (let [operations [:intersection
                      nil
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-2" org=org-1
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               (set #{(td/get-data "B" 5 {})})))))
  (t/testing "intersection-by id test"
    (let [operations [:intersection-by
                      "id"
                      [:filter "org=org-1" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-1" org=org-1
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               (set #{(td/get-data "B" 5 {})})))))
  (t/testing "intersection-by org simple test"
    (let [operations [:intersection-by
                      td/org
                      [:filter "org=org-1" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-1" org=org-1
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               (set #{(td/get-data "A" 3 {})
                      (td/get-data "A" 4 {})
                      (td/get-data "A" 5 {})
                      (td/get-data "A" 6 {})
                      (td/get-data "A" 7 {})
                      (td/get-data "A" 8 {})
                      (td/get-data "A" 9 {})
                      (td/get-data "B" 5 {})})))))
  (t/testing "differenceorgsimple test"
    (let [operations [:difference
                      nil
                      [:filter "org=org-1" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-1" org=org-1
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               (set [(td/get-data "A" 4 {})
                     (td/get-data "A" 5 {})
                     (td/get-data "A" 6 {})
                     (td/get-data "A" 7 {})])))))
  (t/testing "sym-differenceorgsimple test"
    (let [operations [:sym-difference
                      nil
                      [:filter "org=org-1" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-1" org=org-1
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               (set [(td/get-data "A" 3 {})
                     (td/get-data "A" 4 {})
                     (td/get-data "A" 5 {})
                     (td/get-data "A" 6 {})
                     (td/get-data "A" 7 {})
                     (td/get-data "A" 8 {})
                     (td/get-data "A" 9 {})])))))
  (t/testing "nested filter with same id test"
    (let [operations [:filter "fact-1=1"
                      [:filter "same-id" "same-id"]]
          filter-def {"fact-1=1" fact-1=1
                      "same-id" org=org-3}
          data-tile-sets {"same-id" (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               (set #{(td/get-data "A" 9 {})
                      (td/get-data "A" 3 {})})))))
  (t/testing "Select attribute values"
    (let [operations [:select {:attribute td/org}
                      "di1"]
          with-grouping [:select {:attribute td/org}
                         [:group-by {:attributes [td/category-1]}
                          "di1"]]
          data-tile-sets {"di1" (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (of/perform-operation data-tile-sets {} operations)
               [(td/org-val 2) (td/org-val 4) (td/org-val 2)
                (td/org-val 3) (td/org-val 1) (td/org-val 1)
                (td/org-val 1) (td/org-val 1) (td/org-val 3)
                (td/org-val 7) (td/org-val 5) (td/org-val 3)
                (td/org-val 1) (td/org-val 3)]))
      (t/is (= (of/perform-operation data-tile-sets {} with-grouping)
               {{td/category-1 (td/category-val 1 "A")}
                [(td/org-val 2) (td/org-val 3) (td/org-val 3) (td/org-val 7)],
                {td/category-1 (td/category-val 1 "B")}
                [(td/org-val 2) (td/org-val 4) (td/org-val 1) (td/org-val 1) (td/org-val 1) (td/org-val 1) (td/org-val 5) (td/org-val 3) (td/org-val 1) (td/org-val 3)]}))))
  (t/testing "Sort values by frequencies"
    (let [operations [:sort-by-frequencies {}
                      [:select {:attribute td/org}
                       "di1"]]
          with-grouping [:sort-by-frequencies {}
                         [:select {:attribute td/org}
                          [:group-by {:attributes [td/category-1]}
                           "di1"]]]
          data-tile-sets {"di1" (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (of/perform-operation data-tile-sets {} operations)
               [(td/org-val 1) (td/org-val 3) (td/org-val 2) (td/org-val 4) (td/org-val 7)
                (td/org-val 5)]))
      (t/is (= (of/perform-operation data-tile-sets {} with-grouping)
               {{td/category-1 (td/category-val 1 "B")} [(td/org-val 1) (td/org-val 3) (td/org-val 2)
                                                         (td/org-val 4) (td/org-val 5)],
                {td/category-1 (td/category-val 1 "A")} [(td/org-val 3) (td/org-val 2)
                                                         (td/org-val 7)]}))))
  (t/testing "Take first value from list"
    (let [operations [:take-first {}
                      [:sort-by-frequencies {}
                       [:select {:attribute td/org}
                        "di1"]]]
          with-grouping [:take-first {}
                         [:sort-by-frequencies {}
                          [:select {:attribute td/org}
                           [:group-by {:attributes [td/category-1]}
                            "di1"]]]]
          data-tile-sets {"di1" (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (of/perform-operation data-tile-sets {} operations)
               [(td/org-val 1)]))
      (t/is (= (of/perform-operation data-tile-sets {} with-grouping)
               {{td/category-1 (td/category-val 1 "B")} (td/org-val 1),
                {td/category-1 (td/category-val 1 "A")} (td/org-val 3)}))))
  (t/testing "Take last value from list"
    (let [operations [:take-last {}
                      [:sort-by-frequencies {}
                       [:select {:attribute td/org}
                        "di1"]]]
          with-grouping [:take-last {}
                         [:sort-by-frequencies {}
                          [:select {:attribute td/org}
                           [:group-by {:attributes [td/category-1]}
                            "di1"]]]]
          data-tile-sets {"di1" (td/prepare-data td/country-a-datasource-a-data)}]
      (t/is (= (of/perform-operation data-tile-sets {} operations)
               [(td/org-val 5)]))
      (t/is (= (of/perform-operation data-tile-sets {} with-grouping)
               {{td/category-1 (td/category-val 1 "A")} (td/org-val 7),
                {td/category-1 (td/category-val 1 "B")} (td/org-val 5)}))))
  (t/testing "lets go crazy test"
    (let [operations [:union
                      nil
                      [:sym-difference
                       nil
                       [:filter "org=org-4"
                        [:filter "org=org-1" td/country-a]]
                       [:filter "org=org-4" td/country-a]]
                      [:intersection-by
                       td/category-1
                       [:filter (str td/category-1 "=" (td/category-val 2 "A")) td/country-b]
                       [:filter "org=org-9" td/country-b]]]
          filter-def {"org=org-1" org=org-1
                      "org=org-4" org=org-4
                      "org=org-3" org=org-3
                      "fact-1=0" fact-1=0
                      "fact-1=1" fact-1=1
                      "category-1=category-A-2" category-1=category-A-2
                      "org=org-6" org=org-6}
          data-tile-sets {td/country-a (td/prepare-data td/country-a-datasource-a-data)
                          td/country-b (td/prepare-data td/country-b-datasource-a-data)}]
      (t/is (= (set (of/perform-operation data-tile-sets filter-def operations))
               #{(td/get-data "B" 1 {})
                 (td/get-data "B" 2 {})
                 (td/get-data "B" 3 {})
                 (td/get-data "B" 4 {})
                 (td/get-data "B" 5 {})
                 (td/get-data "A" 2 {})})))))

(defn data-tile-lookup [{id :di/identifier}]
  [td/country-a-datasource-a-dt-1
   td/country-a-datasource-a-dt-2
   td/country-a-datasource-a-dt-3
   td/country-a-datasource-a-dt-4])

(defn data-tile-retrieval [missing-data-tiles & [opts]]
  (select-keys td/country-a-datasource-a-data
               missing-data-tiles))

(t/deftest abort-early-operations
  (let [data-instance {:di/data-tile-ref {"di1" {:di/identifier "all"}}
                       :di/operations [:filter "f1" "di1"]
                       :di/filter {"f1" [:and]}}]
    (t/testing "Abort on invalid filter"
      (t/is (thrown-with-msg? #?(:clj AssertionError
                                 :cljs js/Error)
                              #"Result-limit is only allowed for simple operations"
                              (of/transform (assoc data-instance
                                                   :di/operations [:union nil
                                                                   [:filter "f1" "di1"]
                                                                   [:filter "f1" "di1"]])
                                            data-tile-lookup
                                            data-tile-retrieval
                                            :abort-early
                                            {:result-limit 5
                                             :result-chunk-size 3}))))
    (t/testing "Abort on data-tiles"
      (t/is (thrown-with-msg? #?(:clj Exception
                                 :cljs js/Error)
                              #"Data-tile limited exceeded"
                              (of/transform data-instance
                                            data-tile-lookup
                                            data-tile-retrieval
                                            :abort-early
                                            {:data-tile-limit 5}))))
    (t/testing "Abort on filter"
      (t/is (thrown-with-msg? #?(:clj Exception
                                 :cljs js/Error)
                              #"Result limited exceeded"
                              (of/transform data-instance
                                            data-tile-lookup
                                            data-tile-retrieval
                                            :abort-early
                                            {:result-limit 5
                                             :result-chunk-size 3}))))
    (t/testing "Succeed on data-tiles"
      (t/is
       (= (set (of/transform data-instance
                             data-tile-lookup
                             data-tile-retrieval
                             :abort-early
                             {:data-tile-limit 12}
                             :attach-buckets? true))
          (->> td/country-a-datasource-a-data
               vals
               flatten
               (map #(assoc % "bucket" "default"))
               set))))
    (t/testing "Succeed on filter"
      (t/is
       (= (set (of/transform data-instance
                             data-tile-lookup
                             data-tile-retrieval
                             :abort-early
                             {:result-limit 12
                              :result-chunk-size 3}
                             :attach-buckets? true))
          (->> td/country-a-datasource-a-data
               vals
               flatten
               (map #(assoc % "bucket" "default"))
               set))))
    (t/testing "Succeed on both"
      (t/is
       (= (set (of/transform data-instance
                             data-tile-lookup
                             data-tile-retrieval
                             :abort-early
                             {:data-tile-limit 12
                              :result-limit 12
                              :result-chunk-size 3}
                             :attach-buckets? true))
          (->> td/country-a-datasource-a-data
               vals
               flatten
               (map #(assoc % "bucket" "default"))
               set))))))

(t/deftest group-by-date-granularity
  (t/testing "Group-by date granularity test"
    (let [group-operation (fn [attributes]
                            [:group-by {:attributes attributes
                                        :ignore-hierarchy? true
                                        :granularity-attr? true}
                             "di1"])
          data (td/prepare-data td/country-b-datasource-a-data)]
      (t/testing "Year"
        (t/is (= (of/perform-operation
                  {"di1" data}
                  nil
                  (group-operation ["year"]))
                 {{:year "1997"} [(td/get-data "B" 1 {:year "1997"})]
                  {:year "1998"} [(td/get-data "B" 2 {:year "1998"})
                                  (td/get-data "B" 3 {:year "1998"})]
                  {:year "2000"} [(td/get-data "B" 4 {:year "2000"})
                                  (td/get-data "B" 5 {:year "2000"})]})))
      (t/testing "Month"
        (t/is (= (of/perform-operation
                  {"di1" data}
                  nil
                  (group-operation ["month"]))
                 {{:month "04"} [(td/get-data "B" 3 {:month "04"})]
                  {:month "10"} [(td/get-data "B" 1 {:month "10"})]
                  {:month "11"} [(td/get-data "B" 2 {:month "11"})
                                 (td/get-data "B" 4 {:month "11"})
                                 (td/get-data "B" 5 {:month "11"})]})))
      (t/testing "Year-Month"
        (t/is (= (of/perform-operation
                  {"di1" data}
                  nil
                  (group-operation ["year" "month"]))
                 {{:year "1997", :month "10"} [(td/get-data "B" 1 {:year "1997" :month "10"})]
                  {:year "1998", :month "11"} [(td/get-data "B" 2 {:year "1998" :month "11"})]
                  {:year "1998", :month "04"} [(td/get-data "B" 3 {:year "1998" :month "04"})]
                  {:year "2000", :month "11"} [(td/get-data "B" 4 {:year "2000" :month "11"})
                                               (td/get-data "B" 5 {:year "2000" :month "11"})]}))))))
