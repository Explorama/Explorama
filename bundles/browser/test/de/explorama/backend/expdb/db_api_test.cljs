(ns de.explorama.backend.expdb.db-api-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [de.explorama.backend.expdb.middleware.db :as middleware]
            [de.explorama.backend.expdb.persistence.backend-simple :as backend-simple]
            [de.explorama.backend.expdb.persistence.db-api :as sut]))

(deftest upload-into-never-instantiated-bucket
  (with-redefs [de.explorama.backend.expdb.persistence.backend-simple/store (atom {})]
    (let [ok (atom nil)]
      (sut/upload-bucket {:client-callback #(reset! ok %)}
                         [:simple "fresh-bucket" {:k 1}])
      (testing "set-dump reaches a created-on-demand instance"
        (is (true? @ok))
        (is (= 1 (middleware/get "fresh-bucket" :k)))))))
