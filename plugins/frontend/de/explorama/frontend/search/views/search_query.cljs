(ns de.explorama.frontend.search.views.search-query
  (:require [clojure.string :as string :refer [blank?]]
            [date-fns :as dfns]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.shared.search.ws-api :as ws-api]))

(re-frame/reg-event-fx
 ::load-query
 (fn [{db :db} [_ frame-id query-id query]]
   (let [datasources (get-in db spath/search-enabled-datasources)]
     {:db (-> db
              (assoc-in (spath/frame-search-rows frame-id) query)
              (assoc-in (spath/search-frame-changed? frame-id) true))
      :dispatch-n [[ws-api/search-query-update-usage query-id]
                   [::options-backend/request-options frame-id nil true]
                   [ws-api/search-options datasources frame-id (vec (keys query)) query]]})))

(re-frame/reg-sub
 ::queries
 (fn [db [_ filter]]
   (let [all-queries (vals (get-in db (spath/search-queries)))
         filter (string/lower-case filter)]
     (if (seq filter)
       (filterv
        #(string/includes? (string/lower-case (:title %)) filter)
        all-queries)
       all-queries))))

(re-frame/reg-sub
 ::is-new-title-valid?
 :<- [::queries ""]
 (fn [queries [_ new-title]]
   (let [new-title (-> new-title string/trim string/lower-case)
         not-empty? (and (not (blank? new-title))
                         (> (count new-title) 4))
         duplicate (some #(-> % :title string/lower-case (= new-title))
                         queries)]
     {:valid? (not duplicate)
      :not-empty? not-empty?
      :reason (cond
                duplicate :search-query-title-duplicate)})))

(re-frame/reg-event-db
 ::toggle-list-open
 [(fi/ui-interceptor)]
 (fn [db [_ frame-id]]
   (update-in db (spath/search-query-open? frame-id) not)))

(re-frame/reg-sub
 ::query-list-open?
 (fn [db [_ frame-id]]
   (get-in db (spath/search-query-open? frame-id) false)))

(defn now-date []
  (js/Date.now))

(defn- format-distance [date-type num _]
  {:unit (cond
           (and (= date-type "xMinutes")
                (= num 1))
           :=-1-minute
           (and (= date-type "xMinutes")
                (> num 1))
           :x-minutes
           (#{"xMinutes" "xSeconds"} date-type)
           :<-1-minute
           (and (= date-type "xHours")
                (= num 1))
           :=-1-hour
           (and (= date-type "xHours")
                (> num 1))
           :x-hours
           (and (= date-type "xDays")
                (= num 1))
           :=-1-day
           (and (= date-type "xDays")
                (> num 1))
           :x-days
           (and (= date-type "xMonths")
                (= num 1))
           :=-1-month
           :else
           :x-months)
   :num (cond-> num
          (= date-type "xYears")
          (* 12))})

(defn last-used->str [lang from to]
  (let [locale (aget js/dateFns_local (case lang
                                        :en-GB "enGB"
                                        :de-DE "de"
                                        "enGB"))]
    (aset locale "formatDistance" format-distance)
    (dfns/formatDistanceStrict
     (cond-> from
       (number? from)
       (js/Date.))
     (cond-> to
       (number? to)
       (js/Date.))
     #js{:locale locale})))
