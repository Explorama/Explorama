(ns de.explorama.backend.common.storage.redis-backend-helper
  (:require [taoensso.timbre :refer [error]]
            [de.explorama.backend.redis.utils :as ru]
            [taoensso.carmine :as car]))

(defn update* [server-conn redis-key-path data]
  (let [result (ru/wcar* server-conn
                         "update config"
                         (car/set redis-key-path data))]
    (when (= result "OK")
      data)))

(defn save-value-to-redis [server-conn redis-key-path new-val]
  (when-not (update* server-conn redis-key-path new-val)
    (error "Could not persist" redis-key-path)))

(defn load-redis-to-atom [server-conn redis-key-path atom-store default]
  (let [content (ru/wcar* server-conn
                          (str "fetch " redis-key-path)
                          (car/get redis-key-path))]
    (if content
      (reset! atom-store content)
      (if (update* server-conn redis-key-path default)
        (reset! atom-store default)
        (error "Could not persist default" default)))))

(defn add-save-watcher [server-conn redis-key-path atom-store watcher-key]
  (add-watch atom-store
             watcher-key
             (fn [_ _ _ new-val]
               (save-value-to-redis server-conn redis-key-path new-val))))
