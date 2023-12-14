(ns de.explorama.frontend.woco.operations-test
  (:require [clojure.test :refer [deftest testing is run-tests]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.operations :as op]))

(def di-1
  #:di{:data-tile-ref
       {"0889c83d983d8a7a1f762efb419c05d5c8ecd6d9db05e0cd223d9b6284719167"
        {:di/identifier "search"
         :formdata
         "[[[\"datasource\" \"Datasource\"] {:values [\"datasource-b\" \"datasource-a\"], :timestamp 1589543742430, :valid? true}] [[\"country\" \"Context\"] {:values [\"country-a\"], :timestamp 1589543760568, :valid? true}]]"}}
       :filter {"0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8" [:and]}
       :operations [:filter
                    "0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8"
                    "0889c83d983d8a7a1f762efb419c05d5c8ecd6d9db05e0cd223d9b6284719167"]})

(def di-2
  #:di{:data-tile-ref
       {"e9e2edc61ca00dffac81c52db5681b552b7dfce966a3dd402eec57f908e3bc20"
        {:di/identifier "search"
         :formdata
         "[[[\"temp\" \"Datasource\"] {:values [\"datasource-d\"], :timestamp 1589543742430, :valid? true}] [[\"country\" \"Context\"] {:values [\"country-c\"], :timestamp 1589543760568, :valid? true}]]"}}
       :filter {"0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8" [:and]}
       :operations [:filter
                    "0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8"
                    "e9e2edc61ca00dffac81c52db5681b552b7dfce966a3dd402eec57f908e3bc20"]})

(def union-op-result-di
  #:di{:data-tile-ref {"0889c83d983d8a7a1f762efb419c05d5c8ecd6d9db05e0cd223d9b6284719167"
                       {:di/identifier "search"
                        :formdata
                        "[[[\"datasource\" \"Datasource\"] {:values [\"datasource-b\" \"datasource-a\"], :timestamp 1589543742430, :valid? true}] [[\"country\" \"Context\"] {:values [\"country-a\"], :timestamp 1589543760568, :valid? true}]]"}
                       "e9e2edc61ca00dffac81c52db5681b552b7dfce966a3dd402eec57f908e3bc20"
                       {:di/identifier "search"
                        :formdata
                        "[[[\"temp\" \"Datasource\"] {:values [\"datasource-d\"], :timestamp 1589543742430, :valid? true}] [[\"country\" \"Context\"] {:values [\"country-c\"], :timestamp 1589543760568, :valid? true}]]"}}
       :filter {"0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8" [:and]}
       :operations [:union nil
                    [:filter
                     "0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8"
                     "0889c83d983d8a7a1f762efb419c05d5c8ecd6d9db05e0cd223d9b6284719167"]
                    [:filter
                     "0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8"
                     "e9e2edc61ca00dffac81c52db5681b552b7dfce966a3dd402eec57f908e3bc20"]]})

(def op-di #'op/op-di)

(deftest perform-ops
  (testing "union two di's"
    (is (= (op-di {:source-di di-1
                   :target-di di-2
                   :op :union})
           union-op-result-di))))