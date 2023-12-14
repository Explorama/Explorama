(ns de.explorama.frontend.mosaic.views.canvas
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.pixi.common :as grpc]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [warn]]))

(re-frame/reg-sub
 ::status
 (fn [db [_ path]]
   (get-in db (gp/canvas-status path))))

(re-frame/reg-sub
 ::host
 (fn [db [_ path]]
   (get-in db (conj path :host))))

(re-frame/reg-sub
 ::width
 (fn [db [_ path]]
   (get-in db (conj (gp/canvas path) :width))))

(re-frame/reg-sub
 ::height
 (fn [db [_ path]]
   (get-in db (conj (gp/canvas path) :height))))

(defn render-guard [status]
  (= :new
     status))

(defn reagent-canvas [path vis-settings]
  (let [host-ref (atom nil)]
    (reagent/create-class {:display-name (str path)
                           :reagent-render
                           (fn [path {:keys [disable-canvas-click?]}]
                             (let [status @(re-frame/subscribe [::status path])
                                   host @(re-frame/subscribe [::host path])
                                   width @(re-frame/subscribe [::width path])
                                   height @(re-frame/subscribe [::height path])]
                               (when (and status height width host)
                                 [:canvas.mosaic-canvas
                                  (cond-> {:key host
                                           :ref #(reset! host-ref %)
                                           :id host
                                           :style {:width width
                                                   :height height}
                                           :on-drag-enter #() ;For Woco handling drop-target
                                           :on-drag-leave #() ;For Woco handling drop-target
                                           :on-mouse-up #(do (reset! grpc/drag-interaction nil)
                                                             (when-not disable-canvas-click?
                                                               (re-frame/dispatch (fi/call-api :reset-vertical-drag-event-vec))))
                                           :on-mouse-enter #(reset! grpc/drag-interaction-left false)
                                           :on-mouse-leave #(reset! grpc/drag-interaction-left true)}
                                    (not disable-canvas-click?)
                                    (assoc :on-click #(re-frame/dispatch (fi/call-api :frame-bring-to-front-event-vec (gp/frame-id path)))))])))
                           :component-did-mount
                           (fn [_]
                             (let [status @(re-frame/subscribe [::status path])]
                               (when (render-guard status)
                                 (reset! grpc/vis-settings vis-settings)
                                 (re-frame/dispatch [:de.explorama.frontend.mosaic.render.core/init path]))))
                           :should-component-update
                           (fn [_ _ _]
                             (let [status @(re-frame/subscribe [::status path])]
                               (render-guard status)))
                           :component-did-update
                           (fn [_ _]
                             (let [status @(re-frame/subscribe [::status path])
                                   host @(re-frame/subscribe [::host path])
                                   container (js/document.getElementById host)]
                               (when (nil? container)
                                 (warn "updating nil canvas" host))

                               (when (render-guard status)
                                 (re-frame/dispatch [:de.explorama.frontend.mosaic.render.core/init path]))))
                           :component-will-unmount
                           (fn [_])})))
                           ;(re-frame/dispatch [:de.explorama.frontend.mosaic.render.core/close path]))})) ;! prbly not needed?! -> throws a error because it does not exsists.
