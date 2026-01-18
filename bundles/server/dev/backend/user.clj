(ns user
  (:require [de.explorama.backend.handler :as handler]
            [de.explorama.backend.woco.app.server :as server]
            [de.explorama.backend.woco.server-config :as server-config]
            [taoensso.timbre :refer [info]]))

(defn start-dev []
  (info "Stopping old dev server")
  (server/stop-server!)
  (info "Starting dev server")
  (server/start-server! server-config/explorama-host
                        server-config/explorama-port
                        handler/handler)
  (info "Starting dev server done!"))

(start-dev)

(in-ns 'adventurer.core)
