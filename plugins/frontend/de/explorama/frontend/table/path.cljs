(ns de.explorama.frontend.table.path
  (:require [taoensso.timbre :refer [error]]))

(def root-key :table)
(def filter-key :filter)
(def table-root [root-key])

(def replay-progress-key :replay-progress)
(def replay-progress [root-key replay-progress-key])

(defn frame-desc [frame-id]
  [root-key frame-id])

(defn removed-detail-view-events [frame-id]
  (conj (frame-desc frame-id) :removed-detail-view-events))

(defn di-desc [frame-id]
  (conj (frame-desc frame-id)
        :di-desc))

(defn dim-info [frame-id]
  (conj (di-desc frame-id)
        :dim-info))

(defn custom-title [frame-id]
  (conj (frame-desc frame-id)
        :custom-title))

(defn frame-di [frame-id]
  (conj (frame-desc frame-id)
        :di))

(defn frame-external-refs [frame-id]
  (conj (frame-desc frame-id)
        :external-refs))

(defn frame-filter [frame-id]
  (conj (frame-desc frame-id)
        filter-key))

(defn filter-warn-limit-reached [frame-id]
  (conj (frame-filter frame-id)
        :filter-warn-limit-reached?))

(defn filter-stop-limit-reached [frame-id]
  (conj (frame-filter frame-id)
        :filter-stop-limit-reached?))

;; Handle Dialog -> Different then flags before, because user can hide
(defn stop-view-display [frame-id]
  (conj (frame-desc frame-id)
        :display-stop?))

(defn stop-view-details [frame-id]
  (conj (frame-desc frame-id)
        :stop-view-details))

(defn frame-warn [frame-id]
  (conj (frame-desc frame-id)
        :warn))

(defn warn-view-display [frame-id]
  (conj (frame-warn frame-id)
        :display?))

(defn warn-view-callback [frame-id]
  (conj (frame-warn frame-id)
        :callback))

(def warn-view-cancel-event-key :cancel-event)
(def warn-view-proceed-event-key :proceed-event)
(def warn-view-stop-event-key :stop-event)

(defn warn-view-cancel-event [frame-id]
  (conj (frame-desc frame-id)
        warn-view-cancel-event-key))

(defn warn-view-proceed-event [frame-id]
  (conj (frame-desc frame-id)
        warn-view-proceed-event-key))

(defn warn-view-stop-event [frame-id]
  (conj (frame-desc frame-id)
        warn-view-stop-event-key))

(defn applied-filter [frame-id]
  (conj (frame-desc frame-id)
        :applied-filter))

(defn show-filter? [frame-id]
  (conj (frame-desc frame-id)
        :show-filter?))

(defn last-request-params [frame-id]
  (conj (frame-desc frame-id)
        :last-request-params))

(def volatile-acs [root-key :volatile-acs])

(defn volatile-acs-frame [frame-id]
  [root-key :volatile-acs frame-id])

(def replay-update-needed-key :replay-update-con-needed)
(def replay-update-needed [root-key replay-update-needed-key])

(def replay-update-current-key :replay-update-con-done)
(def replay-update-current [root-key replay-update-current-key])

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

;Still used???

(def notifications-key :notifications)
(def not-supported-redo-ops-key :not-supported-redo-ops)
(def undo-connection-update-event-key :undo-connection-update-event)

(defn notifications [vis-path]
  (conj vis-path notifications-key))

(defn not-supported-redo-ops [vis-path]
  (conj (notifications vis-path)
        not-supported-redo-ops-key))

(defn undo-connection-update-event [vis-path]
  (conj vis-path undo-connection-update-event-key))

(defn reset-stop-views [db frame-id]
  (-> db
      (dissoc-in (stop-view-display frame-id))
      (dissoc-in (stop-view-details frame-id))
      (dissoc-in (filter-stop-limit-reached frame-id))
      (dissoc-in (filter-warn-limit-reached frame-id))))

(def table-frame frame-desc)

(defn height [frame-id]
  (conj (table-frame frame-id) :height))

(defn width [frame-id]
  (conj (table-frame frame-id) :width))

(defn size-changed [frame-id]
  (conj (table-frame frame-id) :size-changed))

(defn table-infos [frame-id]
  (conj (table-frame frame-id)
        :table-infos))

(def table-datasource frame-di)

(defn table-data [frame-id]
  (conj (table-infos frame-id) :table-data))

(defn infos-columns [frame-id]
  (conj (table-infos frame-id) :columns))

(defn infos-url [frame-id]
  (conj (table-infos frame-id) :url))

(defn flag-render [frame-id]
  (conj (table-infos frame-id) :flag))

(defn infos-limit [frame-id]
  (conj (table-infos frame-id) :limit))

(defn loaded-page [frame-id]
  (conj (table-infos frame-id)
        :loaded-page))

(defn current-selection [frame-id]
  (conj (table-frame frame-id)
        :current-selection))

(defn current-focus [frame-id]
  (conj (table-frame frame-id)
        :current-focus))

(defn replay-queue [frame-id]
  (conj (table-frame frame-id)
        :replay-queue))
