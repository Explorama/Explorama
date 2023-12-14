(ns de.explorama.shared.cache.data-tile.local.retrieval
  (:require [de.explorama.shared.cache.data-tile.retrieval :as dt-retrieval]
            [de.explorama.shared.cache.interfaces.retrieval :as exploramaretrieval])
  #?(:clj
     (:import [de.explorama.shared.cache.interfaces.retrieval Retrieval])))

(deftype LocalDataTileRetrieval [config miss]
  #?(:clj Retrieval
     :cljs exploramaretrieval/Retrieval)
  (exploramaretrieval/miss [_ data-tiles opts]
    (dt-retrieval/request-data-tiles config miss data-tiles opts)))

(defn init [config miss]
  (LocalDataTileRetrieval. config
                           miss))