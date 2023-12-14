(ns de.explorama.frontend.woco.frame.view.overlay.warn
  (:require [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]))

(defn warn-screen [id api]
  (when-let [warn-screen-desc @api]
    (let [{:keys [title-sub message-1-sub message-2-sub recommendation-sub
                  stop-sub proceed-sub stop-fn proceed-fn show?]}
          warn-screen-desc
          show? @(show? id)]
      (when show?
        [dialog
         {:show? true
          :title @(title-sub show? id)
          :hide-fn #(do)
          :message [:<>
                    [:p @(message-1-sub show? id)
                     [:br]
                     @(message-2-sub show? id)]
                    [:p @(recommendation-sub show? id)]]
          :ok {:on-click #(proceed-fn show? id %)
               :label @(proceed-sub show? id)}
          :cancel {:variant :secondary
                   :on-click #(stop-fn show? id %)
                   :label @(stop-sub show? id)}}]))))