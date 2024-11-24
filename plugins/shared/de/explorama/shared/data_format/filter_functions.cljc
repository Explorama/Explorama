(ns de.explorama.shared.data-format.filter-functions
  (:refer-clojure :exclude [filter filterv merge map
                            mapv group-by get assoc
                            dissoc update coll? some
                            every? contains? conj concat
                            reduce])
  (:require [clojure.core :as cc]
            [clojure.set :as cs]
            [de.explorama.shared.common.data.attributes :as attrs]))

(defprotocol FilterFunctions
  (->ds [this c])
  (ds-> [this c])
  (assoc [this m k v])
  (coll? [this c])
  (concat [this c1 c2])
  (conj [this c v])
  (contains? [this c k])
  (difference [this c1 c2])
  (dissoc [this m ks])
  (every? [this f c])
  (filter [this f c])
  (filterv [this f c])
  (get [this c k])
  (group-by [this f c])
  (intersection [this c1 c2])
  (map [this f c])
  (mapv [this f c])
  (merge [this m1 m2])
  (reduce [this f init c])
  (some [this f c])
  (union [this c1 c2])
  (update [this c k f]))

(def default-impl
  (reify FilterFunctions
    (->ds [this c]
      c)
    (ds-> [this c]
      c)
    (assoc [this m k v]
      (cc/assoc m k v))
    (coll? [this c]
      (cc/coll? c))
    (concat [this c1 c2]
      (cc/concat c1 c2))
    (conj [this c v]
      (cc/conj c v))
    (contains? [this c k]
      (cc/contains? c k))
    (difference [this c1 c2]
      (cs/difference c1 c2))
    (dissoc [this m ks]
      (apply cc/dissoc m ks))
    (every? [this f c]
      (cc/every? f c))
    (filter [this f c]
      (cc/filter f c))
    (filterv [this f c]
      (cc/filterv f c))
    (get [this c k]
      (attrs/value c k))
    (group-by [this f c]
      (cc/group-by f c))
    (intersection [this c1 c2]
      (cs/intersection c1 c2))
    (map [this f c]
      (cc/map f c))
    (mapv [this f c]
      (cc/mapv f c))
    (merge [this m1 m2]
      (cc/merge m1 m2))
    (reduce [this f init c]
      (cc/reduce f init c))
    (some [this f c]
      (cc/some f c))
    (union [this c1 c2]
      (cs/union c1 c2))
    (update [this c k f]
      (cc/update c k f))))