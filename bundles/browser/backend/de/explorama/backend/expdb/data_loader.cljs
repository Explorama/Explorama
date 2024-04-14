(ns de.explorama.backend.expdb.data-loader
  (:require [data.dummy-data-netflix :as dummy-data]
            [data.dummy-data-roadmap :as dummy-data-roadmap]
            [de.explorama.backend.expdb.persistence.shared :as imp]))

(defn load-data []
  (imp/transform->import dummy-data-roadmap/data {} "default")
  (imp/transform->import dummy-data/data {} "default"))
