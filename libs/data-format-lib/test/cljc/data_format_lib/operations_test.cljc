(ns data-format-lib.operations-test
  (:require #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            [data-format-lib.operations :as of]
            [de.explorama.shared.common.test-data :as td]))

(defn- prepare-data-set [data-set]
  (into {}
        (map (fn [[key value]]
               [key (of/prepare-data value)]))
        data-set))

(t/deftest prepare-data
  (t/testing "Simple test"
    (t/is (= (set (of/prepare-data td/country-b-datasource-a-data))
             (set (flatten (vals td/country-b-datasource-a-data)))))
    (t/is (= (set (of/prepare-data (into td/country-b-datasource-a-data td/country-a-datasource-a-data)))
             td/all-events))))

(def fact-1=0 [:and {:data-format-lib.filter/op :=, :data-format-lib.filter/prop td/fact-1, :data-format-lib.filter/value 0}])
(def fact-1=1 [:and {:data-format-lib.filter/op :=, :data-format-lib.filter/prop td/fact-1, :data-format-lib.filter/value 1}])
(def org=org-2 [:and {:data-format-lib.filter/op :has, :data-format-lib.filter/prop td/org, :data-format-lib.filter/value (td/org-val 2)}])
(def org=org-3 [:and {:data-format-lib.filter/op :has, :data-format-lib.filter/prop td/org, :data-format-lib.filter/value (td/org-val 3)}])
(def org=org-4 [:and {:data-format-lib.filter/op :has, :data-format-lib.filter/prop td/org, :data-format-lib.filter/value (td/org-val 4)}])
(def category-1=category-A-2 [:and {:data-format-lib.filter/op :has, :data-format-lib.filter/prop td/category-1, :data-format-lib.filter/value (td/category-val "A" 2)}])
(def org=org-9 [:and {:data-format-lib.filter/op :has, :data-format-lib.filter/prop td/org, :data-format-lib.filter/value (td/org-val 9)}])

(t/deftest simple-operations
  (t/testing "Union test"
    (let [operations [:union
                      nil
                      [:filter "fact-1=1" td/country-a]
                      [:filter "fact-1=0" td/country-b]]
          filter-def {"fact-1=0" fact-1=0
                      "fact-1=1" fact-1=1}
          data-tile-sets {td/country-a td/country-a-datasource-a-data
                          td/country-b td/country-b-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 1), "datasource" td/datasource-a
                      td/org (td/org-val 9), "location" [[15 15]]
                      "annotation" "", "date" "1997-10-30", "notes" "Text", td/fact-1 0}
                     {td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 2), "datasource" td/datasource-a
                      td/org [(td/org-val 7) (td/org-val 8)], "location" [[15 15]]
                      "annotation" "", "date" "1998-11-21", "notes" "Text", td/fact-1 0}
                     {td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 1) (td/org-val 1)], "location" [[15 15]]
                      "annotation" "", "date" "1998-04-11", "notes" "Text", td/fact-1 0}
                     {td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 4), "datasource" td/datasource-a
                      td/org (td/org-val 5), "location" [[15 15]]
                      "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                     {td/country [td/country-a td/country-b], td/category-1 (td/category-val "A" 2), "id"  (td/id-val "B" 5), "datasource" td/datasource-a
                      td/org (td/org-val 5), "location" [[15 15]]
                      "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 9), "datasource" td/datasource-a
                      td/org [(td/org-val 10) (td/org-val 4)], "location" [[15 15]]
                      "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
                      "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}])))))
  (t/testing "Intersection test"
    (let [operations [:intersection
                      nil
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-2" org=org-2
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 8), "datasource" td/datasource-a
                      td/org [(td/org-val 2) (td/org-val 3)], "location" [[15 15]]
                      "annotation" "", "date" "1999-06-06", "notes" "Text", td/fact-1 0}])))))
  (t/testing "intersection-by id test"
    (let [operations [:intersection-by
                      "id"
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-2" org=org-2
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 8), "datasource" td/datasource-a
                      td/org [(td/org-val 2) (td/org-val 3)], "location" [[15 15]]
                      "annotation" "", "date" "1999-06-06", "notes" "Text", td/fact-1 0}])))))
  (t/testing "intersection-byorgsimple test"
    (let [operations [:intersection-by
                      td/org
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-2" org=org-2
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 8), "datasource" td/datasource-a
                      td/org [(td/org-val 2) (td/org-val 3)], "location" [[15 15]]
                      "annotation" "", "date" "1999-06-06", "notes" "Text", td/fact-1 0}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
                      "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}])))))
  (t/testing "intersection-byorgnot so simple test"
    (let [operations [:intersection-by
                      td/org
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-4" td/country-a]]
          filter-def {"org=org-2" org=org-2
                      "org=org-4" org=org-4}
          data-tile-sets {td/country-a td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 9), "datasource" td/datasource-a
                      td/org [(td/org-val 10) (td/org-val 4)], "location" [[15 15]]
                      "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 8), "datasource" td/datasource-a
                      td/org [(td/org-val 2) (td/org-val 3)], "location" [[15 15]]
                      "annotation" "", "date" "1999-06-06", "notes" "Text", td/fact-1 0}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
                      "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}])))))
  (t/testing "differenceorgsimple test"
    (let [operations [:difference
                      nil
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-3" td/country-a]]
          filter-def {"org=org-2" org=org-2
                      "org=org-3" org=org-3}
          data-tile-sets {td/country-a td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
                      "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}])))))
  (t/testing "sym-differenceorgsimple test"
    (let [operations [:sym-difference
                      nil
                      [:filter "org=org-2" td/country-a]
                      [:filter "org=org-4" td/country-a]]
          filter-def {"org=org-2" org=org-2
                      "org=org-4" org=org-4}
          data-tile-sets {td/country-a td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 9), "datasource" td/datasource-a
                      td/org [(td/org-val 10) (td/org-val 4)], "location" [[15 15]]
                      "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 8), "datasource" td/datasource-a
                      td/org [(td/org-val 2) (td/org-val 3)], "location" [[15 15]]
                      "annotation" "", "date" "1999-06-06", "notes" "Text", td/fact-1 0}])))))
  (t/testing "nested filter with same id test"
    (let [operations [:filter "fact-1=1"
                      [:filter "same-id" "same-id"]]
          filter-def {"fact-1=1" fact-1=1
                      "same-id" org=org-4}
          data-tile-sets {"same-id" td/country-a-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" (td/id-val "A" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
                      "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 9), "datasource" td/datasource-a
                      td/org [(td/org-val 10) (td/org-val 4)], "location" [[15 15]]
                      "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}])))))
  (t/testing "Select attribute values"
    (let [operations [:select {:attribute td/org}
                      "di1"]
          with-grouping [:select {:attribute td/org}
                         [:group-by {:attributes [td/category-1]}
                          "di1"]]
          data-tile-sets {"di1" td/country-a-datasource-a-data}]
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} operations)
               [(td/org-val 10) (td/org-val 4) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 5) (td/org-val 4) (td/org-val 2) (td/org-val 2) (td/org-val 3)]))
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} with-grouping)
               {{td/category-1 (td/category-val "A" 2)} [(td/org-val 10) (td/org-val 4) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 6) (td/org-val 5)],
                {td/category-1 (td/category-val "A" 1)} [(td/org-val 4) (td/org-val 2) (td/org-val 2) (td/org-val 3)]}))))
  (t/testing "Sort values by frequencies"
    (let [operations [:sort-by-frequencies {}
                      [:select {:attribute td/org}
                       "di1"]]
          with-grouping [:sort-by-frequencies {}
                         [:select {:attribute td/org}
                          [:group-by {:attributes [td/category-1]}
                           "di1"]]]
          data-tile-sets {"di1" td/country-a-datasource-a-data}]
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} operations)
               [(td/org-val 6) (td/org-val 4) (td/org-val 2) (td/org-val 10) (td/org-val 5)
                (td/org-val 3)]))
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} with-grouping)
               {{td/category-1 (td/category-val "A" 2)} [(td/org-val 6) (td/org-val 10) (td/org-val 4)
                                                 (td/org-val 5)],
                {td/category-1 (td/category-val "A" 1)} [(td/org-val 2) (td/org-val 4)
                                                             (td/org-val 3)]}))))
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
          data-tile-sets {"di1" td/country-a-datasource-a-data}]
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} operations)
               [(td/org-val 6)]))
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} with-grouping)
               {{td/category-1 (td/category-val "A" 2)} (td/org-val 6),
                {td/category-1 (td/category-val "A" 1)} (td/org-val 2)}))))
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
          data-tile-sets {"di1" td/country-a-datasource-a-data}]
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} operations)
               [(td/org-val 3)]))
      (t/is (= (of/perform-operation (prepare-data-set data-tile-sets) {} with-grouping)
               {{td/category-1 (td/category-val "A" 2)} (td/org-val 5),
                {td/category-1 (td/category-val "A" 1)} (td/org-val 3)}))))
  (t/testing "lets go crazy test"
    (let [operations [:union
                      nil
                      [:sym-difference
                       nil
                       [:filter "org=org-4"
                        [:filter "org=org-2" td/country-a]]
                       [:filter "org=org-4" td/country-a]]
                      [:intersection-by
                       td/category-1
                       [:filter (str td/category-1 "=" (td/category-val "A" 2)) td/country-b]
                       [:filter "org=org-9" td/country-b]]]
          filter-def {"org=org-2" org=org-2
                      "org=org-4" org=org-4
                      "org=org-3" org=org-3
                      "fact-1=0" fact-1=0
                      "fact-1=1" fact-1=1
                      "category-1=category-A-2" category-1=category-A-2
                      "org=org-9" org=org-9}
          data-tile-sets {td/country-a td/country-a-datasource-a-data
                          td/country-b td/country-b-datasource-a-data}]
      (t/is (= (set (of/perform-operation (prepare-data-set data-tile-sets) filter-def operations))
               (set [{td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 1), "datasource" td/datasource-a
                      td/org (td/org-val 9), "location" [[15 15]]
                      "annotation" "", "date" "1997-10-30", "notes" "Text", td/fact-1 0}
                     {td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 3), "datasource" td/datasource-a
                      td/org [(td/org-val 1) (td/org-val 1)], "location" [[15 15]]
                      "annotation" "", "date" "1998-04-11", "notes" "Text", td/fact-1 0}
                     {td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 4), "datasource" td/datasource-a
                      td/org (td/org-val 5), "location" [[15 15]]
                      "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                     {td/country [td/country-a td/country-b], td/category-1 (td/category-val "A" 2), "id"  (td/id-val "B" 5), "datasource" td/datasource-a
                      td/org (td/org-val 5), "location" [[15 15]]
                      "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                     {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 9), "datasource" td/datasource-a
                      td/org [(td/org-val 10) (td/org-val 4)], "location" [[15 15]]
                      "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}]))))))

(defn data-tile-lookup [{id :di/identifier}]
  [{"datasource" td/datasource-a, "year" "2000", "identifier" "search", td/country td/country-a, "bucket" "default"}
   {"datasource" td/datasource-a, "year" "1999", "identifier" "search", td/country td/country-a, "bucket" "default"}
   {"datasource" td/datasource-a, "year" "1998", "identifier" "search", td/country td/country-a, "bucket" "default"}
   {"datasource" td/datasource-a, "year" "1997", "identifier" "search", td/country td/country-a, "bucket" "default"}])

(defn data-tile-retrieval [missing-data-tiles & [opts]]
  (select-keys (apply merge td/country-a-datasource-a-data)
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
               (apply merge)
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
               (apply merge)
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
               (apply merge)
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
          data (-> (mapv vals td/country-b-datasource-a-data)
                   (flatten)
                   (vec))]
      (t/testing "Year"
        (t/is (= (of/perform-operation
                  {"di1" data}
                  nil
                  (group-operation ["year"]))
                 {{:year "1997"} [{:year "1997" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 1), "datasource" td/datasource-a
                                   td/org (td/org-val 9), "location" [[15 15]]
                                   "annotation" "", "date" "1997-10-30", "notes" "Text", td/fact-1 0}]
                  {:year "1998"} [{:year "1998" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 2), "datasource" td/datasource-a
                                   td/org [(td/org-val 7) (td/org-val 8)], "location" [[15 15]]
                                   "annotation" "", "date" "1998-11-21", "notes" "Text", td/fact-1 0}
                                  {:year "1998" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 3), "datasource" td/datasource-a
                                   td/org [(td/org-val 1) (td/org-val 1)], "location" [[15 15]]
                                   "annotation" "", "date" "1998-04-11", "notes" "Text", td/fact-1 0}]
                  {:year "2000"} [{:year "2000" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 4), "datasource" td/datasource-a
                                   td/org (td/org-val 5), "location" [[15 15]]
                                   "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                                  {:year "2000" td/country [td/country-a td/country-b], td/category-1 (td/category-val "A" 2), "id"  (td/id-val "B" 5), "datasource" td/datasource-a
                                   td/org (td/org-val 5), "location" [[15 15]]
                                   "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}]})))
      (t/testing "Month"
        (t/is (= (of/perform-operation
                  {"di1" data}
                  nil
                  (group-operation ["month"]))
                 {{:month "04"} [{:month "04" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 3), "datasource" td/datasource-a
                                  td/org [(td/org-val 1) (td/org-val 1)], "location" [[15 15]]
                                  "annotation" "", "date" "1998-04-11", "notes" "Text", td/fact-1 0}]
                  {:month "10"} [{:month "10" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 1), "datasource" td/datasource-a
                                  td/org (td/org-val 9), "location" [[15 15]]
                                  "annotation" "", "date" "1997-10-30", "notes" "Text", td/fact-1 0}]
                  {:month "11"} [{:month "11" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 2), "datasource" td/datasource-a
                                  td/org [(td/org-val 7) (td/org-val 8)], "location" [[15 15]]
                                  "annotation" "", "date" "1998-11-21", "notes" "Text", td/fact-1 0}
                                 {:month "11" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 4), "datasource" td/datasource-a
                                  td/org (td/org-val 5), "location" [[15 15]]
                                  "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                                 {:month "11" td/country [td/country-a td/country-b], td/category-1 (td/category-val "A" 2), "id"  (td/id-val "B" 5), "datasource" td/datasource-a
                                  td/org (td/org-val 5), "location" [[15 15]]
                                  "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}]})))
      (t/testing "Year-Month"
        (t/is (= (of/perform-operation
                  {"di1" data}
                  nil
                  (group-operation ["year" "month"]))
                 {{:year "1997", :month "10"} [{:year "1997" :month "10" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 1), "datasource" td/datasource-a
                                                td/org (td/org-val 9), "location" [[15 15]]
                                                "annotation" "", "date" "1997-10-30", "notes" "Text", td/fact-1 0}]
                  {:year "1998", :month "11"} [{:year "1998" :month "11" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 2), "datasource" td/datasource-a
                                                td/org [(td/org-val 7) (td/org-val 8)], "location" [[15 15]]
                                                "annotation" "", "date" "1998-11-21", "notes" "Text", td/fact-1 0}]
                  {:year "1998", :month "04"} [{:year "1998" :month "04" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 3), "datasource" td/datasource-a
                                                td/org [(td/org-val 1) (td/org-val 1)], "location" [[15 15]]
                                                "annotation" "", "date" "1998-04-11", "notes" "Text", td/fact-1 0}]
                  {:year "2000", :month "11"} [{:year "2000" :month "11" td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" (td/id-val "B" 4), "datasource" td/datasource-a
                                                td/org (td/org-val 5), "location" [[15 15]]
                                                "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}
                                               {:year "2000" :month "11" td/country [td/country-a td/country-b], td/category-1 (td/category-val "A" 2), "id"  (td/id-val "B" 5), "datasource" td/datasource-a
                                                td/org (td/org-val 5), "location" [[15 15]]
                                                "annotation" "", "date" "2000-11-02", "notes" "Text", td/fact-1 0}]}))))))
