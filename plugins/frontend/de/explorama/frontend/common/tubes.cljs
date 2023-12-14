(ns de.explorama.frontend.common.tubes
  (:require [de.explorama.frontend.backend-api :as backend-api]
            [de.explorama.frontend.common.config :as config-frontend]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.common.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error warn]]))

(re-frame/reg-event-fx ::init-tube backend-api/init-tube)
(re-frame/reg-event-fx ::close-tube backend-api/close-tube)
(re-frame/reg-fx :backend-tube backend-api/dispatch)
(re-frame/reg-fx :backend-tube-n backend-api/dispatch-n)
(def dispatch-to-server backend-api/dispatch)

(defn notify-vec [notification-desc]
  (fi/call-api :notify-event-vec notification-desc))

;; To provide notifications for server side
(re-frame/reg-event-fx
 ::trigger-notify
 (fn [_ [_ notification-desc]]
   {:dispatch (notify-vec notification-desc)}))

(defn on-disconnect []
  (let [server-ws-connection-lost-message @(re-frame/subscribe [::i18n/translate :server-ws-connection-lost-message])]
    (re-frame/dispatch (notify-vec
                        {:type :warn
                         :category {:network :ws}
                         :message server-ws-connection-lost-message}))))

(defn on-receive [event-v]
  (re-frame/dispatch event-v))

(re-frame/reg-event-fx
 ::retry-update-user-info
 (fn [_ [_ tries]]
   (if (<= tries config-frontend/explorama-max-user-info-updates-tries)
     (do
       (warn "update user info failed. Retry" {:tries tries})
       {:fx [[:dispatch-later {:ms config-frontend/explorama-retry-update-user-info-timeout
                               :dispatch [::update-user-info tries]}]]})
     (do
       (error "Failed to update user-info - close connection")
       {:dispatch [::close-tube]}))))

(re-frame/reg-event-fx
 ::update-user-info
 (fn [{db :db} [_ tries]]
   {:backend-tube [ws-api/user-info-update-route
                   {:failed-callback [::retry-update-user-info (inc (or tries 0))]}
                   (select-keys (fi/call-api :user-info-db-get db) [:username :role :token])]}))

(defn sync-event-vec [event-vec]
  (when config-shared/explorama-project-sync?
    (dispatch-to-server [:project-sync-event event-vec])))
