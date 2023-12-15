(ns de.explorama.backend.projects.direct-search-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.projects.core :as project]
            [de.explorama.backend.projects.direct-search :as direct-search]))

(def ^:private project-search #'direct-search/project-search)

(defn test-proj [id name desc]
  {:project-id id
   :title name
   :description desc})

(def p1 (test-proj 1 "test1" nil))
(def p2 (test-proj 2 "2test" nil))
(def p3 (test-proj 3 "fooo3" nil))
(def p4 (test-proj 4 "fooo4" "test uia4"))

(def projects {:created-projects {1 p1
                                  3 p3}
               :allowed-projects {2 p2
                                  4 p4}})

(deftest test-project-search
  (with-redefs [project/list-projects (identity projects)]
    (testing "simple search"
      (is (project-search "foo" nil)
          {:created-projects {3 p3}
           :allowed-projects {4 p4}})
      (is (project-search "tes" nil)
          {:created-projects {1 p1}
           :allowed-projects {2 p2
                              4 p4}}))
    (testing "empty"
      (is (project-search "bar" nil)
          {:created-projects {}
           :allowed-projects {}}))))