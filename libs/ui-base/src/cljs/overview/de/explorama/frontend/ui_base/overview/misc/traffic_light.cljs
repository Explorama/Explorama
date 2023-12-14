(ns de.explorama.frontend.ui-base.overview.misc.traffic-light
  (:require [de.explorama.frontend.ui-base.components.misc.traffic-light :refer [traffic-light default-parameters parent-class parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Traffic light"
   :desc "Component to display traffic light indicator"
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [traffic-light]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [traffic-light {}]
  {:title "Default (empty)"})

(defexample
  [traffic-light {:parent-class (str parent-class " my-class")}]
  {:title "With parent-class"})

(defexample
  [traffic-light {:color :green}]
  {:title "Green"})

(defexample
  [traffic-light {:color :yellow}]
  {:title "Yellow"})

(defexample
  [traffic-light {:color :red}]
  {:title "Red"})

(defexample
  [traffic-light {:color :grey}]
  {:title "Grey (off)"})

(defexample
  [traffic-light {:label "I'am a Label"}]
  {:title "Only Label"})

(defexample
  [traffic-light {:color :green
                  :label "I'am green"}]
  {:title "Color with Label"})

(defexample
  [traffic-light {:color :yellow
                  :label "I'am yellow"
                  :hint-text "It's yellow because I like the sun 🌞"}]
  {:title "Color, label and hint"})
