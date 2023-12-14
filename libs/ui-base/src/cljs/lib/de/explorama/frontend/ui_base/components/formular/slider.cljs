(ns de.explorama.frontend.ui-base.components.formular.slider
  (:require [reagent.core :as r]
            [clojure.string :as clj-str]
            [de.explorama.frontend.ui-base.components.formular.input-field :refer [input-field]]
            [de.explorama.frontend.ui-base.components.common.core :refer [label tooltip error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [form-hint-class]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [cljsjs.rc-slider]))

(def parameter-definition
  {:value {:type [:vector :number :derefable]
           :required true
           :desc "Specifies the value of slider. When :range? is true it must be a Vector [<from> <to>] otherwise a number. When :range? is true and :ranges-count = n then it's a Vector with n+1 entries"}
   :range? {:type :boolean
            :desc "If true then a range is selectable"}
   :dots? {:type :boolean
           :desc "When the step value is greater than 1, you can set the dots to true if you want to render the slider with dots"}
   :dot-style {:type :map
               :desc "Styling of dots when :dots? is true"}
   :min {:type :number
         :desc "Minimum valid number"}
   :max {:type :number
         :desc "Maximum valid number"}
   :input-aria-label {:type [:string :derefable :keyword]
                      :desc "Aria label for the input field"}
   :left-input-aria-label {:type [:string :derefable :keyword]
                           :desc "Aria label for the left input field in a range"}
   :right-input-aria-label {:type [:string :derefable :keyword]
                            :desc "Aria label for the right input field in a range"}
   :clear-input-aria-label {:type [:string :derefable :keyword]
                            :desc "Aria label for the clear button in each input field"}
   :ranges-count {:type :number
                  :desc "Determine how many ranges to render"}
   :disabled? {:type :boolean
               :desc "If true, handles can't be moved"}
   :vertical? {:type :boolean
               :desc "If true, the slider will be vertical"}
   :add-parent? {:type :boolean
                 :desc "If true the parent-div will set if :label is set or :show-number-input? is true"}
   :included? {:type :boolean
               :desc "If the value is true, it means a continuous value interval, otherwise, it is a independent value."}
   :auto-marks? {:type :boolean
                 :desc "If true there will be automatically :auto-mark-count marks"}
   :auto-mark-count {:type :number
                     :desc "Defines the number of marks, that automatically will be added if :auto-marks? is true"}
   :number-input {:type :map
                  :desc "Parameters for number-input when :show-number-input? is true. When :range? is true then it's the first number-input"}
   :end-number-input {:type :map
                      :desc "Parameters for the second number-input when :show-number-input? and :range? are true"}
   :show-number-input? {:type :boolean
                        :desc "If true use can define range or single value over number-inputs. Limitation: Only visible when :ranges-count = 1 (:range? true), :vertical? = false and :auto-marks? = false"}
   :pushable? {:type [:number :boolean]
               :desc "Set to true to allow pushing of surrounding handles when moving a handle. When set to a number, the number will be the minimum ensured distance between handles."}
   :allow-crossing? {:type :boolean
                     :desc "AllowCross could be set as true to allow those handles to cross."}
   :reverse? {:type :boolean
              :desc "If the value is true, it means the component is rendered reverse."}
   :marks {:type :map
           :desc "Marks on the slider. The key determines the position, and the value determines what will show. If you want to set the style of a specific mark point, the value should be an object which contains style and label properties."}
   :step {:type :number
          :desc "Value to be added or subtracted on each step the slider makes. Must be greater than zero, and max - min should be evenly divisible by the step value. When marks is not an empty object, step can be set to null, to make marks as steps."}
   :extra-class {:type :string
                 :desc "You should avoid it, because the most common cases this component handles by itself. But if its necessary to have an custom css class on component, you can add it here as a string."}
   :label {:type [:string :component :derefable]
           :desc "An label for Slider. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :hint {:type [:derefable :string]
          :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
   :on-change {:type :function
               :default-fn-str "(fn [new-value])"
               :required true
               :desc "Will be triggered, if smth. is changed"}
   :num-format? {:type :boolean
                 :desc "If the numbers should be formated with a thousand seperator."}
   :thousand-separator {:type [:string :derefable]
                        :desc "Specifies the thousand separator."}
   :decimal-separator {:type [:string :derefable]
                       :desc "Specifies the decimal separator."}
   :language {:type [:string :derefable]
              :desc "Specifies the language to be used for formating min/max display."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:range? false
                         :dots? false
                         :included? true
                         :allow-crossing? true
                         :add-parent? true
                         :pushable? false
                         :show-number-input? true
                         :input-aria-label :aria-slider-input
                         :left-input-aria-label :aria-slider-left-input
                         :right-input-aria-label :aria-slider-right-input
                         :clear-input-aria-label :aria-clear
                         :number-input {:extra-class "input--w4"}
                         :end-number-input {:extra-class "input--w4"}
                         :auto-marks? false
                         :auto-mark-count 10
                         :ranges-count 1
                         :min 0
                         :max 100
                         :step 1
                         :dot-style {:width "4px"
                                     :height "8px"}})

(def slider-reversed-class "rc-slider-reversed")

(def range-slider (r/adapt-react-class (aget js/RcSlider "Range")))
(def normal-slider (r/adapt-react-class (aget js/RcSlider "default")))

(defn- calculate-auto-marks [auto-mark-count min-val max-val]
  (let [diff (- max-val min-val)
        auto-mark-step (js/Math.floor (/ diff auto-mark-count))]
    (reduce (fn [r mark-num]
              (assoc r mark-num {:label mark-num}))
            {min-val {:label min-val}
             max-val {:label max-val}}
            (mapv #(+ min-val (* % auto-mark-step))
                  (range 1 auto-mark-count)))))

(defn- space []
  [:div {:style {:width "20px"}}]) ;TODO r1/css move this to css

(defn- number-input-check [{:keys [show-number-input? vertical? ranges-count]}]
  (and show-number-input?
       (not vertical?)
       (= ranges-count 1)))

(defn- translate-std-params [{min-val :min max-val :max
                              :keys [vertical? reverse? dots? included?
                                     disabled? allow-crossing? pushable?
                                     step marks dot-style extra-class
                                     auto-marks? auto-mark-count
                                     on-change]}]
  (cond-> {}
    (boolean? reverse?) (assoc :reverse reverse?)
    extra-class (update :class-name conj extra-class)
    (boolean? reverse?) (update :class-name conj slider-reversed-class)
    :always (update :class-name #(clj-str/join " " %))
    (boolean? vertical?) (assoc :vertical vertical?)
    (boolean? dots?) (assoc :dots dots?)
    (boolean? included?) (assoc :included included?)
    (or (boolean? pushable?)
        (number? pushable?))
    (assoc :pushable pushable?)
    (boolean? allow-crossing?) (assoc :allow-cross allow-crossing?)
    disabled? (assoc :disabled true)
    min-val (assoc :min min-val)
    max-val (assoc :max max-val)
    step (assoc :step step)
    dot-style (assoc :dot-style dot-style)
    on-change (assoc :on-change #(on-change (js->clj %)))
    marks (assoc :marks (cond->> marks
                          auto-marks? (merge (calculate-auto-marks auto-mark-count min-val max-val)
                                             marks)))
    (and (not marks)
         auto-marks?)
    (assoc :marks (calculate-auto-marks auto-mark-count min-val max-val))))

(defn- range-sli [{min-val :min max-val :max
                   :keys [value step ranges-count end-number-input
                          on-change  number-input disabled?
                          num-format? thousand-separator
                          decimal-separator language
                          right-input-aria-label
                          left-input-aria-label clear-input-aria-label]
                   :as params}]
  (let [value (val-or-deref value)
        is-vec? (vector? value)]

    (cond-> [:<>]
      (number-input-check params)
      (conj
       [input-field (cond-> (merge (or number-input {})
                                   {:type :number
                                    :aria-label left-input-aria-label
                                    :clear-aria-label clear-input-aria-label
                                    :step step
                                    :value (if is-vec?
                                             (or (first value)
                                                 min-val)
                                             min-val)
                                    :min min-val
                                    :max (if is-vec?
                                           (or (second value)
                                               max-val)
                                           max-val)
                                    :on-change (fn [new-val]
                                                 (when on-change
                                                   (on-change [(or new-val min-val)
                                                               (or (second (or value []))
                                                                   max-val)])))})
                      disabled? (assoc :disabled? disabled?)
                      thousand-separator (assoc :thousand-separator thousand-separator)
                      decimal-separator (assoc :decimal-separator decimal-separator)
                      language (assoc :language language)
                      num-format? (assoc :num-format? num-format?))]
       [space])
      :always (conj [range-slider (cond-> (translate-std-params params)
                                    is-vec? (assoc :value value)
                                    ranges-count (assoc :count ranges-count))])
      (number-input-check params)
      (conj
       [space]
       [input-field (cond-> (merge (or end-number-input {})
                                   {:type :number
                                    :aria-label right-input-aria-label
                                    :clear-aria-label clear-input-aria-label
                                    :step step
                                    :value (if is-vec?
                                             (or (second value)
                                                 max-val)
                                             max-val)
                                    :min (if is-vec?
                                           (or (first value)
                                               min-val)
                                           min-val)
                                    :max max-val
                                    :on-change (fn [new-val]
                                                 (when on-change
                                                   (on-change [(or (first (or value []))
                                                                   min-val)
                                                               (or new-val max-val)])))})
                      disabled? (assoc :disabled? disabled?)
                      thousand-separator (assoc :thousand-separator thousand-separator)
                      decimal-separator (assoc :decimal-separator decimal-separator)
                      language (assoc :language language)
                      num-format? (assoc :num-format? num-format?))]))))

(defn- normal-sli [{min-val :min max-val :max
                    :keys [value step number-input on-change disabled?
                           num-format? thousand-separator
                           decimal-separator language input-aria-label
                           clear-input-aria-label]
                    :as params}]
  (let [value (val-or-deref value)]
    (cond-> [:<>]
      (number-input-check params)
      (conj
       [input-field (cond-> (merge (or number-input {})
                                   {:type :number
                                    :aria-label input-aria-label
                                    :clear-aria-label clear-input-aria-label
                                    :step step
                                    :value (or value min-val)
                                    :min min-val
                                    :max max-val
                                    :on-change (fn [new-val]
                                                 (when on-change
                                                   (on-change new-val)))})
                      disabled? (assoc :disabled? disabled?)
                      thousand-separator (assoc :thousand-separator thousand-separator)
                      decimal-separator (assoc :decimal-separator decimal-separator)
                      language (assoc :language language)
                      num-format? (assoc :num-format? num-format?))]
       [space])
      :always
      (conj [normal-slider (cond-> (translate-std-params params)
                             (number? value) (assoc :value value))]))))

(defn ^:export slider [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "slider" specification params)}
     (let [{lb :label :keys [range? label-params hint add-parent?]} params]
       (cond-> (if (and add-parent? (or lb (number-input-check params)))
                 [:div.explorama__form__input.explorama__form__flex]
                 [:<>])
         lb (conj [label (assoc (or label-params {})
                                :label lb)])
         range? (conj [range-sli params])
         (not range?) (conj [normal-sli params])
         hint (conj (when hint [tooltip {:text hint} [:div {:class form-hint-class} [:span]]]))))]))