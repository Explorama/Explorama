(ns de.explorama.frontend.ui-base.overview.common.virtualized-list
  (:require [de.explorama.frontend.ui-base.components.common.virtualized-list :refer [virtualized-list default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [reagent.core :as r]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Virtualized List"
   :desc "List component for high performance. Uses https://github.com/bvaughn/react-virtualized"
   :require-statement "[de.explorama.frontend.ui-base.components.common.core :refer [virtualized-list]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [virtualized-list {:rows (mapv identity (range 0 100000))}]
  {:title "Simple list with 100.000 rows"})

(defexample
  [virtualized-list {:width 80
                     :height 50}]
  {:title "Empty list"})

(defexample
  [virtualized-list
   {:rows (mapv identity (shuffle (range 0 1000)))
    :row-renderer (fn [key index style row]
                    (r/as-element
                     ^{:key key}
                     [:center {:role "row"
                               :style (assoc style
                                             :background-color :gray
                                             :border-bottom "1px solid white"
                                             :font-size 8)}
                      [:button {:role "gridcell"
                                :style {:width 30
                                        :height 18}
                                :on-click #(js/console.log "Clicked on index" index "with row" row)}
                       row]]))}]
  {:title "Custom row-renderer"})

(defexample
  [virtualized-list
   {:rows []
    :no-rows-renderer (fn []
                        (r/as-element [:div {:role "row"}  [:div {:role "gridcell"} "Custom text for empty list"]]))}]
  {:title "Custom no-rows-renderer"})

(defexample
  [:div {:style {:height "300px"
                 :width "800px"
                 :background-color :lightgray}}
   [virtualized-list
    {:full-width? true
     :full-height? true
     :rows (mapv identity (range 0 1000))}]]
  {:title "Use 100% width and height"})

(defexample
  [virtualized-list
   {:dynamic-height? true
    :full-width? true
    :rows (mapv #(str "a\nb" % "\n\n") (range 0 10))}]
  {:title "Dynamic height"})

(defexample
  (let [calc-examples #(mapv (fn [i]
                               {:i i
                                :height (+ 20 (rand-int 50))})
                             (range 0 10))
        examples (r/atom (calc-examples))]
    [:div
     [button {:label "Change heights"
              :start-icon :reset
              :on-click #(reset! examples (calc-examples))}]
     [:div {:style {:height 20}}]
     [virtualized-list
      {:dynamic-height? true
       :row-renderer (fn [key index style {:keys [height i]}]
                       [:div {:style style}
                        [:div {:style {:height height
                                       :color :white
                                       :border-bottom "1px solid white"
                                       :background-color (str "rgb(60, 10," (* 2 height) ")")}}
                         i]])
       :full-width? true
       :rows examples}]])
  {:title "Dynamic height with custom renderer"})

