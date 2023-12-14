(ns de.explorama.frontend.projects.views.tooltip
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [re-frame.core :as re-frame]
            [clojure.string :as string]))

(defn step-action-button [{:keys [tooltip-text icon-class deactivated event direction style]
                           :or {direction :up
                                tooltip-text ""}}]
  [button {:variant :secondary
           :disabled? deactivated
           :title tooltip-text
           :tooltip-extra-params {:direction direction
                                  :text tooltip-text
                                  :distance 15
                                  :extra-style style}
           :on-click #(do
                        (.stopPropagation %)
                        (when (and event
                                   (not deactivated))
                          (re-frame/dispatch event)))
           :start-icon icon-class}])

(defn tool-button [{:keys [tooltip-text container-extra-class icon-class deactivated func direction]
                    :or {direction :right
                         tooltip-text ""}}]
  [:div {:style {:width :fit-content}
         :class "project__tool__wrapper"
         :on-click #(do
                      (.stopPropagation %)
                      (when (and func
                                 (not deactivated))
                        (func)))}
   [tooltip {:direction direction
             :text tooltip-text
             :distance 15
             :extra-class (string/join " " (remove string/blank? ["projects__project__tool" container-extra-class]))
             :extra-style {:cursor :pointer}}
    [:span {:href "#"
            :class icon-class}]]])
