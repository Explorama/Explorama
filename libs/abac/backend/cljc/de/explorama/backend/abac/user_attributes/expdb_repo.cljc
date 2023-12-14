(ns de.explorama.backend.abac.user-attributes.expdb-repo
  (:require [de.explorama.backend.abac.user-attributes.repository :as repo]
            [de.explorama.backend.expdb.middleware.db :as db]))

(def ^:private bucket "/abac/user_attributes/")

(deftype Backend []
  repo/Repository
  (get-attribute [instance attr]
    (-> (db/get+ bucket)
        (get attr)))
  (add-attribute [instance attr value]
    (if (some #{value} (repo/get-attribute instance attr))
      (repo/get-attribute instance attr)
      (db/set bucket attr value))))

(defn new-instance []
  (Backend.))
