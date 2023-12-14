(ns de.explorama.frontend.woco.frame.info
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.frame.events :as evts]))

(defn- frame-info-api [db frame-id]
  (fi/call-api :frame-info-api-get-db
               db
               frame-id))

(defn gather-information [db publishing-frame-id & [overwrites]]
  (let [publishing-frame-info-api (frame-info-api db publishing-frame-id)]
    (cond-> (into {}
                  (map (fn [[info-key func]]
                         [info-key (func db publishing-frame-id)])
                       (dissoc publishing-frame-info-api :custom)))
      (:custom publishing-frame-info-api)
      (assoc :custom (into {} (map (fn [[custom-key func]]
                                     [custom-key (func db publishing-frame-id)])
                                   (:custom publishing-frame-info-api))))
      overwrites
      (merge (:info overwrites)))))

(re-frame/reg-sub
 ::published-by
 (fn [db [_ frame-id]]
   (get-in db (path/frame-published-by frame-id))))

(re-frame/reg-sub
 ::publishing?
 (fn [db [_ frame-id]]
   (get-in db (path/frame-publishing? frame-id))))

(re-frame/reg-event-fx
 ::set-publishing
 (fn [{db :db} [_ frame-id value]]
   (let [db (assoc-in db (path/frame-publishing? frame-id) value)]
     {:db db
      :dispatch [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id]})))

(defn api-value [db frame-id values]
  (when-let [api (frame-info-api db frame-id)]
    (if (vector? values)
      (into {}
            (map (fn [value]
                   (if (vector? value)
                     ((get-in api values) db frame-id)
                     ((get api values) db frame-id)))
                 values))
      ((get api values) db frame-id))))

(re-frame/reg-sub
 ::api-value
 (fn [db [_ frame-id values]]
   (api-value db frame-id values)))

(defn source?
  ([frame-desc]
   (:publishing? frame-desc))
  ([db frame-id]
   (get-in db (path/frame-publishing? frame-id))))

(defn children [db frame-id]
  (let [frames (get-in db path/frames frame-id)]
    (into {} (filter (fn [[_ frame]]
                       (and (= frame-id (:published-by-frame frame))
                            (not (source? frame))))
                     frames))))

(defn parent [db frame-id]
  (let [frame-published-by (get-in db (path/frame-published-by frame-id))]
    (cond (get-in db (path/frame-publishing? frame-id))
          frame-id
          (and frame-published-by
               (get-in db (path/frame-desc frame-published-by)))
          (get-in db (path/frame-published-by frame-id))
          :else frame-id)))

(re-frame/reg-event-fx
 ::update-children
 (fn [{db :db} [_ frame-id values]]
   {:fx (mapv (fn [[fid]]
                [:dispatch [(get-in db (path/frame-event fid))
                            evts/update-frame
                            (assoc {:frame-id fid}
                                   :payload values)]])
              (children db frame-id))}))