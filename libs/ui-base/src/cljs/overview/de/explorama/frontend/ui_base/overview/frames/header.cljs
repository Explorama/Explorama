(ns de.explorama.frontend.ui-base.overview.frames.header
  (:require [de.explorama.frontend.ui-base.components.frames.header :refer [header default-parameters parameter-definition extra-item-definition]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample defutils]]))

(defcomponent
  {:name "Header"
   :desc "A basic explorama frame header which handles maximize, minimize and normalize automatically"
   :require-statement "[de.explorama.frontend.ui-base.components.frames.core :refer [header]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Header Item"
   :is-sub? true
   :section "components"
   :desc "A single item entry"
   :parameters extra-item-definition})

(defexample
  [:div.frame.center-x.w-6-12.top-12
   [header {:title "My Frame Title"}]]
  {:title "Simple Frame"})

(defexample
  [:div.frame.center-x.w-6-12.top-12
   [header {:title "My Frame Title"
            :icon :cogs
            :icon-params {:color :white}}]]
  {:title "With Icon"})

(defexample
  [:div.frame.center-x.w-6-12.top-12
   (let [is-minimized? (reagent/atom false)
         is-maximized? (reagent/atom false)]
     [header {:title "My Frame Title"
              :double-click-resizes? true
              :on-minimize (fn []
                             (reset! is-minimized? true)
                             (reset! is-maximized? false))
              :on-maximize (fn []
                             (reset! is-minimized? false)
                             (reset! is-maximized? true))
              :on-normalize (fn []
                              (reset! is-minimized? false)
                              (reset! is-maximized? false))
              :is-maximized? is-maximized?
              :is-minimized? is-minimized?
              :on-close #(js/alert "Close!")}])]
  {:title "Maximize, minimize, normalize, close"})

(defexample
  [:div.frame.center-x.w-6-12.top-12
   (let [is-minimized? (reagent/atom false)
         is-maximized? (reagent/atom false)
         is-commented? (reagent/atom false)]
     [header {:title "My Frame Title"
              :on-minimize (fn []
                             (reset! is-minimized? true)
                             (reset! is-maximized? false))
              :on-maximize (fn []
                             (reset! is-minimized? false)
                             (reset! is-maximized? true))
              :on-normalize (fn []
                              (reset! is-minimized? false)
                              (reset! is-maximized? false))
              :is-maximized? is-maximized?
              :is-minimized? is-minimized?
              :on-info #(js/alert "Info!")
              :on-close #(js/alert "Close!")
              :on-filter #(js/alert "Filter!")
              :on-copy #(js/alert "Copy!")
              :is-commented? is-commented?
              :on-comment (fn []
                            (swap! is-commented? not)
                            (js/alert "Comment!"))}])]
  {:title "All"})

(defexample
  [:div.frame.center-x.w-6-12.top-12
   [header {:title "My Frame Title"
            :extra-items [{:tooltip "Save"
                           :icon :save
                           :on-click #(js/alert "save..")
                           :extra-props {:extra-class "test"}}
                          {:tooltip "Download"
                           :icon :download
                           :on-click #(js/alert "download..")}]
            :on-close #(js/alert "Close!")}]]
  {:title "Specific extra icons"})

(defexample
  [:div.frame.center-x.w-6-12.top-12
   (let [user-title
         (reagent/atom "My Frame Title has to contain an number like 123")]
     [header {:title "Fixed title prefix - "
              :icon :search
              :user-title user-title
              :on-title-set #(reset! user-title %)
              :title-validator (re-pattern "\\d")
              :on-close #(js/alert "Close!")}])]
  {:title "Editable title"})
