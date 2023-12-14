(ns de.explorama.frontend.search.views.components.simple-mode-components
  (:require [re-frame.core :as re-frame]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.shared.common.unification.time :as t]
            [de.explorama.frontend.search.views.components.elements :as elements]))

(defn advanced-mode-view [advanced-mode-enableable? path disabled?]
  (when advanced-mode-enableable?
    [:span.input__mode {:on-click #(when-not disabled?
                                     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/reset-values-from-attr path])
                                     (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :advanced true]))}
     @(re-frame/subscribe [::i18n/translate :enable-advancedmode-label])]))

(defn selection-func [path key onchangefunc]
  (fn [selection]
    (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path key selection])
    (when onchangefunc
      (onchangefunc))))

(defn component-wrapper [{:keys [path advanced-mode-enableable? disabled?]} & childs]
  (conj (apply conj
               [:<>]
               childs)
        [advanced-mode-view advanced-mode-enableable? path disabled?]))

(defn range-select [{:keys [path on-change on-blur-range autofocus disabled? child] :as props}]
  (let [select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        from-options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/options-from path])
        to-options (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/options-to path])
        from (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/from path])
        to (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/to path])]
    [component-wrapper props
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
        {:key (str path "-" :to)})]
     child]))

(defn range-number-input [{:keys [path on-change on-blur-range is-int? autofocus disabled? child] :as props}]
  (let [from (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/from path])
        to (re-frame/subscribe [:de.explorama.frontend.search.views.formdata/to path])
        change-fn (fn [val save-key]
                    (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path save-key val])
                    (when on-change (on-change "simple-range-number")))]
    [component-wrapper props
     [:div.flex.gap-8
      (with-meta
        [elements/number-input
         {:on-change (fn [new-val]
                       (change-fn new-val :from))
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
                       (change-fn new-val :to))
          :on-blur   on-blur-range
          :classname "input--w4"
          :value     to
          :min       from
          :is-int?   is-int?
          :disabled? disabled?}]
        {:key (str path "-" :to)})]
     child]))

(defn string-selection-input [{:keys [class path options on-change on-blur autofocus disabled? child] :as props}]
  (let [ui-selection @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/ui-selection path])
        lang @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language])
        select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])]
    [component-wrapper props
     (with-meta
       [elements/selection-dropdown
        {:placeholder select-placeholder
         :all-group? true
         :autofocus   autofocus
         :on-change   (fn [selections]
                        (let [selections (elements/reduce-selections selections)]
                          (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :ui-selection selections])
                          (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :values
                                              (mapv :value selections)])
                          (when on-change
                            (on-change))))
         :on-blur     on-blur
         :values      (if (= (last path) ["month" attrs/date-node])
                        (mapv (fn [{:keys [value] :as selected}]
                                (assoc selected :label (i18n/month-name value lang)))
                              ui-selection)
                        ui-selection)
         :options     options
         :classname   class
         :disabled? disabled?}]
       {:key (str path "-" :ui-selection)})
     child]))

(defn simple-input [{:keys [path class autofocus disabled? child] :as props}]
  (let [value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])]
    [component-wrapper props
     (with-meta
       [elements/sinput
        {:autofocus autofocus
         :on-blur      (fn [new-val]
                         (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :value new-val]))
         :classname    class
         :value value
         :disabled? disabled?}]
       {:key (str path "-" :value)})
     child]))

(defn date-range-input [{:keys [path on-change on-blur child] :as props}]
  (let [[start-date end-date] @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/date-range path])
        selected-date @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/selected-date path])
        min-date @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/min-date path])
        max-date @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/max-date path])
        props (assoc props :selected-date selected-date
                     :on-blur #(when (and start-date end-date)
                                 (on-blur)))]
    [component-wrapper props
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
       {:key (str path "-" :date-range)})
     child]))

(defn contains-input [{:keys [path class on-change child autofocus disabled?] :as props}]
  (let [value @(re-frame/subscribe [:de.explorama.frontend.search.views.formdata/value path])]
    [component-wrapper props
     (with-meta
       [elements/sinput
        {:autofocus autofocus
         :on-change    (fn [new-val]
                         (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :value new-val])
                         (when on-change (on-change)))
         :classname    class
         :value value
         :disabled? disabled?}]
       {:key (str path "-" :value)})
     child]))
