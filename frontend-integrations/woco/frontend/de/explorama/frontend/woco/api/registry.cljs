(ns de.explorama.frontend.woco.api.registry
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]))

(defn lookup-category [db category]
  (get-in db (conj path/registry category)))

(defn lookup-target [db category ui-service]
  (get-in db (conj path/registry category ui-service)))

(defn register-ui-service [db category ui-service target]
  (assoc-in db (conj path/registry category ui-service) target))

(defn unregister-ui-service [db category ui-service]
  (let [ndb (update-in db (conj path/registry category) dissoc ui-service)]
    (if (empty? (get-in ndb (conj path/registry category)))
      (update-in ndb path/registry dissoc category)
      ndb)))

(re-frame/reg-event-db
 ::register-ui-service
 (fn [db [_ category ui-service target]]
   (register-ui-service db category ui-service target)))

(re-frame/reg-event-db
 ::unregister-ui-service
 (fn [db [_ category ui-service]]
   (unregister-ui-service db category ui-service)))

(re-frame/reg-sub
 ::lookup-target
 (fn [db [_ category ui-service]]
   (lookup-target db category ui-service)))

(re-frame/reg-sub
 ::lookup-category
 (fn [db [_ category]]
   (lookup-category db category)))
