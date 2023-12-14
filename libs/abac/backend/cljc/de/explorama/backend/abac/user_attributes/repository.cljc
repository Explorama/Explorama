(ns de.explorama.backend.abac.user-attributes.repository)

(defprotocol Repository
  (get-attribute [instance attr])
  (add-attribute [instance attr value]))
