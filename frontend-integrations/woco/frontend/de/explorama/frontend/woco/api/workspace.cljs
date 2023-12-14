(ns de.explorama.frontend.woco.api.workspace
  (:require [de.explorama.frontend.woco.path :as path]
            [re-frame.core :as re-frame]))

(def id (path/workspace-id))

(re-frame/reg-event-db
 ::id
 (fn [db [_ id]]
   (assoc-in db (path/workspace-id) id)))

(re-frame/reg-sub
 ::id
 (fn [db _]
   (get-in db (path/workspace-id))))
