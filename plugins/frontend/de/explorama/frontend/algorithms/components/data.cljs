(ns de.explorama.frontend.algorithms.components.data
  (:require [react-window :refer [FixedSizeList]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [section loading-message]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.algorithms.components.custom :refer [row]]
            [de.explorama.frontend.algorithms.components.subsection :as sub]
            [reagent.core :as reagent]
            [clojure.string :as str]))

(defn input-view [state loading? translate-function language-fuction]
  (let [attribute-labels @(fi/call-api [:i18n :get-labels-sub])]
    [section {:default-open? false
              :disabled? (or (nil? @state)
                             (empty? @state)
                             @loading?)
              :label [:div.flex.gap-8
                      (translate-function :input-data-section)
                      (when (every? (fn [[_ {e :train-error w :train-warning}]]
                                      (not (or e w)))
                                    @state)
                        [icon {:icon :check :color :green}])]}
     [:<>
      (when @loading?
        [loading-message {:show? @loading?
                          :message (translate-function :generating-training-data-running)}])
      (into [:<>]
            (for [[{:keys [problem-type algorithm country]}
                   {:keys [data header mapping]
                    {warn-label :warn-label :as warning?} :train-warning
                    {error-label :error-label :as error?} :train-error}]
                  @state]
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
               (when warning?
                 [sub/section {:label (translate-function :warning-section)}
                  [:p (translate-function warn-label)]])
               (when error?
                 [sub/section {:label (translate-function :error-section)}
                  [:p (translate-function error-label)]])
               [:div.prediction__data__list
                (into
                 [:ul {:style {:width "100%"
                               :height 10}}]
                 (for [[num item] (map-indexed vector header)]
                   [:li {:style {:float :left
                                 :display :block
                                 :width 80}
                         :key (str (str "ki-data-preview-header-" num))}
                    (get attribute-labels item item)]))
                [:> FixedSizeList
                 {:item-count (count data)
                  :height 200
                  :item-size 25}
                 (reagent/reactify-component
                  (fn [{:keys [index style]}]
                    (let [items (get data index (str (translate-function :no-element-at) index))]
                      (into
                       [:ul {:style style}]
                       (for [[num item-key] (map-indexed vector header)
                             :let [item-org (get items item-key)
                                   item (if (number? item-org)
                                          (.toLocaleString item-org (language-fuction))
                                          item-org)
                                   mapping-item (get mapping [item-key item-org])]]
                         [:li {:key (str "ki-data-preview-" num "-" index)
                               :style {:float :left
                                       :display :block
                                       :width 80}}
                          (if-let [mapping-item (if (number? mapping-item)
                                                  (.toLocaleString mapping-item (language-fuction))
                                                  mapping-item)]
                            (str item " (" (str/join " " mapping-item) ")")
                            item)])))))]]]))]]))
