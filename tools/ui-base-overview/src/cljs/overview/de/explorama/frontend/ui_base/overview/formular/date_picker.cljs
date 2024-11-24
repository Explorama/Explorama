(ns de.explorama.frontend.ui-base.overview.formular.date-picker
  (:require [de.explorama.frontend.ui-base.components.formular.date-picker :refer [date-picker default-parameters parameter-definition]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Date-Picker"
   :desc "Standard formular component for selecting dates"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [date-picker]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  (let [value (reagent/atom (js/Date.))]
    [date-picker {:value value
                  :on-change (fn [new-value]
                               (js/console.log "Date obj:" new-value)
                               (reset! value new-value))}])
  {:title "On-change"})

(defexample
  (let [value (reagent/atom (js/Date.))]
    [date-picker {:value value
                  :selected-as-string? true
                  :on-change (fn [new-value]
                               (js/console.log "Date string:" new-value)
                               (reset! value new-value))}])
  {:title "Selected as string"})

(defexample
  [date-picker {:disabled? true}]
  {:title "Disabled"})

(defexample
  (let [value (reagent/atom [(js/Date.) (js/Date.)])]
    [date-picker {:value value
                  :range? true
                  :on-change (fn [new-value]
                               (reset! value new-value))}])
  {:title "Range-Select"})

(defexample
  [date-picker {:range? true
                :extra-class "input--w5"}]
  {:title "Range-Select with specific width"})

(defexample
  [date-picker {:possible-dates [(js/Date.)]}]
  {:title "Filter dates"})

(defexample
  [date-picker {:on-filter (fn [date]
                             (not= (.toDateString date)
                                   (.toDateString (js/Date.))))}]
  {:title "Filter dates custom"})

(defexample
  (let [today (js/Date.)
        min-date (js/Date.)
        max-date (js/Date.)]
    (.setDate min-date (- (.getDate today)
                          2))
    (.setDate max-date (+ (.getDate today)
                          2))
    [date-picker {:min-date min-date
                  :max-date max-date}])
  {:title "Min/Max dates"})

(defexample
  [date-picker {:lang :de-DE}]
  {:title "German menu"})

(defexample
  [date-picker {:label "Date:"
                :label-params {:extra-class "input--w4"}}]
  {:title "With label"})