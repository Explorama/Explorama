(ns de.explorama.backend.expdb.persistence.backend-simple
  (:require [de.explorama.backend.expdb.persistence.simple :as itf]))

(defonce ^:private store (atom {}))

(deftype Backend [bucket config internal-data-store]
  itf/Simple
  (schema [_]
    bucket)

  (dump [_]
    @internal-data-store)
  (set-dump [_ data]
    (reset! internal-data-store data)
    {:success true
     :pairs (count data)})

  (del [_ key]
    (swap! internal-data-store dissoc key)
    {:success true
     :pairs -1})
  (del-bucket [_]
    (reset! internal-data-store {})
    {:success true
     :dropped-bucket? true})

  (get [_ key]
    (get @internal-data-store key))
  (get+ [_]
    @internal-data-store)
  (get+ [_ keys]
    (select-keys @internal-data-store
                 keys))

  (set [_ key value]
    (swap! internal-data-store assoc key value)
    {:success true
     :pairs 1})
  (set+ [_ data]
    (swap! internal-data-store merge data)
    {:success true
     :pairs (count data)}))

(defn new-instance [config bucket]
  (if-let [instance (get @store bucket)]
    instance
    (let [instance (Backend. bucket config (atom {}))]
      (swap! store assoc bucket instance)
      instance)))

(defn instances []
  @store)
