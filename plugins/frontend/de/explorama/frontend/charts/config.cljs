(ns de.explorama.frontend.charts.config
  (:require [de.explorama.frontend.common.i18n :as i18n]))

(def default-namespace :charts)
(def default-vertical-str (name default-namespace))

(def tool-name-charts "tool-charts")
(defn frame-body-dom-id-charts [frame-id]
  (str frame-id "__" default-vertical-str "-frame-body"))

(def vis-origin (aget js/window "EXPLORAMA_charts_ORIGIN"))

(def header-height 36)

(def change-request-delay 3000) ;ms

; theme color

(def light-mode-grid-color "rgb(0, 0, 0, 0.1)")
(def dark-mode-grid-color "rgba(195, 199, 203, 0.1)")
(def light-mode-text-color "rgb(102, 102, 102)")
(def dark-mode-text-color "rgb(195, 199, 203)")

(defn ^String attribute->display [attribute]
  (str (i18n/attribute-label-with-unit attribute)))