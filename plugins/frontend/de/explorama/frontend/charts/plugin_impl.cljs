(ns de.explorama.frontend.charts.plugin-impl
  (:require [re-frame.core :as re-frame]
            [cuerdas.core :as cuerdas]
            [de.explorama.frontend.charts.charts.core :as cimpl]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.components.stop-screen :as stop-screen]
            [de.explorama.frontend.charts.components.filter :refer [filter-impl]]
            [de.explorama.frontend.charts.components.frame-header :refer [frame-header-impl loading-impl]]
            [de.explorama.frontend.charts.components.frame-notifications :refer [frame-notifications-impl]]
            [de.explorama.frontend.charts.charts.toolbar :as charts-toolbar]
            [de.explorama.frontend.charts.charts.legend :as c-legend]))

(def ^:private stop-screens
  {:stop-view-display {:title :load-stop-screen-title
                       :message-1 :load-stop-screen-message-part-1
                       :message-2 :load-stop-screen-message-part-2
                       :stop :load-stop-screen-follow-recommendation}

   :stop-view-too-much-data {:title :too-much-data-title
                             :message-1 :too-much-data-message-part-1-min-max
                             :message-2 :too-much-data-message-part-2
                             :stop :too-much-data-follow-recommendation}

   :stop-view-invalid-selection {:title :stop-view-invalid-selection-title
                                 :message-1 :stop-view-invalid-selection-message-part-1
                                 :message-2 :stop-view-invalid-selection-message-part-2
                                 :stop :stop-view-invalid-selection-recommendation}

   :stop-view-invalid-time-selection {:title :stop-view-invalid-time-selection-title
                                      :message-1 :stop-view-invalid-time-selection-message-part-1
                                      :message-2 :stop-view-invalid-time-selection-message-part-2
                                      :stop :stop-view-invalid-time-selection-recommendation}

   :stop-view-unknown {:title :stop-view-unknown-title
                       :message-1 :stop-view-unknown-part-1
                       :message-2 :stop-view-unknown-part-2
                       :stop :stop-view-unknown-recommendation}})

(defn- invalid-selection-details [db {:keys [selections]}]
  (reduce (fn [acc [lang-key attribute]]
            (str acc  "\n" (i18n/translate db lang-key) ":\t" attribute))
          ""
          selections))

(re-frame/reg-sub
 ::stopscreen-label
 (fn [db [_ frame-id k label]]
   (let [details (get-in db (path/stop-view-details frame-id))
         label (i18n/translate db (get-in stop-screens [k label]))]
     (cond-> label
       (= k :stop-view-too-much-data)
       (cuerdas/format {:data-count (i18n/localized-number (:data-count details))
                        :max-data-amount (i18n/localized-number (:max-data-amount details))})
       (= k :stop-view-invalid-selection)
       (cuerdas/format {:selection (invalid-selection-details db details)})
       (= k :stop-view-invalid-time-selection)
       (cuerdas/format {:selection (invalid-selection-details db details)})))))

(def stop-screen-impl
  {:show? (fn [frame-id]
            (re-frame/subscribe [::stop-screen/stop-view-display frame-id]))
   :title-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :title]))
   :message-1-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-1]))
   :message-2-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-2]))
   :stop-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :stop]))
   :ok-fn (fn [_ frame-id]
            (re-frame/dispatch [::stop-screen/stop-view-display frame-id false]))})

(def charts-desc
  {:component-did-mount (fn [frame-id])
   :clean-up-event-vec (fn [follow-event frame-ids]
                         [:de.explorama.frontend.charts.core/clean-up follow-event frame-ids])
   :toolbar charts-toolbar/toolbar-impl
   :loading? loading-impl
   :legend c-legend/legend-impl
   :burger {:show-button? (constantly true)}
   :frame-header frame-header-impl

   :frame-header-context-menu nil
   :loading-screen cimpl/loading-screen-impl
   :warn-screen cimpl/warn-screen-impl
   :stop-screen stop-screen-impl

   :product-tour cimpl/product-tour-impl

   :filter filter-impl
   :notifications frame-notifications-impl})
