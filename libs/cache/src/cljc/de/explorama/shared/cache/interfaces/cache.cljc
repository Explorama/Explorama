(ns de.explorama.shared.cache.interfaces.cache
  (:refer-clojure :exclude [keys vals])
  (:require [clojure.core :as cc]))

(defprotocol Cache
  (lookup [this key] [this key not-found])

  (has? [this key])

  (keys [this])
  (vals [this])

  (hit [this key])

  (miss [this key value])
  (evict [this key]))

(deftype LRUCache [evict-cb
                   LIMIT
                   i
                   T->KEY
                   KEY->T
                   MAP]
  Cache

  (lookup [this key]
    (get MAP key))
  (lookup [this key not-found]
    (get MAP key not-found))

  (has? [this key]
    (boolean (get MAP key)))

  (keys [this]
    (cc/keys MAP))
  (vals [this]
    (cc/vals MAP))

  (hit [this key]
    (let [ni (inc i)]
      (LRUCache. evict-cb
                 LIMIT
                 ni
                 (assoc T->KEY ni key)
                 (assoc KEY->T key ni)
                 MAP)))

  (evict [this key]
    (when evict-cb
      (evict-cb key (get MAP key)))
    (let [ci (get KEY->T key)]
      (LRUCache. evict-cb
                 LIMIT
                 i
                 (dissoc T->KEY ci)
                 (dissoc KEY->T key)
                 (dissoc MAP key))))

  (miss [this key value]
    (let [[MAP T->KEY KEY->T] (if (<= LIMIT (count MAP))
                                (let [[evict-i evict-key] (first T->KEY)]
                                  (when evict-cb
                                    (evict-cb evict-key (get MAP evict-key)))
                                  [(dissoc MAP evict-key)
                                   (dissoc T->KEY evict-i)
                                   (dissoc KEY->T evict-key)])
                                [MAP T->KEY KEY->T])]
      (let [ni (inc i)]
        (LRUCache. evict-cb
                   LIMIT
                   ni
                   (assoc T->KEY ni key)
                   (assoc KEY->T key ni)
                   (assoc MAP key value))))))

(defn lru-cache [limit]
  (LRUCache. nil limit 0 (sorted-map) {} {}))

(defn lru-multi-cache [limit cb]
  (LRUCache. cb limit 0 (sorted-map) {} {}))