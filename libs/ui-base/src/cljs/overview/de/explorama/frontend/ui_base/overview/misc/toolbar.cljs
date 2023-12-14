(ns de.explorama.frontend.ui-base.overview.misc.toolbar
  (:require [de.explorama.frontend.ui-base.components.misc.toolbar :refer [toolbar toolbar-divider default-parameters item-default-parameters parameter-definition toolbar-item-definition popout-definition]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defutils defexample]]))

(defcomponent
  {:name "Toolbar"
   :desc "Component used to render the toolbars on the woco."
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [toolbar toolbar-divider]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Toolbar Item"
   :is-sub? true
   :section "components"
   :desc "A single item"
   :default-parameters item-default-parameters
   :parameters toolbar-item-definition})

(defutils
  {:name "Popout"
   :is-sub? true
   :section "components"
   :desc "A single popout"
   :parameters popout-definition})

(defexample
  [:div {:style {:height "100px"}}
   [toolbar {:extra-class ["absolute" "center-y"]
             :items [[{:title "Search"
                       :id "serach1"
                       :icon :search}
                      {:title "Globe"
                       :id "globe1"
                       :icon :atlas}]]}]]
  {:title "Toolbar"})

(defexample
  [:div {:style {:height "50px"}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :up
             :extra-class ["absolute" "center-x"]
             :items [[{:title "Search"
                       :id "serach2"
                       :icon :search}
                      {:title "Globe"
                       :id "globe2"
                       :icon :atlas}]]}]]
  {:title "Horizontal Toolbar"})
(defexample
  [:div {:style {:height "50px"}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :up
             :extra-class ["absolute" "center-x"]
             :items [[{:title "Search"
                       :id "serach3"
                       :icon :search}
                      {:title "Globe"
                       :id "globe3"
                       :icon :atlas}]
                     [{:title "mosaic"
                       :id "mosaic3"
                       :icon :mosaic2}
                      {:title "Map"
                       :id "map3"
                       :icon :map}]]}]]
  {:title "Segmented Toolbar"})

(defexample
  [:div {:style {:height "50px"}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :up
             :separator :symbol
             :extra-class ["absolute" "center-x"]
             :items [[{:title "Search"
                       :id "serach4"
                       :icon :search}
                      {:title "Globe"
                       :id "globe4"
                       :icon :atlas}]
                     [{:title "mosaic"
                       :id "mosaic4"
                       :icon :mosaic2}
                      {:title "Map"
                       :id "map4"
                       :icon :map}]]}]]
  {:title "Separated Toolbar"})

(defexample
  [:div {:style {:height "50px"}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :up
             :separator :symbol
             :extra-class ["absolute" "center-x"]
             :items [[{:title "Search"
                       :id "serach5"
                       :icon :search}
                      {:title "Globe"
                       :id "globe5"
                       :icon :atlas}]
                     [{:title "mosaic"
                       :id "mosaic5"
                       :active? true
                       :icon :mosaic2}
                      {:title "Map"
                       :id "map5"
                       :active? true
                       :icon :map}]
                     [{:title "Chart"
                       :id "chart5"
                       :disabled? true
                       :icon :charts}
                      {:title "table"
                       :id "table5"
                       :disabled? true
                       :icon :table}]]}]]
  {:title "Item States"})

(defn- toolbar-elem [color-state font-color-state]
  (let [font-color @font-color-state
        color @color-state]
    [:div {:style {:height "100px"}}
     [toolbar {:orientation :horizontal
               :extra-class ["absolute" "center-y"]
               :items [{:title "bold"
                        :id "bold"
                        :icon :bold
                        :label "bold"}
                       {:title "color"
                        :id "color"
                        :icon :color-circle
                        :icon-props {:color color
                                     :color-important? true}
                        :on-click #(swap! color-state (fn [old]
                                                        (if (= old :teal) :orange :teal)))
                        :label "colorcolor"}
                       {:title "text-color"
                        :id "text-color"
                        :icon :text-color
                        :icon-props {:color font-color
                                     :color-important? true}
                        :on-click #(swap! font-color-state (fn [old]
                                                             (if (= old :teal) :orange :teal)))
                        :label "text-color"}]}]]))

(defexample
  (let [font-color-state (reagent/atom :teal)
        color-state (reagent/atom :teal)]
    [toolbar-elem color-state font-color-state])

  {:title "Rich editor toolbar with color selecting"
   :code-before
   "(defn- toolbar-elem [color-state font-color-state]
  (let [font-color @font-color-state
        color @color-state]
    [:div {:style {:height \"100px\"}}
     [toolbar {:orientation :horizontal
               :extra-class [\"absolute\" \"center-y\"]
               :items [{:title \"bold\"
                        :id \"bold\"
                        :icon :bold
                        :label \"bold\"}
                       {:title \"color\"
                        :id \"color\"
                        :icon :color-circle
                        :icon-props {:color color
                                     :color-important? true}
                        :on-click #(swap! color-state (fn [old]
                                                        (if (= old :teal) :orange :teal)))
                        :label \"colorcolor\"}
                       {:title \"text-color\"
                        :id \"text-color\"
                        :icon :text-color
                        :icon-props {:color font-color
                                     :color-important? true}
                        :on-click #(swap! font-color-state (fn [old]
                                                             (if (= old :teal) :orange :teal)))
                        :label \"text-color\"}]}]]))"})

(defexample
  [:div {:style {:height "100px"}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :up
             :extra-class ["absolute" "center-y"]
             :toolbar-options-tooltip "Toolbar tooltip"
             :on-click-toolbar-options (fn [e] (js/alert "Toolbar options"))
             :items [[{:title "Search"
                       :id "serach1"
                       :icon :search
                       :label "Search"}
                      toolbar-divider
                      {:title "mosaic"
                       :id "mosaic1"
                       :icon :mosaic2
                       :label "mosaic"}]
                     [{:title "Globe"
                       :id "globe1"
                       :icon :atlas
                       :label "Atlas"}]]}]]
  {:title "Labels, dividers and toolbar-options"})

(defexample
  [:div {:style {:height "170px"}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :up
             :separator :symbol
             :extra-class ["absolute" "center-x"]
             :popouts [{:id "pop1"
                        :show? true
                        :content [:div {:style {:width "100%"
                                                :height "100px"
                                                :background-color "#dddddd"}}]}]
             :popout-position :start
             :items [[{:title "Search"
                       :id "serach6"
                       :icon :search}
                      {:title "Globe"
                       :id "globe6"
                       :icon :atlas}]
                     [{:title "mosaic"
                       :id "mosaic6"
                       :active? true
                       :icon :mosaic2}
                      {:title "Map"
                       :id "map6"
                       :active? true
                       :icon :map}]
                     [{:title "Chart"
                       :id "chart6"
                       :disabled? true
                       :icon :charts}
                      {:title "table"
                       :id "table6"
                       :disabled? true
                       :icon :table}]]}]]
  {:title "Popouts"})

(defexample
  [:div {:style {:height "100px"}}
   (let [show? (reagent/atom true)]
     [toolbar {:extra-class ["absolute" "center-y"]
               :popouts (mapv
                         #(hash-map
                           :id (str "pop" %)
                           :show? show?
                           :content [:div {:style {:width "40px"
                                                   :height "74px"
                                                   :background-color %}}])
                         ["#222222" "#333333" "#444444" "#555555"
                          "#666666" "#777777" "#888888" "#999999"
                          "#aaaaaa" "#bbbbbb" "#cccccc" "#dddddd"])
               :items [[{:title "Popouts"
                         :id "popouts7"
                         :label "click"
                         :active? show?
                         :on-click #(swap! show? not)}
                        {:title "Globe"
                         :id "globe7"
                         :disabled? true
                         :icon :atlas}]]}])]
  {:title "Stacked Popouts (toggle with first item)"})
