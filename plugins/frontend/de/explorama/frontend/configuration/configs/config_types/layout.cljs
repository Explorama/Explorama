(ns de.explorama.frontend.configuration.configs.config-types.layout
  (:require [re-frame.core :refer [reg-sub]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.configs.access :refer [get-config]]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.configuration.config :as config]
            [clojure.set :refer [union]]
            [taoensso.timbre :refer-macros [error]]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(def config-type :layouts)
(def defaults-config-type :default-layouts)

(defn default-layouts [db]
  (get-config db defaults-config-type))

(defn user-layouts [db]
  (get-config db config-type))

(defn- gather-names [coll]
  (->> (map (fn [[_ {:keys [name id default?]}]]
              {name {:id id
                     :default? default?}})
            coll)
       set
       (into {})))

(reg-sub
 ::existing-user-layout-names
 (fn [db]
   (union (gather-names (default-layouts db))
          (gather-names (user-layouts db)))))