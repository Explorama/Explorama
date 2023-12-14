(ns de.explorama.backend.expdb.query.graph
  (:require [de.explorama.backend.expdb.query.index :refer [current current-inv
                                                            expdb-hash->dt-key]]
            [de.explorama.backend.expdb.utils :refer [expdb-hash]]))

(defn dts-full [schema]
  (vals (get @expdb-hash->dt-key schema)))

(defn nodes [schema]
  (keys (get @current schema)))

(defn all-nodes []
  (keys (reduce (fn [acc [_ schema-nodes]]
                  (merge acc schema-nodes))
                {}
                @current)))

(defn dts [schema]
  (keys (get @current-inv schema)))

(defn node->dts [schema node]
  (get-in @current [schema node]))

(defn dt->nodes [schema dt]
  (if (number? dt)
    (get-in @current-inv [schema dt])
    (get-in @current-inv [schema (expdb-hash dt)])))

(defn uncond-neighborhood [schema nodes]
  (mapcat (fn [node]
            (mapcat (partial dt->nodes schema)
                    (node->dts schema node)))
          nodes))