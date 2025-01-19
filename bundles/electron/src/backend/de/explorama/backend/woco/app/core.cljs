(ns de.explorama.backend.woco.app.core
            ;;require all app backends here
  (:require [de.explorama.backend.algorithms.backend :as algorithms-backend]
            [de.explorama.backend.charts.backend :as charts-backend]
            [de.explorama.backend.configuration.backend :as configuration-backend]
            [de.explorama.backend.data-atlas.backend :as data-atlas-backend]
            [de.explorama.backend.electron.config :as electron-config]
            [de.explorama.backend.expdb.backend :as expdb-backend]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.indicator.backend :as indicator-backend]
            [de.explorama.backend.map.backend :as map-backend]
            [de.explorama.backend.mosaic.backend :as mosaic-backend]
            [de.explorama.backend.projects.backend :as projects-backend]
            [de.explorama.backend.reporting.backend :as reporting-backend]
            [de.explorama.backend.search.backend :as search-backend]
            [de.explorama.backend.table.backend :as table-backend]
            [de.explorama.backend.woco.backend :as woco-backend]
            [de.explorama.shared.common.logging :as logging]
            [de.explorama.shared.woco.config :as backend-config]
            [taoensso.timbre :refer [debug error info]]))

(logging/set-log-level backend-config/DEFAULT_LOG_LEVEL
                       {:force-str-output? (not backend-config/dev-mode?)
                        :appenders-conf (:appenders electron-config/log-config)})

(defn ^:export reload [_changed-files]
  (debug "reload worker"))

(defonce initialized (atom nil))

(defn ^:export init []
  (try
    (when-not backend-config/dev-mode?
      (js/addEventListener
       "error"
       (fn [e]
       ;; Force to write out global errors to log file
         (.preventDefault e)
         (error (aget e "error")))))
    (expdb-backend/init)
    (woco-backend/init)
    (configuration-backend/init)
    (projects-backend/init)
    (data-atlas-backend/init)
    (table-backend/init)
    (charts-backend/init)
    (mosaic-backend/init)
    (map-backend/init)
    (reporting-backend/init)
    (search-backend/init)
    (indicator-backend/init)
    (algorithms-backend/init)
    (frontend-api/init)
    (info "Backend started")
    (catch :default e
      (error e "Initalization failed - force app crash")
      (js/electronAPI.forceAppCrash))))

(when-not @initialized
  (reset! initialized true)
  (init))