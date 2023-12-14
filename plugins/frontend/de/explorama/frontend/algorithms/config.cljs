(ns de.explorama.frontend.algorithms.config
  (:require [clojure.set :as st]))

(def debug?
  ^boolean goog.DEBUG)

;; Purpose of the data attached to a model-frame. going forward a model-frame might be connected to more than one data object for different purposes beyond training eg  application and the like
(def data-purpose
  :training-data)

(def ki-origin (aget js/window "EXPLORAMA_ALGORITHMS_ORIGIN"))

(def default-namespace :algorithms)
(def default-vertical (name default-namespace))
(def default-vertical-str (name default-namespace))

(def default-chart-height 200)

(def tool-name "tool-algorithms")
(defn frame-body-dom-id [frame-id]
  (str frame-id "__" default-vertical "-frame-body"))

(def max-attempts 4000)
