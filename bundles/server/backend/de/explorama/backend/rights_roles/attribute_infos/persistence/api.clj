(ns de.explorama.backend.rights-roles.attribute-infos.persistence.api
  (:require [de.explorama.backend.rights-roles.attribute-infos.persistence.adapter-expdb :as expdb]
            [de.explorama.backend.rights-roles.attribute-infos.persistence.interface :as interface]))

(defonce ^:private instance (atom nil))

(defn init-instance [_]
  (reset! instance (expdb/new-instance)))

(defn blacklist-attribute-value [attribute value]
  (interface/blacklist-attribute-value @instance attribute value))

(defn whitelist-attriubte-values [attribute values]
  (interface/whitelist-attribute-values @instance attribute values))

(defn whitelist-attribute-values [attribute]
  (interface/get-whitelist-attribute-values @instance attribute))

(defn blacklist-attribute-values [attribute]
  (interface/blacklist-attribute-values @instance attribute))

(comment
  (init-instance {})

  (blacklist-attribute-value :role "users")

  (blacklist-attribute-values :role))