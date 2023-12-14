(ns de.explorama.backend.mosaic.ac-api
  (:require [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]))

(defn datasource-options []
  (ac-api/attribute-values {:attributes [[attrs/datasource-attr attrs/datasource-node]]}))

(defn- datasource-query [datasource-attr datasource-val]
  [[datasource-attr
    {:values [datasource-val] :timestamp 1}]])

(defn datasource->attributes [datasource-attr datasource-val]
  (ac-api/attributes {:formdata (datasource-query datasource-attr datasource-val)
                      :allowed-types #{"Feature" "Date" "Context" "Datasource" "Fact"}}))
