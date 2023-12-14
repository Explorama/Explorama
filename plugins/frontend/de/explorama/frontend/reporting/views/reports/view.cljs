(ns de.explorama.frontend.reporting.views.reports.view
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.util.frames :as frames-util]
            [de.explorama.frontend.reporting.views.legend
             :refer [legend
                     legend-open?
                     legend-toggle]]
            [de.explorama.frontend.reporting.views.module-loading-screen :refer [module-loading-screen]]
            [de.explorama.frontend.reporting.views.parameters :as parameters]
            [de.explorama.frontend.reporting.views.text-module :as text]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [clojure.string :refer [split]]
            [re-frame.core :refer [reg-sub subscribe]]))

(defn visible-report [db report-id]
  (get-in db (dr-path/visible-dr report-id)))

(reg-sub
 ::visible-dr
 (fn [db [_ report-id]]
   (visible-report db report-id)))

(reg-sub
 ::module-sizes
 (fn [db [_ report-id]]
   (let [{:keys [width height top left padding]} (parameters/report-container-size (fi/call-api [:tabs :tab-content-size-db-get] db))
         {:keys [selected-template]} (visible-report db report-id)
         {:keys [grid tiles]} selected-template
         [gw gh] grid
         tile-width-unit (/ width gw)
         tile-height-unit (/ height gh)]
     (reduce (fn [acc [tile-idx {[_ y] :position
                                 [w h] :size
                                 legend-position :legend-position}]]
               (assoc-in acc
                         [y tile-idx]
                         {:width (- (* w tile-width-unit) padding)
                          :height (max height
                                       (- (* h tile-height-unit) padding))
                          :legend-position legend-position}))
             {}
             (map-indexed (fn [idx itm] [idx itm]) tiles)))))

(reg-sub
 ::report-titles
 (fn [db [_ report-id]]
   (select-keys (visible-report db report-id)
                [:subtitle :name])))

(reg-sub
 ::module
 (fn [db [_ report-id tile-idx]]
   (-> (visible-report db report-id)
       (get-in [:modules tile-idx]))))

(defn- datasource-info-wrapper [frame-id]
  (when-let [di-desc-sub (:di-desc-sub @(fi/call-api :papi-api-sub frame-id :legend))]
    (when-let [di-desc @(di-desc-sub frame-id)]
      (let [datasources  (-> di-desc
                             (get-in [:base :datasources])
                             (split #", ")
                             (set)
                             (sort))]
        [:div.dashboard__datasources
         [frames-util/datasources-info-node frame-id datasources]]))))

(defn- report-description [report-id]
  (let [subtitle @(subscribe [::report-subtitle report-id])]
    [:div.dashboard__description subtitle]))

(defn- tile-module [report-id tile-idx {:keys [legend-position] :as size-params}]
  (let [{:keys [frame-id title vertical module state di tool]} @(subscribe [::module report-id tile-idx])
        legend-active? (legend-open? frame-id)
        size-params (select-keys size-params [:width :height])
        text-module? (= tool "text")
        module-size (parameters/report-module-size size-params legend-position legend-active?)]
    [:div.report__element {:style size-params}
     (cond
       text-module?
       [text/read-only-text-module tile-idx state]
       (and module vertical state)
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
         [legend frame-id legend-position legend-active?]]
        [datasource-info-wrapper frame-id]])]))

(defn- modules-grid [report-id dom-id]
  (let [sizes @(subscribe [::module-sizes report-id])]
    (reduce (fn [acc [row row-data]]
              (conj acc
                    (reduce (fn [acc [tile-idx size-params]]
                              (conj acc [tile-module report-id tile-idx size-params]))
                            [:div.report__row {:style {:position :relative}
                                               :id (str dom-id row)}]
                            row-data)))
            [:<>]
            sizes)))

(defn- header [report-id dom-id]
  (let [{:keys [subtitle] title :name} @(subscribe [::report-titles report-id])]
    [:div.report__header {:id (str dom-id "header")}
     [:div.report__titles
      [:div.report__title title]
      [:div.report__subtitle subtitle]]]))


(defn report-view [{report-id :id}]
  (let [{:keys [left top]} @(fi/call-api [:tabs :tab-content-size-sub])
        dom-id (config/export-dom-id :report report-id)]
    [:div.a4-container {:style {:margin-top top
                                :margin-left left
                                :height "calc(100vh - 75px)"}}
     [:div.report__container
      [:div {:id dom-id}
       [header report-id dom-id]
       [modules-grid report-id dom-id]]]]))
