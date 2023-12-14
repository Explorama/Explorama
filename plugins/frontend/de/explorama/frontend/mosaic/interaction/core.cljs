(ns de.explorama.frontend.mosaic.interaction.core
  (:require [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]))

(defn calculate-position [_ _ start event scale]
  (let [m-left (.-clientX event)
        m-top (.-clientY event)
        relative-x (max (- (:parent-left start))
                        (+ (:left start)
                           (/ (- m-left
                                 (:x start))
                              scale)))
        relative-y (max (- (:parent-top start))
                        (+ (:top start)
                           (/ (- m-top
                                 (:y start))
                              scale)))]
    [relative-x relative-y]))

(re-frame/reg-event-db
 ::move-container
 (fn [db [_ path x y]]
   (update-in db path assoc :top y :left x)))

(def simple-drag-end-action
  [::simple-drag-end-action])

(re-frame/reg-event-fx
 ::simple-drag-end-action
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ {path :source} scale event _]]
   (let [start (get-in db gp/drag-start-path)
         [relative-x relative-y] (calculate-position db path start event scale)]
     {:dispatch [::move-container path relative-x relative-y]})))