(ns de.explorama.backend.rights-roles.attribute-infos.persistence.interface)

(defprotocol Attributes-Persistence
  (blacklist-attribute-value [instance attribute value])
  (whitelist-attribute-values [instance attribute values])
  (get-whitelist-attribute-values [instance attribute])
  (blacklist-attribute-values [instance attribute]))