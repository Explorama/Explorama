(ns de.explorama.backend.expdb.data-loader
  (:require [de.explorama.backend.expdb.persistence.shared :as imp]
            [clojure.edn :as edn]))

(defn load-data []
  (imp/transform->import (edn/read-string (slurp "resources/dummy-data-roadmap.edn" :encoding "UTF-8") ) {} "default")
  (imp/transform->import (edn/read-string (slurp "resources/dummy-data-netflix.edn" :encoding "UTF-8") ) {} "default"))
