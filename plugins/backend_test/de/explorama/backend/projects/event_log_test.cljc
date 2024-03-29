(ns de.explorama.backend.projects.event-log-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.expdb.middleware.simple-db-test :refer [test-setup]]
            [de.explorama.backend.projects.persistence.event-log.expdb :as expdb]
            [de.explorama.backend.projects.persistence.event-log.repository :as api]))

(defn test-vec [number]
  (map #(vector (str "test" %1) :a :b :c)
       (range number)))

(def size 100)

(deftest test-event-log
  (testing "Reading (full and partially) and writing lines"
    (let [backend (expdb/new-instance "project 1")
          test-data (test-vec size)]
      (is (api/append-lines backend test-data))
      (is (= test-data
             (api/read-lines backend)))
      (is (= (take 6 test-data)
             (api/read-lines backend 0 5))))))

(use-fixtures :once test-setup)
