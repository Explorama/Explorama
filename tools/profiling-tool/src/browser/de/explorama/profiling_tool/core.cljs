(ns de.explorama.profiling-tool.core
  (:require
   ;;This must be on first place to ensure that fi api is set first
   [de.explorama.frontend.woco.api.core]
   [de.explorama.frontend.woco.app.core :as woco]
   [de.explorama.profiling-tool.resources :as resources]
   [de.explorama.profiling-tool.verticals.expdb-import :as expdb-import]
   [de.explorama.profiling-tool.verticals.expdb-acs :as expdb-acs]
   [de.explorama.frontend.ui-base.utils.data-exchange :as data-exchange]
   [de.explorama.profiling-tool.verticals.data-atlas :as data-atlas]
   [de.explorama.profiling-tool.verticals.map :as map]
   [de.explorama.profiling-tool.verticals.mosaic :as mosaic]
   [de.explorama.profiling-tool.verticals.projects :as projects]
   [de.explorama.profiling-tool.verticals.search :as search]
   [de.explorama.profiling-tool.verticals.visualization :as visualization]
   [taoensso.timbre :refer [info]]))

(defn start []
  (info "Starting profiling")
  (info "Starting import benchmarking")
  (expdb-import/vertical-benchmark-all)
  (info "Starting ac benchmarking")
  #_(expdb-acs/vertical-benchmark-all)
  (info "Starting search benchmarking")
  #_(search/vertical-benchmark-all)
  (info "Starting data-atlas benchmarking")
  #_(data-atlas/vertical-benchmark-all)
  (info "Starting map benchmarking")
  (map/vertical-benchmark-all)
  (info "Starting mosaic benchmarking")
  (mosaic/vertical-benchmark-all)
  (info "Starting projects benchmarking")
  (projects/vertical-benchmark-all)
  (info "Starting visualization benchmarking")
  (visualization/vertical-benchmark-all)
  (info "Profiling done"))

(defn download []
  (data-exchange/download-content
   (str (.toISOString (js/Date. (.now js/Date))) "-benchmark-browser.edn")
   @resources/results)
  nil)

(defn init []
  (woco/init))