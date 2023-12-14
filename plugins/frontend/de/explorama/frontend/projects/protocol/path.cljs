(ns de.explorama.frontend.projects.protocol.path
  (:require [de.explorama.frontend.projects.path :as path]))

(def root path/root)

(def protocol-window-root :protocol)
(def protocol-window [root protocol-window-root])
(def protocol-window-open (conj protocol-window
                                :open?))
(def protocol-window-only-snapshots (conj protocol-window
                                          :snapshots-only))
(def protocol-not-open (conj protocol-window
                             :not-open-reasons))
(def step-loading [root :step-loading?])
(def step-loaded-desc-key :step-loaded-desc)
(def step-loaded-desc [root step-loaded-desc-key])
(def step-read-only-key :step-read-only?)
(def step-read-only [root step-read-only-key])

(def snapshot-editor [root :snapshot-editor])
(def snapshot-edit-active? (conj snapshot-editor :active?))
(def snapshot-edit-title (conj snapshot-editor :title))
(def snapshot-edit-description (conj snapshot-editor :description))