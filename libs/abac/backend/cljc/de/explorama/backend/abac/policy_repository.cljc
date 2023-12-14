(ns de.explorama.backend.abac.policy-repository
  (:require [clojure.string :as string]
            [clojure.walk :refer [postwalk]]
            [de.explorama.backend.abac.repository-adapter.adapter :as adapter]
            [de.explorama.backend.abac.repository-adapter.expdb-policy-repository :as expdb]
            [de.explorama.backend.abac.util :as util]))

(defonce backend (atom nil))

(def lock (atom 0))

(defn set-backend []
  (reset! backend (expdb/new-adapter)))

(defn create-policy
  ([id data]
   (let [updated-data (util/role-lookup-in-policies data)]
     (locking lock
       (create-policy id updated-data lock))))
  ([id data lock]
   (adapter/create @backend id data)))

(defn fetch-policy
  ([id]
   (locking lock
     (fetch-policy id lock)))
  ([id lock]
   (adapter/fetch @backend id)))

(defn fetch-multiple-policies
  ([ids]
   (locking lock
     (fetch-multiple-policies ids lock)))
  ([ids lock]
   (adapter/fetch-multiple @backend ids)))

(defn edit-policy
  ([id data]
   (locking lock
     (edit-policy id data lock)))
  ([id data lock]
   (adapter/edit @backend id data)))

(defn edit-all-policies
  ([data]
   (locking lock
     (edit-all-policies data lock)))
  ([data lock]
   (adapter/edit-all @backend data)))

(defn delete-policy
  ([id]
   (locking lock
     (delete-policy id lock)))
  ([id lock]
   (adapter/delete @backend id)))

(defn reset-repo
  ([default-counfig]
   (locking lock
     (reset-repo (util/role-lookup-in-policies default-counfig) lock)))
  ([default-data lock]
   (adapter/init @backend default-data true)))

(defn check-and-update-old-pols [lock]
  (let [curr-pols (fetch-multiple-policies [] lock)
        updated-pols (util/role-lookup-in-policies curr-pols)]
    (when-not (= curr-pols updated-pols)
      (edit-all-policies updated-pols lock))))

(defn init-repository
  ([default-data should-overwrite?]
   (let [updated-data (util/role-lookup-in-policies default-data)]
     (locking lock
       (init-repository updated-data should-overwrite? lock))))
  ([default-data should-overwrite? lock]
   (adapter/init @backend default-data should-overwrite?)
   (check-and-update-old-pols lock)))

(defn remove-elem [col elem]
  (let [new-val (vec (remove #{elem} col))]
    (if (empty? new-val)
      nil
      new-val)))

(defn remove-nils
  [m]
  (let [f (fn [[k v]] (when v [k v]))]
    (postwalk (fn [x]
                (let [new-map (when (map? x)
                                (into {} (map f x)))]
                  (if (map? x)
                    (when (not-empty new-map)
                      new-map)
                    x)))
              m)))

(defn update-optional
  [user-attributes optional required attribute]
  (cond
    (nil? (get-in user-attributes
                  [:attributes attribute])) (-> user-attributes
                                                (update :optional
                                                        remove-elem
                                                        attribute)
                                                (update :required
                                                        remove-elem
                                                        attribute))
    (or (some #{attribute} optional)
        (some #{attribute} required)) user-attributes
    :else (update user-attributes
                  :optional
                  (fnil conj [])
                  attribute)))

(defn update-policy-core [feature-id value role-name]
  (when-let [policy (fetch-policy feature-id lock)]
    (edit-policy feature-id
                 (update policy
                         :user-attributes
                         (fn [{attributes :attributes
                               optional   :optional
                               required   :required
                               :as        user-attributes}]
                           (cond
                             (and (not value)
                                  (nil? attributes)) (-> user-attributes
                                                         (update :role
                                                                 remove-elem
                                                                 role-name)
                                                         remove-nils)
                             (and value
                                  (nil? attributes)) (if user-attributes
                                                       (if (some #{role-name} (:role user-attributes))
                                                         user-attributes
                                                         (update user-attributes :role (fnil conj []) role-name))
                                                       {:role [role-name]})
                             (and (not value)
                                  attributes) (-> user-attributes
                                                  (update-in
                                                   [:attributes :role]
                                                   remove-elem
                                                   role-name)
                                                  (update-optional optional required :role)
                                                  remove-nils)
                             (and value
                                  attributes) (-> user-attributes
                                                  (update-in [:attributes :role]
                                                             (fn [role-values]
                                                               (if role-values
                                                                 (if (some #{role-name} role-values)
                                                                   role-values
                                                                   (conj role-values role-name))
                                                                 [role-name])))
                                                  (update-optional optional required :role)
                                                  remove-nils))))
                 lock)))

(defn update-grouped-policy [feature-id value role-name]
  (let [[value conf-key] (string/split value #"__")
        pol-base-id (str conf-key "-" (name feature-id))
        use-pol-id (keyword (str pol-base-id "-use"))
        read-pol-id (keyword (str pol-base-id "-read"))
        write-pol-id (keyword (str pol-base-id "-write"))
        write-rights? (= value "write")
        read-rights? (or write-rights?
                         (= value "read"))
        use-rights? (or read-rights?
                        (= value "use"))]
    (update-policy-core write-pol-id write-rights? role-name)
    (update-policy-core read-pol-id read-rights? role-name)
    (update-policy-core use-pol-id use-rights? role-name)))

(defn update-policy
  [feature-id role-name grouped-policy? value]
  (locking lock
    (if grouped-policy?
      (update-grouped-policy feature-id value role-name)
      (update-policy-core feature-id value role-name))))
