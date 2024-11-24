(ns de.explorama.frontend.ui-base.components.formular.input-field
  (:require "react-number-format"
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.utils.interop :refer [safe-aget]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [input-parent-class input-text-class]]
            [de.explorama.frontend.ui-base.components.common.core :refer [parent-wrapper label error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]))

(def parameter-definition
  {:type {:type :keyword
          :characteristics [:text :number :color :password]
          :desc "Defines the type of input field"}
   :id {:type :string
        :desc "Adds :id to html-element. Should be unique"}
   :min {:type [:number :derefable]
         :desc "Minimum valid number, works only with :type :number"}
   :max {:type [:number :derefable]
         :desc "Maximum valid number, works only with :type :number"}
   :step {:type [:number :string]
          :desc "Specifies the stepping when pressing up/down arrow-keys, works only with :type :number."}
   :thousand-separator {:type [:string :derefable]
                        :desc "Specifies the thousand separator, works only with :type :number."}
   :aria-label {:type [:string :derefable :keyword]
                :required :aria-label
                :desc "The aria-label for the input. If not set either the placeholder or the label will be used. Should be set if the label is a component."}
   :clear-aria-label {:type [:string :derefable :keyword]
                      :desc "The aria-label for the clear button"}
   :decimal-separator {:type [:string :derefable]
                       :desc "Specifies the decimal separator, works only with :type :number."}
   :language {:type [:string :derefable]
              :desc "Specifies the language to be used for formating min/max display. Works only with :type :number."}
   :value {:type [:string :number :derefable]
           :desc "Specifies the content of input field. It can be a string or a derefable like an atom or an re-frame event. It's recommanded to use an derefeable"}
   :label {:type [:string :component :derefable]
           :required :aria-label
           :desc "An label for input field. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :hint {:type [:derefable :string]
          :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
   :start-icon {:type [:string :keyword]
                :required false
                :desc "An icon which will be placed before the input. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :default-value {:type [:string :number :derefable]
                   :desc "Specifies the content of input field when component did mount. It can be a string or a derefable like an atom or an re-frame event. It's recommanded to use an derefeable"}
   :max-length {:type :number
                :desc "The maximum lenght of content text"}
   :placeholder {:type [:string :derefable]
                 :required :aria-label
                 :desc "Content which is visible outgrayed when no value is set. It can be a string or a derefable like an atom or an re-frame event. It's recommanded to use an derefeable"}
   :compact? {:type :boolean
              :desc "Will decrease the height of the input field."}
   :prevent-dragging? {:type :boolean
                       :desc "If true, prevents from dragging"}
   :disabled? {:type :boolean
               :desc "If true, the input field will be grayed out and the on-blur will not be triggered"}
   :read-only? {:type :boolean
                :desc "If true, text can be selected but not deleted or changed"}
   :required? {:type :boolean
               :desc "If true, it is required to send an formular when the Input-Field is in form-tag"}
   :caption {:type [:string :component :derefable]
             :desc "Will desiplay a caption beneth the input element. Captions will be ignored for number inputs."}
   :invalid? {:type :boolean
              :desc "If true, both the input element and the caption will turn red. For number inputs: if set to true, input element and caption turn red, if undefined or false, the validity depends on the min and max check."}
   :no-error-hiding? {:type :boolean
                      :desc "Only for number input: If true, any error will always be displayed, i.e. right after load without the need to interact with the input field"}
   :extra-class {:type :string
                 :desc "You should avoid it, because the most common cases this component handles by itself. But if its necessary to have an custom css class on component, you can add it here as a string."}
   :autofocus?  {:type :boolean
                 :desc "Flag for autofocusing input. Only works, when input field is not disabled"}
   :name {:type :string
          :desc "Specifies the name of the input"}
   :auto-complete {:type :string
                   :characteristics ["on" "off"]
                   :desc "Specifies whether an input element should have autocomplete enabled"}
   :num-format? {:type :boolean
                 :desc "If the numbers should be formated with a thousand seperator."}
   :on-change {:type :function
               :default-fn-str "(fn [new-value])"
               :desc "Will be triggered, if input text is changed. Used by the clear button to set an empty string if not on-clear function is given."}
   :on-clear {:type :function
              :default-fn-str "(fn [event])"
              :desc "The function called by the clear button within the input element. If no function is given, it will instead set an empty string with the on-change function."}
   :on-blur {:type :function
             :default-fn-str "(fn [event])"
             :desc "Will be triggered, if user clicks outside of input field"}
   :on-key-down {:type :function
                 :default-fn-str "(fn [event])"
                 :desc "Will be triggered, if user is pressing a key"}
   :on-key-up {:type :function
               :default-fn-str "(fn [event])"
               :desc "Will be triggered, if user releases a key"}
   :on-key-press {:type :function
                  :default-fn-str "(fn [event])"
                  :desc "Will be triggered, if user presses a key"}
   :on-input {:type :function
              :default-fn-str "(fn [event])"
              :desc "Will be triggered, if input text is changed. This event is similar to the :on-change. The difference is that the :on-input event occurs immediately after the value of an element has changed, while :on-change occurs when the element loses focus, after the content has been changed "}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:prevent-dragging? false
                         :required? false
                         :read-only? false
                         :autofocus? false
                         :disabled? false
                         :clear-aria-label :aria-clear
                         :no-error-hiding? false
                         :type :text
                         :max-length 200
                         :min (inc (aget js/Number "MIN_SAFE_INTEGER"))
                         :max (dec (aget js/Number "MAX_SAFE_INTEGER"))
                         :on-change (fn [new-value])
                         :thousand-separator ","
                         :decimal-separator "."
                         :language "en-EN"
                         :num-format? true
                         :step 1})

(def numper-input-format (reagent/adapt-react-class js/NumberFormat))

(def error-class "invalid")
(def caption-class "input-hint")
(def compact-class "compact")

(def input-button-class "btn-clear")

(defn- assoc-type [props type]
  (assoc props :type (case type
                       :number "number"
                       :color "color"
                       :password "password"
                       "text")))

(defn- base-input-map [{:keys [id max-length
                               read-only? required?
                               disabled? autofocus?
                               prevent-dragging? aria-label
                               on-change on-blur on-key-down
                               on-key-up on-key-press on-input
                               type name auto-complete hint]
                        lb :label}
                       value
                       default-value
                       placeholder]
  (let [value (val-or-deref value)
        default-value (val-or-deref default-value)
        aria-label (translate-label aria-label)
        lb (val-or-deref lb)
        placeholder (val-or-deref placeholder)]
    (cond->  {}
      :always
      (assoc :aria-label (or aria-label lb placeholder))
      prevent-dragging?
      (assoc :draggable     false
             :on-drag-start #(.preventDefault %)
             :on-drag       #(.preventDefault %)
             :on-drag-end   #(.preventDefault %))
      name (assoc :name name)
      type (assoc-type type)
      id (assoc :id id)
      max-length (assoc :max-length max-length)
      required? (assoc :required true)
      read-only? (assoc :read-only true)
      disabled? (assoc :disabled true)
      placeholder (assoc :placeholder placeholder)
      auto-complete (assoc :autocomplete auto-complete)
      on-change (assoc :on-change (fn [e]
                                    (let [elem (aget e "nativeEvent" "target")
                                          value (aget elem "value")]
                                      (on-change value e))))
      on-key-down (assoc :on-key-down on-key-down)
      on-key-up (assoc :on-key-up on-key-up)
      on-key-press (assoc :on-key-press on-key-press)
      on-input (assoc :on-input on-input)
      on-blur (assoc :on-blur (fn [e]
                                (when-not
                                 (and (safe-aget e "relatedTarget")
                                      (= input-button-class
                                         (safe-aget e "relatedTarget" "className")))
                                  (on-blur e))))
      autofocus? (assoc :auto-focus true)
      default-value (assoc :default-value default-value)
      value (assoc :value value)
      hint (assoc :title hint))))

(defn- clear-button [{:keys [on-change
                             on-clear
                             read-only?
                             clear-aria-label]}]
  (let [clear-aria-label (translate-label clear-aria-label)]
    [:button {:class input-button-class
              :aria-label clear-aria-label
              :on-mouse-down #(.preventDefault %)
              :on-click #(do
                           (.preventDefault %)
                           (when-not read-only? (if on-clear (on-clear %) (on-change ""))))}
     [:span.icon-close]]))

(defn- num-val-valid? [min max num-val]
  (or
   (nil? num-val)
   (and (or (nil? min)
            (>= num-val min))
        (or (nil? max)
            (<= num-val max)))))

(defn- num-input [params]
  (let [{:keys [no-error-hiding?]} params
        input-valid? (reagent/atom false)
        show-error? (reagent/atom no-error-hiding?)
        internal-val (when-not (contains? params :value)
                       (reagent/atom nil))
        validate-input (fn [this]
                         (let [[_ params] (reagent/argv this)
                               {:keys [min max invalid?]
                                outside-value :value} params
                               min (val-or-deref min)
                               max (val-or-deref max)
                               invalid?  (val-or-deref invalid?)
                               num-val (val-or-deref (or internal-val
                                                         outside-value))]
                           (reset! input-valid? (and (not invalid?) (num-val-valid? min max num-val)))))]
    (reagent/create-class
     {:component-did-mount validate-input
      :component-did-update validate-input
      :reagent-render (fn [{:keys [default-value placeholder
                                   on-change on-blur on-key-down
                                   min max step language
                                   thousand-separator decimal-separator
                                   num-format? compact?]
                            outside-value :value
                            lb :label
                            :as params}]
                        [error-boundary {:validate-fn #(validate "input-field" specification params)}
                         (let [value (or internal-val
                                         outside-value)
                               min (val-or-deref min)
                               max (val-or-deref max)
                               language (val-or-deref language)
                               thousand-separator (val-or-deref thousand-separator)
                               decimal-separator (val-or-deref decimal-separator)
                               min? (and
                                     (number? min)
                                     (not= min (:min default-parameters)))
                               max? (and
                                     (number? max)
                                     (not= max (:max default-parameters)))
                               set-error? (and (not @input-valid?) @show-error?)
                               step (js/Number step)]
                           [:div {:class [input-parent-class
                                          (when set-error?
                                            error-class)]}
                            (when lb [label params])
                            [:div {:class (cond-> [input-text-class]
                                            compact? (conj compact-class))}
                             [numper-input-format (-> (base-input-map params value default-value placeholder)
                                                      (dissoc :type) ;Otherwise thousand-seperator will break parsing...
                                                      (assoc :thousand-separator (if num-format?
                                                                                   thousand-separator
                                                                                   "")
                                                             :decimal-separator decimal-separator
                                                             :allow-negative true
                                                             :on-blur (fn [e]
                                                                        (when-not
                                                                         (and (safe-aget e "relatedTarget")
                                                                              (= input-button-class
                                                                                 (safe-aget e "relatedTarget" "className")))
                                                                          (reset! show-error? true)
                                                                          (when on-blur
                                                                            (on-blur e))))
                                                             :on-change (fn [_])
                                                             :on-key-down (fn [e]
                                                                            (let [cur-val (val-or-deref value)
                                                                                  cur-val (if (number? cur-val)
                                                                                            cur-val
                                                                                            (js/Number cur-val))
                                                                                  step-fn (cond
                                                                                            (= (.-keyCode e) 38) + ; Arrow UP
                                                                                            (= (.-keyCode e) 40) - ; Arrow Down
                                                                                            )
                                                                                  updated-val (when step-fn
                                                                                                (step-fn (or cur-val 0) step))
                                                                                  new-val-valid? (when updated-val
                                                                                                   (num-val-valid? min max updated-val))]
                                                                              (when step-fn
                                                                                (.preventDefault e)
                                                                                (when internal-val
                                                                                  (reset! internal-val updated-val))
                                                                                (when on-change
                                                                                  (on-change updated-val new-val-valid? e))))
                                                                            (when on-key-down
                                                                              (on-key-down e)))
                                                             :on-value-change (fn [e]
                                                                                (let [val (aget e "value")
                                                                                      num-val (aget e "floatValue")
                                                                                      valid? (num-val-valid? min max num-val)]
                                                                                  (when internal-val
                                                                                    (reset! internal-val val))
                                                                                  (when on-change
                                                                                    (on-change num-val valid? e))))))]
                             [clear-button params]]
                            (when (or min? max?)
                              [:div {:class [caption-class]}
                               (cond
                                 (and min? num-format?) (str "Min: " (.toLocaleString min
                                                                                      language))
                                 min? (str "Min: " min))
                               (when (and min? max?)
                                 " , ")
                               (cond
                                 (and max? num-format?) (str "Max: " (.toLocaleString max
                                                                                      language))
                                 max? (str "Max: " max))])])])})))

(defn input-parentless [{:keys [value
                                type
                                default-value
                                placeholder
                                start-icon
                                compact?]
                         :as params}]
  [error-boundary {:validate-fn #(validate "input-field" specification params)}
   [:div {:class (cond-> []
                   (not= type :color) (conj input-text-class)
                   compact? (conj compact-class))}
    (when start-icon
      [icon  {:icon start-icon}])
    [:input (base-input-map params value default-value placeholder)]
    (when-not (= type :color)
      [clear-button params])]])

(defn ^:export input-field [params]
  [error-boundary
   (let [{:keys [type]
          :as params} (merge default-parameters params)]
     (if (= type :number)
       [num-input params]
       [parent-wrapper params [input-parentless params]]))])