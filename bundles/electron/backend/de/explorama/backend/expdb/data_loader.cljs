(ns de.explorama.backend.expdb.data-loader
  (:require [de.explorama.backend.expdb.persistence.shared :as imp]))

(defn load-data []
  #_(imp/transform->import dummy-data/data {} "default"))