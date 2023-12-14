(ns de.explorama.backend.frontend-api
  (:require [data-format-lib.core :as dfl.core]
            [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.backend.common.environment.probe :as probe]
            [de.explorama.backend.concurrent :as concurrent]
            [de.explorama.backend.woco.server-config :as config-server]
            [pneumatic-tubes.core :as tubes :refer [receiver]]
            [taoensso.timbre :refer [debug error warn]]))

(defonce ^:private api-routes (atom {}))

(def tx (tubes/transmitter))
(def dispatch (partial tubes/dispatch tx))

(defonce thread-pool (concurrent/create-thread-pool config-server/explorama-thread-pool))

(defn broadcast [ev & [tube-filter]]
  (try
    (when ev
      (tubes/dispatch tx
                      (partial dfl.core/check-datapoint tube-filter)
                      ev)
      true)
    (catch Exception e
      (probe/rate-exception e)
      (debug e "WS Broadcast failed. If no ws connections are available, you can ignore this")
      false)))

(defn- dispatch-client
  "Use this in long running requests to make sure the answere gets dispatched to a tube.
   Otherwise its possible that the old tube is already closed."
  [{client-id :client-id :as tubes} event]
  (if client-id
    (if-let [tube (first (tubes/find-tubes (fn [{tube-client-id :client-id}]
                                             (= tube-client-id client-id))))]
      (dispatch tube event)
      (error "No tube found for client-id" {:client-id client-id
                                            :all-tubes (tubes/find-tubes :all)
                                            :event event}))
    (do
      (warn "Coulnd't find any tube becuse the given client-id is nil. Fallback to current tubes connection.")
      (dispatch tubes event))))

(defn broadcast-notify-function [vertical-key text category desc]
  (broadcast [:woco.api.notifications/notify (assoc desc
                                                    :vertical vertical-key
                                                    :category category
                                                    :message text)]))

(defn notification-function [vertical-key tube text category desc]
  (dispatch-client tube
                   [:woco.api.notifications/notify (assoc desc
                                                          :vertical vertical-key
                                                          :category category
                                                          :message text)]))

(defmacro go [& body]
  `(concurrent/submit thread-pool (fn []
                                    (try
                                      ~@body
                                      (catch Exception e#
                                        (probe/rate-exception e#)
                                        (error e#)
                                        nil)))))

(defn- prepare-callback-vec [callback]
  (cond
    (nil? callback) nil
    (vector? callback)
    callback
    (keyword? callback)
    [callback]))

(defn- user-valid? [tube user-info]
  (and (jwt/user-valid? user-info)
       (jwt/user-valid? tube)
       (= (select-keys tube [:username :role :token])
          (select-keys user-info [:username :role :token]))))

(defn update-user-info [tube user-info failed-callback]
  (if (jwt/user-valid? user-info)
    (merge tube user-info)
    (do
      (dispatch-client tube failed-callback)
      tube)))

(defn create! [& _]
  (debug "Created"))

(defn stop! [[& _]]
  (debug "Stopped"))

(defn register-routes [routes]
  (swap! api-routes merge routes))

(defn routes->tubes []
  (receiver @api-routes))

(defn- route-wrapper [route-fn tube [_ metas & params]]
  (let [{:keys [client-callback failed-callback user-info client-id broadcast-callback broadcast-filter custom]}
        (when (map? metas) metas)
        ignore-first? (and (map? metas)
                           (or client-callback broadcast-callback user-info client-id))
        client-callback (prepare-callback-vec client-callback)
        broadcast-callback (prepare-callback-vec broadcast-callback)
        params (if ignore-first?
                 params
                 (into [metas] params))]
    (route-fn (cond-> {:tube tube
                       :user-info user-info
                       :client-id client-id
                       :broadcast-callback (fn [& params]
                                             (when broadcast-callback
                                               (warn (str "Not yet implemented" :broadcast-callback))))
                       :failed-callback (fn [& params]
                                          (when failed-callback
                                            (dispatch (apply conj failed-callback params))))
                       :broadcast-notify-fn (fn [& params]
                                              (warn (str "Not yet implemented" :broadcast-notify-fn)))
                       :notify-fn (fn [& params]
                                    (warn (str "Not yet implemented" :notify-fn)))
                       :client-callback (fn [& params]
                                          (when client-callback
                                            (dispatch (apply conj client-callback params))))
                       :user-validation (constantly true)}
                (map? custom)
                (into (map (fn [[k event]]
                             [k
                              (fn [& params]
                                (dispatch (apply conj (prepare-callback-vec event) params)))])
                           custom)))
              params)))

(defn init []
  (routes->tubes))
