(ns de.explorama.backend.algorithms.apache.lr
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.algorithms.apache.regression.lr :as lr]
            [de.explorama.backend.algorithms.data.algorithm.core :as algo]
            [de.explorama.backend.algorithms.data.features.core :as feat]
            [de.explorama.backend.algorithms.temp.explorama-adapter :as temp]
            [de.explorama.shared.common.test-data :as td]))

(def attributes-structure-1
  {:algorithm :lr-apache
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :ignore
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def result-1
  [{"date" "2003-01-01", td/fact-1 14.5}
   {"date" "2004-01-01", td/fact-1 16.2}
   {"date" "2005-01-01", td/fact-1 17.9}])

(def data-1
  [{"date" "1999-10-13" td/fact-1 0
    "notes" "stuff"}
   {"date" "1999-10-14" td/fact-1 3
    "notes" "stuff"}
   {"date" "1999-11-13" td/fact-1 1
    "notes" "stuff"}
   {"date" "1999-12-31" td/fact-1 3
    "notes" "stuff"}
   {"date" "2000-01-05" td/fact-1 5
    "notes" "stuff"}
   {"date" "2000-05-06" td/fact-1 8
    "notes" "stuff"}
   {"date" "2002-01-13" td/fact-1 2
    "notes" "stuff"}
   {"date" "2002-02-13" td/fact-1 3
    "notes" "stuff"}
   {"date" "2002-02-14" td/fact-1 4
    "notes" "stuff"}
   {"date" "2002-02-15" td/fact-1 6
    "notes" "stuff"}
   {"date" "2001-02-15" td/fact-1 6
    "notes" "stuff"}])

(deftest simple-lr-test-case
  (let [task attributes-structure-1
        data data-1
        training-data
        (->> (feat/transform task data)
             (algo/transform task))
        options (temp/calculate-options data #{})
        model-result
        (lr/create-model (assoc task
                                :task-id "1"
                                :datainstance-id "1"
                                :training-data training-data
                                :options options
                                :country :ignore))
        execute-result
        (lr/execute-model model-result)]
    (testing "input data"
      (is (= (mapv #(dissoc % "id") (:prediction-data execute-result))
             result-1)))))
