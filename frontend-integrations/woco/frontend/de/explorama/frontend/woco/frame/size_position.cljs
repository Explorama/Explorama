(ns de.explorama.frontend.woco.frame.size-position
  (:require [reagent.core :as r]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.path :as path]))

(defonce frame-positions (r/atom {}))

(defn frame-position-sub [frame-id]
  (r/cursor frame-positions [frame-id]))

(defn set-frame-position [frame-id position]
  (swap! frame-positions assoc frame-id position))

(defn delete-frame-position [frame-id]
  (swap! frame-positions dissoc frame-id))

(defn reset-frame-positions []
  (reset! frame-positions {}))

(def grid-border [0 0])
(def grid-period [50 50])

(defn calculate-coords [grid-coords]
  (map +
       grid-border
       (map * grid-period grid-coords)))

(defn calculate-size [grid-size]
  (map * grid-period grid-size))

(defn calculate-min-h [min-h]
  (if min-h
    (* min-h (get grid-period 1))
    nil))

(defn resize-info [delta-width delta-height width height full-width full-height & opts]
  (let [opts (set opts)]
    (cond-> {:delta-width delta-width
             :delta-height delta-height
             :width width
             :height height
             :full-width full-width
             :full-height full-height}
      (opts :force?)
      (assoc :force? true))))

(defn set-frame-size
  "Sets frame-size"
  [db frame-id width height]
  (update-in db
             (path/frame-size frame-id)
             (fn [[old-width old-height]]
               [(or width old-width)
                (or height old-height)])))

(defn set-frame-full-size
  "Sets frame-size"
  [db frame-id fw fh]
  (update-in db
             (path/frame-full-size frame-id)
             (fn [[old-width old-height]]
               [(or fw old-width)
                (or fh old-height)])))

(defn set-resize-infos
  "Sets resize infos"
  [db frame-id resized-infos]
  (if (not-empty (get-in db (path/frame-desc frame-id)))
    (assoc-in db
              (path/frame-resized-infos frame-id)
              resized-infos)
    db))

(defn legend-size [frame-desc legend-open?]
  (cond-> frame-desc
    (not legend-open?)
    (assoc :full-size (:size frame-desc))
    legend-open?
    (update-in [:full-size 0] + config/legend-width)))