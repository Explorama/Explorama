(ns de.explorama.frontend.ui-base.overview.formular.radio
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.formular.radio :refer [radio default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample defutils]]))

(defcomponent
  {:name "Radio"
   :desc "Standard formular component for creating radio buttons"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [radio]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  (let [value (reagent/atom 0)]
    [:<>
     [radio {:label "small text"
             :strong-label "0. "
             :name "simple-radio"
             :on-change (fn [e]
                          (reset! value 0))
             :checked? (= 0 @value)}]
     [radio {:label "small text"
             :strong-label "1. "
             :name "simple-radio"
             :on-change (fn [e]
                          (reset! value 1))
             :checked? (= 1 @value)}]])
  {:title "Simple radio buttons"})

(defexample
  (let [value (reagent/atom 0)]
    [:<>
     [radio {:on-change (fn [e]
                          (reset! value 0))
             :name "image-radio"
             :label-variant :img
             :src "/img/window-grow-left.png"
             :width 150
             :height 75
             :tooltip "Tooltip 0"
             :tooltip-class "icon-info-circle icon-gray"
             :checked? (= 0 @value)}]
     [radio {:on-change (fn [e]
                          (reset! value 1))
             :name "image-radio"
             :label-variant :img
             :src "/img/window-grow-right.png"
             :width 150
             :height 75
             :tooltip "Tooltip 1"
             :tooltip-class "icon-info-circle icon-gray"
             :checked? (= 1 @value)}]])
  {:title "Image radio buttons"})