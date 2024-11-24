(ns de.explorama.shared.cache.data-tiles.multi-layer-cache-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :as core]
            [de.explorama.shared.cache.data-tile.transparent.retrieval :as retrieval]
            [de.explorama.shared.cache.data-tiles.cache-test :as ct]
            [de.explorama.shared.cache.data-tiles.tiling-test :as dt-test]
            [de.explorama.shared.cache.test-common :as tc]
            [de.explorama.shared.common.data.data-tiles :as tiles]
            [de.explorama.shared.common.test-data :as td]
            [taoensso.tufte :as tufte]))

(def example-query-bucket1 {:query {dt-test/bucket-dim #{"default"}}})
(def example-query-bucket2 {:query {dt-test/bucket-dim #{"default"}}
                            :prevent-caching? true})

(def example-query-country1 {:query {dt-test/country-dim #{td/country-h}
                                     dt-test/bucket-dim  #{"default"}}
                             :prevent-caching? true})
(def example-query-country2 {:query {dt-test/country-dim #{td/country-f}}})

(def example-query-country3 {:query {dt-test/country-dim #{td/country-e}
                                     dt-test/bucket-dim  #{"default"}}
                             :prevent-caching? true})
(def example-query-country4 {:query {dt-test/country-dim #{td/country-g}}})

(def example-query-datasource1 {:query {dt-test/datasource-dim #{td/datasource-a}
                                        dt-test/bucket-dim #{"default"}}
                                :prevent-caching? true})
(def example-query-datasource2 {:query {dt-test/datasource-dim #{td/datasource-a}}})
(def example-query-datasource3 {:query {dt-test/datasource-dim #{td/datasource-a td/datasource-b}}})
(def example-query-year1 {:query {dt-test/year-dim #{2004}
                                  dt-test/bucket-dim #{"default"}}
                          :prevent-caching? true})
(def example-query-year2 {:query {dt-test/year-dim #{2008}}})

(def example-query-year3 {:query {dt-test/year-dim #{2001}}})
(def example-query-year4 {:query {dt-test/year-dim #{2002}
                                  dt-test/bucket-dim #{"default"}}
                          :prevent-caching? true})

(def example-query-identifier1 {:query {dt-test/identifier-dim #{"search"}
                                        dt-test/bucket-dim #{"default"}}
                                :prevent-caching? true})
(def example-query-identifier2 {:query {dt-test/identifier-dim #{"search"}}})

(def example-query-dims1 {:query {dt-test/datasource-dim #{td/datasource-a}
                                  dt-test/country-dim #{td/country-h}
                                  dt-test/year-dim #{2004}
                                  dt-test/bucket-dim #{"default"}}})
(def example-query-dims2 {:query {dt-test/datasource-dim #{td/datasource-a}
                                  dt-test/country-dim #{td/country-h}
                                  dt-test/year-dim #{2004}
                                  dt-test/bucket-dim #{"default"}}
                          :prevent-caching? true})
(def example-query-dims3 {:query {dt-test/datasource-dim #{td/datasource-a}
                                  dt-test/country-dim #{td/country-c}
                                  dt-test/year-dim #{2003}
                                  dt-test/bucket-dim #{"default"}}})
(def example-query-dims4 {:query {dt-test/datasource-dim #{td/datasource-a}
                                  dt-test/country-dim #{td/country-c}
                                  dt-test/year-dim #{2003}
                                  dt-test/bucket-dim #{"default"}}
                          :prevent-caching? true})

(def response (atom {}))

(def test-data-tiles
  (let [[d1 d2 d3 d4]
        (partition-all 2 (mapv (fn [v]
                                 {(tiles/access-key "key")
                                  (str v)
                                  (tiles/access-key "size")
                                  (str (mod v 3))
                                  (tiles/access-key "identifier")
                                  "test"
                                  (tiles/access-key "bucket")
                                  "default"})
                               (range 4)))]
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
                                               (or cval (ct/gen-test-data-sample))))
                                      tile-spec)]))
                  tile-specs))))

(defn request-data-tiles-mock-1 [_ _ tile-specs opts]
  (request-data-tiles-mock-1- tile-specs opts))

(defn test-into-cache [cache return-type cache-function id-list opts]
  (reset! response {})
  (with-redefs [retrieval/miss request-data-tiles-mock-1]
    (let [raw-result (map (fn [id]
                            (tufte/p ::get-data-tiles-test
                                     (return-type (cache-function cache id opts))))
                          id-list)
          merged-result (reduce merge raw-result)]
      [raw-result merged-result])))

(def data-tile-index
  {[:data-tile-index {"datasource" td/datasource-a "year" "1997" "identifier" "search" td/country td/country-a "bucket" "default"}]
   [["default" (td/id-val "A" 6)]
    ["default" (td/id-val "A" 2)]]})

(def event-index
  {["default" (td/id-val "A" 1)]
   {:count 3 :event {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 1), "datasource" td/datasource-a
                     td/org (td/org-val 6), "location" [[15 15]]
                     "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}}
   ["default" (td/id-val "A" 2)]
   {:count 5 :evt {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 2), "datasource" td/datasource-a
                   td/org [(td/org-val 10) (td/org-val 4)], "location" [[6.63 1.72]]
                   "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}}

   ["default" (td/id-val "A" 3)]
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 3), "datasource" td/datasource-a
    td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   ["default" (td/id-val "A" 6)]
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" (td/id-val "A" 4), "datasource" td/datasource-a
    td/org [(td/org-val 10) (td/org-val 4)], "location" [[6.63 1.72]]
    "annotation" "", "date" "2000-05-05", "notes" "Text", td/fact-1 1}})

(def data-tiles-lite
  '({"year" 2000, "datasource" td/datasource-a, td/country td/country-i, "identifier" "search", "bucket" "default"}
    {"year" 2001, "datasource" td/datasource-a, td/country td/country-j, "identifier" "search", "bucket" "default"}
    {"year" 2002, "datasource" td/datasource-a, td/country td/country-e, "identifier" "search", "bucket" "default"}
    {"year" 2003, "datasource" td/datasource-a, td/country td/country-k, "identifier" "search", "bucket" "default"}))

(def data-tiles-lite2
  '({"year" 2004, "datasource" td/datasource-a, td/country td/country-i, "identifier" "search", "bucket" "default"}
    {"year" 2005, "datasource" td/datasource-a, td/country td/country-j, "identifier" "search", "bucket" "default"}
    {"year" 2006, "datasource" td/datasource-a, td/country td/country-e, "identifier" "search", "bucket" "default"}
    {"year" 2007, "datasource" td/datasource-a, td/country td/country-k, "identifier" "search", "bucket" "default"}))

(defn compare-indices [c]
  (let [dt-index-ids (-> c
                         .storage
                         .storage
                         deref
                         .vals
                         ((partial apply concat))
                         set)
        evt-index-ids (-> c
                          .storage
                          .event-index
                          deref
                          keys
                          set)]
    (= evt-index-ids dt-index-ids)))

(defn check-delete-query [cache-fn {:keys [query] :as query-params} gen-data]
  (let [cache (cache-fn)
        should-cache-fn (tc/should-cache-fn #{query})]

    (cache-api/lookup cache gen-data {})

    (.evict-by-query cache query-params)
    (is (every? (fn [data-tile]
                  (let [r (.has? cache data-tile)]
                    (or (and (should-cache-fn data-tile)
                             r)
                        (and (not (should-cache-fn data-tile))
                             (not r)))))
                gen-data))

    (cache-api/lookup cache gen-data {})

    (is (every? (fn [data-tile]
                  (.has? cache data-tile))
                gen-data))))

(defn check-delete-query-2 [cache {:keys [query] :as query-params} gen-data]
  (cache-api/lookup cache gen-data {})
  (.evict-by-query cache query-params)
  (is (= true (compare-indices cache)))
  (cache-api/lookup cache gen-data {})
  (is (= true (compare-indices cache))))

(defn request-data-tiles-mock-2-
  ([tile-specs]
   (request-data-tiles-mock-2- tile-specs {}))
  ([tile-specs opts]
   (into {} (mapv (fn [tile-spec]
                    [tile-spec {}])
                  tile-specs))))

(defn request-data-tiles-mock-3-
  ([tile-specs]
   (request-data-tiles-mock-1- tile-specs {}))
  ([tile-specs opts]
   (tufte/p ::request-data-tiles-mock
            (mapv (fn [tile-spec]
                    [tile-spec (map #(assoc % "year" (tile-spec "year"))
                                    (ct/gen-test-data-sample))])
                  tile-specs))))

(defn request-data-tiles-mock-3 [_ _ tile-specs opts]
  (request-data-tiles-mock-3- tile-specs opts))

(defn request-data-tiles-mock-2 [_ _ tile-specs opts]
  (request-data-tiles-mock-2- tile-specs opts))

(defn multi-layer-cache
  [retrieval-config storage-config
   cache-services data-tile-services]
  (core/multi-layer-cache retrieval-config
                          storage-config
                          cache-services
                          data-tile-services
                          (fn [url body])
                          (fn [url body])))

(defn multi-layer-cache-into-cache [cfg]
  (multi-layer-cache
   {:query-partition
    {"search" {:small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:default :small}}

   {:strategy cfg
    :size 150000}

   (atom {})

   (atom {})))

(defn multi-layer-small []
  (multi-layer-cache
   {:query-partition
    {"search" {:small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:default :small}}

   {:strategy :lru
    :size 30}

   (atom {})

   (atom {})))

(defn sample-test-data [inp]
  (->> (for [[k v] inp
             evt v]
         {[(k "bucket") (evt "id")] evt})
       shuffle
       (take 5)
       (apply merge)
       ((juxt keys vals))
       (map vec)))

(deftest retrieve-transparent-into-cache!-test
  (tufte/profile
   {:when ct/profile-tests?}
   (let [[raw-result merged-result] (test-into-cache (multi-layer-cache-into-cache nil) identity cache-api/lookup test-data-tiles nil)]
     (testing "retrieving a few tiles: default"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (multi-layer-cache-into-cache :lru) identity cache-api/lookup test-data-tiles nil)]
     (testing "retrieving a few tiles: lru"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [[raw-result merged-result] (test-into-cache (multi-layer-cache-into-cache :lru) identity cache-api/lookup test-data-tiles nil)]
     (testing "retrieving a few tiles: lu"
       (is (= 4 (count raw-result)))
       (is (= (set (keys merged-result)) test-data-tiles-set))
       (is (= merged-result @response))))
   (let [c (multi-layer-cache-into-cache :lru)
         [raw-result merged-result] (test-into-cache c identity cache-api/lookup test-data-tiles nil)
         [buckets-ids ids-events] (sample-test-data merged-result)
         [raw-result merged-result] (test-into-cache c identity cache-api/lookup buckets-ids {:single-events true})]
     (testing "retrieving a few tiles: lru"
       (is (= 5 (count raw-result)))
       (is (=  ids-events raw-result))))))

(deftest multi-layer-delete-by-query-test
  (with-redefs [retrieval/miss request-data-tiles-mock-3]
    (testing "delete by query with specific bucket"
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-bucket1 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-bucket2 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-bucket1 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-bucket2 dt-test/data-tiles-3))
    (testing "delete by query with specific country"
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-country1 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-country2 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-country3 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-country4 dt-test/data-tiles-3))
    (testing "delete by query with specific datasource"
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-datasource1 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-datasource2 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-datasource3 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-datasource1 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-datasource2 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-datasource3 dt-test/data-tiles-3))
    (testing "delete by query with specific year"
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-year1 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-year2 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-year3 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-year4 dt-test/data-tiles-3))
    (testing "delete by query with specific identifier"
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-identifier1 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-identifier2 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-identifier1 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-identifier2 dt-test/data-tiles-3))
    (testing "delete by query with specific dims"
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-dims1 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-dims2 dt-test/data-tiles-2)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-dims3 dt-test/data-tiles-3)
      (check-delete-query #(multi-layer-cache-into-cache :lru) example-query-dims4 dt-test/data-tiles-3))))

(deftest compare-cache-with-atom-events
  (with-redefs [retrieval/miss request-data-tiles-mock-3]
    (testing "compare cache and atom-events"
      (let [c (multi-layer-cache-into-cache :lru)]
        (is (= true (compare-indices c)))
        (cache-api/lookup c data-tiles-lite {})
        (is (= true (compare-indices c)))))
    (testing "compare cache & atom events after evict"
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-bucket1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-bucket2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-bucket1 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-bucket2 dt-test/data-tiles-3))
    (testing "delete by query with specific country"
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-country1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-country2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-country3 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-country4 dt-test/data-tiles-3))
    (testing "delete by query with specific datasource"
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-datasource1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-datasource2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-datasource3 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-small) example-query-datasource1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-small) example-query-datasource2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-small) example-query-datasource3 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-datasource1 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-datasource2 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-datasource3 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-small) example-query-datasource1 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-small) example-query-datasource2 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-small) example-query-datasource3 dt-test/data-tiles-3))
    (testing "delete by query with specific year"
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-year1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-year2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-year3 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-year4 dt-test/data-tiles-3))
    (testing "delete by query with specific identifier"
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-identifier1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-identifier2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-identifier1 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-identifier2 dt-test/data-tiles-3))
    (testing "delete by query with specific dims"
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-dims1 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-dims2 dt-test/data-tiles-2)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-dims3 dt-test/data-tiles-3)
      (check-delete-query-2 (multi-layer-cache-into-cache :lru) example-query-dims4 dt-test/data-tiles-3))))
