(ns de.explorama.backend.charts.ac-api
  (:require [de.explorama.backend.expdb.middleware.ac :as ac-api]))

(def attribute-blocklist
  "Blacklist to define which ACs should not send to Clients. This collection
  contains [<nodetype> <attribute>] tuples."
  #{["location" "Context"]})

(defn attributes []
  (ac-api/attributes {:allowed-types #{"Feature" "Date" "Context" "Datasource" "Fact"}
                      :blocklist attribute-blocklist}))
