(ns de.explorama.frontend.algorithms.path.core
  (:require [taoensso.timbre :refer [error]]))

(def root-key :algorithms)
(def frames [root-key :frames])

(def data-instances [root-key :data-instances])

(defn frame [frame-id]
  (conj frames frame-id))

;? find a better place for this function
(defn clear-path [db path frames]
  (if (seq frames)
    (apply update-in db path dissoc frames)
    db))

;? find a better place for this function
(defn clean-frames [db frames-to-delete]
  (clear-path db frames frames-to-delete))

(defn frame-filter [frame-id]
  (conj (frame frame-id)
        :frame-filter))

(defn flyout-open? [frame-id]
  (conj (frame frame-id)
        :flyout-open?))

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(def data-instance-consuming-key :data-instance-consuming)
(defn data-instance-consuming [frame-id]
  (conj (frame frame-id) data-instance-consuming-key))

(defn data-instance-publishing [frame-id]
  (conj (frame frame-id) :data-instance-publishing))

(def replay-progress
  [root-key :replay-progress])

(def replay-frames-hit-callback-tracker
  [root-key :replay-frames-hit-callback-tracker])

(def procedures
  [root-key :procedures])

(def predictions
  [root-key :predictions])

(defn load-prediction [frame-id]
  (conj (frame frame-id) :load-prediction))

(defn is-predicting? [frame-id]
  (conj (frame frame-id) :is-predicting?))

(def problem-types
  [root-key :problem-types])

(defn data-options [frame-id]
  (conj (frame frame-id) :data-options))

(defn data-options-server [frame-id]
  (conj (frame frame-id) :data-options-server))

(defn last-prediction-task [frame-id]
  (conj (frame frame-id) :last-prediction-task))

(defn training-data [frame-id]
  (conj (frame frame-id) :training-data))

(defn loading [frame-id]
  (conj (frame frame-id) :loading?))

(defn result [frame-id]
  (conj (frame frame-id) :result))

(defn prediction-task [frame-id]
  (conj (result frame-id) :prediction-task))

(defn reduced-settings [frame-id]
  (conj (frame frame-id) :reduced-settings))

(defn status [frame-id]
  (conj (frame frame-id) :status))

(def volatile-acs [root-key :volatile-acs])

(def project-temp-key :project-temp)

(defn ratom-app-state-sync [frame-id]
  (conj (frame frame-id)
        project-temp-key))

(defn goal-state [frame-id]
  (conj (ratom-app-state-sync frame-id)
        :goal-state))

(defn settings-state [frame-id]
  (conj (ratom-app-state-sync frame-id)
        :settings-state))

(defn parameter-state [frame-id]
  (conj (ratom-app-state-sync frame-id)
        :parameter-state))

(defn simple-parameter-state [frame-id]
  (conj (ratom-app-state-sync frame-id)
        :simple-parameter-state))

(defn future-data-state [frame-id]
  (conj (ratom-app-state-sync frame-id)
        :future-data-state))

(defn data-changed [frame-id]
  (conj (frame frame-id)
        :data-changed))

(defn reset-view [frame-id]
  (conj (frame frame-id)
        :reset-view))

(defn pred-id [frame-id]
  (conj (result frame-id)
        :prediction-id))

(defn frame-dialog [frame-id]
  (conj (result frame-id)
        :frame-dialog))

(defn training-data-id [frame-id]
  (conj (result frame-id)
        :training-data-id))

(def notifications-key :notifications)
(def not-supported-redo-ops-key :not-supported-redo-ops)
(def redo-problem-type-before-key :redo-problem-type-before)
(def undo-connection-update-event-key :undo-connection-update-event)

(defn notifications [frame-id]
  (conj (frame frame-id)
        notifications-key))

(defn not-supported-redo-ops [frame-id]
  (conj (notifications frame-id)
        not-supported-redo-ops-key))

(defn redo-problem-type-before [frame-id]
  (conj (notifications frame-id)
        redo-problem-type-before-key))

(defn undo-connection-update-event [frame-id]
  (conj (frame frame-id)
        undo-connection-update-event-key))

(defn di-desc [frame-id]
  (conj (frame frame-id) :di-desc))

(defn custom-title [frame-id]
  (conj (frame frame-id) :custom-title))

(defn connect-task-id [frame-id]
  (conj (frame frame-id) :connect-task-id))

(defn export-states [frame-id]
  (conj (frame frame-id) :export-states))

(defn show-filter? [frame-id]
  (conj (frame frame-id) :show-filter?))

(defn reset-stop-views [db frame-id]
  (-> db
      (dissoc-in (frame-dialog frame-id))))