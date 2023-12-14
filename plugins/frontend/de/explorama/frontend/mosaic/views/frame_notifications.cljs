(ns de.explorama.frontend.mosaic.views.frame-notifications
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]
            [de.explorama.shared.mosaic.common-paths :as gcp]))

(defn- build-message [_frame-id {:keys [show-undo? not-supported-redo-ops clear-notification-fn undo-fn]}]
  (when (set? not-supported-redo-ops)
    (let [{:keys [redo-not-possible-single
                  redo-not-possible-multi
                  undo-button-label
                  contextmenu-top-level-groupby
                  contextmenu-top-level-sortby
                  contextmenu-top-level-groupby-1
                  contextmenu-top-level-sort-group
                  contextmenu-top-level-sub-sort-group
                  load-warning-screen-not-follow-recommendation
                  redo-not-possible-layouts
                  scatter-plot-settings-title
                  redo-no-data]}
          @(re-frame/subscribe [::i18n/translate-multi
                            :redo-not-possible-single
                            :redo-not-possible-multi
                            :redo-not-possible-layouts
                            :redo-no-data
                            :undo-button-label
                            :contextmenu-top-level-groupby
                            :contextmenu-top-level-sortby
                            :contextmenu-top-level-groupby-1
                            :contextmenu-top-level-sort-group
                            :contextmenu-top-level-sub-sort-group
                            :load-warning-screen-not-follow-recommendation
                            :scatter-plot-settings-title])]
      (cond-> {:message (str (if (= 1 (count not-supported-redo-ops))
                               redo-not-possible-single
                               redo-not-possible-multi)
                             (->> (map #(cond (= % gcp/grp-by-key) contextmenu-top-level-groupby
                                              (= % gcp/sort-key) contextmenu-top-level-sortby
                                              (= % gcp/sub-grp-by-key) contextmenu-top-level-groupby-1
                                              (= % gcp/sort-grp-key) contextmenu-top-level-sort-group
                                              (= % gcp/sort-sub-grp-key) contextmenu-top-level-sub-sort-group
                                              (= % gcp/layouts) redo-not-possible-layouts
                                              (= % :no-data) redo-no-data
                                              (#{gcp/scatter-x gcp/scatter-y} %) scatter-plot-settings-title
                                              :else (error "Not supported operation" %))
                                       (set (mapv (fn [{:keys [op]}] (cond-> op
                                                                       (vector? op)
                                                                       (first)))
                                                  not-supported-redo-ops)))
                                  (set)
                                  (sort)
                                  (clj-str/join ", ")))}

        show-undo?
        (assoc :actions [{:label load-warning-screen-not-follow-recommendation
                          :on-click clear-notification-fn}
                         {:label undo-button-label
                          :start-icon :back
                          :variant :secondary
                          :on-click undo-fn}])))))

(def frame-notifications-impl
  {:show? true
   :build-message build-message
   :undo-path-fn gp/undo-connection-update-event})
