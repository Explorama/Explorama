(ns de.explorama.frontend.algorithms.components.reduced-result
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.formular.core :refer [loading-message]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.algorithms.path.core :as path]
            [de.explorama.frontend.algorithms.components.charts :as charts]
            [de.explorama.frontend.algorithms.components.result :as result]))

(defn- chart-data [prediction procedures translate-function language-function attribute-labels]
  (let [{:keys [algorithm
                attributes
                prediction-data
                prediction-input-data
                backdated-forecast-data]}
        prediction]
    (result/prediction-data->chart-data
     procedures
     algorithm
     attributes
     prediction-data
     language-function
     prediction-input-data
     backdated-forecast-data
     translate-function
     attribute-labels)))

(re-frame/reg-event-db
 ::update-settings
 (fn [db [_ frame-id settings]]
   (assoc-in db (path/reduced-settings frame-id) settings)))

(re-frame/reg-sub
 ::settings
 (fn [db [_ frame-id]]
   (get-in db (path/reduced-settings frame-id))))

(defn reduced-view [frame-id infos-sub translate-function language-function]
  (let [{size :size} @infos-sub
        procedures @(re-frame/subscribe [:de.explorama.frontend.algorithms.components.main/procedures])
        attribute-labels @(fi/call-api [:i18n :get-labels-sub])
        {:keys [selection legend]} @(re-frame/subscribe [::settings frame-id])
        {predictions :predictions} @(re-frame/subscribe [:de.explorama.frontend.algorithms.components.main/result frame-id])]
    [:div.window__body {:style {:height (second size)
                                :width (first size)}}
     (if predictions
       (reduce (fn [acc {:keys [algorithm prediction-error test-measure prediction-id] :as prediction}]
                 (if-not (= algorithm (or selection (get (first predictions) :algorithm)))
                   acc
                   (conj acc
                         (if prediction-error
                           ^{:key (str "reporting-dr-ki-error-" prediction-id)}
                           [:div (translate-function (first prediction-error))]
                           (if (= :text (get-in procedures [algorithm :result-type]))
                             ^{:key (str "reporting-dr-ki-module-text" prediction-id)}
                             [result/text-result-view
                              translate-function
                              (first test-measure)
                              (second test-measure)]
                             ^{:key (str "reporting-dr-ki-module-chart" prediction-id)}
                             [charts/view frame-id
                              (str "ki-dashboard-" algorithm "-" frame-id)
                              (chart-data prediction procedures translate-function language-function attribute-labels)
                              size])))))
               [:<>]
               predictions)
       [loading-message {:show? true
                         :message (translate-function :prediction-running)}])]))

(re-frame/reg-sub
 ::available-options
 (fn [db [_ frame-id]]
   (let [{:keys [problem-type]} (get-in db (path/prediction-task frame-id))
         option-values (get-in db (conj path/problem-types problem-type :algorithms))
         option-names @(re-frame/subscribe (into [::i18n/translate-multi] option-values))]
     (mapv
      #(hash-map :value % :label (get option-names %))
      option-values))))
