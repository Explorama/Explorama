(ns de.explorama.frontend.woco.api.compare
  (:require [de.explorama.frontend.woco.path :as path]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug]]
            [vimsical.re-frame.cofx.inject :as inject]))

(re-frame/reg-event-fx
 ::add-event
 [(re-frame/inject-cofx ::inject/sub
                        ^:ignore-dispose [:de.explorama.frontend.woco.api.interaction-mode/render?])]
 (fn [{db :db is-in-render-mode? :de.explorama.frontend.woco.api.interaction-mode/render?} [_ {:keys [frame-id event-id left top context]}]]
   (when (and frame-id event-id is-in-render-mode? (path/event-details event-id))
     (debug "Add Event to Details-view" frame-id event-id context left top)
     {:dispatch-n [[:de.explorama.frontend.woco.details-view/add-to-comparison {:new-event-id event-id
                                                                                :first-element? true
                                                                                :frame-id frame-id}]
                   [:de.explorama.frontend.woco.details-view/open-details-view true]]})))

(re-frame/reg-event-fx
 ::remove-event
 [(re-frame/inject-cofx ::inject/sub
                        ^:ignore-dispose [:de.explorama.frontend.woco.api.interaction-mode/render?])]
 (fn [{db :db is-in-render-mode? :de.explorama.frontend.woco.api.interaction-mode/render?} [_ {:keys [frame-id event-id]}]]
   (debug "Remove Event from Details-view" frame-id event-id)
   (when (and event-id is-in-render-mode?)
     {:dispatch-n [[:de.explorama.frontend.woco.details-view/remove-from-comparison {:remove-event-id event-id}]]})))

