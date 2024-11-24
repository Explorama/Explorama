(ns de.explorama.frontend.woco.frame.interaction.z-order
  (:require [re-frame.core :refer [reg-event-fx]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.components.context-menu :refer [close-context-menu-db]]
            [de.explorama.frontend.woco.frame.util :refer [list-frames is-content-frame? is-custom-frame?]]
            [reagent.core :as r]))

(defn bring-to-front
  "Caluclates and applies new ordering of frames with bringen the frame with frame-id to the front"
  ([db frame-id]
   (bring-to-front db (list-frames db nil) frame-id))
  ([db frames frame-id]
   (if (is-custom-frame? db frame-id)
     (let [db (reduce (fn [db {custom-type-frame-id :id}]
                        (cond-> db
                          (is-custom-frame? db custom-type-frame-id)
                          (assoc-in (path/frame-z-index custom-type-frame-id)
                                    (if (= frame-id custom-type-frame-id)
                                      config/custom-frame-z-index-ontop
                                      config/custom-frame-z-index-min))))

                      db
                      frames)]
       [db true config/custom-frame-z-index-ontop])
     (let [curr-max-zindex (get-in db path/curr-max-zindex config/z-index-min)
           frames-zindex-limit (- config/z-index-max
                                  config/z-index-min)
           max-zindex-reached? (<= config/z-index-max curr-max-zindex)
           frames-limit-reached? (<= frames-zindex-limit (count frames))
           new-max-zindex (if (and max-zindex-reached?
                                   (not frames-limit-reached?))
                            (+ config/z-index-min
                               (count frames))
                            (inc curr-max-zindex))
           is-maximized? (get-in db (path/frame-is-maximized frame-id))
           frame-idx (get-in db (path/frame-z-index frame-id))]
       (if (or is-maximized?
               (= frame-idx curr-max-zindex))
         [db false curr-max-zindex]
         [(:db (reduce (fn [{ndb :db :keys [idx-counter]} {o-frame-id :id zidx :z-index}]
                         (let [idx-counter (when idx-counter (inc idx-counter))
                               new-zindex (cond (= frame-id
                                                   o-frame-id)
                                                new-max-zindex ;bring to front                                        
                                                idx-counter idx-counter ;recalc all idx (limit reached)
                                                :else zidx)]
                           (cond-> {:db (assoc-in ndb (path/frame-z-index o-frame-id) new-zindex)}
                             idx-counter
                             (assoc :idx-counter idx-counter))))
                       (cond-> {:db (assoc-in db path/curr-max-zindex new-max-zindex)}
                         (and max-zindex-reached?
                              (not frames-limit-reached?))
                         (assoc :idx-counter (dec config/z-index-min)))
                       (if max-zindex-reached?
                         (sort-by :z-index frames)
                         frames)))
          true
          new-max-zindex])))))

;; Workaround to find out if there is a click in frame until last screenshot
(defonce clicked-focused-frame (r/atom nil))

(defn reset-focused-frame []
  (reset! clicked-focused-frame nil))

(reg-event-fx
 ::bring-to-front
 (fn [{db :db} [_ frame-id ignore-context-menu-handling?]]
   (let [frames (list-frames db nil)]
     (when (not-empty (get-in db (path/frame-desc frame-id)))
       (let [[db is-new? z-index] (bring-to-front db frames frame-id)]
         (if is-new?
           (reset-focused-frame)
           (reset! clicked-focused-frame frame-id))
         {:db (cond-> db
                (not ignore-context-menu-handling?)
                (close-context-menu-db))
          :dispatch-n [(if (and is-new?
                                (is-content-frame? db frame-id))
                         [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id]
                         [:de.explorama.frontend.woco.event-logging/broadcast-frame-event frame-id])]})))))

(defn z-index-lookup
  "Should only be used on frame creation."
  [db]
  (let [curr-max-zindex (get-in db path/curr-max-zindex config/z-index-min)]
    (inc curr-max-zindex)))