(ns de.explorama.frontend.backend-api
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.common.configs.platform-specific :as config-shared-platform]
            [de.explorama.frontend.woco.config :as wconfig]
            [pneumatic-tubes.core :as tubes]
            [re-frame.core :as re-frame]))

(defn on-connect []
  (js/console.log "connected"))

(defn- on-connect-failed []
  (js/console.log "failed"))

(defn- on-disconnect []
  (let [server-ws-connection-lost-message @(re-frame/subscribe [::i18n/translate :server-ws-connection-lost-message])]
    (re-frame/dispatch [:woco.api.notifications/notify {:type :warn
                                                        :vertical (name wconfig/default-namespace)
                                                        :category {:network :ws}
                                                        :message server-ws-connection-lost-message}])))

(defn- on-receive [event-v]
  (re-frame/dispatch event-v))

(def ^:private websocket-url
  (str (clj-str/replace-first (or config-shared-platform/explorama-origin "")
                              #"^http" "ws")
       "/ws"))

(def ^:private tube-spec (tubes/tube
                          websocket-url
                          on-receive
                          on-connect
                          on-disconnect
                          on-connect-failed))

(def ^:private tube-instance (atom nil))

(defn- dispatch-to-server [event-vector]
  (tubes/dispatch tube-spec event-vector))

(def ^:private send-to-server (re-frame/after (fn [_ v] (dispatch-to-server v))))

(defn dispatch [event]
  (send-to-server event)
  {})

(defn dispatch-n [events]
  (doseq [event events :when event]
    (dispatch event))
  {})

(defn init-tube [{db :db} [_ user-info after-fxs]]
  (when (nil? @tube-instance)
    (let [client-id (fi/call-api :client-id-db-get db)]
      (reset! tube-instance
              (tubes/create! tube-spec (-> user-info
                                           (select-keys [:role :username :token])
                                           (assoc :client-id client-id)))))
    (when after-fxs
      {:fx after-fxs})))

(defn close-tube [_ [_ tube-events]]
  (doseq [event tube-events
          :when event]
    (tubes/dispatch tube-spec event))
  (tubes/destroy! tube-spec)
  (reset! tube-instance nil)
  {})
