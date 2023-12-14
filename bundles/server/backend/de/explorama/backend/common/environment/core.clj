
(ns de.explorama.backend.common.environment.core
  (:require [de.explorama.backend.common.environment.discovery :as discovery]
            [de.explorama.backend.common.middleware.cache-invalidate :as invalidate]
            [de.explorama.backend.common.middleware.data-provider :as provider]))

(defn discovery-client [service]
  (get {discovery/VERTICALS (atom {})
        discovery/CACHE_SERVICES (atom (reduce-kv (fn [acc plugin _]
                                                    (conj acc {:plugin plugin
                                                               :delete-by-query
                                                               (fn [sub params]
                                                                 (invalidate/send-invalidate plugin sub params))}))
                                                  []
                                                  @invalidate/functions))
        discovery/DATA_SERVICES (atom (reduce-kv (fn [acc plugin desc]
                                                   (assoc acc
                                                          (keyword plugin)
                                                          (merge {:plugin plugin}
                                                                 desc)))
                                                 {}
                                                 @provider/provider))}
       service))
