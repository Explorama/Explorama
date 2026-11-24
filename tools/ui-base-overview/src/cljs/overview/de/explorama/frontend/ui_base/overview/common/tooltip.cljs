(ns de.explorama.frontend.ui-base.overview.common.tooltip
  (:require [de.explorama.frontend.ui-base.components.common.tooltip :refer [tooltip default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
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
             :direction :down}
    [icon {:icon :intersect}]]]
  {:title "Direction"})

(defexample
  [:div {:style {:width :fit-content}}
   [tooltip {:text "my tooltip"
             :direction :right
             :distance 20}
    [button {:label "Hover me"}]]]
  {:title "Distance"})

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