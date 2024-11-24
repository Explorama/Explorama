(ns de.explorama.backend.charts.components.base-charts-test
  "Testing basic datasets (without labels, styling, etc.) calculations 
   which are use by all base charts (line, bar, scatter, bubble, pie)"
  (:require [clojure.pprint :as pprint]
            [clojure.set :refer [rename-keys]]
            [clojure.test :refer [deftest is testing]]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.backend.charts.data.api :as data-api]
            [de.explorama.backend.charts.data.core :as charts-core]
            [de.explorama.backend.charts.data.fetch :as data-fetch]
            [de.explorama.backend.charts.data.helper :as charts-helper]
            [de.explorama.shared.common.test-data :as td]
            [taoensso.timbre :refer [error]]))

(def month-key :month)
(def year-key :year)
(def date "date")
(def year "year")
(def month "month")
(def datasource "datasource")
(def country td/country)
(def fact-1 td/fact-1)
(def event-type td/category-1)
(def org td/org)

(def number-of-events
  :number-of-events)

(def all-attributes [datasource country year month date fact-1 event-type org number-of-events])

(def all-label "All")
(def sum-by-all
  "all")
(def sum-filter-empty
  #{})
(def event-type-characteristics
  #{(td/category-val "A" 1) (td/category-val "A" 2)})

(def month-characteristics
  #{"08" "03" "01" "02"})

(def year-characteristics
  #{"1998" "1997" "1999"})

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3", "datasource" td/datasource-c, td/org [(td/org-val 2) (td/org-val 5) (td/org-val 4)], "location" [[15 15]], "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-03-20", "notes" "Text", td/fact-1 6}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-03-10", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "7", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1999-01-03", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "8", "datasource" td/datasource-c, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0}
   {td/country td/country-b, td/category-1 (td/category-val "A" 2), "id" "2", "datasource" td/datasource-a, td/org (td/org-val 5), "location" [[15 15]], "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 30}])

(def datasource-labels
  [td/datasource-a td/datasource-c])

(def datasource-results
  {td/datasource-c {:fact-1-sum 8
                    :fact-1-average (float 1.0)
                    :fact-1-median (float 0.0)
                    :fact-1-max 6
                    :fact-1-min 0
                    :number-of-events 8}
   td/datasource-a {:fact-1-sum 30
                    :fact-1-average (float 30.0)
                    :fact-1-median (float 30.0)
                    :fact-1-max 30
                    :fact-1-min 30
                    :number-of-events 1}})

(def country-labels
  [td/country-a td/country-b])

(def country-results
  {td/country-a {:fact-1-sum 8
                 :fact-1-average (float 1.0)
                 :fact-1-median (float 0.0)
                 :fact-1-max 6
                 :fact-1-min 0
                 :number-of-events 8}
   td/country-b {:fact-1-sum 30
                 :fact-1-average (float 30.0)
                 :fact-1-median (float 30.0)
                 :fact-1-max 30
                 :fact-1-min 30
                 :number-of-events 1}})

(def event-type-labels
  [(td/category-val "A" 2) (td/category-val "A" 1)])

(def event-type-results
  {(td/category-val "A" 2) {:fact-1-sum 37
                            :fact-1-average (float 4.625)
                            :fact-1-median (float 0.0)
                            :fact-1-max 30
                            :fact-1-min 0
                            :number-of-events 8}
   (td/category-val "A" 1) {:fact-1-sum 1
                            :fact-1-average (float 1.0)
                            :fact-1-median (float 1.0)
                            :fact-1-max 1
                            :fact-1-min 1
                            :number-of-events 1}})

(def org-labels
  [(td/org-val 2) (td/org-val 4) (td/org-val 6) (td/org-val 5)])

(def org-results
  {(td/org-val 2) {:fact-1-sum 1
                   :fact-1-average (float 1.0)
                   :fact-1-median (float 1.0)
                   :fact-1-max 1
                   :fact-1-min 1
                   :number-of-events 1}
   (td/org-val 4) {:fact-1-sum 1
                   :fact-1-average (float 1.0)
                   :fact-1-median (float 1.0)
                   :fact-1-max 1
                   :fact-1-min 1
                   :number-of-events 1}
   (td/org-val 6) {:fact-1-sum 7
                   :fact-1-average (float 1.0)
                   :fact-1-median (float 0.0)
                   :fact-1-max 6
                   :fact-1-min 0
                   :number-of-events 7}
   (td/org-val 5) {:fact-1-sum 31
                   :fact-1-average (float 15.5)
                   :fact-1-median (float 15.5)
                   :fact-1-max 30
                   :fact-1-min 1
                   :number-of-events 2}})

(def month-labels
  ["1997-10" "1997-11" "1997-12"
   "1998-01" "1998-02" "1998-03" "1998-04" "1998-05" "1998-06" "1998-07" "1998-08" "1998-09" "1998-10" "1998-11" "1998-12"
   "1999-01"])

(def month-results
  {"1997-10" {:fact-1-sum 30
              :fact-1-average (float 15.0)
              :fact-1-median (float 15.0)
              :fact-1-max 30
              :fact-1-min 0
              :number-of-events 2}
   "1997-11" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1997-12" {:fact-1-sum 0
              :fact-1-average (float 0.0)
              :fact-1-median (float 0.0)
              :fact-1-max 0
              :fact-1-min 0
              :number-of-events 1}
   "1998-01" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-02" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-03" {:fact-1-sum 7
              :fact-1-average (float 3.5)
              :fact-1-median (float 3.5)
              :fact-1-max 6
              :fact-1-min 1
              :number-of-events 2}
   "1998-04" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-05" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-06" {:fact-1-sum 0
              :fact-1-average (float 0.0)
              :fact-1-median (float 0.0)
              :fact-1-max 0
              :fact-1-min 0
              :number-of-events 1}
   "1998-07" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-08" {:fact-1-sum 1
              :fact-1-average (float 0.5)
              :fact-1-median (float 0.5)
              :fact-1-max 1
              :fact-1-min 0
              :number-of-events 2}
   "1998-09" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-10" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-11" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1998-12" {:fact-1-sum nil
              :fact-1-average nil
              :fact-1-median nil
              :fact-1-max nil
              :fact-1-min nil
              :number-of-events 0}
   "1999-01" {:fact-1-sum 0
              :fact-1-average (float 0.0)
              :fact-1-median (float 0.0)
              :fact-1-max 0
              :fact-1-min 0
              :number-of-events 1}})

(def year-labels ["1997" "1998" "1999"])

(def year-results
  {"1997" {:fact-1-sum 30
           :fact-1-average (float 10.0)
           :fact-1-median (float 0.0)
           :fact-1-max 30
           :fact-1-min 0
           :number-of-events 3}
   "1998" {:fact-1-sum 8
           :fact-1-average (float 1.6)
           :fact-1-median (float 1.0)
           :fact-1-max 6
           :fact-1-min 0
           :number-of-events 5}
   "1999" {:fact-1-sum 0
           :fact-1-average (float 0.0)
           :fact-1-median (float 0.0)
           :fact-1-max 0
           :fact-1-min 0
           :number-of-events 1}})

(defn- result-helper
  ([result-map result-labels target-attr relevant-keys]
   (vec (sort-by #(get % fact-1)
                 (mapv (fn [k]
                         (-> (get result-map k)
                             (assoc target-attr k)
                             (select-keys relevant-keys)
                             (rename-keys {:fact-1-sum fact-1
                                           :fact-1-average fact-1
                                           :fact-1-median fact-1
                                           :fact-1-max fact-1
                                           :fact-1-min fact-1})))
                       result-labels)))))

(def ^:private aggregation-methods (->> dfl-agg/descs
                                        vals
                                        (filter (fn [{:keys [need-attribute? result-type]}]
                                                  (and need-attribute?
                                                       (= result-type "number"))))
                                        (map :attribute)
                                        set))
(defn- aggr-attr [attr aggregation-method]
  (if (= aggregation-method number-of-events)
    number-of-events
    (keyword (str attr "-" (name aggregation-method)))))

(defn- show-by-all [{:keys [aggregation-method test-vars] :as desc}]
  (let [{:keys [expected-result-attr expected-labels expected-results]} test-vars
        y-axis (if (= aggregation-method number-of-events)
                 number-of-events
                 fact-1)
        desc (-> desc
                 (assoc :y-axis y-axis
                        :y-target-access y-axis
                        :sum-by sum-by-all
                        :sum-filter sum-filter-empty))]
    (is (= (list
            [{sum-by-all all-label}
             (result-helper expected-results expected-labels expected-result-attr [expected-result-attr (aggr-attr y-axis aggregation-method)])])
           (update-in (vec (charts-helper/chart-grouping data (charts-helper/gen-grouping-desc desc)))
                      [0 1]
                      #(vec (sort-by (fn [a] (get a fact-1)) %))))
        (str "Failed for the following params\n" (with-out-str (pprint/pprint (dissoc desc :test-vars)))))))

(defn- simple-show-by
  ([data x-axis y-axis show-by characteristics aggregation-method]
   (let [desc (charts-helper/gen-grouping-desc {:y-axis y-axis
                                                :x-axis x-axis
                                                :y-target-access y-axis
                                                :sum-by show-by
                                                :sum-filter characteristics
                                                :aggregation-method aggregation-method})]
     (charts-helper/chart-grouping data desc)))
  ([data x-axis y-axis show-by characteristics]
   (simple-show-by data x-axis y-axis show-by characteristics :sum))
  ([x-axis y-axis show-by characteristics]
   (simple-show-by data x-axis y-axis show-by characteristics)))

(deftest show-by-all-test
  (testing "show-by-all"
    (doseq [aggregation-method aggregation-methods]
      (show-by-all {:y-axis fact-1
                    :x-axis datasource
                    :aggregation-method aggregation-method
                    :test-vars {:expected-result-attr datasource
                                :expected-labels datasource-labels
                                :expected-results datasource-results}})
      (show-by-all {:y-axis fact-1
                    :x-axis country
                    :aggregation-method aggregation-method
                    :test-vars {:expected-result-attr country
                                :expected-labels country-labels
                                :expected-results country-results}})
      (show-by-all {:y-axis fact-1
                    :x-axis year
                    :aggregation-method aggregation-method
                    :test-vars {:expected-result-attr date
                                :expected-labels year-labels
                                :expected-results year-results}})
      (show-by-all {:y-axis fact-1
                    :x-axis month
                    :aggregation-method aggregation-method
                    :test-vars {:expected-result-attr date
                                :expected-labels month-labels
                                :expected-results month-results}})
      (show-by-all {:y-axis fact-1
                    :x-axis org
                    :aggregation-method aggregation-method
                    :test-vars {:expected-result-attr org
                                :expected-labels org-labels
                                :expected-results org-results}})

      (show-by-all {:y-axis fact-1
                    :x-axis event-type
                    :aggregation-method aggregation-method
                    :test-vars {:expected-result-attr event-type
                                :expected-labels event-type-labels
                                :expected-results event-type-results}}))))

(deftest show-by-test
  (testing "x year, y fact-1, show-by month"
    (is (= (list
            [{month-key "01"}
             [{year-key "1997", fact-1 nil, month-key "01"}
              {year-key "1998", fact-1 nil, month-key "01"}
              {year-key "1999", fact-1 0  , month-key "01"}]]
            [{month-key "02"}
             [{year-key "1997", fact-1 nil, month-key "02"}
              {year-key "1998", fact-1 nil, month-key "02"}
              {year-key "1999", fact-1 nil, month-key "02"}]]
            [{month-key "03"}
             [{year-key "1997", fact-1 nil, month-key "03"}
              {year-key "1998", fact-1 7, month-key "03"}
              {year-key "1999", fact-1 nil, month-key "03"}]]
            [{month-key "08"}
             [{year-key "1997", fact-1 nil, month-key "08"}
              {year-key "1998", fact-1 1  , month-key "08"}
              {year-key "1999", fact-1 nil, month-key "08"}]])
           (simple-show-by year fact-1 month month-characteristics))))

  (testing "x year, y number-of-events, show-by month"
    (is (= (list
            [{month-key "01"}
             [{year-key "1997", number-of-events 0, month-key "01"}
              {year-key "1998", number-of-events 0, month-key "01"}
              {year-key "1999", number-of-events 1, month-key "01"}]]
            [{month-key "02"}
             [{year-key "1997", number-of-events 0, month-key "02"}
              {year-key "1998", number-of-events 0, month-key "02"}
              {year-key "1999", number-of-events 0, month-key "02"}]]
            [{month-key "03"}
             [{year-key "1997", number-of-events 0, month-key "03"}
              {year-key "1998", number-of-events 2, month-key "03"}
              {year-key "1999", number-of-events 0, month-key "03"}]]
            [{month-key "08"}
             [{year-key "1997", number-of-events 0, month-key "08"}
              {year-key "1998", number-of-events 2, month-key "08"}
              {year-key "1999", number-of-events 0, month-key "08"}]])
           (simple-show-by year number-of-events month month-characteristics)))))

(testing "x country, y fact-1, show-by datasource aggregate by average"
  (is (= (list [{"datasource" td/datasource-a}
                [{td/country td/country-a, "datasource" td/datasource-a}
                 {td/country td/country-b, "datasource" td/datasource-a, td/fact-1 30}]]
               [{"datasource" td/datasource-c}
                [{td/country td/country-a, "datasource" td/datasource-c, td/fact-1  8}
                 {td/country td/country-b, "datasource" td/datasource-c}]])
         (simple-show-by data country fact-1 datasource (set datasource-labels) :sum))))

(testing "x country, y fact-1, show-by datasource aggregate by average"
  (is (= (list [{"datasource" td/datasource-a}
                [{td/country td/country-a, "datasource" td/datasource-a}
                 {td/country td/country-b, "datasource" td/datasource-a, td/fact-1 30}]]
               [{"datasource" td/datasource-c}
                [{td/country td/country-a, "datasource" td/datasource-c, td/fact-1 0}
                 {td/country td/country-b, "datasource" td/datasource-c}]])
         (simple-show-by data country fact-1 datasource (set datasource-labels) :min))))


(testing "x country, y fact-1, show-by datasource aggregate by average"
  (is (= (list [{"datasource" td/datasource-a}
                [{td/country td/country-a, "datasource" td/datasource-a}
                 {td/country td/country-b, "datasource" td/datasource-a, td/fact-1 30}]]
               [{"datasource" td/datasource-c}
                [{td/country td/country-a, "datasource" td/datasource-c, td/fact-1 6}
                 {td/country td/country-b, "datasource" td/datasource-c}]])
         (simple-show-by data country fact-1 datasource (set datasource-labels) :max))))

(testing "x country, y fact-1, show-by datasource aggregate by average"
  (is (= (list [{"datasource" td/datasource-a}
                [{td/country td/country-a, "datasource" td/datasource-a}
                 {td/country td/country-b, "datasource" td/datasource-a, td/fact-1 (float 30.0)}]]
               [{"datasource" td/datasource-c}
                [{td/country td/country-a, "datasource" td/datasource-c, td/fact-1  (float 1.0)}
                 {td/country td/country-b, "datasource" td/datasource-c}]])
         (simple-show-by data country fact-1 datasource (set datasource-labels) :average))))

(testing "x country, y fact-1, show-by datasource aggregate by median"
  (is (= (list [{"datasource" td/datasource-a}
                [{td/country td/country-a, "datasource" td/datasource-a}
                 {td/country td/country-b, "datasource" td/datasource-a, td/fact-1 (float 30.0)}]]
               [{"datasource" td/datasource-c}
                [{td/country td/country-a, "datasource" td/datasource-c, td/fact-1  (float 0.0)}
                 {td/country td/country-b, "datasource" td/datasource-c}]])
         (simple-show-by data country fact-1 datasource (set datasource-labels) :median))))

(defn- negative-test-helper [{:keys [x-axis y-axis sum-by] :as chart-params}]
  (doseq [chart-type charts-core/multiple-axis-chart-types]
    (let [chart-params (assoc chart-params :type chart-type)]
      (data-api/chart-data {:client-callback #(do (error "Negative check failed with params" chart-params)
                                                  (is false))
                            :failed-callback (fn [resp]
                                               (is (= {:error-desc
                                                       {:error :same-attributes,
                                                        :selections (cond-> {}
                                                                      x-axis (assoc :x-axis-attribute-label x-axis)
                                                                      y-axis (assoc :y-axis-attribute-label y-axis)
                                                                      sum-by (assoc :sum-by-label sum-by))}}
                                                      resp)))}
                           [chart-params]))))

(defn- positive-test-helper [chart-params]
  (doseq [chart-type charts-core/non-axis-chart-types]
    (let [chart-params (assoc chart-params :type chart-type)]
      (data-api/chart-data {:client-callback (fn [_]
                                               (is true))
                            :failed-callback (fn [_]
                                               (error "Positive check failed with params" chart-params)
                                               (is false))}
                           [chart-params]))))

(def get-full-data @#'data-fetch/get-full-data)

(deftest negative-test
  (testing "testing not supported selection"
    (with-redefs [get-full-data (fn [_] data)]
      (doseq [attribute all-attributes]
        (let [chart-params {:x-axis attribute
                            :y-axis attribute}]
          (negative-test-helper chart-params)
          (positive-test-helper chart-params)))

      (doseq [attribute all-attributes]
        (let [chart-params {:x-axis attribute
                            :sum-by attribute}]
          (negative-test-helper chart-params)
          (positive-test-helper chart-params)))))

  (testing "testing same-time-selection"
    (with-redefs [get-full-data  (fn [_] data)]
      (data-api/chart-data
       {:client-callback #(is false)
        :failed-callback (fn [resp]
                           (is (= {:error-desc
                                   {:error :same-time-selection,
                                    :selections {:x-axis-attribute-label month
                                                 :sum-by-label year}}}
                                  resp)))}
       [{:x-axis month
         :sum-by year}])))
    (testing "testing unhandled exception"
      (with-redefs [charts-core/chart-data (fn [_] (throw (ex-info "Test unknown error" {})))]
        (data-api/chart-data
         {:client-callback #(is false)
          :failed-callback (fn [resp]
                             (is (= {:error-desc {:error :unknown}}
                                    resp)))}
         [{:x-axis month
           :y-axis fact-1}]))))