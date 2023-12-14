(ns de.explorama.frontend.map.config
  (:require [taoensso.tufte :as tufte]))

(def debug?
  ^boolean goog.DEBUG)

(def default-namespace :map)
(def default-vertical-str (name default-namespace))

(def tool-name "tool-map")
(defn frame-body-dom-id [{:keys [frame-id workspace-id]}]
  (str frame-id "__" workspace-id "__" default-vertical-str "-frame-body"))

(def default-language :en-GB)

(def max-config-wait-tries 100) ; 100ms per try

(def selectable-color-limit 15)

;Used to decide if the clustering can be disabled by the user
(def marker-cluster-threshold (aget js/window "EXPLORAMA_map_MARKER_CLUSTER_THRESHOLD") #_10000)
(def max-hover-marker (aget js/window "EXPLORAMA_map_MAX_MARKER_HOVER"))
(def profile-time (aget js/window "EXPLORAMA_map_PROFILE_TIME"))

(tufte/add-basic-println-handler! {})

;In ms when the actual click should be executed
;This is used to decide if instead of click dbl-click should be done
(def click-timeout 300)

(def marker-stroke-rgb-color ["80" "80" "80"])
(def marker-highlight-stroke-rgb-color ["255" "0" "0"])

(def default-area-color [238 238 238 0.5])

(def default-aggregate-method {"string" :first-matching-color
                               "number" :sum
                               :number :sum})