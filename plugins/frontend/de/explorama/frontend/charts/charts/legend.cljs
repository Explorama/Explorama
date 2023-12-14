(ns de.explorama.frontend.charts.charts.legend
  (:require [clojure.string :refer [join]]
            [data-format-lib.filter]
            [data-format-lib.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [label tooltip]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.formular.core
             :refer [checkbox input-field select]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.charts.charts.bubble :as bubble]
            [de.explorama.frontend.charts.charts.pie :as pie]
            [de.explorama.frontend.charts.charts.settings :as settings]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]
            [reagent.core :as reagent]))

(defn i18n-sub [i18n-key]
  (re-frame/subscribe [::i18n/translate i18n-key]))

(re-frame/reg-sub
 ::applied-filter
 (fn [db [_ frame-id]]
   (get-in db (path/applied-filter frame-id))))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (get-in db (path/di-desc frame-id))))

(re-frame/reg-sub
 ::frame-datasource
 (fn [db [_ path]]
   (get-in db (path/frame-di path))))

(re-frame/reg-sub
 ::simplified-di-desc
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::frame-datasource frame-id])
    (re-frame/subscribe [::di-desc frame-id])
    (re-frame/subscribe [::applied-filter frame-id])])
 (fn [[di-filter {:keys [filtered-data-info] :as di-desc} local-filter] _]
   (let [base (if (and local-filter (seq filtered-data-info))
                filtered-data-info
                di-desc)]


     {:base base
      :local-filter? (boolean local-filter)
      :additional (dflsv/simplified-filter di-filter local-filter)})))

(re-frame/reg-sub
 ::data-display-count
 (fn [db [_ frame-id]]
   (let [{:keys [global local]} (get-in db (conj (path/frame-filter frame-id) :counts))]
     {:all-data global
      :local-data local})))

(defn- y-range-settings [frame-id chart-index]
  (let [current-min-val (re-frame/subscribe [::settings/changed-min-y frame-id chart-index])
        current-max-val (re-frame/subscribe [::settings/changed-max-y frame-id chart-index])
        min-val (re-frame/subscribe [::settings/original-min-y frame-id chart-index])
        max-val (re-frame/subscribe [::settings/original-max-y frame-id chart-index])
        thousand-sep @(re-frame/subscribe [::i18n/translate :thousand-separator])
        decimal-sep @(re-frame/subscribe [::i18n/translate :decimal-separator])
        y-range-label @(re-frame/subscribe [::i18n/translate :y-range])
        lang @(re-frame/subscribe [::i18n/current-language])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})
        data-loading? (boolean @(re-frame/subscribe [::queue-util/loading? frame-id]))
        is-int? true #_(.isInteger @min-val)
        on-blur-fn (fn [_]
                     (re-frame/dispatch [::settings/y-range-request-dataset frame-id]))]
    [:div.input.input--w100
     [label {:label y-range-label}]
     [:div {:class ["flex" "gap-8" "max-w-full"]}
      [input-field {:prevent-dragging? true
                    :extra-class "input--w5"
                    :default-value min-val
                    :disabled? (or read-only? data-loading?)
                    :type :number
                    :thousand-separator thousand-sep
                    :decimal-separator decimal-sep
                    :language lang
                    :on-change (fn [val valid?]
                                 (re-frame/dispatch [::settings/change-min-y
                                                     frame-id
                                                     chart-index
                                                     val
                                                     valid?]))
                    :on-blur on-blur-fn
                    :step (if is-int?
                            1
                            "any")
                    :max-length 15
                    :max current-max-val
                    :value current-min-val}]
      [icon {:icon :minus}]
      [input-field {:prevent-dragging? true
                    :extra-class "input--w5"
                    :default-value max-val
                    :max-length 15
                    :min current-min-val
                    :disabled? (or read-only? data-loading?)
                    :type :number
                    :thousand-separator thousand-sep
                    :decimal-separator decimal-sep
                    :language lang

                    :on-change (fn [val valid?]
                                 (re-frame/dispatch [::settings/change-max-y
                                                     frame-id
                                                     chart-index
                                                     val
                                                     valid?]))
                    :on-blur on-blur-fn
                    :step (if is-int?
                            1
                            "any")
                    :value current-max-val}]]
     [:span.input__mode {:style {:display "block"
                                 :padding "4px 12px 0 0"
                                 :white-space "nowrap"
                                 :color "#868e96"
                                 :line-height "25px"
                                 :width "fit-content"}
                         :on-click #(re-frame/dispatch [::settings/y-range-change
                                                        frame-id
                                                        chart-index
                                                        false])}
      @(re-frame/subscribe [::i18n/translate :y-axis-change-range-default-label])]]))

(defn- y-settings [frame-id chart-index]
  (let [change-range? (re-frame/subscribe [::settings/y-range-change? frame-id chart-index])
        data-loading? (boolean @(re-frame/subscribe [::queue-util/loading? frame-id]))
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})
        select-placeholder-prefix @(i18n-sub :select-placeholder-prefix)
        y-axis-attribute-label @(i18n-sub :y-axis-attribute-label)
        select-placeholder-yaxis (str select-placeholder-prefix " " y-axis-attribute-label)
        y-options (re-frame/subscribe [::settings/y-options frame-id])
        y-value (re-frame/subscribe [::settings/y-option frame-id chart-index])]
    [:<>
     [select {:name (str "chart-y-attr_" frame-id)
              :placeholder select-placeholder-yaxis
              :label y-axis-attribute-label
              :parent-extra-class "explorama__form--flex"
              :extra-class "input--w100"
              :disabled? (or read-only? data-loading?)
              :is-multi? false
              :is-grouped? true
              :on-change #(re-frame/dispatch [::settings/change-y-option frame-id chart-index %])
              :options y-options
              :values y-value
              :is-clearable? false}]
     (if-not @change-range?
       [:span.input__mode {:on-click #(re-frame/dispatch [::settings/y-range-change
                                                          frame-id
                                                          chart-index
                                                          true])}
        @(re-frame/subscribe [::i18n/translate :y-axis-change-range-label])]
       [y-range-settings frame-id chart-index change-range?])]))

(defn- sum-by-val-render [chart-colors {:keys [label]}]
  [:div.flex.align-items-center.gap-6
   [:div {:style {:min-width "0.875rem"}}
    [icon {:icon :mosaic-circle :custom-color (get chart-colors label "#e7e8ea")}]]
   [:div.truncate-text label]])

(defn- chart-input-impl [frame-id active-chart-id chart-index {:keys [attribute? size?]}]
  (let [x-options (re-frame/subscribe [::settings/x-options frame-id])
        x-value (re-frame/subscribe [::settings/x-option frame-id])
        y-options (re-frame/subscribe [::settings/y-options frame-id])
        y-value (re-frame/subscribe [::settings/y-option frame-id chart-index])
        aggregate-methods (re-frame/subscribe [::settings/aggregate-methods frame-id])
        aggregate-method (re-frame/subscribe [::settings/aggregate-method frame-id chart-index])
        x-axis-attribute-label @(i18n-sub :x-axis-attribute-label)
        chart-attr-label @(i18n-sub :chart-attr-label)
        select-placeholder-prefix @(i18n-sub :select-placeholder-prefix)
        select-placeholder-xaxis (str select-placeholder-prefix " " x-axis-attribute-label)
        select-placeholder-attr (str select-placeholder-prefix " " chart-attr-label)
        sum-by-options (re-frame/subscribe [::settings/sum-by-options frame-id])
        sum-by-value (re-frame/subscribe [::settings/sum-by-option frame-id chart-index])
        sum-by-vals-options (re-frame/subscribe [::settings/sum-by-characteristics frame-id chart-index])
        sum-by-values @(re-frame/subscribe [::settings/sum-by-values frame-id chart-index])
        sum-by-label @(i18n-sub :sum-by-label)
        sum-by-vals-label @(i18n-sub :sum-by-vals-label)
        sum-remaining-value (re-frame/subscribe [::settings/sum-remaining frame-id chart-index])
        sum-remaining-label @(i18n-sub :sum-remaining-label)
        r-value (re-frame/subscribe [::settings/r-option frame-id chart-index])
        r-attribute-label @(i18n-sub :r-attribute-label)
        metrics [{:label @(i18n-sub :same-position-metric-label)
                  :value :same-position-metric-label}]
        r-options y-options ;;(atom (into metrics (charts.charts.settings/y-options frame-id)))
        select-placeholder-prefix @(i18n-sub :select-placeholder-prefix)
        select-placeholder-r (str select-placeholder-prefix " " r-attribute-label)
        select-by-info @(i18n-sub :select-by-info)
        characteristics-info @(i18n-sub :characteristics-info)
        max-selected-characters @(i18n-sub :max-selected-characters)
        select-placeholder-prefix @(i18n-sub :select-placeholder-prefix)
        aggregation-method-label @(i18n-sub :aggregation-method-label)
        no-options-label @(i18n-sub :no-options)
        select-placeholder-sumby (str select-placeholder-prefix " " sum-by-label)
        select-placeholder-sumby-vals (str select-placeholder-prefix " " sum-by-vals-label)
        data-loading? (boolean @(re-frame/subscribe [::queue-util/loading? frame-id]))
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id})
        chart-colors (->> (cond-> @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                            (not= active-chart-id path/pie-id-key) (get-in [chart-index :datasets])
                            (= active-chart-id path/pie-id-key) (-> (get-in [chart-index :datasets 0 :legend])
                                                                    (->> (mapv (fn [entry]
                                                                                 (assoc entry
                                                                                        :chartIndex chart-index
                                                                                        :legend entry))))))
                          (reduce (fn [acc {:keys [legend label chartIndex]}]
                                    (cond-> acc
                                      (= chart-index chartIndex)
                                      (assoc label (:color legend))))
                                  {}))]
    (cond-> [:<>]
      attribute?
      (conj [select {:name (str "chart-attr_" frame-id)
                     :placeholder select-placeholder-attr
                     :label chart-attr-label
                     :parent-extra-class "explorama__form--flex"
                     :extra-class "input--w100"
                     :disabled? (or read-only? data-loading?)
                     :is-multi? false
                     :is-grouped? true
                     :on-change #(re-frame/dispatch [::settings/change-y-option frame-id chart-index %])
                     :options y-options
                     :values y-value
                     :is-clearable? false}])
      (not attribute?)
      (conj [select {:name (str "chart-x-attr_" frame-id)
                     :placeholder select-placeholder-xaxis
                     :label x-axis-attribute-label
                     :parent-extra-class "explorama__form--flex"
                     :extra-class "input--w100"
                     :disabled? (or read-only? data-loading?)
                     :is-multi? false
                     :on-change #(re-frame/dispatch [::settings/change-x-option frame-id %])
                     :options x-options
                     :values x-value
                     :is-clearable? false}]
            [y-settings frame-id chart-index])
      (not (keyword? (:value @y-value)))
      (conj [select {:name (str "chart-agg-method_" frame-id)
                     :label aggregation-method-label
                     :parent-extra-class "explorama__form--flex"
                     :extra-class "input--w100"
                     :disabled? (or read-only? data-loading?)
                     :is-multi? false
                     :is-clearable? false
                     :on-change #(re-frame/dispatch [::settings/change-aggregate-method frame-id chart-index %])
                     :options aggregate-methods
                     :values aggregate-method}])
      :always
      (conj
       [:div.explorama__form__select.explorama__form--flex
        [label
         {:label
          [:<>
           sum-by-label
           [tooltip {:text select-by-info}
            [:div.form__field__info
             [:span]]]]}]
        [select {:name (str "chart-sumby_" frame-id)
                  ; :label sum-by-label
                  ;  :hint select-by-info
                 :parent-extra-class "explorama__form--flex"
                 :placeholder select-placeholder-sumby
                 :extra-class "input--w100"
                 :disabled? (or read-only? data-loading?)
                 :is-multi? false
                 :on-change #(re-frame/dispatch [::settings/change-sum-by-option frame-id chart-index %])
                 :options sum-by-options
                 :values sum-by-value
                 :is-clearable? false}]])
      (not= "all" (get @sum-by-value :value))
      (conj
       [label
        {:label
         [:<>
          sum-by-vals-label
          [tooltip {:text characteristics-info
                    :extra-style {:display :inline-block ;;TODO r1/css
                                  :top "8px"}}
           [:div.form__field__info
            [:span]]]]}]
       [select {:name (str "chart-sumby-vals_" frame-id)
                ;:label sum-by-vals-label
               ; :hint characteristics-info
                :parent-extra-class "explorama__form--flex"
                :placeholder select-placeholder-sumby-vals
                :no-options-placeholder (if (empty? @sum-by-vals-options)
                                          max-selected-characters
                                          no-options-label)
                :value-render-fn (partial sum-by-val-render chart-colors)
                :disabled? (or read-only?
                               data-loading?
                               (and (seq @sum-by-value)
                                    (= (:values-sub @sum-by-value)
                                       "all")))
                :is-multi? true
                :close-on-select? false
                :extra-class "input--w100"
                :on-change #(re-frame/dispatch [::settings/change-sum-by-values frame-id chart-index %])
                :options sum-by-vals-options
                :values sum-by-values}])
      (and (not= "all" (get @sum-by-value :value))
           attribute?)
      (conj [checkbox {:label sum-remaining-label
                       :checked? sum-remaining-value
                       :disabled? (or read-only? data-loading?)
                       :on-change #(re-frame/dispatch [::settings/change-sum-remaining frame-id chart-index %])}])
      size?
      (conj [select {:name (str "chart-r-attr_" frame-id)
                     :placeholder select-placeholder-r
                     :label r-attribute-label
                     :parent-extra-class "explorama__form--flex"
                     :extra-class "input--w100"
                     :disabled? (or read-only? data-loading?)
                     :is-multi? false
                     :is-clearable? false
                     :is-grouped? true
                     :on-change #(re-frame/dispatch [::settings/change-r-option frame-id chart-index %])
                     :options r-options
                     :values r-value}]))))

(defn- chart-input [frame-id active-chart-id chart-index {:keys [attribute? size? on-normalize]}]
  (reagent/create-class
   {:display-name (str "charts input" frame-id)
    :component-did-update (fn [this argv]
                            (let [[_ _ _ _ {old-active-charts :active-charts}] argv
                                  [_ _ _ _ {new-active-charts :active-charts}] (reagent/argv this)]
                              (when (and
                                     (fn? on-normalize)
                                     (> old-active-charts new-active-charts))
                                (on-normalize))))
    :reagent-render chart-input-impl}))

(defn- get-legend [datasets]
  (into []
        (mapcat (fn [ds]
                  (let [defaults (select-keys ds [:label])
                        legend (:legend ds)]
                    (if (map? legend)
                      [(merge defaults legend)]
                      (map (partial merge defaults) legend)))))
        datasets))

(re-frame/reg-sub
 ::legend-description
 (fn [[_ frame-id]]
   (re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id]))
 (fn [datasets _]
   (get-legend (:datasets datasets))))

(defn- chart-desc->option [active-charts disabled-hint-text charts-desc]
  (let [is-multi-possible? (get charts-desc path/chart-desc-multiple-key)
        chart-icon (get charts-desc path/chart-desc-icon-key)
        option-disabled? (and (> active-charts 1)
                              (not is-multi-possible?))
        chart-label @(re-frame/subscribe [::i18n/translate (get charts-desc
                                                                path/chart-desc-label-key)])]
    {:value (get charts-desc path/chart-desc-id-key)
     :tooltip chart-label
     :label [:div {:class ["flex"
                           "align-items-center"
                           "gap-6"]}
             [icon {:icon chart-icon}]
             chart-label]
     :disabled-hint disabled-hint-text
     :disabled? option-disabled?}))

(defn- chart-legend [frame-id {:keys [maximized-data on-normalize]}]
  (let [{:keys [chart-index]} (val-or-deref maximized-data)
        active-charts @(re-frame/subscribe [::settings/num-of-charts frame-id])
        {disabled-hint-text :chart-disabled-hint
         chart-type-label :chart-type-label}
        @(re-frame/subscribe [::i18n/translate-multi
                              :chart-disabled-hint
                              :chart-add-button
                              :chart-remove-button
                              :chart-type-label])
        charts-options (mapv (fn [[_ charts-desc]]
                               (chart-desc->option active-charts disabled-hint-text charts-desc))
                             settings/possible-charts)]

    [:div.section__content
     [:div.panel__subsection>div.subsection__content>div.subsection__control
      (let [{active-chart-id path/chart-desc-id-key
             active-chart-settings path/chart-desc-settings-key
             :as chart-desc}
            @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/chart-type
                                  frame-id chart-index])
            selected-option (chart-desc->option active-charts disabled-hint-text chart-desc)]
        [:<>
         [select {:label chart-type-label
                  :options charts-options
                  :is-clearable? false
                  :values selected-option
                  :tooltip-key :tooltip
                  :on-change (fn [{chart-desc-id :value}]
                               (re-frame/dispatch [:de.explorama.frontend.charts.charts.core/change-chart-type
                                                   frame-id
                                                   chart-index
                                                   (get settings/possible-charts
                                                        chart-desc-id)]))}]
         (if active-chart-settings
           [active-chart-settings frame-id]
           [chart-input frame-id active-chart-id chart-index
            {:attribute? (= active-chart-id
                            (path/chart-desc-id-key pie/chart-desc))
             :size? (= active-chart-id
                       (path/chart-desc-id-key bubble/chart-desc))
             :on-normalize on-normalize
             :active-charts active-charts}])])]]))



(defn edit-chart [frame-id {:keys [on-normalize maximized-data] :as props}]
  (let [{:keys [chart-index]} (val-or-deref maximized-data)
        chart-component-label @(re-frame/subscribe [::i18n/translate :chart-component-label])]
    [:div.panel__section
     [:div.panel__header
      [button {:on-click #(on-normalize)
               :label @(re-frame/subscribe [::i18n/translate :back-label])
               :start-icon :previous
               :size :big
               :variant :back}]]
     [:div.section__title
      (str chart-component-label " " (inc chart-index))]
     [:div.panel__scroll__container
      [chart-legend frame-id props]]]))

(defn- desc-elem [show? label {val :label :as selection}]
  (let [multi-vals? (and (not val)
                         (coll? selection))]
    (when (and show? label (or val multi-vals?))
      [:<>
       [:li
        [:span.legend__label {:style {:min-width "100px"
                                      :font-size :small}}
         label]
        (when val
          [:span {:style {:font-size :small
                          :text-overflow :ellipsis
                          :overflow :hidden
                          :white-space :nowrap}}
           val])]
       (when multi-vals?
         [:li
          [:span {:style {:margin-left "10px"
                          :font-size :x-small}}
           (join ", " (map :label selection))]])])))

(defn- color-code-list [frame-id chart-index chart-colors]
  (reduce (fn [acc {:keys [color label type shape on-toggle visible?]}]
            (cond-> acc
              (and label color)
              (conj
               [:li {:on-click on-toggle
                     :style (cond-> {:cursor :pointer}
                              (not visible?)
                              (assoc :opacity 0.4))}
                [:span.legend__color {:style {:background-color color}}]
                [tooltip {:text label
                          :direction :right
                          :tag-name "span"
                          :extra-class "legend__value"}
                 label]])))
          [:ul]
          chart-colors))

(defn- coloring [show? frame-id chart-index chart-colors]
  (let [color-label @(re-frame/subscribe [::i18n/translate :chart-colors])]
    (when (and show? (seq chart-colors))
      [:<>
       [:span.legend__label color-label]
        ;; [:dd.truncate-text (join ", " (map #(attr->display-name % labels) attributes))]
       [color-code-list frame-id chart-index chart-colors]])))

(defn- chart-desc [frame-id chart-index {:keys [on-maximize show-actions?]}]
  (let [{chart-type path/chart-desc-label-key
         id path/chart-desc-id-key
         chart-icon path/chart-desc-icon-key}
        @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/chart-type frame-id chart-index])
        read-only? @(fi/call-api [:interaction-mode :pending-read-only-sub?])
        hidden-datasets @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/hidden-datasets frame-id])
        visible-fn (fn [idx label]
                     (boolean (not (hidden-datasets {:idx idx
                                                     :label label}))))
        sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)
        on-color-click (fn [idx label]
                         (when-not read-only?
                           (let [action (if (visible-fn idx label)
                                          :add
                                          :rm)]
                             (sync-event-fn [:de.explorama.frontend.charts.charts.settings/change-hidden-datasets frame-id id idx label action])
                             (re-frame/dispatch [:de.explorama.frontend.charts.charts.settings/change-hidden-datasets frame-id id idx label action]))))
        start-idx (if (= 0 chart-index)
                    0
                    (-> @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                        (get-in [0 :datasets])
                        (count)))
        chart-colors (->> (cond-> @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                            (not= id path/pie-id-key) (get-in [chart-index :datasets])
                            (= id path/pie-id-key) (-> (get-in [chart-index :datasets 0 :legend])
                                                       (->> (mapv (fn [entry]
                                                                    (js/console.info entry)
                                                                    (assoc entry
                                                                           :chartIndex chart-index
                                                                           :legend entry))))))
                          (map-indexed vector)
                          (reduce (fn [acc [idx {:keys [legend type label chartIndex] :as a}]]
                                    (let [idx (+ start-idx idx)]
                                      (cond-> acc
                                        (= chart-index chartIndex)
                                        (conj {:label label
                                               :idx idx
                                               :visible? (visible-fn idx label)
                                               :on-toggle (partial on-color-click idx label)
                                               :type type
                                               :color (:color legend)
                                               :shape (:shape :line)}))))
                                  []))
        x @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/x-option frame-id])
        y @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/y-option frame-id chart-index])
        r @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/r-option frame-id chart-index])
        aggr @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/aggregate-method frame-id chart-index])
        {sum-by-v :value :as sum-by} @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/sum-by-option frame-id chart-index])
        sum-by-values @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/sum-by-values frame-id chart-index])
        show-x? (#{:line :bar :bubble :scatter} id)
        show-y? (#{:line :bar :bubble :scatter} id)
        show-size-attr? (#{:bubble} id)
        show-attr? (#{:pie} id)
        show-aggregation? (and (#{:line :bar :bubble :scatter :pie} id)
                               (not= (:value y) :number-of-events))
        show-show-by? (#{:line :bar :bubble :scatter :pie} id)
        show-show-by-options? (and (not= sum-by-v "all")
                                   (#{:line :bar :bubble :scatter :pie} id))
        show-coloring? true
      ;;  sum-remaining-label @(i18n-sub :sum-remaining-label)
        {:keys [chart-component-label x-axis-attribute-label y-axis-attribute-label r-attribute-label
                aggregation-method-label sum-by-label sum-by-vals-label chart-attr-label]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :chart-component-label
                              :x-axis-attribute-label :y-axis-attribute-label
                              :r-attribute-label :aggregation-method-label
                              :sum-by-label :sum-by-vals-label :chart-attr-label])]
    [:div.panel__subsection.draggable-content>div.subsection__content>div.subsection__element>div.draggable__content
     [:div.subsection__element__title
      [:div.truncate-text
       (str chart-component-label " " (inc chart-index))]
      (when (val-or-deref show-actions?)
        [:div.flex {:style {:position :absolute
                            :right "30px"}}
         [button {:start-icon :edit
                  :label (re-frame/subscribe [::i18n/translate :edit-label])
                  :aria-label :edit-label
                  :variant :tertiary
                  :size :small
                  :on-click #(on-maximize {:chart-index chart-index})
                  :extra-class export-ignore-class
                  :disabled?  read-only?}]])
      [icon {:icon chart-icon
             :color :gray}]]
     [:ul
      [desc-elem show-x? x-axis-attribute-label x]
      [desc-elem show-y? y-axis-attribute-label y]
      [desc-elem show-attr? chart-attr-label y]
      [desc-elem show-size-attr? r-attribute-label r]
      [desc-elem show-aggregation? aggregation-method-label aggr]
      [desc-elem show-show-by? sum-by-label sum-by]
      ;; [protoype-legend-elem show-show-by-options? sum-by-vals-label sum-by-values]
      [coloring show-coloring? frame-id chart-index chart-colors]]]))

(defn- charts-configuration [{:keys [frame-id is-maximized?] :as props}]
  (if-let [is-maximized? (val-or-deref is-maximized?)]
    [edit-chart frame-id props]
    (let [active-charts @(re-frame/subscribe [::settings/num-of-charts frame-id])]
      (reduce (fn [acc idx]
                (conj acc [chart-desc frame-id idx props]))
              [:<>]
              (range active-charts)))))

(def legend-impl
  {:visible? true
   :disabled? (fn [frame-id]
                false)

   :data-display-count
   (fn [frame-id]
     (re-frame/subscribe [::data-display-count frame-id]))
   :di-desc-sub
   (fn [frame-id]
     (re-frame/subscribe [::simplified-di-desc frame-id]))
   :configuration [{:module charts-configuration}]})

