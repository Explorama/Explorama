(ns de.explorama.frontend.ui-base.overview.formular.checkbox
  (:require [de.explorama.frontend.ui-base.components.formular.checkbox :refer [checkbox default-parameters parameter-definition]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Checkbox"
   :desc "Standard formular component for activating/deactivating something"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [checkbox]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  (let [checked? (reagent/atom false)]
    [checkbox {:checked? checked?
               :label "Test-Label"
               :on-change (fn [new-state]
                            (reset! checked? new-state))}])
  {:title "Checkbox"})

(defexample
  (let [checked? (reagent/atom false)]
    [checkbox {:checked? checked?
               :as-toggle? true
               :label "Active?"
               :on-change (fn [new-state]
                            (reset! checked? new-state))}])
  {:title "Toggle"})

(defexample
  [checkbox {:checked? true
             :read-only? true
             :label "Option 1"
             :on-change #(js/console.log "Change" %)}]
  {:title "Read-only"})

(defexample
  (let [checked? (reagent/atom false)]
    [checkbox {:checked? checked?
               :label "I'm a disabled checkbox"
               :label-params {:extra-class "input--w20"}
               :disabled? true
               :on-change (fn [new-state]
                            (reset! checked? new-state))}])
  {:title "Disabled and with label-class"})

(defexample
  (let [checked? (reagent/atom false)]
    [checkbox {:checked? checked?
               :box-position :right
               :label "Check me"
               :extra-class "input--w6"
               :on-change (fn [new-state]
                            (reset! checked? new-state))}])
  {:title "Box-position and extra-class"})

(defexample
  [checkbox {:checked? true
             :on-change #()
             :label [:div {:style {:display :inline
                                   :background-color :black
                                   :color :white}}
                     "My Label"]}]
  {:title "With component-label"})