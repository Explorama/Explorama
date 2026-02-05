(ns de.explorama.backend.rights-roles.attribute-infos.persistence.adapter-expdb
  (:require [de.explorama.backend.expdb.middleware.db :as expdb]
            [de.explorama.backend.rights-roles.attribute-infos.persistence.interface :as interface]))

(def ^:private bucket "rights-roles")

(defn- expdb-key [key-path & [attribute list-type]]
  (cond-> key-path
    attribute (str "/" attribute)
    list-type (str "/" list-type)))

(defn- current-attribute-state [key-path attribute]
  (let [whitelist-key (expdb-key key-path attribute :whitelist)
        blacklist-key (expdb-key key-path attribute :blacklist)]
    [(expdb/get bucket whitelist-key)
     (expdb/get bucket blacklist-key)]))

(defn- new-attribute-state [key-path attribute whitelist blacklist]
  (let [whitelist-key (expdb-key key-path attribute :whitelist)
        blacklist-key (expdb-key key-path attribute :blacklist)]
    [(expdb/set bucket whitelist-key whitelist)
     (expdb/set bucket blacklist-key blacklist)]))

(defn- blacklist-attribute-value [key-path cache attribute value]
  (let [whitelist-key (expdb-key key-path attribute :whitelist)
        blacklist-key (expdb-key key-path attribute :blacklist)
        [whitelist blacklist] (current-attribute-state key-path
                                                       attribute)
        updated-whitelist ((fnil disj #{}) whitelist value)
        updated-blacklist ((fnil conj #{}) blacklist value)]
    (new-attribute-state key-path
                         attribute
                         updated-whitelist
                         updated-blacklist)
    (-> cache
        (assoc whitelist-key updated-whitelist)
        (assoc blacklist-key updated-blacklist))))

(defn- whitelist-attribute-values [key-path cache attribute values]
  (let [whitelist-key (expdb-key key-path attribute :whitelist)
        blacklist-key (expdb-key key-path attribute :blacklist)
        [whitelist blacklist] (current-attribute-state 
                                                       key-path
                                                       attribute)
        updated-whitelist (reduce (fnil conj #{}) whitelist values)
        updated-blacklist (reduce (fnil disj #{}) blacklist values)]
    (new-attribute-state 
                         key-path
                         attribute
                         updated-whitelist
                         updated-blacklist)
    (-> cache
        (assoc whitelist-key updated-whitelist)
        (assoc blacklist-key updated-blacklist))))

(defn- get-whitelist-attribute-values [key-path cache attribute]
  (let [whitelist-key (expdb-key key-path attribute :whitelist)]
    (get cache whitelist-key #{})))

(defn- blacklist-attribute-values [key-path cache attribute]
  (let [blacklist-key (expdb-key key-path attribute :blacklist)]
    (get cache blacklist-key #{})))

(deftype Persistence-Expdb [key-path
                            ^:unsynchronized-mutable cache]
  interface/Attributes-Persistence
  (blacklist-attribute-value [_ attribute value]
    (set! cache
          (blacklist-attribute-value key-path cache attribute value)))
  (whitelist-attribute-values [_ attribute values]
    (set! cache
          (whitelist-attribute-values key-path cache attribute values)))
  (get-whitelist-attribute-values [_ attribute]
    (get-whitelist-attribute-values key-path cache attribute))
  (blacklist-attribute-values [_ attribute]
    (blacklist-attribute-values key-path cache attribute)))

(defn new-instance []
  (let [key-path (str "attributes/")
        init-state (expdb/get+ bucket)]
    (->Persistence-Expdb key-path init-state)))