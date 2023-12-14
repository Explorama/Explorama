(ns de.explorama.backend.projects.repo-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [compose-fixtures deftest is testing
                                  use-fixtures]]
            [de.explorama.backend.mocks.redis :as redis-mock]
            [de.explorama.backend.storage.agent.core :as pcore]
            [de.explorama.backend.projects.persistence.redis :as redis]
            [de.explorama.backend.projects.persistence.file :as file-repo]
            [de.explorama.backend.projects.persistence.repository :as repo]
            [de.explorama.backend.projects.projects.path :as ppath]
            [de.explorama.backend.projects.projects.store :as store]
            [de.explorama.backend.redis.utils :as red]
            [taoensso.carmine :as car]))

(defn test-vec [number]
  (map #(vector (str "test" %1) :a :b :c)
       (range number)))

(def size 100)

(def test-file-name "test-store.txt")

(deftest test-file-persistence1
  (testing "reading back the entire thing"
    (try (let [backend (file-repo/new-instance test-file-name)
               test-data (test-vec size)]
           (repo/append-lines backend test-data)
           (is (= test-data (repo/read-lines backend))))
         (finally (io/delete-file test-file-name)))))

(deftest test-file-persistence2
  (testing "reading back line by line"
    (try (let [backend (file-repo/new-instance test-file-name)
               test-data (test-vec size)]
           (repo/append-lines backend test-data)
           (for [i (range size)]
             (is (= (vector (str "test" i) :a :b :c)
                    (first (repo/read-lines backend))))))
         (finally (io/delete-file test-file-name)))))

(deftest test-file-persistence3
  (testing "reading back everything with force"
    (try (let [backend (file-repo/new-instance test-file-name)
               test-data (test-vec size)]
           (repo/append-lines backend test-data)
           (is (= test-data (repo/read-lines-force backend))))
         (finally (io/delete-file test-file-name)))))

(deftest test-clean-up-keys
  (testing "Test cleanup keys from redis"
    (let [conn (red/config->conspec)
          test-key "abc"]
      (red/wcar* conn
                 "test-clean-up-keys-set"
                 (car/set test-key {:last-modified (.toEpochMilli
                                                    (.toInstant
                                                     (.minusDays
                                                      (java.time.ZonedDateTime/now) 5)))}))
      (is (= 1 (redis/clean-up-keys conn test-key 1))))))

(defn test-setup [test-fn]
  (with-redefs
   [ppath/project-prefix "test-plogs/projects/"
    ppath/workspaces-prefix "test-plogs/workspaces/"
    store/projects-store (pcore/create {:impl :test
                                        :path @#'store/path
                                        :init {}})]
    (store/start!)
    (test-fn)))

(use-fixtures :each (compose-fixtures redis-mock/fixture test-setup))
