(ns de.explorama.backend.woco.app.dev-core
  (:require [de.explorama.backend.handler :as handler]
            [de.explorama.backend.woco.app.server :as server]
            [de.explorama.backend.woco.server-config :as server-config]
            [figwheel-sidecar.repl-api :as fig]
            [reitit.ring :as ring]
            [taoensso.timbre :refer [info]]))

(defn- extend-routes [routes]
  routes)

(defn- extend-routes-opts [routes-opts]
  routes-opts)

(def ^:private handler-dev (ring/ring-handler
                            (ring/router
                             (extend-routes handler/routes)
                             (extend-routes-opts handler/routes-opts))))

(defn start-dev []
  (info "Stopping old dev server")
  (server/stop-server!)
  (fig/stop-figwheel!)
  (info "Starting dev server")
  (fig/start-figwheel!)
  (server/start-server! server-config/explorama-host
                        server-config/explorama-port
                        handler-dev)
  (info "Starting dev server done!"))

(defn cljs-repl []
  (fig/cljs-repl))
