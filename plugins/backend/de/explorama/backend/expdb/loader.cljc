(ns de.explorama.backend.expdb.loader
  (:require [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.buckets :as buckets]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.core :as legacy-ac-core]
            [de.explorama.backend.expdb.persistence.indexed :as pers]
            [de.explorama.backend.expdb.query.index :as idx]
            [taoensso.timbre :refer [info]]))

(defn index-init []
  (info "index init" config-expdb/explorama-bucket-config)
  (doseq [[conf-key] config-expdb/explorama-bucket-config]
    (let [instance (buckets/new-instance conf-key :indexed)
          [new-index new-index-inv new-dt-key-index] (pers/get-index instance)]
      (swap! idx/expdb-hash->dt-key assoc (pers/schema instance) new-dt-key-index)
      (swap! idx/current
             assoc
             (pers/schema instance)
             new-index)
      (swap! idx/current-inv
             assoc
             (pers/schema instance)
             new-index-inv)))
  (legacy-ac-core/all-attributes))
