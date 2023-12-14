(ns de.explorama.backend.expdb.data-loader
  (:require [de.explorama.backend.expdb.dummy-data-1 :as dummy-data]
            [de.explorama.backend.expdb.persistence.shared :as imp]))

(defn load-data []
  (imp/transform->import dummy-data/data {} "default"))