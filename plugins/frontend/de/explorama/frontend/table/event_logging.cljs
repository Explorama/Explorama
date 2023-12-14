(ns de.explorama.frontend.table.event-logging
  (:require [clojure.string :as string]
            [de.explorama.frontend.common.event-logging.util :as log-util :refer [base-desc]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.table.config :as config]
            [de.explorama.frontend.table.event-replay :as e-replay]
            [de.explorama.shared.table.ws-api :as ws-api]
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
                          :vertical "table"}]
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

(defmulti table-action-settings-string (fn [action _] action))

(defmethod table-action-settings-string :current-page [_ current-page]
  [:table-protocol-action-current-page
   ": "
   (str current-page)
   "\n"])

(defmethod table-action-settings-string :page-size [_ page-size]
  [:table-protocol-action-page-size
   ": "
   (str page-size)
   "\n"])

(defmethod table-action-settings-string :sorting [_ sorting]
  (into [:table-protocol-action-sort-data
         ":\n"]
        (mapcat (fn [{:keys [attr direction]}]
                  (let [dir-key (if (= direction "asc") :table-sort-asc :table-sort-desc)]
                    [[:label attr]
                     " - "
                     dir-key]))
                sorting)))

(defn- table-action-settings [event-params]
  (let [{current-page ws-api/current-page-key
         page-size ws-api/page-size-key
         sorting ws-api/sorting-key} event-params]
    (cond-> []
      current-page (into (table-action-settings-string :current-page current-page))
      page-size (into (table-action-settings-string :page-size page-size))
      (seq sorting) (into (table-action-settings-string :sorting sorting)))))

(defn- action-desc [base-op action both only-old only-new]
  (let [event-params (merge both only-new)]
    (cond (= action "update-table") (base-desc :update-table-protocol-label
                                               (table-action-settings event-params))
          :else nil)))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))