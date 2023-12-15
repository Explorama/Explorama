(ns de.explorama.backend.projects.store-test
  (:require [clojure.test :refer [deftest is testing]]))

#_;TODO r1/tests fix this test
(deftest test-frame-project
  (with-redefs [store/projects-store
                (atom {:projects {"Project 1" {:id "Project 1"
                                               :title "Project title 1"}}})]
    (testing (is (= "Project title 1" (:title (store/frame-project {:workspace-id "Project 1"
                                                                    :frame-id 1})))))))
