(ns de.explorama.backend.expdb.persistence.interceptor
  (:require [de.explorama.backend.expdb.persistence.indexed :as persistence]
            [de.explorama.backend.expdb.query.index :as index]))

(defn delete-all [instance]
  (persistence/set-index instance [{} {} {}])
  (swap! index/expdb-hash->dt-key dissoc (persistence/schema instance))
  (swap! index/current dissoc (persistence/schema instance))
  (swap! index/current-inv dissoc (persistence/schema instance))
  {:result (persistence/delete-all instance)})

(defn delete [instance params])