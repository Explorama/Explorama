(ns de.explorama.frontend.charts.util.queue
  (:require [re-frame.core :refer [reg-sub reg-event-fx subscribe]]
            [de.explorama.frontend.common.queue :as ddq]))

;; Used if an wrapper is needed before event is pushed to queue.
;; Example: local-filter will be requested from other frame and these will conj it to the given event
(reg-event-fx
 ::queue-wrapper
 (fn [_ [_ frame-id event-vec & additional-event-params]]
   {:dispatch [::ddq/queue frame-id
               (conj event-vec (or additional-event-params []))]}))

(reg-sub
 ::loading?
 (fn [[_ frame-id] _]
   [(subscribe [::ddq/finished? frame-id])])
 (fn [[queue-finished?] _]
   (not queue-finished?)))