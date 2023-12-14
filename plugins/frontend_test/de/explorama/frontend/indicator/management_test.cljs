(ns de.explorama.frontend.indicator.management-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.indicator.views.management :as management]
            [de.explorama.shared.common.test-data :as td]))

(def sum-desc {:ui {:definition-rows [{:label :indicator-attribute-selection
                                       :comps
                                       [{:id "calc-attribute-select"
                                         :label :indicator-attribute-label
                                         :type :select
                                         :content :calc-attributes
                                         :replace :replace_A}]} ; => :replace_A_attr_name :replace_A_diid
                                      {:label :indicator-settings-label
                                       :comps
                                       [{:id "time-granularity-select"
                                         :label :indicator-time-granularity-label
                                         :type :select
                                         :content :time
                                         :replace :replace_time_grouping}
                                        {:id "grouping-select"
                                         :label :indicator-grouping-label
                                         :type :select
                                         :content :group-attributes
                                         :replace :replace_time_grouping}]}]
                    :additional-attributes {:label :indicator-additional-attributes-label
                                            :comps
                                            [{:id "additional-attribute-select"
                                              :label :indicator-attribute-label
                                              :type :select
                                              :content :all-attributes
                                              :replace :replace_X} ; => :replace_X_attr_name, :replace_X_diid, ...
                                             {:id "additional-aggregation-select"
                                              :depends-on "additional-attribute-select"
                                              :label :indicator-aggregation-label
                                              :type :select
                                              :content :aggregations
                                              :replace :replace_aggregation_#}]}}
               :description [:heal-event {:policy :merge
                                          :descs [:#
                                                  {:attribute :replace_defaults_indicator_name}
                                                  {:attribute :replace_X_heal_attr_name}]}

                             [:sum {:attribute :replace_A_attr_name}
                              [:group-by {:attributes :replace_time_grouping}
                               :replace_A_di]]
                             [:#
                              [:replace_aggregation_# {:attribute :replace_X_attr_name}
                               [:group-by {:attributes :replace_time_grouping
                                           :reduce-date? true}
                                :replace_X_di]]]]})

(def custom-desc {:ui {:definition-rows [{:label :indicator-custom
                                          :comps
                                          [{:id "custom-indicator-desc"
                                            :label :indicator-custom-description
                                            :type :textarea
                                            :content :description}]}]}})

(def test-sum-db
  {:indicator {:indicators {}
               :templates {:sum sum-desc}
               :connected-data {"id1" {"di-1" {:di "di-1"
                                               :ui-options {:calc-attributes [{:label td/fact-1
                                                                               :value td/fact-1}]
                                                            :group-attributes [{:label td/country, :value td/country}
                                                                               {:value "datasource", :label "datasource"}
                                                                               {:label td/category-1, :value td/category-1}
                                                                               {:label td/org, :value td/org}]
                                                            :time-attributes [{:value "day"
                                                                               :label "day"}
                                                                              {:value "month"
                                                                               :label "month"}
                                                                              {:value "year"
                                                                               :label "year"}]}
                                               :timestamp 1}}}
               :new-indicator {:name "Foo"
                               :id "id1"}
               :changes {"id1" {:name "Foo2"
                                :indicator-type :sum
                                :ui-desc {:min {"calc-attribute-select" {:value td/fact-1
                                                                         :option-type :calc
                                                                         :diid "di-1"}
                                                "time-granularity-select" {:value "year"}
                                                "grouping-select" [{:value td/org}]}
                                          :sum {"calc-attribute-select" {:value td/fact-1
                                                                         :option-type :calc
                                                                         :diid "di-1"}
                                                "time-granularity-select" {:value "year"}
                                                "grouping-select" [{:value td/org}]
                                                :additional [{"additional-attribute-select" {:value td/country
                                                                                             :option-type :group
                                                                                             :diid "di-1"}
                                                              "additional-aggregation-select" {:value :distinct}}]}}}}}})

(def test-custom-db 
  {:indicator {:indicators {}
               :templates {:custom custom-desc}
               :connected-data {"id1" {"di-1" {:di "di-1"
                                               :ui-options {:calc-attributes [{:label td/fact-1
                                                                               :value td/fact-1}]
                                                            :group-attributes [{:label td/country, :value td/country}
                                                                               {:value "datasource", :label "datasource"}
                                                                               {:label td/category-1, :value td/category-1}
                                                                               {:label td/org, :value td/org}]
                                                            :time-attributes [{:value "day"
                                                                               :label "day"}
                                                                              {:value "month"
                                                                               :label "month"}
                                                                              {:value "year"
                                                                               :label "year"}]}
                                               :timestamp 1}
                                       "di-2" {:di "di-2"
                                               :ui-options {:calc-attributes [{:label td/fact-1
                                                                               :value td/fact-1}]
                                                            :group-attributes [{:label td/country, :value td/country}
                                                                               {:value "datasource", :label "datasource"}
                                                                               {:label td/category-1, :value td/category-1}
                                                                               {:label td/org, :value td/org}]
                                                            :time-attributes [{:value "day"
                                                                               :label "day"}
                                                                              {:value "month"
                                                                               :label "month"}
                                                                              {:value "year"
                                                                               :label "year"}]}
                                               :timestamp 2}}}
               :new-indicator {:name "Foo"
                               :id "id1"}
               :changes {"id1" {:name "Foo2"
                                :indicator-type :custom
                                :ui-desc {:custom {"custom-indicator-desc" "[:sum {:attribute \"fact-1\"} \"Dataset 1\"]"}
                                          :sum {"calc-attribute-select" {:value td/fact-1
                                                                         :option-type :calc
                                                                         :diid "di-1"}
                                                "time-granularity-select" {:value "year"}
                                                "grouping-select" [{:value td/org}]
                                                :additional [{"additional-attribute-select" {:value td/country
                                                                                             :option-type :group
                                                                                             :diid "di-1"}
                                                              "additional-aggregation-select" {:value :distinct}}]}}}}}})

(def custom-server-indicator-desc {:id "id1"
                                   :dis {"di-1" "di-1"
                                           "di-2" "di-2"}
                                   :name "Foo2"
                                   :indicator-type :custom
                                   :ui-desc {:custom {"custom-indicator-desc" "[:sum {:attribute \"fact-1\"} \"Dataset 1\"]"}}
                                   :calculation-desc [:sum {:attribute td/fact-1}
                                                      "di-1"]})

(def description-map {:replace-datasource-name "Foo2"
                      :replace-notes-value "Foo2, Key not found: "
                      :replace-indicator-type "sum"
                      :replace_defaults_indicator_name "indicator"
                      :replace_A_attr_name td/fact-1
                      :replace_A_heal_attr_name td/fact-1
                      :replace_A_di "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
                      :replace_time_grouping ["year" td/org]
                      :# [{:replace_aggregation_# :distinct
                           :replace_X_attr_name td/country
                           :replace_X_heal_attr_name td/country
                           :replace_X_di "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"}]})

(def server-indicator-desc {:id "id1"
                            :dis {"di-1" "di-1"}
                            :name "Foo2"
                            :indicator-type :sum
                            :ui-desc {:sum {"calc-attribute-select" {:value td/fact-1
                                                                     :option-type :calc
                                                                     :diid "di-1"}
                                            "time-granularity-select" {:value "year"}
                                            "grouping-select" [{:value td/org}]
                                            :additional [{"additional-attribute-select" {:value td/country
                                                                                         :option-type :group
                                                                                         :diid "di-1"}
                                                          "additional-aggregation-select" {:value :distinct}}]}}
                            :calculation-desc [:heal-event {:policy :merge
                                                            :descs [{:attribute "indicator"}
                                                                    {:attribute "country (distinct)"}]}

                                               [:sum {:attribute td/fact-1}
                                                [:group-by {:attributes ["year" td/org]}
                                                 "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]]
                                               [:distinct {:attribute td/country}
                                                [:group-by {:attributes ["year" td/org]
                                                            :reduce-date? true}
                                                 "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]]]})

(deftest ui-translations
  (testing "generate description-map"
    (is (= description-map
           (management/indicator->description-map test-sum-db "id1"))))
  (testing "generate final indicator-desc"
    (is (= server-indicator-desc
           (management/indicator->final-description test-sum-db "id1"))))
  (testing "generate final indicator-desc for custom"
    (is (= custom-server-indicator-desc
           (management/indicator->final-description test-custom-db "id1")))))

(def ^:private unique-name #'management/unique-name)
(def ^:private copy-name #'management/copy-name)

(deftest misc-tests
  (testing "unique-names"
    (is (= "Indicator 1"
           (unique-name 1 [])))
    (is (= "Indicator 3"
           (unique-name 2 [{:name "Indicator 2"}]))))
  (testing "copy-names"
    (is (= "Indicator 1 (copy)"
           (copy-name "Indicator 1" {"1" {:name "Indicator 1"}})))
    (is (= "Indicator 3 (copy)"
           (copy-name "Indicator 3" {"1" {:name "Indicator 2"}})))
    (is (= "Indicator 2 (copy)"
           (copy-name "Indicator 2" {"2" {:name "Indicator 2"}})))
    (is (= "Indicator 2 (copy 1)"
           (copy-name "Indicator 2" {"2" {:name "Indicator 2"}
                                     "3" {:name "Indicator 2 (copy)"}})))))