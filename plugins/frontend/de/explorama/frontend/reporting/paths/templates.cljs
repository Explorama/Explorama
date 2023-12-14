(ns de.explorama.frontend.reporting.paths.templates
  (:require [de.explorama.frontend.reporting.paths.discovery-base :as discovery-base-path]))

(def templates-root-key :templates)
(def templates-root (conj discovery-base-path/root templates-root-key))

(def all-templates-key :all)
(def all-templates (conj templates-root all-templates-key))

(defn template [template-id]
  (conj all-templates template-id))