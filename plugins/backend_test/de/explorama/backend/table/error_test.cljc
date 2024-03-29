(ns de.explorama.backend.table.error-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.table.config :as table-config]
            [de.explorama.backend.table.data.fetch :as data-fetch]
            [de.explorama.shared.common.test-data :as td]))

(use-fixtures :each (fn [tf]
                      (with-redefs [table-config/explorama-table-max-data-amount 1
                                    data-fetch/get-full-data (fn [_] td/error-case-data)]
                        (tf))))

(deftest too-much-data-test
  (testing "testing exception when data amount is too much"
    (is (thrown-with-msg? #?(:clj Exception
                             :cljs js/Error)
                          #"Too much data"
                          (data-fetch/di-data {})))))
