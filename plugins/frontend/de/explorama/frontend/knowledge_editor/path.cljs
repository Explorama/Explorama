(ns de.explorama.frontend.knowledge-editor.path
  (:require [taoensso.timbre :refer [error]]))

(def root [:knowledge-editor])

(defn frame [frame-id] (conj root :frames frame-id))

(defn canvas-status [frame-id] (conj (frame frame-id) :canvas :status))
(defn canvas-content-root [frame-id] (conj (frame frame-id) :canvas :content))
(defn canvas-content-title [frame-id] (conj (frame frame-id) :canvas :content :title))
(defn canvas-content [frame-id] (conj (frame frame-id) :canvas :content :data))
(defn canvas-parked [frame-id] (conj (frame frame-id) :canvas :parked))
(defn details [frame-id] (conj (frame frame-id) :details))
(defn canvas-dialog [frame-id] (conj (frame frame-id) :canvas-dialog))

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))