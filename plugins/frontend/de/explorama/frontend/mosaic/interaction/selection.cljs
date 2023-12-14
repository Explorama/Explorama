(ns de.explorama.frontend.mosaic.interaction.selection
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::select
 [(fi/ui-interceptor)]
 (fn [_ [_ path selection]]
   (let [frame-id (gp/frame-id path)]
     {:dispatch (fi/call-api :select-event-vec frame-id selection {:instance-path path})})))

(re-frame/reg-event-fx
 ::deselect
 [(fi/ui-interceptor)]
 (fn [_ [_ path deselection]]
   (let [frame-id (gp/frame-id path)]
     {:dispatch  (fi/call-api :deselect-event-vec frame-id deselection {:instance-path path})})))