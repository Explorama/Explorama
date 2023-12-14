
(ns de.explorama.frontend.ui-base.overview.frames.vertical-frame
  (:require [de.explorama.frontend.ui-base.components.frames.vertical-frame :refer [vertical-frame default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.components.frames.header :refer [header]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [reagent.core :as reagent]
            [clojure.string :as clj-str]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Vertical Frame (deprecated)"
   :desc "A basic explorama frame (vertical) container which provides basic dom-structure and handles propagation of drag-events automatically"
   :require-statement "[de.explorama.frontend.ui-base.components.frames.core :refer [vertical-frame]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [vertical-frame {:frame-class? true
                   :extra-props {:style {:position :relative
                                         :height "fit-content"}}}
   [header {:title "My Frame Title"}]
   [:div.body
    "body content"]]
  {:title "Basic Frame"})

(defexample
  (let [extra-props {:style {:position :relative
                             :display :inline-block
                             :width "30%"
                             :height "fit-content"}}]
    [:<>
     [vertical-frame {:frame-class? true
                      :extra-props extra-props}
      [header {:title "My Frame Title"}]
      [:div.body
       "body content"]]
     "    "
     [vertical-frame {:extra-props extra-props}
      [header {:title "My Frame Title"}]
      [:div.body
       "body content"]]
     "    "
     [:div.frame
      extra-props
      [vertical-frame
       [header {:title "My Frame Title"}]
       [:div.body
        "body content"]]]])
  {:title "Variations for basic usage"})

(defexample
  [vertical-frame {:frame-class? ["explorama__window__group-3" "example-class-2"]
                   :extra-props {:style {:position :relative
                                         :height "fit-content"}}}
   [header {:title "My Frame Title"}]
   [:div.body
    "body content"]]
  {:title "Multiple frame classes"})

(defn- add-layer [bubble-area layer-string]
  (cond (some #{"1: window body" "2: red area" layer-string} @bubble-area)
        (reset! bubble-area [layer-string])
        :else
        (swap! bubble-area conj layer-string)))

(defn- propagation-vis [ignore-child-events? propagation-order]
  [:div {:style {:background-color "lightgray"
                 :padding 5
                 :width "100%"}}
   [button {:label "Toggle ignore-child-events?"
            :on-click #(do
                         (reset! propagation-order [])
                         (swap! ignore-child-events? not))}]
   [:br] [:br]
   (str "Event-Propagation (ignore-child-events? = "
        @ignore-child-events?
        ")")
   [:br]
   (clj-str/join " ↣ " @propagation-order)])

(defexample
  (let [ignore-child-events? (reagent/atom false)
        propagation-order (reagent/atom [])]
    [:<>
     [propagation-vis ignore-child-events? propagation-order]
     [:br]
     [vertical-frame {:ignore-child-events? ignore-child-events?
                      :force-on-ignore {:on-mouse-up #(add-layer propagation-order "0: vertical-frame")
                                        :style {:cursor :grab}}
                      :frame-class? true
                      :extra-props {:style {:position :relative
                                            :height "fit-content"}}}
      [header {:title "My Frame Title"}]
      [:div.body {:on-mouse-up #(add-layer propagation-order "1: window body")}
       [:div {:style {:height "30px"
                      :background-color "#b7d58b"}
              :on-mouse-up #(add-layer propagation-order "2: green area")}
        "click this area (bubble)"]
       [:div {:style {:height "30px"
                      :background-color "#d58b8b"}
              :on-mouse-up (fn [e]
                             (.stopPropagation e)
                             (add-layer propagation-order "2: red area"))}
        "click this area (prevents on-mouse-up event)"]]]])
  {:title "Ignoring child events"
   :desc [:<> "Shows how :ignore-child-events? works. Sometimes children like divs, canvases, etc stops bubbling of events, which are necessary on parents of them. Example: You need to react on on-mouse-down on whole vertical-frame dom-layer, but some child stops the propagation of it."
          [:br] [:br]
          [:b "Important: Events of childs don't work anymore when :ignore-child-events? is true!"]]})
