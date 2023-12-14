(ns de.explorama.frontend.indicator.components.direct-visualization
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.indicator.path :as path]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error]]))

(re-frame/reg-event-fx
 ::open-visualization
 (fn [{db :db} [_ indicator-id project? vis-event]]
   (let [frame-id (get-in db path/open-frame-id)
         frame-infos (fi/call-api :frame-db-get db frame-id)
         {pixel-coords :coords
          {original-pixel-coords :coords} :before-minmaximized} frame-infos
         pos (or original-pixel-coords (vec pixel-coords))
         indicator-desc (when project?
                          (get-in db (path/project-indicator-desc indicator-id)))]
     {:fx [[:dispatch [:de.explorama.frontend.indicator.views.core/set-loading true]]
           [:backend-tube [ws-api/create-and-publish-di
                           {:client-callback [ws-api/publish-di-success [::connection-creation-done vis-event pos]]}
                           (or indicator-desc indicator-id)
                           project?]]]})))

(re-frame/reg-event-fx
 ::connection-creation-done
 (fn [{db :db} [_ vis-event pos di]]
   {:dispatch [vis-event (get-in db path/open-frame-id)
               pos
               true
               {:overwrites {:info {:di di}
                             :color {:set-header-color? true}}}]}))

(defn direct-visualization [indicator-id project? disabled?]
  (let [available-visualizations @(fi/call-api :service-category-sub :visual-option)
        items (mapv
               (fn [[key {:keys [event tooltip-search] vertical-icon :icon}]]
                 (let [tooltip-text (if tooltip-search
                                      @(re-frame/subscribe tooltip-search)
                                      (error "tooltip-search not found for key " key))]
                   {:id (str indicator-id "-" key)
                    :extra-class (str "tool-" (name key))
                    :title tooltip-text
                    :start-icon vertical-icon
                    :on-click (fn []
                                (re-frame/dispatch [::open-visualization indicator-id project? event]))
                    :disabled? (boolean disabled?)}))
               available-visualizations)]
    (reduce
     (fn [res item]
       (conj res
             [button item]))
     [:div.flex.gap-2]
     items)))