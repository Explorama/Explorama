(ns de.explorama.backend.abac.pap
  (:require [clojure.string :as str]
            [de.explorama.shared.abac.config :as config]
            [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.shared.abac.pep :as pep]
            [de.explorama.shared.abac.policy-repository :as pr]
            [de.explorama.shared.abac.user-attributes.core :as user-attr]
            [de.explorama.shared.abac.util :as util :refer [user-info-role-fix]]
            [jsonista.core :as json]))

(def button {:name :abac-dialog-name
             :operation :add
             :dialog-title :rights-roles-add-role-title
             :dialog-label :rights-roles-add-role-label})

(def ^:dynamic *grp-key*)

(defn role-in-policy? [policy role]
  (boolean
   ((set (or (get-in policy [:user-attributes :role])
             (get-in policy [:user-attributes :attributes :role])))
    role)))

(defn policy-checkbox
  [policy-value role]
  (let [creator (get policy-value :creator)]
    {:name (str (:name policy-value)
                (when creator
                  (str " (" creator ")")))
     :value
     (if role
       (role-in-policy? policy-value role)
       false)}))

(defn policy-drop-down [pol1 pol2 role]
  (let [read-pol (first (filter #(str/ends-with? (or (:id %) "") "read")
                                [pol1 pol2]))
        write-pol (first (filter #(str/ends-with? (or (:id %) "") "write")
                                 [pol1 pol2]))
        read-rights? (role-in-policy? read-pol role)
        write-rights? (role-in-policy? write-pol role)
        group-name (get pol1 :group-name)
        id (get pol1 :group-conf)
        creator (get pol1 :creator)]
    {:name (str group-name
                (when creator
                  (str " (" creator ")")))
     :selected (cond
                 write-rights? (str "write__" id)
                 read-rights? (str "read__" id)
                 :else (str "none__" id))
     :options [{:display-name "read-rights"
                :name (str "read__" id)}
               {:display-name "write-rights"
                :name (str "write__" id)}
               {:display-name (keyword "none-rights")
                :name (str "none__" id)}]}))

(def ^:private group-conf-blacklist
  #{:map-designer :marker-info :layout-designer})

(defn group-to-config-layout
  [policies parent role]
  (let [grouped-pol (group-by (fn [[_ {id :group-id}]]
                                id)
                              (if config/explorama-filter-config-policies
                                (filter (fn [[_ {:keys [group-conf]}]]
                                          (not (group-conf-blacklist (keyword group-conf))))
                                        policies)
                                policies))
        no-groups (get grouped-pol nil)
        all-grouped (dissoc grouped-pol nil)
        second-grp-key (str *grp-key* "-layouts")]
    (cond-> parent
      :always
      (assoc-in [:tabs :Groups :tabs role]
                (reduce (fn [parent [policy-key policy-value]]
                          (let [{pol-desc-name :name
                                 :as pol-desc} (policy-checkbox policy-value role)]
                            (if (seq pol-desc-name)
                              (assoc-in parent
                                        [:groups *grp-key* :checkboxes policy-key]
                                        pol-desc)
                              parent)))
                        {:name role
                         :groups {*grp-key* {:name (keyword (str (util/stringify-keyword *grp-key*) "-grp-rights-and-roles"))
                                             :labels {*grp-key* {:name *grp-key*}}}}}
                        no-groups))
      (not-empty all-grouped)
      (update-in [:tabs :Groups :tabs role]
                 (fn [v]
                   (reduce (fn [parent [group-key [[_ pol1] [_ pol2]]]]
                             (assoc-in parent
                                       [:groups second-grp-key :drop-downs group-key]
                                       (policy-drop-down pol1 pol2 role)))
                           (assoc-in v
                                     [:groups second-grp-key]
                                     {:name (keyword (str (util/stringify-keyword second-grp-key) "-grp-rights-and-roles"))
                                      :labels {second-grp-key {:name second-grp-key}}})
                           all-grouped))))))

(defn policies-to-config-layout
  ([policies roles filter-func]
   (let [pols (if (fn? filter-func)
                (into {}
                      (filter filter-func
                              policies))
                policies)]
     (reduce
      (partial group-to-config-layout pols)
      {:tabs {:Groups {:name :abac-groups-main-tab
                       :icon :users
                       :tabs {:tab-operation button}}}}
      roles)))

  ([policies roles]
   (policies-to-config-layout policies roles nil)))

(defn register-feature
  ([action-key policy-name user-roles]
   (when (not (pr/fetch-policy action-key))
     (pr/create-policy action-key {:user-attributes (if user-roles
                                                      {:role user-roles}
                                                      {})
                                   :data-attributes {}
                                   :id action-key
                                   :name policy-name})))
  ([{action-id :feature-id feature-name :name user-roles :roles}]
   (let [action-key (util/create-keyword action-id)]
     (when (not (pr/fetch-policy action-key))
       (register-feature action-key feature-name user-roles)))))

(def pap-all-get-route
  ["/pap-all"
   {:get {:handler
          (fn [{user-info :params}]
            (let [user-info (user-info-role-fix user-info)]
              (if (jwt/user-valid? user-info)
                (pep/checked-function-call
                 :rights-and-roles
                 user-info
                 {}
                 (fn [] {:status 200
                         :content-type "application/json"
                         :body (json/write-value-as-string
                                (policies-to-config-layout
                                 (pr/fetch-multiple-policies [])
                                 (user-attr/fetch-attr-values :role)))})
                 (fn [_] {:status 403}))
                {:status 403
                 :body {}})))}}])

(defn pap-all-filtered-route [filter-func]
  ["/pap-all"
   {:get {:handler
          (fn [{user-info :params}]
            (let [user-info (user-info-role-fix user-info)]
              (if (jwt/user-valid? user-info)
                (pep/checked-function-call
                 :rights-and-roles
                 user-info
                 {}
                 (fn [] {:status 200
                         :content-type "application/json"
                         :body (json/write-value-as-string
                                (policies-to-config-layout
                                 (pr/fetch-multiple-policies [])
                                 (user-attr/fetch-attr-values :role)
                                 filter-func))})
                 (fn [_] {:status 403}))
                {:status 403
                 :body {}})))}}])

(defn create-or-modify-policies
  [{changes :changes
    user-info :user}]
  (pep/checked-function-call
   :rights-and-roles
   user-info
   {}
   #(mapv (fn [[path value]]
            (when (< 7 (count path))
              (let [keyword-path (mapv util/create-keyword path)
                    [tab-key _ role-key _ _ element-key feature-key _] keyword-path
                    grouped-policy? (= element-key :drop-downs)]
                (when (= :Groups tab-key)
                  (let [role-name (name role-key)]
                    (user-attr/add-attr-value :role role-name)
                    (pr/update-policy feature-key role-name grouped-policy? value))))))
          changes)
   (fn [_] {:status 403})))

(defn get-capabilities
  []
  (reduce
   (fn [parent [policy-key policy-value]]
     (assoc-in parent
               [:groups *grp-key* :checkboxes policy-key]
               (policy-checkbox policy-value false)))
   {:groups {*grp-key* {:name (keyword (str (util/stringify-keyword *grp-key*) "-grp-rights-and-roles"))
                        :labels {*grp-key* {:name *grp-key*}}}}}
   (pr/fetch-multiple-policies [])))

(def pap-save-all-route
  ["/pap-save"
   {:post {:handler
           (fn
             [{body :body}]
             (let [body (-> body
                            (json/read-value json/keyword-keys-object-mapper))
                   user-info (:user body)]
               (if (jwt/user-valid? user-info)
                 {:status 200
                  :content-type "application/json"
                  :body (-> body
                            create-or-modify-policies
                            json/write-value-as-string)}
                 {:status 403
                  :body {}})))}}])

(def pap-register-feature-all-route
  ["/pap-register"
   {:post {:handler
           (fn [{body :body}]
             {:status 200
              :content-type "application/json"
              :body (-> body
                        (json/read-value json/keyword-keys-object-mapper)
                        register-feature
                        json/write-value-as-string)})}}])

(def pap-capabilites
  ["/pap-capabilites"
   {:get {:handler
          (fn [{body :body}]
            (let [{user-info :user} (-> body
                                        (json/read-value json/keyword-keys-object-mapper))
                  user-info (user-info-role-fix user-info)]
              (if (jwt/user-valid? user-info)
                {:status 200
                 :content-type "application/json"
                 :body (json/write-value-as-string (get-capabilities))}
                {:status 403
                 :body {}})))}}])

(defn pap-reset [default-counfig debug-features?]
  ["/pap-reset"
   {:delete {:handler
             (fn [{headers :headers}]
               (cond
                 (and debug-features?
                      (jwt/admin-claim-valid? (get headers "token")))
                 {:status 200
                  :content-type "application/json"
                  :body (json/write-value-as-string (pr/reset-repo default-counfig))}
                 debug-features?
                 {:status 403
                  :body {}}
                 :else
                 {:status  404
                  :headers {}}))}}])

(defn wrap-abac-pap [handler vertical-grp-key]
  (fn [request]
    (binding [*grp-key* vertical-grp-key]
      (handler request))))
