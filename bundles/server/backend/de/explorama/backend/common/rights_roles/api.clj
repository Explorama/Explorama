(ns de.explorama.backend.common.rights-roles.api
  (:require [de.explorama.backend.environment.common.core :refer [discovery-client-reqw]]
            [de.explorama.backend.http :as http]))

;; This is (almost) identical to the same namespace from map.
;; If you need the same functionality, do not copy it (again).
;; Instead, refactor both to pull it to a library (maybe even to
;; service-base).
;; Problem right now is the reference to the mosaic (or map).services
;; namespace to fetch the "default" endpoint.

(defn- users!
  "Request `/api/all-users` from rights-roles vertical, and return just the
  `:username`s."
  ([]
   (users!
    {}))
  ([http-options]
   (discovery-client-reqw "verticals"
                          (fn call-fn [service]
                            (let [endpoint (get-in service [:rights-roles :url])]
                              (->> (http/request (merge {:request-method :get
                                                         :url            (str endpoint "/api/all-users")
                                                         :accept         :edn}
                                                        (or http-options {})))
                                   :body
                                   (mapv #(select-keys % [:username :name :mail])))))
                          20
                          (fn validate-fn [result]
                            (seq result)))))

(defn- roles!
  "Request `/api/possible-user-attribute-vals` from rights-roles vertical,
  and return just the `:role`s."
  ([]
   (roles!
    {}))
  ([http-options]
   (discovery-client-reqw "verticals"
                          (fn call-fn [service]
                            (let [endpoint (get-in service [:rights-roles :url])]
                              (->> (http/request (merge {:request-method :get
                                                         :url            (str endpoint "/api/possible-user-attribute-vals")
                                                         :accept         :edn}
                                                        (or http-options {})))
                                   :body
                                   :role)))
                          20
                          (fn validate-fn [result]
                            (seq result)))))

(defn roles-users!
  "Returns a map containing all possible roles and users."
  []
  {:roles (roles!)
   :users (users!)})
