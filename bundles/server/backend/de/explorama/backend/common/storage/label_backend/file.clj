(ns de.explorama.backend.common.storage.label-backend.file
  (:require [de.explorama.backend.storage.file-backend-helper :as file-helper]
            [de.explorama.backend.labels.label-protocol :as lp]))

(def ^:private labels-file "labels.edn")

(defonce labels-store (atom {}))

(defn- init! [labels-path watch-fn]
  (file-helper/load-file-to-atom labels-path labels-store {})
  (file-helper/add-save-watcher labels-path labels-store :label-store :pretty)
  (add-watch labels-store :label-propagation watch-fn))

(defn- write-labels [labels]
  (reset! labels-store #(merge-with merge % labels)))

(defn- overwrite-labels [labels]
  (swap! labels-store labels))

(defn- read-labels []
  @labels-store)

(deftype
 File-Backend []
  lp/Label-Backend
  (write-labels [instance labels]
    (write-labels labels))
  (overwrite-labels [instance labels]
    (overwrite-labels labels))
  (read-labels [instance]
    (read-labels)))

(defn new-instance [label-store-dir watch-fn]
  (init! (fn [] (str label-store-dir labels-file)) watch-fn)
  (File-Backend.))