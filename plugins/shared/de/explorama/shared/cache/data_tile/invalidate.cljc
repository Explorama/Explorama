(ns de.explorama.shared.cache.data-tile.invalidate
  (:require [clojure.set :as set]
            [de.explorama.shared.cache.interfaces.invalidate :as invalidate]
            [de.explorama.shared.cache.interfaces.storage :as exploramastorage]
            [taoensso.timbre :refer [debug error]])
  #?(:clj
     (:import [de.explorama.shared.cache.interfaces.invalidate Invalidation])))

(defn- query-match-fn
  "This function checks if 
   
   {\"datasource\" #{\"A\" \"b\"}}"
  [query]
  (fn [tile-k]
    (every? (fn [[dim matches]]
              (matches (get tile-k dim)))
            query)))

(defn- propagate-query-to-depended-services [cache-services
                                             k
                                             delete-propagation-req
                                             params
                                             sub]
  (doseq [[_ {service-sub :sub :as service-desc}] @cache-services
          :when (seq (set/intersection (set service-sub)
                                       (set sub)))]
    (try
      (debug "Propagate" k
             "for subed service"
             service-desc
             (assoc params :sub sub))
      (delete-propagation-req (get service-desc k)
                              (assoc params :sub sub))
      (catch #?(:clj Exception
                :cljs js/Error) e
        (error e "During publish" k service-desc)))))

(defn- delete-by-query [delete-propagation-req
                        cache-services
                        storage
                        {:keys [query]
                         :as params}
                        propagate?
                        before-propagation-fn]
  (debug "delete-by-query request " params)
  (when (and (map? query)
             (not-empty query))
    (let [match-fn (query-match-fn query)
          evicted-keys (filter match-fn (exploramastorage/all-keys storage))]
      (doseq [evicted-key evicted-keys]
        (exploramastorage/evict storage evicted-key))
      (when before-propagation-fn
        (before-propagation-fn evicted-keys))
      (when propagate?
        (propagate-query-to-depended-services cache-services
                                              :delete-by-query
                                              delete-propagation-req
                                              params
                                              propagate?))
      evicted-keys)))

(deftype DataTileInvalidate [cache-services
                             delete-propagation-req]
  #?(:clj Invalidation
     :cljs invalidate/Invalidation)
  (evict-by-query [_ storage params]
    (delete-by-query delete-propagation-req cache-services storage params nil nil))
  (evict-by-query [_ storage params propagate? before-propagation-fn]
    (delete-by-query delete-propagation-req cache-services storage params propagate? before-propagation-fn)))

(defn init [cache-services delete-propagation-req]
  (DataTileInvalidate. cache-services delete-propagation-req))