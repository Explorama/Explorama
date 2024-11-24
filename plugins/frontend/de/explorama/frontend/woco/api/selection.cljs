(ns de.explorama.frontend.woco.api.selection
  (:require [re-frame.core :as re-frame]
            [clojure.set :as cljset]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.event-logging :as event-log]))

(defn- find-selection-frame-id [db frame-id]
  (cond (frame-info/source? db frame-id)
        frame-id
        (get-in db (path/frame-published-by frame-id))
        (get-in db (path/frame-published-by frame-id))
        :else
        frame-id))

(defn selections [db frame-id]
  (->> (find-selection-frame-id db frame-id)
       path/selections
       (get-in db)))

(re-frame/reg-sub
 ::selections
 (fn [db [_ frame-id]]
   (selections db frame-id)))

(defn- change-selections  [type {db :db} [_ source-frame-id source-infos]]
  (let [selection-frame-id (find-selection-frame-id db source-frame-id)
        {old-selections :current} (get-in db (path/selections selection-frame-id) {})
        new-selection (case type
                        :select ((fnil conj []) old-selections source-infos)
                        :deselect (filterv #(not= source-infos %) old-selections)
                        :reset [])
        selections-desc {:current new-selection
                         :source-infos {:frame-id source-frame-id}
                         :last-action type}]
    {:db (assoc-in db (path/selections selection-frame-id) selections-desc)
     :fx (mapv (fn [[fid {event :event}]]
                 [:dispatch [event evts/selection {:frame-id fid
                                                   :payload {:selections selections-desc}}]])
               (frame-info/children db selection-frame-id))}))

(re-frame/reg-event-fx ::reset-selections (partial change-selections :reset))

(re-frame/reg-event-fx ::select (partial change-selections :select))

(re-frame/reg-event-fx ::deselect (partial change-selections :deselect))