(ns de.explorama.frontend.search.views.components.frame-notifications
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.path :as path]
            [de.explorama.frontend.search.config :as config]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.frames.core :as frames :refer [notification]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]))

(re-frame/reg-event-db
 ::too-many-options
 (fn [db [_ frame-id row-name]]
   (assoc-in db
             (path/frame-row-too-many-related frame-id)
             (first row-name))))

(re-frame/reg-sub
 ::too-many-options
 (fn [db [_ frame-id]]
   (get-in db (path/frame-row-too-many-related frame-id))))

(re-frame/reg-event-fx
 ::clear-notification
 (fn [{db :db} [_ frame-id]]
   {:db (update-in db
                   (path/frame-desc frame-id)
                   dissoc
                   path/frame-row-too-many-related-key)}))

(defn frame-notifications [frame-id]
  [error-boundary
   (let [too-many-options-row-name @(re-frame/subscribe [::too-many-options frame-id])
         message-base @(re-frame/subscribe [::i18n/translate :too-many-options-related])
         message (when too-many-options-row-name
                   (format message-base
                           too-many-options-row-name
                           config/max-related-options))]
     [notification (cond-> {:show? (boolean too-many-options-row-name)
                            :extra-props {:style {:white-space :pre
                                                  :z-index 1000}}
                            :on-close #(re-frame/dispatch [::clear-notification frame-id])}
                     message (assoc :message message))])])