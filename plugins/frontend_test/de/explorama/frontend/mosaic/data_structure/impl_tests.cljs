(ns de.explorama.frontend.mosaic.data-structure.impl-tests
  (:require [cljs.test :refer-macros [deftest testing is]]
            [clojure.set :as set]
            [clojure.string :as string]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data-structure.cljs-impl :as gdsci]))

(def get-in-test-data
  [[1 2 3]
   [3 4 5]])

(def data-raw-simple
  [{"id" "1"
    "a" "A"
    "b" "B"
    "c" "C"}
   {"id" "2"
    "a" "B"
    "c" "C"}
   {"id" "3"
    "a" "A"
    "b" "B"}
   {"id" "4"
    "a" "F"
    "b" "B"
    "c" "C"}
   {"id" "5"
    "a" "D"
    "c" "C"}
   {"id" "6"
    "a" "E"
    "c" "C"}
   {"id" "7"
    "a" "A"
    "b" "B"}
   {"id" "8"
    "a" "G"
    "c" "C"}])
(def data-raw
  [{"id" "1"
    "a" "A"
    "b" "B"
    "c" "C"}
   {"id" "2"
    "a" "B"
    "c" "C"}
   {"id" "11"
    "a" "A"
    "b" "B"}
   {"id" "4"
    "a" "F"
    "b" "B"
    "c" "C"}
   {"id" "13"
    "a" "D"
    "c" "C"}
   {"id" "14"
    "a" ["E" "G"]
    "c" "C"}
   {"id" "15"
    "a" ["A" "H"]
    "b" "B"}
   {"id" "16"
    "a" "G"
    "c" "C"}])

(def nil-check-data
  [{"id" "1"
    "prop" "1"
    "b" "B"
    "c" "C"}
   {"id" "2"
    "prop" "2"
    "a" "B"
    "c" "C"}
   {"id" "3"
    "prop" "kokoasd"
    "a" "A"
    "b" "B"}
   {"id" "4"
    "prop" "4"
    "a" "F"
    "b" "B"
    "c" "C"}
   {"id" "5"
    "prop" "gsdfsdf"
    "a" "D"
    "c" "C"}
   {"id" "6"
    "a" "D" ; no prop -> nil
    "c" "C"}
   {"id" "7"
    "prop" "asdasd"
    "a" "E"
    "c" "C"}
   {"id" "8"
    "prop" nil
    "a" "A"
    "b" "B"}
   {"id" "9"
    "a" "G" ; no prop -> nil
    "c" "C"}])

(def every-check-data
  [{"t" "T"
    "a" "A"}
   {"t" "T"
    "a" "A"}
   {"t" "T"
    "a" "A"}
   {"t" "T"
    "a" "B"} ; a != A
   {"t" "T"
    "a" "A"}
   {"t" "T"
    "a" "A"}
   {"t" "T"
    "a" "A"}])

(def vector-data
  ["abc" "bcd" "cdef"])

(defn mapv-test []
  (mapv #(get % "a") data-raw))

(defn grp-by-test []
  (group-by #(get % "a") data-raw-simple))

(defn grp-by-vector-test []
  (gdsci/group-by-expand-vectors #(get % "a") data-raw))

(defn sort-by-asc-test []
  (vec (sort-by (fn [a]
                  (if (vector? (get a "a"))
                    (first (get a "a"))
                    (get a "a")))
                data-raw)))

(defn sort-by-desc-test []
  (vec (sort-by (fn [a]
                  (if (vector? (get a "a"))
                    (first (get a "a"))
                    (get a "a")))
                (fn [a1 a2]
                  (compare a2 a1))
                data-raw)))

(defn sort-by-asc-nil-test []
  (let [k "prop"]
    (vec (sort-by (fn [a]
                    (if (vector? (get a k))
                      (first (get a k))
                      (get a k)))
                  (fn [a1 a2]
                    (or
                     (gdal/handle-nil-compare a1 a2)
                     (compare a1 a2)))
                  nil-check-data))))

(defn sort-by-desc-nil-test []
  (let [k "prop"]
    (vec (sort-by (fn [a]
                    (if (vector? (get a k))
                      (first (get a k))
                      (get a k)))
                  (fn [a1 a2]
                    (or
                     (gdal/handle-nil-compare a2 a1)
                     (compare a2 a1)))
                  nil-check-data))))


(defn transform-test []
  data-raw-simple)

(defn count-test []
  (count data-raw-simple))

(defn keys-test []
  (keys (first data-raw-simple)))

(defn filter-test []
  (filterv (fn [a] (= "B" (get a "a")))
           data-raw-simple))
(defn filter-index-test []
  (->> (map-indexed vector data-raw-simple)
       (filterv (fn [[_ a]] (= "B" (get a "a"))))
       (mapv first)))
(defn remove-test []
  (vec (remove (fn [a] (= "B" (get a "a")))
               data-raw-simple)))
(defn concat-test []
  (vec (concat data-raw-simple
               data-raw)))
(defn conj-test []
  (conj data-raw
        {"a" "Z"}))
(defn assoc-test []
  (assoc (first data-raw)
         "a"
         "Z"))
(defn join-test []
  (string/join "," (mapv #(get % "a") data-raw-simple)))
(defn select-keys-test []
  (select-keys (first data-raw)
               ["a" "b"]))
(defn reduce-test []
  (reduce (fn [acc a]
            (+ acc
               (if (get a "a")
                 1
                 0)))
          0
          data-raw))
(defn union-test []
  (set/union (set data-raw-simple)
             (set data-raw)))
(defn intersection-test []
  (set/intersection (set data-raw-simple)
                    (set data-raw)))
(defn union-vec-test []
  (->> (union-test)
       vec
       (sort-by #(get % "id"))))
(defn intersection-vec-test []
  (->> (intersection-test)
       vec
       (sort-by #(get % "id"))))

(defn reduce-union-one-test []
  (reduce (fn [acc element]
            (set/union acc (set (keys element))))
          #{}
          data-raw-simple))

(defn coll?-test-one []
  (coll? (first data-raw-simple)))

(defn coll?-test-two []
  (coll? data-raw-simple))

(defn merge-test []
  (merge (first data-raw-simple)
         (second data-raw-simple)))

(defn some-test-one []
  (some #(= (get % "id") "1") data-raw-simple))

(defn every?-test-one []
  (every? #(= (get % "t") "T") every-check-data))

(defn every?-test-two []
  (every? #(= (get % "a") "A") every-check-data))

(defn update-test-one []
  (update data-raw-simple 2 (fn [val] (assoc val "id" "99"))))

(defn update-test-two []
  (update (first data-raw-simple) "id" #(str % "1")))

(defn test-impl [data-simple data nil-check-data every-check-data]
  (is (= (transform-test)
         (->> (gdal/->g data-simple)
              (gdal/g->))))
  (is (= (count-test)
         (gdal/count data-simple)))
  (is (= (keys-test)
         (->> (gdal/keys (gdal/get data-simple 0))
              (gdal/g->))))
  (is (= (get-in get-in-test-data [1 2])
         (gdal/get-in (gdal/->g get-in-test-data) [1 2])))
  (is (= (first data-raw)
         (->> (gdal/first (gdal/->g data-raw))
              (gdal/g->))))
  (is (= (second data-raw)
         (->> (gdal/second (gdal/->g data-raw))
              (gdal/g->))))
  (is (gdal/map? (gdal/->g {"t" "T"
                            "a" "A"})))
  (is (=
       (mapv-test)
       (->> (gdal/mapv (fn [a] (gdal/get a "a"))
                       data)
            (gdal/g->))))
  (is (= (filter-test)
         (->> (gdal/filter (fn [a] (= "B" (gdal/get a "a")))
                           data-simple)
              (gdal/g->))))
  (is (= (filter-index-test)
         (->> (gdal/filter-index (fn [a] (= "B" (gdal/get a "a")))
                                 data-simple)
              (gdal/g->))))
  (is (= (remove-test)
         (->> (gdal/remove (fn [a] (= "B" (gdal/get a "a")))
                           data-simple)
              (gdal/g->))))
  (is (= (concat-test)
         (->> (gdal/concat data-simple
                           data)
              (gdal/g->))))
  (is (= (conj-test)
         (->> (gdal/conj data
                         (gdal/->g {"a" "Z"}))
              (gdal/g->))))
  (is (= (assoc-test)
         (->> (gdal/assoc (gdal/get data 0)
                          "a"
                          "Z")
              (gdal/g->))))
  (is (= (join-test)
         (->> (gdal/mapv (fn [a] (gdal/get a "a"))
                         data-simple)
              (gdal/join-strings ",")
              (gdal/g->))))
  (is (= (select-keys-test)
         (->> (gdal/select-keys (gdal/get data 0)
                                ["a" "b"])
              (gdal/g->))))
  (is (= (reduce-test)
         (->> (gdal/reduce (fn [acc a]
                             (+ acc
                                (if (gdal/get a "a")
                                  1
                                  0)))
                           0
                           data)
              (gdal/g->))))
  (is (= (grp-by-test)
         (->> (gdal/group-by (fn [a] (gdal/get a "a"))
                             data-simple)
              (gdal/g->))))
  (is (= (grp-by-vector-test)
         (->> (gdal/group-by-expand-vectors (fn [a] (gdal/get a "a"))
                                            data)
              (gdal/g->))))
  (is (= (union-vec-test)
         (->> (gdal/union-vec data-simple
                              data)
              (gdal/g->)
              (sort-by #(get % "id")))))
  (is (= (intersection-vec-test)
         (->> (gdal/intersection-vec data-simple
                                     data)
              (gdal/g->)
              (sort-by #(get % "id")))))
  (is (= (union-test)
         (gdal/union data-simple
                     data)))
  (is (= (intersection-test)
         (gdal/intersection data-simple
                            data)))
  (is (= (sort-by-asc-test)
         (->> (gdal/sort-by-asc (fn [a]
                                  (if (vector? (gdal/get a "a"))
                                    (first (gdal/get a "a"))
                                    (gdal/get a "a")))
                                data)
              (gdal/g->))))
  (is (= (sort-by-desc-test)
         (->> (gdal/sort-by-dsc (fn [a]
                                  (if (vector? (gdal/get a "a"))
                                    (first (gdal/get a "a"))
                                    (gdal/get a "a")))
                                data)
              (gdal/g->))))
  (is (= (sort-by-asc-nil-test)
         (->> (gdal/sort-by-asc (fn [a]
                                  (if (vector? (gdal/get a "prop"))
                                    (first (gdal/get a "prop"))
                                    (gdal/get a "prop")))
                                nil-check-data)
              (gdal/g->))))
  (is (= (sort-by-desc-nil-test)
         (->> (gdal/sort-by-dsc (fn [a]
                                  (if (vector? (gdal/get a "prop"))
                                    (first (gdal/get a "prop"))
                                    (gdal/get a "prop")))
                                nil-check-data)
              (gdal/g->))))
  (is (= (reduce-union-one-test)
         (gdal/reduce (fn [acc element]
                        (set/union acc (->> (gdal/keys element)
                                            (gdal/g->)
                                            set)))
                      #{}
                      data-simple)))
  (is (= (coll?-test-one)
         (gdal/coll? (gdal/get data-simple 0))))
  (is (= (coll?-test-two)
         (gdal/coll? data-simple)))
  (is (= (merge-test)
         (->>
          (gdal/merge (gdal/get data-simple 0)
                      (gdal/get data-simple 1))
          (gdal/g->))))
  (is (= (some-test-one)
         (->> (gdal/some #(= (gdal/get % "id") "1") data-simple)
              (gdal/g->))))
  (is (= (every?-test-one)
         (->> (gdal/every? #(= (gdal/get % "t") "T")  every-check-data)
              (gdal/g->))))
  (is (= (every?-test-two)
         (->> (gdal/every?  #(= (gdal/get % "a") "A") every-check-data)
              (gdal/g->))))
  (is (= (update-test-one)
         (->> (gdal/update data-simple 2 (fn [val] (gdal/assoc val "id" "99")))
              (gdal/g->))))
  (is (= (update-test-two)
         (->> (gdal/update (gdal/get data-simple 0) "id" #(str % "1"))
              (gdal/g->)))))

(deftest functions-test
  (testing "tests normal impl"
    (with-redefs [gdal/->g gdsci/->g
                  gdal/g-> gdsci/g->
                  gdal/g? gdsci/g?
                  gdal/map? gdsci/map?
                  gdal/copy gdsci/copy
                  gdal/coll? gdsci/coll?
                  gdal/count gdsci/count
                  gdal/keys gdsci/keys
                  gdal/first gdsci/first
                  gdal/second gdsci/second
                  gdal/get gdsci/get
                  gdal/get-in gdsci/get-in
                  gdal/mapv gdsci/mapv
                  gdal/filter gdsci/filter
                  gdal/filter-index gdsci/filter-index
                  gdal/remove gdsci/remove
                  gdal/concat gdsci/concat
                  gdal/merge gdsci/merge
                  gdal/conj gdsci/conj
                  gdal/update gdsci/update
                  gdal/assoc gdsci/assoc
                  gdal/dissoc gdsci/dissoc
                  gdal/join-strings gdsci/join-strings
                  gdal/select-keys gdsci/select-keys
                  gdal/some gdsci/some
                  gdal/every? gdsci/every?
                  gdal/reduce gdsci/reduce
                  gdal/group-by gdsci/group-by
                  gdal/group-by-expand-vectors gdsci/group-by-expand-vectors
                  gdal/union gdsci/union
                  gdal/intersection gdsci/intersection
                  gdal/union-vec gdsci/union-vec
                  gdal/intersection-vec gdsci/intersection-vec
                  gdal/sort-by-asc gdsci/sort-by-asc
                  gdal/sort-by-dsc gdsci/sort-by-dsc
                  gdal/contains? gdsci/contains?]
      (let [data (gdal/->g data-raw)
            data-simple (gdal/->g data-raw-simple)
            nil-check-data (gdal/->g nil-check-data)
            every-check-data (gdal/->g every-check-data)]
        (test-impl data-simple data nil-check-data every-check-data))))
  (testing "tests js impl"
    (let [data (gdal/->g data-raw)
          data-simple (gdal/->g data-raw-simple)
          nil-check-data (gdal/->g nil-check-data)
          every-check-data (gdal/->g every-check-data)]
      (test-impl data-simple data nil-check-data every-check-data))))
