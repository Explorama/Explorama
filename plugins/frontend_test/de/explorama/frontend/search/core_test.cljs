(ns de.explorama.frontend.search.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.search.util :as s-util]))

(deftest clear-path-test
  (testing "search-core/clear-path"
    (is (= (s-util/clear-path {:t {"test" :a}} [:t] ["test"])
           {:t {}}))))
