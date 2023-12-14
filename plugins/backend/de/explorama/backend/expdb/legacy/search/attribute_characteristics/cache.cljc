(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.cache
  (:require [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :refer [put-cache]]
            [de.explorama.shared.cache.util :refer [single-destructuring
                                                    single-return-type]]))

(defn- new-cache []
  (put-cache
   {:strategy config-expdb/explorama-expdb-cache
    :size config-expdb/explorama-expdb-cache-size}

   (atom {})
   (fn [_ _])))

(defonce ac-cache (atom nil))

(defn new-ac-cache []
  (reset! ac-cache (new-cache)))

(defn update-cache [schema selection-path result-selection]
  (single-destructuring (cache-api/lookup @ac-cache
                                          [(str schema "-" selection-path)]
                                          {:miss
                                           (fn [key _]
                                             (single-return-type key (result-selection)))})))
