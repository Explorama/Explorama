(ns de.explorama.frontend.data-atlas.config)

(def debug?
  ^boolean goog.DEBUG)

(def tool-name "data-atlas")
(def default-namespace :data-atlas)
(def default-vertical-str (name default-namespace))

(def header-height 32) ;TODO r1/styles dynamic or merge this with other plugins
