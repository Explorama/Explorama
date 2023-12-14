(ns de.explorama.backend.frontend-api
  (:require [taoensso.timbre :refer [error debug warn trace]]
            [cljs.reader :as edn]))

(defonce ^:private api-routes (atom {}))

(defn dispatch [[event-name :as event]]
  (trace "(webworker) Send" event-name)
  ((aget js/window "sendToUi")
   (str event)))

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
  (let [{:keys [client-callback failed-callback user-info client-id broadcast-callback broadcast-filter custom]}
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
                       :user-validation (constantly true)}
                (map? custom)
                (into (map (fn [[k event]]
                             [k
                              (fn [& params]
                                (dispatch (apply conj (prepare-callback-vec event) params)))])
                           custom)))
              params)))

(defn- request-listener [e]
  (let [[event :as request] (edn/read-string e)
        route-fn (get @api-routes event)]
    (trace "(webworker) message received with action" event (meta route-fn))
    (cond (fn? route-fn)
          (route-wrapper route-fn request)
          :else
          (error "(webworker) No route found for " event))))


(defn init []
  ((aget js/window "setUiRequestHandler")
   request-listener))
  ;; (aset js/self "onmessage" request-listener))