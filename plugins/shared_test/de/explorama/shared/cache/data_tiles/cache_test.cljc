(ns de.explorama.shared.cache.data-tiles.cache-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :as cache.core]
            [de.explorama.shared.cache.data-tile.retrieval]
            [de.explorama.shared.cache.data-tile.transparent.retrieval :as retrieval]
            [de.explorama.shared.cache.data-tiles.tiling-test :as dt-test]
            [de.explorama.shared.cache.test-common :as tc]
            [de.explorama.shared.common.data.data-tiles :as tiles]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [taoensso.tufte :as tufte]))

(def profile-tests? false)

(tufte/add-basic-println-handler! {})

(defn string-table [name-prop] (mapv #(str (name name-prop) %) (range 5000)))
(defn num-table [x] (vec (take x (repeatedly #(rand-int 500000)))))
(defn gen-sample [sample num]
  (let [sample-size (max 1 (rand-int 3))]
    (if (= sample-size 1)
      (first (take 1 (repeatedly #(get sample
                                       (max 1
                                            (dec (rand-int num)))))))
      (vec (take sample-size
                 (repeatedly #(get sample
                                   (max 1
                                        (dec (rand-int num))))))))))

(def organisations (string-table "organisation"))
(def organisations-count (count organisations))

(def countries (string-table "country"))
(def countries-count (count countries))

(def string-fact-1 (string-table "fact-1"))
(def string-fact-1-count (count string-fact-1))

(def string-fact-2 (string-table "fact2"))
(def string-fact-2-count (count string-fact-2))

(def int-fact-1 (num-table 1000000))
(def int-fact-1-count (count int-fact-1))

(def int-fact-2 (num-table 1000000))
(def int-fact-2-count (count int-fact-2))

(def test-data-tiles
  (let [[d1 d2 d3 d4]
        (partition-all 10 (mapv (fn [v]
                                  {(tiles/access-key "key")
                                   (str v)
                                   (tiles/access-key "size")
                                   (str (mod v 3))
                                   (tiles/access-key "identifier")
                                   "test"})
                                (range 40)))]
    [(concat d1
             (random-sample 0.1 d2)
             (random-sample 0.05 d3)
             (random-sample 0.2 d4))
     (concat d2
             (random-sample 0.1 d1)
             (random-sample 0.05 d3)
             (random-sample 0.2 d4))
     (concat d3
             (random-sample 0.1 d1)
             (random-sample 0.05 d2)
             (random-sample 0.2 d4))
     (concat d4
             (random-sample 0.1 d1)
             (random-sample 0.05 d2)
             (random-sample 0.2 d3))]))

(def test-data-tiles-set
  (set (flatten test-data-tiles)))

(def gen-samples
  (vec (take (if profile-tests?
               100000
               1000)
             (repeatedly
              (fn []
                {"id" (cljc-uuid)
                 "organisation" (gen-sample organisations organisations-count)
                 "country" (gen-sample countries countries-count)
                 "string-fact-1" (gen-sample string-fact-1 string-fact-1-count)
                 "string-fact-2" (gen-sample string-fact-2 string-fact-2-count)
                 "int-fact-1" (gen-sample int-fact-1 int-fact-1-count)
                 "int-fact-2" (gen-sample int-fact-2 int-fact-2-count)})))))

(def gen-samples-count (count gen-samples))

(defn take-samples [sample-size]
  (vec (take sample-size
             (repeatedly #(get gen-samples
                               (max 1
                                    (dec (rand-int gen-samples-count))))))))

(defn gen-test-data-sample []
  (take-samples (max 5 (rand-int (if profile-tests?
                                   5000
                                   8)))))

(def response (atom {}))

(defn request-data-tiles-mock-1-
  ([tile-specs]
   (request-data-tiles-mock-1- tile-specs {}))
  ([tile-specs opts]
   (tufte/p ::request-data-tiles-mock
            (mapv (fn [tile-spec]
                    (if-let [value (get @response tile-spec)]
                      [tile-spec value]
                      [tile-spec (get (swap! response
                                             update
                                             tile-spec
                                             (fn [cval]
                                               (or cval (gen-test-data-sample))))
                                      tile-spec)]))
                  tile-specs))))

(defn request-data-tiles-mock-1 [_ _ tile-specs opts]
  (request-data-tiles-mock-1- tile-specs opts))

(defn transparent-data-tile-cache-into-cache [cfg]
  (cache.core/transparent-data-tile-cache
   {:query-partition
    {"search" {:small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:default :small}}

   {:strategy cfg
    :size 150000}

   (atom {})

   (atom {})

   (fn [url body])
   (fn [url body])))

(defn transparent-data-tile-retrieval []
  (cache.core/cache-retrieval-client
   {:query-partition
    {"search" {:small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:default :small}}

   (atom {})
   (fn [url body])))

(defn local-data-tile-cache-into-cache [cfg]
  (cache.core/local-data-tile-cache
   {:query-partition
    {"test" {:small  {:partition 3
                      :keys      ["size"]}
             :medium  {:partition 2
                       :keys      ["size"]}
             :large  {:partition 1
                      :keys      ["size"]}}}
    :workaround-data-tile-classification
    {:classification [{:match {"size" "0"}
                       :=> :small}
                      {:match {"size" "1"}
                       :=> :medium}
                      {:match {"size" "2"}
                       :=> :large}]
     :default :small}}

   {:strategy cfg
    :size 150000}

   (atom {})

   request-data-tiles-mock-1-
   (fn [url body])))

(defn no-tiling-cache [cfg]
  (cache.core/no-tiling-cache
   {:strategy cfg
    :size 150000}

   (atom {})

   request-data-tiles-mock-1-
   (fn [url body])))

(defn put-cache [cfg]
  (cache.core/put-cache
   {:strategy cfg
    :size 150000}

   (atom {})
   (fn [url body])))

(defn test-into-cache [cache return-type cache-function opts]
  (reset! response {})
  (with-redefs [retrieval/miss request-data-tiles-mock-1]
    (let [raw-result (map (fn [test-data-tile-set]
                            (tufte/p ::get-data-tiles-test
                                     (return-type (cache-function cache test-data-tile-set opts))))
                          test-data-tiles)
          merged-result (reduce merge raw-result)]
      [raw-result merged-result])))

(deftest retrieve-transparent-into-cache!-test
  (tufte/profile
   {:when profile-tests?}
   (let [[raw-result merged-result] (test-into-cache (transparent-data-tile-cache-into-cache nil) identity cache-api/lookup nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (transparent-data-tile-cache-into-cache :lru) identity cache-api/lookup nil)]
     (testing "retrieving a few tiles: lru"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (transparent-data-tile-cache-into-cache :lu) identity cache-api/lookup nil)]
     (testing "retrieving a few tiles: lu"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))))

(deftest retrieve-local-into-cache!-test
  (tufte/profile
   {:when profile-tests?}
   (let [[raw-result merged-result] (test-into-cache (local-data-tile-cache-into-cache nil) #(into {} %) cache-api/lookup nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (local-data-tile-cache-into-cache :lru) #(into {} %) cache-api/lookup nil)]
     (testing "retrieving a few tiles: lru"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (local-data-tile-cache-into-cache :lu) #(into {} %) cache-api/lookup nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))))

(deftest retrieve-into-cache!-test-abort-early
  (tufte/profile
   {:when profile-tests?}
   (testing "Testing local cache"
     (is (thrown-with-msg? #?(:clj Exception
                              :cljs js/Error)
                           #"Data-tile limited exceeded"
                           (test-into-cache (local-data-tile-cache-into-cache nil)
                                            #(into {} %)
                                            cache-api/lookup
                                            {:abort-early {:data-tile-limit 15}}))))
   (testing "Testing transparent cache"
     (is (thrown-with-msg? #?(:clj Exception
                              :cljs js/Error)
                           #"Data-tile limited exceeded"
                           (test-into-cache (transparent-data-tile-cache-into-cache nil)
                                            #(into {} %)
                                            cache-api/lookup
                                            {:abort-early {:data-tile-limit 15}}))))
   (testing "Testing transparent client"
     (is (thrown-with-msg? #?(:clj Exception
                              :cljs js/Error)
                           #"Data-tile limited exceeded"
                           (test-into-cache (transparent-data-tile-retrieval)
                                            identity
                                            cache-api/lookup
                                            {:abort-early {:data-tile-limit 15}}))))))

(deftest retrieve-transparent-test
  (tufte/profile
   {:when profile-tests?}
   (let [[raw-result merged-result] (test-into-cache (transparent-data-tile-retrieval) identity cache-api/lookup nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))))

(deftest retrieve-no-tiling-into-cache!-test
  (tufte/profile
   {:when profile-tests?}
   (let [[raw-result merged-result] (test-into-cache (no-tiling-cache nil) #(into {} %) cache-api/lookup nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (no-tiling-cache :lru) #(into {} %) cache-api/lookup nil)]
     (testing "retrieving a few tiles: lru"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (no-tiling-cache :lu) #(into {} %) cache-api/lookup nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))))

(deftest retrieve-put-into-cache!-test
  (tufte/profile
   {:when profile-tests?}
   (let [[raw-result merged-result] (test-into-cache (put-cache nil)
                                                     #(into {} %)
                                                     cache-api/lookup
                                                     {:miss
                                                      request-data-tiles-mock-1-})]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (put-cache :lru)
                                                     #(into {} %)
                                                     cache-api/lookup
                                                     {:miss
                                                      request-data-tiles-mock-1-})]
     (testing "retrieving a few tiles: lru"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (put-cache :lu)
                                                     #(into {} %)
                                                     cache-api/lookup
                                                     {:miss
                                                      request-data-tiles-mock-1-})]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))))

(def example-query-bucket1 {:query {dt-test/bucket-dim #{"default"}}})
(def example-query-bucket2 {:query {dt-test/bucket-dim #{"default"}}
                            :prevent-caching? true})

(def example-query-country1 {:query {dt-test/country-dim #{"country-1"}
                                     dt-test/bucket-dim  #{"default"}}
                             :prevent-caching? true})
(def example-query-country2 {:query {dt-test/country-dim #{"country-2"}}})

(def example-query-datasource1 {:query {dt-test/datasource-dim #{"datasource-1"}
                                        dt-test/bucket-dim #{"default"}}
                                :prevent-caching? true})
(def example-query-datasource2 {:query {dt-test/datasource-dim #{"datasource-2"}}})

(def example-query-year1 {:query {dt-test/year-dim #{2004}
                                  dt-test/bucket-dim #{"default"}}
                          :prevent-caching? true})
(def example-query-year2 {:query {dt-test/year-dim #{2008}}})

(def example-query-identifier1 {:query {dt-test/identifier-dim #{"search"}
                                        dt-test/bucket-dim #{"default"}}
                                :prevent-caching? true})
(def example-query-identifier2 {:query {dt-test/identifier-dim #{"search"}}})

(def example-query-dims1 {:query {dt-test/datasource-dim #{"datasource-1"}
                                  dt-test/country-dim #{"country-1"}
                                  dt-test/year-dim #{2004}
                                  dt-test/bucket-dim #{"default"}}})
(def example-query-dims2 {:query {dt-test/datasource-dim #{"datasource-1"}
                                  dt-test/country-dim #{"country-1"}
                                  dt-test/year-dim #{2004}
                                  dt-test/bucket-dim #{"default"}}
                          :prevent-caching? true})

(defn request-data-tiles-mock-2-
  ([tile-specs]
   (request-data-tiles-mock-2- tile-specs {}))
  ([tile-specs opts]
   (into {} (mapv (fn [tile-spec]
                    [tile-spec {}])
                  tile-specs))))

(defn request-data-tiles-mock-2 [_ _ tile-specs opts]
  (request-data-tiles-mock-2- tile-specs opts))

(defn transparent-data-tile-cache-prevent []
  (cache.core/transparent-data-tile-cache
   {:query-partition
    {"search" {:small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:default :small}}

   {:strategy :lru
    :size 150000}

   (atom {})

   (atom {})
   (fn [url body])
   (fn [url body])))

(defn local-data-tile-cache-prevent []
  (cache.core/local-data-tile-cache
   {:query-partition
    {"search" {:small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:default :small}}

   {:strategy :lru
    :size 150000}

   (atom {})

   request-data-tiles-mock-2-
   (fn [url body])))

(defn check-delete-query [cache-fn {:keys [query] :as query-params}]
  (let [cache (cache-fn)
        should-cache-fn (tc/should-cache-fn #{query})]

    (cache-api/lookup cache dt-test/data-tiles-2 {})

    (cache-api/evict-by-query cache query-params)
    (is (every? (fn [data-tile]
                  (let [r (cache-api/has? cache data-tile)]
                    (or (and (should-cache-fn data-tile)
                             r)
                        (and (not (should-cache-fn data-tile))
                             (not r)))))
                dt-test/data-tiles-2))

    (cache-api/lookup cache dt-test/data-tiles-2 {})

    (is (every? (fn [data-tile]
                  (cache-api/has? cache data-tile))
                dt-test/data-tiles-2))))

(deftest transparent-delete-by-query-test
  (with-redefs [retrieval/miss request-data-tiles-mock-2]
    (testing "delete by query with specific bucket"
      (check-delete-query transparent-data-tile-cache-prevent example-query-bucket1)
      (check-delete-query transparent-data-tile-cache-prevent example-query-bucket2))
    (testing "delete by query with specific country"
      (check-delete-query transparent-data-tile-cache-prevent example-query-country1)
      (check-delete-query transparent-data-tile-cache-prevent example-query-country2))
    (testing "delete by query with specific datasource"
      (check-delete-query transparent-data-tile-cache-prevent example-query-datasource1)
      (check-delete-query transparent-data-tile-cache-prevent example-query-datasource2))
    (testing "delete by query with specific year"
      (check-delete-query transparent-data-tile-cache-prevent example-query-year1)
      (check-delete-query transparent-data-tile-cache-prevent example-query-year2))
    (testing "delete by query with specific identifier"
      (check-delete-query transparent-data-tile-cache-prevent example-query-identifier1)
      (check-delete-query transparent-data-tile-cache-prevent example-query-identifier2))
    (testing "delete by query with specific dims"
      (check-delete-query transparent-data-tile-cache-prevent example-query-dims1)
      (check-delete-query transparent-data-tile-cache-prevent example-query-dims2))))

(deftest local-delete-by-query-test
  (testing "delete by query with specific bucket"
    (check-delete-query local-data-tile-cache-prevent example-query-bucket1)
    (check-delete-query local-data-tile-cache-prevent example-query-bucket2))
  (testing "delete by query with specific country"
    (check-delete-query local-data-tile-cache-prevent example-query-country1)
    (check-delete-query local-data-tile-cache-prevent example-query-country2))
  (testing "delete by query with specific datasource"
    (check-delete-query local-data-tile-cache-prevent example-query-datasource1)
    (check-delete-query local-data-tile-cache-prevent example-query-datasource2))
  (testing "delete by query with specific year"
    (check-delete-query local-data-tile-cache-prevent example-query-year1)
    (check-delete-query local-data-tile-cache-prevent example-query-year2))
  (testing "delete by query with specific identifier"
    (check-delete-query local-data-tile-cache-prevent example-query-identifier1)
    (check-delete-query local-data-tile-cache-prevent example-query-identifier2))
  (testing "delete by query with specific dims"
    (check-delete-query local-data-tile-cache-prevent example-query-dims1)
    (check-delete-query local-data-tile-cache-prevent example-query-dims2)))
