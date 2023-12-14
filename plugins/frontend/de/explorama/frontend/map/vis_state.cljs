(ns de.explorama.frontend.map.vis-state
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.map.acs]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.event-replay]
            [de.explorama.frontend.map.map.render-helper :refer [add-render-done-listener]]
            [de.explorama.frontend.map.operations.tasks :as tasks]
            [de.explorama.frontend.map.paths :as geop]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::restore-rest
 (fn [{db :db} [_ frame-id]]
   (add-render-done-listener db frame-id)
   {}))

(re-frame/reg-event-fx
 ::restore-vis-desc
 (fn [{db :db} [_ frame-id {:keys [task-desc]}]]
   
   {:db (ddq/set-event-callback db frame-id [::restore-rest frame-id])
    :fx [[:dispatch [::ddq/register-tracks frame-id
                     [:de.explorama.frontend.map.map.core/register-state-tracker frame-id]]]
         [:dispatch [::ddq/queue frame-id
                     [:de.explorama.frontend.map.map.core/initialize frame-id]]]
         [:dispatch [::tasks/execute-wrapper
                     frame-id
                     :copy-frame
                     task-desc]]]}))

(defn vis-desc [db frame-id]
  {:di (get-in db (geop/frame-di frame-id))
   :vertical config/default-vertical-str
   :tool config/tool-name
   :title (fi/call-api :full-frame-title-raw frame-id)
   :task-desc (tasks/build-base-payload db frame-id)})