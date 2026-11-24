(ns de.explorama.backend.expdb.db-api-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [de.explorama.backend.expdb.middleware.db :as middleware]
            [de.explorama.backend.expdb.persistence.backend-indexed :as backend-indexed]
            [de.explorama.backend.expdb.persistence.backend-simple :as backend-simple]
            [de.explorama.backend.expdb.persistence.db-api :as sut]))

(def ^:private node-fs (js/require "fs"))

(def ^:private db-key "de.explorama.backend.expdb.db-api-test.sqlite3")

(def ^:private indexed-db-key "de.explorama.backend.expdb.db-api-test.indexed.sqlite3")

(defn- with-db [test-fn]
  (with-redefs [de.explorama.backend.expdb.persistence.backend-simple/db-key db-key
                de.explorama.backend.expdb.persistence.backend-indexed/db-key indexed-db-key]
    (reset! @#'backend-simple/store {})
    (try
      (test-fn)
      (finally
        (reset! @#'backend-simple/store {})
        (doseq [f [db-key indexed-db-key]]
          (when (.existsSync node-fs f)
            (.rmSync node-fs f)))))))

(deftest load-buckets-lists-original-simple-names
  (with-db
    (fn []
      (middleware/set "a-b" :k 1)
      (middleware/set "c/d" :k 2)
      (let [result (atom nil)]
        (sut/load-buckets {:client-callback #(reset! result %)})
        (testing "original names, not mangled table names"
          (is (contains? (set @result) [:simple "a-b"]))
          (is (contains? (set @result) [:simple "c/d"])))))))

(deftest download-bucket-returns-simple-dump
  (with-db
    (fn []
      (middleware/set "dump-me" :answer 42)
      (let [result (atom nil)]
        (sut/download-bucket {:client-callback (fn [& args] (reset! result (vec args)))}
                             [:simple "dump-me"])
        (let [[bucket-name dump] @result]
          (testing "dump keyed by the original bucket name"
            (is (= "dump-me" bucket-name))
            (is (= 42 (get dump :answer)))))))))

(deftest simple-roundtrip-through-wipe
  (with-db
    (fn []
      (middleware/set "roundtrip" :answer 42)
      (let [dumped (atom nil)]
        (sut/download-expdb {:client-callback #(reset! dumped %)})
        (is (= 42 (get-in @dumped [:simple "roundtrip" :answer])))
        (middleware/del-bucket "roundtrip")
        (reset! @#'backend-simple/store {})
        (let [ok (atom nil)]
          (sut/upload-expdb {:client-callback #(reset! ok %)}
                            [{:simple (:simple @dumped)}])
          (testing "upload succeeds onto a wiped instance"
            (is (true? @ok))))
        (testing "data restored under the original name"
          (is (= 42 (middleware/get "roundtrip" :answer))))))))
