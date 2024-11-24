(ns de.explorama.frontend.ui-base.overview.formular.button-group
  (:require [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.components.formular.button-group :refer [button-group default-parameters parameter-definition button-definition]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defutils defexample]]
            [cljs.spec.alpha :as spec]))

(defcomponent
  {:name "Button-Group"
   :desc "Standard formular component to create a group of button, depending on the given parameter it can
          be single and multi select."
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [button-group]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defutils
  {:name "Button"
   :is-sub? true
   :section "components"
   :desc "A single item"
   :parameters button-definition})

(defexample
  [button-group {:items [{:label "Foo"
                          :id "1"}
                         {:label "Bar"
                          :id "2"}]}]
  {:title "Simple list"})

(defexample
  (let [active-item (reagent/atom nil)
        items (mapv (fn [v]
                      {:label (str "T" v)
                       :id (str v)
                       :on-click (fn []
                                   (reset! active-item v))})
                    (range 0 5))]
    [button-group {:items items
                   :active-item active-item}])
  {:title "Single select"})

(defexample
  (let [active-items (reagent/atom [])
        items (mapv (fn [v]
                      {:label (str "T" v)
                       :id (str v)
                       :on-click (fn []
                                   (swap! active-items (fn [active] (if (some #{v} active)
                                                                      (filter #(not= v %) active)
                                                                      (conj active v)))))})
                    (range 0 5))]
    [button-group {:items items
                   :active-items active-items}])
  {:title "Multi select"})

(defexample
  (let [active-items (reagent/atom [])
        icons {0 :mosaic2 1 :map 2 :charts 3 :table 4 :head-cogs}
        items (mapv (fn [v]
                      {:label [icon {:icon (get icons v)}]
                       :title (name (get icons v))
                       :compact? true
                       :id (str v)
                       :on-click (fn []
                                   (swap! active-items (fn [active] (if (some #{v} active)
                                                                      (filter #(not= v %) active)
                                                                      (conj active v)))))})
                    (range 0 5))]
    [button-group {:items items
                   :active-items active-items}])
  {:title "Compact list"})

(defexample
  (let [active-item (reagent/atom nil)
        items (mapv (fn [v]
                      {:label (str "T" v)
                       :id (str v)
                       :on-click (fn []
                                   (reset! active-item v))})
                    (range 0 5))]
    [button-group {:items items
                   :disabled? true
                   :active-item active-item}])
  {:title "Disabled button-group"})

(defexample
  (let [active-item (reagent/atom nil)
        items (mapv (fn [v]
                      {:label (str "T" v)
                       :id (str v)
                       :disabled? (= v 2)
                       :on-click (fn []
                                   (reset! active-item v))})
                    (range 0 5))]
    [button-group {:items items
                   :active-item active-item}])
  {:title "Disabled single-item"})