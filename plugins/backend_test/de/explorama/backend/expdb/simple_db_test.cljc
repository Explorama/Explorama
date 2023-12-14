(ns de.explorama.backend.expdb.simple-db-test
  (:require [clojure.test :as t :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.expdb.middleware.simple-db-test :refer [test-setup db]]
            [de.explorama.backend.expdb.persistence.simple :as sut]))

(use-fixtures :each test-setup)

(deftest happy-path-tests
  (testing "testing single functions"
    (is (= (sut/set @db "foo" "bar")
           {:success true
            :pairs 1}))
    (is (= (sut/get @db "foo")
           "bar"))
    (is (= (sut/del @db "foo")
           {:success true
            :pairs -1}))
    (is (= (sut/get @db "foo")
           nil)))
  (testing "testing multi functions"
    (is (= (sut/set+ @db {"foo" "bar"
                          "bar" "foo"
                          :blub "blub"})
           {:success true
            :pairs 3}))
    (is (= (sut/get+ @db)
           {"foo" "bar"
            "bar" "foo"
            :blub "blub"}))
    (is (= (sut/set+ @db {"foo1" "bar1"
                          "bar1" "foo1"
                          :blub1 "blub1"})
           {:success true
            :pairs 3}))
    (is (= (sut/del @db "bar")
           {:success true
            :pairs -1}))
    (is (= (sut/del @db :blub)
           {:success true
            :pairs -1}))
    (is (= (sut/get+ @db)
           {"foo" "bar"
            "foo1" "bar1"
            "bar1" "foo1"
            :blub1 "blub1"}))
    (is (= (sut/get+ @db ["foo" "foo1" :blub1])
           {"foo" "bar"
            "foo1" "bar1"
            :blub1 "blub1"}))
    (is (= (sut/del-bucket @db)
           {:success true
            :dropped-bucket? true}))
    (is (= (sut/get+ @db)
           {}))))

(deftest dump-test
  (testing "testing dump functions"
    (is (= (sut/dump @db)
           {}))
    (is (= (sut/set-dump @db
                         {"foo" "bar"
                          "bar" "foo"
                          :blub "blub"})
           {:success true
            :pairs 3}))
    (is (= (sut/get+ @db)
           {"foo" "bar"
            "bar" "foo"
            :blub "blub"}))
    (is (= (sut/dump @db)
           {"foo" "bar"
            "bar" "foo"
            :blub "blub"}))))
