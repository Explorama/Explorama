(ns de.explorama.backend.map.ac-api
  (:require [de.explorama.backend.expdb.middleware.ac :as ac-api]))

(def attribute-blacklist
  #{"Location" "Context"})

(defn attributes []
  (ac-api/attributes {:allowed-types #{"Feature" "Date" "Context" "Datasource" "Fact"}}))
