(ns de.explorama.frontend.ui-base.overview.misc.chip
  (:require [de.explorama.frontend.ui-base.components.misc.chip :refer [default-parameters colors chip parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Chip"
   :desc "Component to display chip, used for e.g. tags"
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [chip]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [:div
   {:style {:width :fit-content}}
   [chip {:label "some tag"
          :tooltip "some tooltip"}]]
  {:title "Basic chip"})

(defexample
  [:div
   {:style {:width :fit-content}}
   [chip {:label "some tag"
          :variant :secondary
          :tooltip "some tooltip"}]]
  {:title "Secondary variant"})

(defexample
  [:div.grid.grid-cols-3.gap-8
   {:style {:width :fit-content}}
   (reduce (fn [acc [color-key]]
             (conj acc
                   [chip {:label (str (name color-key) " - light")
                          :color color-key
                          :brightness :light}]
                   [chip {:label (str (name color-key))
                          :color color-key}]
                   [chip {:label (str (name color-key) " - dark")
                          :color color-key
                          :brightness :dark}]))
           [:<>]
           colors)]
  {:title "Colors and Brightness"})

(defexample
  [:div.grid.grid-cols-3.gap-8
   {:style {:width :fit-content}}
   [:div {:style {:justify-content "center"
                  :align-items "center"
                  :display "flex"}}
    [chip {:label "extra-small"
           :size :extra-small}]]
   [:div {:style {:justify-content "center"
                  :align-items "center"
                  :display "flex"}}
    [chip {:label "small"
           :size :small}]]
   [:div {:style {:justify-content "center"
                  :align-items "center"
                  :display "flex"}}
    [chip {:label "normal"}]]
   [:div {:style {:justify-content "center"
                  :align-items "center"
                  :display "flex"}}
    [chip {:label "big"
           :size :big}]]]
  {:title "Sizes"})

(defexample
  [:div
   {:style {:width "100%"}}
   [chip {:label "some tag"
          :full-width? true
          :tooltip "some tooltip"}]]
  {:title "Full width"})

(defexample
  [:div
   {:style {:width :fit-content}}
   [chip {:label "some tag"
          :start-icon :bell
          :tooltip "some tooltip"}]]
  {:title "With icon"})

(defexample
  [:div
   {:style {:width :fit-content}}
   [chip {:label "some tag"
          :button-icon :close
          :button-aria-label "close"
          :on-click #(js/alert "You clicked the tag.")
          :tooltip "some tooltip"}]]
  {:title "With button icon"})
