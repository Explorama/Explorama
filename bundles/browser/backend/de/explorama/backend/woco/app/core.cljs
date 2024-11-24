(ns de.explorama.backend.woco.app.core
  (:require [de.explorama.backend.algorithms.backend :as algorithms-backend]
            [de.explorama.backend.charts.backend :as charts-backend]
            [de.explorama.backend.configuration.backend :as configuration-backend]
            [de.explorama.backend.data-atlas.backend :as data-atlas-backend]
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
            [taoensso.timbre :refer [debug info]]))

(logging/set-log-level backend-config/DEFAULT_LOG_LEVEL
                       {:force-str-output? (not backend-config/dev-mode?)})

(defn ^:export reload [_changed-files]
  (debug "reload worker"))

(defonce initialized (atom nil))

(defn ^:export init []
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
  (info "Backend started"))

(when-not @initialized
  (reset! initialized true)
  (init))