(ns de.explorama.backend.indicator.ac-api
  (:require [de.explorama.backend.expdb.middleware.ac :as ac-api]))

(def attribute-blocklist
  "Blocklist to define which ACs should not send to Clients. This collection
  contains [<nodetype> <attribute>] tuples."
  #{["location" "Context"]})

(defn attribute-types []
  (ac-api/attribute-types {:blocklist attribute-blocklist}))
