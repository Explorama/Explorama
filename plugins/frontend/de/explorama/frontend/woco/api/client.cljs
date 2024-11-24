(ns de.explorama.frontend.woco.api.client
  (:require [re-frame.core :as rf]
            [de.explorama.frontend.woco.path :as path]))

(defn- client-id
  [db _]
  (get-in db path/client-id))

(rf/reg-sub ::client-id client-id)