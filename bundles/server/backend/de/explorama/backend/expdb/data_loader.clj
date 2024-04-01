(ns de.explorama.backend.expdb.data-loader
  (:require [de.explorama.backend.expdb.dummy-data-roadmap :as dummy-data-roadmap]
            [de.explorama.backend.expdb.persistence.shared :as imp]))

(defn load-data []
  (imp/transform->import dummy-data-roadmap/data {} "default"))
