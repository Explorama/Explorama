(ns de.explorama.backend.expdb.middleware.ac
  (:require [de.explorama.backend.expdb.legacy.search.attribute-characteristics.api :as search-api]
            [de.explorama.backend.expdb.legacy.search.data-tile :as data-tile]
            [de.explorama.backend.expdb.legacy.search.data-tile-ref :as dt-api]
            [de.explorama.backend.expdb.legacy.search.direct-search :as ds-api]))

(def attributes search-api/attributes)
(def attribute-types search-api/attribute-types)
(def attribute-values search-api/attribute-values)
(def data-tiles data-tile/get-data-tiles)
(def data-tiles-ref-api dt-api/get-data-tiles-api)
(def neighborhood search-api/neighborhood)
(def attribute-ranges search-api/ranges)
(def datasource-search ds-api/datasource-search)
(def grouped-search ds-api/grouped-search)
