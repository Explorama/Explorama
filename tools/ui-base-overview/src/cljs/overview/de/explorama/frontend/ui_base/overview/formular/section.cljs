(ns de.explorama.frontend.ui-base.overview.formular.section
  (:require [de.explorama.frontend.ui-base.components.formular.section :refer [section default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Section"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [section]]"
   :desc "Standard formular component for providing a collapsable section"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [section {:label "My example Section"
            :footer [:button "Footer"]}
   "My section content"]
  {:title "Section"})

(defexample
  [section {:label "My example Section"
            :default-open? false}
   [:div {:style {:width "200px"
                  :height "40px"
                  :background-color :black}}
    [:font {:style {:color :white}}
     "My section content"]]]
  {:title "Default close"})

(defexample
  [section {:label "My example Section"}
   [section {:label "My example Sub-Section"}
    [section {:label "My example Sub-Sub-Section"
              :footer [:button "Footer"]}
     "Nested content"]]]
  {:title "Nested Section"})

(defexample
  [section {:label "My example Section"}
   [:div.grid.grid-cols-3.gap-12
    [:div.col-span-2
     [section {:label "Column 1"
               :footer [:button "Footer"]}
      "Something"]]
    [:div.col-span-1
     [section {:label "Column 2"
               :footer [:button "Footer"]}
      "Something else"]]]]
  {:title "Column Subsections"})

(re-frame/reg-event-db
 :inc-counter
 (fn [db]
   (update db :open-counter (fn [o] (inc (or o 0))))))

(re-frame/reg-sub
 :open-counter
 (fn [db]
   (get db :open-counter 0)))

(defn section-content []
  (let [open-counter @(re-frame/subscribe [:open-counter])]
    (str "Open-Counter: " open-counter)))

(defexample
  [section {:label "My example Section"
            :on-change (fn [is-open?]
                         (when is-open?
                           (re-frame/dispatch [:inc-counter])))}
   [section-content]]
  {:title "On-change with re-frame"
   :code-before
   "(re-frame/reg-event-db
 :inc-counter
 (fn [db]
  (update db :open-counter (fn [o] (inc (or o 0))))))

(re-frame/reg-sub
 :open-counter
 (fn [db]
  (get db :open-counter 0)))

(defn section-content []
 (let [open-counter @(re-frame/subscribe [:open-counter])]
  (str \"Open-Counter: \" open-counter)))"})

(defexample
  [section {:label "My example Section"
            :disabled? true
            :close-on-disabled? false}
   "My section content"]
  {:title "Disabled open"})

(defexample
  [section {:label "My example Section"
            :disabled? true}
   "My section content"]
  {:title "Disabled close"})

(defexample
  [section {:label [:div.flex.gap-8 "My example Section" [icon {:icon :check :tooltip "Some kind of info"}]]}
   "My section content"]
  {:title "Komponent label"})

(defexample
  [section {:label "My example Section with Hint"
            :hint "Simple hint here :D"}
   "My section content"]
  {:title "With hint"})
