(ns de.explorama.backend.expdb.persistence.db-api
  (:require [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.loader :as loader]
            [de.explorama.backend.expdb.persistence.backend-indexed :as indexed]
            [de.explorama.backend.expdb.persistence.backend-simple :as simple]
            [de.explorama.backend.expdb.persistence.indexed :as iindexed]
            [de.explorama.backend.expdb.persistence.simple :as isimple]
            [taoensso.timbre :refer [warn]]))

(defn load-buckets [{:keys [client-callback]}]
  (client-callback (into (mapv (fn [bucket]
                                 [:simple bucket])
                               (keys (simple/instances)))
                         (map (fn [[bucket]]
                                [:indexed bucket])
                              config-expdb/explorama-bucket-config))))

(defn download-bucket [{:keys [client-callback]} [type bucket-name]]
  (case type
    :simple (client-callback bucket-name (isimple/dump (get (simple/instances) bucket-name)))
    :indexed (client-callback bucket-name (iindexed/dump (get (indexed/instances) bucket-name)))))

(defn download-expdb [{:keys [client-callback]}]
  (let [simple (reduce (fn [acc [bucket-name instance]]
                         (assoc acc bucket-name (isimple/dump instance)))
                       {}
                       (simple/instances))
        indexed (reduce (fn [acc [bucket _]]
                          (assoc acc bucket (iindexed/dump (get (indexed/instances) bucket))))
                        {}
                        config-expdb/explorama-bucket-config)]
    (client-callback {:simple simple
                      :indexed indexed})))

(defn upload-bucket [{:keys [client-callback]} [type bucket-name content]]
  (try
    (case type
      :simple (isimple/set-dump (get (simple/instances) bucket-name) content)
      :indexed (do (iindexed/set-dump (get (indexed/instances) bucket-name) content)
                   (loader/index-init)
                   (cache-invalidate/send-invalidate #{"ac" "data"}
                                                     {"bucket" #{bucket-name}})))
    (client-callback true)
    (catch :default e
      (warn "Upload failed:" e)
      (client-callback false))))

(defn upload-expdb [{:keys [client-callback]} [{:keys [simple indexed]}]]
  (try
    (doseq [[bucket-name data] simple]
      (isimple/set-dump (get (simple/instances) bucket-name)
                        data))
    (doseq [[bucket-name data] indexed]
      (iindexed/set-dump (get (indexed/instances) bucket-name)
                         data))
    (loader/index-init)
    (cache-invalidate/send-invalidate #{"ac" "data"}
                                      {"identifier" #{"search"}})
    (client-callback true)
    (catch :default e
      (warn "Upload failed:" e)
      (client-callback false))))
