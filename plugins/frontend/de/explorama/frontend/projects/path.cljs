(ns de.explorama.frontend.projects.path
  (:require [taoensso.timbre :refer [error]]))

(def root :projects)

(def replay-progress-key :replay-progress)
(def replay-progress [root replay-progress-key])

;; FIXME remove this pattern.  This kind of triple function punning is not helpful.
;; Just store the paths here.

(defn locks
  ([]
   [root :locks])
  ([db]
   (get-in db (locks)))
  ([db value]
   (assoc-in db (locks) value)))

(defn project-id
  ([]
   [root :loaded-project])
  ([db]
   (get-in db (project-id)))
  ([db value]
   (assoc-in db (project-id) value)))

(defn loading-project-id
  ([]
   [root :loading-project])
  ([db]
   (get-in db (loading-project-id)))
  ([db value]
   (assoc-in db (loading-project-id) value)))

(def project-loading
  [root :loading-project])

(def projects
  [root :projects])

(def current-search-query
  "Used to know if the result from the server is still for the query."
  [root :current-query])

(def current-project-filter
  [root :current-filter])

(def current-project-sorting
  [root :current-sorting])

(def created-project-titles
  [root :created-projects-titles])

(def created-projects
  (conj projects
        :created-projects))

(def log-callback-key :log-callback)
(def log-callback [root log-callback-key])

(def confirm-dialog-root :confirm-dialog)
(def confirm-dialog [root confirm-dialog-root])

(def snapshot-window-root :snapshot-window)
(def snapshot-window [root snapshot-window-root])
(def snapshot-window-open (conj snapshot-window :open?))
(def dialog-infos (conj confirm-dialog :infos))
(def dialog-active (conj confirm-dialog :is-active?))

(def snapshot-loading [root :loading-snapshot])
(def loaded-snapshot [root :loaded-snapshot])
(def snapshot-read-only (conj loaded-snapshot :read-only?))

(defn execute-events [project-id]
  [root :execute-events project-id])

(defn load-counter [project-id]
  [root :load-counter project-id])

(defn logs-to-load [project-id]
  [root :logs-to-load project-id])

(def origins-to-load-key :origins-to-load)
(def origins-to-load [root origins-to-load-key])

(def origins-loaded-key :origins-loaded)
(def origins-loaded [root origins-loaded-key])

(def replay-progress-paths-key :replay-progress-paths)
(def replay-progress-paths [root replay-progress-paths-key])

(def to-be-loaded [root :to-be-loaded])

(defn remove-to-be-loaded [db]
  (update db root dissoc :to-be-loaded))

(def overlayer-active-key :overlayer-active?)
(def overview-overlayer-active?
  [root overlayer-active-key])

(def ignore-overview-filter-key :overlayer-show-all?)

(def overview-show-all?
  [root ignore-overview-filter-key])

(def project-show-confirm-dialog
  [root :show-confirm-dialog])

(defn project-update-detail [update-type]
  [root update-type])

(def welcome-callback-fx-key :welcome-callback)
(def welcome-callback-fx [root welcome-callback-fx-key])

(def new-project-key :new-project)
(def new-project [root new-project-key])

(def new-project-title (conj new-project :title))
(def new-project-desc (conj new-project :description))

(def card-menu-props [root new-project :card-menu-props])
(def force-read-only? [root :force-read-only?])

(def joined-users [root :sync :joined-users])
(def show-cursors [root :sync :show-cursors?])

(def open-at-step-dialog
  [root :open-at-step-dialog])

(def opened-step
  [root :opened-step])

(def to-be-loaded-step
  [root :to-be-loaded-step])

(defn remove-to-be-loaded-step [db]
  (update db root dissoc :to-be-loaded-step))
