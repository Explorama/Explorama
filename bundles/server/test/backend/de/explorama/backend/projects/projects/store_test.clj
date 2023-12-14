(ns de.explorama.backend.projects.projects.store-test
  (:require [clojure.test :refer :all]
            [de.explorama.backend.projects.projects.store :as store]))

(deftest test-frame-project
  (with-redefs [store/projects-store
                (atom {:projects {"cafebabe" {:id "cafebabe"
                                              :title "World domination v 4711"}}})]
    (testing (is (= "World domination v 4711" (:title (store/frame-project {:workspace-id "cafebabe"
                                                                            :frame-id 1})))))))
