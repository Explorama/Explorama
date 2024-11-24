(ns de.explorama.frontend.ui-base.overview.formular.icon-select
  (:require [de.explorama.frontend.ui-base.components.formular.icon-select :refer [icon-select parameter-definition option-definition default-parameters]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample defutils]]
            [reagent.core :as reagent]))

(defcomponent
  {:name "Icon-Select"
   :desc "Standard formular component for selecting one icon"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [icon-select]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Option"
   :is-sub? true
   :section "components"
   :desc "A single options entry"
   :parameters option-definition})

(defexample
  (let [value (reagent/atom "icon-map")
        options [{:icon "icon-map"}
                 {:icon "icon-mosaic"}]]
    [icon-select {:options options
                  :aria-label "simple icon-select"
                  :value value
                  :on-change (fn [selection]
                               (reset! value selection))}])
  {:title "Simple Icon-Select"})

(defexample
  (let [value (reagent/atom :map)
        options [{:icon "icon-map"
                  :value :map}
                 {:icon "icon-mosaic"
                  :value :mosaic}]]
    [icon-select {:options options
                  :aria-label "Icon-Select with icon values"
                  :value value
                  :on-change (fn [selection]
                               (reset! value selection))}])
  {:title "Icon-Select with icon values"})

(defexample
  (let [value (reagent/atom "icon-map")
        options [{:icon "icon-map"}
                 {:icon "icon-mosaic"}]]
    [icon-select {:options options
                  :disabled? true
                  :aria-label "Disabled Icon-Select"
                  :value value
                  :on-change (fn [selection]
                               (reset! value selection))}])
  {:title "Disabled Icon-Select"})

(defexample
  (let [value (reagent/atom "icon-map")
        options [{:icon "icon-map"}
                 {:icon "icon-mosaic"}]]
    [icon-select {:options options
                  :label "Icon Selection"
                  :aria-label "With label Icon-Select"
                  :value value
                  :on-change (fn [selection]
                               (reset! value selection))}])
  {:title "With label Icon-Select"})

(defexample
  (let [value (reagent/atom "icon-map")
        options [{:icon "icon-map"
                  :tooltip "Map"}
                 {:icon "icon-mosaic"
                  :tooltip "mosaic"}]]
    [icon-select {:options options
                  :aria-label "Icon-Select options with tooltip"
                  :value value
                  :on-change (fn [selection]
                               (reset! value selection))}])
  {:title "Icon-Select options with tooltip"})

(defexample
  (let [value (reagent/atom "icon-map")
        options [{:icon "icon-map"
                  :extra-params {:color "blue"}}
                 {:icon "icon-mosaic"
                  :tooltip "mosaic"
                  :extra-params {:color "red"}}]]
    [icon-select {:options options
                  :aria-label "Icon-Select options with tooltip"
                  :value value
                  :on-change (fn [selection]
                               (reset! value selection))}])
  {:title "Icon-Select options with extra-params"})