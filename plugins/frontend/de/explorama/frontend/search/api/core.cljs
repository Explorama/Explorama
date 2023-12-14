(ns de.explorama.frontend.search.api.core
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.search.data.acs :as acs]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.views.util :as sutil]
            [de.explorama.frontend.search.backend.traffic-lights :as traffic-lights-backend]
            [taoensso.timbre :refer [debug]]
            [de.explorama.shared.common.data.attributes :as attrs]))

(defn attr-name->attr-desc [db attr-name]
  (let [attrs-descs (keys (get-in db spath/attribute-types))]
    (some (fn [[attr-name- :as attr-desc]]
            (when (= attr-name- attr-name)
              attr-desc))
          attrs-descs)))

(defn- add-row-event [db frame-id directly-create-di? row-name value]
  (let [row-name (if (string? row-name)
                   (attr-name->attr-desc db row-name)
                   row-name)]

    [[:dispatch [:de.explorama.frontend.search.views.attribute-bar/add-search-row
                 frame-id
                 row-name
                 [:search :search-form frame-id row-name]]]
     (when value
       [:dispatch [::sutil/row-selection-init frame-id row-name value directly-create-di?]])]))

(re-frame/reg-event-fx
 ::add-search-row
 (fn [{db :db} [_ row-name value directly-create-di? frame-id]]
   (debug "Init-ui-with-value " {:row-name row-name
                                 :value value
                                 :frame-id frame-id})
   {:fx (-> (add-row-event db frame-id directly-create-di? row-name value)
            (conj [:dispatch [::traffic-lights-backend/request-traffic-lights frame-id true]]))}))

(re-frame/reg-event-fx
 ::add-search-rows
 (fn [{db :db} [_ rows directly-create-di? max-values-limit frame-id]]
   (debug "Init-ui-with-value " {:rows rows
                                 :frame-id frame-id})
   (let [max-values-limit (or max-values-limit
                              config/max-show-all)]
     {:fx (-> (vec (mapcat (fn [[row-name value]]
                             (let [row-vector? (vector? value)
                                   too-many? (when row-vector?
                                               (> (count value) max-values-limit))]
                               (if too-many?
                                 (conj (add-row-event db frame-id directly-create-di? row-name (vec (take max-values-limit value)))
                                       [:dispatch [:de.explorama.frontend.search.views.components.frame-notifications/too-many-options
                                                   frame-id row-name]])
                                 (add-row-event db frame-id directly-create-di? row-name value))))
                           rows))
              (conj [:dispatch [::traffic-lights-backend/request-traffic-lights frame-id true]]))})))

(re-frame/reg-event-fx
 ::open-with-row
 (fn [_ [_ row-name values create-di? max-values-limit]]
   {:dispatch [:de.explorama.frontend.search.core/search-open [::add-search-rows [[row-name values]] create-di? max-values-limit]]}))

(re-frame/reg-event-fx
 ::open-with-rows
 (fn [_ [_ rows create-di?]]
   {:dispatch [:de.explorama.frontend.search.core/search-open [::add-search-rows rows create-di? nil]]}))

(re-frame/reg-event-fx
 ::related-search
 (fn [{db :db} [_ clicked-on-menu datapoint]]
   (let [[attr access-func] (if (vector? clicked-on-menu)
                              clicked-on-menu
                              [clicked-on-menu
                               (fn [datapoint]
                                 (get datapoint clicked-on-menu))])
         row-name (acs/attribute-name->attr-desc db (cond (#{:date attrs/date-attr} attr)
                                                          "day"
                                                          (keyword? attr)
                                                          (name attr)
                                                          :else attr))
         attr-type (get-in db (spath/attribute-type row-name))
         row-value (access-func datapoint)
         row-vector? (vector? row-value)
         row-value (if (and row-vector?
                            (#{"string" "month" "day" "year"}
                             attr-type))
                     (->> row-value
                          (filter identity)
                          (mapv (fn [v]
                                  {:label v})))
                     row-value)
         create-di-directly? (or (and row-vector?
                                      (not-empty row-value))
                                 (and (not row-vector?)
                                      row-value))]
     {:dispatch [::open-with-row row-name row-value create-di-directly? config/max-related-options]})))

(re-frame/reg-event-fx
 ::save-undo-state
 (fn [{db :db} [_ frame-id]]
   (let [undo-step (fi/call-api :project-current-step-db-get db)]
     {:db (assoc-in db
                    (spath/undo-last-search-step frame-id)
                    undo-step)})))

(re-frame/reg-event-fx
 ::undo-to-last-search
 (fn [{db :db} [_ frame-id]]
   (when-let [undo-step (get-in db (spath/undo-last-search-step frame-id))]
     (debug "Undo to last search with step" undo-step)
     {:dispatch (fi/call-api :project-load-step-event-vec db undo-step)})))
