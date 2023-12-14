(ns de.explorama.frontend.mosaic.vis.state
  (:require [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.vis.config :as vis-config]
            [de.explorama.frontend.mosaic.mosaic :as gg]
            [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 ::restore-vis-state
 (fn [{db :db} [_ frame-id {:keys [di local-filter operation-desc selected-layout size]}]]
   {:db (gg/initialize db frame-id size)
    :fx [[:dispatch [::tasks/execute-wrapper
                     (gp/top-level frame-id)
                     :init
                     {:di di
                      :local-filter local-filter
                      :operation-desc operation-desc}]]
         [:dispatch [::tasks/execute-wrapper
                     (gp/top-level frame-id)
                     :layout
                     {:layouts selected-layout}]]]}))

(defn get-state [db frame-id]
  (let [layouts (get-in db (gp/selected-layouts frame-id))]
    {:vertical config/default-vertical-str
     :tool vis-config/tool-name
     :local-filter (fi/call-api :frame-filter-db-get db frame-id)
     :di (get-in db (gp/data-instance frame-id))
     :title (fi/call-api :full-frame-title-raw frame-id)
     :operation-desc (get-in db (gp/operation-desc frame-id))
     :selected-layout layouts})) ;Maybe better slicing here
