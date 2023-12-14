(ns de.explorama.backend.indicator.defaults)

(def indicator-ui-descriptions
  {:division {:label :indicator-division
              :info :indicator-division-info
              :validation-desc {:fixed [:replace_A_attr_name :replace_A_di
                                        :replace_B_attr_name :replace_B_di
                                        :replace_aggregation_A
                                        :replace_aggregation_B
                                        :replace_time_grouping]
                                :generic {:replace_X_attr_name 1
                                          :replace_X_heal_attr_name 1
                                          :replace_aggregation_# 1
                                          :replace_X_di 1}}
              :ui {:definition-rows [{:label :indicator-attribute-selection
                                      :partition 2 ; => result in div col-6 elements for each two comps
                                      :comps
                                      [{:id "calc-attribute-a-select"
                                        :depends-on "calc-attribute-b-select"
                                        :label :indicator-attribute-a-label
                                        :type :select
                                        :content :calc-attributes
                                        :number-of-events {:op :count-events
                                                           :key :replace_aggregation_A}
                                        :replace :replace_A}
                                       {:id "aggregation-attribute-a-select"
                                        :depends-on "calc-attribute-a-select"
                                        :label :indicator-aggregation-label
                                        :type :select
                                        :content :aggregations
                                        :replace :replace_aggregation_A
                                        :disable [["calc-attribute-a-select" :number-of-events]]}
                                       {:id "calc-attribute-b-select"
                                        :depends-on "calc-attribute-a-select"
                                        :label :indicator-attribute-b-label
                                        :type :select
                                        :content :calc-attributes
                                        :number-of-events {:op :count-events
                                                           :key :replace_aggregation_B}
                                        :replace :replace_B}
                                       {:id "aggregation-attribute-b-select"
                                        :depends-on "calc-attribute-b-select"
                                        :label :indicator-aggregation-label
                                        :type :select
                                        :content :aggregations
                                        :replace :replace_aggregation_B
                                        :disable [["calc-attribute-b-select" :number-of-events]]}]}
                                     {:label :indicator-settings-label
                                      :comps
                                      [{:id "time-granularity-select"
                                        :label :indicator-time-granularity-label
                                        :type :select
                                        :content :time
                                        :replace :replace_time_grouping}
                                       {:id "grouping-select"
                                        :hint :indicator-grouping-hint
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
                                             :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                            {:id "additional-aggregation-select"
                                             :depends-on "additional-attribute-select"
                                             :label :indicator-aggregation-label
                                             :type :select
                                             :content :aggregations
                                             :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#
              :description [:heal-event {:policy :merge
                                         :generate-ids {:policy :uuid}
                                         :workaround {"date" {:month "01"
                                                              :day "01"}}
                                         :descs [:#
                                                 {:attribute :replace_defaults_indicator_name}
                                                 {:attribute :replace_X_heal_attr_name}]
                                         :addons [{:attribute "datasource" :value :replace-datasource-name}
                                                  {:attribute "notes" :value :replace-notes-value}
                                                  {:attribute "indicator-type" :value :replace-indicator-type}]
                                         :force-type [{:attribute :replace_defaults_indicator_name
                                                       :new-type :double}]}
                            [:/ nil
                             [:replace_aggregation_A {:attribute :replace_A_attr_name}
                              [:group-by {:attributes :replace_time_grouping}
                               :replace_A_di]]
                             [:replace_aggregation_B {:attribute :replace_B_attr_name}
                              [:group-by {:attributes :replace_time_grouping}
                               :replace_B_di]]]
                            [:#
                             [:replace_aggregation_# {:attribute :replace_X_attr_name}
                              [:group-by {:attributes :replace_time_grouping
                                          :reduce-date? true}
                               :replace_X_di]]]]}

   :sum {:label :indicator-sum
         :info :indicator-sum-info
         :validation-desc {:fixed [:replace_A_attr_name
                                   :replace_A_di
                                   :replace_aggregation_A
                                   :replace_time_grouping]
                           :generic {:replace_X_attr_name 1
                                     :replace_X_heal_attr_name 1
                                     :replace_aggregation_# 1
                                     :replace_X_di 1}}
         :ui {:definition-rows [{:label :indicator-attribute-selection
                                 :comps
                                 [{:id "calc-attribute-select"
                                   :label :indicator-attribute-label
                                   :type :select
                                   :content :calc-attributes
                                   :number-of-events {:op :count-events
                                                      :key :replace_aggregation_A
                                                      :default :sum}
                                   :replace :replace_A}]} ; => :replace_A_attr_name :replace_A_di
                                {:label :indicator-settings-label
                                 :comps
                                 [{:id "time-granularity-select"
                                   :label :indicator-time-granularity-label
                                   :type :select
                                   :content :time
                                   :replace :replace_time_grouping}
                                  {:id "grouping-select"
                                   :hint :indicator-grouping-hint
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
                                        :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                       {:id "additional-aggregation-select"
                                        :depends-on "additional-attribute-select"
                                        :label :indicator-aggregation-label
                                        :type :select
                                        :content :aggregations
                                        :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#
         :description [:heal-event {:policy :merge
                                    :generate-ids {:policy :uuid}
                                    :workaround {"date" {:month "01"
                                                         :day "01"}}
                                    :descs [:#
                                            {:attribute :replace_defaults_indicator_name}
                                            {:attribute :replace_X_heal_attr_name}]
                                    :addons [{:attribute "datasource" :value :replace-datasource-name}
                                             {:attribute "notes" :value :replace-notes-value}
                                             {:attribute "indicator-type" :value :replace-indicator-type}]}

                       [:replace_aggregation_A {:attribute :replace_A_attr_name}
                        [:group-by {:attributes :replace_time_grouping}
                         :replace_A_di]]
                       [:#
                        [:replace_aggregation_# {:attribute :replace_X_attr_name}
                         [:group-by {:attributes :replace_time_grouping
                                     :reduce-date? true}
                          :replace_X_di]]]]}

   :min {:label :indicator-min,
         :info :indicator-min-info
         :validation-desc {:fixed [:replace_A_attr_name :replace_A_di
                                   :replace_time_grouping]
                           :generic {:replace_X_attr_name 1
                                     :replace_X_heal_attr_name 1
                                     :replace_aggregation_# 1
                                     :replace_X_di 1}}
         :ui {:definition-rows [{:label :indicator-attribute-selection
                                 :comps
                                 [{:id "calc-attribute-select"
                                   :label :indicator-attribute-label
                                   :type :select
                                   :content :calc-attributes
                                   :number-of-events {:hide? true}
                                   :replace :replace_A}]}
                                {:label :indicator-settings-label
                                 :comps
                                 [{:id "time-granularity-select"
                                   :label :indicator-time-granularity-label
                                   :type :select
                                   :content :time
                                   :replace :replace_time_grouping}
                                  {:id "grouping-select"
                                   :hint :indicator-grouping-hint
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
                                        :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                       {:id "additional-aggregation-select"
                                        :depends-on "additional-attribute-select"
                                        :label :indicator-aggregation-label
                                        :type :select
                                        :content :aggregations
                                        :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#

         :description [:heal-event {:policy :merge
                                    :generate-ids {:policy :uuid}
                                    :workaround {"date" {:month "01"
                                                         :day "01"}}
                                    :descs [:#
                                            {:attribute :replace_defaults_indicator_name}
                                            {:attribute :replace_X_heal_attr_name}]
                                    :addons [{:attribute "datasource" :value :replace-datasource-name}
                                             {:attribute "notes" :value :replace-notes-value}
                                             {:attribute "indicator-type" :value :replace-indicator-type}]}

                       [:min {:attribute :replace_A_attr_name}
                        [:group-by {:attributes :replace_time_grouping}
                         :replace_A_di]]
                       [:#
                        [:replace_aggregation_# {:attribute :replace_X_attr_name}
                         [:group-by {:attributes :replace_time_grouping
                                     :reduce-date? true}
                          :replace_X_di]]]]}

   :max {:label :indicator-max
         :info :indicator-max-info
         :validation-desc {:fixed [:replace_A_attr_name :replace_A_di
                                   :replace_time_grouping]
                           :generic {:replace_X_attr_name 1
                                     :replace_X_heal_attr_name 1
                                     :replace_aggregation_# 1
                                     :replace_X_di 1}}
         :ui {:definition-rows [{:label :indicator-attribute-selection
                                 :comps
                                 [{:id "calc-attribute-select"
                                   :label :indicator-attribute-label
                                   :type :select
                                   :content :calc-attributes
                                   :number-of-events {:hide? true}
                                   :replace :replace_A}]}
                                {:label :indicator-settings-label
                                 :comps
                                 [{:id "time-granularity-select"
                                   :label :indicator-time-granularity-label
                                   :type :select
                                   :content :time
                                   :replace :replace_time_grouping}
                                  {:id "grouping-select"
                                   :hint :indicator-grouping-hint
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
                                        :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                       {:id "additional-aggregation-select"
                                        :depends-on "additional-attribute-select"
                                        :label :indicator-aggregation-label
                                        :type :select
                                        :content :aggregations
                                        :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#

         :description [:heal-event {:policy :merge
                                    :generate-ids {:policy :uuid}
                                    :workaround {"date" {:month "01"
                                                         :day "01"}}
                                    :descs [:#
                                            {:attribute :replace_defaults_indicator_name}
                                            {:attribute :replace_X_heal_attr_name}]
                                    :addons [{:attribute "datasource" :value :replace-datasource-name}
                                             {:attribute "notes" :value :replace-notes-value}
                                             {:attribute "indicator-type" :value :replace-indicator-type}]}

                       [:max {:attribute :replace_A_attr_name}
                        [:group-by {:attributes :replace_time_grouping}
                         :replace_A_di]]
                       [:#
                        [:replace_aggregation_# {:attribute :replace_X_attr_name}
                         [:group-by {:attributes :replace_time_grouping
                                     :reduce-date? true}
                          :replace_X_di]]]]}

   :average {:label :indicator-average
             :info :indicator-average-info
             :validation-desc {:fixed [:replace_A_attr_name :replace_A_di
                                       :replace_time_grouping]
                               :generic {:replace_X_attr_name 1
                                         :replace_X_heal_attr_name 1
                                         :replace_aggregation_# 1
                                         :replace_X_di 1}}
             :ui {:definition-rows [{:label :indicator-attribute-selection
                                     :comps
                                     [{:id "calc-attribute-select"
                                       :label :indicator-attribute-label
                                       :type :select
                                       :content :calc-attributes
                                       :number-of-events {:hide? true}
                                       :replace :replace_A}]}
                                    {:label :indicator-settings-label
                                     :comps
                                     [{:id "time-granularity-select"
                                       :label :indicator-time-granularity-label
                                       :type :select
                                       :content :time
                                       :replace :replace_time_grouping}
                                      {:id "grouping-select"
                                       :hint :indicator-grouping-hint
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
                                            :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                           {:id "additional-aggregation-select"
                                            :depends-on "additional-attribute-select"
                                            :label :indicator-aggregation-label
                                            :type :select
                                            :content :aggregations
                                            :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#
             :description [:heal-event {:policy :merge
                                        :generate-ids {:policy :uuid}
                                        :workaround {"date" {:month "01"
                                                             :day "01"}}
                                        :descs [:#
                                                {:attribute :replace_defaults_indicator_name}
                                                {:attribute :replace_X_heal_attr_name}]
                                        :addons [{:attribute "datasource" :value :replace-datasource-name}
                                                 {:attribute "notes" :value :replace-notes-value}
                                                 {:attribute "indicator-type" :value :replace-indicator-type}]
                                        :force-type [{:attribute :replace_defaults_indicator_name
                                                      :new-type :double}]}

                           [:/ nil
                            [:sum {:attribute :replace_A_attr_name}
                             [:group-by {:attributes :replace_time_grouping}
                              :replace_A_di]]
                            [:count-events nil
                             [:group-by {:attributes :replace_time_grouping}
                              :replace_A_di]]]
                           [:#
                            [:replace_aggregation_# {:attribute :replace_X_attr_name}
                             [:group-by {:attributes :replace_time_grouping
                                         :reduce-date? true}
                              :replace_X_di]]]]}

   :normalize {:label :indicator-normalize
               :info :indicator-normalize-info
               :validation-desc {:fixed [:replace_A_attr_name
                                         :replace_A_di
                                         :replace_aggregation_A
                                         :replace_time_grouping]
                                 :generic {:replace_X_attr_name 1
                                           :replace_X_heal_attr_name 1
                                           :replace_aggregation_# 1
                                           :replace_X_di 1}}
               :ui {:definition-rows [{:label :indicator-attribute-selection
                                       :comps
                                       [{:id "calc-attribute-select"
                                         :label :indicator-attribute-label
                                         :type :select
                                         :content :calc-attributes
                                         :number-of-events {:op :count-events
                                                            :key :replace_aggregation_A}
                                         :replace :replace_A}
                                        {:id "calc-aggregation-select"
                                         :depends-on "calc-attribute-select"
                                         :label :indicator-aggregation-label
                                         :type :select
                                         :content :aggregations
                                         :replace :replace_aggregation_A
                                         :disable [["calc-attribute-select" :number-of-events]]}]}
                                      {:label :indicator-settings-label
                                       :comps
                                       [{:id "time-granularity-select"
                                         :label :indicator-time-granularity-label
                                         :type :select
                                         :content :time
                                         :replace :replace_time_grouping}
                                        {:id "grouping-select"
                                         :hint :indicator-grouping-hint
                                         :label :indicator-grouping-label
                                         :type :select
                                         :content :group-attributes
                                         :replace :replace_time_grouping}
                                        {:id "range-min-input"
                                         :depends-on "range-max-input"
                                         :label :indicator-range-min-label
                                         :type :number
                                         :content :number
                                         :default 0
                                         :replace :replace_range_min}
                                        {:id "range-max-input"
                                         :depends-on "range-min-input"
                                         :label :indicator-range-max-label
                                         :type :number
                                         :content :number
                                         :default 100
                                         :replace :replace_range_max}]}]
                    :additional-attributes {:label :indicator-additional-attributes-label
                                            :comps
                                            [{:id "additional-attribute-select"
                                              :label :indicator-attribute-label
                                              :type :select
                                              :content :all-attributes
                                              :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                             {:id "additional-aggregation-select"
                                              :depends-on "additional-attribute-select"
                                              :label :indicator-aggregation-label
                                              :type :select
                                              :content :aggregations
                                              :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#

               :description [:heal-event {:policy :merge
                                          :generate-ids {:policy :uuid}
                                          :workaround {"date" {:month "01"
                                                               :day "01"}}
                                          :descs [:#
                                                  {:attribute :replace_defaults_indicator_name}
                                                  {:attribute :replace_X_heal_attr_name}]
                                          :addons [{:attribute "datasource" :value :replace-datasource-name}
                                                   {:attribute "notes" :value :replace-notes-value}
                                                   {:attribute "indicator-type" :value :replace-indicator-type}]
                                          :force-type [{:attribute "indicator"
                                                        :new-type :double}]}
                             [:normalize {:range-min :replace_range_min
                                          :range-max :replace_range_max}
                              [:replace_aggregation_A {:attribute :replace_A_attr_name}
                               [:group-by {:attributes :replace_time_grouping}
                                :replace_A_di]]]
                             [:#
                              [:replace_aggregation_# {:attribute :replace_X_attr_name}
                               [:group-by {:attributes :replace_time_grouping
                                           :reduce-date? true}
                                :replace_X_di]]]]}

   :indicator-rank {:label :indicator-rank
                    :info :indicator-rank-info
                    :validation-desc {:fixed [:replace_A_attr_name :replace_A_di
                                              :replace_B_attr_name :replace_B_di
                                              :replace_aggregation_A
                                              :replace_aggregation_B
                                              :replace_time_grouping]
                                      :generic {:replace_X_attr_name 1
                                                :replace_X_heal_attr_name 1
                                                :replace_aggregation_# 1
                                                :replace_X_di 1}}
                    :ui {:definition-rows [{:label :indicator-attribute-selection
                                            :partition 2 ; => result in div col-6 elements for each two comps
                                            :comps
                                            [{:id "calc-attribute-a-select"
                                              :depends-on "calc-attribute-b-select"
                                              :label :indicator-attribute-a-label
                                              :type :select
                                              :content :calc-attributes
                                              :number-of-events {:op :count-events
                                                                 :key :replace_aggregation_A}
                                              :replace :replace_A}
                                             {:id "aggregation-attribute-a-select"
                                              :depends-on "calc-attribute-a-select"
                                              :label :indicator-aggregation-label
                                              :type :select
                                              :content :aggregations
                                              :replace :replace_aggregation_A
                                              :disable [["calc-attribute-a-select" :number-of-events]]}
                                             {:id "calc-attribute-b-select"
                                              :depends-on "calc-attribute-a-select"
                                              :label :indicator-attribute-b-label
                                              :type :select
                                              :content :calc-attributes
                                              :number-of-events {:op :count-events
                                                                 :key :replace_aggregation_B}
                                              :replace :replace_B} ; => :replace_B_attr_name :replace_B_di
                                             {:id "aggregation-attribute-b-select"
                                              :depends-on "calc-attribute-b-select"
                                              :label :indicator-aggregation-label
                                              :type :select
                                              :content :aggregations
                                              :replace :replace_aggregation_B
                                              :disable [["calc-attribute-b-select" :number-of-events]]}]}
                                           {:label :indicator-settings-label
                                            :comps
                                            [{:id "time-granularity-select"
                                              :label :indicator-time-granularity-label
                                              :type :select
                                              :content :time
                                              :replace :replace_time_grouping}
                                             {:id "grouping-select"
                                              :hint :indicator-grouping-hint
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
                                                   :replace :replace_X}; replace_X is fix for now -> management/deduplicate-description
                                                  {:id "additional-aggregation-select"
                                                   :depends-on "additional-attribute-select"
                                                   :label :indicator-aggregation-label
                                                   :type :select
                                                   :content :aggregations
                                                   :replace :replace_aggregation_#}]}}; same goes for :replace_aggregation_#
                    :description [:heal-event {:policy :merge
                                               :generate-ids {:policy :uuid}
                                               :workaround {"date" {:month "01"
                                                                    :day "01"}}
                                               :descs [:#
                                                       {:attribute :replace_defaults_indicator_name}
                                                       {:attribute :replace_X_heal_attr_name}]
                                               :addons [{:attribute "datasource" :value :replace-datasource-name}
                                                        {:attribute "notes" :value :replace-notes-value}
                                                        {:attribute "indicator-type" :value :replace-indicator-type}]}
                                  [:+ nil
                                   [:* nil
                                    [:normalize {:range-min 0
                                                 :range-max 100}
                                     [:replace_aggregation_A {:attribute :replace_A_attr_name}
                                      [:group-by {:attributes :replace_time_grouping}
                                       :replace_A_di]]]
                                    0.5]
                                   [:* nil
                                    [:normalize {:range-min 0
                                                 :range-max 100}
                                     [:replace_aggregation_B {:attribute :replace_B_attr_name}
                                      [:group-by {:attributes :replace_time_grouping}
                                       :replace_B_di]]]
                                    0.5]]
                                  [:#
                                   [:replace_aggregation_# {:attribute :replace_X_attr_name}
                                    [:group-by {:attributes :replace_time_grouping
                                                :reduce-date? true}
                                     :replace_X_di]]]]}})
 ;; Disabled for now.
 ;; :custom {:label :indicator-custom
 ;;          :info :indicator-custom-info
 ;;          :ui {:definition-rows [{:label :indicator-custom
 ;;                                  :comps
 ;;                                  [{:id "custom-indicator-desc"
 ;;                                    :label :indicator-custom-description
 ;;                                    :type :textarea
 ;;                                    :content :description}]}]}}
 
