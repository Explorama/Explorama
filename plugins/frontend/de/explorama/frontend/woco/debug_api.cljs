(ns de.explorama.frontend.woco.debug-api
  (:require [re-frame.db :as rf-db]
            [de.explorama.frontend.woco.path :as path]))

(defn ^:export frame-ids []
  (clj->js (mapv str (keys (get-in @rf-db/app-db path/frames)))))
