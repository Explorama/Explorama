(ns de.explorama.frontend.algorithms.components.helper-test
  (:require [de.explorama.frontend.algorithms.components.helper :as m]
            [de.explorama.shared.common.test-data :as td]
            [cljs.test :refer-macros [deftest testing is]]))

(def procedures
  {:lmtts
   {:desc-key :lmtts-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}}
                    :types [:numeric]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default
                                          {:given? true
                                           :granularity :year}}
                                   :numeric {:missing-value
                                             {:given? true
                                              :default true
                                              :method :ignore
                                              :replacement :average
                                              :value 0}
                                             :continues-value
                                             {:given? true
                                              :method :range
                                              :step 1
                                              :min :min
                                              :min-value 0
                                              :max :max
                                              :max-value 100}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :ignore-incomplete}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? :greater-zero
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :trend {:row 1
                        :type :double
                        :content-valid? [:greater-zero [:<= 1]]
                        :default 1.0}
                :affect-future-only {:row 2
                                     :type :selection
                                     :options [:all :future-only]
                                     :active? [:trend [:< 1]]
                                     :default :future-only}
                :seasonality {:row 3
                              :type :selection
                              :options [:no-seasonality :user-input :automatic]
                              :default :no-seasonality}
                :periods {:row 4
                          :type :integer
                          :content-valid? :greater-zero
                          :active? [:seasonality :user-input]
                          :ignore? true
                          :default 30}
                :measure-name {:row 5
                               :type :selection
                               :ignore? true
                               :options [:mpe :mse :rmse :et :mad :mase :wmape :smape :mape]
                               :default :mse}
                :seasonal-handle-method {:row 6
                                         :type :selection
                                         :options [:average :fitting]
                                         :active? [:seasonality :automatic]
                                         :default :average}
                :expost-flag {:row 7
                              :type :selection
                              :options [:yes :no]
                              :default :no}
                :ignore-zero {:row 8
                              :type :selection
                              :options [:use-zeros :ignore]
                              :active? [:measure-name :mpe :mape]
                              :default :use-zeros}}}
   :lmmr
   {:desc-key :lmmr-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}}
                    :types [:numeric]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :granularity :year}}
                                   :numeric {:missing-value
                                             {:given? true
                                              :default true
                                              :method :ignore
                                              :replacement :average
                                              :value 0}
                                             :continues-value
                                             {:given? true
                                              :step 1
                                              :method :range
                                              :min :min
                                              :min-value 0
                                              :max :max
                                              :max-value 100}}
                                   :shared {:aggregation :multiple
                                            :multiple-values-per-event :multiply
                                            :merge-policy :ignore-incomplete}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :multi}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? :greater-zero
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :algorithms {:row 1
                             :type :selection
                             :options [:qr-decomposition :svd
                                       :cyclical-coordinate-descent
                                       :cholesky-decomposition
                                       :alternating]
                             :default :qr-decomposition
                             :content-valid? {:cyclical-coordinate-descent [:variable-selection :all]
                                              :alternating [:variable-selection :all]}}
                :variable-selection {:row 2
                                     :type :selection
                                     :options [:all :forward :backward]
                                     :default :all
                                     :content-valid? {:forward [:algorithms
                                                                :qr-decomposition :svd
                                                                :cholesky-decomposition]
                                                      :backward [:algorithms
                                                                 :qr-decomposition :svd
                                                                 :cholesky-decomposition]}}
                :category-col {:row 3
                               :type :integer
                               :content-valid? :greater-zero
                               :ignore? true
                               :default 1}
                :p-value-forward {:row 4
                                  :type :double
                                  :content-valid? :greater-zero
                                  :active? [:variable-selection :forward]
                                  :default 0.05}
                :p-value-backward {:row 5
                                   :type :double
                                   :content-valid? :greater-zero
                                   :active? [:variable-selection :backward]
                                   :default 0.1}
                :penalization-weight {:row 6
                                      :type :double
                                      :content-valid? :zero
                                      :active? [:algorithms :cyclical-coordinate-descent :alternating]
                                      :default 0}
                :penalization-method {:row 7
                                      :type :selection
                                      :options [:lasso :ridge]
                                      :default :lasso
                                      :active? [:algorithms :cyclical-coordinate-descent :alternating]}
                :max-iteration {:row 8
                                :type :integer
                                :content-valid? :greater-zero
                                :active? [:algorithms :cyclical-coordinate-descent :alternating]
                                :default 100000}
                :threshold {:row 9
                            :type :double
                            :content-valid? :zero
                            :active? [:algorithms :cyclical-coordinate-descent]
                            :default 0.0000001}
                :tolerance-abs {:row 10
                                :type :double
                                :content-valid? :zero
                                :active? [:algorithms :alternating]
                                :default 0.0000001}
                :tolerance-rel {:row 11
                                :type :double
                                :content-valid? :zero
                                :active? [:algorithms :alternating]
                                :default 0.0000001}
                :step-size {:row 12
                            :type :double
                            :content-valid? :zero
                            :active? [:algorithms :alternating]
                            :default 1.8}}}
   :k-means
   {:desc-key :k-means-desc
    :hidden? false
    :result-type :pie-chart
    :requirements [{:defined-as :feature
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :given? true
                                              :replacement :average
                                              :value 0}}
                                   :date {:default
                                          {:type :date-aggregation
                                           :given? true
                                           :granularity :year
                                           :aggregation :sum}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :ignore-incomplete}}
                    :types [:numeric :date :categoric]
                    :number       :multi}]
    ;; checkout de.explorama.algorithms.framework.weka.core/instance `train` method destructuring
    :parameter    {:group-number {:row     0
                                  :type    :integer
                                  :default 30}}}
   :k-means-pal
   {:desc-key :k-means-desc
    :hidden? false
    :result-type :pie-chart
    :requirements [{:defined-as :feature
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :given? true
                                              :replacement :average
                                              :value 0
                                              :enum? false}}
                                   :date {:default
                                          {:type :date-aggregation
                                           :given? true
                                           :granularity :year
                                           :aggregation :sum}}
                                   :categoric {:encoding {:given? true
                                                          :encoding :ordinal}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :ignore-incomplete}}
                    :types [:numeric :date :categoric]
                    :number :multi}]
    :parameter {:group-number {:row 0
                               :type :integer
                               :default 2}
                :group-number-min {:row 1
                                   :type :integer
                                   :default :no}
                :group-number-max {:row 2
                                   :type :integer
                                   :default :no}
                :distance-level {:row 3
                                 :type :selection
                                 :options [:manhattan :euclidean :minkowski :chebyshev :cosine]
                                 :default :euclidean}
                :minkowski-power {:row 4
                                  :type :double
                                  :default 3.0}
                :category-weights {:row 5
                                   :type :double
                                   :default 0.707}
                :max-iteration {:row 6
                                :type :integer
                                :default 100}
                :init-type {:row 7
                            :type :selection
                            :options [:fist-k :random-with-replacement :random-without-replacement :patent-init-center]
                            :default :patent-init-center}
                :normalization {:row 8
                                :type :selection
                                :options [:no-n :yes :min-max]
                                :default :no-n}
                :thread-ratio {:row 9
                               :type :double
                               :default 0}
                :exit-threshold {:row 10
                                 :type :double
                                 :default 1e-6}}}})

(def problem-types
  {:linear-model {:desc-key :linear-model-desc
                  :simple-parameter {:length {:row 0
                                              :type :integer
                                              :content-valid? :greater-zero
                                              :default 3}}
                  :parameter {:group-by-country {:row 0
                                                 :type :boolean
                                                 :default false}}
                  :requirements [{:defined-as :dependent-variable
                                  :input-config {:numeric {:missing-value
                                                           {:type :missing-value
                                                            :method :ignore
                                                            :replacement :average
                                                            :value 0}}}
                                  :types [:numeric]
                                  :number :single}
                                 {:defined-as :independent-variable
                                  :types [:date]
                                  :input-config {:date {:default {:granularity :year
                                                                  :given? true}}
                                                 :shared {:aggregation :sum
                                                          :multiple-values-per-event :multiply
                                                          :merge-policy :ignore-incomplete
                                                          :hide [:multiple-values-per-event]}}
                                  :target-config {:numeric {:future-values {:method :range
                                                                            :step 1
                                                                            :start-value {:type :max}}}}
                                  :number :single}]
                  :algorithms [:lmmr :lmtts]}
   :k-means-model {:desc-key :k-means-model-desc
                   :requirements [{:defined-as :feature
                                   :input-config {:numeric {:missing-value {:method :ignore
                                                                            :given? true
                                                                            :replacement :average
                                                                            :value 0}}
                                                  :date {:default {:type :date-aggregation
                                                                   :given? true
                                                                   :granularity :year
                                                                   :aggregation :sum}}
                                                  :categoric {}
                                                  :shared {:aggregation :sum
                                                           :merge-policy :ignore-incomplete}}
                                   :types [:numeric :date :categoric]
                                   :number :multi}]
                   :parameter {:group-number {:row 0
                                              :type :integer
                                              :default 2}}
                   :show-prediction? false
                   :algorithms [:k-means-pal]}})

(def dummy-problem-types problem-types)
(def dummy-procedures procedures)

(def test-attr {"date" :date
                td/fact-1 :numeric
                td/fact-3 :numeric})

(def settings-state-1
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date", :i 0, :type :value}
      {:label td/fact-3, :value td/fact-3, :i 0, :type :value}]
     :attribute-config
     {"date"
      {:granularity {:label "quartar", :value :quartar, :i 0, :type :value}}
      #_#_;? will be set by defaults
          td/fact-1
        {:missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                         :replacement {:label "number", :value :average, :i 0, :type :value}
                         :value 0}}}
     :shared
     {:aggregation {:label "average", :value :average, :i 0, :type :value}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1, :i 0, :type :value}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace, :i 0, :type :value}
                       :replacement {:label "number", :value :number, :i 0, :type :value}
                       :value 0.6}}}}}})

(def settings-state-clean-1
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date"}
      {:label td/fact-3, :value td/fact-3}]
     :attribute-config
     {"date"
      {:granularity
       {:label "quartar", :value :quartar}}
      td/fact-3
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "ignore", :value :ignore}
                       :replacement {:label "average", :value :average}
                       :value 0}
       :future-values {:method {:label "range" :value :range}
                       :step 1
                       :start-value {:type {:label "max" :value :max}}}}}
     :shared
     {:aggregation {:label "average", :value :average}
      :multiple-values-per-event {:label "multiply", :value :multiply}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace}
                       :replacement {:label "number", :value :number}
                       :value 0.6}}}
     :shared {}}}})

(def settings-state-2
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date", :i 0, :type :value}
      {:label td/fact-3, :value td/fact-3, :i 0, :type :value}]
     :attribute-config
     {td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value :i 0 :type :value}}
       :continues-value {:method {:label "range", :value :range, :i 0, :type :value}
                         :step 1
                         :max {:label "max", :value :max, :i 0, :type :value}
                         :min {:label "min", :value :min, :i 0, :type :value}}
       :missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                       :replacement {:label "number", :value :average, :i 0, :type :value}
                       :value 0}}
      "date"
      {:granularity {:label "quartar", :value :quartar, :i 0, :type :value}}}
     #_#_;? will be set by defaults
         td/fact-1
       {:missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                        :replacement {:label "number", :value :average, :i 0, :type :value}
                        :value 0}}
     :shared
     {:aggregation {:label "average", :value :average, :i 0, :type :value}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1, :i 0, :type :value}
     :attribute-config
     {td/fact-1
      {:missing-value {:method {:label "replace", :value :replace, :i 0, :type :value}
                       :replacement {:label "number", :value :number, :i 0, :type :value}
                       :value 0.6}}}}}})

(def settings-state-clean-2
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date"}
      {:label td/fact-3, :value td/fact-3}]
     :attribute-config
     {"date"
      {:granularity
       {:label "quartar", :value :quartar}}
      td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value}}
       :continues-value {:method {:label "range", :value :range}
                         :step 1
                         :max {:label "max", :value :max}
                         :min {:label "min", :value :min}}
       :future-values {:method {:label "range" :value :range}
                       :step 1
                       :start-value {:type {:label "max" :value :max}}}}}
     :shared
     {:aggregation {:label "average", :value :average}
      :multiple-values-per-event {:label "multiply", :value :multiply}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace}
                       :replacement {:label "number", :value :number}
                       :value 0.6}}}
     :shared {}}}})

(def settings-state-3
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date", :i 0, :type :value}
      {:label td/fact-3, :value td/fact-3, :i 0, :type :value}]
     :attribute-config
     {td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value :i 0 :type :value}}
       :continues-value {:method {:label "range", :value :range, :i 0, :type :value}
                         :step 1
                         :max {:label "max", :value :max, :i 0, :type :value}
                         :min {:label "min", :value :min, :i 0, :type :value}}
       :missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                       :replacement {:label "number", :value :average, :i 0, :type :value}
                       :value 0}
       :future-values {:method {:label "range", :value :range, :i 0, :type :value}
                       :step 1
                       :start-value {:type {:label "number", :value :number, :i 0, :type :value}
                                     :value 5}}}
      "date"
      {:granularity {:label "quartar", :value :quartar, :i 0, :type :value}}}
     #_#_;? will be set by defaults
         td/fact-1
       {:missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                        :replacement {:label "number", :value :average, :i 0, :type :value}
                        :value 0}}
     :shared
     {:aggregation {:label "average", :value :average, :i 0, :type :value}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1, :i 0, :type :value}
     :attribute-config
     {td/fact-1
      {:missing-value {:method {:label "replace", :value :replace, :i 0, :type :value}
                       :replacement {:label "number", :value :number, :i 0, :type :value}
                       :value 0.6}}}}}})

(def settings-state-clean-3
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date"}
      {:label td/fact-3, :value td/fact-3}]
     :attribute-config
     {"date"
      {:granularity
       {:label "quartar", :value :quartar}}
      td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value}}
       :continues-value {:method {:label "range", :value :range}
                         :step 1
                         :max {:label "max", :value :max}
                         :min {:label "min", :value :min}}
       :future-values {:method {:label "range", :value :range}
                       :step 1
                       :start-value {:type {:label "number", :value :number}
                                     :value 5}}}}
     :shared
     {:aggregation {:label "average", :value :average}
      :multiple-values-per-event {:label "multiply", :value :multiply}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace}
                       :replacement {:label "number", :value :number}
                       :value 0.6}}}
     :shared {}}}})

(def settings-state-4
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date", :i 0, :type :value}
      {:label td/fact-3, :value td/fact-3, :i 0, :type :value}]
     :attribute-config
     {td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value :i 0 :type :value}}
       :continues-value {:method {:label "range", :value :range, :i 0, :type :value}
                         :step 1
                         :max {:label "max", :value :max, :i 0, :type :value}
                         :min {:label "min", :value :min, :i 0, :type :value}}
       :missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                       :replacement {:label "number", :value :average, :i 0, :type :value}
                       :value 0}
       :future-values {:method {:label "manual", :value :manual, :i 0, :type :value}}}
      "date"
      {:granularity {:label "quartar", :value :quartar, :i 0, :type :value}}}
     #_#_;? will be set by defaults
         td/fact-1
       {:missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                        :replacement {:label "number", :value :average, :i 0, :type :value}
                        :value 0}}
     :shared
     {:aggregation {:label "average", :value :average, :i 0, :type :value}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1, :i 0, :type :value}
     :attribute-config
     {td/fact-1
      {:missing-value {:method {:label "replace", :value :replace, :i 0, :type :value}
                       :replacement {:label "number", :value :number, :i 0, :type :value}
                       :value 0.6}}}}}})

(def settings-state-clean-4
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date"}
      {:label td/fact-3, :value td/fact-3}]
     :attribute-config
     {"date"
      {:granularity
       {:label "quartar", :value :quartar}}
      td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value}}
       :continues-value {:method {:label "range", :value :range}
                         :step 1
                         :max {:label "max", :value :max}
                         :min {:label "min", :value :min}}
       :future-values {:method {:label "manual", :value :manual}}}}
     :shared
     {:aggregation {:label "average", :value :average}
      :multiple-values-per-event {:label "multiply", :value :multiply}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace}
                       :replacement {:label "number", :value :number}
                       :value 0.6}}}
     :shared {}}}})

(def settings-state-5
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date", :i 0, :type :value}
      {:label td/fact-3, :value td/fact-3, :i 0, :type :value}]
     :attribute-config
     {td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value :i 0 :type :value}}
       :continues-value {:method {:label "range", :value :range, :i 0, :type :value}
                         :step 1
                         :max {:label "number", :value :number, :i 0, :type :value}
                         :max-value 15
                         :min {:label "number", :value :number, :i 0, :type :value}
                         :min-value 1}
       :missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                       :replacement {:label "number", :value :average, :i 0, :type :value}
                       :value 0}
       :future-values {:method {:label "manual", :value :manual, :i 0, :type :value}}}
      "date"
      {:granularity {:label "quartar", :value :quartar, :i 0, :type :value}}}
     #_#_;? will be set by defaults
         td/fact-1
       {:missing-value {:method {:label "replace", :value :ignore, :i 0, :type :value}
                        :replacement {:label "number", :value :average, :i 0, :type :value}
                        :value 0}}
     :shared
     {:aggregation {:label "average", :value :average, :i 0, :type :value}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1, :i 0, :type :value}
     :attribute-config
     {td/fact-1
      {:missing-value {:method {:label "replace", :value :replace, :i 0, :type :value}
                       :replacement {:label "number", :value :number, :i 0, :type :value}
                       :value 0.6}}}}}})

(def settings-state-clean-5
  {:lmmr
   {:independent-variable
    {:value
     [{:label "date", :value "date"}
      {:label td/fact-3, :value td/fact-3}]
     :attribute-config
     {"date"
      {:granularity
       {:label "quartar", :value :quartar}}
      td/fact-3
      {:input-config {:active-config {:label "continues-value" :value :continues-value}}
       :continues-value {:method {:label "range", :value :range}
                         :step 1
                         :max {:label "number", :value :number}
                         :max-value 15
                         :min {:label "number", :value :number}
                         :min-value 1}
       :future-values {:method {:label "manual", :value :manual}}}}
     :shared
     {:aggregation {:label "average", :value :average}
      :multiple-values-per-event {:label "multiply", :value :multiply}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace}
                       :replacement {:label "number", :value :number}
                       :value 0.6}}}
     :shared {}}}})

(def future-data-4 {:future-data {0 {td/fact-3 1}
                                  1 {td/fact-3 2}}})

(def parameter-state
  {:lmmr
   {:algorithms
    {:value
     {:label "cholesky-decomposition"
      :value :cholesky-decomposition
      :i 2
      :type :value}}
    :category-col {:ignore? true, :value 3}
    :variable-selection
    {:value {:label "all", :value :all, :i 0, :type :value}}}})

(def parameter-state-clean
  {:lmmr
   {:algorithms
    {:value
     {:label "cholesky-decomposition"
      :value :cholesky-decomposition}}
    :variable-selection
    {:value {:label "all", :value :all}}}})

(def simple-parameter-state
  {:lmmr
   {:length {:value 4}}})

(def simple-parameter-state-clean
  {:lmmr
   {:length {:value 4}}})

(def goal-state
  {:choose-algorithm? true
   :current-algorithm :lmmr
   :choose-algorithm {:label "lmmr", :value :lmmr}})

(def prediction-structure-1 {:algorithm :lmmr
                             :parameter {:algorithms :cholesky-decomposition
                                         :length 4
                                         :variable-selection :all}
                             :attributes {:dependent-variable [{:value td/fact-1
                                                                :type :numeric
                                                                :given? nil
                                                                :missing-value {:method :replace
                                                                                :replacement :number
                                                                                :value 0.6}}]
                                          :independent-variable [{:value "date"
                                                                  :type :date
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :date-config {:granularity :quartar}}
                                                                 {:value td/fact-3
                                                                  :type :numeric
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :missing-value {:method :ignore
                                                                                  :replacement :average
                                                                                  :value 0}
                                                                  :future-values {:method :range
                                                                                  :step 1
                                                                                  :start-value {:type :max}}}]}})

(def prediction-structure-2 {:algorithm :lmmr
                             :parameter {:algorithms :cholesky-decomposition
                                         :length 4
                                         :variable-selection :all}
                             :attributes {:dependent-variable [{:value td/fact-1
                                                                :type :numeric
                                                                :given? nil
                                                                :missing-value {:method :replace
                                                                                :replacement :number
                                                                                :value 0.6}}]
                                          :independent-variable [{:value "date"
                                                                  :type :date
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :date-config {:granularity :quartar}}
                                                                 {:value td/fact-3
                                                                  :type :numeric
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :continues-value {:method :range
                                                                                    :step 1
                                                                                    :max {:type :max}
                                                                                    :min {:type :min}}
                                                                  :future-values {:method :range
                                                                                  :step 1
                                                                                  :start-value {:type :max}}}]}})

(def prediction-structure-3 {:algorithm :lmmr
                             :parameter {:algorithms :cholesky-decomposition
                                         :length 4
                                         :variable-selection :all}
                             :attributes {:dependent-variable [{:value td/fact-1
                                                                :type :numeric
                                                                :given? nil
                                                                :missing-value {:method :replace
                                                                                :replacement :number
                                                                                :value 0.6}}]
                                          :independent-variable [{:value "date"
                                                                  :type :date
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :date-config {:granularity :quartar}}
                                                                 {:value td/fact-3
                                                                  :type :numeric
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :continues-value {:method :range
                                                                                    :step 1
                                                                                    :max {:type :max}
                                                                                    :min {:type :min}}
                                                                  :future-values {:method :range
                                                                                  :step 1
                                                                                  :start-value {:type :start-value
                                                                                                :value 5}}}]}})

(def prediction-structure-4 {:algorithm :lmmr
                             :parameter {:algorithms :cholesky-decomposition
                                         :length 4
                                         :variable-selection :all}
                             :attributes {:dependent-variable [{:value td/fact-1
                                                                :type :numeric
                                                                :given? nil
                                                                :missing-value {:method :replace
                                                                                :replacement :number
                                                                                :value 0.6}}]
                                          :independent-variable [{:value "date"
                                                                  :type :date
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :date-config {:granularity :quartar}}
                                                                 {:value td/fact-3
                                                                  :type :numeric
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :continues-value {:method :range
                                                                                    :step 1
                                                                                    :max {:type :max}
                                                                                    :min {:type :min}}
                                                                  :future-values {:method :manual
                                                                                  :manual-values [1 2]}}]}})

(def prediction-structure-5 {:algorithm :lmmr
                             :parameter {:algorithms :cholesky-decomposition
                                         :length 4
                                         :variable-selection :all}
                             :attributes {:dependent-variable [{:value td/fact-1
                                                                :type :numeric
                                                                :given? nil
                                                                :missing-value {:method :replace
                                                                                :replacement :number
                                                                                :value 0.6}}]
                                          :independent-variable [{:value "date"
                                                                  :type :date
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :date-config {:granularity :quartar}}
                                                                 {:value td/fact-3
                                                                  :type :numeric
                                                                  :given? true
                                                                  :shared {:aggregation :average
                                                                           :multiple-values-per-event :multiply
                                                                           :merge-policy :ignore-incomplete}
                                                                  :continues-value {:method :range
                                                                                    :step 1
                                                                                    :max {:type :value
                                                                                          :value 15}
                                                                                    :min {:type :value
                                                                                          :value 1}}
                                                                  :future-values {:method :manual
                                                                                  :manual-values [1 2]}}]}})

(deftest transformation-test-algorithm
  (testing "basic test"
    (is (= prediction-structure-1
           (m/transform-inputs dummy-procedures
                               test-attr
                               dummy-problem-types
                               goal-state
                               settings-state-1
                               parameter-state
                               simple-parameter-state
                               {}))))
  (testing "basic test"
    (is (= prediction-structure-2
           (m/transform-inputs dummy-procedures
                               test-attr
                               dummy-problem-types
                               goal-state
                               settings-state-2
                               parameter-state
                               simple-parameter-state
                               {}))))
  (testing "basic test"
    (is (= prediction-structure-3
           (m/transform-inputs dummy-procedures
                               test-attr
                               dummy-problem-types
                               goal-state
                               settings-state-3
                               parameter-state
                               simple-parameter-state
                               {}))))
  (testing "basic test"
    (is (= prediction-structure-4
           (m/transform-inputs dummy-procedures
                               test-attr
                               dummy-problem-types
                               goal-state
                               settings-state-4
                               parameter-state
                               simple-parameter-state
                               {:future-data {0 {td/fact-3 1}
                                              1 {td/fact-3 2}}}))))
  (testing "basic test"
    (is (= prediction-structure-5
           (m/transform-inputs dummy-procedures
                               test-attr
                               dummy-problem-types
                               goal-state
                               settings-state-5
                               parameter-state
                               simple-parameter-state
                               {:future-data {0 {td/fact-3 1}
                                              1 {td/fact-3 2}}}))))
  (testing "enum? test"
    (let [prediction-structure {:parameter {}
                                :attributes {:feature [{:value td/fact-1
                                                        :type :numeric
                                                        :given? true
                                                        :missing-value {:method :replace
                                                                        :replacement :average
                                                                        :value 0
                                                                        :enum? true}
                                                        :shared {:aggregation :sum
                                                                 :multiple-values-per-event :multiply
                                                                 :merge-policy :ignore-incomplete}}]}
                                :algorithm :k-means-pal}
          goal-state {:choose-algorithm? true
                      :choose-algorithm {:label "K-Means Clustering", :value :k-means-pal}
                      :current-algorithm :k-means-pal}
          settings-state {:k-means-pal {:feature {:value [{:label td/fact-1, :value td/fact-1}]
                                                  :attribute-config {td/fact-1
                                                                     {:missing-value {:method {:label "Replace", :value :replace
                                                                                               :i 0, :type :value}
                                                                                      :enum? true}}}}}}]
      (is (= prediction-structure
             (m/transform-inputs
              procedures
              {td/fact-1 :numeric}
              {}
              goal-state
              settings-state
              {}
              {}
              {}))))))

(deftest backwards-transformation-test-algorithm
  (testing "basic test"
    (let [[goal
           settings
           parameter
           simple-parameter
           future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types prediction-structure-1 name)]
      (is (= goal-state goal))
      (is (= settings-state-clean-1 settings))
      (is (= parameter-state-clean parameter))
      (is (= simple-parameter-state-clean simple-parameter))
      (is (= {} future-data))))
  (testing "basic test"
    (let [[goal
           settings
           parameter
           simple-parameter
           future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types prediction-structure-2 name)]
      (is (= goal-state goal))
      (is (= settings-state-clean-2 settings))
      (is (= parameter-state-clean parameter))
      (is (= simple-parameter-state-clean simple-parameter))
      (is (= {} future-data))))
  (testing "basic test"
    (let [[goal
           settings
           parameter
           simple-parameter
           future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types prediction-structure-3 name)]
      (is (= goal-state goal))
      (is (= settings-state-clean-3 settings))
      (is (= parameter-state-clean parameter))
      (is (= simple-parameter-state-clean simple-parameter))
      (is (= {} future-data))))
  (testing "basic test"
    (let [[goal
           settings
           parameter
           simple-parameter
           future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types prediction-structure-4 name)]
      (is (= goal-state goal))
      (is (= settings-state-clean-4 settings))
      (is (= parameter-state-clean parameter))
      (is (= simple-parameter-state-clean simple-parameter))
      (is (= future-data-4 future-data))))
  (testing "basic test"
    (let [[goal
           settings
           parameter
           simple-parameter
           future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types prediction-structure-5 name)]
      (is (= goal-state goal))
      (is (= settings-state-clean-5 settings))
      (is (= parameter-state-clean parameter))
      (is (= simple-parameter-state-clean simple-parameter))
      (is (= future-data-4 future-data))))
  (testing "enum? test"
    (let [goal-state {:choose-algorithm? true
                      :current-algorithm :k-means-pal
                      :choose-algorithm {:label "k-means-pal", :value :k-means-pal}}
          settings-state-clean {:k-means-pal {:feature {:value [{:label td/fact-1, :value td/fact-1}]
                                                        :attribute-config {td/fact-1 {:input-config {:active-config {:label "missing-value"
                                                                                                                        :value :missing-value}}
                                                                                         :missing-value {:method {:label "replace", :value :replace}
                                                                                                         :replacement {:label "average", :value :average}
                                                                                                         :value 0
                                                                                                         :enum? true}}}
                                                        :shared {:merge-policy {:label "ignore-incomplete"
                                                                                :value :ignore-incomplete}}}}}
          prediction-structure {:parameter {}
                                :attributes {:feature [{:value td/fact-1
                                                        :type :numeric
                                                        :given? true
                                                        :missing-value {:method :replace
                                                                        :replacement :average
                                                                        :value 0
                                                                        :enum? true}
                                                        :shared {:aggregation nil
                                                                 :multiple-values-per-event nil
                                                                 :merge-policy :ignore-incomplete}}]}
                                :algorithm :k-means-pal}
          [goal settings parameter simple-parameter future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types prediction-structure name)]
      (is (= goal-state goal))
      (is (= settings-state-clean settings))
      (is (= {:k-means-pal {}} parameter))
      (is (= {:k-means-pal {}} simple-parameter))
      (is (= {} future-data)))))

(deftest requirement-test
  (testing "basic test"
    (is (true? (m/validate-requirements
                test-attr
                [{:defined-as :dependent-variable
                  :handle-missing-data? true
                  :defaults-missing-value {:method :ignore
                                           :replacement :average
                                           :value 0}
                  :types [:numeric]
                  :number :single}
                 {:defined-as :independent-variable
                  :types [:numeric :date]
                  :defaults {:date {:granularity :year
                                    :aggregation :multiple}}
                  :number :single}])))
    (is (nil? (m/validate-requirements
               nil
               [{:defined-as :dependent-variable
                 :handle-missing-data? true
                 :defaults-missing-value {:method :ignore
                                          :replacement :average
                                          :value 0}
                 :types [:numeric]
                 :number :single}
                {:defined-as :independent-variable
                 :types [:numeric :date]
                 :defaults {:date {:granularity :year
                                   :aggregation :multiple}}
                 :number :single}])))
    (is (nil? (m/validate-requirements
               test-attr
               nil)))
    (is (false? (m/validate-requirements
                 test-attr
                 [{:defined-as :dependent-variable
                   :handle-missing-data? true
                   :defaults-missing-value {:method :ignore
                                            :replacement :average
                                            :value 0}
                   :types [:categoric]
                   :number :single}
                  {:defined-as :independent-variable
                   :types [:numeric :date]
                   :defaults {:date {:granularity :year
                                     :aggregation :multiple}}
                   :number :single}])))))

(deftest flip-key-value->vec-test
  (testing "basic test"
    (is (= {:numeric [td/fact-1]
            :date ["date"]}
           (m/flip-key-value->vec
            {td/fact-1 :numeric
             "date" :date})))
    (is (= {:numeric [td/fact-2 td/fact-6 td/fact-4 td/fact-1]
            :date ["date"]
            :boolean ["failure?" "success?"]
            :categoric [td/org td/category-1]}
           (m/flip-key-value->vec
            {td/fact-1 :numeric
             "date" :date
             td/fact-2 :numeric
             "success?" :boolean
             "failure?" :boolean
             td/fact-4 :numeric
             td/fact-6 :numeric
             td/org :categoric
             td/category-1 :categoric})))
    (is (= {} (m/flip-key-value->vec nil)))
    (is (= {} (m/flip-key-value->vec {})))))

(def problem-type-goal-state
  {:choose-algorithm? false
   :problem-type {:label "linear-model", :value :linear-model}})

(def problem-type-simple-parameter-state
  {:linear-model
   {:length {:value 4}}})

(def problem-type-simple-parameter-state-clean
  {:linear-model
   {:length {:value 4}}})

(def problem-type-settings-state-1
  {:linear-model
   {:independent-variable
    {:value {:label "date", :value "date", :i 0, :type :value}
     :attribute-config
     {"date"
      {:granularity {:label "quartar", :value :quartar, :i 0, :type :value}}}
     :shared
     {:aggregation {:label "average", :value :average, :i 0, :type :value}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1, :i 0, :type :value}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace, :i 0, :type :value}
                       :replacement {:label "number", :value :number, :i 0, :type :value}
                       :value 0.6}}}}}})

(def problem-type-settings-state-clean-1
  {:linear-model
   {:independent-variable
    {:value
     {:label "date", :value "date"}
     :attribute-config
     {"date"
      {:granularity
       {:label "quartar", :value :quartar}}}
     :shared
     {:aggregation {:label "average", :value :average}
      :multiple-values-per-event {:label "multiply", :value :multiply}
      :merge-policy {:label "ignore-incomplete", :value :ignore-incomplete}}}
    :dependent-variable
    {:value
     {:label td/fact-1, :value td/fact-1}
     :attribute-config
     {td/fact-1
      {:input-config {:active-config {:label "missing-value"
                                      :value :missing-value}}
       :missing-value {:method {:label "replace", :value :replace}
                       :replacement {:label "number", :value :number}
                       :value 0.6}}}
     :shared {}}}})

(def problem-type-prediction-structure-1
  {:problem-type :linear-model
   :parameter {:length 4}
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :given? nil
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0.6}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :shared {:aggregation :average
                                                 :multiple-values-per-event :multiply
                                                 :merge-policy :ignore-incomplete}
                                        :date-config {:granularity :quartar}}]}})

(deftest transformation-test-problem-type
  (testing "basic test"
    (is (= problem-type-prediction-structure-1
           (m/transform-inputs dummy-procedures
                               test-attr
                               dummy-problem-types
                               problem-type-goal-state
                               problem-type-settings-state-1
                               parameter-state
                               problem-type-simple-parameter-state
                               {})))))

(deftest backwards-transformation-test-problem-type
  (testing "basic test"
    (let [[goal
           settings
           parameter
           simple-parameter
           future-data]
          (m/initialize-from-prediction dummy-procedures dummy-problem-types problem-type-prediction-structure-1 (fn [a] (name a)))]
      (is (= problem-type-goal-state goal))
      (is (= problem-type-settings-state-clean-1 settings))
      (is (= {:linear-model {}} parameter))
      (is (= problem-type-simple-parameter-state-clean simple-parameter))
      (is (= {} future-data)))))