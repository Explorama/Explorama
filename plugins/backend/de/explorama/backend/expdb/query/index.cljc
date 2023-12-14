(ns de.explorama.backend.expdb.query.index
  (:require [clojure.set :as set]
            [de.explorama.backend.expdb.persistence.indexed :as pers]
            [de.explorama.backend.expdb.utils :refer [expdb-hash]]))

;TODO r1/expdb not sure when to use hash and when real value
(defonce expdb-hash->dt-key (atom nil))
(defonce current (atom nil))
(defonce current-inv (atom nil))

(defn add-index! [instance new-data-tiles]
  ;TODO r1/expdb doing this twice is prbly bad
  (let [updates (reduce (fn [acc [data-tile-key {acs :acs
                                                 attributes :attributes}]]
                          (reduce (fn [acc [attribute values]]
                                    (reduce (fn [acc value]
                                              (update acc
                                                      [(get attributes attribute)
                                                       attribute
                                                       value]
                                                      (fnil conj #{}) (expdb-hash data-tile-key)))
                                            acc
                                            values))
                                  acc
                                  acs))
                        {}
                        new-data-tiles)
        updates-inv (reduce (fn [acc [data-tile-key {acs :acs
                                                     attributes :attributes}]]
                              (reduce (fn [acc [attribute values]]
                                        (reduce (fn [acc value]
                                                  (update acc
                                                          (expdb-hash data-tile-key)
                                                          (fnil conj #{}) [(get attributes attribute)
                                                                           attribute
                                                                           value]))
                                                acc
                                                values))
                                      acc
                                      acs))
                            {}
                            new-data-tiles)
        new-index (merge-with set/union
                              (get @current (pers/schema instance))
                              updates)
        new-index-inv (merge-with set/union
                                  (get @current-inv (pers/schema instance))
                                  updates-inv)
        new-dt-key-index (merge (get @expdb-hash->dt-key (pers/schema instance))
                                (into {} (map (fn [[data-tile-key]]
                                                [(expdb-hash data-tile-key) data-tile-key])
                                              new-data-tiles)))]
    (pers/set-index instance [new-index new-index-inv new-dt-key-index])
    (swap! expdb-hash->dt-key assoc (pers/schema instance) new-dt-key-index)
    (swap! current
           assoc
           (pers/schema instance)
           new-index)
    (swap! current-inv
           assoc
           (pers/schema instance)
           new-index-inv)
    {:success true}))

(defn rm-index! [instance delete-data-tiles]
  (let [update-hashes (map expdb-hash delete-data-tiles)
        deleted-pairs (select-keys
                       (get @current-inv (pers/schema instance))
                       update-hashes)
        new-index (persistent!
                   (reduce-kv (fn [acc dt-hash nodes]
                                (reduce (fn [acc node]
                                          (let [r (disj (get acc node) dt-hash)]
                                            (if (empty? r)
                                              (dissoc! acc node)
                                              (assoc! acc node r))))
                                        acc
                                        nodes))
                              (transient (get @current (pers/schema instance)))
                              deleted-pairs))

        new-index-inv (apply dissoc
                             (get @current-inv (pers/schema instance))
                             update-hashes)
        new-dt-key-index (apply dissoc
                                (get @expdb-hash->dt-key (pers/schema instance))
                                update-hashes)]
    (pers/set-index instance [new-index new-index-inv new-dt-key-index])
    (swap! expdb-hash->dt-key assoc (pers/schema instance) new-dt-key-index)
    (swap! current
           assoc
           (pers/schema instance)
           new-index)
    (swap! current-inv
           assoc
           (pers/schema instance)
           new-index-inv)
    {:success true}))
