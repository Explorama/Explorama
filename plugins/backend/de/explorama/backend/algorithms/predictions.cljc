(ns de.explorama.backend.algorithms.predictions
  (:require [de.explorama.backend.algorithms.config :as config]
            [de.explorama.backend.common.environment.core :refer [discovery-client]]
            [de.explorama.backend.common.environment.discovery :refer [CACHE_SERVICES]]
            [de.explorama.backend.common.middleware.cache-invalidate :refer [send-invalidate]]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :refer [no-tiling-cache]]))

(defn new-cache [miss-fn]
  (no-tiling-cache
   {:strategy config/explorama-prediction-cache
    :size config/explorama-prediction-cache-size}

   (discovery-client CACHE_SERVICES)

   miss-fn
   send-invalidate))

(defonce ^:private local-cache (atom nil))

(defn init! [miss-fn]
  (reset! local-cache
          (new-cache miss-fn)))

(defn cache-lookup [data-tiles]
  (cache-api/lookup @local-cache data-tiles))

(defn new-train-cache [miss-fn]
  (no-tiling-cache
   {:strategy config/explorama-algorithm-work-cache
    :size config/explorama-algorithm-work-cache-size}

   (discovery-client CACHE_SERVICES)

   miss-fn
   send-invalidate))

(defonce ^:private train-local-cache (atom nil))

(defn train-init! [miss-fn]
  (reset! train-local-cache
          (new-train-cache miss-fn)))

(defn train-cache-lookup [training]
  (cache-api/lookup @train-local-cache [training]))
