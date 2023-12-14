(ns de.explorama.frontend.algorithms.components.prediction
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                section
                                                                loading-message]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.algorithms.components.custom :refer [options-hidden]]
            [de.explorama.frontend.algorithms.components.data :as d]
            [de.explorama.frontend.algorithms.components.future-data :as f]
            [de.explorama.frontend.algorithms.components.goal :as g]
            [de.explorama.frontend.algorithms.components.helper :as helper]
            [de.explorama.frontend.algorithms.components.parameter :as p]
            [de.explorama.frontend.algorithms.components.result :as result]
            [de.explorama.frontend.algorithms.components.settings :as s]))

(defn train-data-not-valid? [train-data]
  (or (some (fn [[_ {error :train-error}]]
              error)
            train-data)
      (not train-data)))

(defn view [frame-id procedures problem-types options data result result-fn prediction is-predicting? training-data-loading?
            translate-function language-function
            {:keys [submit training-data-change logging-value-changed data-changed data-changed-sub]}
            {:keys [goal-state settings-state parameter-state simple-parameter-state future-data-state params-valid-state]}
            read-only?]
  [:div
   (let [{:keys [current-algorithm choose-algorithm? problem-type]} @goal-state
         changed-functions
         {:training-data-change (partial training-data-change
                                         goal-state
                                         settings-state
                                         parameter-state
                                         simple-parameter-state
                                         future-data-state)
          :logging-value-changed logging-value-changed}
         current-algorithm (if choose-algorithm?
                             current-algorithm
                             (:value problem-type))
         {param-desc :parameter
          simple-parameter-desc :simple-parameter
          requirements :requirements
          button-label :button-label
          :as current-algorithm-desc}
         (if choose-algorithm?
           (get @procedures current-algorithm)
           (get @problem-types current-algorithm))
         read-only? (or (nil? @options)
                        @read-only?)
         attribute-labels @(fi/call-api [:i18n :get-labels-sub])
         params-valid? (every? second (get @params-valid-state current-algorithm))
         key-func (fn [& elements] (str frame-id "-" (apply str (interpose "-" elements))))]
     ;hack start
     (swap! goal-state assoc :read-only? read-only?)
     (swap! settings-state assoc :read-only? read-only?)
     (swap! parameter-state assoc :read-only? read-only? :section? true :label-key :advanced-settings-div)
     (swap! simple-parameter-state assoc :read-only? read-only? :section? false :label-key :simple-advanced-settings-div)
     (when @data-changed-sub
       ((:training-data-change changed-functions))
       (data-changed false))
     ;hack end
     [section {:default-open? true
               :label (translate-function :model-settings)
               :footer [:div.w-full.flex.justify-end
                        [:div.prediction__save__action
                         [button {:label     (translate-function (or button-label :predict-button))
                                  :disabled? (or read-only?
                                                 (not current-algorithm)
                                                 (train-data-not-valid? @data)
                                                 @is-predicting?
                                                 (not params-valid?))
                                  :on-click  #(when submit (submit goal-state settings-state parameter-state simple-parameter-state future-data-state))}]
                         [loading-message {:show?   is-predicting?
                                           :message (translate-function :prediction-running)}]]]}
      [:div.input-data-section.settings__section--new
       [g/view @procedures problem-types goal-state translate-function changed-functions
        {:check-requirements (fn [requirements]
                               (helper/validate-requirements @options requirements))}]
       (when current-algorithm
         [:<>
            ;variable selection + simple settings
          [:div.row
           (for [column [:left :right]]
             ^{:key (key-func "var-selection" column)}
             [:div.col-6
              [s/view current-algorithm-desc current-algorithm options settings-state translate-function changed-functions {:simple? true
                                                                                                                            :attribute-labels attribute-labels
                                                                                                                            :column  column}]
              [p/view simple-parameter-desc current-algorithm-desc current-algorithm options simple-parameter-state settings-state translate-function changed-functions params-valid-state {:simple? true
                                                                                                                                                                                            :column  column
                                                                                                                                                                                            :parameter-key :simple-parameter}]])]
          [options-hidden
           translate-function
              ;variable settings
           [:div.row
            (for [column [:left :right]]
              ^{:key (key-func "var-settings" column)}
              [:div.col-6
               [s/view current-algorithm-desc current-algorithm options settings-state translate-function changed-functions {:simple? false
                                                                                                                             :column  column}]])]
              ;parameter settings
           [:div.options__divider]
           [:div.explorama__form__section
            [:div.title (translate-function :parameter)]]
           [:div.row
            (for [column [:left :right]]
              ^{:key (key-func "parameter-settings_" column)}
              [:div.col-6
               [p/view param-desc nil current-algorithm options parameter-state settings-state translate-function changed-functions params-valid-state {:simple? false
                                                                                                                                                        :column  column
                                                                                                                                                        :parameter-key :parameter}]])]]])
       [f/input-view requirements future-data-state data settings-state current-algorithm translate-function language-function changed-functions]]])
   [d/input-view data training-data-loading? translate-function language-function]
   (when @result
     [result/react-view
      frame-id
      procedures
      result
      prediction
      translate-function
      language-function
      result-fn])])

(def react-view view) ; just to have a consistent name
