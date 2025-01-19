(ns de.explorama.backend.woco.app.core
            ;;require all app backends here
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
            [taoensso.timbre :refer [error info]]))

(defonce initialized (atom nil))

(defn init []
  (try
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
    (catch Throwable e
      (error e "Initalization failed - force app crash"))))

(when-not @initialized
  (reset! initialized true)
  (init))