(ns de.explorama.frontend.reporting.views.dashboards.view
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.paths.discovery-base :as path]
            [de.explorama.frontend.reporting.util.frames :as frames-util]
            [de.explorama.frontend.reporting.views.module-loading-screen :refer [module-loading-screen]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [de.explorama.frontend.reporting.views.legend :refer [legend-toggle legend legend-open?]]
            [de.explorama.frontend.reporting.data.templates :as templates]
            [de.explorama.frontend.reporting.views.parameters :as parameters]
            [de.explorama.frontend.reporting.config :as config]
            [clojure.string :refer [split]]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub
                                   subscribe]]))

(defn visible-dashboard [db dashboard-id]
  (get-in db (dr-path/visible-dr dashboard-id)))

(reg-sub
 ::visible-dr
 (fn [db [_ dashboard-id]]
   (visible-dashboard db dashboard-id)))

(reg-sub
 ::dashboard-subtitle
 (fn [db [_ dashboard-id]]
   (:subtitle (visible-dashboard db dashboard-id))))

(defn- dashboard-description [dashboard-id]
  (let [subtitle @(subscribe [::dashboard-subtitle dashboard-id])]
    [:div.dashboard__description subtitle]))

(reg-sub
 ::module-sizes
 (fn [db [_ dashboard-id]]
   (let [{:keys [width height top left padding]} (parameters/dashboard-container-size (fi/call-api [:tabs :tab-content-size-db-get] db))
         {:keys [template-id]} (visible-dashboard db dashboard-id)
         {:keys [grid tiles]} (templates/template db template-id)
         [gw gh] grid
         tile-width-unit (/ width gw)
         tile-height-unit (/ height gh)]
     (reduce (fn [acc [tile-idx {[x y] :position
                                 [w h] :size
                                 legend-position :legend-position}]]
               (assoc acc tile-idx
                      {:left (+ left (* x tile-width-unit))
                       :top (+ top (* y tile-height-unit))
                       :width (- (* w tile-width-unit) padding)
                       :height (- (* h tile-height-unit) padding)
                       :legend-position legend-position}))
             {}
             (map-indexed (fn [idx itm] [idx itm]) tiles)))))

(reg-sub
 ::module
 (fn [db [_ dashboard-id tile-idx]]
   (-> (visible-dashboard db dashboard-id)
       (get-in [:modules tile-idx]))))


(defn tile-module [dashboard-id tile-idx {:keys [legend-position] :as size-params}]
  (let [{:keys [frame-id title vertical module state di]} @(subscribe [::module dashboard-id tile-idx])
        legend-active? (legend-open? frame-id)
        size-params (select-keys size-params [:top :left :width :height])
        module-size (parameters/dashboard-module-size size-params legend-position legend-active?)]
    [:div.dashboard__item {:style (-> size-params
                                      (assoc :position :absolute))}
     (when (and module vertical state)
       [:<>
        [module-loading-screen frame-id size-params]
        [:div.title {:title title}
         [:span.title__content
          title]
         [:div.options {:class export-ignore-class}
          [legend-toggle frame-id legend-active?]]]
        [:div.content {:class (case legend-position
                                :right "legend-right"
                                :bottom "legend-bottom"
                                "")}
         [module
          frame-id
          (assoc state
                 :di di
                 :size module-size)]
         [legend frame-id legend-position legend-active?]]])]))

(defn- modules-grid [dashboard-id]
  (let [sizes @(subscribe [::module-sizes dashboard-id])]
    (reduce (fn [acc [tile-idx size-params]]
              (conj acc [tile-module dashboard-id tile-idx size-params]))
            [:div.dashboard__layout {:style {:position :relative}}]
            sizes)))

(defn dashboard-view [{dashboard-id :id}]
  (let [{:keys [left top]} @(fi/call-api [:tabs :tab-content-size-sub])]
    [:div.dashboard__container {:id (config/export-dom-id :dashboard dashboard-id)
                                :style {:margin-top top
                                        :margin-left left}}
     [modules-grid dashboard-id]
     [dashboard-description dashboard-id]]))
    ;;  [datasource-info-wrapper dashboard-id]]))