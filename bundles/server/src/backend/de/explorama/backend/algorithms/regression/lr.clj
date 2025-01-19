(ns de.explorama.backend.algorithms.regression.lr
  (:require [de.explorama.backend.algorithms.algorithm :as alg]
            [de.explorama.backend.algorithms.data.features
             :refer [adjust-data-for-plot future-data restore-event
                     restore-event-config]]
            [de.explorama.backend.algorithms.utils :refer [measure-prediction]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [taoensso.timbre :refer [debug]])
  (:import [org.apache.commons.math3.stat.regression SimpleRegression]))

(defn generate-function [instance dependent-variable independent-variable]
  [(.getIntercept instance) [(.getSlope instance)] (str dependent-variable " = "
                                                        (.getIntercept instance)
                                                        " + "
                                                        (.getSlope instance)
                                                        " * "
                                                        independent-variable)])

(defn backdated-calc [attributes dependent-func-val independent-func-vals data]
  (let [independent-variables (mapv #(get % :value) (get attributes :independent-variable))
        dependent-variable (-> attributes :dependent-variable first :value)]
    (map (fn [event]
           (let [independent-values  (mapv #(get event %) independent-variables)]
             (-> (assoc event "id" (cljc-uuid))
                 (assoc dependent-variable (+ dependent-func-val
                                              (apply + (map #(* %1 %2) independent-func-vals independent-values)))))))
         data)))

(defn create-model [task]
  task)

(defn execute-model [{:keys [training-data attributes parameter algorithm task-id] :as task}]
  (debug "execute-model" task)
  (try
    (let [{ignore-backdated? :ignore-backdated?} parameter
          dependent-variable (get-in attributes [:dependent-variable 0 :value])
          independent-variable (get-in attributes [:independent-variable 0 :value])
          future-data-input (future-data task)
          future-data-input-data
          (mapv (fn [data]
                  (get data independent-variable))
                (:data future-data-input))
          training-data
          (->> (mapv (fn [data]
                       (double-array
                        [(get data independent-variable)
                         (get data dependent-variable)]))
                     (:data training-data))
               (into-array (Class/forName "[D")))
          instance (SimpleRegression.)
          _ (.addData instance training-data)

          result (mapv (fn [value]
                         (.predict instance (double value)))
                       future-data-input-data)
          {:keys [future-data-input-data] :as restore-event-config} (restore-event-config attributes future-data-input)
          prediction-result (map-indexed (fn [idx event-value]
                                           (let [input-event (get future-data-input-data idx)]
                                             (restore-event restore-event-config
                                                            (merge {"id" (cljc-uuid)
                                                                    dependent-variable event-value}
                                                                   input-event))))
                                         result)
          prediction-statistics [{:name "R2"
                                  :value (.getRSquare instance)}
                                 {:name "MSE"
                                  :value (.getMeanSquareError instance)}
                                 {:name "Standard Error (Slope)"
                                  :value (.getSlopeStdErr instance)}
                                 {:name "Standard Error (Intercept)"
                                  :value (.getInterceptStdErr instance)}]
          [dependent-func-val independent-func-vals prediction-function] (generate-function instance dependent-variable independent-variable)
          prediction-input-raw-data (filterv seq
                                             (map (fn [datapoint]
                                                    (apply (partial assoc
                                                                    {(attrs/access-key "id") (cljc-uuid)
                                                                     (attrs/access-key dependent-variable) (get datapoint dependent-variable)})
                                                           (apply concat (map #(list (attrs/access-key %) (get datapoint %)) [independent-variable]))))
                                                  (:data (:training-data task))))
          prediction-input-data (adjust-data-for-plot attributes (:training-data task) prediction-input-raw-data false)
          backdated-forecast-calc  (when-not ignore-backdated? (backdated-calc attributes dependent-func-val independent-func-vals prediction-input-raw-data))
          backdated-forecast-data (when-not ignore-backdated? (adjust-data-for-plot attributes (:training-data task) backdated-forecast-calc false))]
      (assoc task
             :prediction-execution-time (System/currentTimeMillis)
             :model-id task-id
             :prediction-id  task-id
             :prediction-data
             (vec prediction-result)
             :prediction-statistics (measure-prediction algorithm prediction-statistics)
             :prediction-function prediction-function
             :prediction-input-data prediction-input-data
             :backdated-forecast-data (conj backdated-forecast-data (first prediction-result))))
    (catch Exception e
      (debug e)
      (assoc task
             :model-id task-id
             :prediction-error (cond
                                 :else
                                 [:unknown-error])))))

(deftype LinearRegression []
  alg/Algorithm
  (algorithm-key [_] :linear-regression)
  (create-model [_ task]
    task)
  (execute-model [_ task]
    (execute-model task)))

(defn create-instance []
  (LinearRegression.))
