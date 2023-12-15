(ns de.explorama.backend.search.core-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.search.config :as config-search]
            [de.explorama.backend.search.datainstance.core :refer [gen-di]]))

(deftest test-acs->ac-map
  (testing "wrong result"
    (is true)))

(def formdata [[["datasource" "Datasource"] {:values ["datasource-1"], :timestamp 1643369508621, :valid? true}]
               [["country" "Context"] {:values ["country1"], :timestamp 1643369512702, :valid? true}]])

(def gen-di-result #:di{:data-tile-ref {#?(:clj "ebc12d8f3c27c2031d00844089ac8265b522e7ed56bc117be124608628073fe3"
                                           :cljs "011a05e289bcb4fb6f8cc40d08bb3760ec309fd8eb81fcc9b4112f9486cac512")
                                        {:di/identifier "search",
                                         :formdata "[[[\"datasource\" \"Datasource\"] {:values [\"datasource-1\"], :timestamp 1643369508621, :valid? true}] [[\"country\" \"Context\"] {:values [\"country1\"], :timestamp 1643369512702, :valid? true}]]"}},
                        :operations [:filter "0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8" #?(:clj "ebc12d8f3c27c2031d00844089ac8265b522e7ed56bc117be124608628073fe3"
                                                                                                                   :cljs "011a05e289bcb4fb6f8cc40d08bb3760ec309fd8eb81fcc9b4112f9486cac512")],
                        :filter {"0b5a3a3424f0239ff14443540c3ae259fbf2bd3b4768e2596d2826562878a3b8" [:and]}})

(deftest test-gen-di
  (testing "datainstance generation"
    (is (=
         gen-di-result
         (gen-di formdata)))))

(use-fixtures :each (fn [tf]
                      (with-redefs [config-search/explorama-threshold-count-events-data-tiles 2500000
                                    config-search/explorama-threshold-count-events-filter 2000000
                                    config-search/explorama-threshold-count-events-chunk-size 250000]
                        (tf))))

                      