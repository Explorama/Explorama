(ns de.explorama.frontend.data-atlas.db-utils-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.data-atlas.db-utils :as db-utils]
            [de.explorama.frontend.data-atlas.path :as path]))

(def new-db {})
(def test-frame-id {:frame-id "data-atlas-2cd083f2-83f9-44c6-a5be-b7b0209db927"
                    :workspace-id "8bc400b6-6b7d-48ca-a2c1-19d117532ffd"
                    :vertical "data-atlas"})

(def db-with-frame-id (assoc-in new-db
                                (path/frame test-frame-id)
                                {}))
(def db-with-frame-infos (assoc-in new-db
                                   (path/frame test-frame-id)
                                   {:frame-infos "test"}))

(deftest frame-exist-guard-test
  (testing "frame exist guard tests"
    ;not saving check
    (is (= new-db
           (db-utils/frame-exist-guard test-frame-id
                                       new-db
                                       db-with-frame-infos)))
    ;saving check
    (is (= db-with-frame-infos
           (db-utils/frame-exist-guard test-frame-id
                                       db-with-frame-id
                                       db-with-frame-infos)))))
