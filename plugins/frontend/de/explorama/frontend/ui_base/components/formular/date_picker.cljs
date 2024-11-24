(ns de.explorama.frontend.ui-base.components.formular.date-picker
  (:require [reagent.core :as r]
            [de.explorama.shared.common.unification.time :as t]
            [de.explorama.frontend.ui-base.components.common.core :refer [label tooltip error-boundary]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [form-hint-class input-parent-class input-text-class]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:possible-dates {:type [:vector :derefable]
                    :desc "Dates which can be selected. Must be date-objects (js/Date.)"}
   :label {:type [:string :component :derefable]
           :desc "An label for date-picker. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :hint {:type [:derefable :string]
          :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
   :placeholder {:type [:derefable :string]
                 :desc "Content which is visible outgrayed when no value is set"}
   :allow-single-day-range? {:type :boolean
                             :desc "If true, user can choose same date for from and to when :range? is true"}
   :selected-as-string? {:type :boolean
                         :desc "If true, first parameter of :on-change will be a date-string. Else it is a date-obj"}
   :out-of-range-message {:type [:derefable :string]
                          :desc "Message which will be visible if a selected range is out of range"}
   :invalid-date-message {:type [:derefable :string]
                          :desc "Message which will be visible if a selected range is invalid"}
   :extra-class {:type :string
                 :desc "You should avoid it, because the most common cases this component handles by itself. But if its necessary to have an custom css class on component, you can add it here as a string."}
   :lang {:type [:derefable :keyword]
          :characteristics [:de-DE :en-GB]
          :desc "The language for menu (like the month names etc.)"}
   :value {:type [:string :date :vector :derefable]
           :desc "Specifies the selected date. If :range is true, it must be a vector [<from-date><to-date>] or a derefable which return it. Both can be a date-string or a date-object. If :range is false it is a date-string, a date-object or a derefable which returns one of it"}
   :max-date {:type [:date :string]
              :desc "The latest date the user can select"}
   :min-date {:type [:date :string]
              :desc "The earliest date the user can select"}
   :disabled? {:type [:derefable :boolean]
               :desc "If true, open/closing is disabled"}
   :range? {:type :boolean
            :desc "If true, a date range is selectable and value is a vector [<from-date><to-date>]"}
   :close-on-select? {:type :boolean
                      :desc "If true, date-picker will be closed on select"}
   :on-change {:type :function
               :default-fn-str "(fn [selected-date])"
               :desc "Triggered when user selects a value"}
   :on-filter {:type :function
               :default-fn-str "(fn [date]\n <check if date is in :possible-dates>)"
               :desc "Will be used to filter selectable dates"}
   :on-format-day {:type :function
                   :default-fn-str "(fn [date lang]\n (obj->date-str date))"
                   :desc "Will be called to format the day"}
   :on-format-date {:type :function
                    :default-fn-str "(fn [date lang]\n (obj->date-str date))"
                    :desc "Will be called to format the date"}
   :on-parse-date {:type :function
                   :default-fn-str "(fn [date lang]\n (date-str->obj date))"
                   :desc "Will be called to parse the :dates"}
   :on-blur {:type :function
             :default-fn-str "(fn [event])"
             :desc "Will be triggered, if user clicks outside of input field"}})
(def specification (parameters->malli parameter-definition nil))

#_(def date-range-input (r/adapt-react-class DateRangeInput))
#_(def date-input (r/adapt-react-class DateInput))

(def en-GB-days-short ["Su" "Mo" "Tu" "We" "Th" "Fr" "Sa"])
(def en-GB-months ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"])

(def de-DE-days-short ["So" "Mo" "Di" "Mi" "Do" "Fr" "Sa"])
(def de-DE-months ["Januar" "Februar" "März" "April" "Mai" "Juni" "Juli" "August" "September" "Oktober" "November" "Dezember"])

(defn- day-short [day-num lang]
  (case lang
    "de-DE" (get de-DE-days-short day-num)
    "en-GB" (get en-GB-days-short day-num)
    (get en-GB-days-short day-num)))

(defn- months [lang]
  (case lang
    "de-DE" de-DE-months
    "en-GB" en-GB-months
    en-GB-months))

(def input-group-class "inputs-grouped")

(defn- filter-date? [possible-dates d]
  (not (some (fn [d2]
               (t/is-same-day? d d2))
             possible-dates)))

(def default-parameters {:range? false
                         :close-on-select? true
                         :allow-single-day-range? true
                         :lang :en-GB
                         :selected-as-string? false
                         :out-of-range-message "Out of range"
                         :invalid-date-message "Invalid date"
                         :placeholder t/date-format-placeholder
                         :on-format-day (fn [d] (t/obj->date-str d))
                         :on-format-date (fn [d] (t/obj->date-str d))
                         :on-parse-date (fn [d] (t/date-str->obj d))})

(defn- value->date [value]
  (cond (string? value)
        (t/date-str->obj value)
        (instance? js/Date value)
        value
        :else nil))

(defn- apply-default-params [{:keys [lang on-blur range? on-filter
                                     allow-single-day-range? invalid-date-message
                                     disabled? close-on-select?
                                     possible-dates min-date max-date
                                     placeholder extra-class out-of-range-message
                                     on-format-day on-format-date on-parse-date]}]
  (let [extra-class [extra-class input-text-class]]
  ;hidden defaults for date-picker
    (cond-> {:localeUtils {:format-day (fn [d] (t/obj->date-str d))
                           :getMonths (fn [lang] (clj->js (months lang)))
                           :formatWeekdayLong (fn [d-num] "") ;is required but not in use currently
                           :formatWeekdayShort day-short}
             :dayPickerProps {:firstDayOfWeek 1}
             :shortcuts false
             :popover-props {:boundary "viewport"}
             :formatDate (fn [d] (t/obj->date-str d))
             :parseDate (fn [d] (t/date-str->obj d))}
      allow-single-day-range? (assoc :allow-single-day-range true)
      on-blur (assoc :on-blur :on-blur)
      out-of-range-message (assoc :outOfRangeMessage out-of-range-message)
      invalid-date-message (assoc :invalidDateMessage invalid-date-message)
      disabled? (assoc :disabled true)
      close-on-select? (assoc :closeOnSelection true)
      possible-dates (assoc-in [:dayPickerProps :disabled-days] (partial filter-date? possible-dates))
      on-filter (assoc-in [:dayPickerProps :disabled-days] on-filter)
      min-date (assoc :min-date (value->date min-date))
      max-date (assoc :max-date (value->date max-date))
      placeholder (assoc :placeholder placeholder)
      (and (not range?) extra-class) (assoc-in [:inputProps :className] extra-class)
      (and range? extra-class) (assoc :start-input-props {:className extra-class})
      (and range? extra-class) (assoc :end-input-props {:className extra-class})
      lang (assoc :locale (cond-> lang
                            (keyword? lang) (name)))
      on-format-day (assoc-in [:localUtils :format-day] on-format-day)
      on-format-date (assoc :formatDate on-format-date)
      on-parse-date (assoc :parseDate on-parse-date))))

(defn- single-date-input [{:keys [value on-change selected-as-string?] :as params}]
  (let [value (if (or (string? value)
                      (instance? js/Date value))
                (value->date value)
                (val-or-deref value))
        value (value->date value)]
    #_[date-input (cond-> (assoc (apply-default-params params)
                                 :value value)
                    on-change (assoc :on-change
                                     (fn [d user-change?]
                                       (when user-change?
                                         (on-change (cond-> d
                                                      selected-as-string? (t/obj->date-str)))))))]))

(defn- range-date-input [{:keys [value on-change selected-as-string?] :as params}]
  (let [value (or (val-or-deref value)
                  [nil nil])
        value (when value [(value->date (first value))
                           (value->date (second value))])]
    [:div {:class input-group-class}
     #_[date-range-input (cond-> (assoc (apply-default-params params)
                                        :value value)
                           on-change (assoc :on-change
                                            (fn [drange]
                                              (let [[sdate edate :as drange] (js->clj drange)]
                                                (when drange
                                                  (on-change (if  selected-as-string?
                                                               [(t/obj->date-str sdate)
                                                                (t/obj->date-str edate)]
                                                               drange)))))))]]))

(defn ^:export date-picker [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "date-picker" specification params)}
     (let [{lb :label :keys [label-params range? lang hint
                             possible-dates placeholder disabled?
                             out-of-range-message invalid-date-message]}
           params
           possible-dates (val-or-deref possible-dates)
           placeholder (val-or-deref placeholder)
           lang (val-or-deref lang)
           disabled? (val-or-deref disabled?)
           out-of-range-message (val-or-deref out-of-range-message)
           invalid-date-message (val-or-deref invalid-date-message)
           params (assoc params
                         :possible-dates possible-dates
                         :placeholder placeholder
                         :lang lang
                         :disabled? disabled?
                         :out-of-range-message out-of-range-message
                         :invalid-date-message invalid-date-message)]
       [:div {:class input-parent-class}
        (when lb
          [label (assoc (or label-params {})
                        :label lb)])
        (if range?
          [range-date-input params]
          [single-date-input params])
        (when hint
          [tooltip {:text hint}
           [:div {:class form-hint-class}
            [:span]]])])]))