(ns de.explorama.shared.data-format.data-test
  (:require [de.explorama.shared.data-format.data :as data]
            [clojure.spec.alpha :as spec]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest test-data-spec
  (t/is (spec/valid? ::data/data
                     [{"a" 1, "b" "1"} {"a" 2, "b" "2"}]))
  (t/is (spec/valid? ::data/datapoint
                     {"a" 1, "b" "1"})))
