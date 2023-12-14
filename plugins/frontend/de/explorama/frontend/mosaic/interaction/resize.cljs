(ns de.explorama.frontend.mosaic.interaction.resize
  (:require [de.explorama.frontend.mosaic.vis.config :as vis-config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.common.queue :as ddq]))

(defn- size-changed? [db frame-id new-width new-height]
  (let [{:keys [width height]} (get-in db (gp/top-level frame-id))]
    (or (not= width new-width)
        (not= height (vis-config/body-height new-height)))))

(re-frame/reg-event-fx
 ::resize-listener
 (fn [{db :db} [_ frame-id {:keys [ignore-header? force-use-resize-infos?]
                            resize-width :width
                            resize-height :height}]]
   (let [{frame-size :size} (fi/call-api :frame-db-get db frame-id)
         [width height] (if force-use-resize-infos?
                          [resize-width resize-height]
                          frame-size)]
     {:dispatch (if (size-changed? db frame-id width height)
                  [::ddq/queue frame-id
                   [:de.explorama.frontend.mosaic.render.actions/resize
                    (gp/top-level frame-id)
                    width
                    (if ignore-header?
                      height
                      (vis-config/body-height height))]]
                  [::ddq/execute-callback-vec frame-id])})))

(re-frame/reg-event-fx
 ::trigger-resize
 (fn [{db :db}
      [_ frame-id]]
   (let [{[width height] :size} (fi/call-api :frame-db-get db frame-id)]
     {:dispatch-n [(when (size-changed? db frame-id width height)
                     [::ddq/queue frame-id
                      [:de.explorama.frontend.mosaic.render.actions/resize
                       (gp/top-level frame-id)
                       width
                       (vis-config/body-height height)]])]})))
