(ns de.explorama.backend.indicator.calculate-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [de.explorama.shared.common.test-data :as td]
            [de.explorama.backend.indicator.calculate :as calculate]))

(def ^:private apply-calculation-desc #'calculate/apply-calculation-desc)

(defn- with-heal [calculation]
  [:heal-event
   {:policy :merge
    :descs [{:attribute "indicator"}]}
   calculation])

(def input-data [{td/country td/country-a
                  "date" "1997-01-01"
                  td/fact-1 12
                  td/org ["A" "B"]}
                 {td/country td/country-a
                  "date" "1997-05-01"
                  td/fact-1 6
                  td/org ["B" "C"]}
                 {td/country td/country-a
                  "date" "1997-05-04"
                  td/fact-1 156
                  td/org "C"}
                 {td/country td/country-c
                  "date" "1997-05-04"
                  td/fact-1 155
                  td/org "CC"}
                 {td/country td/country-c
                  "date" "1998-01-04"
                  td/fact-1 6
                  td/org "A"}
                 {td/country td/country-c
                  "date" "1998-01-04"
                  td/fact-1 2
                  td/org ["A" "B"]}])

(def simple-average-calculation-country-year
  (with-heal
    [:/
     nil
     [:sum {:attribute td/fact-1}
      [:group-by {:attributes [td/country "year"]}
       "di-1"]]
     [:count-events nil
      [:group-by {:attributes [td/country "year"]}
       "di-1"]]]))
(def simple-average-calculation-org-month
  (with-heal
    [:/ nil
     [:sum {:attribute td/fact-1}
      [:group-by {:attributes [td/org "month"]}
       "di-1"]]
     [:count-events nil
      [:group-by {:attributes [td/org "month"]}
       "di-1"]]]))

(def average-country-year [{td/country td/country-c
                            "date" "1998"
                            "indicator" 4}
                           {td/country td/country-a
                            "date" "1997"
                            "indicator" 58}
                           {td/country td/country-c
                            "date" "1997"
                            "indicator" 155}])
(def average-org-month [{"date" "1997-05"
                         "indicator" 81
                         td/org "C"}
                        {"date" "1997-05"
                         "indicator" 6
                         td/org "B"}
                        {"date" "1997-01"
                         "indicator" 12
                         td/org "B"}
                        {"date" "1998-01"
                         "indicator" 4
                         td/org "A"}
                        {"date" "1997-05"
                         "indicator" 155
                         td/org "CC"}
                        {"date" "1997-01"
                         "indicator" 12
                         td/org "A"}
                        {"date" "1998-01"
                         "indicator" 2
                         td/org "B"}])

(def normalize-calculation-country-year
  [:heal-event
   {:policy :vals}
   [:normalize
    {:attribute td/fact-1
     :result-name "indicator"
     :range-min 0
     :range-max 100
     :all-data? false}
    [:group-by
     {:attributes [td/country "year"]}
     "di-1"]]])
(def normalize-calculation-org-month
  [:heal-event
   {:policy :vals}
   [:normalize
    {:attribute td/fact-1
     :result-name "indicator"
     :range-min 0
     :range-max 100
     :all-data? false}
    [:group-by
     {:attributes [td/org "month"]}
     "di-1"]]])

(def average-month-add-attr
  [:heal-event {:policy :merge, :generate-ids {:policy :uuid},
                :workaround {"date" {:month "01", :day "01"}},
                :descs [{:attribute "indicator"}
                        {:attribute "month (distinct)"}],
                :addons [{:attribute "datasource", :value "Indicator 7"}
                         {:attribute "notes", :value "Indicator 7, Average"}
                         {:attribute "indicator-type", :value "average"}],
                :force-type [{:attribute "indicator", :new-type :double}]}
   [:/ nil [:sum {:attribute td/fact-1}
            [:group-by {:attributes ["month"]}
             "di-1"]]
    [:count-events nil [:group-by {:attributes ["month"]}
                        "di-1"]]]
   [:distinct {:attribute "month"}
    [:group-by {:attributes ["month"], :reduce-date? true}
     "di-1"]]])

(def normalized-org-month
  [{td/country td/country-a, "date" "1997-01-01", td/fact-1 12, "indicator" 100, td/org "B"}
   {td/country td/country-a, "date" "1997-01-01", td/fact-1 12, "indicator" 100, td/org "A"}
   {td/country td/country-a, "date" "1997-05-01", td/fact-1 6, "indicator" 100, td/org "B"}
   {td/country td/country-a, "date" "1997-05-01", td/fact-1 6, "indicator" 0, td/org "C"}
   {td/country td/country-a, "date" "1997-05-04", td/fact-1 156, "indicator" 100, td/org "C"}
   {td/country td/country-c, "date" "1997-05-04", td/fact-1 155, "indicator" 100, td/org "CC"}
   {td/country td/country-c, "date" "1998-01-04", td/fact-1 6, "indicator" 100, td/org "A"}
   {td/country td/country-c, "date" "1998-01-04", td/fact-1 2, "indicator" 0, td/org "A"}
   {td/country td/country-c, "date" "1998-01-04", td/fact-1 2, "indicator" 100, td/org "B"}])

(def normalized-country-year
  [{td/country td/country-a, "date" "1997-01-01", td/fact-1 12, "indicator" 4N, td/org ["A" "B"]}
   {td/country td/country-a, "date" "1997-05-01", td/fact-1 6, "indicator" 0, td/org ["B" "C"]}
   {td/country td/country-a, "date" "1997-05-04", td/fact-1 156, "indicator" 100, td/org "C"}
   {td/country td/country-c, "date" "1997-05-04", td/fact-1 155, "indicator" 100, td/org "CC"}
   {td/country td/country-c, "date" "1998-01-04", td/fact-1 6, "indicator" 100, td/org "A"}
   {td/country td/country-c, "date" "1998-01-04", td/fact-1 2, "indicator" 0, td/org ["A" "B"]}])

(def min-calculation-country-year
  (with-heal
    [:min {:attribute td/fact-1}
     [:group-by {:attributes [td/country "year"]}
      "di-1"]]))

(def min-calculation-org-month
  (with-heal
    [:min {:attribute td/fact-1}
     [:group-by {:attributes [td/org "month"]}
      "di-1"]]))

(def min-org-month [{"date" "1997-05", "indicator" 6, td/org "C"}
                    {"date" "1997-05", "indicator" 6, td/org "B"}
                    {"date" "1997-01", "indicator" 12, td/org "B"}
                    {"date" "1998-01", "indicator" 2, td/org "A"}
                    {"date" "1997-05", "indicator" 155, td/org "CC"}
                    {"date" "1997-01", "indicator" 12, td/org "A"}
                    {"date" "1998-01", "indicator" 2, td/org "B"}])
(def min-country-year [{td/country td/country-c, "date" "1998", "indicator" 2}
                       {td/country td/country-a, "date" "1997", "indicator" 6}
                       {td/country td/country-c, "date" "1997", "indicator" 155}])

(def max-calculation-org-month
  (with-heal
    [:max {:attribute td/fact-1}
     [:group-by {:attributes [td/org "month"]}
      "di-1"]]))

(def max-calculation-country-year
  (with-heal
    [:max {:attribute td/fact-1}
     [:group-by {:attributes [td/country "year"]}
      "di-1"]]))
(def max-org-month [{"date" "1997-05", "indicator" 156, td/org "C"}
                    {"date" "1997-05", "indicator" 6, td/org "B"}
                    {"date" "1997-01", "indicator" 12, td/org "B"}
                    {"date" "1998-01", "indicator" 6, td/org "A"}
                    {"date" "1997-05", "indicator" 155, td/org "CC"}
                    {"date" "1997-01", "indicator" 12, td/org "A"}
                    {"date" "1998-01", "indicator" 2, td/org "B"}])
(def max-country-year [{td/country td/country-c, "date" "1998", "indicator" 6}
                       {td/country td/country-a, "date" "1997", "indicator" 156}
                       {td/country td/country-c, "date" "1997", "indicator" 155}])

(deftest test-apply-calculation
  (testing "Simple average calculation"
    (is (= (set average-country-year)
           (set (apply-calculation-desc simple-average-calculation-country-year
                                        {"di-1" input-data}))))
    (is (= (set average-org-month)
           (set (apply-calculation-desc simple-average-calculation-org-month
                                    {"di-1" input-data})))))
  (testing "Normalization"
    (is (= (set normalized-country-year)
           (set
            (apply-calculation-desc normalize-calculation-country-year
                                    {"di-1" input-data}))))
    (is (= (set normalized-org-month)
           (set
            (apply-calculation-desc normalize-calculation-org-month
                                    {"di-1" input-data})))))
  (testing "Min"
    (is (= (set min-country-year)
           (set
            (apply-calculation-desc min-calculation-country-year
                                    {"di-1" input-data}))))
    (is (= (set min-org-month)
           (set
            (apply-calculation-desc min-calculation-org-month
                                    {"di-1" input-data})))))
  (testing "Max"
    (is (= (set max-country-year)
           (set
            (apply-calculation-desc max-calculation-country-year
                                    {"di-1" input-data}))))
    (is (= (set max-org-month)
           (set
            (apply-calculation-desc max-calculation-org-month
                                    {"di-1" input-data}))))))

;;;;;;;;;;

(def datasource-a [{td/country td/country-a
                          "date" "1997-01-04"
                          td/fact-1 0
                          td/org ["A" "B"]
                         {td/country td/country-a
                          "date" "1997-01-31"
                          td/fact-1 20
                          td/org ["C" "B"]}
                         {td/country td/country-a
                          "date" "1997-05-05"
                          td/fact-1 5
                          td/org "CC"}
                         {td/country td/country-a
                          "date" "1997-06-01"
                          td/fact-1 1000
                          td/org ["A" "BC"]}
                         {td/country td/country-a
                          "date" "1997-10-12"
                          td/fact-1 300
                          td/org "A"}}])
(def datasource-e [{td/country td/country-a
                    "date" "1997"
                    "indicator" "indicator value 1"
                    td/fact-2 20}])

(def dis {"di-1" datasource-a
          "di-2" datasource-e})

(def control-fact-1-indicator
  [:+ nil
   [:* {:attribute "indicator"}
    [:normalize {:attributes td/fact-1
                 :range-min 0
                 :range-max 100
                 :all-data? true}
     [:sum {:attribute td/fact-1}
      [:group-by {:attributes ["year" td/country]}
       "di-1"]]]
    0.5]
   [:* {:attribute td/fact-2}
    [:group-by {:attributes ["year" td/country]}
     "di-2"]
    0.5]])

(def control-fact-1-indicator-result {{td/country td/country-a, "date" "1997"} 60.0})

(def select-all-orgs
  [:distinct {:attribute td/org}
   [:group-by {:attributes ["year" td/country]}
    "di-1"]])

(def control-fact-1-with-simple-heal
  (with-heal
    control-fact-1-indicator))

(def control-fact-1-indicator-simple-heal-result
  [{td/country td/country-a
    "date" "1997"
    "indicator" 60.0}])

(def control-fact-1-indicator-with-heal-result
  [{td/country td/country-a
    "date" "1997"
    "indicator" 60.0
    td/org ["A" "B" "C" "CC" "BC"]}])

(def control-fact-1-with-heal
  [:heal-event
   {:policy :merge
    :descs [{:attribute "indicator"}
            {:attribute td/org}]}
   control-fact-1-indicator
   select-all-orgs])

(deftest test-multi-dis
  (testing "Simple proportion"
    (is (= control-fact-1-indicator-result
           (apply-calculation-desc control-fact-1-indicator
                                   dis))))
  (testing "Proportion with heal-event"
    (is (= control-fact-1-indicator-simple-heal-result
           (apply-calculation-desc control-fact-1-with-simple-heal
                                   dis)))
    #_;TODO r1/tests fix this test
    (is (= control-fact-1-indicator-with-heal-result
           (apply-calculation-desc control-fact-1-with-heal
                                   dis)))))
