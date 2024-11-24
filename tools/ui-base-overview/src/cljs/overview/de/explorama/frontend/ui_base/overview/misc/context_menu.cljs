(ns de.explorama.frontend.ui-base.overview.misc.context-menu
  (:require [de.explorama.frontend.ui-base.components.misc.context-menu :refer [context-menu calc-menu-position default-parameters parameter-definition list-entry-definition]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.frames.header :refer [header]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample defutils]]))

(defcomponent
  {:name "Context-menu"
   :desc "Component to display an context-menu"
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [context-menu]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "List Entry"
   :is-sub? true
   :section "components"
   :desc "A single context-menu item"
   :parameters list-entry-definition})

(defexample
  (let [position (reagent/atom {})
        show? (reagent/atom {})
        click-fn (fn [_ label value]
                   (js/alert (str  "Clicked - label: " label " ; value: " value)))]
    [:<>
     [button {:start-icon :burgermenu
              :aria-label "context menu"
              :on-click (fn [event]
                          (reset! position (calc-menu-position event))
                          (reset! show? true))}]
     [context-menu {:on-close #(reset! show? false)
                    :show? show?
                    :position position
                    :items [{:label "Test1"
                             :value :test1
                             :on-click click-fn}
                            {:label "Test2"
                             :on-click click-fn}]}]])
  {:title "Open with button click"})

(defexample
  (let [position (reagent/atom {})
        show? (reagent/atom {})
        click-fn (fn [_ label value]
                   (js/alert (str  "Clicked - label: " label " ; value: " value)))]
    [:<>
     [button {:start-icon :burgermenu
              :aria-label "context menu"
              :on-click (fn [event]
                          (reset! position (calc-menu-position event))
                          (reset! show? true))}]
     [context-menu {:on-close #(reset! show? false)
                    :show? show?
                    :position position
                    :close-on-select? false
                    :items [{:label "Test1"
                             :value :test1
                             :on-click click-fn}
                            {:label "Test2"
                             :on-click click-fn}]}]])
  {:title "Dont close on select item"})

(defn show-header [show-header? show? position]
  (when @show-header?
    [header {:title "My Frame Title"
             :extra-props {:on-context-menu (fn [event]
                                              (.preventDefault event)
                                              (reset! position (calc-menu-position event))
                                              (reset! show? true))}
             :extra-items [{:tooltip "Context menu"
                            :icon :menu
                            :on-click (fn [event]
                                        (reset! position (calc-menu-position event))
                                        (reset! show? true))}]
             :on-close #(js/alert "Close!")}]))

(defexample
  (let [show-header? (reagent/atom true)
        position (reagent/atom {})
        show? (reagent/atom {})
        click-fn (fn [_ label value]
                   (js/alert (str  "Clicked - label: " label " ; value: " value)))]
    [:<>
     [button {:label "Toggle header"
              :on-click #(swap! show-header? not)}]
     [:br] [:br]
     [:div.frame
      {:style {:position :relative}}
      [show-header show-header? show? position]
      [context-menu {:on-close #(reset! show? false)
                     :show? show?
                     :position position
                     :items [{:label "Test1"
                              :value :test1
                              :on-click click-fn}
                             {:label "Test2"
                              :on-click click-fn}]}]]])
  {:title "Header with context menü (icon and right click)"
   :code-before "
(defn show-header [show-header? show? position]
  (when @show-header?
    [header {:title \"My Frame Title\"
             :extra-props {:on-context-menu (fn [event]
                                              (.preventDefault event)
                                              (reset! position (calc-menu-position event))
                                              (reset! show? true))}
             :extra-items [{:tooltip \"Context menu\"
                            :icon :menu
                            :on-click (fn [event]
                                        (reset! position (calc-menu-position event))
                                        (reset! show? true))}]
             :on-close #(js/alert \"Close!\")}]))
                 "})

(defexample
  (let [position (reagent/atom {})
        show? (reagent/atom {})
        click-fn (fn [e label value]
                   (js/alert (str  "Clicked - label: " label " ; value: " value)))]
    [:<>
     [:div {:on-context-menu (fn [event]
                               (.preventDefault event)
                               (reset! position (calc-menu-position event))
                               (reset! show? true))
            :style {:border "1px solid gray"}}
      "Do Right Click here"]
     [context-menu {:on-close #(reset! show? false)
                    :show? show?
                    :position position
                    :items [{:type :group
                             :sub-items [{:label "G1 E1"
                                          :on-click click-fn}
                                         {:label "G1 E2"
                                          :on-click click-fn}]}
                            {:type :group
                             :sub-items [{:label "G2 E1"
                                          :on-click click-fn}
                                         {:label "G2 E2"
                                          :sub-items [{:label "sub 1"
                                                       :sub-items [{:label "sub1-sub1"
                                                                    :on-click click-fn}
                                                                   {:label "sub1-sub2"
                                                                    :on-click click-fn}]}
                                                      {:label "sub 2"
                                                       :on-click click-fn}]}]}]}]])
  {:title "Grouped and nested context-menu"})

(defexample
  (let [position (reagent/atom {})
        show? (reagent/atom {})
        click-fn (fn [e label value]
                   (js/alert (str  "Clicked - label: " label " ; value: " value)))]
    [:<>
     [:div {:on-context-menu (fn [event]
                               (.preventDefault event)
                               (reset! position (calc-menu-position event))
                               (reset! show? true))
            :style {:border "1px solid gray"}}
      "Do Right Click here"]
     [context-menu {:on-close #(reset! show? false)
                    :show? show?
                    :position position
                    :items [{:label "Normal item"
                             :on-click click-fn
                             :value :normal}
                            {:label "Disabled action"
                             :disabled? true
                             :value :disabled
                             :on-click click-fn}
                            {:label "Normal with subs"
                             :value :disabled-subitem
                             :sub-items [{:label "disabled sub 1"
                                          :on-click click-fn
                                          :disabled? true}
                                         {:label "sub 2"
                                          :value :sub2
                                          :on-click click-fn}]}
                            {:label "Disabled subitems"
                             :value :disabled-with-subs
                             :disabled? true
                             :sub-items [{:label "sub 1"
                                          :on-click click-fn}
                                         {:label "sub 2"
                                          :on-click click-fn}]}]}]])

  {:title "Disabled items"})

(defexample
  (let [position (reagent/atom {})
        show? (reagent/atom {})
        click-fn (fn [e label value]
                   (js/alert (str  "Clicked - label: " label " ; value: " value)))]
    [:<>
     [:div {:on-context-menu (fn [event]
                               (.preventDefault event)
                               (reset! position (calc-menu-position event))
                               (reset! show? true))
            :style {:border "1px solid gray"}}
      "Do Right Click here"]
     [context-menu {:on-close #(reset! show? false)
                    :show? show?
                    :position position
                    :menu-max-height 50
                    :items [{:label "Test1"
                             :value :test1
                             :on-click click-fn}
                            {:label "Test2"
                             :on-click click-fn}]}]])
  {:title "Max Menu Height"})

(defexample
  (let [position (reagent/atom {})
        show? (reagent/atom {})]
    [:<>
     [:div {:on-context-menu (fn [event]
                               (.preventDefault event)
                               (reset! position (calc-menu-position event))
                               (reset! show? true))
            :style {:border "1px solid gray"}}
      "Do Right Click here"]
     [context-menu {:on-close #(reset! show? false)
                    :show? show?
                    :position position
                    :items [nil
                            {}
                            {:label "Test1"
                             :value :test1
                             :sub-items [nil {}]}]}]])
  {:title "Empty or nil items are ignored"})