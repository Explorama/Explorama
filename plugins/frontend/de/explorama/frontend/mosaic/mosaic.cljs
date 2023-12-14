(ns de.explorama.frontend.mosaic.mosaic
  (:require [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.frontend.mosaic.config :as gconfig]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.vis.config :as config]
            [re-frame.core :as re-frame]
            [taoensso.tufte :as tufte]))

(tufte/add-basic-println-handler! {})

(defn top-level-container-id [container-path]
  (str container-path "-top-box-body"))

(defn top-level-header-container-id [container-path]
  (str container-path "-top-box-header"))

(defn create-top-level
  ([db
    container-path
    width
    height
    position]
   (create-top-level db
                     container-path
                     width
                     height
                     position
                     nil))
  ([db
    container-path
    width
    height
    position
    title]
   (let [{top :top left :left} position]
     (-> db
         (assoc-in (gp/top-level container-path)
                   {:top top
                    :left left
                    :container-id (top-level-container-id container-path)
                    :container-header-id (top-level-header-container-id container-path)
                    :width width
                    :height height
                    :all-count 0
                    :global-count 0
                    :title title
                    :local-count 0})))))

(defn- gen-canvas-id [path]
  (str (gp/frame-id path) "-" 0 "-" (random-uuid)))

(defn- add-canvas [path init-state app-state]
  (let [host-name (gen-canvas-id path)
        canvas (merge
                {:host host-name
                 :type :canvas
                 :status :new
                 :app-state-path path}
                init-state
                app-state)]
    (gdb/set-events! path #js [])
    canvas))

(defn initialize [db frame-id size]
  (let [old-container {:width (first size)
                       :height (config/body-height (second size))}
        width (get old-container :width config/mosaic-top-width)
        height (get old-container :height config/mosaic-top-height)
        app-state-path (gp/canvas frame-id)
        canvas-size (config/canvas-size (or width config/mosaic-top-width)
                                        (or height
                                            config/mosaic-top-height))]
    (-> (create-top-level db
                          (gp/container-path frame-id)
                          width
                          height
                          {:top 0 :left 0})
        (assoc-in app-state-path
                  (add-canvas app-state-path
                              canvas-size
                              canvas-size)))))

(re-frame/reg-event-fx
 ::connected-to-di
 (fn [{db :db}
      [_ frame-id di filtered-data data-count filtered-data-count task-id]]
   (tufte/profile
    {:when gconfig/timelog-tubes?}
    (tufte/p
     ::connected-to-di
     (let [{:keys [opts]} (fi/call-api :frame-db-get db frame-id)
           path (gp/top-level frame-id)
           canvas-path (gp/canvas path)
           {grp-by-key gcp/grp-by-key}
           (get-in db (gp/operation-desc frame-id))
           filtered-data (if grp-by-key
                           (gdal/conj
                            (clj->js [{:group-key? true
                                       :key :root}])
                            filtered-data)
                           filtered-data)
           new-title (:new-title opts)]
       (gdb/set-events! canvas-path filtered-data)
       {:db (-> db
                (update-in path
                           (fn [val]
                             (-> val
                                 (dissoc gp/data-request-pending-key)
                                 (assoc :all-count data-count
                                        :global-count data-count
                                        :local-count (or filtered-data-count data-count)
                                        :title new-title))))
                (assoc-in (conj canvas-path :cards-all)
                          data-count)
                (assoc-in (gp/data-instance path) di)
                (assoc-in (gp/external-ref-set path) (set (:di/external-ref di))))
        :dispatch [:de.explorama.frontend.mosaic.render.actions/update path :reconnect? task-id]})))))
