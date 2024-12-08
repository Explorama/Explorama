(ns de.explorama.frontend.charts.charts.pie
  (:require ["chart.js/auto$default" :as Chart]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.utils.interop :refer [safe-aget]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.config :as config]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]))

(defonce chart-id-prefix "vis_pie-")

(defn show-chart [frame-id props]
  (let [context (cutils/get-element-context chart-id-prefix frame-id)
        cur-lang (re-frame/subscribe [::i18n/current-language])
        {:keys [data height width render-done]} @props
        {:keys [labels datasets]} data
        chart-data (cutils/disable-legend-interaction
                    {:type "pie"
                     :data {:datasets datasets
                            :labels labels}
                     :animation false
                     :options {:responsive true
                               :animation {:duration 0}
                               :maintainAspectRatio false
                               :elements {:arc {:borderWidth 0}}
                               :tooltips {:callbacks
                                          {:title (fn [_])
                                           :label (fn [item data]
                                                    (let [dataset-index (safe-aget item "datasetIndex")
                                                          data-index (safe-aget item "index")
                                                          dataset (safe-aget data "datasets" dataset-index)
                                                          y-value (safe-aget dataset "data" data-index)
                                                          dataset-label (safe-aget dataset "label")]
                                                      (str dataset-label ": " (i18n/localized-number y-value @cur-lang))))}}
                               :legend {:position :top
                                        :labels {:fontSize 12
                                                 :boxWidth 10}}}})]
    (when chart-data
      (cutils/save-instance frame-id (Chart. context (clj->js chart-data)))
      (cutils/resize-chart frame-id height width))
    (when render-done
      (render-done frame-id))))

(defn chart-component [frame-id props]
  (reagent/create-class
   {:display-name chart-id-prefix
    :component-did-mount #(show-chart frame-id props)
    :should-component-update cutils/should-update?
    :component-did-update cutils/component-update
    :component-will-unmount cutils/component-unmount
    :component-did-catch cutils/component-did-catch
    :reagent-render (fn []
                      [:canvas {:id (str chart-id-prefix frame-id)}])}))

(defn render-done [frame-id]
  (re-frame/dispatch
   (fi/call-api :render-done-event-vec frame-id (str config/default-namespace " - pie chart"))))

(defn content [frame-id vis-desc]
  (reagent/create-class
   {:display-name frame-id
    :component-did-mount (fn []
                           (let [data @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                                 loading? @(re-frame/subscribe [::queue-util/loading? frame-id])]
                             (when (and vis-desc (not loading?) (empty? data))
                               (re-frame/dispatch [:de.explorama.frontend.charts.vis-state/restore-vis-desc frame-id vis-desc]))
                             (when (empty? data)
                               (render-done frame-id))))
    :reagent-render (fn [frame-id {:keys [size]}]
                      ;(cutils/resize-chart frame-id)
                     ; (cutils/resize-chart frame-id)
                      (let [[data] @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                            {[width height] :size} @(fi/call-api :frame-sub frame-id)
                            scale-info @(fi/call-api :scale-info-sub)
                            props (reagent/atom {:scale-info scale-info
                                                 :data data
                                                 :height (if size (second size) height)
                                                 :width (if size (first size) width)
                                                 :render-done render-done
                                                 :type path/pie-id-key})]
                        (when (not-empty data)
                          ;; [:div {:style {:width "100%"}}
                          [chart-component frame-id props])))}))

(def chart-desc {path/chart-desc-id-key path/pie-id-key
                 path/chart-desc-label-key :pie-chart-label
                 path/chart-desc-selector-class-key "chart__pie"
                 path/chart-desc-content-key content
                 path/chart-desc-multiple-key false
                 path/chart-desc-icon-key :charts-pie})
