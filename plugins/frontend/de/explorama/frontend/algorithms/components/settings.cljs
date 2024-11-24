(ns de.explorama.frontend.algorithms.components.settings
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :refer [attribute-label]]
            [de.explorama.shared.data-format.aggregations :refer [number-of-events]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [checkbox
                                                                input-field select]]
            [de.explorama.frontend.algorithms.components.helper :refer [default-option-settings
                                          flip-key-value->vec keyword-translate-option
                                          keyword-translate-options]]
            [de.explorama.frontend.algorithms.components.subsection :as sub]
            [taoensso.timbre :refer-macros [error]]))

(defn missing-data-handling [value defined-as defaults current-algorithm state translate-function {:keys [training-data-change logging-value-changed]}]
  (when defaults
    (let [{defaults-method :method
           defaults-replacement :replacement
           defaults-value :value
           defaults-enum? :enum?}
          defaults
          missing-value-path [current-algorithm defined-as :attribute-config value :missing-value :method]
          missing-value (get-in @state missing-value-path (keyword-translate-option defaults-method translate-function))
          replacement-path [current-algorithm defined-as :attribute-config value :missing-value :replacement]
          replacement (get-in @state
                              replacement-path
                              (keyword-translate-option defaults-replacement translate-function))
          replacement-value-path [current-algorithm defined-as :attribute-config value :missing-value :value]
          replacement-value (get-in @state
                                    replacement-value-path
                                    defaults-value)
          enum?-path [current-algorithm defined-as :attribute-config value :missing-value :enum?]
          enum?-value (get-in @state enum?-path defaults-enum?)
          {:keys [read-only?]} @state]
      [:div
       [select {:disabled? read-only?
                :is-clearable? false
                :label (translate-function :handling-missing-label)
                :options (keyword-translate-options [:replace :ignore] translate-function)
                :on-change (fn [e]
                             (swap! state assoc-in missing-value-path e)
                             (logging-value-changed :settings missing-value-path e)
                             (training-data-change))
                :values missing-value
                :extra-class "input--w16"}]
       (when (some? defaults-enum?)
         [checkbox {:disabled? read-only?
                    :label (translate-function :attribute-config-enum?)
                    :checked? enum?-value
                    :on-change (fn [e]
                                 (swap! state assoc-in enum?-path e)
                                 (logging-value-changed :settings enum?-path e)
                                 (training-data-change))
                    :extra-class "input--w16"}])
       (when (= :replace (:value missing-value))
         [select {:disabled? read-only?
                  :is-clearable? false
                  :label (translate-function :missing-value-replacement)
                  :options (keyword-translate-options [:number :average :most-frequent] translate-function)
                  :on-change (fn [e]
                               (swap! state assoc-in replacement-path e)
                               (logging-value-changed :settings replacement-path e)
                               (training-data-change))
                  :values replacement
                  :extra-class "input--w16"}])
       (when (and (= :number (:value replacement))
                  (= :replace (:value missing-value)))
         [input-field {:disabled? read-only?
                       :on-change (fn [e]
                                    (swap! state assoc-in replacement-value-path e)
                                    (logging-value-changed :settings replacement-value-path e)
                                    (training-data-change))
                       :thousand-separator (translate-function :thousand-separator)
                       :decimal-separator  (translate-function :decimal-separator)
                       :label (translate-function :missing-value-replacement-value)
                       :default-value 0
                       :type :number
                       :value replacement-value
                       :step 0.1
                       :extra-class "input--w16"}])])))

(defn date-configuration [value defined-as defaults current-algorithm state translate-function {:keys [training-data-change logging-value-changed]}]
  (let [{:keys [read-only?]} @state]
    (when value
      [:div
       (let [{:keys [granularity]} defaults
             path [current-algorithm defined-as :attribute-config value :granularity]]
         [select {:disabled? read-only?
                  :is-clearable? false
                  :label (translate-function :attribute-config-date-granularity)
                  :options (keyword-translate-options [:year :quarter :month :day] translate-function)
                  :on-change (fn [e]
                               (swap! state assoc-in path e)
                               (logging-value-changed :settings path e)
                               (training-data-change))
                  :values (get-in @state path (keyword-translate-option granularity translate-function))
                  :extra-class "input--w16"}])])))

(defn categoric-configuration [value defined-as defaults current-algorithm state translate-function {:keys [training-data-change logging-value-changed]}]
  (when defaults
    (let [{defaults-method :method}
          defaults
          missing-value-path [current-algorithm defined-as :attribute-config value :missing-value :method]
          missing-value (get-in @state missing-value-path (keyword-translate-option defaults-method translate-function))
          {:keys [read-only?]} @state]
      [:div
       [select {:disabled? read-only?
                :is-clearable? false
                :label (translate-function :handling-missing-label)
                :options (keyword-translate-options [:ignore] translate-function)
                :on-change (fn [e]
                             (swap! state assoc-in missing-value-path e)
                             (logging-value-changed :settings missing-value-path e)
                             (training-data-change))
                :values missing-value
                :extra-class "input--w16"}]])))

(defn continues-configuration [value defined-as defaults current-algorithm state translate-function {:keys [training-data-change logging-value-changed]}]
  (let [{:keys [read-only?]} @state]
    (when value
      [:div
       (let [{:keys [method min max min-value max-value]} defaults
             max-number-path [current-algorithm defined-as :attribute-config value :continues-value :max]
             max-number (get-in @state max-number-path (keyword-translate-option max translate-function))
             min-number-path [current-algorithm defined-as :attribute-config value :continues-value :min]
             min-number (get-in @state min-number-path (keyword-translate-option min translate-function))
             step-path [current-algorithm defined-as :attribute-config value :continues-value :step]
             step (get-in @state step-path)]
         [:<>
          (let [path [current-algorithm defined-as :attribute-config value :continues-value :method]]
            [select {:disabled? read-only?
                     :is-clearable? false
                     :label (translate-function :continues-method)
                     :options (keyword-translate-options [:range] translate-function)
                     :on-change (fn [e]
                                  (swap! state assoc-in path e)
                                  (logging-value-changed :settings path e)
                                  (training-data-change))
                     :values (get-in @state path (keyword-translate-option method translate-function))
                     :extra-class "input--w16"}])
          [input-field {:disabled? read-only?
                        :on-change (fn [e]
                                     (swap! state assoc-in step-path e)
                                     (logging-value-changed :settings step-path e)
                                     (training-data-change))
                        :thousand-separator (translate-function :thousand-separator)
                        :decimal-separator  (translate-function :decimal-separator)
                        :label (translate-function :step)
                        :default-value 1
                        :type :number
                        :value step
                        :step 0.1
                        :extra-class "input--w16"}]
          [select {:disabled? read-only?
                   :is-clearable? false
                   :label (translate-function :continues-max)
                   :options (keyword-translate-options [:max :number] translate-function)
                   :on-change (fn [e]
                                (swap! state assoc-in max-number-path e)
                                (logging-value-changed :settings max-number-path e)
                                (training-data-change))
                   :values max-number
                   :extra-class "input--w16"}]
          (when (= :number (:value max-number))
            (let [path [current-algorithm defined-as :attribute-config value :continues-value :max-value]]
              [input-field {:disabled? read-only?
                            :on-change (fn [e]
                                         (swap! state assoc-in path e)
                                         (logging-value-changed :settings path e)
                                         (training-data-change))
                            :thousand-separator (translate-function :thousand-separator)
                            :decimal-separator  (translate-function :decimal-separator)
                            :label (translate-function :max)
                            :default-value (or max-value 0)
                            :type :number
                            :value (get-in @state path)
                            :step step
                            :extra-class "input--w16"}]))
          [select {:disabled? read-only?
                   :is-clearable? false
                   :label (translate-function :continues-min)
                   :options (keyword-translate-options [:min :number] translate-function)
                   :on-change (fn [e]
                                (swap! state assoc-in min-number-path e)
                                (logging-value-changed :settings min-number-path e)
                                (training-data-change))
                   :values min-number
                   :extra-class "input--w16"}]
          (when (= :number (:value min-number))
            (let [path [current-algorithm defined-as :attribute-config value :continues-value :min-value]]
              [input-field {:disabled? read-only?
                            :on-change (fn [e]
                                         (swap! state assoc-in path e)
                                         (logging-value-changed :settings path e)
                                         (training-data-change))
                            :label (translate-function :min)
                            :thousand-separator (translate-function :thousand-separator)
                            :decimal-separator  (translate-function :decimal-separator)
                            :default-value (or min-value 0)
                            :type :number
                            :value (get-in @state path)
                            :step step
                            :extra-class "input--w16"}]))])])))

(defn future-configuration [value defined-as defaults current-algorithm state translate-function {:keys [training-data-change logging-value-changed]}]
  (let [{:keys [read-only?]} @state]
    (when value
      (let [{:keys [method step start-value manual-values]} defaults
            method-path [current-algorithm defined-as :attribute-config value :future-values :method]
            method (get-in @state
                           method-path
                           (keyword-translate-option method translate-function))]
        [:div
         [select {:disabled? read-only?
                  :is-clearable? false
                  :label (translate-function :future-values)
                  :options (keyword-translate-options [:range :manual :auto] translate-function)
                  :on-change (fn [e]
                               (swap! state assoc-in method-path e)
                               (logging-value-changed :settings method-path e)
                               (training-data-change))
                  :values method
                  :extra-class "input--w16"}]
         (case (:value method)
           :range
           (let [start-value-path [current-algorithm defined-as :attribute-config value :future-values :start-value :type]
                 start-value-sel (get-in @state start-value-path (keyword-translate-option (get start-value :type) translate-function))
                 step-path [current-algorithm defined-as :attribute-config value :future-values :step]
                 step (get-in @state step-path step)]
             [:<>
              [input-field {:disabled? read-only?
                            :on-change (fn [e]
                                         (swap! state assoc-in step-path e)
                                         (logging-value-changed :settings step-path e)
                                         (training-data-change))
                            :label (translate-function :step)
                            :thousand-separator (translate-function :thousand-separator)
                            :decimal-separator  (translate-function :decimal-separator)
                            :default-value 0
                            :type :number
                            :value step
                            :step 0.1
                            :extra-class "input--w16"}]
              [select {:disabled? read-only?
                       :is-clearable? false
                       :label (translate-function :future-max)
                       :options (keyword-translate-options [:max :number] translate-function)
                       :on-change (fn [e]
                                    (swap! state assoc-in start-value-path e)
                                    (logging-value-changed :settings start-value-path e)
                                    (training-data-change))
                       :values start-value-sel
                       :extra-class "input--w16"}]
              (when (= :number (:value start-value-sel))
                (let [path [current-algorithm defined-as :attribute-config value :future-values :start-value :value]]
                  [input-field {:disabled? read-only?
                                :on-change (fn [e]
                                             (swap! state assoc-in path e)
                                             (logging-value-changed :settings path e)
                                             (training-data-change))
                                :label (translate-function :number)
                                :thousand-separator (translate-function :thousand-separator)
                                :decimal-separator  (translate-function :decimal-separator)
                                :default-value 0
                                :type :number
                                :value (get-in @state path (get start-value :type))
                                :step step
                                :extra-class "input--w16"}]))])
           :manual
           [:div (translate-function :future-data-manual-hint)]
           :auto
           [:div (translate-function :future-data-auto-hint)])]))))

(defn shared-configuration [defined-as defaults current-algorithm state label? translate-function {:keys [training-data-change logging-value-changed]}]
  (let [{:keys [read-only?]} @state
        value (get-in @state [current-algorithm defined-as :value])]
    (when (and (seq (get defaults :shared))
               (or (and (map? value)
                        (not= :number-of-events (get value :value)))
                   (and (vector? value)
                        (= 1 (count value))
                        (not= :number-of-events (get-in value [0 :value])))))
      (let [{:keys [aggregation multiple-values-per-event merge-policy hide]} (get defaults :shared)
            hide (set hide)]
        [:<>
         (when (and label?
                    (not (and (hide :aggregation)
                              (hide :multiple-values-per-event)
                              (hide :merge-policy))))
           [:div.title (translate-function :shared)])
         (when-not (hide :aggregation)
           (let [path [current-algorithm defined-as :shared :aggregation]]
             [select {:disabled? read-only?
                      :is-clearable? false
                      :label (translate-function :attribute-config-aggregation)
                      :options (keyword-translate-options [:sum :average :min :max :multiple] translate-function)
                      :on-change (fn [e]
                                   (swap! state assoc-in path e)
                                   (logging-value-changed :settings path e)
                                   (training-data-change))
                      :values (get-in @state
                                      path
                                      (keyword-translate-option aggregation translate-function))
                      :extra-class "input--w16"}]))
         (when-not (hide :multiple-values-per-event)
           (let [path [current-algorithm defined-as :shared :multiple-values-per-event]]
             [select {:disabled? read-only?
                      :is-clearable? false
                      :label (translate-function :attribute-config-multiple-values-per-event)
                      :options (keyword-translate-options [:multiply #_:keep] translate-function)
                      :on-change (fn [e]
                                   (swap! state assoc-in path e)
                                   (logging-value-changed :settings path e)
                                   (training-data-change))
                      :values (get-in @state path (keyword-translate-option multiple-values-per-event translate-function))
                      :extra-class "input--w16"}]))
         (when-not (hide :merge-policy)
           (let [path [current-algorithm defined-as :shared :merge-policy]]
             [select {:disabled? read-only?
                      :is-clearable? false
                      :label (translate-function :attribute-config-merge-policy)
                      :options (keyword-translate-options [:merge-incomplete :ignore-incomplete] translate-function)
                      :on-change (fn [e]
                                   (swap! state assoc-in path e)
                                   (logging-value-changed :settings path e)
                                   (training-data-change))
                      :values (get-in @state path (keyword-translate-option merge-policy translate-function))
                      :extra-class "input--w16"}]))]))))

(defn feature-configuration [types value defined-as input-or-target config current-algorithm state translate-function {:keys [training-data-change logging-value-changed] :as changed-functions}]
  (let [{:keys [read-only?]} @state
        value-type (get types value)
        value-desc (get config value-type)
        choices (count value-desc)]
    (when (pos? choices)
      (let [choice? (< 1 choices)
            active-option-key (or (get-in @state [current-algorithm defined-as :attribute-config value input-or-target :active-config :value])
                                  (default-option-settings value-desc))
            active-option (get value-desc
                               active-option-key)
            simple? (get active-option :simple?)]
        (when (not simple?)
          [:<>
           (when choice?
             (let [path [current-algorithm defined-as :attribute-config value input-or-target :active-config]]
               [select {:disabled? read-only?
                        :is-clearable? false
                        :label (translate-function :feature-configuration)
                        :options (keyword-translate-options (keys value-desc) translate-function)
                        :on-change (fn [e]
                                     (swap! state assoc-in path e)
                                     (logging-value-changed :settings path e)
                                     (training-data-change))
                        :values (get-in @state path (keyword-translate-option active-option-key translate-function))
                        :extra-class "input--w16"}]))
           (cond
             (and (= active-option-key :missing-value)
                  (= value-type :numeric))
             [missing-data-handling value defined-as active-option current-algorithm state translate-function changed-functions]
             (and (= active-option-key :missing-value)
                  (= value-type :categoric))
             [categoric-configuration value defined-as active-option current-algorithm state translate-function changed-functions]
             (and (= active-option-key :continues-value)
                  (= value-type :numeric))
             [continues-configuration value defined-as active-option current-algorithm state translate-function changed-functions]
             (and (= active-option-key :future-values)
                  (= value-type :numeric))
             [future-configuration value defined-as active-option current-algorithm state translate-function changed-functions]
             (and (= active-option-key :default)
                  (= value-type :date))
             [date-configuration value defined-as active-option current-algorithm state translate-function changed-functions]
             :else
             (error "no valid dialog " active-option-key " " value-type))])))))

(defn view [parameter-desc
            current-algorithm
            options
            state
            translate-function
            {:keys [training-data-change logging-value-changed] :as changed-function}
            {:keys [simple? column attribute-labels]}]
  (let [{:keys [read-only?]} @state
        attribute-labels @(fi/call-api [:i18n :get-labels-sub])
        column-requirements (filter identity (map #(%1 %2)
                                                  (cycle [identity (constantly nil)])
                                                  (cond->> (:requirements parameter-desc)
                                                    (= column :right)
                                                    (drop 1))))]
    (into [:<>]
          (for [{:keys [defined-as number input-config target-config types]} column-requirements
                :let [is-multi? (case number
                                  :multi true
                                  :single false
                                  false)
                      path [current-algorithm defined-as :value]
                      select-options
                      (as-> @options $
                        (flip-key-value->vec $)
                        (select-keys $ types)
                        (mapcat second $)
                        (mapv (fn [value]
                                (if (= :number-of-events value)
                                  {:label (translate-function (:label number-of-events))
                                   :value :number-of-events}
                                  (hash-map :label (get attribute-labels value value)
                                            :value value)))
                              $)
                        (sort-by (fn [{:keys [label]}] (str/lower-case label)) $)
                        (vec $))
                      value (get-in @state path)
                      value (if-not value
                              (let [value (if is-multi?
                                            [(first select-options)]
                                            (first select-options))]
                                (swap! state assoc-in path value)
                                (logging-value-changed :settings path value)
                                (training-data-change)
                                value)
                              (if (vector? value)
                                (mapv (fn [v] (update v :label #(get attribute-labels % %))) value)
                                (update value :label #(get attribute-labels % %))))]]
            [:<>
              ;attribute selection
             (when simple?
               [select {:disabled? (or read-only? (= (count select-options) 1))
                        :is-clearable? false
                        :label (translate-function defined-as)
                        :is-multi? is-multi?
                        :options select-options
                        :on-change (fn [e]
                                     (swap! state assoc-in path e)
                                     (logging-value-changed :settings path e)
                                     (training-data-change))
                        :values value
                        :extra-class "input--w18"}])
              ;attribute settings
             (when (not simple?)
               [:div.explorama__form__section
                [:div.title (translate-function defined-as)]
                (if is-multi?
                  [:<>
                   (into [:<>]
                         (for [{v :value} value
                               :when (not= v "date")] ;used in exception-view (parameter.clj)
                           [sub/section {:default-open? true
                                         :label (str (translate-function :settings-attribute-for) " " (attribute-label attribute-labels v) ":")}
                            [feature-configuration @options v defined-as :input-config input-config current-algorithm state translate-function changed-function]
                            [feature-configuration @options v defined-as :target-config target-config current-algorithm state translate-function changed-function]]))
                   [shared-configuration defined-as input-config current-algorithm state true translate-function changed-function]]
                  [:<>
                   [:<>
                    [feature-configuration @options (:value value) defined-as :input-config input-config current-algorithm state translate-function changed-function]
                    [feature-configuration @options (:value value) defined-as :target-config target-config current-algorithm state translate-function changed-function]]
                   [shared-configuration defined-as input-config current-algorithm state false translate-function changed-function]])])]))))
