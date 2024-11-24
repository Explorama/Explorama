(ns de.explorama.frontend.woco.frame.view.overlay.stop
  (:require [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]))

(defn stop-screen [id api]
  (when-let [stop-screen-desc @api]
    (let [{:keys [title-sub message-1-sub message-2-sub stop-sub ok-fn show?]}
          stop-screen-desc
          show? @(show? id)]
      (when show?
        [dialog
         {:show? true
          :title @(title-sub show? id)
          :hide-fn #(do)
          :message [:<>
                    [:p @(message-1-sub show? id)]
                    [:p @(message-2-sub show? id)]]
          :ok {:on-click #(ok-fn show? id %)
               :label @(stop-sub show? id)}}]))))