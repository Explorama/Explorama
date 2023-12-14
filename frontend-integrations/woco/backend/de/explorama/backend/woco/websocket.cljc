(ns de.explorama.backend.woco.websocket
  (:require [de.explorama.backend.woco.user-preferences :as user-preferences]
            [de.explorama.shared.common.fi.ws-api :as fi-ws-api]
            [de.explorama.shared.woco.ws-api :as ws-api]
            [taoensso.timbre :refer [warn]]))

(defn- default-fn [& _]
  (warn "Not yet implemented"))

(def endpoints
 {ws-api/update-user-info default-fn
  ws-api/roles-and-users default-fn
  ws-api/request-datalink default-fn
  fi-ws-api/user-preferences user-preferences/get-user-preferences
  fi-ws-api/save-user-preference user-preferences/save-user-preference})
