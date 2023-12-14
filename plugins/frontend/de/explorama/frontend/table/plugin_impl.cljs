(ns de.explorama.frontend.table.plugin-impl
  (:require [re-frame.core :as re-frame]
            [cuerdas.core :as cuerdas]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.components.stop-screen :as stop-screen]
            [de.explorama.frontend.table.components.filter :refer [filter-impl]]
            [de.explorama.frontend.table.components.frame-header :refer [frame-header-impl loading-impl]]
            [de.explorama.frontend.table.table.toolbar :as table-toolbar]
            [de.explorama.frontend.table.table.legend :as t-legend]
            [de.explorama.frontend.table.table.core :as timpl]))

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

(def table-desc
  {:component-did-mount (fn [_] nil)
   :clean-up-event-vec (fn [follow-event frame-ids]
                         [:de.explorama.frontend.table.core/clean-up follow-event frame-ids])
   :toolbar table-toolbar/toolbar-impl
  ;;  :optimize-frame-performance true
   :loading? loading-impl
   :legend t-legend/legend-impl
   :burger {:show-button? (constantly true)}
   :frame-header frame-header-impl

   :frame-header-context-menu nil
   :loading-screen timpl/loading-screen-impl
   :warn-screen timpl/warn-screen-impl
   :stop-screen stop-screen-impl

   :product-tour timpl/product-tour-impl

   :filter filter-impl
   :notifications nil})
