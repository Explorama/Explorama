(ns de.explorama.frontend.ui-base.overview.misc.icon
  (:require [reagent.core :as r]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon icon-collection default-parameters colors sizes parameter-definition]]
            [de.explorama.frontend.ui-base.utils.select :refer [to-option]]
            [de.explorama.frontend.ui-base.overview.renderer.infos :refer [md-code]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [select input-field slider button checkbox]]
            [clojure.string :as clj-str]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Icon"
   :desc "Component to display an icon"
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [icon]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defn- icon-demo [selected-icon selected-color selected-size selected-brightness selected-tooltip selected-color-important?]
  [icon (cond-> {}
          @selected-icon (assoc :icon (:value @selected-icon))
          @selected-color (assoc :color (:value @selected-color))
          @selected-color-important? (assoc :color-important? @selected-color-important?)
          @selected-size (assoc :size (get @selected-size :value @selected-size))
          @selected-brightness (assoc :brightness @selected-brightness)
          @selected-tooltip (assoc :tooltip @selected-tooltip))])

(defn- explorer-selections [selected-icon selected-color selected-size selected-brightness selected-tooltip selected-color-important?]
  [:div
   [:h3
    "Props    "
    [button {:start-icon :reset
             :aria-label "reset"
             :on-click (fn [_]
                         (reset! selected-icon (to-option (first (sort (keys icon-collection)))))
                         (reset! selected-color nil)
                         (reset! selected-color-important? nil)
                         (reset! selected-size nil)
                         (reset! selected-brightness nil)
                         (reset! selected-tooltip nil))}]]
   [select {:label "Icon"
            :extra-class "input--w20"
            :is-multi? false
            :values selected-icon
            :options (mapv #(to-option %)
                           (sort (keys icon-collection)))
            :on-change #(reset! selected-icon %)}]
   [select {:label "Color"
            :extra-class "input--w20"
            :is-multi? false
            :values selected-color
            :options (mapv #(to-option %)
                           (sort (keys colors)))
            :on-change #(reset! selected-color %)}]
   [checkbox {:label "Color Important?"
              :extra-class "input--w20"
              :checked? selected-color-important?
              :on-change #(reset! selected-color-important? %)}]
   [select {:label "Size (fixed)"
            :extra-class "input--w20"
            :is-multi? false
            :values (when (map? @selected-size)
                      selected-size)
            :options (mapv #(to-option %)
                           (sort (keys sizes)))
            :on-change #(reset! selected-size %)}]
   [slider {:label "Size (px)"
            :extra-class "input--w20"
            :auto-marks? true
            :auto-mark-count 6
            :min 0
            :max 300
            :show-number-input? false
            :value (if (number? @selected-size)
                     selected-size
                     0)
            :on-change #(reset! selected-size %)}]
   [slider {:label "Brightness"
            :value selected-brightness
            :extra-class "input--w20"
            :on-change #(reset! selected-brightness %)
            :auto-marks? true
            :auto-mark-count 10
            :show-number-input? false
            :min 0
            :max 10}]
   [input-field {:label "Tooltip"
                 :value selected-tooltip
                 :on-change #(reset! selected-tooltip %)}]])

(defn- code-preview [selected-icon selected-color selected-size selected-brightness selected-tooltip selected-color-important?]
  [md-code
   (str "[icon "
        (cond-> {}
          @selected-icon (assoc :icon (:value @selected-icon))
          @selected-color (assoc :color (:value @selected-color))
          @selected-color-important? (assoc :color-important? @selected-color-important?)
          @selected-size (assoc :size (get @selected-size :value @selected-size))
          @selected-brightness (assoc :brightness @selected-brightness)
          @selected-tooltip (assoc :tooltip @selected-tooltip))
        "]")])

(defexample
  (let [selected-icon (r/atom (to-option (first (sort (keys icon-collection)))))
        selected-color (r/atom nil)
        selected-size (r/atom nil)
        selected-brightness (r/atom nil)
        selected-tooltip (r/atom nil)
        selected-color-important? (r/atom nil)]

    [:<>
     [:div {:style {:width "100%"}}
      [:table
       [:tbody
        [:tr
         [:td
          [:div {:style {:width "350px"
                         :height "350px"
                         :overflow :auto
                         :display :inline-block
                         :text-align :center
                         :vertical-align :middle
                         :line-height "340px"
                         :margin-right "10px"
                         :background-color "#e9e9e9"}}
           [icon-demo selected-icon selected-color selected-size selected-brightness selected-tooltip selected-color-important?]]]
         [:td
          [:div {:style {:overflow :auto
                         :display :inline-block
                         :padding-right "50px"
                         :padding-left "30px"
                         :border "1px solid #d5d5d5"
                         :height "350px"}}
           [explorer-selections selected-icon selected-color selected-size selected-brightness selected-tooltip selected-color-important?]]]]]]]
     [:br]
     [:div
      [:h3 "Code"]
      [code-preview selected-icon selected-color selected-size selected-brightness selected-tooltip selected-color-important?]]])
  {:title "Icon Editor"
   :desc "Explore icon configurations"
   :show-code? false})

(defexample
  (reduce (fn [p [group icons]]
            (let [icons (sort icons)]
              (conj p
                    [:h2 {:style {:text-transform :capitalize}}
                     (cond-> group
                       (keyword? group) (name))]
                    (reduce (fn [sp [icon-key {:keys [class]}]]
                              (conj sp
                                    [:div {:style {:display :inline-block
                                                   :padding "5px"
                                                   :margin-right "10px"}}
                                     [:center
                                      [:code {:style {:font-size "10px"}}
                                       (str icon-key)]
                                      [:br]
                                      [icon {:icon class}]]]))
                            [:div {:style {:padding "0px 10px 5px 10px"
                                           :background-color "#d5d5d5"}}]
                            icons))))
          [:<>]
          (group-by #(clj-str/lower-case (get-in % [1 :group]))
                    icon-collection))
  {:title "Icon collection"
   :desc "Use a keyword from the following overview to use an existing icon"
   :show-code? false})

(defexample
  [icon {:icon :info-circle
         :tooltip "Your tooltip"}]
  {:title "Collection example"
   :bg-color "#d5d5d5"})

(defexample
  [icon {:icon "icon-info-circle"}]
  {:title "Class example"
   :bg-color "#d5d5d5"})

(defexample
  [icon {:icon :info-circle
         :color :orange
         :brightness 9
         :size :large}]
  {:title "Preset example"
   :desc "Preset overrides size, color, and brightness"})

(defexample
  (reduce (fn [r color]
            (conj r [icon {:icon :info-circle
                           :color color}]))
          [:div]
          (sort (keys colors)))
  {:title "Color example"
   :bg-color "#d5d5d5"})

(defexample
  [icon {:icon :info-circle
         :size 60
         :custom-color "rgba(126, 57,161 ,0.72)"}]
  {:title "Custom color example"
   :bg-color "#d5d5d5"})

(defexample
  (reduce (fn [r brightness]
            (conj r [icon {:icon :info-circle
                           :color :gray
                           :brightness brightness}]))
          [:div]
          (range 0 11))
  {:title "Brightness example"
   :bg-color "#d5d5d5"})

(defexample
  [icon {:icon :info-circle
         :size :large}]
  {:title "Size example (keyword)"
   :bg-color "#d5d5d5"})

(defexample
  [icon {:icon :info-circle
         :size 60}]
  {:title "Size example (number)"
   :bg-color "#d5d5d5"})

