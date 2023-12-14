(ns de.explorama.backend.search.client-api
  (:require [clojure.set :as set]
            [de.explorama.backend.common.config :as config-backend]
            [de.explorama.backend.common.data.descriptions :as descs]
            [de.explorama.backend.search.attribute-characteristics.api :as ac-api]
            [de.explorama.backend.search.config :as config-search]
            [de.explorama.backend.search.datainstance.core :as di]
            [de.explorama.backend.search.direct-search :as direct-search]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :refer [debug error]]))

(defn recalc-traffic-light-status [callback-fn frame-id formdata token]
  (let [{:keys [size status] :as tf-status} (di/traffic-light-status formdata)]
    (debug "Request-traffic-lights"
           :frame frame-id
           :formdata formdata
           :size size
           :status status)
    (callback-fn frame-id
                 tf-status
                 token)))

(defn formdata-with-enabled-sources [formdata enabled-sources]
  (if config-backend/explorama-datasource-access-control-enabled
    (let [used-sources (if (empty? enabled-sources)
                         ["_"] ;; attributes-by needs an invalid datasource to exclude all nodes
                         enabled-sources)
          formdata-with-removed-sources (filter (fn [[[attr-name attr-type] _]] (not= attr-name attrs/datasource-attr)) formdata)]
      (if (and (< 0 (count formdata)) (not= (count formdata) (count formdata-with-removed-sources)))
        formdata ;; we should leave explicitly selected datasources as is or attributes from other sources won't get filtered out
        (vec (conj formdata-with-removed-sources
                   [[attrs/datasource-attr attrs/datasource-node]
                    {:values used-sources :timestamp 1 :valid? true}]))))
    formdata))

(defn enabled-sources-vec [user-info]
  (let [all-sources (set (first (vals (ac-api/search-options [[attrs/datasource-attr attrs/datasource-node]] []))))]
    (if config-backend/explorama-datasource-access-control-enabled
      (let [{:keys [role username]} user-info
            name-enabled-sources (set (get config-backend/explorama-enabled-datasources-by-name username))
            role-enabled-sources (set (if (vector? role)
                                        (apply concat
                                               (-> (select-keys config-backend/explorama-enabled-datasources-by-role role)
                                                   vals))
                                        (get config-backend/explorama-enabled-datasources-by-role role)))]
        (vec (set/intersection all-sources (set/union name-enabled-sources role-enabled-sources))))
      (vec all-sources))))

(defn initialize [{:keys [client-callback]} [user-info]]
  (let [datasources (enabled-sources-vec user-info)
        bucket-datasources (ac-api/bucket-datasources)]
    (debug "datasources" datasources)
    (client-callback {:search-parameter-config config-search/explorama-search-parameter-config
                      :attr-types (ac-api/attribute-types)
                      :enabled-datasources datasources
                      :bucket-datasources bucket-datasources})))

(defn direct-search-handler [{:keys [client-callback]} [search-config query]]
  (let [datasources (get search-config 2)
        excluded-fd (formdata-with-enabled-sources [] datasources)
        search-result (direct-search/search excluded-fd datasources search-config query)]
    (client-callback search-result)))

(defn create-di-handler [{:keys [client-callback]} [datasources frame-id formdata callback-event]]
  (debug "create datainstance" frame-id "," formdata)
  (let [excluded-fd (formdata-with-enabled-sources formdata datasources)
        di (di/gen-di excluded-fd)]
    (client-callback frame-id di callback-event)))

(defn recalc-traffic-lights [{:keys [client-callback]} [datasources frame-id formdata token]]
  (debug "Recalc traffic lights"  {:frame-id frame-id
                                   :datasources datasources
                                   :formdata formdata
                                   :token token})
  (let [excluded-fd (formdata-with-enabled-sources formdata datasources)]
    (recalc-traffic-light-status client-callback frame-id excluded-fd token)))

(defn request-attributes [{:keys [client-callback]} [datasources frame-id row-attrs formdata callback-event]]
  (debug "Request-attributes" {:frame-id frame-id
                               :row-attrs row-attrs
                               :datasources datasources
                               :formdata formdata})
  (let [excluded-fd (formdata-with-enabled-sources formdata datasources)
        attributes (ac-api/possible-attributes row-attrs excluded-fd)]
    (client-callback frame-id attributes callback-event)))

(defn search-options [{:keys [client-callback]} [datasources frame-id attributes formdata callback-event]]
  (debug "req search-options" {:frame-id frame-id
                               :attributes attributes
                               :formdata formdata
                               :datasources datasources})
  (let [excluded-fd (formdata-with-enabled-sources formdata datasources)
        result-options (ac-api/search-options attributes excluded-fd)]
    (client-callback frame-id result-options callback-event)))

(defn search-bar-find-elements [{:keys [client-callback]} [datasources frame-id labels lang search-term formdata search-config task-id]]
  (let [excluded-fd (formdata-with-enabled-sources formdata datasources)]
    (direct-search/search-elements client-callback frame-id labels lang search-term excluded-fd datasources search-config task-id)))

(defn data-descs [{:keys [client-callback failed-callback]} [{:keys [language]}]]
  (debug "Request data-descs")
  (try
    (let [bucket-datasources (ac-api/bucket-datasources)
          get-ds-desc (fn [datasource]
                        (descs/datasource-desc {:datasource datasource
                                                :language language}))]
      (client-callback
       (reduce (fn [acc [bucket datasources]]
                 (cond-> acc
                   (get bucket-datasources bucket)
                   (assoc bucket
                          (reduce (fn [acc datasource]
                                    (-> acc
                                        (assoc datasource (get-ds-desc datasource))))
                                  {}
                                  datasources))))
               {}
               bucket-datasources)))
    (catch #?(:clj Throwable :cljs :default) e
      (error e "Failed to get data descs")
      (failed-callback))))