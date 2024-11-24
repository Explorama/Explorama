(ns de.explorama.frontend.woco.frame.events
  (:require [re-frame.core :refer [reg-sub]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.config :as config]))

(def query :frame/query)
(def init :frame/init)
(def recreate :frame/recreate)
(def connection-negotiation :frame/connection-negotiation)
(def override :frame/override)
(def connect-to :frame/connect-to)
(def update-frame :frame/update)
(def selection :frame/selection)
(def close :frame/close)
(def id :frame/id)

(def custom-type :frame/custom-type) ;; e.g. frames, which dont need any frame skeleton like sticky-notes
(def content-type :frame/content-type)
(def consumer-type :frame/consumer-type)
(def management-type :frame/management-type)

(reg-sub
 ::frames
 (fn [db]
   (get-in db path/frames)))

(defn ordered-consumer-frames [frames priorized-frames ignore-frame-ids]
  (sort-by :z-index
           >
           (reduce (fn [res {:keys [id data-consumer] :as fr}]
                     (cond
                       (or (not data-consumer)
                           (and (set? ignore-frame-ids)
                                (ignore-frame-ids id)))
                       res

                       data-consumer
                       (conj res (if (and priorized-frames (priorized-frames id))
                                   (assoc fr :z-index 99999999)
                                   fr))

                       :else res))
                   []
                   frames)))

(defn frame-desc [db frame-id]
  (get-in db (path/frame-desc frame-id)))

(defn frame-state-aware [db frame-id]
  (let [frame-desc (get-in db (path/frame-desc frame-id))]
    (cond (:is-maximized? frame-desc)
          (let [{:keys [top left width height]} (get-in db path/workspace-rect)
                {legend? :frame-open-legend} frame-desc]
            (assoc frame-desc
                   :size [(if legend?
                            (- width config/legend-width)
                            width)
                          height]))
          (:is-minimized? frame-desc)
          (assoc frame-desc :size [(get-in frame-desc [:size 0])
                                   config/minimized-height])
          :else frame-desc)))

(reg-sub
 ::frame
 (fn [db [_ frame-id]]
   (frame-desc db frame-id)))

(reg-sub
 ::frame-state-aware
 (fn [db [_ frame-id]]
   (frame-state-aware db frame-id)))

(reg-sub
 ::is-maximized?
 (fn [db [_ id]]
   (get-in db (path/frame-is-maximized id))))

(reg-sub
 ::is-minimized?
 (fn [db [_ id]]
   (get-in db (path/frame-is-minimized id))))

(reg-sub
 ::on-drop
 (fn [db [_ frame-id]]
   (get-in db (path/frame-on-drop frame-id))))

(reg-sub
 ::frame-type
 (fn [db [_ frame-id]]
   (get-in db (path/frame-type frame-id))))