(ns de.explorama.frontend.table.vis-state
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.table.config :as vconfig]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.shared.table.util :refer [is-table?]]
            [de.explorama.frontend.table.table.backend-interface :as table-backend-interface]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.frontend.table.util.queue :as queue-util]))

(re-frame/reg-event-fx
 ::restore-vis-desc
 (fn [{db :db} [_ frame-id {:keys [table-state di local-filter]}]]
   (let [table? (is-table? frame-id)]
     {:db (cond-> db
            table? (assoc-in (path/table-datasource frame-id) di))
      :fx (cond-> []
            di
            (conj [:dispatch [::queue-util/queue-wrapper frame-id
                              [::table-backend-interface/connect-to-datainstance
                               {:di di
                                :frame-target-id frame-id
                                :source-table-state table-state
                                :reset-state? true}]
                              local-filter]]))})))


(defn vis-desc [db frame-id]
  {:di (get-in db (path/table-datasource frame-id))
   :vertical vconfig/default-vertical-str
   :tool vconfig/tool-name-table
   :title (fi/call-api :full-frame-title-raw frame-id)
   :table-state (table-data/copy-params frame-id)
   :local-filter (fi/call-api :frame-filter-db-get db frame-id)})