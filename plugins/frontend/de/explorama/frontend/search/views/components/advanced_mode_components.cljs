(ns de.explorama.frontend.search.views.components.advanced-mode-components
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [checkbox]]
            [de.explorama.frontend.search.views.components.elements :as elements]
            [de.explorama.shared.search.conditions-utils :as cond-utils]
            [de.explorama.shared.common.unification.time :as t]
            [reagent.core :as reagent]
            [clojure.string :as st]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(def number-conditions-ops (atom cond-utils/number-conditions-ops))
(def text-conditions-ops (atom cond-utils/text-conditions-ops))
(def contains-conditions-ops (atom cond-utils/contains-conditions-ops))
(def day-year-conditions-ops (atom cond-utils/day-year-conditions-ops))
(def month-conditions-ops (atom cond-utils/month-conditions-ops))

(defonce dissoc-keys [:all-values? :empty-values?
                      :ui-selection :from :to :value :values
                      :selected-date :start-date :end-date])

(re-frame/reg-event-db
 ::change-condition-reset
 [(fi/ui-interceptor)]
 (fn [db [_ path]]
   (let [formdata-attr (get-in db path)]
     (assoc-in db path (apply dissoc
                              formdata-attr
                              dissoc-keys)))))

(defn selection-func
  ([path key on-change trigger-on-mount]
   (fn [selection]
     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path key selection])
     (when (and on-change (not trigger-on-mount))
       (on-change "selection-function"))))
  ([path key on-change]
   (selection-func path key on-change false)))

(defn simple-mode-view [{:keys [path on-change disabled?]}]
  [:span.input__mode {:on-click #(when-not disabled?
                                   (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/reset-values-from-attr path])
                                   (when on-change (on-change)))}
   @(re-frame/subscribe [::i18n/translate :deactivate-advancedmode-label])])

(defn same-condition-type? [old-cond new-cond]
  (let [old-is-range? (st/includes? (get old-cond :label) "range")
        new-is-range? (st/includes? (get new-cond :label) "range")]
    (= old-is-range? new-is-range?)))

(defn condition-select [{:keys [path key on-change default]}]
  (let [condition-placeholder @(re-frame/subscribe [::i18n/translate :condition-placeholder])
        condi (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/condition path default])]
    (reagent/create-class
     {:display-name        "select-condition"
      :reagent-render      (fn [{:keys [path key on-change on-blur ops disabled?]}]
                             (with-meta
                               [elements/one-selection-dropdown
                                {:placeholder    condition-placeholder
                                 :disabled? disabled?
                                 :show-clean-all false
                                 :on-change      (fn [selection]
                                                   (when (not (same-condition-type? @condi selection))
                                                     (re-frame/dispatch [::change-condition-reset path]))
                                                   (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path key selection])
                                                   (when on-change
                                                     (on-change "condition-select change")))
                                 :on-blur        on-blur
                                 :values         condi
                                 :options        ops
                                 :classname      "input--w8"}]
                               {:key (str path "-" :cond)}))
      :component-did-mount #(when (= @condi default)
                              ((selection-func path key on-change true)
                               default))})))

(defn text-condition-select [props]
  [condition-select (assoc props
                           :ops text-conditions-ops
                           :default cond-utils/text-conditions-default)])

(defn number-condition-select [props]
  [condition-select (assoc props
                           :ops number-conditions-ops
                           :default cond-utils/number-conditions-default)])

(defn range-number-condition-select [props]
  [condition-select (assoc props
                           :ops number-conditions-ops
                           :default cond-utils/range-number-conditions-default)])

(defn contains-condition-select [props]
  [condition-select (assoc props
                           :ops contains-conditions-ops
                           :default cond-utils/contains-conditions-default)])

(defn day-year-condition-selet [props]
  [condition-select (assoc props
                           :ops day-year-conditions-ops
                           :default cond-utils/range-number-conditions-default)])

(defn month-condition-select [props]
  [condition-select (assoc props
                           :ops month-conditions-ops
                           :default cond-utils/text-conditions-default)])

(defn component-wrapper [{:keys [path on-blur on-blur-range on-change disabled?] :as props} & childs]
  (let [empty-label @(re-frame/subscribe [::i18n/translate :any-empty-values-label])
        any-label @(re-frame/subscribe [::i18n/translate :any-non-empty-values-label])]
    (conj [:<> (apply conj
                      [:div.multiple__inputs]
                      childs)]
          (with-meta
            [checkbox {:label any-label
                       :disabled? disabled?
                       :id (str "cb_all_values_" path)
                       :checked? (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/all-values? path])
                       :on-change (fn [new-val]
                                    (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :all-values? new-val])
                                    (when new-val
                                      (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :empty-values? false]))
                                    (when on-change
                                      (on-change))
                                    (when (and on-blur (not on-blur-range))
                                      (on-blur))
                                    (when on-blur-range
                                      (on-blur-range)))}]
            {:key (str "cb_all_values_" path)})
          (with-meta
            [checkbox {:label empty-label
                       :disabled? disabled?
                       :id (str "cb_empty-values_" path)
                       :checked? (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/empty-values? path])
                       :on-change (fn [new-val]
                                    (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :empty-values? new-val])
                                    (when new-val
                                      (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :all-values? false]))
                                    (when on-change
                                      (on-change))
                                    (when (and on-blur (not on-blur-range))
                                      (on-blur))
                                    (when on-blur-range
                                      (on-blur-range)))}]
            {:key (str "cb_empty-values_" path)})
          [simple-mode-view props])))

(defn range-select [{:keys [path on-change on-blur on-blur-range autofocus disabled? child] :as props}] ;path attr options on-change onblurfunc onblurfunc-range]
  (let [select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        value (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])
        from-options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/options-from path])
        to-options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/options-to path])
        from (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/from path])
        to (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/to path])
        condi (:value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/condition path]))
        range-condi? (cond-utils/range-condition? condi)
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [range-number-condition-select (assoc props
                                              :key :cond
                                              :on-blur (if (cond-utils/range-condition? condi)
                                                         on-blur-range
                                                         on-blur))]
        [:br]])
     (when (and (not range-condi?) can-define-values?)
       (with-meta
         [elements/one-selection-dropdown
          {:placeholder select-placeholder
           :autofocus   autofocus
           :on-change   (selection-func path :value on-change)
           :on-blur     on-blur
           :values      value
           :options     from-options
           :classname   "input--w100"
           :disabled? disabled?}]
         {:key (str path "-" :value)}))
     (when (and range-condi? can-define-values?)
       [:div.flex.gap-8
        (with-meta
          [elements/one-selection-dropdown
           {:placeholder select-placeholder
            :autofocus   autofocus
            :on-change   (selection-func path :from on-change)
            :on-blur     on-blur-range
            :values      from
            :options     from-options
            :disabled? disabled?}]
          {:key (str path "-" :from)})
        [icon {:icon :minus}]
        (with-meta
          [elements/one-selection-dropdown
           {:placeholder select-placeholder
            :on-change   (selection-func path :to on-change)
            :on-blur     on-blur-range
            :values      to
            :options     to-options
            :disabled? disabled?}]
          {:key (str path "-" :to)})])
     child]))

(defn range-number-input [{:keys [path on-change on-blur on-blur-range is-int? autofocus disabled? child] :as props}]
  (let [value (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])
        from (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/from path])
        to (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/to path])
        condi (:value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/condition path]))
        range-condi? (cond-utils/range-condition? condi)
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [range-number-condition-select (assoc props :key :cond
                                              :on-blur (if (cond-utils/range-condition? condi)
                                                         on-blur-range
                                                         on-blur))]])
     (when (and (not range-condi?) can-define-values?)
       (with-meta
         [elements/number-input
          {:on-change (fn [new-val]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :value new-val])
                        (when on-change
                          (on-change "adv. range-numper-input (not range)")))
           :on-blur   on-blur
           :autofocus autofocus
           :classname "input--w100"
           :value     value
           :is-int?   is-int?
           :disabled? disabled?}]
         {:key (str path "-" :value)}))

     (when (and can-define-values? range-condi?)
       [:div.flex.gap-8.min-w-0
        (with-meta
          [elements/number-input
           {:on-change (fn [new-val]
                         (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :from new-val])
                         (when on-change
                           (on-change "adv. range-numper-input (range from)")))
            :on-blur   on-blur-range
            :autofocus autofocus
            :classname "input--w4"
            :value     from
            :max       to
            :is-int?   is-int?
            :disabled? disabled?}]
          {:key (str path "-" :from)})
        [icon {:icon :minus}]
        (with-meta
          [elements/number-input
           {:on-change (fn [new-val]
                         (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :to new-val])
                         (when on-change
                           (on-change "adv. range-numper-input (range to)")))
            :on-blur   on-blur-range
            :classname "input--w4"
            :value     to
            :min       from
            :is-int?   is-int?
            :disabled? disabled?}]
          {:key (str path "-" :to)})])
     child]))

(defn string-selection-input [{:keys [class path options on-change on-blur autofocus disabled? child] :as props}]
  (let [ui-selection (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])
        select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [text-condition-select (assoc props :key :cond)]
        [:br]])
     (when can-define-values?
       (with-meta
         [elements/selection-dropdown
          {:placeholder select-placeholder
           :autofocus autofocus
           :on-change (fn [selections]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection selections])
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values (mapv (fn [{value :value}]
                                                                                                                                value)
                                                                                                                              selections)])
                        (when on-change
                          (on-change)))
           :on-blur on-blur
           :values ui-selection
           :options options
           :classname class
           :disabled? disabled?}]
         {:key (str path "-" :ui-selection)}))
     child]))

(defn simple-input [{:keys [path class on-change on-blur autofocus disabled? child] :as props}]
  (let [value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [text-condition-select (assoc props :key :cond)]
        (with-meta
          [elements/sinput
           {:autofocus autofocus
            :on-change    on-change
            :on-blur      (fn [new-val]
                            (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :value new-val])
                            (on-blur))
            :classname    class
            :value value
            :disabled? disabled?}]
          {:key (str path "-" :value)})])
     child]))

(defn contains-input [{:keys [path class on-change child autofocus disabled?] :as props}]
  (let [value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [contains-condition-select (assoc props :key :cond)]
        (with-meta
          [elements/sinput
           {:autofocus autofocus
            :on-change (fn [new-val]
                         (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :value new-val])
                         (when on-change (on-change)))
            :classname class
            :value value
            :disabled? disabled?}]
          {:key (str path "-adv" :value)})])
     child]))

;; DATE RELATED INPUTS ====================

(defn date-range-input [{:keys [path options on-change on-blur disabled? child] :as props}]
  (let [condi (:value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/condition path]))
        [start-date end-date] @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/date-range path])
        selected-date @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/selected-date path])
        min-date @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/min-date path])
        max-date @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/max-date path])
        last-x-value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/last-x-value path])
        props (assoc props
                     :selected-date selected-date
                     :min-date min-date
                     :max-date max-date)
        range-condi? (cond-utils/range-condition? condi)
        is-current? (cond-utils/current-condition? condi)
        is-last-x? (cond-utils/last-x? condi)
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])
        possible-dates @options]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [day-year-condition-selet (assoc props :key :cond)]])
     (when (and can-define-values? is-last-x?)
       (with-meta
         [elements/number-input
          {:on-change (fn [e]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :last-x e])
                        (on-change)
                        (on-blur))
           :on-blur #(when last-x-value (on-blur))
           :value last-x-value
           :is-int? true}]
         {:key (str path "-" :adv-last-x-days)}))
     (when (and (not range-condi?) can-define-values? (not is-current?) (not is-last-x?))
       (with-meta
         [elements/date-input
          {:on-change     (fn [e]
                            (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :selected-date (t/obj->date-str e)])
                            (on-change)
                            (on-blur))
           :path path
           :selected-date selected-date
           :on-blur       #(when selected-date (on-blur))
           :min-date      min-date
           :max-date      max-date
           :possible-dates possible-dates
           :extra-class "input--w100"
           :disabled? disabled?}]
         {:key (str path "-" :adv-selected-date)}))
     (when (and can-define-values? range-condi? (not is-current?) (not is-last-x?))
       (with-meta
         [elements/date-range-input
          (assoc props :on-change
                 (fn [sdate edate]
                   (when sdate
                     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :start-date (t/obj->date-str sdate)]))
                   (when edate
                     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :end-date (t/obj->date-str edate)]))
                   (when (and sdate edate)
                     (on-change)
                     (on-blur)))
                 :start-date start-date
                 :end-date end-date
                 :min-date min-date
                 :max-date max-date)]
         {:key (str path "-" :adv-date-range)}))
     child]))

(defn month-input [{:keys [class path options on-change on-blur autofocus disabled? child] :as props}]
  (let [ui-selection @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])
        select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])
        last-x-value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/last-x-value path])
        condi (:value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/condition path]))
        is-current? (cond-utils/current-condition? condi)
        is-last-x? (cond-utils/last-x? condi)
        lang @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language])]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [month-condition-select (assoc props :key :cond)]
        [:br]])
     (when (and can-define-values? is-last-x?)
       (with-meta
         [elements/number-input
          {:on-change (fn [e]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :last-x e])
                        (on-change)
                        (on-blur))
           :on-blur #(when last-x-value (on-blur))
           :value last-x-value
           :is-int? true}]
         {:key (str path "-" :adv-last-x-months)}))
     (when (and can-define-values?
                (not is-current?) (not is-last-x?))
       (with-meta
         [elements/selection-dropdown
          {:placeholder select-placeholder
           :autofocus autofocus
           :on-change (fn [selections]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection selections])
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values (mapv (fn [{value :value}]
                                                                                                                                value)
                                                                                                                              selections)])
                        (when on-change
                          (on-change)))
           :on-blur on-blur
           :values (mapv (fn [{:keys [value] :as selected}]
                           (assoc selected :label (i18n/month-name value lang)))
                         ui-selection)
           :options options
           :classname class
           :disabled? disabled?}]
         {:key (str path "-" :ui-selection)}))
     child]))

(defn year-select [{:keys [path on-change on-blur on-blur-range autofocus disabled? child] :as props}] ;path attr options on-change onblurfunc onblurfunc-range]
  (let [select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        value (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])
        last-x-value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/last-x-value path])
        from-options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/options-from path])
        to-options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/options-to path])
        from (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/from path])
        to (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/to path])
        condi (:value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/condition path]))
        range-condi? (cond-utils/range-condition? condi)
        can-define-values? @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/can-define-values? path])
        is-current? (cond-utils/current-condition? condi)
        is-last-x? (cond-utils/last-x? condi)]
    [component-wrapper props
     (when can-define-values?
       [:<>
        [day-year-condition-selet (assoc props
                                         :key :cond
                                         :on-blur (if (cond-utils/range-condition? condi)
                                                    on-blur-range
                                                    on-blur))]
        [:br]])
     (when (and can-define-values? is-last-x?)
       (with-meta
         [elements/number-input
          {:on-change (fn [e]
                        (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :last-x e])
                        (on-change)
                        (on-blur))
           :on-blur #(when last-x-value (on-blur))
           :value last-x-value
           :is-int? true}]
         {:key (str path "-" :adv-last-x-years)}))
     (when (and (not range-condi?) can-define-values? (not is-current?) (not is-last-x?))
       (with-meta
         [elements/one-selection-dropdown
          {:placeholder select-placeholder
           :autofocus   autofocus
           :on-change   (selection-func path :value on-change)
           :on-blur     on-blur
           :values      value
           :options     from-options
           :classname   "input--w100"
           :disabled? disabled?}]
         {:key (str path "-" :value)}))
     (when (and range-condi? can-define-values? (not is-current?) (not is-last-x?))
       [:div.flex.gap-8
        (with-meta
          [elements/one-selection-dropdown
           {:placeholder select-placeholder
            :autofocus   autofocus
            :on-change   (selection-func path :from on-change)
            :on-blur     on-blur-range
            :values      from
            :options     from-options
            :disabled? disabled?}]
          {:key (str path "-" :from)})
        [icon {:icon :minus}]
        (with-meta
          [elements/one-selection-dropdown
           {:placeholder select-placeholder
            :on-change   (selection-func path :to on-change)
            :on-blur     on-blur-range
            :values      to
            :options     to-options
            :disabled? disabled?}]
          {:key (str path "-" :to)})])
     child]))
;; =======================================
