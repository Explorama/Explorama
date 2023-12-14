(ns de.explorama.backend.expdb.data-loader
  (:require [de.explorama.backend.expdb.dummy-data-netflix :as dummy-data]
            [de.explorama.backend.expdb.dummy-data-roadmap :as dummy-data-roadmap]
            [de.explorama.backend.expdb.persistence.shared :as imp]))

(defn load-data []
  (imp/transform->import dummy-data-roadmap/data {} "default")
  (imp/transform->import dummy-data/data {} "default"))
