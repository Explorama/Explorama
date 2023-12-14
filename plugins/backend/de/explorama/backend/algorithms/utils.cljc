(ns de.explorama.backend.algorithms.utils
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [de.explorama.backend.algorithms.config :as config]))

(defn measure-prediction
  "Updates prediction-statistics-map to include a ':light :grey/:green/:yellow/:red' within selected statistic-maps, based upon quality-measure.edn settings.
   The worst of which is used for the actual traffic light, the others to mark the statistics themselves."
  [algorithm prediction-statistics]
  (let [alg-measure (get config/explorama-algorithms-quality-measures-config algorithm)
        model-measure (get config/explorama-algorithms-quality-measures-config (get alg-measure :model))
        measure (merge-with merge model-measure alg-measure)]
    (reduce
     (fn [result {:keys [name value] :as statistic}]
       (let [stat-measure (get-in measure [:prediction name])

             test-range (fn [[a b c] x]
                          (cond
                            (or (apply <= [b x c]) (apply >= [b x c])) :green
                            (or (apply <= [a x b]) (apply >= [a x b])) :yellow
                            :else :red))
             test-equal (fn [elements x]
                          (if (some true? (map #(= x %) elements))
                            :green
                            :red))

             light (if (and stat-measure value)
                     (case (get stat-measure :type)
                       :range (test-range (get stat-measure :value) value)
                       :compare (test-equal (get stat-measure :value) value)
                       :red)
                     :grey)
             header? (get stat-measure :header false)
             rounded-value (if (or (float? value)
                                   (double? value))
                             (edn/read-string (pp/cl-format nil (str "~," config/explorama-default-rounding-decimal-place "f") value))
                             value)]
         (conj result
               (assoc statistic :light light :header header? :value rounded-value))))
     []
     prediction-statistics)))

(defn measure-test
  "Determines both the sentence written underneath each test result and the icons displayed."
  [algorithm prediction-statistics]
  (let [found? (case algorithm
                 :trend (for [{:keys [name value]} prediction-statistics :when (= name "TREND")] (vector (= value 1.0) (= value -1.0)))
                 :seasonality (for [{:keys [name value]} prediction-statistics :when (= name "type")] (not= value "non-seasonal"))
                 :white-noise (for [{:keys [name value]} prediction-statistics :when (= name "WN")] (= value 1.0))
                 nil)]
    (case algorithm
      :trend (if (some true? (first found?))
               [(if (ffirst found?) :trend-up :trend-down) :contains-trend] ; [icon text-key]
               [:close :contains-no-trend])
      :seasonality (if (first found?)
                     [:check :contains-seasonality]
                     [:close :contains-no-seasonality])
      :white-noise (if (first found?)
                     [:check :contains-white-noise]
                     [:close :contains-no-white-noise])
      nil)))