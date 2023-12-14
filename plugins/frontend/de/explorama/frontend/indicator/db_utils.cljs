(ns de.explorama.frontend.indicator.db-utils
  (:require [de.explorama.frontend.indicator.path :as path]))

(defn frame-exist-guard [old-db new-db]
  (if (seq (get-in old-db path/open-frame-id))
    new-db
    old-db))