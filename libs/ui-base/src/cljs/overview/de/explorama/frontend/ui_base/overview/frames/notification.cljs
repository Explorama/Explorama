(ns de.explorama.frontend.ui-base.overview.frames.notification
  (:require [de.explorama.frontend.ui-base.components.frames.notification :refer [notification default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.frames.header :refer [header]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Notification"
   :desc "Shows an notification in frame. For example to show an warning"
   :require-statement "[de.explorama.frontend.ui-base.components.frames.core :refer [notification]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [:div.frame.center-x.top-12
   {:style {:position :relative
            :height "200px"}}
   [header {:title "My Frame Title"}]
   [notification {:show? true
                  :message "My Warning"}]]
  {:title "Simple Notification"})

(defexample
  (let [show? (reagent/atom true)]
    [:div.frame.center-x.top-12
     {:style {:position :relative
              :height "200px"}}
     [header {:title "My Frame Title"}]
     [:div.body
      "----My body Content----" [:br]
      "----My body Content----" [:br]
      "----My body Content----" [:br]
      "----My body Content----" [:br]
      [button {:label "Toggle Notification"
               :on-click #(swap! show? not)}]]
     [notification {:show? show?
                    :message "My Warning"
                    :on-close #(reset! show? false)}]])
  {:title "Close"})

(defexample
  [:div.frame.center-x.top-12
   {:style {:position :relative
            :height "200px"}}
   [header {:title "My Frame Title"}]
   [notification {:show? true
                  :icon :info-circle
                  :icon-params {:color :blue
                                :size 25}
                  :actions [{:label "continue"
                             :on-click #(js/console.log "continue")}
                            {:variant :secondary
                             :label "undo"
                             :start-icon :back
                             :on-click #(js/console.log "undo")}]
                  :message "My Info"}]]
  {:title "Actions"})

(defexample
  [:div.frame.center-x.top-12
   {:style {:position :relative
            :height "200px"}}
   [header {:title "My Frame Title"}]
   [notification {:show? true
                  :icon :info-circle
                  :message "My Info"}]]
  {:title "Specific Icon"})

(defexample
  [:div.frame.center-x.top-12
   {:style {:position :relative
            :height "200px"}}
   [header {:title "My Frame Title"}]
   [notification {:show? true
                  :icon :info-circle
                  :icon-params {:color :blue
                                :size 25}
                  :message "My Info"}]]
  {:title "Specific Icon Props"})