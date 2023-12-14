(ns de.explorama.frontend.reporting.views.dropzone
  "Handles the dropzone-highlighting from tiles e.g. when dragging an frame over the creation tiles"
  (:require [reagent.core :as r]))

(defonce dropzone-state (r/atom {}))
(defn tile-dropzone-sub [tile-idx]
  (r/cursor dropzone-state [tile-idx]))

(defn activate-dropzone-state [tile-idx]
  (swap! dropzone-state assoc tile-idx true))

(defn clear-dropzone-state
  ([tile-idx]
   (swap! dropzone-state dissoc tile-idx))
  ([]
   (reset! dropzone-state {})))