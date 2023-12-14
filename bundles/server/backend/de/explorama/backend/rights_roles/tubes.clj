(ns de.explorama.backend.rights-roles.tubes
  (:require [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.backend.concurrent.core :as concurrent]
            [de.explorama.backend.config :as sync-config]
            [de.explorama.backend.environment.probe.core :as probe]
            [pneumatic-tubes.core :as tubes :refer [receiver]]
            [pneumatic-tubes.httpkit :refer [websocket-handler]]
            [de.explorama.backend.rights-roles.auth-attrs-api :as auth-attrs-management]
            [de.explorama.shared.rights-roles.config :as config]
            [de.explorama.backend.rights-roles.shared.ws-api :as ws-api]
            [taoensso.timbre :refer [debug error]]))

(defonce thread-pool (concurrent/create-thread-pool sync-config/thread-pool))

(defmacro go [& body]
  `(concurrent/submit thread-pool (fn []
                                    (try
                                      ~@body
                                      (catch Exception e#
                                        (probe/rate-exception e#)
                                        (error e#)
                                        nil)))))

(def tx (tubes/transmitter))
(def dispatch-to (partial tubes/dispatch tx))

(defn broadcast [ev]
  (try
    (when ev
      (tubes/dispatch tx
                      (fn [{:keys [type]}]
                        (= type :data)) ;dont dispatch to other tubes like config
                      ev))
    (catch Exception e
      (probe/rate-exception e)
      (debug e "WS Broadcast failed. If no ws connections are available, you can ignore this"))))

(defn broadcast-notify-function [text category desc]
  (broadcast [:woco.api.notifications/notify (assoc desc
                                                    :vertical (name config/vertical-key)
                                                    :category category
                                                    :message text)]))

(defn notification-function [tube text category desc]
  (dispatch-to tube [:woco.api.notifications/notify (assoc desc
                                                           :vertical (name config/vertical-key)
                                                           :category category
                                                           :message text)]))

(defn- prepare-callback-vec [callback]
  (cond
    (nil? callback) nil
    (vector? callback)
    callback
    (keyword? callback)
    [callback]))

(defn- route-wrapper [route-fn tube [_ metas & params]]
  (go
    (let [{:keys [client-callback failed-callback broadcast-callback]} (when (map? metas) metas)
          ignore-first? (and (map? metas)
                             (or client-callback broadcast-callback))
          client-callback (prepare-callback-vec client-callback)
          broadcast-callback (prepare-callback-vec broadcast-callback)
          params (if ignore-first?
                   params
                   (into [metas] params))]
      (route-fn {:broadcast-callback (fn [& params]
                                       (when broadcast-callback
                                         (broadcast (apply conj broadcast-callback params))))
                 :failed-callback (fn [& params]
                                    (when failed-callback
                                      (dispatch-to tube (apply conj failed-callback params))))
                 :broadcast-notify-fn broadcast-notify-function
                 :notify-fn (partial notification-function tube)
                 :client-callback (fn [& params]
                                    (when client-callback
                                      (dispatch-to tube (apply conj client-callback params))))}
                params)))
  tube)

(defn all-users-roles [{:keys [client-callback]} _]
  (let [users (auth-attrs-management/list-users)
        roles (auth-attrs-management/list-roles)]
    (client-callback {:roles roles
                      :users users})))

(defn validate-token [{:keys [client-callback failed-callback]} [old-token]]
  (let [{token-valid? :valid?
         msg :reason} (jwt/token-valid? old-token)
        {:keys [username role] :as old-payload} (jwt/token-payload old-token)
        user-info (auth-attrs-management/username->user-info username role)
        new-token (jwt/user-token (dissoc old-payload :exp))]
    (if token-valid?
      (client-callback (assoc user-info :token new-token))
      (failed-callback msg))))

(defn token-payload [{:keys [client-callback]} [token]]
  (let [{token-valid? :valid?} (jwt/token-valid? token)
        {:keys [username role]} (jwt/token-payload token)
        user-info (auth-attrs-management/username->user-info username role)]
    (when token-valid?
      (client-callback (assoc user-info :token token)))))

(defn blacklist-role [{:keys [client-callback failed-callback]} [user-info rolename]]
  (if (seq (jwt/admin-token (dissoc user-info :token))) ;check if we are a admin
    (do (auth-attrs-management/blacklist-role rolename)
        (let [current-list (auth-attrs-management/blacklist-roles)
              not-in-list? (current-list rolename)]
          (if not-in-list?
            (client-callback rolename)
            (failed-callback :not-on-blacklist))))
    (failed-callback :no-admin)))

(def websocket-routes
  (receiver
   {:tube/on-create (fn [tube _]
                      (assoc tube :type :service))
    :tube/on-destroy
    (fn [tube _]
      tube)
    ws-api/all-user-roles (partial route-wrapper all-users-roles)
    ws-api/validate-token (partial route-wrapper validate-token)
    ws-api/token-payload (partial route-wrapper token-payload)
    ws-api/blacklist-role (partial route-wrapper blacklist-role)}))

(defn tube-handler []
  (websocket-handler websocket-routes))