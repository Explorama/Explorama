(ns de.explorama.backend.algorithms.resources.problem-types)

(def value
  {:data-tests {:desc-key :data-tests-desc
                :sort 2
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
                                                     :multi-select? true}}
                :button-label :data-test-button
                :algorithms [:trend :seasonality :white-noise]}
   :linear-model {:desc-key :linear-model-desc
                  :sort 1
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
                                                       :multi-select? true}}
                  :requirements [{:defined-as :dependent-variable
                                  :input-config {:numeric {:missing-value
                                                           {:type :missing-value
                                                            :method :ignore
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
                                  :input-config {:date {:default {:granularity :year
                                                                  :simple? true
                                                                  :given? true}}}
                                  :target-config {:numeric {:future-values {:method :range
                                                                            :step 1
                                                                            :start-value {:type :max}}}}
                                  :number :single}]
                  :button-label :predict-button
                  :algorithms [:lmmr :lmtts :er :aarima :pnr :nlr :linear-regression]}
   #_#_:k-means-model {:desc-key :k-means-model-desc
                       :requirements [{:defined-as :feature
                                       :input-config {:numeric {:missing-value {:method :ignore
                                                                                :given? true
                                                                                :replacement :average
                                                                                :value 0}}
                                                      :categoric {:missing-value
                                                                  {:method :ignore
                                                                   :replacement :string
                                                                   :value "Missing"
                                                                   :given? true}}
                                                      :shared {:hide [:aggregation :multiple-values-per-event :merge-policy]}}
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
                                                  :default :no}}
                       :show-prediction? false
                       :algorithms [:k-means-pal :k-means]}})
