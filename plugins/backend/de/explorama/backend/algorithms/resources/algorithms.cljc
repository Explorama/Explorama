(ns de.explorama.backend.algorithms.resources.algorithms)

(def value
  {:lmtts
   {:desc-key :lmtts-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date]
                    :input-config {:date {:default
                                          {:given? true
                                           :simple? true
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
                                              :max-value 0}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}
                :trend {:row 2
                        :type :double
                        :content-valid? [:greater-zero [:<= 1]]
                        :default 1.0}
                :affect-future-only {:row 3
                                     :type :selection
                                     :options [:all :future-only]
                                     :active? [:trend [:< 1]]
                                     :default :future-only}
                :seasonality {:row 4
                              :type :selection
                              :options [:no-seasonality :user-input :automatic]
                              :default :no-seasonality}
                :periods {:row 5
                          :type :integer
                          :content-valid? :greater-zero
                          :active? [:seasonality :user-input]
                          :ignore? true
                          :default 30}
                :measure-name {:row 6
                               :type :selection
                               :ignore? true
                               :options [:mpe :mse :rmse :et :mad :mase :wmape :smape :mape]
                               :default :mse}
                :seasonal-handle-method {:row 7
                                         :type :selection
                                         :options [:average :fitting]
                                         :active? [:seasonality :automatic]
                                         :default :average}
                :expost-flag {:row 8
                              :type :selection
                              :options [:yes :no]
                              :default :no}
                :ignore-zero {:row 9
                              :type :selection
                              :options [:use-zeros :ignore]
                              :active? [:measure-name :mpe :mape]
                              :default :use-zeros}}}
   :geor
   {:desc-key :geor-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :simple? true
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}}}
   :pnr
   {:desc-key :pnr-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :simple? true
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:polynomial-num {:row 1
                                        :type :integer
                                        :content-valid? [[:<= 1]]
                                        :default 2}
                       :length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}
                :algorithms {:row 2
                             :type :selection
                             :options [:lu :svd]
                             :default :lu}}}

   :lmmr
   {:desc-key :lmmr-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :simple? true
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
                                              :max-value 0}}}
                    :target-config {:numeric {:future-values {:method :auto
                                                              :start-value {:type :max}
                                                              :step 1}}}
                    :number :multi}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}
                :algorithms {:row 2
                             :type :selection
                             :options [:qr-decomposition :svd
                                       :cyclical-coordinate-descent
                                       :cholesky-decomposition
                                       :alternating]
                             :default :qr-decomposition
                             :content-valid? {:cyclical-coordinate-descent [:variable-selection :all]
                                              :alternating [:variable-selection :all]}}
                :variable-selection {:row 3
                                     :type :selection
                                     :options [:all :forward :backward]
                                     :default :all
                                     :content-valid? {:forward [:algorithms
                                                                :qr-decomposition :svd
                                                                :cholesky-decomposition]
                                                      :backward [:algorithms
                                                                 :qr-decomposition :svd
                                                                 :cholesky-decomposition]}}
                :category-col {:row 4
                               :type :integer
                               :content-valid? :greater-zero
                               :ignore? true
                               :default 1}
                :p-value-forward {:row 5
                                  :type :double
                                  :content-valid? :greater-zero
                                  :active? [:variable-selection :forward]
                                  :default 0.05}
                :p-value-backward {:row 6
                                   :type :double
                                   :content-valid? :greater-zero
                                   :active? [:variable-selection :backward]
                                   :default 0.1}
                :penalization-weight {:row 7
                                      :type :double
                                      :content-valid? :zero
                                      :active? [:algorithms :cyclical-coordinate-descent :alternating]
                                      :default 0}
                :penalization-method {:row 8
                                      :type :selection
                                      :options [:lasso :ridge]
                                      :default :lasso
                                      :active? [:algorithms :cyclical-coordinate-descent :alternating]}
                :max-iteration {:row 9
                                :type :integer
                                :content-valid? :greater-zero
                                :active? [:algorithms :cyclical-coordinate-descent :alternating]
                                :default 100000}
                :threshold {:row 10
                            :type :double
                            :content-valid? :zero
                            :active? [:algorithms :cyclical-coordinate-descent]
                            :default 0.0000001}
                :tolerance-abs {:row 11
                                :type :double
                                :content-valid? :zero
                                :active? [:algorithms :alternating]
                                :default 0.0000001}
                :tolerance-rel {:row 12
                                :type :double
                                :content-valid? :zero
                                :active? [:algorithms :alternating]
                                :default 0.0000001}
                :step-size {:row 13
                            :type :double
                            :content-valid? :zero
                            :active? [:algorithms :alternating]
                            :default 1.8}}}
   :er
   {:desc-key :er-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default
                                          {:given? true
                                           :simple? true
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}
                :algorithms {:row 2
                             :type :selection
                             :options [:lu :svd]
                             :default :lu}}}
   #_#_:k-means-pal
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
                                     :categoric {:missing-value
                                                 {:method :ignore
                                                  :replacement :string
                                                  :value "Missing"
                                                  :given? true}}
                                     :shared {:hide [:aggregation :multiple-values-per-event :merge-policy]
                                              :multiple-values-per-event :multiply
                                              :merge-policy :ignore-incomplete}}
                      :types [:numeric :categoric]
                      :number :multi}]
      :parameter {:group-by-country {:row 0
                                     :type :boolean
                                     :default false}
                  :aggregate-by-attribute {:row 1
                                           :type :selection
                                           :options-ref {:type :variable-reference
                                                         :defined-as :independent-variable
                                                         :default-prio-by-type [:date :numeric]}
                                           :multi-select? true}
                  :group-number {:row 2
                                 :type :integer
                                 :content-valid? :greater-one
                                 :default :no}
                  :group-number-min {:row 3
                                     :type :integer
                                     :content-valid? :greater-one
                                     :default 2}
                  :group-number-max {:row 4
                                     :type :integer
                                     :content-valid? :greater-two
                                     :default :no}
                  :distance-level {:row 5
                                   :type :selection
                                   :options [:manhattan :euclidean :minkowski :chebyshev :cosine]
                                   :default :euclidean}
                  :minkowski-power {:row 6
                                    :type :double
                                    :content-valid? :greater-zero
                                    :default 3.0}
                  :category-weights {:row 7
                                     :type :double
                                     :content-valid? :greater-zero
                                     :default 0.707}
                  :max-iteration {:row 8
                                  :type :integer
                                  :content-valid? :greater-one
                                  :default 100}
                  :init-type {:row 9
                              :type :selection
                              :options [:fist-k :random-with-replacement :random-without-replacement :patent-init-center]
                              :default :patent-init-center}
                  :normalization {:row 10
                                  :type :selection
                                  :options [:no-n :yes :min-max]
                                  :default :no-n}
                  :thread-ratio {:row 11
                                 :type :double
                                 :content-valid? :greater-zero
                                 :default 0}
                  :exit-threshold {:row 12
                                   :type :double
                                   :content-valid? :greater-zero
                                   :default 1e-6}}}
   :arima
   {:desc-key :arima-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default
                                          {:given? true
                                           :simple? true
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}
                :d {:row 2
                    :type :integer
                    :default 0}
                :p {:row 3
                    :type :integer
                    :default 2}
                :q {:row 4
                    :type :integer
                    :default 0}
                :method {:row 5
                         :type :selection
                         :options [:css :mle]
                         :default :mle}
                :seasonal-p {:row 6
                             :type :integer
                             :default 0}
                :seasonal-q {:row 7
                             :type :integer
                             :default 0}
                :seasonal-d {:row 8
                             :type :integer
                             :default 0}
                :seasonal-period {:row 9
                                  :type :integer
                                  :default 0}}}
   :aarima
   {:desc-key :aarima-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default
                                          {:given? true
                                           :simple? true
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :guess-states {:row 1
                               :type :boolean
                               :default true}
                :aggregate-by-attribute {:row 2
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}
                :search-strategy {:row 3
                                  :type :selection
                                  :options [:exhaust :stepwise]
                                  :default :stepwise}
                :max-order {:row 4
                            :type :integer
                            :default 15}
                :max-iterations {:row 5
                                 :type :integer
                                 :default 100}
                :seasonal-period {:row 6
                                  :type :integer
                                  :default -1
                                  :explanation :seasonal-period-info}
                :seasonality-criterion {:row 7
                                        :type :double
                                        :default 0.5}
                :d {:row 8
                    :type :integer
                    :default -1
                    :explanation :d-info}
                :kpss-sig-level {:row 9
                                 :type :double
                                 :default 0.05}
                :ch-sig-level {:row 10
                               :type :double
                               :default 0.05}
                :max-d {:row 11
                        :type :integer
                        :default 2}
                :max-p {:row 12
                        :type :integer
                        :default 5}
                :max-q {:row 13
                        :type :integer
                        :default 5}
                :seasonal-d {:row 14
                             :type :integer
                             :default -1
                             :explanation :seasonal-d-info}
                :max-seasonal-d {:row 15
                                 :type :integer
                                 :default 1}
                :initial-p {:row 16
                            :type :integer
                            :default 0}
                :initial-seasonal-p {:row 17
                                     :type :integer
                                     :default 0}
                :max-seasonal-p {:row 18
                                 :type :integer
                                 :default 2}
                :initial-q {:row 19
                            :type :integer
                            :default 0}
                :initial-seasonal-q {:row 20
                                     :type :integer
                                     :default 0}
                :max-seasonal-q {:row 21
                                 :type :integer
                                 :default 2}}}
   :trend
   {:desc-key :trend-desc
    :hidden? false
    :result-type :text
    :button-label :data-test-button
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date]
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :parameter {:aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date]}
                                         :multi-select? true}
                :alpha {:row 2
                        :type :double
                        :default 0.05}
                :method {:row 3
                         :type :selection
                         :options [:mk :difference-sign]
                         :default :mk}}}
   :white-noise
   {:desc-key :white-noise-desc
    :hidden? false
    :result-type :text
    :button-label :data-test-button
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date]
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :parameter {:aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date]}
                                         :multi-select? true}
                :lag {:row 2
                      :type :integer
                      :explanation :lag-info
                      :default 3}
                :probability {:row 3
                              :type :double
                              :default 0.9}}}
   :seasonality
   {:desc-key :seasonality-desc
    :hidden? false
    :result-type :text
    :button-label :data-test-button
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date]
                    :input-config {:date {:default
                                          {:given? true
                                           :granularity :month}}
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :parameter {:aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date]}
                                         :multi-select? true}
                :alpha {:row 2
                        :type :double
                        :default 0.2}
                :extrapolation {:row 3
                                :type :boolean
                                :default false}
                :smooth-width {:row 4
                               :type :integer
                               :explanation :smooth-width-info
                               :default 0}
                :auxiliary-normailty {:row 5
                                      :type :boolean
                                      :default false}}}
   :nlr
   {:desc-key :nlr-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :simple? true
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
                                              :max :max}}}
                    :target-config {:numeric {:future-values {:method :range
                                                              :step 1
                                                              :start-value {:type :max}}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}}}
   :lr-apache
   {:desc-key :lr-apache-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :simple? true
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
                                              :max-value 0}}}
                    :target-config {:numeric {:future-values {:method :auto
                                                              :start-value {:type :max}
                                                              :step 1}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}}}
   :linear-regression
   {:desc-key :linear-regression-desc
    :hidden? false
    :result-type :line-chart
    :requirements [{:defined-as :dependent-variable
                    :input-config {:numeric {:missing-value
                                             {:method :ignore
                                              :replacement :average
                                              :value 0}}
                                   :shared {:aggregation :sum
                                            :multiple-values-per-event :multiply
                                            :merge-policy :merge-incomplete
                                            :hide [:multiple-values-per-event :merge-policy]}}
                    :types [:numeric :number-of-events]
                    :number :single}
                   {:defined-as :independent-variable
                    :types [:date :numeric]
                    :input-config {:date {:default {:type :date-aggregation
                                                    :given? true
                                                    :simple? true
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
                                              :max-value 0}}}
                    :target-config {:numeric {:future-values {:method :auto
                                                              :start-value {:type :max}
                                                              :step 1}}}
                    :number :single}]
    :simple-parameter {:length {:row 0
                                :type :integer
                                :content-valid? [[:<= 1] [:>= 100]]
                                :default 3}}
    :parameter {:group-by-country {:row 0
                                   :type :boolean
                                   :default false}
                :aggregate-by-attribute {:row 1
                                         :type :selection
                                         :options-ref {:type :variable-reference
                                                       :defined-as :independent-variable
                                                       :default-prio-by-type [:date :numeric]}
                                         :multi-select? true}}}})
