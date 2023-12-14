(ns de.explorama.frontend.charts.components.frame-notifications
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.charts.path :as path]
            [re-frame.core :as re-frame]))

(defn- build-message [frame-id {:keys [show-undo? not-supported-redo-ops clear-notification-fn undo-fn]}]
  (when (coll? not-supported-redo-ops)
    (let [load-warning-screen-not-follow-recommendation @(re-frame/subscribe [::i18n/translate :load-warning-screen-not-follow-recommendation])
          redo-not-possible-single @(re-frame/subscribe [::i18n/translate :redo-not-possible-single])
          redo-not-possible-multi @(re-frame/subscribe [::i18n/translate :redo-not-possible-multi])
          undo-button-label @(re-frame/subscribe [::i18n/translate :undo-button-label])
          y-axis-attribute-label @(re-frame/subscribe [::i18n/translate :y-axis-attribute-label])
          x-axis-attribute-label @(re-frame/subscribe [::i18n/translate :x-axis-attribute-label])
          r-attribute-label @(re-frame/subscribe [::i18n/translate :r-attribute-label])
          chart-attr-label @(re-frame/subscribe [::i18n/translate :chart-attr-label])
          sum-by-label @(re-frame/subscribe [::i18n/translate :sum-by-label])
          sum-by-vals-label @(re-frame/subscribe [::i18n/translate :sum-by-vals-label])]

      (cond-> {:message   (str (if (= 1 (count not-supported-redo-ops))
                                 redo-not-possible-single
                                 redo-not-possible-multi)
                               (->> (map #(cond (= % :y-option) y-axis-attribute-label
                                                (= % :x-option) x-axis-attribute-label
                                                (= % :r-option) r-attribute-label
                                                (= % :attr-option) chart-attr-label
                                                (= % :sum-by-option) sum-by-label
                                                (= % :sum-by-values) sum-by-vals-label)
                                         (set (mapv :op not-supported-redo-ops)))
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
   :undo-path-fn (fn [frame-id]
                   (path/undo-connection-update-event (path/chart-frame frame-id)))})