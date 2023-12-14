(ns de.explorama.frontend.ui-base.overview.common.tooltip
  (:require [de.explorama.frontend.ui-base.components.common.tooltip :refer [tooltip default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Tooltip"
   :desc "A standard tooltip"
   :require-statement "[de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [:div {:style {:width :fit-content}}
   [tooltip {:text "my tooltip"}
    [button {:label "Hover me"}]]]
  {:title "Simple tooltip"})

(defexample
  [:div {:style {:width :fit-content}}
   [tooltip {:text "my tooltip"
             :direction :down
             :alignment :end}
    [icon {:icon :intersect}]]]
  {:title "Direction and alignment"})

(defexample
  [:div {:style {:width :fit-content}}
   [tooltip {:text "my tooltip"
             :event-toggle :click
             :use-hover? false}
    [button {:label "Click me"}]]]
  {:title "Toggle"})

(defexample
  [:div {:style {:width :fit-content}}
   (let [counter (reagent/atom 0)]
     [tooltip {:text counter
               :on-toggle (fn [toggle?]
                            (when toggle?
                              (swap! counter inc)))}
      [button {:label "Hover me"}]])]
  {:title "React on toggle"})

(defexample
  [:div {:style {:width :fit-content}}
   (let [tooltip-text (reagent/atom "")]
     [tooltip {:text tooltip-text
               :event-on :change
               :event-off :blur
               :use-hover? false}
      [input-field {:on-change #(reset! tooltip-text %)
                    :aria-label "show on change example"}]])]
  {:title "Show on change"})

(defexample
  [:div {:style {:width :fit-content}}
   [tooltip {:text "my tooltip"
             :extra-style {:display :inline}}
    [button {:label "Hover me"}]
    [button {:label "Or hover me"}]]]
  {:title "Multiple Childs"})

(defexample
  [:div {:style {:width :fit-content}}
   [tooltip {:text "myasdasdasdasdasdasdasdasdasd \n tool \n tip "}
    [button {:label "Hover me"}]]]
  {:title "Tooltip with linebreaks"})