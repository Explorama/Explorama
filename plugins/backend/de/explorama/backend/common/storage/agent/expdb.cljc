(ns de.explorama.backend.common.storage.agent.expdb
  (:require [de.explorama.backend.common.storage.agent.api :as ai]
            [de.explorama.backend.expdb.middleware.db :as db]))

(deftype ExpDBStorage [^:volatile-mutable initialized?
                       params]
  ai/Storage!
  (ai/start [instance agent]
    (let [{:keys [path init bucket]} params
          old-value (db/get bucket path)]
      (remove-watch agent nil)
      (reset! agent (or old-value init))
      (add-watch agent nil (fn [_ _ _ new-state]
                             (db/set bucket path new-state)))
      (ai/ready! instance)))
  (ai/ready? [_] initialized?)
  (ai/ready! [_] (set! initialized? true)))

(defn new-instance [params]
  (ExpDBStorage. false params))
