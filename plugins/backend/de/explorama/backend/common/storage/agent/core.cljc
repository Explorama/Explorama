(ns de.explorama.backend.common.storage.agent.core
  (:require [de.explorama.backend.common.storage.agent.api :as ai]
            [de.explorama.backend.common.storage.agent.expdb :as expdb]))

(defonce ^:private states (atom {}))

(defmulti create-instance (fn [params]
                            (:impl params)))

(defmethod create-instance :expdb [params]
  (expdb/new-instance params))

(defn create [{path :path :as params}]
  (let [instance (create-instance params)
        ag (atom nil)]
    (add-watch ag nil (fn [key derefable old-state new-state]
                        (throw (ex-info "Can not access not initialized agent" {:cause :not-ready}))))
    (swap! states assoc path {:instance instance
                              :ag ag})
    ag))

(defn start! [path]
  (let [{:keys [instance ag]} (get @states path)]
    (ai/start instance ag)))
