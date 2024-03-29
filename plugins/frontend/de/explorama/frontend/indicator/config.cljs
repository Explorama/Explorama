(ns de.explorama.frontend.indicator.config)

(def debug?
  ^boolean goog.DEBUG)

(def default-namespace :indicator)
(def default-vertical-str (name default-namespace))

(def header-height 32) ;TODO r1/styles dynamic or merge this with other plugins
