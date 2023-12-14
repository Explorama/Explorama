(ns de.explorama.frontend.map.paths
  (:require [taoensso.timbre :refer [error]]))

;; APP-State Paths
(def root :map)

(def callback-vec-key :callback-vec)
(def config-key :config)
(def layer-config-key :layers)
(def feature-layer-config-key :overlayers-designer)

(def replay-progress-key :replay-progress)
(def replay-progress [root replay-progress-key])

(def replay-update-needed-key :replay-update-con-needed)
(def replay-update-needed [root replay-update-needed-key])

(def volatile-acs [root :volatile-acs])
(defn volatile-acs-frame [frame-id] [root :volatile-acs frame-id])

(def replay-update-current-key :replay-update-con-done)
(def replay-update-current [root replay-update-current-key])

(def map-config
  [root config-key])

(def acs [root :acs])
(def color-acs (conj acs :color-acs))
(def full-acs (conj acs :full-acs))
(def geolocated-acs (conj acs :geo-acs))
(def context-acs (conj acs :context-acs))

(def layer-config (conj map-config layer-config-key))
(def base-layers (conj layer-config :base-layers))
(def overlayers (conj layer-config :overlayers :overlays))
(def feature-color-layers (conj layer-config :overlayers :feature-layers))
(defn feature-color-layer [layer-id]
  (conj feature-color-layers layer-id))

(def feature-layer-config (conj map-config feature-layer-config-key))

(def active-overlayer-config (conj feature-layer-config
                                   :active-overlayer-config))

(def feature-layer-changes (conj feature-layer-config
                                 :feature-layer-changes))

(def new-feature-layer (conj feature-layer-config
                             :new-feature-layer))

(def raw-marker-layouts (conj feature-layer-config
                              :raw-marker-layout-config))

(def raw-feature-layers (conj feature-layer-config
                              :raw-feature-layers-config))

(def module-configs (conj feature-layer-config :module-configs))

(defn feature-layer-comp-change
  ([feature-layer-id comp-key]
   (conj feature-layer-changes
         feature-layer-id
         comp-key))
  ([feature-layer-id grp-key comp-key]
   (conj feature-layer-changes
         feature-layer-id
         grp-key
         comp-key)))

(defn frame-desc [frame-id]
  [root frame-id])

(defn removed-detail-view-events [frame-id]
  (conj (frame-desc frame-id)
        :removed-detail-view-events))

(defn stop-view-display [frame-id]
  (conj (frame-desc frame-id)
        :display-stop?))

(defn stop-view-details [frame-id]
  (conj (frame-desc frame-id)
        :stop-view-details))

(defn frame-selections [frame-id]
  (conj (frame-desc frame-id)
        :selections))

(defn frame-callback-vec [frame-id]
  (conj (frame-desc frame-id)
        callback-vec-key))

(defn frame-loading [frame-id]
  (conj (frame-desc frame-id)
        :loading-view))

(defn frame-active-layers [frame-id]
  (conj (frame-desc frame-id)
        :active-layer))

(defn frame-active-layer [frame-id layer-id]
  (conj (frame-active-layers frame-id)
        layer-id))

(defn frame-position [frame-id]
  (conj (frame-desc frame-id)
        :position))

(defn frame-external-refs [frame-id]
  (conj (frame-desc frame-id)
        :external-refs))

(defn frame-di
  "Description where the data comes from (publisher)"
  [frame-id]
  (conj (frame-desc frame-id)
        :di))

(defn frame-task-id [frame-id]
  (conj (frame-desc frame-id)
        :current-task-id))

(defn frame-warn-screen [frame-id]
  (conj (frame-desc frame-id)
        :warn))

(defn frame-warn-view? [frame-id]
  (conj (frame-warn-screen frame-id)
        :show?))

(defn frame-warn-callback [frame-id]
  (conj (frame-warn-screen frame-id)
        :callback))

(defn marker-layout-data [frame-id]
  (conj (frame-desc frame-id)
        :marker-layer-data))

(defn add-overlayer-active? [frame-id]
  (conj (frame-desc frame-id)
        :add-overlayer-active?))

(defn applied-filter [frame-id]
  (conj (frame-desc frame-id)
        :applied-filter))

(defn show-filter? [frame-id]
  (conj (frame-desc frame-id)
        :show-filter?))

(def filter-key :filter)
(def counts-key :counts)

(defn frame-filter [frame-id]
  (conj (frame-desc frame-id)
        filter-key))

(defn warn-filterview [frame-id]
  (conj (frame-filter frame-id)
        :show-warn?))

(defn stop-filterview [frame-id]
  (conj (frame-filter frame-id)
        :show-stop?))

(defn data-acs [frame-id]
  (conj (frame-filter frame-id)
        :data-acs))

(defn frame-filter-counts [frame-id]
  (conj (frame-filter frame-id)
        counts-key))

(defn frame-count-global [frame-id]
  (conj (frame-filter-counts frame-id)
        :global))

(defn frame-count-local [frame-id]
  (conj (frame-filter-counts frame-id)
        :local))

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(def notifications-key :notifications)
(def not-supported-redo-ops-key :not-supported-redo-ops)
(def undo-connection-update-event-key :undo-connection-update-event)

(defn notifications [frame-id]
  (conj (frame-desc frame-id)
        notifications-key))

(defn not-supported-redo-ops [frame-id]
  (conj (notifications frame-id)
        not-supported-redo-ops-key))

(defn undo-connection-update-event [frame-id]
  (conj (frame-desc frame-id)
        undo-connection-update-event-key))

;; Layout
(def layers-path [root :layers])

(defn usable-marker-layouts-id [path]
  (conj (frame-desc path)
        :usable-marker-layouts-id))

(defn usable-feature-layouts-id [frame-id]
  (conj (frame-desc frame-id)
        :usable-feature-layouts-id))

(defn too-much-data? [frame-id]
  (conj (frame-desc frame-id)
        :too-much-data?))

(defn headless? [frame-id]
  (conj (frame-desc frame-id)
        :headless?))

(defn data-request-pending [frame-id]
  (conj (frame-desc frame-id)
        :data-request?))
(defn frame-state [frame-id]
  (conj (frame-desc frame-id)
        :state))

(defn frame-state-update [frame-id]
  (conj (frame-state frame-id)
        :update-timestamp))

(defn running-tasks [frame-id]
  (conj (frame-desc frame-id)
        :running-tasks))

(defn base-layer [frame-id]
  (conj (frame-state frame-id)
        :base-layer))

(defn selected-marker-layouts [frame-id]
  (conj (frame-desc frame-id)
        :selected-marker-layouts))

(defn cluster-marker? [path]
  (conj (frame-state path)
        :cluster?))

(defn popup-desc [frame-id]
  (conj (frame-state frame-id)
        :popup-desc))

(defn frame-di-desc [frame-id]
  (conj (frame-state frame-id)
        :di-desc))

(defn dim-info [frame-id]
  (conj (frame-di-desc frame-id) :dim-info))

(defn frame-displayable-data [frame-id]
  (conj (frame-state frame-id)
        :displayable-data))

(defn view-position [frame-id]
  (conj (frame-state frame-id)
        :view-position))

(defn highlighted-markers [frame-id]
  (conj (frame-state frame-id)
        :highlighted-markers))

(defn selected-overlayers [frame-id]
  (conj (frame-state frame-id)
        :selected-overlayers))

(defn selected-feature-layers [frame-id]
  (conj (frame-desc frame-id)
        :selected-feature-layers))

(defn removed-feature-layers [frame-id]
  (conj (frame-desc frame-id)
        :removed-feature-layers))

(defn temp-feature-layer-config [frame-id]
  (conj (frame-desc frame-id)
        :temp-configs))

(defn temp-raw-marker-layouts [frame-id]
  (conj (temp-feature-layer-config frame-id)
        :marker-layouts))

(defn temp-raw-feature-layers [frame-id]
  (conj (temp-feature-layer-config frame-id)
        :feature-layers))