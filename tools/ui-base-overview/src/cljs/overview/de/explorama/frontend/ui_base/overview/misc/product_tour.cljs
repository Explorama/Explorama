(ns de.explorama.frontend.ui-base.overview.misc.product-tour
  (:require [de.explorama.frontend.ui-base.components.misc.product-tour :refer [product-tour-step default-parameters parameter-definition step-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample defutils]]))

(defcomponent
  {:name "Product-Tour-Step"
   :desc "Component to display a step from a product-tour."
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [product-tour-step]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Step"
   :is-sub? true
   :section "components"
   :desc "One step"
   :parameters step-definition})

(defexample
  [:div {:style {:height 200}
         :id "simple"}
   [product-tour-step {:offset-top 0
                       :offset-left 250
                       :val-sub-fn (fn [val]
                                     val)
                       :component :test
                       :prev-fn (fn [])
                       :next-fn (fn [])
                       :cancel-fn (fn [])
                       :max-steps 10
                       :portal-target "simple"
                       :current-step {:component :test
                                      :additional-info :1
                                      :step 1
                                      :auto-next? false
                                      :title "First step in many"
                                      :description [[:translation "Take your first step."]]}}]]
  {:title "Simple step"})

(defexample
  [:div {:id "simple-with-back"
         :style {:height 200}}
   [product-tour-step {:offset-top 0
                       :offset-left 250
                       :val-sub-fn (fn [val]
                                     val)
                       :component :test
                       :prev-fn (fn [])
                       :next-fn (fn [])
                       :cancel-fn (fn [])
                       :max-steps 10
                       :show-back-button? true
                       :portal-target "simple-with-back"
                       :current-step {:component :test
                                      :additional-info :1
                                      :step 2
                                      :auto-next? false
                                      :title "Next step"
                                      :description [[:translation "Take your next step."]]}}]]
  {:title "Simple step with back-button"})

(defexample
  [:div {:style {:height 450}
         :id "with-top-image"}
   [product-tour-step {:offset-top 0
                       :offset-left 250
                       :val-sub-fn (fn [val]
                                     val)
                       :component :test
                       :prev-fn (fn [])
                       :next-fn (fn [])
                       :cancel-fn (fn [])
                       :max-steps 10
                       :portal-target "with-top-image"
                       :current-step {:component :test
                                      :additional-info :2
                                      :step 2
                                      :auto-next? false
                                      :title "Step with top-image"
                                      :top-desc-img "img/explorama-logo.svg"
                                      :top-desc-alt-img "Logo"
                                      :description [[:translation "Take your second step."]]}}]]
  {:title "Top image step"})

(defexample
  [:div {:style {:height 250}
         :id "with-step-icon"}
   [product-tour-step {:offset-top 0
                       :offset-left 250
                       :val-sub-fn (fn [val] val)
                       :component :test
                       :prev-fn (fn [])
                       :next-fn (fn [])
                       :cancel-fn (fn [])
                       :max-steps 10
                       :portal-target "with-step-icon"
                       :current-step {:component :test
                                      :additional-info :2
                                      :step 2
                                      :auto-next? false
                                      :title "Step with icon"
                                      :description [[:msg
                                                     [:translation "Take your second step."]
                                                     [:icon {:icon :add-project
                                                             :color :explorama__window__group-12
                                                             :brightness 2
                                                             :tooltip "Icon Tooltip here"}]]]}}]]
  {:title "Step Icon"})

(defexample
  [:div {:style {:height 400}
         :id "with-complex-desc"}
   [product-tour-step {:offset-top 0
                       :offset-left 250
                       :val-sub-fn (fn [val]
                                     val)
                       :component :test
                       :prev-fn (fn [])
                       :next-fn (fn [])
                       :cancel-fn (fn [])
                       :max-steps 10
                       :portal-target "with-complex-desc"
                       :current-step {:component :test
                                      :additional-info :3
                                      :step 3
                                      :auto-next? false
                                      :title "Complex Step desc"
                                      :description [[:msg
                                                     [:translation "first part of the text"]
                                                     [:img "img/plus.png" "plus"]
                                                     [:translation "second part of the text"]]
                                                    [:translation "this is in its own div"]
                                                    [:img "img/logo.svg" "logo"]]}}]]
  {:title "Complex desc"})
