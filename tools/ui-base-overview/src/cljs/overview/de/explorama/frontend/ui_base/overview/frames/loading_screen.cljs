(ns de.explorama.frontend.ui-base.overview.frames.loading-screen
  (:require [de.explorama.frontend.ui-base.components.frames.loading-screen :refer [loading-screen default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.frames.header :refer [header]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Loading-screen"
   :desc "Frame modal loading screen to visualize for example that data are loading. Must be a child of a parent that :position is set to :relative for correct display. Provide no actions"
   :require-statement "[de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [:div {:style {:width "400px"
                 :position :relative
                 :height "300px"}}
   [loading-screen {:show? true}]]
  {:title "Basic loading screen"})

(defn do-with-timeout [func timeout]
  (js/window.setTimeout #(do
                           (func)
                           (do-with-timeout func timeout))
                        timeout))

(defexample
  (let [progress (reagent/atom 0)]
    (do-with-timeout #(if (>= @progress 100)
                        (reset! progress 0)
                        (swap! progress inc))
                     25)
    [:div {:style {:width "400px"
                   :position :relative
                   :height "300px"}}
     [loading-screen {:show? true
                      :message "Some message..."
                      :progress progress}]])
  {:title "Progress bar loading screen"})

(defexample
  [:div {:style {:width "600px"
                 :position :relative
                 :height "400px"}}
   [loading-screen {:show? true
                    :message "My Screen Title"
                    :tip-title "My Tip Title"
                    :tip "My screen tip"}]]
  {:title "Messages"})

(defexample
  [:div.frame.top-12
   {:style {:width "600px"
            :position :relative
            :height "350px"}}
   [header {:title "My Frame Title"}]
   [:div.body
    "TEST"
    [:br]
    (for [c (range 0 255)]
      ^{:key (str "example_color_" c)}
      [:div {:style {:background-color (str "rgb(" c "," c "," c ")")
                     :width "5px"
                     :height "5px"
                     :display :inline-block}}])
    [:br] [:br]
    "TEST2"
    [:br]
    (for [c (range 0 255)]
      ^{:key (str "example_color2_" c)}
      [:div {:style {:background-color (str "rgb(" (rand-int 256) "," (rand-int 256) "," (rand-int 256) ")")
                     :width "5px"
                     :height "5px"
                     ;:margin-right "1px"
                     :display :inline-block}}])]
   [loading-screen {:show? true
                    :message "My Screen Title"
                    :tip "My Tip Title"
                    :tip-title "My screen tip"}]]
  {:title "Messages with content behind"})

(defexample
  [:div {:style {:width "500px"
                 :position :relative
                 :height "400px"}}
   [loading-screen {:show? true
                    :message "Cancellable action in progress!"
                    :buttons [{:label "Cancel"
                               :on-click #(js/alert "Cancellation triggered")}]}]]
  {:title "Cancellable"})

(defexample
  [:div {:style {:width "400px"
                 :position :relative
                 :height "300px"}}
   [loading-screen {:show? true
                    :message-state :info
                    :message "Loading is done!"
                    :buttons [{:label "Awesome!"
                               :on-click #(js/alert "Just imagine the loading screen closed right now")}]}]]
  {:title "Simple info"})

(defexample
  [:div {:style {:height "300px" :width "400px"}}
   [loading-screen {:show? true
                    :message "Do you want to retry it?"
                    :message-state :info
                    :tip-title "Tip title"
                    :tip "Tip"
                    :buttons [{:label "OK"
                               :on-click #(js/alert "Okay!")}
                              {:label "No, leave me alone"
                               :variant :secondary
                               :on-click #(js/alert "Cancel!")}]}]]
  {:title "Multiple button info"})

(defexample
  [:div {:style {:height "200px"}}
   [loading-screen {:show? true
                    :message "Process failed!"
                    :message-state :error
                    :buttons [{:label "Fix it"
                               :on-click #(js/alert "Failed again.")}]}]]
  {:title "Tip message and message states"})