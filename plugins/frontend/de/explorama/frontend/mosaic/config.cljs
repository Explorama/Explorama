(ns de.explorama.frontend.mosaic.config)

(def debug?
  ^boolean goog.DEBUG)

(def timelog-operations false)
(def timelog-filter? false)
(def timelog-tubes? false)

(def max-transfer-tries 10)
(def transfer-waiting-time 500) ;ms

(def log-error false)

(def default-namespace :mosaic)
(def default-vertical-str (name default-namespace))

(def configs (js->clj (aget js/window "EXPLORAMA_MOSAIC_CONFIGS") :keywordize-keys true))

;char for .. when pruning text
(def prune-char \u2026) ;".." \u2025

(def white-replacement "#f8f8f8")