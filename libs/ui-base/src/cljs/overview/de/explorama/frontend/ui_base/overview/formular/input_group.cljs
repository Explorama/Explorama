(ns de.explorama.frontend.ui-base.overview.formular.input-group
  (:require [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.components.formular.input-group :refer [input-group default-parameters parameter-definition input-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defutils defexample]]))

(defcomponent
  {:name "Input-Group"
   :desc "Component used to combine other formular elements into one."
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [input-group]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Input"
   :is-sub? true
   :section "components"
   :desc "A single item"
   :parameters input-definition})

(defexample
  [input-group {:label "Some label"
                :caption "Some caption"
                :items [{:type :button
                         :id "1"
                         :component-props {:start-icon :user
                                           :aria-label "some example button"
                                           :variant :secondary}}
                        {:type :select
                         :id "2"
                         :component-props {:options []
                                           :values {}}}]}]
  {:title "Button with select"})

(defexample
  [input-group {:items [{:type :input
                         :id "1"
                         :component-props {:aria-label "example input"}}
                        {:type :input
                         :id "2"
                         :component-props {:aria-label "example input"}}]}]
  {:title "Double input"})

(defexample
  [input-group {:label "Save as.."
                :items [{:type :select
                         :id "1"
                         :component-props {:options []
                                           :values {}}}
                        {:type :button
                         :id "2"
                         :component-props {:start-icon :save
                                           :aria-label "some example button"}}]}]
  {:title "Save input"})
