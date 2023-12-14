(ns de.explorama.frontend.charts.charts.toolbar
  (:require [cuerdas.core :refer [format]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.charts.charts.settings :as settings]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]
            [re-frame.core :refer [dispatch subscribe]]))

(defn- operation-disabled? [frame-id]
  (or
   @(subscribe [::queue-util/loading? frame-id])
   (not @(subscribe [:de.explorama.frontend.charts.charts.legend/frame-datasource frame-id]))))

(def add-chart-elem {:icon :plus
                     :group ""
                     :label :chart-add-button
                     :disabled? (fn [frame-id]
                                  (or
                                   (operation-disabled? frame-id)
                                   (boolean (#{:pie :wordcloud}
                                             (:cid @(subscribe [::settings/chart-type frame-id 0]))))
                                   (not (:active? @(subscribe [::settings/add-chart-active? frame-id])))))
                     :visible? (fn [frame-id]
                                 (= 1 @(subscribe [::settings/num-of-charts frame-id])))
                     :on-click (fn [_ frame-id]
                                 (dispatch [::settings/add-chart frame-id]))})

(def remove-chart-elem {:icon :trash
                        :group ""
                        :label :chart-remove-button
                        :disabled? operation-disabled?
                        :visible? (fn [frame-id]
                                    (< 1 @(subscribe [::settings/num-of-charts frame-id])))
                        :on-click (fn [_ frame-id]
                                    (let [chart-remove-idx-chart @(subscribe [::i18n/translate :chart-remove-idx-chart])]
                                      {:items (mapv (fn [idx]
                                                      (let [{chart-icon path/chart-desc-icon-key}
                                                            @(subscribe [::settings/chart-type frame-id idx])]
                                                        {:label (format chart-remove-idx-chart
                                                                        {:num (inc idx)})
                                                         :icon chart-icon
                                                         :left-icon-params {:color :gray
                                                                            :brightness 4}
                                                         :on-click #(dispatch [::settings/remove-chart frame-id idx])}))
                                                    (range 0 @(subscribe [::settings/num-of-charts frame-id])))}))})

(def toolbar-impl
  {:on-duplicate-fn (fn [frame-id]
                      (dispatch [:de.explorama.frontend.charts.core/duplicate-chart-frame frame-id]))
   :on-toggle (fn [frame-id show?])
   :items (fn [frame-id]
            (let [read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                           {:frame-id frame-id})]
              (if read-only?
                []
                [add-chart-elem
                 remove-chart-elem
                 :divider
                 :filter])))})
