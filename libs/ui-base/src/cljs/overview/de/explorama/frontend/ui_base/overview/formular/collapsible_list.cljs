(ns de.explorama.frontend.ui-base.overview.formular.collapsible-list
  (:require [de.explorama.frontend.ui-base.components.formular.collapsible-list :refer [collapsible-list default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Collapsible list"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [collapsible-list]]"
   :desc "Standard formular component for providing a collapsable list"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [:div {:style {:width "300px"
                 :height "200px"}}
   [collapsible-list {:items (mapv (fn [i] {:label (str "Item " i)
                                            :id i})
                                   (range 0 10))
                      :on-click (fn [item]
                                  (js/alert (str "Click on " item)))
                      :collapse-items-fn (fn [parent-id]
                                           (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                          :value i})
                                                 (range 0 5)))}]]
  {:title "Simple list"})

(defexample
  [:div {:style {:width "300px"
                 :height "200px"}}
   [collapsible-list {:items (mapv (fn [i] {:label (str "Item " i)
                                            :id i})
                                   (range 0 10))
                      :row-height 28
                      :on-click (fn [item]
                                  (js/alert (str "Click on " item)))
                      :collapse-items-fn (fn [parent-id]
                                           (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                          :value i})
                                                 (range 0 5)))}]]
  {:title "Compact list"})

(defexample
  [:div {:style {:width "300px"
                 :height "200px"}}
   [collapsible-list {:items (mapv (fn [i] {:label (str "Item " i)
                                            :id i})
                                   (range 0 5))
                      :multiple-open? true
                      :on-click (fn [item]
                                  (js/alert (str "Click on " item)))
                      :collapse-items-fn (fn [parent-id]
                                           (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                          :value i})
                                                 (range 0 3)))}]]
  {:title "Multiple collapsable"})

(defexample
  [:div {:style {:width "300px"
                 :height "200px"}}
   [collapsible-list {:items (mapv (fn [i] {:label (str "Item " i)
                                            :id i})
                                   (range 0 5))
                      :open-all? true
                      :on-click (fn [item]
                                  (js/alert (str "Click on " item)))
                      :collapse-items-fn (fn [parent-id]
                                           (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                          :value i})
                                                 (range 0 3)))}]]
  {:title "All collapsed"})

(defexample
  [:div {:style {:width "300px"
                 :height "200px"}}
   [collapsible-list {:disabled? true
                      :items (mapv (fn [i] {:label (str "Item " i)
                                            :id i})
                                   (range 0 10))
                      :on-click (fn [item]
                                  (js/alert (str "Click on " item)))
                      :collapse-items-fn (fn [parent-id]
                                           [{:label (str "P" parent-id "-Item")
                                             :value 10}])}]]
  {:title "Disabled"})

(defexample
  [:div {:style {:width "300px"
                 :height "150px"}}
   [collapsible-list {:items [{:label  "Collapsable Item" :id 1}
                              {:label  "non-collapsible Item" :id 2 :collapsible? false}
                              {:label  "Collapsable Item" :id 3}]
                      :on-click (fn [item]
                                  (js/alert (str "Click on " item)))
                      :collapse-items-fn (fn [parent-id]
                                           (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                          :value i})
                                                 (range 0 5)))}]]
  {:title "Mixed with non-collapsible"})

(defexample
  [:div {:style {:width "300px"
                 :height "150px"}}
   [collapsible-list {:items [{:label  "Collapsable Item" :id :first-id}
                              {:label  "Collapsable Item" :id :second-id}]
                      :default-open-id :second-id
                      :collapse-items-fn (fn [parent-id]
                                           (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                          :value i})
                                                 (range 0 5)))}]]
  {:title "Mount with open item"})

(defexample
  [:div {:style {:width "300px"
                 :height "150px"}}
   [collapsible-list {:items [{:label  "Collapsable Item" :id :first-id}
                              {:label  "Disabled Item" :id :disabled-id :disabled? true}
                              {:label  "Collapsable Item" :id :second-id}]
                      :collapse-items-fn (fn [parent-id]
                                           (if (= parent-id :first-id)
                                             [{:label "Item -> " :value 0}
                                              {:label "Item (disabled)" :value 1 :disabled? true}
                                              {:label "Item -> " :value 2}]
                                             (mapv (fn [i] {:label (str "P" parent-id "-Item " i)
                                                            :value i})
                                                   (range 0 5))))}]]
  {:title "Single disabled items"})