(ns de.explorama.frontend.ui-base.overview.formular.select
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.formular.select :refer [select default-parameters parameter-definition option-definition]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample defutils]]))

(defcomponent
  {:name "Select"
   :desc "Standard formular component for selecting one or multiple values"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [select]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Option"
   :is-sub? true
   :section "components"
   :desc "A single options entry"
   :parameters option-definition})

(defexample
  (let [value (reagent/atom {})
        options (mapv (fn [v]
                        {:label (str "T" v)
                         :value (str "T" v)})
                      (range 0 10000))]
    [select {:options options
             :values value
             :label "Label"
             :caption "Caption"
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Single-Select"})

(defexample
  (let [values (reagent/atom [])
        options  (mapv (fn [v]
                         {:label (str "T" v)
                          :value (str "T" v)})
                       (range 0 10000))]
    [select {:options options
             :values values
             :is-multi? true
             :on-change (fn [selections]
                          (reset! values selections))}])
  {:title "Multi-Select"})

(defexample
  (let [value (reagent/atom {})
        options (mapv (fn [v]
                        {:label (str "T" v)
                         :value (str "T" v)})
                      (range 0 10000))]
    [select {:options options
             :values value
             :start-icon :search
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Start Icon"})

(defexample
  (let [value (reagent/atom {})
        options [{:label "Group 1"
                  :options [{:label "T1" :value "T1"}
                            {:label "T2" :value "T2"}]}
                 {:label "Group 2"
                  :options [{:label "T3" :value "T3"}
                            {:label "T4" :value "T4"}]}
                 {:label "Group 3"
                  :options [{:label "T5" :value "T5"}
                            {:label "T6" :value "T6"}]}]]
    [select {:options options
             :values value
             :on-change (fn [selection]
                          (reset! value selection))
             :is-grouped? true}])
  {:title "Single-Select-Grouped"})

(defexample
  (let [values (reagent/atom [])
        options [{:label "Group 1"
                  :fixed? true
                  :options [{:label "T1" :value "T1"}
                            {:label "T2" :value "T2"}]}
                 {:label "Group 2"
                  :options [{:label "T3" :value "T3"}
                            {:label "T4" :value "T4"}]}
                 {:label "Group 3"
                  :options [{:label "T5" :value "T5"}
                            {:label "T6" :value "T6"}]}]]
    [select {:options options
             :values values
             :on-change (fn [selection]
                          (js/console.log "--" selection)
                          (reset! values selection))
             :is-grouped? true
             :is-multi? true}])
  {:title "Multi-Select-Grouped"})

(defexample
  (let [values (reagent/atom [])
        options [{:label "Group 1"
                  :fixed? true
                  :options [{:label "T1" :value "T1"}
                            {:label "T2" :value "T2"}]}
                 {:label "Group 2"
                  :options [{:label "T3" :value "T3"}
                            {:label "T4" :value "T4"}]}
                 {:label "Group 3"
                  :options [{:label "T5" :value "T5"}
                            {:label "T6" :value "T6"}]}]]
    [select {:options options
             :values values
             :on-change (fn [selection]
                          (js/console.log "--" selection)
                          (reset! values selection))
             :keep-selected? true
             :is-grouped? true
             :is-multi? true}])
  {:title "Multi-Select-Grouped - Keep selected elements"})

(defexample
  (let [values (reagent/atom [])
        options [{:label "Group 1"
                  :options [{:label "T1" :value "T1"}
                            {:label "T2" :value "T2"}]}
                 {:label "Group 2"
                  :options [{:label "T3" :value "T3"}
                            {:label "T4" :value "T4"}]}]]
    [select {:options options
             :values values
             :on-change (fn [selection]
                          (js/console.log "--" selection)
                          (reset! values selection))
             :is-grouped? true
             :group-selectable? false
             :is-multi? true}])
  {:title "Multi-Select-Grouped not selectable"})

(defexample
  (let [values (reagent/atom [{:label "T1" :value "T1" :gkey "g2"}])
        options [{:label "Group 1"
                  :fixed? true
                  :gkey "g1"
                  :options [{:label "T1" :value "T1" :gkey "g1"}
                            {:label "T2" :value "T2" :gkey "g1"}]}
                 {:label "Group 2"
                  :gkey "g2"
                  :options [{:label "T3" :value "T3" :gkey "g2"}
                            {:label "T4" :value "T4" :gkey "g2"}]}
                 {:label "Group 3"
                  :gkey "g3"
                  :options [{:label "T5" :value "T5" :gkey "g3"}
                            {:label "T6" :value "T6" :gkey "g3"}]}]]
    [select {:options options
             :values values
             :on-change (fn [selection]
                          (js/console.log "--" selection)
                          (reset! values selection))
             :is-grouped? true
             :group-selectable? false
             :is-multi? true
             :group-value-key :gkey
             :mark-invalid? true}])
  {:title "Multi-Select-Grouped invalid marking with group-key"})

(defexample
  (let [value (reagent/atom [])
        options [{:label "T1" :value "T1"}
                 {:label "T2" :value "T2"}
                 {:label "T3" :value "T3"}]]
    [select {:options options
             :values value
             :is-multi? true
             :show-all-group? true
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Multi-Select with all group"})

(defexample
  (let [value (reagent/atom {})
        options [{:label "T1" :value "T1"}]]
    [select {:options options
             :values value
             :disabled? true
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Disabled-Single-Select"})

(defexample
  (let [value (reagent/atom {})
        options [{:label "T1" :value "T1"}
                 {:label "T2" :value "T2" :disabled? true}
                 {:label "T3" :value "T3" :disabled? true :disabled-hint "Möööp I am disabled"}
                 {:label "T4" :value "T4"}]]
    [select {:options options
             :values value
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Disabled option (menu)"})

(re-frame/reg-sub
 :sel-options
 (fn [_]
   [{:label "T1" :value "T1"}
    {:label "T2" :value "T2"}
    {:label "T3" :value "T3"}]))

(re-frame/reg-event-db
 :change-sel-single
 (fn [db [_ selection]]
   (assoc db :selected selection)))

(re-frame/reg-sub
 :selection
 (fn [db]
   (get db :selected {})))

(defexample
  [select {:options (re-frame/subscribe [:sel-options])
           :values (re-frame/subscribe [:selection])
           :on-change (fn [selection]
                        (re-frame/dispatch [:change-sel-single selection]))}]
  {:title "Re-frame Single-Select"
   :code-before
   "(re-frame/reg-sub
 :sel-options
 (fn [_]
   [{:label \"T1\" :value \"T1\"}
    {:label \"T2\" :value \"T2\"}
    {:label \"T3\" :value \"T3\"}]))

(re-frame/reg-event-db
 :change-sel-single
 (fn [db [_ selection]]
   (assoc db :selected selection)))

(re-frame/reg-sub
 :selection
 (fn [db]
   (get db :selected {})))"})

(defexample
  (let [value (reagent/atom {:label "T2" :value "make-invalid"})
        options [{:label "T1" :value "T1"}
                 {:label "T2" :value "T2"}
                 {:label "T3" :value "T3"}]]
    [select {:options options
             :values value
             :mark-invalid? true
             :on-change (fn [selection]
                          (reset! value
                                  (assoc selection
                                         :value "make-invalid")))}])
  {:title "Invalid-Option"})

(defexample
  (let [value (reagent/atom {})
        options [{:label "T1" :value "T1"}
                 {:label "T2" :value "T2"}
                 {:label "T3" :value "T3"}]]
    [select {:options options
             :values value
             :close-on-select? false
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Don't close on select"})

(defexample
  [select {:options []
           :values {}
           :extra-class "input--w10"}]
  {:title "Specific width"})

(defexample
  [select {:options [{:l "T1"}]
           :values {}
           :label-key :l
           :value-key :l}]
  {:title "Specific option keys"})

(defexample
  (let [values (reagent/atom [])
        options [{:label [:div {:style {:width "10px"
                                        :height "10px"
                                        :background-color "blue"}}]
                  :value "blue-option"}
                 {:label [:div {:style {:width "10px"
                                        :height "10px"
                                        :background-color "red"}}]
                  :value "red-option"}]]
    [select {:options options
             :values values
             :is-multi? true
             :tooltip-key :value
             :on-change (fn [selections]
                          (reset! values selections))}])
  {:title "Component as label"})

(defexample
  (let [options [{:label "T1" :value "T1"}
                 {:label "T2" :value "T2"}]
        values (reagent/atom (first options))]
    [select {:options options
             :values values
             :on-change (fn [selection]
                          (reset! values selection))
             :is-clearable? false}])
  {:title "Not clearable"})

(defexample
  [select {:options []
           :label "My Label"}]
  {:title "With label"})

(defexample
  (let [options [{:label "T2" :value "T2"}
                 {:label "T3" :value "T3"}]
        values (reagent/atom [{:label "T1" :value "T1" :fixed? true}])]
    [select {:options options
             :values values
             :is-multi? true
             :on-change (fn [selection]
                          (reset! values selection))}])
  {:title "Fixed option"})

(defexample
  (let [value (reagent/atom {:label (str "T" 100)
                             :value 100})
        options (mapv (fn [v]
                        {:label (str "T" v)
                         :value v})
                      (range 0 255))]
    [select {:options options
             :value-render-fn (fn [{:keys [label value]}]
                                [:div.flex.align-items-center.gap-6
                                 [icon {:icon :mosaic-circle
                                        :custom-color (str "rgb(" value ", 0 ,0)")}]
                                 label])
             :values value
             :label "Label"
             :caption "Caption"
             :on-change (fn [selection]
                          (reset! value selection))}])
  {:title "Custom value renderer"})