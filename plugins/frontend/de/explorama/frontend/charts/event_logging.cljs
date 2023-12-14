(ns de.explorama.frontend.charts.event-logging
  (:require [clojure.string :as string]
            [de.explorama.frontend.common.event-logging.util :as log-util :refer [base-desc]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.charts.config :as config]
            [de.explorama.frontend.charts.event-replay :as e-replay]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug error]]))

(def event-version 1.0)

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ [_ resp]]
    ;; (debug (str "Event-logging success" resp))
   {}))


(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ [_ resp]]
   (error (str "Event-logging failed" resp))
   {}))

(re-frame/reg-event-fx
 ::log-event
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event-name description]]
   (when-let [log-fn (fi/call-api :service-target-db-get db :project-fns :event-log)]
     (log-fn db
             config/default-vertical-str
             frame-id
             event-name
             description
             event-version))))

(re-frame/reg-event-fx
 ::log-pseudo-init
 (fn [{db :db} _]
   (let [workspace-id (fi/call-api :workspace-id-db-get db)
         pseudo-frame-id {:workspace-id workspace-id
                          :vertical "charts"}]
     {:dispatch [::log-event pseudo-frame-id "init-vis" nil]})))

(re-frame/reg-event-fx
 ::ui-wrapper
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event-name event-params]]
   (if-let [event-func (e-replay/event-func event-name)]
     (merge-with into
                 (event-func db frame-id nil event-params)
                 {:dispatch-n [[::log-event frame-id event-name event-params]]})
     (do
       (debug "no event-function found for " [event-name event-version])
       nil))))

(defn event->settings [{{sum-by :value} :sum-by
                        sum-values :sum-values
                        {x :label} :x-option
                        {y :label} :y-option
                        {r :label} :r-option
                        {attr :label} :attr-option}
                       db]
  (string/join "\n"
               (cond-> []
                 (and (not= sum-by "all")
                      (not-empty sum-values)) (conj
                                               (str (i18n/translate db :sum-by-label) ": " sum-by
                                                    "\n"
                                                    (i18n/translate db :sum-by-vals-label)
                                                    ": " (string/join ", "
                                                                      (map :label sum-values))))
                 x (conj (str "x: " x))
                 y (conj (str "y: " y))
                 r (conj (str "r: " r))
                 attr (conj (str (i18n/translate db :chart-attr-label) ": " attr)))))

(defn chart-selection->step [event-params]
  (base-desc :charts-protocol-action-update-chart
             (into [:charts-protocol-action-chart-config
                    (get-in event-params [:selection :chart-desc :x-option :label])
                    "\n"]
                   (->> (map-indexed (fn [idx {{label-type :label} :type-desc
                                               {label-agg :label} :aggregate-method
                                               {label-y :label} :y-option}]
                                       [:charts-protocol-action-chart-axis-config
                                        "(" label-type "):"
                                        (inc idx)
                                        "\n"
                                        :charts-protocol-action-chart-config-y
                                        label-y
                                        "\n"
                                        :charts-protocol-action-chart-config-aggregated
                                        label-agg])
                                     (get-in event-params [:selection :chart-desc :charts]))
                        (interpose ["\n" "\n"])
                        (mapcat identity)))))

(defn- action-desc [base-op action both only-old only-new]
  (let [event-params (merge both only-new)]
    (cond (= action "update-chart") (chart-selection->step event-params)
          :else nil)))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))