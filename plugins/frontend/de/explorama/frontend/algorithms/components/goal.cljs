(ns de.explorama.frontend.algorithms.components.goal
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [select checkbox section]]
            [de.explorama.frontend.ui-base.components.common.core :refer [label tooltip]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.algorithms.components.helper :refer [keyword-translate-options]]
            [reagent.core :as reagent]
            [clojure.string :as str]))

(defn visualize-requirements [requirements translate-function]
  (str
   (translate-function :vr-require)
   " "
   (str/join (str " " (translate-function :vr-and) " ")
             (for [{:keys [types defined-as number]} requirements]
               (str (case number
                      :single (translate-function :vr-one)
                      :multi (translate-function :vr-one-or-multiple)) " "
                    (translate-function defined-as)
                    ""
                    (case number
                      :single (translate-function :vr-singular)
                      :multi (translate-function :vr-plural))
                    " (" (->> types
                              (mapv translate-function)
                              (str/join ", ")) ")")))))

(defn icon-option [options translate check-requirements]
  (mapv (fn [[key {:keys [requirements]}]]
          {:label
           [:div.flex
            [:span {:class "input--w14"
                    :style {:white-space :nowrap
                            :overflow :hidden
                            :text-overflow :ellipsis}}
             (translate key)]
            (if (check-requirements requirements)
              [icon {:icon :check}]
              [:svg {:style {:padding-left 5
                             :padding-top 5}
                     :width 20
                     :height 20}
               [:polygon {:points "20,0 0,20 30,50 0,80 20,100 50,70 80,100 100,80 70,50 100,20 80,0 50,30"
                          :transform "scale(0.15, 0.15)"
                          :style {:fill "red"}}]])]
           :title (translate key)
           :value key})
        options))

(defn- algorithm-selection [state parameter-desc check-requirements translate-function read-only? changed-functions]
  (let [info-state (reagent/atom false)]
    (fn [state parameter-desc check-requirements translate-function read-only?
         {:keys [training-data-change logging-value-changed]}]
      (let [{:keys [current-algorithm]} @state
            {:keys [desc-key requirements]} (get parameter-desc current-algorithm)
            parameter-desc (filterv #(not (get % :hidden?)) parameter-desc)
            values (get @state :choose-algorithm)
            info-tooltip (translate-function :toggle-desc-button)
            values (if (empty? values)
                     (let [values (or (second (icon-option parameter-desc translate-function check-requirements)) ; this will select pnr with the current config
                                      (first (icon-option parameter-desc translate-function check-requirements)))]
                       (swap! state assoc :choose-algorithm values)
                       (swap! state assoc :current-algorithm (:value values))
                       (training-data-change)
                       (logging-value-changed :goal [:choose-algorithm] values)
                       (logging-value-changed :goal [:current-algorithm] (:value values)))
                     values)]
        [:<>
         [:div.emphasized
          [:div.explorama__form__select
           [select
            {:label (translate-function :choose-algorithm-div)
             :is-clearable? false
             :tooltip-key :title
             :on-change (fn [e]
                          (swap! state assoc :choose-algorithm e)
                          (swap! state assoc :current-algorithm (:value e))
                          (logging-value-changed :goal [:choose-algorithm] e)
                          (logging-value-changed :goal [:current-algorithm] (:value e))
                          (training-data-change))
             :options (icon-option parameter-desc translate-function check-requirements)
             :placeholder (translate-function :select-placeholder)
             :values values
             :menu-row-height 35
             :extra-class "input--w20"
             :disabled? read-only?}]
           (when (or desc-key requirements)
             [:a {:href "#"
                  :aria-label info-tooltip
                  :on-click #(swap! info-state not)}
              [tooltip {:text info-tooltip}
               [icon {:icon :info-circle
                      :color :gray
                      :brightness 5}]]])
           [:span.explorama__input__mode.input--w4
            {:on-click (fn [_]
                         (when-not read-only?
                           (swap! state assoc :choose-algorithm? false)
                           (logging-value-changed :goal [:choose-algorithm?] false)
                           (training-data-change)))}
            (translate-function :choose-problem-type-action)]]]
         (when (and (or desc-key requirements) @info-state)
           [:div.emphasized
            (when desc-key [:p (translate-function desc-key)])
            (when requirements [:p [visualize-requirements requirements translate-function]])])]))))

(defn- problem-type-selection [state problem-types check-requirements translate-function changed-functions]
  (let [info-state (reagent/atom false)]
    (fn [state problem-types check-requirements translate-function
         {:keys [training-data-change logging-value-changed]}]
      (let [{{problem-type :value} :problem-type read-only? :read-only?} @state
            {:keys [requirements desc-key]} (get @problem-types problem-type)
            info-tooltip (translate-function :toggle-desc-button)
            problem-type-options (icon-option (->> @problem-types
                                                   (sort-by (comp :sort second)
                                                            <)
                                                   vec)
                                              translate-function
                                              check-requirements)]
        [:<>
         [:div.emphasized
          [:div.explorama__form__select
           (let [path [:problem-type]
                 value (get-in @state path [])
                 value (if (empty? value)
                         (let [value (first problem-type-options)]
                           (swap! state assoc-in path value)
                           (training-data-change)
                           (logging-value-changed :goal path value))
                         value)]
             [select {:tooltip-key :title
                      :is-clearable? false
                      :label (translate-function :problem-type-select)
                      :on-change (fn [e]
                                   (swap! state assoc-in path e)
                                   (training-data-change)
                                   (logging-value-changed :goal path e))
                      :options problem-type-options
                      :placeholder (translate-function :select-placeholder)
                      :values value
                      :menu-row-height 35
                      :extra-class "input--w20"
                      :disabled? read-only?}])
           (when (or desc-key requirements)
             [:a {:href "#"
                  :aria-label info-tooltip
                  :on-click #(swap! info-state not)}
              [tooltip {:text info-tooltip}
               [icon {:icon :info-circle
                      :color :gray}]]])
           [:span.explorama__input__mode.input--w4
            {:on-click (fn [_]
                         (when-not read-only?
                           (swap! state assoc :choose-algorithm? true)
                           (logging-value-changed :goal [:choose-algorithm?] true)
                           (training-data-change)))}
            (translate-function :choose-algorithm-action)]]]
         (when (and (or desc-key requirements) @info-state)
           [:div.emphasized
            (when desc-key [:p (translate-function desc-key)])
            (when requirements [:p [visualize-requirements requirements translate-function]])])]))))

(defn view [parameter-desc problem-types
            state translate-function
            changed-functions
            {:keys [check-requirements]
             :or {check-requirements (constantly false)}}]
  (let [{:keys [choose-algorithm? read-only?]} @state]
    (if choose-algorithm?
      [algorithm-selection state parameter-desc check-requirements translate-function read-only? changed-functions]
      [problem-type-selection state problem-types check-requirements translate-function changed-functions])))
