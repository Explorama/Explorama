(ns de.explorama.frontend.mosaic.data-structure.cljs-impl
  (:refer-clojure :exclude [filter filterv merge map
                            mapv group-by get assoc
                            dissoc update coll? some every?
                            contains? count keys reduce
                            remove concat conj select-keys
                            get-in map? first second])
  (:require [clojure.core :as cc]
            [clojure.set :as set]
            [clojure.string :as string]))

(defn ->g [c]
  c)
(defn g-> [c]
  c)
(defn g->keywords [c]
  c)
(defn g? [v]
  (vector? v))
(defn map? [m]
  (cc/map? m))
(defn copy [c]
  c)
(defn coll? [c]
  (cc/coll? c))
(defn count [c]
  (cc/count c))
(defn keys [c]
  (cc/keys c))
(defn first [v]
  (cc/first v))
(defn second [v]
  (cc/second v))
(defn get
  ([c k]
   (cc/get c k))
  ([c k default]
   (cc/get c k default)))
(defn get-in
  ([c ks]
   (cc/get-in c ks))
  ([c ks default]
   (cc/get-in c ks default)))
(defn filter [f c]
      (cc/filterv f c))
(defn filter-index [f c]
  (transduce (comp (map-indexed vector)
                   (cc/filter (fn [[_ val]]
                                (f val)))
                   (cc/map first))
             cc/conj
             []
             c))
(defn remove [f c]
  (vec (cc/remove f c)))
(defn concat [c1 c2]
  (vec (cc/concat c1 c2)))
(defn merge [c1 c2]
  (cc/merge c1 c2))
(defn conj [c v]
  (cc/conj c v))
(defn update [c k f]
  (cc/update c k f))
(defn assoc [c k v]
  (cc/assoc c k v))
(defn dissoc [c ks]
  (apply cc/dissoc c ks))
(defn join-strings [s c]
  (string/join s c))
(defn select-keys [c ks]
  (cc/select-keys c ks))
(defn some [f c]
  (cc/some f c))
(defn every? [f c]
  (cc/every? f c))
(defn reduce [f init c]
  (cc/reduce f init c))
(defn mapv [f c]
  (cc/mapv f c))
(defn group-by [f c]
  (cc/group-by f c))
(defn group-by-expand-vectors [f c]
  (cc/reduce (fn [result element]
               (let [value (f element)]
                 (if (and (not (empty? value))
                          (vector? value))
                   (cc/reduce (fn [result value]
                                (cc/update result value (fn [val]
                                                          (if val
                                                            (cc/conj val element)
                                                            [element]))))
                              result
                              value)
                   (cc/update result value (fn [val]
                                             (if val
                                               (cc/conj val element)
                                               [element]))))))
             {}
             c))
(defn union [c1 c2]
  (let [c1 (if (set? c1) c1 (set c1))
        c2 (if (set? c2) c2 (set c2))]
    (set/union c1 c2)))
(defn intersection [c1 c2]
  (let [c1 (if (set? c1) c1 (set c1))
        c2 (if (set? c2) c2 (set c2))]
    (set/intersection c1 c2)))
(defn union-vec [c1 c2]
  (vec (union c1 c2)))
(defn intersection-vec [c1 c2]
  (vec (intersection c1 c2)))
(defn sort-by-asc [f c]
  (vec (cc/sort-by f c)))
(defn sort-by-dsc [f c]
  (vec (cc/sort-by f #(compare %2 %1) c)))
(defn contains? [c k]
  (cc/contains? c k))