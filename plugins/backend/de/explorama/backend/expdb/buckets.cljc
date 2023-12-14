(ns de.explorama.backend.expdb.buckets
  (:require [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.persistence.backend-indexed :as indexed]
            [de.explorama.backend.expdb.persistence.backend-simple :as simple]))

(defn new-instance [bucket access-type]
  (let [bucket-config config-expdb/explorama-bucket-config]
    (cond (and (get bucket-config bucket)
               (= access-type :indexed))
          (let [bucket-config (assoc (get bucket-config bucket)
                                     :bucket bucket)]
            (indexed/new-instance bucket-config))
          (and (not (get bucket-config bucket))
               (= access-type :simple))
          (simple/new-instance (get bucket-config :simple) bucket)
          (or (and (get bucket-config bucket)
                   (= access-type :simple))
              (and (not (get bucket-config bucket))
                   (= access-type :indexed)))
          (throw (ex-info "Wrong db access" {:bucket bucket}))
          :else
          (throw (ex-info "Abort no valid bucket config for" {:bucket bucket})))))
