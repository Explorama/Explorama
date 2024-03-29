(ns de.explorama.backend.frontend-api
  (:require [taoensso.timbre :refer [error debug warn]]))

(defonce ^:private api-routes (atom {}))
(def failed-send-timeout 1000)

(defn dispatch [event]
  (if-let [send-to-frontend (-> (aget js/self "backendAPI")
                                :listener)]
    (send-to-frontend event)
    (js/setTimeout (fn []
                     (dispatch event))
                   failed-send-timeout)))

(defn create! [& _]
  (debug "Created"))

(defn stop! [[& _]]
  (debug "Stopped"))

(defn register-routes [routes]
  (swap! api-routes merge routes))

(defn- prepare-callback-vec [callback]
  (cond
    (nil? callback) nil
    (vector? callback)
    callback
    (keyword? callback)
    [callback]))

(defn broadcast [event]
  (dispatch event))

(defn- route-wrapper [route-fn [_ metas & params]]
  (let [{:keys [client-callback failed-callback async-callback user-info client-id broadcast-callback broadcast-filter custom]}
        (when (map? metas) metas)
        ignore-first? (and (map? metas)
                           (or client-callback broadcast-callback user-info client-id))
        client-callback (prepare-callback-vec client-callback)
        broadcast-callback (prepare-callback-vec broadcast-callback)
        params (if ignore-first?
                 params
                 (into [metas] params))]
    (route-fn (cond-> {:user-info user-info
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
                       :async-callback (fn [& params]
                                         (when async-callback
                                           (dispatch (apply conj async-callback params))))
                       :user-validation (constantly true)}
                (map? custom)
                (into (map (fn [[k event]]
                             [k
                              (fn [& params]
                                (dispatch (apply conj (prepare-callback-vec event) params)))])
                           custom)))
              params)))

(defn request-listener [e]
  (let [[event :as request] e
        route-fn (get @api-routes event)]
    (cond (fn? route-fn)
          (route-wrapper route-fn request)
          :else
          (error "[backend] No route found for " event))))


(defn init []
  (debug "[backend] Init frontend API")
  (aset js/self "frontendAPI"
        {:send dispatch
         :listener request-listener}))