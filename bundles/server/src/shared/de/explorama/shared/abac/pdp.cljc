(ns de.explorama.shared.abac.pdp
  (:require [de.explorama.shared.abac.pip :as pip]
            [de.explorama.shared.abac.policy-repository :as pr]
            [de.explorama.shared.abac.util :as util]
            #?(:clj [taoensso.timbre :refer [error]])))

(defn attribute-check
  [attr
   policy
   {valid? :valid? :as init}
   [policy-attribute-key policy-attribute-values]]
  (let [attribute-values (get attr policy-attribute-key)
        new-valid? (or
                    (not (nil? ((set policy-attribute-values) "*")))
                    (not (nil? (some
                                (set (if (or (vector? attribute-values)
                                             (seq? attribute-values))
                                       attribute-values
                                       [attribute-values]))
                                policy-attribute-values))))]
    (cond-> (assoc init :valid? (and valid? new-valid?))
      (not new-valid?) (update-in [:failed]
                                  conj
                                  [(:id policy)
                                   (:name policy)
                                   policy-attribute-key
                                   (get attr policy-attribute-key)
                                   policy-attribute-values]))))

(defn check-policy- [optional required attributes data policy init-check]
  (if (and (nil? optional) (nil? required))
    (reduce (partial attribute-check data policy)
            init-check
            attributes)
    (let [required-result     (reduce (partial attribute-check data policy)
                                      init-check
                                      (select-keys attributes required))
          has-optionals?      (not-empty optional)
          optional-attributes (select-keys attributes optional)]
      (loop [result          required-result
             rest-attributes (rest optional-attributes)
             cur-attribute   (first optional-attributes)
             matched-once?   false]
        (cond
          (not (:valid? result)) result
          (not has-optionals?) result
          matched-once? result
          (and has-optionals?
               (not matched-once?)
               (nil? cur-attribute)) {:valid? false
                                      :failed ["no optional attribute matched"]}
          :else (let [{valid? :valid?
                       :as    attr-check-result} (attribute-check data policy result cur-attribute)]
                  (if valid?
                    (recur attr-check-result
                           (rest rest-attributes)
                           (first rest-attributes)
                           true)
                    (recur result
                           (rest rest-attributes)
                           (first rest-attributes)
                           false))))))))

(defn check-policy [user obj-attrs init policy]
  (if (and (empty? (:user-attributes policy)) (empty? (:data-attributes policy)))
    {:valid? false
     :failed ["policy has no attributes"]}
    (let [required-user-attrs (get-in policy [:user-attributes :required])
          optional-user-attrs (get-in policy [:user-attributes :optional])
          user-attributes     (or (get-in policy [:user-attributes :attributes])
                                  (:user-attributes policy))
          user-attributes (if (contains? user-attributes :username)
                            (update user-attributes :username
                                    #(mapv util/normalize-username %))
                            user-attributes)
          required-obj-attrs  (get-in policy [:data-attributes :required])
          optional-obj-attrs  (get-in policy [:data-attributes :optional])
          object-attributes   (or (get-in policy [:data-attributes :attributes])
                                  (:data-attributes policy))]
      (as->
       init
       $
        (check-policy- optional-user-attrs required-user-attrs user-attributes user policy $)
        (check-policy- optional-obj-attrs required-obj-attrs object-attributes obj-attrs policy $)))))

(defn call
  [action-key user data]
  (let [obj-attributes (pip/fetch-obj-attrs action-key)
        policy         (-> action-key
                           pr/fetch-policy
                           util/role-lookup-in-policies)]
    (if (empty? policy)
      {:valid? false
       :failed ["no policy"]}
      (check-policy user
                    (or data obj-attributes)
                    {:valid? true
                     :policy policy
                     :failed []}
                    policy))))

(defn call-request [body]
  (call (util/create-keyword (:feature body))
        (:user body)
        (:data body)))

#?(:clj
   (def pdp-call-route
     ["/pdp-call"
      {:post {:handler (fn [{params :parameters}]
                         (error "not implemented")
                         {:status 200
                          :body {:result (-> params
                                             call-request
                                             (select-keys [:valid? :failed]))}})}}]))
