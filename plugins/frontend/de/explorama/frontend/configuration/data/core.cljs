(ns de.explorama.frontend.configuration.data.core
  "Acces to graph-acs, which are a reduced form of Graph-ACs (calculated on import)"
  (:require [re-frame.core :refer [reg-sub reg-event-db reg-event-fx]]
            [clojure.set :refer [difference union]]
            [clojure.string :refer [lower-case]]
            [de.explorama.frontend.configuration.configs.config-types.geographic :as geo-config]
            [de.explorama.shared.configuration.data-management.geographic-attributes-config :as ga-config]
            [de.explorama.frontend.configuration.path :as cp]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(def default-bucket :default)

(reg-sub
 ::share-users
 (fn [db]
   (let [current-user (fi/call-api :user-info-db-get db)
         all-users (filter #(not= (get % :value)
                                  (get current-user :username))
                           (fi/call-api :users-db-get db))]
     all-users)))

(defn- is-frame-context? [frame-data-acs-path context]
  (and (vector? frame-data-acs-path)
       (= context :frame)))

(reg-sub
 ::attr-types
 (fn [db [_ frame-data-acs-path context]]
   (cond
     (is-frame-context? frame-data-acs-path context)
     (reduce (fn [acc [attr-label {std :std}]]
               (cond-> acc
                 attr-label
                 (assoc attr-label {:std (select-keys std [:type :node-type])})))
             {}
             (get-in db (conj frame-data-acs-path)))
     (= context :global)
     (get-in db cp/attr-types))))

(reg-sub
 ::acs
 (fn [db [_ frame-data-acs-path attributes filter-vals]]
   (when attributes
     (let [attributes (set attributes)
           global-attributes (-> (get-in db cp/characteristics-root)
                                 (select-keys attributes)
                                 vals
                                 (->> (apply union))
                                 set
                                 (difference (or filter-vals #{})))
           frame-attributes (when (vector? frame-data-acs-path)
                              (-> (get-in db frame-data-acs-path)
                                  (select-keys attributes)
                                  vals
                                  (->> (map #(get-in % [:std :vals]))
                                       (apply union))
                                  set
                                  (difference (or filter-vals #{}))))
           sort-fn (if (number? (first global-attributes))
                     sort
                     #(sort-by (fn [v]
                                 (lower-case (str v)))
                               %))]
       (cond-> {}
         :always
         (assoc :global (->> global-attributes
                             sort-fn
                             vec))
         (vector? frame-data-acs-path)
         (assoc :frame (->> frame-attributes
                            sort-fn
                            vec)))))))

(reg-sub
 ::context-attributes
 (fn [db]
   (->> (get-in db cp/attr-types)
        (filter (fn [[_ v]] (= "Context" (get-in v [:std :node-type])))))))


(reg-sub
 ::geographic-attributes
 (fn [db]
   (or (get-in db
               (conj (cp/config-type geo-config/config-type)
                     ga-config/geographic-attributes-id
                     ga-config/geographic-attributes-key))
       [])))

(reg-sub
 ::topics
 (fn [db]
   (get-in db
           (cp/config-type :topics))))

(reg-sub
 ::datasources
 (fn [db]
   (reduce (fn [acc [k vals]] (assoc acc k (map (fn [v] [k v]) vals)))
           {} (get-in db
                      cp/available-datasources))))

(defn- gen-db-identifier [frame-id]
  (or frame-id :global))

(defn- gather-all-used-attrs [db]
  (reduce (fn [acc [_ {:keys [attr-usage]}]]
            (apply conj acc attr-usage))
          #{}
          (get-in db cp/characteristics-usage-root {})))

(reg-event-fx
 ::clear-characteristics
 (fn [{db :db} [_ frame-id all?]]
   {:db  (if all?
           (-> db
               (assoc-in cp/characteristics-root {})
               (assoc-in cp/characteristics-usage-root {}))
           (let [db (update-in db cp/characteristics-usage-root dissoc (gen-db-identifier frame-id))]
             (update-in db
                        cp/characteristics-root
                        select-keys
                        (gather-all-used-attrs db))))}))

(reg-event-fx
 ::request-characteristics
 (fn [{db :db} [_ frame-id new-attributes]]
   (let [existing-characs (set (keys (get-in db cp/characteristics-root)))
         request-attrs (difference (set new-attributes)
                                   existing-characs)]

     (when (seq request-attrs)
       {:db (assoc-in db
                      (cp/characteristics-usage (gen-db-identifier frame-id))
                      {:requesting? true
                       :attr-usage (set new-attributes)})
        :backend-tube [ws-api/get-attr-characteristics
                       {:client-callback [ws-api/set-attr-characteristics frame-id]}
                       request-attrs]}))))

(reg-event-fx
 ::request-available-datasources
 (fn [_ [_ bucket]]
   {:backend-tube [ws-api/get-available-datasources
                   {:client-callback [ws-api/set-available-datasources bucket]}
                   bucket]}))

(reg-event-db
 ws-api/set-available-datasources
 (fn [db [_ bucket available-datasources]]
   (assoc-in db (conj cp/available-datasources bucket) available-datasources)))

(reg-event-db
 ws-api/set-attr-characteristics
 (fn [db [_ frame-id attrs-characts]]
   (let [db-identifier (gen-db-identifier frame-id)
         usage (get-in db (cp/characteristics-usage db-identifier))]
     (cond-> db
       (and usage (map? attrs-characts))
       (->
        (assoc-in (cp/characteristics-requesting? db-identifier) false)
        (update-in cp/characteristics-root merge attrs-characts))))))

(reg-event-db
 ws-api/set-acs
 (fn [db [_ {:keys [types]}]]
   (assoc-in db cp/attr-types types)))

(reg-event-db
 ws-api/set-search-config
 (fn [db [_ [config-type value]]]
   (-> db
       (assoc-in (cp/config-type config-type) value))))

(reg-sub
 ::layout-error-status
 (fn [db [_ id]]
   (get-in db (cp/layout-error-status id))))

(reg-event-db
 ::set-layout-error-status
 (fn [db [_ id status]]
   (assoc-in db (cp/layout-error-status id) status)))

(reg-sub
 ::mouse-layout-error-status
 (fn [db]
   (get-in db cp/mouse-layout-error-status)))

(reg-event-db
 ::set-mouse-layout-error-status
 (fn [db [_ status]]
   (assoc-in db cp/mouse-layout-error-status status)))