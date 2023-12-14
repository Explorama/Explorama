(ns de.explorama.frontend.search.views.components.elements
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [date-picker input-field select]]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.search.config :as config]
            [taoensso.tufte :as tufte]
            [de.explorama.shared.common.unification.time :as t]))

(defn all-group-selected?
  "Checks if all group is selected in select-component"
  [selections]
  (and
   (vector? selections)
   (= 1 (count selections))
   (= :group (get-in selections [0 :type]))))

(defn reduce-selections
  "Removes some entries when all group is selected in select-component to reduced space in db"
  [selections]
  (cond-> selections
    ;Remove some entries when all group is selected to reduced space in db
    (all-group-selected? selections)
    (update 0 dissoc :options :all-options :group-count :filtered-count)))

(defn selection-dropdown [{:keys [all-group? placeholder on-change on-blur
                                  is-grouped?
                                  values options classname autofocus show-clean-all disabled?
                                  tooltip-key]
                           :or   {show-clean-all true
                                  all-group? false
                                  tooltip-key :label}}]
  (let [invalid-option-hint @(re-frame/subscribe [::i18n/translate :invalid-option-hint])
        all-group-label @(re-frame/subscribe [::i18n/translate :select-all-group-label])]
    [select (cond-> {:placeholder           placeholder
                     :show-clean-all?       (boolean show-clean-all)
                     :autofocus?            (boolean autofocus)
                     :options               options
                     :values                values
                     :is-grouped?           (boolean is-grouped?)
                     :on-change             on-change
                     :on-blur               on-blur
                     :tooltip-key           tooltip-key
                     :is-multi?             true
                     :show-all-group?       (boolean all-group?)
                     :all-group-label       all-group-label
                     :group-selectable?     false
                     :close-on-select?      false
                     :mark-invalid?         true
                     :invalid-hint          invalid-option-hint
                     :show-options-tooltip? true
                     :disabled?             (boolean disabled?)}
              classname (assoc :extra-class classname))]))

(defn one-selection-dropdown [{:keys [is-grouped? placeholder on-change on-blur values options classname autofocus show-clean-all disabled?]
                               :or   {show-clean-all true}}]
  (let [invalid-option-hint @(re-frame/subscribe [::i18n/translate :invalid-option-hint])]
    [select (cond-> {:placeholder           placeholder
                     :show-clean-all?       (boolean show-clean-all)
                     :is-grouped?           (boolean is-grouped?)
                     :group-selectable?     false
                     :autofocus?            (boolean autofocus)
                     :options               options
                     :values                values
                     :on-change             on-change
                     :on-blur               on-blur
                     :mark-invalid?         true
                     :invalid-hint          invalid-option-hint
                     :show-options-tooltip? true
                     :disabled?             (boolean disabled?)}
              classname (assoc :extra-class classname))]))

(defn sinput [{:keys [on-change on-blur classname defaultvalue value autofocus disabled?]}]
  [input-field (cond->  {:prevent-dragging? true
                         :extra-class classname
                         :on-change on-change
                         :on-blur on-blur
                         :disabled? disabled?}
                 autofocus (assoc :autofocus? true)
                 defaultvalue (assoc :default-value defaultvalue)
                 value (assoc :value value))])

(defn number-input [{:keys [on-change on-blur classname value min max is-int? autofocus disabled?]}]
  (let [thousand-sep @(re-frame/subscribe [::i18n/translate :thousand-separator])
        decimal-sep @(re-frame/subscribe [::i18n/translate :decimal-separator])
        lang @(re-frame/subscribe [::i18n/current-language])]
    [input-field (cond->  {:prevent-dragging? true
                           :disabled? disabled?
                           :type :number
                           :thousand-separator thousand-sep
                           :decimal-separator decimal-sep
                           :language lang
                           :extra-class   classname
                           :on-change     on-change
                           :on-blur       on-blur
                           :step          (if is-int?
                                            1
                                            "any")
                           :value         value}
                   min (assoc :min min)
                   max (assoc :max max)
                   autofocus (assoc :autofocus? true))]))

(def default-datepicker-props
  {:placeholder t/date-format-placeholder
   :allow-single-day-range true
   :on-format-date (fn [d] (t/obj->date-str d))
   :on-parse-date (fn [d] (t/date-str->obj d))})

(defn filter-date? [possible-dates d]
  (tufte/p ::date-picker-filter-per-element
           (let [d (t/from-date d)]
             (not (some (fn [d2]
                          (t/is-same-day? d d2))
                        (get possible-dates (t/get-month-year d)))))))

;Only for messure times of filter-op
(defn timelog [possible-dates]
  (let [check-dates (mapv #(t/date-time 2003 6 %) ; -> 2003-07-[01-31] depends on ACs if avail.
                          (range 2 33))]
    (tufte/profile
     {:when config/timelog-day-calc?}
     (doseq [d check-dates]
       (filter-date? possible-dates d)))))

(defn date-input [{:keys [extra-class path on-blur on-change selected-date min-date max-date possible-dates disabled?]}]
  [error-boundary
   (let [lang (name @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language]))
         selected-date-obj (t/to-date (t/date-str->obj selected-date))]
    ;workaround to messure, because day-picker triggers filter-ops for every Date
     (when config/timelog-day-calc?
       (timelog possible-dates))
     [date-picker (cond-> (merge default-datepicker-props
                                 {:value selected-date-obj
                                  :lang lang
                                  :disabled? disabled?
                                  :on-change (fn [d]
                                               (let [d (cond
                                                         (< d min-date) min-date
                                                         (> d max-date) max-date
                                                         :else d)]
                                                 (on-change d)))
                                  :on-filter (partial filter-date? possible-dates)})
                    on-blur (assoc :on-blur on-blur)
                    extra-class (assoc :extra-class extra-class)
                    min-date (assoc :min-date min-date)
                    max-date (assoc :max-date max-date))])])

(defn date-range-input [{:keys [path on-blur on-change min-date max-date start-date end-date disabled?]}]
  [error-boundary]
  (let [lang (name @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language]))]
    [date-picker (cond-> (merge default-datepicker-props
                                {:lang lang
                                 :range? true
                                 :disabled? disabled?
                                 :on-change (fn [drange]
                                              (let [[sdate edate] (js->clj drange)]
                                                (on-change sdate edate)))})
                   (or start-date end-date)
                   (assoc :value [(when start-date (t/date-str->obj start-date))
                                  (when end-date (t/date-str->obj end-date))])
                   on-blur (assoc :on-blur on-blur)
                   min-date (assoc :min-date min-date)
                   max-date (assoc :max-date max-date))]))