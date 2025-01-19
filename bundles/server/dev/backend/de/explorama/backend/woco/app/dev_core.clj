(ns de.explorama.backend.woco.app.dev-core
  (:require [de.explorama.backend.handler :as handler]
            [de.explorama.backend.woco.app.server :as server]
            [de.explorama.backend.woco.server-config :as server-config]
            [figwheel-sidecar.repl-api :as fig]
            [taoensso.timbre :refer [info]]))

(defn start-dev []
  (info "Stopping old dev server")
  (server/stop-server!)
  (fig/stop-figwheel!)
  (info "Starting dev server")
  (fig/start-figwheel!)
  (server/start-server! server-config/explorama-host
                        server-config/explorama-port
                        handler/handler)
  (info "Starting dev server done!"))

(defn cljs-repl []
  (fig/cljs-repl))
