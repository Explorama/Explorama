(ns de.explorama.backend.charts.error-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.charts.config :as charts-config]
            [de.explorama.backend.charts.data.fetch :as data-fetch]
            [de.explorama.shared.common.test-data :as td]))

(use-fixtures :each (fn [tf]
                      (with-redefs [charts-config/explorama-charts-max-data-amount 1
                                    data-fetch/get-full-data (fn [_] td/error-case-data)]
                        (tf))))

(deftest too-much-data-test
  (testing "testing exception when data amount is too much"
    (is (thrown-with-msg? #?(:clj Exception
                             :cljs js/Error)
                          #"Too much data"
                          (data-fetch/di-data {})))))