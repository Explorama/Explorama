(ns data-format-lib.simplified-view-gen-test
  (:require
   [data-format-lib.simplified-view :as sv]
   [malli.core :as m]
   [malli.generator :as mg]
   [clojure.test :refer [deftest is]]))

(defn- function-checker [iterations]
  (fn [schema options]
    (mg/function-checker schema (assoc options ::mg/=>iterations iterations))))

(def =>flatten-filter
  (m/schema sv/=>flatten-filter {::m/function-checker (function-checker 20)}))

(deftest validate-flatten-filter
  (is (true? (m/validate =>flatten-filter sv/flatten-filter))))
