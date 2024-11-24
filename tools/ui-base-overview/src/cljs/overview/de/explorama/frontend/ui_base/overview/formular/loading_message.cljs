(ns de.explorama.frontend.ui-base.overview.formular.loading-message
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.formular.loading-message :refer [loading-message default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Loading Message"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [loading-message]]"
   :desc "Message with a loading animation."
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [loading-message {:show? true}]
  {:title "Default"})

(defexample
  [:div.flex.gap-64.align-items-center
   [loading-message {:show? true :size :small}]
   [loading-message {:show? true :size :medium}]
   [loading-message {:show? true :size :large}]]
  {:title "Sizes"})

(defexample
  [loading-message {:show? true
                    :orientation :right
                    :message "Iam at the right side"}]
  {:title "Right-Message"})

(defexample
  [loading-message {:show? true
                    :orientation :left
                    :message "Iam at the left side"}]
  {:title "Left-Message"})