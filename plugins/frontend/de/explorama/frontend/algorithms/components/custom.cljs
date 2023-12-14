(ns de.explorama.frontend.algorithms.components.custom
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [label]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [reagent.core :as reagent]))

(defn row [header content]
  [:div.explorama__form__static
   [label {:label header}]
   [:div.form__info__block.input--w18
    content]])

;! remove this and put it into the ui-base
(defn options-hidden [translate-function & hidden-elements]
  (let [state (reagent/atom false)]
    (fn [translate-function & hidden-elements]
      [:div.options__collapsible
       [:div {:class (cond-> "content"
                       @state
                       (str " open"))}
        (into [:<>] hidden-elements)]
       [:a {:href "#"
            :on-click #(swap! state not)}
        [:div.options__collapsible__bar
         [:div.label
          [:span
           (if @state (translate-function :settings-show-less)
               (translate-function :settings-show-more))]]]]])))