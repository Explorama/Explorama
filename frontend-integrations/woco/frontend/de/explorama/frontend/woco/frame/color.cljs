(ns de.explorama.frontend.woco.frame.color
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.path :as path]))

(defn access-data [connection-data access-key]
  (cond (nil? access-key) connection-data
        (vector? access-key) (get-in connection-data access-key)
        (not (vector? access-key)) (get connection-data access-key)))

(def max-group-num 15)
(def group-classes (mapv #(str "explorama__window__group-" %) (range 1 (inc max-group-num))))

(def reference-classes (mapv #(str "explorama__reference-" %) (range 1 (inc max-group-num))))

(defn get-group-class [current-group-num]
  (let [group-class (get group-classes current-group-num)
        reference-class (get reference-classes current-group-num)
        new-group-num (inc current-group-num)]
    [(mod new-group-num max-group-num)
     group-class
     reference-class]))

(defn new-header-color [db frame-id]
  (let [[new-group-num group-class reference-class] (get-group-class (get-in db path/current-group 0))]
    (-> (assoc-in db
                  (path/frame-header-color frame-id)
                  group-class)
        (assoc-in path/current-group
                  new-group-num))))

(re-frame/reg-event-fx
 ::new-header-color
 (fn [{db :db} [_ frame-id]]
   (let [db (new-header-color db frame-id)]
     {:db db
      :dispatch [:de.explorama.frontend.woco.event-logging/log-event frame-id "header-colors" (get-in db path/frame-header-colors)]})))

(defn- color-parent [db frame-id]
  (let [frame-published-by (get-in db (path/frame-published-by frame-id))]
    (cond (get-in db (path/frame-publishing? frame-id))
          frame-id
          frame-published-by
          (get-in db (path/frame-published-by frame-id))
          :else frame-id)))

(defn header-color [db frame-id]
  (get-in db (path/frame-header-color (color-parent db frame-id))))

(re-frame/reg-sub
 ::header-color
 (fn [db [_ frame-id]]
   (header-color db frame-id)))