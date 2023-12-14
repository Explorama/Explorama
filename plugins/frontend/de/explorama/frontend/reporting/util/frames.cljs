(ns de.explorama.frontend.reporting.util.frames
  (:require [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :refer [reg-sub]]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]))

(defn gen-frame-id [vertical provider-details]
  {:vertical vertical
   :frame-id (str (random-uuid))
   :provider-origin config/default-namespace
   :provider-details provider-details
   :temporary? true})

(defn id-type [db id]
  (cond
    (or (get-in db (dr-path/dashboard id))
        (get-in db (dr-path/shared-dashboard id)))
    :dashboard
    (or (get-in db (dr-path/report id))
        (get-in db (dr-path/shared-report id)))
    :report))

(reg-sub
 ::id-type
 (fn [db [_ id]]
   (id-type db id)))

(defn datasources-info-node [id frame-datasources]
  [:<>
   "Datasources"
   (map (fn [datasource]
          ^{:key (str "dr_data_info" id datasource)}
          [:span datasource])
        (sort frame-datasources))])

(defn handle-param [func & params]
  (cond
    (fn? func)
    (apply func params)
    (not (nil? func))
    (val-or-deref func)))