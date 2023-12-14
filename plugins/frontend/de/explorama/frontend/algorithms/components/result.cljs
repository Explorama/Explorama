(ns de.explorama.frontend.algorithms.components.result
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon traffic-light]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [section]]
            [de.explorama.frontend.algorithms.components.subsection :as sub]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [reagent.core :as reagent]
            [de.explorama.frontend.algorithms.components.charts :as charts]
            [clojure.string :as string]
            [de.explorama.frontend.common.i18n :as i18n]))

(defn find-first-leading-variable [attributes]
  (let [date-attribute
        (first (filter (fn [{type :type}]
                         (= type :date))
                       attributes))
        number-continues-attribute
        (first (filter (fn [{type :type continues :continues-value leading :given?}]
                         (and (= type :numeric)
                              continues
                              leading))
                       attributes))
        number-all-attribute
        (first (filter (fn [{type :type leading :given?}]
                         (and (= type :numeric)
                              leading))
                       attributes))
        categoric-attribute
        (first (filter (fn [{type :type leading :given?}]
                         (and (= type :categoric)
                              leading))
                       attributes))]
    (cond date-attribute
          [(:value date-attribute) :date]
          number-continues-attribute
          [(:value number-continues-attribute) :numeric]
          number-all-attribute
          [(:value number-all-attribute) :numeric]
          categoric-attribute
          [(:value categoric-attribute) :categoric]
          :else [(->> attributes
                      first
                      :value)
                 :categoric])))

(defn find-first-not-leading-variable [attributes]
  (let [attribute
        (first (filter (fn [{given? :given?}]
                         (not given?))
                       attributes))]
    (:value attribute)))


(defn time-attr? [attr]
  (#{"year" "quarter" "month" "day"} attr))

(defn x-axis-time [x-axis]
  (when-not (nil? x-axis)
    (let [x-axis (name x-axis)]
      (when (time-attr? x-axis)
        {:parser "yyyy-MM-dd"
         :unit x-axis
         :displayFormats {:year "yyyy"
                          :month "yyyy-MM"
                          :quarter "yyyy-MM"
                          :day "yyyy-MM-dd"}}))))

(defn prediction-data->chart-data [procedures algorithm attributes prediction-data language-fuction
                                   prediction-input-data backdated-forecast-data translate-function
                                   attribute-labels]
  (case (get-in procedures [algorithm :result-type])
    :line-chart
    (let [attributes (flatten (map val attributes))
          [x attr-type] (find-first-leading-variable attributes)
          y (find-first-not-leading-variable attributes)
          y (if (= :number-of-events y)
              "number-of-events"
              y)
          x-axis-format (get-in (last attributes) [:date-config  :granularity])
          datasets (charts/line-chart-data-set x y (translate-function :forecast) prediction-data)
          input-datasets (charts/line-chart-data-set x y (translate-function :input-data-section) prediction-input-data)
          backdated-datasets (charts/line-chart-data-set x y (translate-function :retrospective-forecast) backdated-forecast-data)]
      (when (or (seq prediction-data)
                (seq prediction-input-data)
                (seq backdated-forecast-data))
        (charts/line-chart-config datasets
                                  (charts/axes-number (get attribute-labels y y) language-fuction)
                                  (charts/axes-description (get attribute-labels x x) (charts/labels (into prediction-data prediction-input-data) x) (x-axis-time x-axis-format) attr-type)
                                  language-fuction
                                  input-datasets
                                  backdated-datasets)))
    :pie-chart
    (charts/pie-chart-config prediction-data "cluster")
    nil))

(defn- get-parameter-strings [idx parameter prediction-id translate-function attribute-labels]
  (map (fn [[k value]]
         (when (and value
                    (or (and (string? value)
                             (not-empty value))
                        (not (string? value))))
           (str (name (translate-function k)) ": "
                (cond (number? value)
                      value
                      (vector? value)
                      (->> value
                           (mapv #(get attribute-labels % %))
                           (string/join ", "))
                      :else
                      (translate-function value)))))
       parameter))

(defn- prediction-info [frame-id
                        idx
                        {:keys [parameter prediction-id prediction-function]
                         {dependent-variables :dependent-variable ;FIXME for multiple later
                          independent-variables :independent-variable} :attributes}
                        translate-function
                        attribute-labels]
  (let [key-func #(str "prediction-info-" % "-" prediction-id "-" idx "-" (:frame-id frame-id))
        dependent-variables (map :value dependent-variables)
        independent-variables (map :value independent-variables)
        display-function (reduce
                          (fn [res var]
                            (when res
                              (string/replace res
                                              (re-pattern (str "(^|[^a-zA-z])(" var ")([^a-zA-Z]|$)"))
                                              (str "$1" (get attribute-labels var var) "$3"))))
                          prediction-function
                          (concat dependent-variables independent-variables))]
    [:<>
     [:div.title
      (translate-function :additional-infos)]
     [:div.info__panel
      (into
       [:dl]
       (map
        (fn [[lang-key values]]
          (when values
            (into
             ^{:key (key-func (name lang-key))}
             [:<>
              [:dt (translate-function lang-key)]]
             (for [val values]
               [:dd {:key (key-func val)} (get attribute-labels val val)]))))
        [[:dependent-variables dependent-variables]
         [:independent-variables independent-variables]
         (when (not-empty parameter) [:parameter (get-parameter-strings idx parameter prediction-id translate-function attribute-labels)])
         (when prediction-function [:function [display-function]])]))]]))

(defn- prediction-statistics-table [frame-id idx {:keys [prediction-statistics prediction-id]} translate-function]
  (let [sorted-stats (vec (sort-by :name prediction-statistics))

        elements-per-row (/ (count sorted-stats) 2)
        key-func #(str "prediction-statistics-table-" % "-" prediction-id "-" idx "-" (:frame-id frame-id))]
    [:<>
     [:div.title
      (translate-function :model-metrics)]
     [:table
      (into [:tbody]
            (map (fn [left-stat right-stat]
                   (into [:tr {:key (key-func (str "tr-" (:name left-stat) "-" (:name right-stat)))}]
                         (map (fn [stat]
                                ^{:key (key-func (:name stat))}
                                (if (= stat {})
                                  [:<>
                                   [:td]
                                   [:td]]
                                  [:<>
                                   [:td
                                    (if (:header stat)
                                      {:class "evaluation__determinant"}
                                      {})
                                    (:name stat)]
                                   [:td
                                    [traffic-light {:color (:light stat)
                                                    :label (i18n/localized-number (:value stat))}]]]))
                              [left-stat right-stat])))
                 (take (Math/ceil elements-per-row) sorted-stats)
                 (cond-> (drop (Math/ceil elements-per-row) sorted-stats)
                   (< 0 (mod elements-per-row 1))
                   (conj {}))))]]))

(defn- pred-header [frame-id idx prediction translate-function attribute-labels]
  (fn [frame-id idx {:keys [country algorithm prediction-statistics header] :as prediction} translate-function]
    (let [name (str (translate-function algorithm) (when (not= country :ignore) (str " - " country)))]
      [section {:default-open? false
                :label
                [:div.flex.align-items-center
                 name
                 [traffic-light {:color (:light header)
                                 :hint-text (translate-function (:hint header))}]]}
       [:<>
        (when (not-empty prediction-statistics)
          [prediction-statistics-table frame-id idx prediction translate-function])
        [prediction-info frame-id idx prediction translate-function attribute-labels]]])))

(defn warning-error-view [translate-function icon-props warning-class label msg]
  [:div
   {:class warning-class}
   [:div.hint__icon
    [icon icon-props]]
   [:div.hint__body
    [:div.hint__title (translate-function label)]
    [:div.hint__message (translate-function msg)]]])

(defn text-result-view [translate-function test-icon msg]
  [:div
   {:class "result__hint"
    :style {:background-color "#FFFFFF"}}
   [:div.hint__icon
    [icon {:icon test-icon}]]
   [:div.hint__body
    [:div.hint__message (translate-function msg)]]])

(defn- pred-result [frame-id
                    idx
                    {:keys [prediction-data
                            prediction-id
                            prediction-warning
                            prediction-error
                            test-measure
                            algorithm
                            prediction-input-data
                            backdated-forecast-data]
                     attributes :attributes
                     :as prediction}
                    translate-function
                    language-function
                    procedures
                    attribute-labels]
  (let [chart-id (str "pred-result-chart-" idx "-" (:frame-id frame-id) "-" prediction-id)
        [test-icon test-result-text] test-measure]
    [:<>
     [pred-header frame-id idx prediction translate-function attribute-labels]
     (when prediction-warning
       (if (keyword? prediction-warning)
         [warning-error-view translate-function {:icon :warning} "result__hint--warning" :prediction-warning-label prediction-warning]
         (into [:<>]
               (for [prediction-warning prediction-warning]
                 [warning-error-view translate-function {:icon :warning} "result__hint--warning" :prediction-warning-label prediction-warning]))))
     (when prediction-error
       (if (keyword? prediction-error)
         [warning-error-view
          translate-function
          {:icon :error}
          "result__hint--error"
          :prediction-error-label
          prediction-error]
         (into [:<>]
               (for [prediction-error prediction-error]
                 [warning-error-view
                  translate-function
                  {:icon :error}
                  "result__hint--error"
                  :prediction-error-label prediction-error]))))
     (when-not prediction-error
       (if (= :text (get-in @procedures [algorithm :result-type]))
         [text-result-view translate-function test-icon test-result-text]
         [charts/view frame-id chart-id
          (prediction-data->chart-data @procedures algorithm attributes prediction-data language-function
                                       prediction-input-data backdated-forecast-data translate-function attribute-labels)
          nil]))]))

(defn- determine-header-keys
  "Defines the header based on the worst individually graded parameter with 'header = true'. The rating is used for sorting and is based on R2."
  [predictions]
  (for [prediction predictions]
    (let [all-header-lights (map :light (filter :header (:prediction-statistics prediction)))
          rating (:value (first (filter #(= "R2" (:name %)) (:prediction-statistics prediction))))
          [color-class light-key hint-key]
          (condp some all-header-lights
            #{:red}    ["result__bad" :red :traffic-light-red]
            #{:yellow} ["result__medium" :yellow :traffic-light-yellow]
            #{:green}  ["result__good" :green :traffic-light-green]
            ["result__unknown" :grey :traffic-light-none])]
      (assoc prediction :header {:color color-class :light light-key :hint hint-key :rating rating}))))

(defn view [frame-id procedures result state translate-function language-function {:keys [publish]}]
  (let [{prediction-error :error prediction-warning :warning
         predictions :predictions} @result
        attribute-labels @(fi/call-api [:i18n :get-labels-sub])
        prediction-groups (group-by #(get-in % [:header :light]) (determine-header-keys predictions))
        sorted-predictions (-> []
                               (into (sort-by #(get-in % [:header :rating]) > (:green prediction-groups)))
                               (into (sort-by #(get-in % [:header :rating]) > (:yellow prediction-groups)))
                               (into (sort-by #(get-in % [:header :rating]) > (:red prediction-groups)))
                               (into (:grey prediction-groups)))
        {:keys [publish?]} @state]
    (when (not-empty sorted-predictions)
      [:<>
       [section {:default-open? true
                 :label (translate-function :prediction-result-section)}
        [:div
         (when prediction-error
           [sub/section {:label (translate-function :error-section)}
            [:p (translate-function prediction-error)]
            (when (instance? js/Error prediction-error)
              [:pre (.-stack prediction-error)])])
         (map-indexed (fn [idx {pred-id :prediction-id
                                :as pred}]
                        (with-meta [pred-result frame-id idx pred translate-function language-function procedures attribute-labels]
                          {:key (str pred-id "-" idx)}))
                      sorted-predictions)
         #_[button {:label (translate-function :prediction-publish)
                    :on-click #(if (not-empty prediction-name)
                                 (publish prediction-name)
                                 (swap! state assoc :publish? true))}]
         #_(when publish?
             [input-field {:value prediction-name
                           :on-change (fn [val]
                                        (swap! state assoc :prediction-name val))
                           :on-blur (fn []
                                      (swap! state assoc :publish? false)
                                      (when (not-empty prediction-name)
                                        (publish prediction-name)))
                           :on-key-down (fn [ev]
                                          (when (= (.-keyCode ev) 13) ; Enter
                                            (swap! state assoc :publish? false)
                                            (when (not-empty prediction-name)
                                              (publish prediction-name)))
                                          (when (= (.-keyCode ev) 27) ; Escape
                                            (reset! state {:prediction-name ""
                                                           :publish? false})))}])]]])))

(defn react-view [frame-id procedures result prediction translate-function language-function functions]
  (let [{{:keys [prediction-name]
          :or {prediction-name ""}} :prediction} @prediction
        state (reagent/atom {:prediction-name prediction-name
                             :save? false
                             :publish? false})]
    [view frame-id procedures result state translate-function language-function functions]))
