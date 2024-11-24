(ns de.explorama.shared.data-format.operations-indicator-test
  (:require #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            [de.explorama.shared.data-format.operations :as of]
            [de.explorama.shared.common.test-data :as td]))

(def ^:private group-data #'of/group-data)
(def ^:private time-value #'of/time-value)

(defn sort-by-date [data]
  (sort-by #(get % "date") data))

(def benin-input-data {td/country td/country-a
                       "date" "1997-01-01"
                       td/fact-1 12
                       td/org ["A" "B"]})

(def input-data [benin-input-data
                 {td/country td/country-a
                  "date" "1997-05-01"
                  td/fact-1 6
                  td/org ["B" "C"]}
                 {td/country td/country-a
                  "date" "1997-05-04"
                  td/fact-1 18
                  td/org "C"}
                 {td/country td/country-c
                  "date" "1997-05-04"
                  td/fact-1 16
                  td/org "CC"}
                 {td/country td/country-c
                  "date" "1998-01-04"
                  td/fact-1 6
                  td/org "A"}
                 {td/country td/country-c
                  "date" "1998-01-04"
                  td/fact-1 2
                  td/org ["A" "B"]}])
(def group-attributes-country-year {:attributes [td/country "year"]
                                    :reduce-date? true})
(def grouped-data-country-year {{td/country td/country-a
                                 "date" "1997"} [{td/country td/country-a
                                                  "date" "1997"
                                                  td/fact-1 12
                                                  td/org ["A" "B"]}
                                                 {td/country td/country-a
                                                  "date" "1997"
                                                  td/fact-1 6
                                                  td/org ["B" "C"]}
                                                 {td/country td/country-a
                                                  "date" "1997"
                                                  td/fact-1 18
                                                  td/org "C"}]
                                {td/country td/country-c
                                 "date" "1997"} [{td/country td/country-c
                                                  "date" "1997"
                                                  td/fact-1 16
                                                  td/org "CC"}]
                                {td/country td/country-c
                                 "date" "1998"} [{td/country td/country-c
                                                  "date" "1998"
                                                  td/fact-1 6
                                                  td/org "A"}
                                                 {td/country td/country-c
                                                  "date" "1998"
                                                  td/fact-1 2
                                                  td/org ["A" "B"]}]})

(def group-attributes-country-year-date {:attributes [td/country "year"]})
(def grouped-data-country-year-date {{td/country td/country-a
                                      "date" "1997"} [{td/country td/country-a
                                                       "date" "1997-01-01"
                                                       td/fact-1 12
                                                       td/org ["A" "B"]}
                                                      {td/country td/country-a
                                                       "date" "1997-05-01"
                                                       td/fact-1 6
                                                       td/org ["B" "C"]}
                                                      {td/country td/country-a
                                                       "date" "1997-05-04"
                                                       td/fact-1 18
                                                       td/org "C"}]
                                     {td/country td/country-c
                                      "date" "1997"} [{td/country td/country-c
                                                       "date" "1997-05-04"
                                                       td/fact-1 16
                                                       td/org "CC"}]
                                     {td/country td/country-c
                                      "date" "1998"} [{td/country td/country-c
                                                       "date" "1998-01-04"
                                                       td/fact-1 6
                                                       td/org "A"}
                                                      {td/country td/country-c
                                                       "date" "1998-01-04"
                                                       td/fact-1 2
                                                       td/org ["A" "B"]}]})

(def group-attributes-org-month {:attributes [td/org "month"]
                                 :reduce-date? true})
(def grouped-data-org-month {{td/org "A"
                              "date" "1997-01"} [{td/country td/country-a
                                                  "date" "1997-01"
                                                  td/fact-1 12
                                                  td/org "A"}]
                             {td/org "B"
                              "date" "1997-01"} [{td/country td/country-a
                                                  "date" "1997-01"
                                                  td/fact-1 12
                                                  td/org "B"}]
                             {td/org "B"
                              "date" "1997-05"} [{td/country td/country-a
                                                  "date" "1997-05"
                                                  td/fact-1 6
                                                  td/org "B"}]
                             {td/org "C"
                              "date" "1997-05"} [{td/country td/country-a
                                                  "date" "1997-05"
                                                  td/fact-1 6
                                                  td/org "C"}
                                                 {td/country td/country-a
                                                  "date" "1997-05"
                                                  td/fact-1 18
                                                  td/org "C"}]
                             {td/org "CC"
                              "date" "1997-05"} [{td/country td/country-c
                                                  "date" "1997-05"
                                                  td/fact-1 16
                                                  td/org "CC"}]
                             {td/org "A"
                              "date" "1998-01"} [{td/country td/country-c
                                                  "date" "1998-01"
                                                  td/fact-1 6
                                                  td/org "A"}
                                                 {td/country td/country-c
                                                  "date" "1998-01"
                                                  td/fact-1 2
                                                  td/org "A"}]
                             {td/org "B"
                              "date" "1998-01"} [{td/country td/country-c
                                                  "date" "1998-01"
                                                  td/fact-1 2
                                                  td/org "B"}]})

(def group-attributes-org-month-date {:attributes [td/org "month"]})
(def grouped-data-org-month-date {{td/org "A"
                                   "date" "1997-01"} [{td/country td/country-a
                                                       "date" "1997-01-01"
                                                       td/fact-1 12
                                                       td/org "A"}]
                                  {td/org "B"
                                   "date" "1997-01"} [{td/country td/country-a
                                                       "date" "1997-01-01"
                                                       td/fact-1 12
                                                       td/org "B"}]
                                  {td/org "B"
                                   "date" "1997-05"} [{td/country td/country-a
                                                       "date" "1997-05-01"
                                                       td/fact-1 6
                                                       td/org "B"}]
                                  {td/org "C"
                                   "date" "1997-05"} [{td/country td/country-a
                                                       "date" "1997-05-01"
                                                       td/fact-1 6
                                                       td/org "C"}
                                                      {td/country td/country-a
                                                       "date" "1997-05-04"
                                                       td/fact-1 18
                                                       td/org "C"}]
                                  {td/org "CC"
                                   "date" "1997-05"} [{td/country td/country-c
                                                       "date" "1997-05-04"
                                                       td/fact-1 16
                                                       td/org "CC"}]
                                  {td/org "A"
                                   "date" "1998-01"} [{td/country td/country-c
                                                       "date" "1998-01-04"
                                                       td/fact-1 6
                                                       td/org "A"}
                                                      {td/country td/country-c
                                                       "date" "1998-01-04"
                                                       td/fact-1 2
                                                       td/org "A"}]
                                  {td/org "B"
                                   "date" "1998-01"} [{td/country td/country-c
                                                       "date" "1998-01-04"
                                                       td/fact-1 2
                                                       td/org "B"}]})

(t/deftest test-group-data
  (t/testing "Testing group data by country year"
    (t/is (= (group-data group-attributes-country-year input-data)
             grouped-data-country-year)))
  (t/testing "Testing group data by org month"
    (t/is (= (group-data group-attributes-org-month input-data)
             grouped-data-org-month)))
  (t/testing "Testing group data by country year with complete-date"
    (t/is (= (group-data group-attributes-country-year-date input-data)
             grouped-data-country-year-date)))
  (t/testing "Testing group data by org month with complete-date"
    (t/is (= (group-data group-attributes-org-month-date input-data)
             grouped-data-org-month-date))))

(def simple-average-calculation-country-year
  [:/
   nil
   [:sum {:attribute td/fact-1}
    [:group-by {:attributes [td/country "year"]
                :reduce-date? true}
     "di-1"]]
   [:count-events nil
    [:group-by {:attributes [td/country "year"]
                :reduce-date? true}
     "di-1"]]])

(def simple-average-calculation-org-month
  [:/ nil
   [:sum {:attribute td/fact-1}
    [:group-by {:attributes [td/org "month"]
                :reduce-date? true}
     "di-1"]]
   [:count-events nil
    [:group-by {:attributes [td/org "month"]
                :reduce-date? true}
     "di-1"]]])

(def average-country-year {{td/country td/country-a
                            "date" "1997"} 12
                           {td/country td/country-c
                            "date" "1997"} 16
                           {td/country td/country-c
                            "date" "1998"} 4})
(def average-org-month {{"date" "1997-01"
                         td/org "A"} 12
                        {"date" "1997-01"
                         td/org "B"} 12
                        {"date" "1997-05"
                         td/org "B"} 6
                        {"date" "1997-05"
                         td/org "C"} 12
                        {"date" "1997-05"
                         td/org "CC"} 16
                        {"date" "1998-01"
                         td/org "A"} 4
                        {"date" "1998-01"
                         td/org "B"} 2})

(def normalize-calculation-org-month
  [:normalize
   {:attribute td/fact-1
    :result-name "indicator"
    :range-min 0
    :range-max 100
    :all-data? false}
   [:group-by
    {:attributes [td/org "month"]
     :reduce-date? true}
    "di-1"]])
(def normalize-calculation-org-month-healed
  [:heal-event
   {:policy :vals}
   normalize-calculation-org-month])
(def normalize-calculation-org-month-healed-2
  [:heal-event
   {:policy :vals
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "indicator-type"
              :value "indicator1"}]}
   normalize-calculation-org-month])
(defn normalize-calculation-country-year [flag]
  [:normalize
   {:attribute td/fact-1
    :result-name "indicator"
    :range-min 0
    :range-max 100
    :all-data? flag}
   [:group-by
    {:attributes [td/country "year"]
     :reduce-date? true}
    "di-1"]])
(def normalize-calculation-country-year-list
  [:normalize
   {:attribute td/fact-1
    :result-name "indicator"
    :range-min 0
    :range-max 100}
   "di-1"])


(def normalized-org-month
  {{td/org "A", "date" "1997-01"} [{td/country td/country-a, "date" "1997-01", td/fact-1 12, td/org "A", "indicator" 100}]
   {td/org "B", "date" "1997-01"} [{td/country td/country-a, "date" "1997-01", td/fact-1 12, td/org "B", "indicator" 100}]
   {td/org "B", "date" "1997-05"} [{td/country td/country-a, "date" "1997-05", td/fact-1 6, td/org "B", "indicator" 100}]
   {td/org "C", "date" "1997-05"} [{td/country td/country-a, "date" "1997-05", td/fact-1 6, td/org "C", "indicator" 0}
                                   {td/country td/country-a, "date" "1997-05", td/fact-1 18, td/org "C", "indicator" 100}]
   {td/org "CC", "date" "1997-05"} [{td/country td/country-c, "date" "1997-05", td/fact-1 16, td/org "CC", "indicator" 100}]
   {td/org "A", "date" "1998-01"} [{td/country td/country-c, "date" "1998-01", td/fact-1 6, td/org "A", "indicator" 100}
                                   {td/country td/country-c, "date" "1998-01", td/fact-1 2, td/org "A", "indicator" 0}]
   {td/org "B", "date" "1998-01"} [{td/country td/country-c, "date" "1998-01", td/fact-1 2, td/org "B", "indicator" 100}]})

(def normalized-org-month-healed
  #{{td/country td/country-a, "date" "1997-01", td/fact-1 12, td/org "B", "indicator" 100}
    {td/country td/country-a, "date" "1997-01", td/fact-1 12, td/org "A", "indicator" 100}
    {td/country td/country-a, "date" "1997-05", td/fact-1 6, td/org "B", "indicator" 100}
    {td/country td/country-a, "date" "1997-05", td/fact-1 6, td/org "C", "indicator" 0}
    {td/country td/country-a, "date" "1997-05", td/fact-1 18, td/org "C", "indicator" 100}
    {td/country td/country-c, "date" "1997-05", td/fact-1 16, td/org "CC", "indicator" 100}
    {td/country td/country-c, "date" "1998-01", td/fact-1 6, td/org "A", "indicator" 100}
    {td/country td/country-c, "date" "1998-01", td/fact-1 2, td/org "A", "indicator" 0}
    {td/country td/country-c, "date" "1998-01", td/fact-1 2, td/org "B", "indicator" 100}})

(def normalized-org-month-healed-2
  #{{td/country td/country-a, "date" "1997-01", td/fact-1 12, td/org "B", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-a, "date" "1997-01", td/fact-1 12, td/org "A", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-a, "date" "1997-05", td/fact-1 6, td/org "B", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-a, "date" "1997-05", td/fact-1 6, td/org "C", "indicator" 0, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-a, "date" "1997-05", td/fact-1 18, td/org "C", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-c, "date" "1997-05", td/fact-1 16, td/org "CC", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-c, "date" "1998-01", td/fact-1 6, td/org "A", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-c, "date" "1998-01", td/fact-1 2, td/org "A", "indicator" 0, "datasource" "indicator1", "indicator-type" "indicator1"}
    {td/country td/country-c, "date" "1998-01", td/fact-1 2, td/org "B", "indicator" 100, "datasource" "indicator1", "indicator-type" "indicator1"}})

(def normalized-country-year
  {{td/country td/country-a, "date" "1997"} [{td/country td/country-a, "date" "1997", td/fact-1 12, td/org ["A" "B"], "indicator" 50N}
                                             {td/country td/country-a, "date" "1997", td/fact-1 6, td/org ["B" "C"], "indicator" 0}
                                             {td/country td/country-a, "date" "1997", td/fact-1 18, td/org "C", "indicator" 100}],
   {td/country td/country-c, "date" "1997"} [{td/country td/country-c, "date" "1997", td/fact-1 16, td/org "CC", "indicator" 100}],
   {td/country td/country-c, "date" "1998"} [{td/country td/country-c, "date" "1998", td/fact-1 6, td/org "A", "indicator" 100}
                                             {td/country td/country-c, "date" "1998", td/fact-1 2, td/org ["A" "B"], "indicator" 0}]})

(def normalized-country-year-all-data
  {{td/country td/country-a, "date" "1997"} [{td/country td/country-a, "date" "1997", td/fact-1 12, td/org ["A" "B"], "indicator" (/ 125 2)}
                                             {td/country td/country-a, "date" "1997", td/fact-1 6, td/org ["B" "C"], "indicator" 25N}
                                             {td/country td/country-a, "date" "1997", td/fact-1 18, td/org "C", "indicator" 100}]
   {td/country td/country-c, "date" "1997"} [{td/country td/country-c, "date" "1997", td/fact-1 16, td/org "CC", "indicator" (/ 175 2)}]
   {td/country td/country-c, "date" "1998"} [{td/country td/country-c, "date" "1998", td/fact-1 6, td/org "A", "indicator" 25N}
                                             {td/country td/country-c, "date" "1998", td/fact-1 2, td/org ["A" "B"], "indicator" 0}]})

(def normalized-country-year-list
  (list {td/country td/country-a, "date" "1997-01-01", td/fact-1 12, td/org ["A" "B"], "indicator" (/ 125 2)}
        {td/country td/country-a, "date" "1997-05-01", td/fact-1 6, td/org ["B" "C"], "indicator" 25N}
        {td/country td/country-a, "date" "1997-05-04", td/fact-1 18, td/org "C", "indicator" 100}
        {td/country td/country-c, "date" "1997-05-04", td/fact-1 16, td/org "CC", "indicator" (/ 175 2)}
        {td/country td/country-c, "date" "1998-01-04", td/fact-1 6, td/org "A", "indicator" 25N}
        {td/country td/country-c, "date" "1998-01-04", td/fact-1 2, td/org ["A" "B"], "indicator" 0}))


(def result-top-events [{td/country td/country-a
                         "date" "1997-01-01"
                         td/fact-1 12
                         td/org ["A" "B"]}
                        {td/country td/country-a
                         "date" "1997-05-01"
                         td/fact-1 6
                         td/org ["B" "C"]}])

(def result-top-groups [{td/country td/country-c
                         "date" "1998-01-04"
                         td/fact-1 6
                         td/org "A"}
                        {td/country td/country-c
                         "date" "1998-01-04"
                         td/fact-1 2
                         td/org ["A" "B"]}])

(def result-top-groups-number
  [{td/country td/country-a, "date" "1997-05-01", td/fact-1 6, td/org "C"}
   {td/country td/country-a, "date" "1997-05-04", td/fact-1 18, td/org "C"}])

(def min-calculation-country-year
  [:min {:attribute td/fact-1}
   [:group-by {:attributes [td/country "year"]
               :reduce-date? true}
    "di-1"]])
(def min-calculation-org-month
  [:min {:attribute td/fact-1}
   [:group-by {:attributes [td/org "month"]
               :reduce-date? true}
    "di-1"]])

(def min-org-month {{"date" "1997-01", td/org "A"} 12
                    {"date" "1997-01", td/org "B"} 12
                    {"date" "1997-05", td/org "B"} 6
                    {"date" "1997-05", td/org "C"} 6
                    {"date" "1997-05", td/org "CC"} 16
                    {"date" "1998-01", td/org "A"} 2
                    {"date" "1998-01", td/org "B"} 2})
(def min-country-year {{td/country td/country-a, "date" "1997"} 6
                       {td/country td/country-c, "date" "1997"} 16
                       {td/country td/country-c, "date" "1998"} 2})

(def max-calculation-org-month [:max {:attribute td/fact-1}
                                [:group-by {:attributes [td/org "month"]
                                            :reduce-date? true}
                                 "di-1"]])
(def max-calculation-countr-year [:max {:attribute td/fact-1}
                                  [:group-by {:attributes [td/country "year"]
                                              :reduce-date? true}
                                   "di-1"]])
(def max-org-month {{"date" "1997-01", td/org "A"} 12
                    {"date" "1997-01", td/org "B"} 12
                    {"date" "1997-05", td/org "B"} 6
                    {"date" "1997-05", td/org "C"} 18
                    {"date" "1997-05", td/org "CC"} 16
                    {"date" "1998-01", td/org "A"} 6
                    {"date" "1998-01", td/org "B"} 2})
(def max-country-year {{td/country td/country-a, "date" "1997"} 18
                       {td/country td/country-c, "date" "1997"} 16
                       {td/country td/country-c, "date" "1998"} 6})

(comment
  (of/perform-operation {"di-1" input-data}
                        {}
                        [:group-by
                         {:attributes [td/country "year"]
                          :reduce-date? true}
                         "di-1"]))



(t/deftest test-apply-calculation
  (t/testing "Simple average calculation"
    (t/is (= average-country-year
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   simple-average-calculation-country-year)))
    (t/is (= average-org-month
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   simple-average-calculation-org-month))))
  (t/testing "Normalization"
    (t/is (= normalized-country-year
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   (normalize-calculation-country-year false))))
    (t/is (= normalized-org-month
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   normalize-calculation-org-month)))
    (t/is (= normalized-org-month-healed
             (set
              (of/perform-operation {"di-1" input-data}
                                    {}
                                    normalize-calculation-org-month-healed))))
    (t/is (= normalized-org-month-healed-2
             (set
              (of/perform-operation {"di-1" input-data}
                                    {}
                                    normalize-calculation-org-month-healed-2))))
    (t/is (= normalized-country-year-all-data
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   (normalize-calculation-country-year true))))
    (t/is (= normalized-country-year-list
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   normalize-calculation-country-year-list))))
  (t/testing "Min"
    (t/is (= min-country-year
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   min-calculation-country-year)))
    (t/is (= min-org-month
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   min-calculation-org-month))))
  (t/testing "Max"
    (t/is (= max-country-year
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   max-calculation-countr-year)))
    (t/is (= max-org-month
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   max-calculation-org-month)))))

(def datasource-a-data [{td/country td/country-a
                         "date" "1997-01-04"
                         td/org ["a" "b"]
                         td/fact-1 0}
                        {td/country td/country-a
                         "date" "1997-01-31"
                         td/org "a"
                         td/fact-1 20}
                        {td/country td/country-a
                         "date" "1997-05-05"
                         td/org "b"
                         td/fact-1 5}
                        {td/country td/country-a
                         "date" "1997-06-01"
                         td/org "c"
                         td/fact-1 1000}
                        {td/country td/country-a
                         "date" "1997-10-12"
                         td/org "c"
                         td/fact-1 300}])
(def datasource-e-data [{td/country td/country-a
                         "date" "1997"
                         "indicator" "Control of Corruption"
                         td/fact-2 20}])

(def control-fact-1-indicator
  [:+ nil
   [:* nil
    [:normalize {:range-min 0
                 :range-max 100}
     [:sum {:attribute td/fact-1}
      [:group-by {:attributes ["year" td/country]
                  :reduce-date? true}
       "di-1"]]]
    0.5] ;Weight value
   [:* {:attribute td/fact-2}
    [:group-by {:attributes ["year" td/country]
                :reduce-date? true}
     "di-2"]
    0.5]]) ;Weight value

(def select-all-orgs
  [:distinct {:attribute td/org}
   [:group-by {:attributes ["year" td/country]
               :reduce-date? true}
    "di-1"]])

(def select-all-country
  [:distinct {:attribute td/country}
   [:group-by {:attributes ["year" td/country]
               :reduce-date? true}
    "di-1"]])

(def heal-control-fact-1-indicator
  [:heal-event
   {:policy :merge
    :descs [{:attribute "indicator"}
            {:attribute td/org}]}
   control-fact-1-indicator
   select-all-orgs])

(def heal-control-fact-1-indicator-2
  [:heal-event
   {:policy :merge
    :descs [{:attribute "indicator"}
            {:attribute td/org}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]}
   control-fact-1-indicator
   select-all-orgs])

(def heal-control-fact-1-force-type-int
  [:heal-event
   {:policy :merge
    :descs [{:attribute "indicator"}
            {:attribute td/org}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]
    :force-type [{:attribute "indicator"
                  :new-type :integer}]}
   control-fact-1-indicator
   select-all-orgs])

(def heal-control-fact-1-force-type-str
  [:heal-event
   {:policy :merge
    :descs [{:attribute "indicator"}
            {:attribute td/org}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]
    :force-type [{:attribute "indicator"
                  :new-type :string}]}
   control-fact-1-indicator
   select-all-orgs])

(def heal-event-fix-date-year
  [:heal-event
   {:policy :merge
    :workaround {"date" {:month "01"
                         :day "01"}}
    :descs [{:attribute "indicator"}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]}
   [:sum {:attribute td/fact-1}
    [:group-by {:attributes ["year" td/country]
                :reduce-date? true}
     "di-1"]]])

(def heal-event-fix-date-month
  [:heal-event
   {:policy :merge
    :workaround {"date" {:month "01"
                         :day "01"}}
    :descs [{:attribute "indicator"}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]}
   [:sum {:attribute td/fact-1}
    [:group-by {:attributes ["month" td/country]
                :reduce-date? true}
     "di-1"]]])

(def heal-event-fix-date-month-uuids
  [:heal-event
   {:policy :merge
    :generate-ids {:policy :uuid}
    :workaround {"date" {:month "01"
                         :day "01"}}
    :descs [{:attribute "indicator"}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]}
   [:sum {:attribute td/fact-1}
    [:group-by {:attributes ["month" td/country]
                :reduce-date? true}
     "di-1"]]])

(def heal-event-fix-date-month-select-keys
  [:heal-event
   {:policy :merge
    :generate-ids {:policy :select-vals
                   :keys ["datasource" "date"]}
    :workaround {"date" {:month "01"
                         :day "01"}}
    :descs [{:attribute "indicator"}]
    :addons [{:attribute "datasource"
              :value "indicator1"}
             {:attribute "notes"
              :value "description"}
             {:attribute "indicator-type"
              :value "indicator1"}]}
   [:sum {:attribute td/fact-1}
    [:group-by {:attributes ["month" td/country]
                :reduce-date? true}
     "di-1"]]])

(def heal-event-noe
  [:heal-event {:policy :merge,
                :generate-ids {:policy :select-vals
                               :keys ["datasource" "date"]}
                :workaround {"date" {:month "01", :day "01"}}
                :descs [{:attribute "indicator"}]
                :addons [{:attribute "datasource", :value "indicator"}
                         {:attribute "notes", :value ""}
                         {:attribute "indicator-type", :value "sum"}]}
   [:count-events {:attribute :number-of-events}
    [:group-by {:attributes ["year"]}
     "di-1"]]])

(def heal-event-month
  [:heal-event {:policy :merge,
                :generate-ids {:policy :select-vals
                               :keys ["datasource" "date"]}
                :workaround {"date" {:month "01", :day "01"}}
                :descs [{:attribute "indicator"}]
                :addons [{:attribute "datasource", :value "indicator"}
                         {:attribute "notes", :value ""}
                         {:attribute "indicator-type", :value "sum"}]}
   [:distinct {:attribute "month"}
    [:group-by {:attributes ["year"]}
     "di-1"]]])

(def top-events
  [:sort-by {:attribute "date"
             :attribute-types {"date" :date}
             :direction :asc
             :take {:value 2}}
   "di-1"])

(def top-groups
  [:heal-event {:policy :vals}
   [:sort-by {:attribute "date"
              :attribute-types {"date" :date}
              :direction :desc
              :take {:value 1}}
    [:group-by {:attributes ["year"]}
     "di-1"]]])

(def top-groups-number
  [:heal-event {:policy :vals}
   [:sort-by {:attribute td/fact-1
              :attribute-types {td/fact-1 :number}
              :aggregate [:sum {:attribute td/fact-1}]
              :direction :desc
              :take {:value 1}}
    [:group-by {:attributes [td/org]}
     "di-1"]]])

(def heal-event-fix-date-month-result
  [{"date"           "1997-05-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      5}
   {"date"           "1997-06-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      1000}
   {"date"           "1997-01-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      20}
   {"date"           "1997-10-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      300}])

(def heal-event-fix-date-month-select-keys-result
  [{"id"             "indicator1-1997-05-01"
    "date"           "1997-05-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      5}
   {"id"             "indicator1-1997-06-01"
    "date"           "1997-06-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      1000}
   {"id"             "indicator1-1997-01-01"
    "date"           "1997-01-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      20}
   {"id"             "indicator1-1997-10-01"
    "date"           "1997-10-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      300}])

(def heal-event-fix-date-year-result
  [{"date"           "1997-01-01"
    td/country        td/country-a
    "datasource"     "indicator1"
    "notes"          "description"
    "indicator-type" "indicator1"
    "indicator"      1325}])

(def control-fact-1-indicator-result
  {{td/country td/country-a
    "date" "1997"}
   60.0})

(def control-fact-1-indicator-result-healed
  [{td/country td/country-a
    "date" "1997"
    "indicator" 60.0
    td/org ["a" "b" "c"]}])

(def control-fact-1-indicator-result-healed-2
  [{td/country td/country-a
    "date" "1997"
    "indicator" 60.0
    td/org ["a" "b" "c"]
    "datasource" "indicator1"
    "notes" "description"
    "indicator-type" "indicator1"}])

(def control-fact-1-indicator-result-forced-int
  [{td/country td/country-a
    "date" "1997"
    "indicator" 60
    td/org ["a" "b" "c"]
    "datasource" "indicator1"
    "notes" "description"
    "indicator-type" "indicator1"}])

(def control-fact-1-indicator-result-forced-str
  [{td/country td/country-a
    "date" "1997"
    "indicator" #?(:clj "60.0"
                   :cljs "60")
    td/org ["a" "b" "c"]
    "datasource" "indicator1"
    "notes" "description"
    "indicator-type" "indicator1"}])

(def heal-event-noe-result
  [{"date"           "1997-01-01"
    "datasource"     "indicator"
    "notes"          ""
    "indicator-type" "sum"
    "indicator"      4
    "id"             "indicator-1997-01-01"}
   {"date"           "1998-01-01"
    "datasource"     "indicator"
    "notes"          ""
    "indicator-type" "sum"
    "indicator"      2
    "id"             "indicator-1998-01-01"}])

(def heal-event-month-result
  [{"date"           "1997-01-01"
    "datasource"     "indicator"
    "notes"          ""
    "indicator-type" "sum"
    "indicator"      ["01" "05"]
    "id"             "indicator-1997-01-01"}
   {"date"           "1998-01-01"
    "datasource"     "indicator"
    "notes"          ""
    "indicator-type" "sum"
    "indicator"      ["01"]
    "id"             "indicator-1998-01-01"}])

(t/deftest complex-indicator
  (t/testing ""
    (t/is (= control-fact-1-indicator-result
             (of/perform-operation {"di-1" datasource-a-data
                                    "di-2" datasource-e-data}
                                   {}
                                   control-fact-1-indicator)))
    (t/is (= control-fact-1-indicator-result-healed
             (of/perform-operation {"di-1" datasource-a-data
                                    "di-2" datasource-e-data}
                                   {}
                                   heal-control-fact-1-indicator)))
    (t/is (= control-fact-1-indicator-result-healed-2
             (of/perform-operation {"di-1" datasource-a-data
                                    "di-2" datasource-e-data}
                                   {}
                                   heal-control-fact-1-indicator-2)))
    (t/is (= (sort-by-date heal-event-fix-date-month-result)
             (sort-by-date (of/perform-operation {"di-1" datasource-a-data
                                                  "di-2" datasource-e-data}
                                                 {}
                                                 heal-event-fix-date-month))))
    (t/is (= heal-event-fix-date-year-result
             (of/perform-operation {"di-1" datasource-a-data
                                    "di-2" datasource-e-data}
                                   {}
                                   heal-event-fix-date-year)))
    (t/is (= control-fact-1-indicator-result-forced-int
             (of/perform-operation {"di-1" datasource-a-data
                                    "di-2" datasource-e-data}
                                   {}
                                   heal-control-fact-1-force-type-int)))
    (t/is (= control-fact-1-indicator-result-forced-str
             (of/perform-operation {"di-1" datasource-a-data
                                    "di-2" datasource-e-data}
                                   {}
                                   heal-control-fact-1-force-type-str)))
    (t/is (= heal-event-noe-result
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   heal-event-noe)))
    (t/is (= heal-event-month-result
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   heal-event-month)))
    (t/is (= result-top-events
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   top-events)))
    (t/is (= result-top-groups
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   top-groups)))
    (t/is (= result-top-groups-number
             (of/perform-operation {"di-1" input-data}
                                   {}
                                   top-groups-number)))
    (let [result (of/perform-operation {"di-1" datasource-a-data
                                        "di-2" datasource-e-data}
                                       {}
                                       heal-event-fix-date-month-uuids)]
      (t/is (every? #(get % "id") result))
      (t/is (= (sort-by-date heal-event-fix-date-month-result)
               (sort-by-date (mapv #(dissoc % "id") result)))))
    (t/is (= (sort-by-date heal-event-fix-date-month-select-keys-result)
             (sort-by-date (of/perform-operation {"di-1" datasource-a-data
                                                  "di-2" datasource-e-data}
                                                 {}
                                                 heal-event-fix-date-month-select-keys))))))
