(ns de.explorama.backend.storage.agent-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [config.core :refer [env]]
            [de.explorama.backend.config :as config]
            [de.explorama.backend.storage.agent.core :as sut]
            [taoensso.carmine :as car]
            [de.explorama.backend.redis.utils :as rutil]))

(defn- init-test [test-instance]
  (loop [i 0
         result []]
    (let [{success :success
           val :val}
          (try {:success true
                :val @test-instance}
               (catch Exception e
                 {:success false
                  :val (ex-data e)}))]
      (if (and (< i 10)
               (not success)
               (= :not-ready (:cause val)))
        (do
          (Thread/sleep 500)
          (recur (inc i)
                 (conj result val)))
        result))))

(def test-file-impl?
  (or (env :explorama-test-file-impl?)
      false))

(def test-redis-impl?
  (or (env :explorama-test-redis-impl?)
      false))

(def redis-path "de.explorama.backend.storage.agent/test")

(defn test-case [desc]
  (let [test-instance (sut/create desc)
        _ (sut/start! (:path desc))
        init-test-result (init-test test-instance)
        init-value @test-instance]
    [(is (or (empty? init-test-result)
             (every? (fn [data]
                       (= :not-ready (:cause data)))
                     init-test-result)))
     (is (=  init-value
             {:a ["lets go"]}))]))

(let [tests (cond-> [["test-dummy" {:impl :test
                                    :path "test"
                                    :init {:a ["lets go"]}}]]
              test-file-impl?
              (conj ["test-file" {:impl :file
                                  :path "temp/test.edn"
                                  :init {:a ["lets go"]}}])
              test-redis-impl?
              (conj ["test-redis" {:impl :redis
                                   :connection (rutil/config->conspec)
                                   :path redis-path
                                   :init {:a ["lets go"]}}]))]
  (deftest test-cases
    (testing "Testing storage"
      (mapv (fn [[test-case-sym desc]]
              (testing (str "testing " test-case-sym)
                (test-case desc)))
            tests))))

(defn test-setup [test-fn]
  (test-fn)
  (when test-redis-impl?
    (rutil/wcar* {:spec (rutil/config->conspec config/redis-connection-spec)
                  :pool {}}
                 "Clean up test data"
                 (car/del redis-path)))
  (when test-file-impl?
    (io/delete-file "temp/test.edn")))

(use-fixtures :each test-setup)