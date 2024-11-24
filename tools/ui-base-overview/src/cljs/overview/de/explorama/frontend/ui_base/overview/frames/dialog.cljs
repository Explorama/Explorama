(ns de.explorama.frontend.ui-base.overview.frames.dialog
  (:require [de.explorama.frontend.ui-base.components.frames.dialog :refer [dialog default-parameters button-defaults parameter-definition]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.frames.header :refer [header]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Dialog box"
   :desc "A dialog box"
   :default-parameters (merge default-parameters button-defaults)
   :require-statement "[de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]"
   :parameters parameter-definition})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)]
     [:div
      [button {:label "Click me"
               :on-click (fn []
                           (reset! show? true))}]
      [dialog
       {:show? show?
        :hide-fn #(reset! show? false)}]])]
  {:title "Empty dialog"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)
         message (reagent/atom "Message for you, Sir!")]
     [:div
      [button {:label "Click me"
               :on-click (fn [] (reset! show? true))}]
      [dialog
       {:show? show?
        :hide-fn #(reset! show? false)
        :title "The message"
        :message message}]])]
  {:title "Simple alert box with OK button with :show? atom"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)
         message (reagent/atom "Watch the tutorial?")]
     [:div
      [button {:label "Click me"
               :on-click (fn [] (reset! show? true))}]
      [dialog
       {:show? show?
        :hide-fn #(reset! show? false)
        :title "Something new"
        :message message
        :yes {:on-click #(js/alert "YES clicked")}
        :no {:on-click #(js/alert "NO clicked")}
        :checkbox {:checked? true
                   :on-change #(js/alert "checkbox clicked")}}]])]
  {:title "Simple checkbox"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)]
     [(fn []
        [:div
         [button {:label "Click me"
                  :on-click (fn []
                              (reset! show? true))}]
         [dialog
          {:show? @show?
           :hide-fn #(reset! show? false)
           :title "The message"
           :message "Message for you, Sir!"}]])])]
  {:title "Simple alert box with OK button with :show? value"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)
         message (reagent/atom "Confirmation needed, Sir!")]
     [:div
      [button {:label "Click me"
               :on-click (fn [] (reset! show? true))}]
      [dialog
       {:show? show?
        :type :prompt
        :hide-fn #(reset! show? false)
        :title "The message"
        :message message}]])]
  {:title "Prompt dialog"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)
         message (reagent/atom "You're on fire, Sir!")]
     [:div
      [button {:label "Click me"
               :on-click (fn [] (reset! show? true))}]
      [dialog
       {:show? show?
        :type :warning
        :hide-fn #(reset! show? false)
        :title "The message"
        :yes {:type :warning}
        :message message}]])]
  {:title "Warning dialog"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)]
     [:div
      [button {:label "Click me"
               :on-click (fn []
                           (reset! show? true))}]
      [dialog
       {:show? show?
        :hide-fn #(reset! show? false)
        :type :container}]])]
  {:title "Container dialog"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)]
     [:div
      [button {:label "Click me"
               :on-click (fn [] (reset! show? true))}]
      [dialog
       {:show? show?
        :hide-fn #(reset! show? false)
        :title "Confirmation"
        :message "Do you want to proceed"
        :cancel true
        :ok {:on-click #(js/alert "OK clicked")}}]])]
  {:title "Ok/Cancel dialog"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   (let [show? (reagent/atom false)]
     [:div
      [button {:label "Click me"
               :on-click (fn [] (reset! show? true))}]
      [dialog
       {:show? show?
        :hide-fn #(reset! show? false)
        :title "Confirmation"
        :message "Do you want to proceed"
        :yes {:on-click #(js/alert "YES clicked")}
        :no {}}]])]
  {:title "Yes/No style dialog"})

(defexample
  [:div {:style {:display "flex"
                 :align-items "center"
                 :justify-content "center"
                 :height "200px"}}
   [(fn []
      (let [show? (reagent/atom false)]
        [:div
         [button {:label "Click me"
                  :on-click (fn [] (reset! show? true))}]
         [dialog
          {:show? show?
           :hide-fn #(reset! show? false)
           :title "Generic dialog with :ok :yes :no :cancel buttons"
           :message "What do you want to do?"
           :details "Choose a button. All of them will hide the dialog. Yes and No have default label. OK has additional action assigned"
           :cancel {:label "Ignore"}
           :ok {:label "Go"
                :on-click #(js/alert "OK clicked")}
           :yes {:on-click #(js/alert "Yes clicked")}
           :no {:variant :secondary}}]]))]]
  {:title "Custom dialog"})

(defexample
  [:div.frame.center-x.top-12
   {:style {:width "600px"
            :position :relative
            :height "350px"}}
   (let [show? (reagent/atom false)]
     [:<>
      [dialog
       {:show? show?
        :message "Somehting about your frame"
        :title "Message"
        :hide-fn #(reset! show? false)}]
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
                        :display :inline-block}}])
       [:br] [:br]
       [button {:label "Click me"
                :on-click (fn [] (reset! show? true))}]]])]
  {:title "Within frame"})

(defexample
  [:div.frame.center-x.top-12
   {:style {:width "600px"
            :position :relative
            :height "350px"}}
   (let [show? (reagent/atom false)]
     [:<>
      [dialog
       {:show? show?
        :message "Somehting about your frame"
        :title "Message"
        :full-size? true
        :hide-fn #(reset! show? false)}]
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
                        :display :inline-block}}])
       [:br] [:br]
       [button {:label "Click me"
                :on-click (fn [] (reset! show? true))}]]])]
  {:title "Cover full frame"})
