(ns de.explorama.frontend.ui-base.overview.formular.card
  (:require [de.explorama.frontend.ui-base.components.formular.card :refer [card default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]
            [de.explorama.frontend.ui-base.utils.data-exchange :refer [download-content edn-content-type]]))

(defcomponent
  {:name "Card"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [card]]"
   :desc "Standard formular component for displaying something as card"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [card {:type :text
         :content "my text card"}]
  {:title "Text card"})

(defexample
  [card {:type :button
         :aria-label "Example button"
         :icon :bell
         :icon-params {:color :teal
                       :size :xxl}
         :on-click #(js/alert "clicked!")}]
  {:title "Icon-Button card"})

(defexample
  [card {:type :button
         :title "Title"
         :content "content"
         :on-click #(js/alert "clicked!")}]
  {:title "Text-Button card"})

(defexample
  [card {:type :button
         :icon :search
         :icon-params {:size :3xl}
         :title [:h2 "Search"]
         :content "Start your analysis with the definition of a data set."
         :on-click #(js/alert "clicked!")}]
  {:title "Text+Icon-Button card"})

(defexample
  [card {:type :button
         :icon :search
         :icon-position :end
         :icon-params {:size :3xl}
         :title [:h2 "Search"]
         :content "Start your analysis with the definition of a data set."
         :on-click #(js/alert "clicked!")}]
  {:title "Text+Icon-Button card - End icon"})

(defexample
  [card {:type :button
         :icon :bell
         :orientation :vertical
         :title "Title"
         :content "content"
         :on-click #(js/alert "clicked!")}]
  {:title "Vertical Text+Icon-Button card"})

(defexample
  [card {:type :button
         :icon :bell
         :icon-position :end
         :orientation :vertical
         :title "Title"
         :content "content"
         :on-click #(js/alert "clicked!")}]
  {:title "Vertical Text+Icon-Button card  - End icon"})

(defexample
  [card {:type :button
         :show-divider? false
         :icon "icon-tour"
         :icon-position :end
         :orientation :vertical
         :title "Product Tour"
         :on-click #(js/alert "clicked!")}]
  {:title "Product Tour"})

(defexample
  [card {:type :button
         :show-divider? false
         :icon "icon-charts-line"
         :icon-params {:size :3xl}
         :orientation :vertical
         :title "Line Chart"
         :on-click #(js/alert "clicked!")}]
  {:title "Line Chart"})

(defexample
  [card {:type :button
         :show-divider? false
         :disabled? true
         :icon "icon-charts-line"
         :icon-params {:size :3xl}
         :orientation :vertical
         :title "Line Chart"
         :on-click #(js/alert "clicked!")}]
  {:title "Line Chart - disabled"})

(defexample
  [card {:type :carousel
         :items (mapv (fn [i]
                        {:title (str "Title " i)
                         :content (str "Content " i)})
                      (range 1 6))}]
  {:title "Carousel card"})

(defexample
  [card {:type :carousel
         :items (mapv (fn [i]
                        (str "Content " i))
                      (range 1 6))}]
  {:title "Carousel card - Simple"})

(defexample
  [:div {:style {:height "200px"}}
   [card {:type :carousel
          :full-height? true
          :items (mapv (fn [i]
                         (str "Content " i))
                       (range 1 6))}]]
  {:title "Carousel card - Full height"})

(defexample
  [card {:type :carousel
         :auto-slide? true
         :slide-timeout-ms 1000
         :items (mapv (fn [i]
                        {:title (str "Title " i)
                         :content (str "Content " i)})
                      (range 1 6))}]
  {:title "Carousel card - Auto-slide"})

(defexample
  [card {:type :carousel
         :auto-slide? true
         :slide-timeout-ms 1000
         :slide-direction :left
         :items (mapv (fn [i]
                        {:title (str "Title " i)
                         :content (str "Content " i)})
                      (range 1 6))}]
  {:title "Carousel card - Auto-slide left-side"})

(defexample
  [card {:type :childs}
   [:div "My custom card content"]
   [:div.grid.grid-cols-2.gap-16.mb-64
    [card {:type :text
           :content "my text card"}]
    [card {:type :button
           :icon :bell
           :title "Title"
           :content "content"
           :on-click #(js/alert "clicked!")}]]]
  {:title "Card with childs"})