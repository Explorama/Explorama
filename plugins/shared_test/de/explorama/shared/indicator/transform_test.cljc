(ns de.explorama.shared.indicator.transform-test
  (:require #?(:cljs [cljs.test :refer-macros [deftest testing is]]
               :clj [clojure.test :as test :refer [deftest is testing]])
            [de.explorama.shared.common.test-data :as td]
            [de.explorama.shared.indicator.transform :refer [traverse]]))

(def test-desc-1a [:heal-event {:policy :merge
                                :descs [:#
                                        {:attribute :replace_defaults_indicator_name}
                                        {:attribute :replace_attr_name_#}]}
                   [:sum {:attribute :replace_A_attr_name}
                    [:group-by {:attributes :replace_time_grouping}
                     :replace_A_diid]]
                   [:#
                    [:replace_aggregation_# {:attribute :replace_attr_name_#}
                     [:group-by {:attributes :replace_time_grouping
                                 :reduce-date? true}
                      :replace_diid_#]]]])

(def test-desc-1b [:heal-event {:policy :merge
                                :descs [:#
                                        {:attribute :replace_defaults_indicator_name}
                                        {:attribute :replace_attr_name_#}]}
                   [:#
                    [:sum {:attribute :replace_A_attr_name}
                     [:group-by {:attributes :replace_time_grouping}
                      :replace_A_diid]]
                    [:replace_aggregation_# {:attribute :replace_attr_name_#}
                     [:group-by {:attributes :replace_time_grouping
                                 :reduce-date? true}
                      :replace_diid_#]]]])

(def test-desc-1c [:heal-event {:policy :merge
                                :descs [:#
                                        {:attribute :replace_defaults_indicator_name}
                                        {:attribute :replace_attr_heal_name_#}]}
                   [:sum {:attribute :replace_A_attr_name}
                    [:group-by {:attributes :replace_time_grouping}
                     :replace_A_diid]]
                   [:#
                    [:replace_aggregation_# {:attribute :replace_attr_name_#}
                     [:group-by {:attributes :replace_time_grouping
                                 :reduce-date? true}
                      :replace_diid_#]]]])

(def test-values-1 {:replace_defaults_indicator_name "indicator"
                    :replace_A_attr_name td/fact-1
                    :replace_A_diid "di-1"
                    :replace_time_grouping ["year" td/country]
                    :# [{:replace_aggregation_# :distinct
                         :replace_attr_name_# td/org
                         :replace_diid_# "di-1"}
                        {:replace_aggregation_# :distinct
                         :replace_attr_name_# td/category-1
                         :replace_diid_# "di-1"}]})

(def test-values-3 {:replace_defaults_indicator_name "indicator"
                    :replace_A_attr_name td/fact-1
                    :replace_A_diid "di-1"
                    :replace_time_grouping ["year" td/country]
                    :# [{:replace_aggregation_# :sum
                         :replace_attr_name_# td/fact-4
                         :replace_attr_heal_name_# (str td/fact-4 "(sum)")
                         :replace_diid_# "di-1"}
                        {:replace_aggregation_# :min
                         :replace_attr_name_# td/fact-4
                         :replace_attr_heal_name_# (str td/fact-4 "lower (min)")
                         :replace_diid_# "di-1"}]})

(def test-values-1-faulty-1 (dissoc test-values-1 :replace_A_attr_name))
(def test-values-1-faulty-2 (update-in test-values-1 [:# 1] dissoc :replace_attr_name_#))
(def test-values-1-faulty-3 (-> (dissoc test-values-1 :replace_A_attr_name)
                                (update-in [:# 1] dissoc :replace_attr_name_#)))

(def test-result-1 [:heal-event {:policy :merge
                                 :descs [{:attribute "indicator"}
                                         {:attribute td/org}
                                         {:attribute td/category-1}]}
                    [:sum {:attribute td/fact-1}
                     [:group-by {:attributes ["year" td/country]}
                      "di-1"]]
                    [:distinct {:attribute td/org}
                     [:group-by {:attributes ["year" td/country]
                                 :reduce-date? true}
                      "di-1"]]
                    [:distinct {:attribute td/category-1}
                     [:group-by {:attributes ["year" td/country]
                                 :reduce-date? true}
                      "di-1"]]])

(def test-result-3 [:heal-event {:policy :merge
                                 :descs [{:attribute "indicator"}
                                         {:attribute (str td/fact-4 "(sum)")}
                                         {:attribute (str td/fact-4 "lower (min)")}]}
                    [:sum {:attribute td/fact-1}
                     [:group-by {:attributes ["year" td/country]}
                      "di-1"]]
                    [:sum {:attribute td/fact-4}
                     [:group-by {:attributes ["year" td/country]
                                 :reduce-date? true}
                      "di-1"]]
                    [:min {:attribute td/fact-4}
                     [:group-by {:attributes ["year" td/country]
                                 :reduce-date? true}
                      "di-1"]]])

(def test-desc-2
  [:heal-event {:policy :merge
                :descs [:#
                        {:attribute :replace_defaults_indicator_name}
                        {:attribute :replace_attr_name_#}]}
   [:+ nil
    [:* nil
     [:normalize {:range-min 0
                  :range-max 100}
      [:replace_A_aggregation {:attribute :replace_A_attr_name}
       [:group-by {:attributes :replace_time_grouping
                   :reduce-date? true}
        :replace_A_diid]]]
     :replace_A_weight_value] ;Weight value
    [:* nil
     [:normalize {:range-min 0
                  :range-max 100}
      [:replace_B_aggregation {:attribute :replace_B_attr_name}
       [:group-by {:attributes :replace_time_grouping
                   :reduce-date? true}
        :replace_B_diid]]
      :replace_B_weight_value]]]
   [:#
    [:replace_aggregation_# {:attribute :replace_attr_name_#}
     [:group-by {:attributes :replace_time_grouping
                 :reduce-date? true}
      :replace_diid_#]]]])

(def test-values-2
  {:replace_defaults_indicator_name "indicator"
   :replace_A_attr_name td/fact-1
   :replace_A_aggregation :sum
   :replace_A_diid "di-1"
   :replace_A_weight_value 0.5
   :replace_B_attr_name td/fact-2
   :replace_B_aggregation :sum
   :replace_B_diid "di-2"
   :replace_B_weight_value 0.5
   :replace_time_grouping ["year" td/country]
   :# [{:replace_aggregation_# :distinct
        :replace_attr_name_# td/org
        :replace_diid_# "di-1"}
       {:replace_aggregation_# :distinct
        :replace_attr_name_# td/category-1
        :replace_diid_# "di-1"}
       {:replace_aggregation_# :max
        :replace_attr_name_# td/fact-2
        :replace_diid_# "di-2"}
       {:replace_aggregation_# :max
        :replace_attr_name_# td/fact-1
        :replace_diid_# "di-2"}]})

(def test-result-2
  [:heal-event {:policy :merge
                :descs [{:attribute "indicator"}
                        {:attribute td/org}
                        {:attribute td/category-1}
                        {:attribute td/fact-2}
                        {:attribute td/fact-1}]}
   [:+ nil
    [:* nil
     [:normalize {:range-min 0
                  :range-max 100}
      [:sum {:attribute td/fact-1}
       [:group-by {:attributes ["year" td/country]
                   :reduce-date? true}
        "di-1"]]]
     0.5] ;Weight value
    [:* nil
     [:normalize {:range-min 0
                  :range-max 100}
      [:sum {:attribute td/fact-2}
       [:group-by {:attributes ["year" td/country]
                   :reduce-date? true}
        "di-2"]]
      0.5]]]
   [:distinct {:attribute td/org}
    [:group-by {:attributes ["year" td/country]
                :reduce-date? true}
     "di-1"]]
   [:distinct {:attribute td/category-1}
    [:group-by {:attributes ["year" td/country]
                :reduce-date? true}
     "di-1"]]
   [:max {:attribute td/fact-2}
    [:group-by {:attributes ["year" td/country]
                :reduce-date? true}
     "di-2"]]
   [:max {:attribute td/fact-1}
    [:group-by {:attributes ["year" td/country]
                :reduce-date? true}
     "di-2"]]])

(deftest ui-transformation-test
  (testing "Simple transformation test"
    (is (= test-result-1
           (traverse test-desc-1a test-values-1)))
    (is (= test-result-1
           (traverse test-desc-1b test-values-1)))
    (is (= test-result-2
           (traverse test-desc-2 test-values-2)))))

(deftest ui-transformation-test-complex
  (testing "Duplicate attributes"
    (is (= test-result-3
           (traverse test-desc-1c test-values-3)))))

(def validation-desc
  {:fixed [:replace_defaults_indicator_name
           :replace_A_attr_name
           :replace_A_diid
           :replace_time_grouping]
   :generic {:replace_aggregation_# 1
             :replace_attr_name_# 2
             :replace_diid_# 1}})

(def test-constant-value-desc
  [:heal-event {:policy :merge
                :descs [:#
                        {:attribute :replace_defaults_indicator_name}
                        {:attribute :replace_attr_name_#}]
                :addons [{:attribute "datasource" :value :replace-datasource-name}
                         {:attribute "notes" :value :replace-notes-value}]}
   [:sum {:attribute :replace_A_attr_name}
    [:group-by {:attributes :replace_time_grouping}
     :replace_A_diid]]
   [:#
    [:replace_aggregation_# {:attribute :replace_attr_name_#}
     [:group-by {:attributes :replace_time_grouping
                 :reduce-date? true}
      :replace_diid_#]]]])

(def test-constant-values (assoc test-values-1
                                 :replace-datasource-name "indicator"
                                 :replace-notes-value "notes"))

(def test-result-constant-values [:heal-event {:policy :merge
                                               :descs [{:attribute "indicator"}
                                                       {:attribute td/org}
                                                       {:attribute td/category-1}]
                                               :addons [{:attribute "datasource" :value "indicator"}
                                                        {:attribute "notes" :value "notes"}]}
                                  [:sum {:attribute td/fact-1}
                                   [:group-by {:attributes ["year" td/country]}
                                    "di-1"]]
                                  [:distinct {:attribute td/org}
                                   [:group-by {:attributes ["year" td/country]
                                               :reduce-date? true}
                                    "di-1"]]
                                  [:distinct {:attribute td/category-1}
                                   [:group-by {:attributes ["year" td/country]
                                               :reduce-date? true}
                                    "di-1"]]])

(deftest ui-transformation-validation-test
  (testing "Transformation validation test - error"
    (is (=
         {:replace_A_attr_name {:used 0
                                :target 1}}
         (try
           (traverse test-desc-1a
                     test-values-1-faulty-1
                     validation-desc)
           (catch #?(:clj Exception
                     :cljs js/Error) e
             (ex-data e)))))
    (is (= {:replace_attr_name_# {:used 2
                                  :target 4}}
           (try
             (traverse test-desc-1a
                       test-values-1-faulty-2
                       validation-desc)
             (catch #?(:clj Exception
                       :cljs js/Error) e
               (ex-data e)))))
    (is (= {:replace_attr_name_# {:used 2
                                  :target 4}
            :replace_A_attr_name {:used 0
                                  :target 1}}
           (try
             (traverse test-desc-1a
                       test-values-1-faulty-3
                       validation-desc)
             (catch #?(:clj Exception
                       :cljs js/Error) e
               (ex-data e))))))
  (testing "Transformation validation test - success"
    (is (= test-result-1
           (traverse test-desc-1a
                     test-values-1
                     validation-desc)))
    (is (= test-result-constant-values
           (traverse test-constant-value-desc
                     test-constant-values)))))