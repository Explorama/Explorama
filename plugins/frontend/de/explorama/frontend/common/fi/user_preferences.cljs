(ns de.explorama.frontend.common.fi.user-preferences
  (:require [re-frame.core :refer [reg-event-fx reg-sub]]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.fi.path :as path]
            [de.explorama.shared.common.fi.ws-api :as ws-api]))

(defn- access-key [pref-key]
  (str pref-key))

(defn get-preferences [db]
  (reduce (fn [acc [pref-key v]]
            (assoc acc
                   (access-key pref-key)
                   v))
          {}
          (get-in db path/user-preferences)))

(defn get-preference [db pref-key default-val]
  (get-in db
          (path/user-preference (access-key pref-key))
          default-val))

(reg-sub
 ::user-preferences
 (fn [db]
   (get-preferences db)))

(reg-sub
 ::user-preference
 (fn [db [_ pref-key default-val]]
   (get-preference db pref-key default-val)))

(reg-event-fx
 ws-api/save-user-preference-result
 (fn [{db :db} [_ pref-key pref-val]]
   (debug "User setting saved" {:pref-key pref-key
                                :pref-val pref-val})
   {:db (cond-> db
          pref-key
          (assoc-in (path/user-preference pref-key) pref-val))
    :fx (mapv (fn [event]
                [:dispatch (conj event pref-val)])
              (get-in db (path/user-preference-watcher pref-key)))}))


(reg-event-fx
 ws-api/save-user-preference
 (fn [{db :db} [_ effect pref-key pref-val]]
   (let [user-info (fi/call-api :user-info-db-get db)
         pref-key (access-key pref-key)]
     {effect [ws-api/save-user-preference
              {:client-callback [ws-api/save-user-preference-result]}
              user-info
              pref-key
              pref-val]})))

(reg-event-fx
 ws-api/user-preferences-result
 (fn [{db :db} [_ user-preferences]]
   (debug "Loaded user preferences" user-preferences)
   {:db (cond-> db
          (map? user-preferences)
          (assoc-in path/user-preferences user-preferences)
          :always (assoc-in path/user-preferences-loaded true))
    :fx (reduce (fn [acc [pref-key pref-val]]
                  (into acc
                        (mapv (fn [event]
                                [:dispatch (conj event pref-val)])
                              (get-in db (path/user-preference-watcher pref-key)))))
                []
                user-preferences)}))

(reg-event-fx
 ws-api/user-preferences
 (fn [{db :db} [_ effect]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {effect [ws-api/user-preferences
              {:client-callback [ws-api/user-preferences-result]}
              user-info]})))

(defn user-preferences-loaded [db]
  (get-in db path/user-preferences-loaded))

(reg-sub
 ::user-preferences-loaded
 user-preferences-loaded)

(reg-event-fx
 ::add-watcher
 (fn [{db :db} [_ pref-key event default-value not-immediate?]]
   (cond-> {:db (update-in db (path/user-preference-watcher pref-key) (fnil conj []) event)}
     (not not-immediate?)
     (assoc :dispatch (conj event (get-preference db pref-key default-value))))))

(reg-event-fx
 ::rm-watcher
 (fn [{db :db} [_ pref-key event]]
   {:db (update-in db (path/user-preference-watcher pref-key)
                   (fn [v]
                     (filterv #(not= event %) v)))}))
