(ns de.explorama.backend.woco.app.server
  (:require [clojure.math.numeric-tower :as math]
            [de.explorama.backend.handler :as handler]
            [de.explorama.backend.woco.server-config :as config-server]
            [de.explorama.shared.woco.config]
            [org.httpkit.server :as http]
            [reitit.ring :as ring])
  (:gen-class))

(defn- add-shutdown-hook! [f]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. f)))

(defonce ^:private server-instance (atom nil))

(defn- MiB [n]
  (int (* n (math/expt 2 20))))

(def ^:private http-max-body (MiB config-server/explorama-http-max-body))

(defn stop-server! []
  (when-not (nil? @server-instance)
    (http/server-stop! @server-instance)
    (reset! server-instance nil)))

(defn start-server! [ip port handler]
  (add-shutdown-hook! stop-server!)
  (swap! server-instance
         (fn [instance]
           (or instance
               (http/run-server handler
                                {:ip ip
                                 :port port
                                 :join? false
                                 :max-body http-max-body
                                 :legacy-return-value? false})))))

(defn -main [& _args]
  (start-server! config-server/explorama-host
                 config-server/explorama-port
                 (ring/ring-handler
                  (ring/router
                   handler/routes
                   handler/routes-opts))))
