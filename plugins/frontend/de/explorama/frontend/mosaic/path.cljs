(ns de.explorama.frontend.mosaic.path
  (:require [taoensso.timbre :refer [error]]
            [de.explorama.shared.mosaic.common-paths :as gcp]))

(def mosaic-root :mosaic)
(def rights-and-roles-key :rights-and-roles)
(def login-root [rights-and-roles-key])

(def instances-key :instances)
(def instances [mosaic-root instances-key])
(def frame :frame)
(def top-level-key :top-level)
(def sub-groups-key :sub-groups)
(def filter-view-key :filter-view)
(def data-instance-key :data-instance)
(def canvas-key :canvas)

(defn is-mosaic-frame? [frame-id]
  (= (:vertical frame-id)
     "mosaic"))

(def filter-view [mosaic-root filter-view-key])

(def open-dialogs [mosaic-root :open-dialogs])

(def top-level-render-wait-key :top-level-render-wait)
(def render-wait-root [mosaic-root top-level-render-wait-key])

(def replay-update-needed-key :replay-update-con-needed)
(def replay-update-needed [mosaic-root replay-update-needed-key])

(def replay-update-current-key :replay-update-con-done)
(def replay-update-current [mosaic-root replay-update-current-key])

(def replay-progress-key :replay-progress)
(def replay-progress [mosaic-root replay-progress-key])

(defn frame-path-id [frame-id]
  (conj instances frame-id))

(defn frame-id [path-or-frame-id]
  (if (and (vector? path-or-frame-id)
           (= mosaic-root (first path-or-frame-id)))
    (get path-or-frame-id 2)
    path-or-frame-id))

(defn frame-path [path]
  (conj instances (frame-id path)))

(defn container-path [path]
  (conj instances (frame-id path)))

(defn top-level-instances [frame-id-or-path]
  (if (vector? frame-id-or-path)
    (conj instances (frame-id frame-id-or-path))
    (conj instances frame-id-or-path)))

(defn instance-by-frame-id
  ([frame-id]
   (conj instances
         frame-id))
  ([db frame-id]
   (get-in db (instance-by-frame-id frame-id))))

(defn frame-infos [frame-id-or-path]
  (conj instances
        (frame-id frame-id-or-path)
        frame))

(defn removed-detail-view-events [frame-id-or-path]
  (conj (frame-infos frame-id-or-path)
        :removed-detail-view-events))

(defn selected-years [frame-id-or-path]
  (conj (frame-infos frame-id-or-path)
        :selected-years))

(defn selected-countries [frame-id-or-path]
  (conj (frame-infos frame-id-or-path)
        :selected-countries))

(defn selected-datasources [frame-id-or-path]
  (conj (frame-infos frame-id-or-path)
        :selected-datasources))

(defn filtered-data-info [frame-id-or-path]
  (conj (frame-infos frame-id-or-path)
        :filtered-data-info))

(defn custom-title [frame-id-or-path]
  (conj (frame-infos frame-id-or-path)
        :custom-title))

(defn operations-state [path]
  (conj (frame-infos path)
        :operations-state))

(defn top-level [frame-id-or-path]
  (if (vector? frame-id-or-path)
    (conj instances
          (frame-id frame-id-or-path)
          top-level-key)
    (conj instances
          frame-id-or-path
          top-level-key)))

(def data-request-pending-key :data-request-pending?)
(defn data-request-pending [path]
  (conj (top-level path)
        data-request-pending-key))

(defn canvas [frame-id-or-path]
  (conj (top-level frame-id-or-path) canvas-key))

(defn undo-connection-update-event [path]
  (conj (top-level path)
        :undo-connection-update-event))

(defn add-layout-active? [path]
  (conj (top-level path)
        :add-layout-active?))

(defn top-level-context-menu [path]
  (conj (top-level path)
        :interaction :context-menu))

(defn top-level-canvas-context-menu [path]
  (conj (top-level path)
        :canvas :interaction :context-menu))

(defn sub-group-idx [path group-idx]
  (conj (canvas path)
        sub-groups-key
        group-idx))

(defn contains-sub-group? [path]
  (= sub-groups-key (get path 5)))

(defn sub-group-sub-path [path]
  (subvec path 4))

(defn replace-top-level-id [path top-level-id]
  (assoc path 2 top-level-id))

(defn data-instance [frame-id-or-path]
  (conj instances
        (frame-id frame-id-or-path)
        data-instance-key))

(defn applied-filter [frame-id-or-path]
  (conj instances
        (frame-id frame-id-or-path)
        :applied-filter))

(defn show-filter? [frame-id-or-path]
  (conj instances
        (frame-id frame-id-or-path)
        :show-filter?))

(defn top-level-operation [path]
  (conj (top-level path)
        :operation))

(defn top-level-annotation [path]
  (conj (top-level path)
        :annotation))

(defn frame-desc
  ([path]
   (conj instances
         (frame-id path)
         frame))
  ([db path]
   (get-in db (frame-desc path))))

(defn instance-datasources [path]
  (conj (frame-desc path)
        :datasources))

(defn external-ref-set [frame-id-or-path]
  (conj (frame-desc frame-id-or-path)
        :external-ref-set))

(defn filter-instances
  ([param]
   (if (vector? param)
     (conj filter-view (frame-id param))
     (conj filter-view param))))

(defn filter-desc [path-or-frame-id]
  (filter-instances (frame-id path-or-frame-id)))

(defn warn-filterview [path]
  (conj (filter-desc path)
        :show-warn?))

(defn warn-view [frame-id]
  [mosaic-root :warn-view frame-id :show])

(defn warn-view-callback [frame-id]
  [mosaic-root :warn-view frame-id :callback])

(defn stop-filterview [path]
  (conj (filter-desc path)
        :show-stop?))

(defn stop-view [frame-id]
  [mosaic-root :stop-view frame-id :show])

(defn stop-view-details [frame-id]
  [mosaic-root :stop-view frame-id :stop-view-details])

(defn data-acs [path-or-frame-id]
  (conj (filter-desc path-or-frame-id)
        :data-acs))

(defn plain-operation []
  {:type :plain})

(defn group-operation [by]
  {:type :group-by
   :by by})

(defn op-counter
  ([path]
   (conj (frame-desc path)
         :op-counter))
  ([db path]
   (get-in db (op-counter path)))
  ([db path value]
   (assoc-in db (op-counter path) value)))

(def drag-start-key :drag-start)

(def drag-start-path
  [mosaic-root drag-start-key])

(defn ignore-frame-update-path [frame-id]
  [mosaic-root :ignore-update frame-id])

(defn on-frame-exit [path]
  [mosaic-root :on-frame-exit (frame-id path)])

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(defn canvas-status [path]
  (conj (canvas path) :status))

(defn updates [path]
  (conj (canvas path) :updates))

(defn selected-layouts [path]
  (conj (frame-desc path)
        :selected-layout))

(defn usable-layouts [path]
  (conj (frame-desc path)
        :usable-layouts))

(defn card-margin [path]
  (conj (top-level path) :card-margin))

(defn css []
  [mosaic-root :css])

(defn layout-details [path]
  (conj (top-level path) :layout-details))

(defn card-layout [path]
  (conj (top-level path) :card-layout))

(defn maximized
  ([]
   [mosaic-root :maximized])
  ([frame-id]
   [mosaic-root :maximized frame-id]))

(defn render-wait [frame-id]
  (conj render-wait-root frame-id))

(defn tool-tip [path]
  (conj (canvas path)
        :tool-tip))

(defn layout-task-id [path]
  (conj (frame-desc path)
        :layout-task-id))

(def mosaic-configs
  [mosaic-root :configs])

(def layout-config (conj mosaic-configs
                         :active-layout-config))

(def layout-changes (conj mosaic-configs
                          :layout-changes))

(def new-layout (conj mosaic-configs
                      :new-layout))

(def raw-layouts (conj mosaic-configs
                       :raw-layouts))

(def attribute-tree
  (conj mosaic-configs :attribute-tree))

(def scatter-plot-ignored-events-key :scatter-plot-ignored-events)
(defn scatter-plot-ignored-events [path]
  (conj (top-level path)
        scatter-plot-ignored-events-key))

(def volatile-acs
  [mosaic-root :volatile-acs])

(defn volatile-acs-frame [frame-id]
  [mosaic-root :volatile-acs frame-id])

(def on-exit-callback-key :on-exit-callback)
(def on-exit-callback-root [mosaic-root on-exit-callback-key])

(defn on-exit-callback [path]
  (conj on-exit-callback-root
        (frame-id path)))

(def not-supported-redo-ops-key :not-supported-redo-ops)

(defn not-supported-redo-ops [frame-id]
  (conj (top-level frame-id)
        not-supported-redo-ops-key))

(def ignore-redo-ops-key :ignore-redo-ops)
(defn ignore-redo-ops [frame-id]
  (conj (top-level frame-id)
        ignore-redo-ops-key))

(def show-top-level-context-menu-key :show-top-level-context-menu)
(defn show-top-level-context-menu [frame-id]
  (conj (top-level frame-id)
        show-top-level-context-menu-key))

(defn operation-desc [frame-id-or-path]
  (conj (frame-desc frame-id-or-path) :operation-desc))

(defn operation-desc-prev [frame-id-or-path]
  (conj (frame-desc frame-id-or-path) :operation-desc-prev))

(defn operation-desc-last-logged [frame-id-or-path]
  (conj (frame-desc frame-id-or-path) :operation-desc-last-logged))

(defn operation-desc-current-logged [frame-id-or-path]
  (conj (frame-desc frame-id-or-path) :operation-desc-current-logged))

(defn sort-desc [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) gcp/sort-key))

(defn sort-grp-desc [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) gcp/sort-grp-key))

(defn sort-sub-grp-desc [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) gcp/sort-sub-grp-key))

(defn grp-by-desc [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) gcp/grp-by-key))

(defn sub-grp-by-desc [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) gcp/sub-grp-by-key))

(defn render-mode-desc [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) gcp/render-mode-key))

(defn local-filter
  ([]
   gcp/filter-key)
  ([frame-id-or-path]
   (conj (operation-desc frame-id-or-path)
         (local-filter)))
  ([db frame-id-or-path]
   (get-in db (local-filter frame-id-or-path))))

(defn selections [frame-id-or-path]
  (conj (operation-desc frame-id-or-path) :selections))

(defn fallback-layout [frame-id-or-path]
  (conj (frame-desc frame-id-or-path) :fallback-layout?))

(def ac-path [mosaic-root :ac])
(def ac-obj-path [mosaic-root :obj-ac])
(def ac-color-path [mosaic-root :color-ac])

(defn dim-info [frame-id-or-path]
  (conj (frame-desc frame-id-or-path) :dim-info))

(defn canvas-state-replay [frame-id]
  [mosaic-root :canvas-state-replay frame-id])

(def canvas-states-replay [mosaic-root :canvas-state-replay])

(defn frame-couple-synced? [frame-id]
  (conj (frame-desc frame-id) :couple-synced?))

(defn reset-stop-views [db frame-id]
  (-> db
      (dissoc-in (stop-view frame-id))
      (dissoc-in (stop-view-details frame-id))
      (dissoc-in (stop-filterview frame-id))
      (dissoc-in (warn-filterview frame-id))
      (dissoc-in (warn-view frame-id))
      (dissoc-in (warn-view-callback frame-id))))