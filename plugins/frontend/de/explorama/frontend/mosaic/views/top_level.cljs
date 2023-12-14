(ns de.explorama.frontend.mosaic.views.top-level
  (:require [de.explorama.frontend.mosaic.interaction.context-menu.canvas :refer [context-menu-canvas]]
            [de.explorama.frontend.mosaic.interaction.tooltip :as tooltip-canvas]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.views.canvas :as goocanvas]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::rename-title
 [(.-uiInterceptor js/window)]
 (fn [db [_ path title]]
   (assoc-in db (conj path :rename-value) title)))

(re-frame/reg-sub
 ::rename-title
 (fn [db [_ path]]
   (get-in db (conj path :rename-value))))

(re-frame/reg-event-db
 ::set-title
 (fn [db [_ path new-title]]
   (update-in db path assoc :title new-title :custom-title true)))

(re-frame/reg-sub
 ::annotation
 (fn [db [_ path]]
   (get-in db (gp/top-level-annotation path))))

(re-frame/reg-event-db
 ::annotation
 (fn [db [_ path annotation]]
   (assoc-in db (gp/top-level-annotation path) annotation)))

(re-frame/reg-sub
 ::top-level
 (fn [db [_ path]]
   (get-in db (gp/top-level path))))

(defn top-level-container [path vis-settings]
  (let [{mosaic-box-body-id :container-id
         body-height :height
         body-width :width
         {:as canvas?} :canvas}
        @(re-frame/subscribe [::top-level path])
        mosaic-box-body {:width body-width
                        :height body-height
                        :outline :none}]
    [:<>
     (with-meta
       [context-menu-canvas path]
       {:key (str "context-menu-canvas" path)})
     [tooltip-canvas/groups (gp/frame-id path)]
     [:div {:style (assoc mosaic-box-body :overflow :hidden)}
      [:div.mosaic__box__body {:style mosaic-box-body
                              :id mosaic-box-body-id
                              :on-context-menu #(.preventDefault %)
                              :key (str "mosaic-body-" path)}
       (when canvas?
         (with-meta
           [goocanvas/reagent-canvas (conj path :canvas) vis-settings]
           {:key (str "mosaic-canvas-" (conj path :canvas))}))]]]))
