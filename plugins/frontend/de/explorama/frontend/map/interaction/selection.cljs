(ns de.explorama.frontend.map.interaction.selection
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::select
 [(fi/ui-interceptor)]
 (fn [_ [_ frame-id selection]]
   {:dispatch (fi/call-api :select-event-vec frame-id selection)}))

(re-frame/reg-event-fx
 ::deselect
 [(fi/ui-interceptor)]
 (fn [_ [_ frame-id deselection]]
   {:dispatch (fi/call-api :deselect-event-vec frame-id deselection)}))
