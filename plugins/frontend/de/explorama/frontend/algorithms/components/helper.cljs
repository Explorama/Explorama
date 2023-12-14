(ns de.explorama.frontend.algorithms.components.helper
  (:require [clojure.set :as set]
            [taoensso.timbre :refer [info debug error]]))

(defn op [[op-key boundary]]
  (fn [value]
    ((case op-key
       :< <
       :<= <=
       :> >
       :>= >=
       := =)
     value
     boundary)))

(defn parameter-disabled? [[depend-parameter & conds :as active?] parameter-desc current-algorithm state]
  (let [depend-parameter-type (get-in parameter-desc [depend-parameter :type])
        value-dependency (get-in state [current-algorithm depend-parameter :value]
                                 (if (= depend-parameter-type :selection)
                                   {:value (get-in parameter-desc [depend-parameter :default])}
                                   (get-in parameter-desc [depend-parameter :default])))]
    (if-not active?
      false
      (not
       (some (fn [pred]
               (case depend-parameter-type
                 :selection
                 (= pred (:value value-dependency))
                 (:double :integer :boolean)
                 ((op pred) value-dependency)))
             conds)))))

;;; UI Util

(defn keyword-option [a]
  {:label (name a) :value a})

(defn keyword-translate-option [a translate]
  {:label (translate a) :value a})

(defn select-option [[key v]]
  {:label v :value key})

(defn keyword-options [options & [access]]
  (mapv #(-> % ((or access identity)) keyword-option) options))

(defn keyword-translate-options [options translate & [access]]
  (mapv #(-> % ((or access identity)) (keyword-translate-option translate)) options))

(defn select-options [options]
  (mapv select-option options))

(defn value-of-parameter [ignore? {:keys [type multi-select?]} value]
  (let [val (cond (and (= :selection type)
                       multi-select?)
                  (mapv :value value)
                  (and (= :selection type)
                       (not multi-select?))
                  (:value value)
                  :else
                  value)]
    (when (and (not ignore?)
               (some? value))
      val)))

;;; Forward transformation client -> server

(defn merge-parameters [parameter-acc
                        current-algorithm
                        parameter-state
                        current-algorithm-desc
                        parameter-key]
  (let [{parameter parameter-key} current-algorithm-desc]
    (if current-algorithm
      (reduce (fn [acc [param-key default-param-value]]
                (let [chosen-parameter (get-in parameter-state
                                               [current-algorithm param-key :value])
                      ignore? (get-in parameter-state
                                      [current-algorithm param-key :ignore?])
                      param-value
                      (value-of-parameter ignore?
                                          default-param-value
                                          chosen-parameter)
                      disabled? (or (parameter-disabled? (get-in parameter [param-key :content-valid? param-value])
                                                         parameter
                                                         current-algorithm
                                                         parameter-state)
                                    (parameter-disabled? (get-in parameter [param-key :active?])
                                                         parameter
                                                         current-algorithm
                                                         parameter-state))]
                  (if (and (some? param-value)
                           (not disabled?))
                    (assoc acc param-key param-value)
                    acc)))
              parameter-acc
              parameter)
      false)))

(defn merge-missing-values [config value defaults-missing-value]
  (cond-> {:method (get-in config
                           [value :missing-value :method :value]
                           (:method defaults-missing-value))
           :replacement (get-in config
                                [value :missing-value :replacement :value]
                                (:replacement defaults-missing-value))
           :value (get-in config
                          [value :missing-value :value]
                          (:value defaults-missing-value))}
    (some? (:enum? defaults-missing-value))
    (assoc :enum? (get-in config
                          [value :missing-value :enum?]
                          (:enum? defaults-missing-value)))))

(defn merge-date-config [config default-configs value]
  (let [granularity (get-in config [value :granularity :value]
                            (:granularity default-configs))]
    {:granularity granularity}))

(defn merge-categoric-config [config default-configs value]
  (let [encoding (get-in config [value :encoding :value]
                         (:encoding default-configs))]
    {:encoding encoding}))

(defn merge-shared-values [config shared-default-config]
  {:aggregation (get-in config [:aggregation :value]
                        (:aggregation shared-default-config))
   :multiple-values-per-event (get-in config [:multiple-values-per-event :value]
                                      (:multiple-values-per-event shared-default-config))
   :merge-policy (get-in config [:merge-policy :value]
                         (:merge-policy shared-default-config))})

(defn merge-continues-values [config value {:keys [method step max min max-value min-value]}]
  {:method (get-in config
                   [value :continues-value :method :value]
                   method)
   :step (get-in config
                 [value :continues-value :step]
                 step)
   :max
   (let [selected-method (get-in config [value :continues-value :max :value])]
     (if selected-method
       (if (= :max selected-method)
         {:type :max}
         {:type :value
          :value (get-in config
                         [value :continues-value :max-value]
                         max-value)})
       max))
   :min
   (let [selected-method (get-in config [value :continues-value :min :value])]
     (if selected-method
       (if (= :min selected-method)
         {:type :min}
         {:type :value
          :value (get-in config
                         [value :continues-value :min-value]
                         min-value)})
       min))})

(defn merge-future-values [config value {:keys [method step start-value]} future-data-state]
  (let [future-values-method (get-in config [value :future-values :method :value]
                                     method)
        {default-start-value-type :type
         default-start-value-value :value} start-value
        start-value-type (get-in config [value :future-values :start-value :type :value]
                                 default-start-value-type)]
    (cond (= future-values-method :range)
          {:method :range
           :step (get-in config [value :future-values :step]
                         step)
           :start-value (cond-> {}
                          (= start-value-type :max)
                          (assoc :type :max)
                          (= start-value-type :number)
                          (assoc :type :start-value
                                 :value (get-in config [value :future-values :start-value :value]
                                                default-start-value-value)))}
          (= future-values-method :auto)
          {:method :auto}
          (= future-values-method :manual)
          {:method :manual
           :manual-values (let [{future-data :future-data} future-data-state]
                            (mapv (fn [[_ row]]
                                    (get row value))
                                  (sort-by (fn [[idx]] idx) future-data)))})))

(defn default-option-settings [default-configs]
  (let [defaults-active (filter (fn [[_ {:keys [default]}]] default) default-configs)
        [default-active] (if (empty? defaults-active)
                           (first default-configs)
                           (first defaults-active))]
    default-active))

(defn merge-attributes [current-algorithm settings-state
                        future-data-state
                        current-algorithm-desc types]
  (if current-algorithm
    (reduce (fn [acc {:keys [defined-as input-config target-config]}]
              (let [{variables :value
                     config :attribute-config
                     shared :shared}
                    (get-in settings-state [current-algorithm defined-as])
                    variables (if (map? variables)
                                [variables]
                                variables)
                    variables (mapv :value variables)]
                (assoc acc
                       defined-as
                       (mapv (fn [value]
                               (let [value-type (get types value)
                                     default-configs (get input-config value-type)
                                     base {:value value
                                           :type (get types value)
                                           :given? (get-in default-configs [(first (keys default-configs)) :given?])} ;? change this if leading available in the ui
                                     shared-default-config (get input-config :shared)

                                     active-config (get-in config [value :input-config :active-config :value]
                                                           (default-option-settings default-configs))]
                                 (cond-> base
                                   (= :date (get types value))
                                   (assoc :date-config (merge-date-config config (get default-configs :default) value))
                                   (and (get default-configs :missing-value)
                                        (= :missing-value active-config))
                                   (assoc :missing-value
                                          (merge-missing-values config value (get default-configs :missing-value)))
                                   (and (get default-configs :continues-value)
                                        (= :continues-value active-config))
                                   (assoc :continues-value
                                          (merge-continues-values config value (get default-configs :continues-value)))
                                   (get-in target-config [value-type :future-values])
                                   (assoc :future-values
                                          (merge-future-values config value (get-in target-config [value-type :future-values]) future-data-state))
                                   shared-default-config
                                   (assoc :shared
                                          (merge-shared-values shared shared-default-config))
                                   (and (= :encoding active-config)
                                        (= :categoric (get types value)))
                                   (assoc :encoding
                                          (merge-categoric-config config (get default-configs :encoding) value)))))
                             variables))))
            {}
            (:requirements current-algorithm-desc))
    false))

(defn transform-inputs [procedures
                        options
                        problem-types
                        {:keys [problem-type current-algorithm choose-algorithm?]}
                        settings-state
                        parameter-state
                        simple-parameter-state
                        future-data-state]
  (let [[current-algorithm current-algorithm-desc] (cond (and choose-algorithm?
                                                              current-algorithm)
                                                         [current-algorithm (get procedures current-algorithm)]
                                                         (and (:value problem-type)
                                                              (not choose-algorithm?))
                                                         [(:value problem-type) (get problem-types (:value problem-type))])
        simple-parameter (merge-parameters {} current-algorithm
                                           simple-parameter-state current-algorithm-desc
                                           :simple-parameter)
        parameter (merge-parameters simple-parameter current-algorithm
                                    parameter-state current-algorithm-desc
                                    :parameter)
        attributes (merge-attributes current-algorithm
                                     settings-state
                                     future-data-state
                                     current-algorithm-desc
                                     options)]
    (if (and parameter
             attributes)
      (cond-> {:parameter parameter
               :attributes attributes}
        choose-algorithm?
        (assoc :algorithm current-algorithm)
        (not choose-algorithm?)
        (assoc :problem-type (:value problem-type)))
      false)))

;;; Backward transformation server -> client

(defn find-variable [requirements-spec defined-as]
  (some #(when (= defined-as (:defined-as %)) %) requirements-spec))

(defn goal-transformation [choose-algorithm? algorithm problem-type translate-function]
  (if choose-algorithm?
    {:choose-algorithm? choose-algorithm?
     :current-algorithm algorithm
     :choose-algorithm (keyword-translate-option algorithm translate-function)}
    {:choose-algorithm? choose-algorithm?
     :problem-type (keyword-translate-option problem-type translate-function)}))

(defn value-transformation [requirements-spec attr-values]
  (cond (= :single (:number requirements-spec))
        (keyword-option (get-in attr-values [0 :value]))
        (= :multi (:number requirements-spec))
        (reduce (fn [acc {:keys [value]}]
                  (conj acc (keyword-option value)))
                []
                attr-values)
        :else
        (do
          (error "invalid value number - please check configuration")
          (reduce (fn [acc {:keys [value]}]
                    (conj acc (keyword-option value)))
                  []
                  attr-values))))

(defn attribute-config-transformation [attr-values translate-function]
  (reduce (fn [acc {value :value
                    {continues-value-method :method
                     continues-value-step :step
                     {continues-value-type-max :type
                      continues-value-max :value}
                     :max
                     {continues-value-type-min :type
                      continues-value-min :value}
                     :min}
                    :continues-value
                    {missing-value-method :method
                     missing-value-replacement :replacement
                     missing-value :value
                     missing-value-enum? :enum?}
                    :missing-value
                    {date-config-granularity :granularity}
                    :date-config
                    {future-values-method :method
                     {future-values-start-value-type :type
                      future-values-start-value-value :value} :start-value
                     future-values-step :step}
                    :future-values
                    {encoding :encoding}
                    :encoding}]
            (cond-> acc
              missing-value-method
              (assoc-in [value :input-config :active-config] (keyword-translate-option :missing-value translate-function))
              continues-value-method
              (assoc-in [value :input-config :active-config] (keyword-translate-option :continues-value translate-function))
              missing-value-method
              (assoc-in
               [value :missing-value]
               (cond-> {:method (keyword-translate-option missing-value-method translate-function)
                        :replacement (keyword-translate-option missing-value-replacement translate-function)
                        :value (cond (and (= :replace missing-value-method)
                                          (= :number missing-value-replacement))
                                     missing-value
                                     :else missing-value)}
                 (some? missing-value-enum?)
                 (assoc :enum? missing-value-enum?)))
              continues-value-method
              (assoc-in
               [value :continues-value]
               {:method (keyword-translate-option continues-value-method translate-function)
                :step continues-value-step
                :max (if (= :max continues-value-type-max)
                       (keyword-translate-option continues-value-type-max translate-function)
                       (keyword-translate-option :number translate-function))
                :min (if (= :min continues-value-type-min)
                       (keyword-translate-option continues-value-type-min translate-function)
                       (keyword-translate-option :number translate-function))})
              (and continues-value-method (not= :min continues-value-type-min))
              (assoc-in
               [value :continues-value :min-value]
               continues-value-min)
              (and continues-value-method (not= :max continues-value-type-max))
              (assoc-in
               [value :continues-value :max-value]
               continues-value-max)
              date-config-granularity
              (assoc-in [value :granularity] (keyword-translate-option date-config-granularity translate-function))
              (and (= future-values-method :range)
                   (= future-values-start-value-type :max))
              (assoc-in [value :future-values]
                        {:method (keyword-translate-option :range translate-function)
                         :step future-values-step
                         :start-value {:type (keyword-translate-option :max translate-function)}})
              (and (= future-values-method :range)
                   (= future-values-start-value-type :start-value))
              (assoc-in [value :future-values]
                        {:method (keyword-translate-option :range translate-function)
                         :step future-values-step
                         :start-value {:type (keyword-translate-option :number translate-function)
                                       :value future-values-start-value-value}})
              (= future-values-method :manual)
              (assoc-in [value :future-values]
                        {:method (keyword-translate-option :manual translate-function)})
              (= future-values-method :auto)
              (assoc-in [value :future-values]
                        {:method (keyword-translate-option :auto translate-function)})
              encoding
              (assoc-in [value :encoding]
                        (keyword-translate-option :encoding translate-function))))
          {}
          attr-values))

(defn shared-config-transformation [attr-values translate-function]
  (reduce (fn [acc {{config-aggregation :aggregation
                     multiple-values-per-event :multiple-values-per-event
                     merge-policy :merge-policy}
                    :shared}]
            (cond-> acc
              config-aggregation
              (assoc :aggregation (keyword-translate-option config-aggregation translate-function))
              multiple-values-per-event
              (assoc :multiple-values-per-event (keyword-translate-option multiple-values-per-event translate-function))
              merge-policy
              (assoc :merge-policy (keyword-translate-option merge-policy translate-function))))
          {}
          attr-values))

(defn settings-transformation [algorithm problem-type choose-algorithm? procedures problem-types attributes translate-function]
  {(or algorithm problem-type)
   (reduce (fn [acc [attr-type attr-values]]
             (let [requirements-spec
                   (find-variable
                    (if choose-algorithm?
                      (get-in procedures [algorithm :requirements])
                      (get-in problem-types [problem-type :requirements]))
                    attr-type)]
               (assoc acc attr-type
                      (as-> {} $
                        (assoc $ :value
                               (value-transformation requirements-spec attr-values))
                        (assoc $ :attribute-config
                               (attribute-config-transformation attr-values translate-function))
                        (assoc $ :shared
                               (shared-config-transformation attr-values translate-function))))))
           {}
           attributes)})

(defn future-data-transformation-values [acc attr-values]
  (reduce (fn [acc {value :value
                    {future-values-method :method
                     future-values-manual-values :manual-values}
                    :future-values}]
            (if (and (= future-values-method :manual)
                     future-values-manual-values)
              (reduce (fn [acc [index future-val]]
                        (assoc-in acc [:future-data index value] future-val))
                      acc
                      (map-indexed vector future-values-manual-values))
              acc))
          acc
          attr-values))

(defn future-data-transformation [attributes]
  (reduce (fn [acc [_ attr-values]]
            (future-data-transformation-values acc attr-values))
          {}
          attributes))

(defn parameter-transformation [algorithm problem-type choose-algorithm? procedures problem-types parameter parameters-key translate-function]
  {(or algorithm problem-type)
   (reduce (fn [acc [parameter-key]]
             (let [parameter-type
                   (if choose-algorithm?
                     (get-in procedures [algorithm parameters-key parameter-key :type])
                     (get-in problem-types [problem-type parameters-key parameter-key :type]))
                   parameter-value (get parameter parameter-key)]
               (if parameter-value
                 (assoc-in acc [parameter-key :value]
                           (case parameter-type
                             :selection
                             (keyword-translate-option parameter-value translate-function)
                             (:integer :double :boolean)
                             parameter-value))
                 acc)))
           {}
           (if choose-algorithm?
             (get-in procedures [algorithm parameters-key])
             (get-in problem-types [problem-type parameters-key])))})

(defn initialize-from-prediction [procedures
                                  problem-types
                                  {:keys [algorithm problem-type parameter attributes]}
                                  translate-function]
  (let [choose-algorithm? (boolean algorithm)]
    [(goal-transformation choose-algorithm? algorithm problem-type translate-function)
     (settings-transformation algorithm problem-type choose-algorithm? procedures problem-types attributes translate-function)
     (parameter-transformation algorithm problem-type choose-algorithm? procedures problem-types parameter :parameter translate-function)
     (parameter-transformation algorithm problem-type choose-algorithm? procedures problem-types parameter :simple-parameter translate-function)
     (future-data-transformation attributes)]))

(defn flip-key-value->vec [m]
  (reduce (fn [acc [k v]]
            (update acc v #(conj (or % []) k)))
          {}
          m))

(defn validate-requirements [options requirements]
  (when (and options requirements)
    (set/subset?
     (set (mapcat :types requirements))
     (set (vals options)))))
