(ns de.explorama.frontend.mosaic.render.exit
  (:require [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]))

(defn empty-check? [db path]
  (let [container (get-in db (gp/container-path (gp/frame-id path)))]
    (and (empty? (get-in container [:frame :current-job]))
         (empty? (get-in container [:frame :queue]))
         (empty? (get container gp/top-level-key)))))

(defn delete-frame [db path]
  (if (empty-check? db path)
    (gp/dissoc-in db (gp/frame-path path))
    db))

(defn unset-all! [path]
  (gdb/unset-events! path)
  (gdb/unset-events! {:path path :filter :local-filter})
  (gdb/unset-events! {:path path :filter :global-filter}))

(defn handle-ungrouped [db path]
  (let [tl-path (gp/top-level path)
        top-level (get-in db tl-path)]
    (cond (and (get top-level :exit?)
               (get top-level :exit-frame?))
            ;handle exit top-level with exit frame
          (let [db (gp/dissoc-in db (gp/container-path path))]
            (unset-all! path)
            (if (empty-check? db path)
                  ;delete-frame
              {:db (gp/dissoc-in db (gp/frame-path path))
               :dispatch [:de.explorama.frontend.mosaic.operations.core/canvas-exit path]}
                  ;wait for delete -> only delete top-level
              {:db db}))
          (get top-level :exit?)
            ;dissoc whole top-level
          (do
            (unset-all! path)
            {:db (delete-frame (gp/dissoc-in db (gp/container-path path))
                               path)
             :dispatch [:de.explorama.frontend.mosaic.operations.core/canvas-exit path]})
          :else
                ;dissoc only canvas
          {:db (update-in db tl-path dissoc gp/canvas-key)
           :dispatch [:de.explorama.frontend.mosaic.operations.core/canvas-exit path]})))

(re-frame/reg-event-fx
 ::ready-for-exit
 (fn [{db :db} [_ path]]
   (handle-ungrouped db path)))

(defn exit-sub-groups [db path]
  (assoc-in db
            (conj (gp/top-level path)
                  gp/canvas-key
                  :exit?)
            true))

(defn exit-top-level [db path]
  (-> db
      (assoc-in (conj (gp/top-level path)
                      :exit?)
                true)
      (exit-sub-groups path)))

(defn exit-frame [db path]
  (-> db
      (exit-top-level (gp/top-level path))
      (assoc-in (conj (gp/top-level path) :exit-frame?)
                true)))

(defn clean-track [path]
  [::ddq/dispose-tracks (gp/frame-id path)
   [:de.explorama.frontend.mosaic.operations.core/track-dispose-canvas-rendering (gp/top-level path)]])

(defn clean-couples [path]
  [:de.explorama.frontend.mosaic.operations.couple/decouple (gp/top-level path)])

(re-frame/reg-event-fx
 ::exit-frame
 (fn [{db :db} [_ path-or-id]]
   (let [path (if (vector? path-or-id)
                (gp/frame-path path-or-id)
                (gp/frame-path-id path-or-id))]
     {:db (exit-frame db path)
      :dispatch-n [(clean-track path)
                   (clean-couples path)]})))
