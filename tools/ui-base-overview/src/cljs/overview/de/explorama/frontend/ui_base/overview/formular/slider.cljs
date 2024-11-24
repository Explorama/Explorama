(ns de.explorama.frontend.ui-base.overview.formular.slider
  (:require [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.components.formular.slider :refer [slider default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Slider"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [slider]]"
   :desc [:<> "A slider component to select a number or range. Uses " [:a {:target "_blank"
                                                                           :href "https://github.com/react-component/slider"} "Rc-Slider"]]
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  (let [value (reagent/atom 16)]
    [slider {:step 2
             :label "Slide!"
             :label-params {:extra-class "input--w4"}
             :dots? true
             :value value
             :min 10
             :max 20
             :on-change #(reset! value %)}])
  {:title "Normal slider with dots and label"})

(defexample
  [slider {:disabled? true
           :on-change #(do)
           :value 20}]
  {:title "Disabled"})

(defexample
  (let [value (reagent/atom 1550)]
    [slider {:label "Slide!"
             :label-params {:extra-class "input--w4"}
             :num-format? false
             :value value
             :min 1000
             :max 2000
             :on-change #(reset! value %)}])
  {:title "No thousand seperator"})

(defexample
  (let [value (reagent/atom 16)]
    [slider {:reverse? true
             :value value
             :extra-class "adff"
             :on-change #(reset! value %)}])
  {:title "Reverse"})

(defexample
  (let [value1 (reagent/atom 16)
        value2 (reagent/atom 16)]
    [:<>
     [:div.col-2 {:style {:height "100px"
                          :display :inline-block}}
      [slider {:vertical? true
               :on-change #(reset! value1 %)
               :value value1}]]
     [:div.col-2 {:style {:height "100px"
                          :display :inline-block}}
      [slider {:vertical? true
               :reverse? true
               :on-change #(reset! value2 %)
               :value value2}]]])
  {:title "Vertical slider"})

(defexample
  (let [value (reagent/atom nil)]
    [slider {:value value
             :on-change #(reset! value %)
             :marks {16 (reagent/as-element [:div {:style {:background-color :black
                                                           :color :white
                                                           :display :inline-block}}
                                             " 16 "])
                     50 {:label " 50 "}
                     80 {:style {:background-color :black
                                 :color :white
                                 :display :inline-block}
                         :label " 80 "}}}])
  {:title "Custom marks"})

(defexample
  (let [value (reagent/atom [30 60])]
    [slider {:range? true
             :label "My Range:"
             :value value
             :on-change #(reset! value %)}])
  {:title "Range slider with label"})

(defexample
  (let [value (reagent/atom [30 60])]
    [slider {:range? true
             :value value
             :allow-crossing? false
             :on-change #(reset! value %)}])
  {:title "Range (disable crossing)"})

(defexample
  (let [value (reagent/atom [10 30 40 60])]
    [slider {:range? true
             :value value
             :ranges-count 3
             :pushable? true
             :on-change #(reset! value %)}])
  {:title "Multi range and pushable"})

(defexample
  (let [value (reagent/atom 16)]
    [slider {:value value
             :show-number-input? false
             :auto-marks? true
             :min -1000000
             :max 1000000
             :on-change #(reset! value %)}])
  {:title "Big range with auto-marks"})
