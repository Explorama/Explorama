(ns de.explorama.frontend.woco.app.core
  (:require
   ;;This must be on first place to ensure that fi api is set first
   [de.explorama.frontend.woco.api.core]
   [de.explorama.frontend.woco.config :as frontend-config]
   [de.explorama.shared.common.logging :as logging]
   [de.explorama.frontend.configuration.core :as configuration-core]
   [de.explorama.frontend.data-atlas.core :as data-atlas-core]
   [de.explorama.frontend.indicator.core :as indicator]
   [de.explorama.frontend.map.core :as map-core]
   [de.explorama.frontend.mosaic.core :as mosaic-core]
   [de.explorama.frontend.projects.core :as projects-core]
   [de.explorama.frontend.reporting.plugin.core :as reporting-plugin-core]
   [de.explorama.frontend.rights-roles.core :as rights-roles-core]
   [de.explorama.frontend.search.core :as search-core]
   [de.explorama.frontend.table.core :as table-core]
   [de.explorama.frontend.charts.core :as charts-core]
   [de.explorama.frontend.expdb.core :as expdb]
   [de.explorama.frontend.algorithms.core :as algorithms-core]
   [de.explorama.frontend.woco.core :as woco-core]
   [taoensso.timbre :as timbre :refer [error info]])) ;;require all app cores here

(logging/set-log-level frontend-config/DEFAULT_LOG_LEVEL
                       {:force-str-output? (not frontend-config/dev-mode?)})

(defn ^:export reload []
  (info "Reload Explorama..")
  #_(woco-core/rerender))

(defn ^:export init []
  (try
    (info "Starting Explorama..")
    (woco-core/init)
    (rights-roles-core/init)
    (.setTimeout js/window
                 (fn []
                   (configuration-core/init)
                   (projects-core/init)
                   (expdb/init)
                   (data-atlas-core/init)
                   (table-core/init)
                   (charts-core/init)
                   (mosaic-core/init)
                   (map-core/init)
                   (search-core/init)
                   (indicator/init)
                   (algorithms-core/init)
                   (reporting-plugin-core/init))
                 500)
    (info "Frontend started")
    (catch :default e
      (error e "Initalization failed - force app crash")
      (js/electronAPI.forceAppCrash))))
