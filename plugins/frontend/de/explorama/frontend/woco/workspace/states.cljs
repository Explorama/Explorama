(ns de.explorama.frontend.woco.workspace.states
  (:require [reagent.core :as r]))

(def multiselect-current-selection (atom #{}))
(def multiselect-bb-before-move (atom nil)) ;;To be able to reset it when temporary move will be aborted
(def multiselect-bb (r/atom nil))

;TODO r1/window-handling does it work to use both atoms as is-multi-select-active? -flag?
(defn multi-selection? []
  (seq @multiselect-current-selection))

(def temporary-frames (atom {}))
(def temporary-selection (r/atom #{}))

(def window-creation-mouse (r/atom nil))
