(ns de.explorama.frontend.woco.api.flags
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.config :as config]
            [taoensso.timbre :refer-macros [debug error]]))

(def woco-provider-flags
  {:fi-name "woco"
   :activate-logging? true
   :data-interaction? true
   :legend-default-open? true
   :force-read-only? false})

(defn- flags-impl [db {:keys [provider-origin provider-details] :as frame-id}]
  (if (or (= provider-origin config/default-namespace)
          (not provider-origin))
    woco-provider-flags
    (let [provider-flags-fn (registry/lookup-target db :provider provider-origin)]
      (if (fn? provider-flags-fn)
        (provider-flags-fn db provider-details frame-id)
        (error "No provider flags found for" frame-id)))))

(defn flags
  "flag-keys can be one single key or multiple keys
   Return: Map of values (multiple-keys), single value if one key"
  [db frame-id flag-keys]
  (if (vector? flag-keys)
    (select-keys (flags-impl db frame-id) flag-keys)
    (get (flags-impl db frame-id) flag-keys)))

(re-frame/reg-sub
 ::flags
 (fn [db [_ frame-id flag-keys]]
   (flags db frame-id flag-keys)))