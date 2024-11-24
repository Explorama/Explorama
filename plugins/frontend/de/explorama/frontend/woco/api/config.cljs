(ns de.explorama.frontend.woco.api.config
  (:require [clojure.string :as str]
            [de.explorama.frontend.woco.configs.paths :as cpath]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.woco.ws-api :as ws-api]
            [re-frame.core :as re-frame]))

(defn roles [db]
  (get-in db cpath/possible-roles []))

(re-frame/reg-sub
 ::roles
 (fn [db _]
   (roles db)))

(defn users [db]
  (get-in db cpath/possible-users []))

(re-frame/reg-sub
 ::users
 (fn [db _]
   (users db)))

(defn- normalize-username [username]
  (if (and config-shared/explorama-normalize-username username)
    (-> username
        str/trim
        str/lower-case)
    username))

(defn name-for-user [db username]
  (let [username (normalize-username username)]
    (->> (users db)
         (filter #(= username (get % :value)))
         first
         :label)))

(re-frame/reg-sub
 ::name-for-user
 (fn [db [_ username]]
   (name-for-user db username)))

(re-frame/reg-event-fx
 ::load-users-roles
 (fn [_ _]
   {:backend-tube-n [[ws-api/roles-and-users
                      {:client-callback [ws-api/roles-and-users-result]}]]}))