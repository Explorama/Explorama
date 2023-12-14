(ns de.explorama.frontend.algorithms.components.parameter
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [select checkbox input-field section]]
            [de.explorama.frontend.algorithms.components.helper :refer [keyword-translate-options keyword-translate-option flip-key-value->vec keyword-options parameter-disabled?]]
            [clojure.string :as str]
            [de.explorama.frontend.algorithms.components.custom :refer [row]]))

(defn ignore-view [ignore-able? state current-algorithm key logging-value-changed translate-function]
  (let [path [current-algorithm key :ignore?]
        ignore? (get-in @state path)
        {:keys [read-only?]} @state]
    (when ignore-able?
      [checkbox {:disabled? read-only?
                 :checked? (if (boolean? ignore?) ignore? ignore-able?)
                 :on-change (fn [e]
                              (swap! state assoc-in path e)
                              (logging-value-changed :parameter path e))
                 :box-position :right
                 :extra-class  "input--w18"
                 :label (translate-function :ignore)}])))

(defn handle-number-value [value]
  (cond
    (= :greater-zero value)
    0 ;! this needs to be handled properly!!!
    (= :zero value)
    0
    (= :greater-one value)
    2
    (= :greater-two value)
    3
    (vector? value)
    (let [[op boundary] value]
      (case op
        :<= boundary
        :< boundary
        :>= boundary)) ;! this needs to be handled properly!!!
    :else nil))

(defn min-number-boundaries [values]
  (if (vector? values)
    (let [[lower] values]
      (handle-number-value lower))
    (handle-number-value values)))

(defn max-number-boundaries [values]
  (if (vector? values)
    (let [[_ upper] values]
      (handle-number-value upper))
    (dec (aget js/Number "MAX_SAFE_INTEGER"))))

(defn get-power [value]
  (str
   "0."
   (str/join
    (take
     (max (- (let [dot-not (count (get (str/split (str value) #"\.") 1 ""))
                   e-not (js/parseFloat (get (str/split (str value) #"e\-") 1 ""))]
               (cond (< 0 dot-not)
                     dot-not
                     (< 0 e-not)
                     e-not
                     :else 0)) 1) 0)
     (repeatedly (constantly "0"))))
   "1"))

(defn pre-process-dependencies [translate-function value]
  (cond (keyword? value)
        (translate-function value)
        (vector? value)
        (let [[op val] value]
          (str (translate-function :explanation-value) (translate-function op) " " val))))

(defn explanation-section [desc-key active? valid? type content-valid? translate-function]
  (when active?
    (let [[dependent-parameter & conds] active?]
      [:div #_{:label (translate-function :explanation-div)}
       #_#_:icon (if valid? :success :warning)
       (when desc-key
         [row (translate-function :desc) (translate-function desc-key)])
       [row (translate-function :explanation-type) (translate-function type)]
       [row
        (str (translate-function :explanation-depending-on) (translate-function dependent-parameter))
        (str (translate-function :explanation-depending-valid)  (str/join ", " (mapv (partial pre-process-dependencies translate-function) conds)) "")]
       (case type
         :selection
         (into [:<>]
               (mapv (fn [[opt [depend-parameter & conds]]]
                       [row
                        (str opt (translate-function :explanation-depending) (translate-function depend-parameter))
                        (str (translate-function :explanation-depending-valid) (str/join ", " (mapv (partial pre-process-dependencies translate-function) conds)) "")])
                     content-valid?))
         (:integer :double)
         [row
          (translate-function :explanation-valid-range)
          (str
           "[ " ;! handle this
           (or (min-number-boundaries content-valid?) (translate-function :min)) ", "
           (or (max-number-boundaries content-valid?) (translate-function :max))
           " ]")])])))

;;; exception view

(defn length-date-configuration [length-key length-value length-default on-change-function on-blur disabled? content-valid? length-read-only?
                                 select-value defined-as defaults current-algorithm state translate-function {:keys [training-data-change logging-value-changed]}]
  (let [{:keys [simple?]} defaults]
    [:div {:style {:display "flex"}}
     [input-field
      {:label              (translate-function length-key)
       :thousand-separator (translate-function :thousand-separator)
       :decimal-separator  (translate-function :decimal-separator)
       :disabled?          (or disabled?
                               length-read-only?)
       :on-change          on-change-function
       :on-blur            on-blur
       :default-value      length-default
       :step               1
       :type               :number
       :min                (min-number-boundaries content-valid?)
       :max                (max-number-boundaries content-valid?)
       :value              (or length-value length-default)
       :extra-class        (if simple? "input--w6" "input--w18")}]
     (when (and simple? (seq (filter (fn [{value :value}]
                                       (= "date" value))
                                     (if (vector? select-value)
                                       select-value
                                       [select-value]))))
       (let [{:keys [read-only?]} @state
             {:keys [granularity]} defaults
             path [current-algorithm defined-as :attribute-config "date" :granularity]]
         [select {:disabled?          read-only?
                  :label (translate-function :unit-label)
                  :is-clearable? false
                  :options            (keyword-translate-options [:year :quarter :month :day] translate-function)
                  :on-change          (fn [e]
                                        (swap! state assoc-in path e)
                                        (logging-value-changed :settings path e)
                                        (training-data-change))
                  :values             (get-in @state path (keyword-translate-option granularity translate-function))
                  :extra-class        "input--w8"}]))]))

(defn exception-view [current-algorithm key key-value key-default on-change-function on-blur disabled? content-valid? read-only? current-algorithm-desc options
                      {:keys [training-data-change logging-value-changed] :as funcs} settings-state translate-function]
  (case key
    :length
    (let [independent-desc (get-in current-algorithm-desc [:requirements 1])
          {defined-as :defined-as
           number     :number
           types      :types}       independent-desc
          is-multi? (case number
                      :multi true
                      :single false
                      false)
          granularity-desc (get-in independent-desc [:input-config :date :default])
          path [current-algorithm defined-as :value]
          select-options
          (as-> @options $
            (flip-key-value->vec $)
            (select-keys $ types)
            (mapcat second $)
            (keyword-options $))
          value (get-in @settings-state path)
          value (if-not value
                  (let [value (if is-multi?
                                [(first select-options)]
                                (first select-options))]
                    (swap! settings-state assoc-in path value)
                    (logging-value-changed :settings path value)
                    (training-data-change)
                    value)
                  value)]
      [length-date-configuration key key-value key-default on-change-function on-blur disabled? content-valid? read-only?
       value defined-as granularity-desc current-algorithm settings-state translate-function funcs])))

(def exception-keys [:length])

;;; standard parameter view

(defn view [parameter-desc current-algorithm-desc current-algorithm attribute-options state settings-state translate-function {:keys [logging-value-changed training-data-change] :as funcs} params-valid? {:keys [simple? column parameter-key]}]
  (let [column-parameter-desc (filter identity (map #(%1 %2)
                                                    (cycle [identity (constantly nil)])
                                                    (cond->> parameter-desc
                                                      true
                                                      (sort-by (comp :row val))
                                                      (= column :right)
                                                      (drop 1))))
        {:keys [read-only?]} @state]
    (when (and column-parameter-desc
               (seq column-parameter-desc))
      (into [:div.settings__section]
            (for [[key {:keys [type content-valid? default
                               active? options ignore?
                               desc-key options-ref
                               multi-select? explanation]
                        :or   {content-valid? {}}}]
                  column-parameter-desc
                  :let [path [current-algorithm key :value]
                        value (get-in @state path)
                        ignore-value? (get-in @state [current-algorithm key :ignore?])
                        ignore-value? (if (boolean? ignore-value?) ignore-value? ignore?)
                        disabled? (or (parameter-disabled? active? parameter-desc current-algorithm @state)
                                      ignore-value?)
                        on-change-function (fn [value num-valid?]
                                             (logging-value-changed parameter-key path value)
                                             (swap! state assoc-in path value)
                                             (when (#{:integer :double} type)
                                               (swap! params-valid?
                                                      assoc-in
                                                      [current-algorithm key]
                                                      num-valid?)))
                        options (cond (and options
                                           (= :selection type))
                                      (keyword-translate-options options translate-function)
                                      options options
                                      (and options-ref
                                           (= :variable-reference (:type options-ref)))
                                      (let [options (get-in @settings-state [current-algorithm (:defined-as options-ref) :value])]
                                        (if (map? options)
                                          [(select-keys options [:label :value])]
                                          (mapv #(select-keys % [:label :value])
                                                options))))
                        default (cond (and default
                                           (= :selection type))
                                      (keyword-translate-option default translate-function)
                                      default default
                                      (and options-ref (:default-prio-by-type options-ref))
                                      (first (sort-by (fn [{:keys [value]}]
                                                        (.indexOf (:default-prio-by-type options-ref)
                                                                  (@attribute-options value)))
                                                      options)))
                        default (cond (and multi-select?
                                           (= :selection type)
                                           (vector? default))
                                      default
                                      (and multi-select?
                                           (= :selection type)
                                           (not (vector? default)))
                                      [default]
                                      :else default)
                        on-blur (fn [_]
                                  (training-data-change))
                        _ (when (and (nil? value)
                                     default
                                     (= key :aggregate-by-attribute))
                            (on-change-function default false)
                            (on-blur nil))
                        value (cond (and options-ref
                                         (= :variable-reference (:type options-ref))
                                         (empty? value)
                                         (seq options))
                                    (let [result (filter #(= (:value %) (:value default))
                                                         options)
                                          new-value (if-not (empty? result)
                                                      default
                                                      [(first options)])]
                                      (on-change-function new-value false)
                                      (on-blur nil)
                                      new-value)
                                    (and value
                                         options-ref
                                         (= :variable-reference (:type options-ref)))
                                    (let [valid? (set (map :value options))
                                          new-value (filterv (fn [{:keys [value]}]
                                                               (valid? value))
                                                             value)]
                                      (when (not= (vec value) new-value)
                                        (on-change-function new-value false))
                                      new-value)
                                    :else value)]]
              (when-not (and (= key :aggregate-by-attribute)
                             (= 1 (count options)))
                [:<>
                 [ignore-view (boolean? ignore?) state current-algorithm key logging-value-changed translate-function]
                 (if (some #{key} exception-keys)
                   [exception-view
                    current-algorithm
                    key
                    value
                    default
                    on-change-function
                    on-blur
                    disabled?
                    content-valid?
                    read-only?
                    current-algorithm-desc
                    attribute-options
                    funcs
                    settings-state
                    translate-function]
                   (case type
                     :selection
                     [select
                      (cond-> {:label              (str (translate-function key)
                                               ;! Put this somehere else
                                                        (when-let [selection-cond (content-valid? (:value value))]
                                                          (when (parameter-disabled? selection-cond parameter-desc current-algorithm @state)
                                                            (translate-function :advanced-selection-no-valid))))
                               :is-clearable? false
                               :disabled?          (or disabled?
                                                       read-only?)
                               :on-change          on-change-function
                               :on-blur            on-blur
                               :options            options
                               :values             (or value default)
                               :is-multi?          (boolean multi-select?)
                               :extra-class        "input--w18"}
                        explanation
                        (assoc :hint (translate-function explanation)))]
                     :boolean
                     [checkbox
                      (cond-> {:label        (translate-function key)
                               :disabled?    (or disabled?
                                                 read-only?)
                               :checked?     (or value default false)
                               :on-change    (fn [& args]
                                               (apply on-change-function args)
                                               (apply on-blur args))
                               :box-position :right
                               :extra-class  "input--w18"}
                        explanation
                        (assoc :hint (translate-function explanation)))]
                     :integer
                     [input-field
                      (cond-> {:label              (translate-function key)
                               :thousand-separator (translate-function :thousand-separator)
                               :decimal-separator  (translate-function :decimal-separator)
                               :disabled?          (or disabled?
                                                       read-only?)
                               :on-change          on-change-function
                               :on-blur            on-blur
                               :default-value      default
                               :step               1
                               :type               :number
                               :value              (or value default)
                               :extra-class        "input--w18"}
                        (min-number-boundaries content-valid?)
                        (assoc :min (min-number-boundaries content-valid?))
                        (max-number-boundaries content-valid?)
                        (assoc :max (max-number-boundaries content-valid?))
                        explanation
                        (assoc :hint (translate-function explanation)))]
                     :double
                     [input-field
                      (cond-> {:label              (translate-function key)
                               :thousand-separator (translate-function :thousand-separator)
                               :decimal-separator  (translate-function :decimal-separator)
                               :disabled?          (or disabled?
                                                       read-only?)
                               :on-change          on-change-function
                               :on-blur            on-blur
                               :default-value      default
                               :step               (get-power default)
                               :type               :number
                               :value              (or value default)
                               :extra-class        "input--w18"}
                        (min-number-boundaries content-valid?)
                        (assoc :min (min-number-boundaries content-valid?))
                        (max-number-boundaries content-valid?)
                        (assoc :max (max-number-boundaries content-valid?))
                        explanation
                        (assoc :hint (translate-function explanation)))]
                     :string
                     [input-field
                      (cond-> {:label              (translate-function key)
                               :disabled?          (or disabled?
                                                       read-only?)
                               :on-change          on-change-function
                               :on-blur            on-blur
                               :default-value      default
                               :value              (or value default)
                               :type               :string
                               :extra-class        "input--w18"}
                        explanation
                        (assoc :hint (translate-function explanation)))]))
                 [explanation-section desc-key active? (not disabled?) type content-valid? translate-function]]))))))
