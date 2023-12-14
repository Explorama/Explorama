(ns de.explorama.frontend.algorithms.components.future-data
  (:require [react-window :refer [FixedSizeList]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [section input-field]]
            [de.explorama.frontend.algorithms.components.custom :refer [row]]
            [reagent.core :as reagent]
            [clojure.string :as str]))

(defn input-view [requirements future-data-state state settings-state current-algorithm translate-function
                  language-fuction {:keys [logging-value-changed]}]
  (let [default-future-values
        (reduce (fn [acc config]
                  (if (= :manual
                         (get-in config [:target-config :numeric :future-values :method]))
                    (assoc acc [config :numeric] true)
                    acc))
                {}
                requirements)
        manual-values
        (reduce (fn [acc [defined-as {attribute-config :attribute-config}]]
                  (reduce (fn [acc [attribute {{{method :value} :method :as future-values} :future-values}]]
                            (if (or (= method :manual)
                                    (and (not future-values)
                                         (get default-future-values [defined-as :numeric])))
                              (assoc acc attribute defined-as)
                              acc))
                          acc
                          attribute-config))
                {}
                (get @settings-state current-algorithm))
        manual-value-name-set (set (keys manual-values))
        {:keys [read-only?]} @settings-state]
    (when-not (empty? manual-value-name-set)
      [section {:label (translate-function :future-data-section)
                :default-open? false}
       (into [:<>]
             (for [[{:keys [problem-type algorithm country]} {:keys [future-data future-header future-mapping]}] @state]
               [:div
                (when problem-type
                  [row
                   (translate-function :problem-type)
                   (translate-function problem-type)])
                (when algorithm
                  [row
                   (translate-function :algorithm)
                   (translate-function algorithm)])
                (when (and country
                           (not= country :ignore))
                  [row
                   (translate-function :country)
                   country])
                [:div.prediction__data__list
                 (into
                  [:ul {:style {:width "100%"
                                :height 10}}]
                  (for [[num item] (map-indexed vector future-header)]
                    [:li {:style {:float :left
                                  :display :block
                                  :width 80}
                          :key (str (str "ki-data-preview-header-" num))}
                     item]))
                 [:> FixedSizeList
                  {:item-count (count future-data)
                   :height 150
                   :item-size 25}
                  (reagent/reactify-component
                   (fn [{:keys [index style]}]
                     (let [items (get future-data index (str (translate-function :no-element-at) index))]
                       (into
                        [:ul {:style style}]
                        (for [[num item-key] (map-indexed vector future-header)
                              :let [item-org (get items item-key)
                                    item (if (number? item-org)
                                           (.toLocaleString item-org (language-fuction))
                                           item-org)
                                    mapping-item (get future-mapping [item-key item-org])]]
                          [:li {:key (str "ki-data-preview-" num "-" index)
                                :style {:float :left
                                        :display :block
                                        :width 80}}
                           (if (manual-value-name-set item-key)
                             [input-field {:disabled? read-only?
                                           :on-change (fn [e]
                                                        (swap! future-data-state assoc-in [:future-data index item-key] e)
                                                        (logging-value-changed :future-data [:future-data index item-key] e))
                                           #_#_:label (translate-function :step)
                                           :thousand-separator (translate-function :thousand-separator)
                                           :decimal-separator  (translate-function :decimal-separator)
                                           :type :number
                                           :value (get-in @future-data-state [:future-data index item-key])
                                           :step 0.1
                                           :extra-class "input--w8"
                                           :placeholder "?"}]
                             (if-let [mapping-item (if (number? mapping-item)
                                                     (.toLocaleString mapping-item (language-fuction))
                                                     mapping-item)]
                               (str item " (" (str/join " " mapping-item) ")")
                               item))])))))]]]))])))
