(ns de.explorama.frontend.data-atlas.db-utils
  (:require [de.explorama.frontend.data-atlas.path :as path]))

(defn frame-exist-guard [frame-id old-db new-db]
  (if (boolean (and frame-id (get-in old-db (path/frame frame-id))))
    new-db
    old-db))