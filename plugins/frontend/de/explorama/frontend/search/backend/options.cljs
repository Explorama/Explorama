(ns de.explorama.frontend.search.backend.options
  (:require [de.explorama.shared.search.ws-api :as ws-api]
            [de.explorama.frontend.search.path :as path]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.search.backend.traffic-lights :as traffic-lights-backend]
            [de.explorama.frontend.search.backend.util :refer [build-options-request-params]]
            [re-frame.core :refer [reg-event-fx
                                   dispatch]]))

(reg-event-fx
 ws-api/search-options
 (fn [_ [_ datasources frame-id row-attrs formdata callback-event]]
   {:backend-tube [ws-api/search-options
                   {:client-callback [ws-api/search-options-result]
                    :failed-callback [ws-api/failed-handler :search-options]}
                   datasources frame-id row-attrs formdata callback-event]}))

(reg-event-fx
 ws-api/search-options-result
 (fn [_ [_ frame-id result-options callback-event]]
   {:fx [[:dispatch [::update-search-options frame-id result-options]]
         (when callback-event
           [:dispatch callback-event])]}))

;:value :values :from :to :selected-date :start-date :end-date
(defn get-relevant-values [row]
  (let [values (get row :values)
        value (get-in row [:value :value] (get row :value))
        from (get-in row [:from :value])
        to (get-in row [:to :value])
        range (and
               from
               (mapv str (filterv (fn [o]
                                    (and (>= o from)
                                         (<= o to)))
                                  (get row :options))))
        start-date (get row :start-date)
        end-date (get row :end-date)
        selected-date (get row :selected-date)
        date-range (and
                    start-date
                    (filterv (fn [o]
                               (and (>= o start-date)
                                    (<= o end-date)))
                             (get row :options)))]
    (or values
        (and value [(str value)])
        range
        (and selected-date [selected-date])
        date-range)))

(reg-event-fx
 ::update-search-options
 (fn [{db :db} [_ frame-id attr-values]]
   {:db (reduce
         (fn [ndb [attr-desc values]]
           (let [row-in-db? (get-in db (path/search-row-data frame-id attr-desc))]
             (if (and row-in-db?
                      (not-empty values))
               (assoc-in ndb
                         (path/search-row-options frame-id attr-desc)
                         values)
               ndb)))
         (update-in db path/event-callback dissoc frame-id)
         attr-values)
    :dispatch-n [(get-in db (path/frame-event-callback frame-id))]}))

(reg-event-fx
 ::request-options
 (fn [{db :db} [_ frame-id base-attr request-attributes?]]
   (let [compl-formdata (vec (sort-by #(get-in % [1 :timestamp])
                                      (get-in db (path/frame-search-rows frame-id))))
         last-row (peek compl-formdata)
         [attr-desc] last-row
         is-last? (= attr-desc base-attr)
         is-valid? (validation/is-row-valid? db frame-id last-row)
         any-not-valid (some (fn [row]
                               (not (validation/is-row-valid? db frame-id row)))
                             compl-formdata)
         row-attrs (mapv first compl-formdata)
         {attr-formdata :formdata} (when request-attributes?
                                     (build-options-request-params db frame-id nil compl-formdata false))
         base-attr-request-params (build-options-request-params db frame-id base-attr compl-formdata false)
         {:keys [formdata attrs]} (when (or (not is-last?) is-valid?)
                                    base-attr-request-params)
         datasources (get-in db path/search-enabled-datasources)]
     (merge {:timeout {:id    [::traffic-lights-backend/request-traffic-lights frame-id]
                       :time  1000
                       :event [::traffic-lights-backend/request-traffic-lights frame-id]}}
            (cond
              (and is-last? (not is-valid?) (seq compl-formdata))
              (if (and request-attributes? (not any-not-valid))
                {:dispatch [ws-api/request-attributes datasources frame-id row-attrs (:formdata base-attr-request-params)]}
                {})
              (and request-attributes? (not any-not-valid))
              {:dispatch-n [[ws-api/request-attributes datasources frame-id row-attrs attr-formdata]
                            (when (and attrs formdata)
                              [ws-api/search-options datasources frame-id attrs formdata])]}
              (and attrs formdata)
              {:dispatch [ws-api/search-options datasources frame-id attrs formdata]}
              (and (nil? attrs) (seq formdata) (not any-not-valid))
              {:dispatch [ws-api/request-attributes datasources frame-id row-attrs formdata]})))))

(defonce ^:private timeout-requests (atom {}))

(defn clear-timeout-request [frame-id]
  (when-let [old-timeout (get @timeout-requests frame-id)]
    (js/clearTimeout old-timeout)
    (swap! timeout-requests dissoc frame-id)))

(defn start-timeout-request [frame-id base-attr-desc]
  (clear-timeout-request frame-id)
  (swap! timeout-requests
         assoc frame-id
         (js/setTimeout (fn []
                          (swap! timeout-requests dissoc frame-id)
                          (dispatch [::request-options frame-id base-attr-desc true]))
                        config/search-change-request-delay)))