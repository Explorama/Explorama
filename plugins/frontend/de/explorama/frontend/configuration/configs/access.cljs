(ns de.explorama.frontend.configuration.configs.access
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.configuration.config :as config]
            [taoensso.timbre :refer-macros [error]]))

(defn get-config
  ([db config-type config-id]
   (if config-id
     (get-in db (path/config-entry config-type config-id))
     (get-config db config-type)))
  ([db config-type]
   (get-in db (path/config-type config-type))))

(re-frame/reg-sub
 ::get-config
 (fn [db [_ config-type config-id]]
   (get-config db config-type config-id)))

;; config-key = entity-type

;; user-info entity-type=config-type config-id