(ns de.explorama.frontend.mosaic.operations.couple
  (:require [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.pixi.db :as render-db]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-event-fx
 ::couple
 (fn [_ [_ source-frame-id target-frame-id couple-infos]]
   (if (and (gp/is-mosaic-frame? source-frame-id)
            (gp/is-mosaic-frame? target-frame-id))
     (let [path (gp/top-level target-frame-id)
           pwith (->> (remove #{target-frame-id} (:with couple-infos))
                      (mapv #(gp/top-level %)))]
       {:dispatch-n [[::tasks/execute-wrapper
                      path
                      :couple
                      (assoc couple-infos
                             :with pwith)]]})
     (debug "Execute Couple ignored due to non-mosaic-frames" source-frame-id target-frame-id))))

(re-frame/reg-event-fx
 ::action
 (fn [_ [_ _source-frame-id target-frame-id action-desc]]
   (let [{:keys [sort-by order-timeline]} action-desc
         path (gp/frame-path target-frame-id)
         action-event (cond
                        sort-by [::tasks/execute-wrapper
                                 path
                                 :sort-by
                                 {:by sort-by
                                  :direction :desc}]
                        order-timeline [:de.explorama.frontend.mosaic.views.frame/event-wrapper
                                        [::ddq/queue
                                         (gp/frame-id path)
                                         [:de.explorama.frontend.mosaic.render.actions/update
                                          path
                                          :adjust-one-row?]]])]
     {:dispatch-n [action-event]})))

(re-frame/reg-event-fx
 ::decouple
 (fn [_
      [_ _trigger-frame-id removed-frame-id couple-infos]]
   (when (gp/is-mosaic-frame? removed-frame-id)
     (render-db/decouple-instance removed-frame-id)
     {:dispatch [::tasks/execute-wrapper
                 (gp/top-level removed-frame-id)
                 :decouple
                 couple-infos]})))