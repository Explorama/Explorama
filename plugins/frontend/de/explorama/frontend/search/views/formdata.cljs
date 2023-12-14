(ns de.explorama.frontend.search.views.formdata
  (:require [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :refer [reg-event-db reg-event-fx reg-sub]]
            [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.common.unification.time :as t]
            [de.explorama.shared.search.options-utils :as outils]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.views.util :as sutil]
            [taoensso.tufte :as tufte]))

(defn attribute-options [db path {:keys [transform? is-int? default-transform-val default-val]
                                  :or {default-transform-val []
                                       default-val {}}}]
  (if transform?
    (sutil/vec->reactselect-options (get-in db (spath/options path) default-transform-val)
                                    is-int?
                                    default-transform-val)
    (get-in db (spath/options path) default-val)))

(defn datasource-options [db frame-id]
  (let [bucket-datasources (get-in db spath/search-bucket-datasources)
        attr-desc [attrs/datasource-attr attrs/datasource-node]
        options (get-in db (spath/search-row-options frame-id attr-desc))
        grouped-options (reduce (fn [acc [bucket datasources]]
                                  (let [check-fn (set datasources)
                                        filtervals (filterv check-fn options)]
                                    (cond-> acc
                                      (seq filtervals)
                                      (assoc-in [bucket :options]
                                                (sutil/vec->reactselect-options
                                                 filtervals
                                                 false)))))

                                (cond-> {}
                                  (seq (:default bucket-datasources))
                                  (assoc :default {:options []
                                                   :label (i18n/translate db :datasources)
                                                   :value :default})

                                  (seq (:temp bucket-datasources))
                                  (assoc :temp {:options []
                                                :label (i18n/translate db :temp-datasources)
                                                :value :temp}))
                                bucket-datasources)]
    (mapv (fn [[_ group-desc]]
            group-desc)
          grouped-options)))

(reg-sub
 ::from
 (fn [db [_ path]]
   (get-in db (spath/from path))))

(reg-sub
 ::to
 (fn [db [_ path]]
   (get-in db (spath/to path))))

(reg-sub
 ::value
 (fn [db [_ path]]
   (get-in db (spath/value path))))

(reg-sub
 ::values
 (fn [db [_ path]]
   (get-in db (spath/values path))))

(reg-sub
 ::adv-mode
 (fn [db [_ path]]
   (get-in db (spath/advanced path) false)))

(reg-sub
 ::topic-selection?
 (fn [db [_ path]]
   (get-in db (conj path :topic-selection?) false)))

(reg-sub
 ::all-values?
 (fn [db [_ path]]
   (get-in db (spath/all-values? path) false)))

(reg-sub
 ::empty-values?
 (fn [db [_ path]]
   (get-in db (spath/empty-values? path) false)))

(defn can-define-values? [db attribute-path]
  (and (not (get-in db (spath/all-values? attribute-path) false))
       (not (get-in db (spath/empty-values? attribute-path) false))))

(reg-sub
 ::can-define-values?
 (fn [db [_ path]]
   (can-define-values? db path)))

(reg-sub
 ::condition
 (fn [db [_ path default-val]]
   (let [condition (get-in db (spath/condition path) default-val)]
     (if (string? condition)
       (sutil/val->select-option condition false)
       condition))))

(reg-sub
 ::search-attribute-options
 (fn [db [_ frame-id attr params]]
   (attribute-options
    db
    (spath/search-row-data frame-id attr)
    params)))

(reg-sub
 ::datasource-options
 (fn [db [_ frame-id]]
   (datasource-options db frame-id)))

(reg-sub
 ::options-from
 (fn [db [_ path]]
   (let [to (get-in db (spath/to path))
         options (get-in db (spath/options path) [])
         options (sutil/range-options to options <=)]
     (sutil/vec->reactselect-options options true))))

(reg-sub
 ::options-to
 (fn [db [_ path]]
   (let [from (get-in db (spath/from path))
         options (get-in db (spath/options path) [])
         options (sutil/range-options from options >=)]
     (sutil/vec->reactselect-options options true))))

(reg-sub
 ::selected-date
 (fn [db [_ path]]
   (get-in db (spath/selected-date path))))

(reg-sub
 ::last-x-value
 (fn [db [_ path]]
   (get-in db (spath/last-x-value path))))

(defn least-by
  "Return the least non nil element from `xs` as defined by `f`"
  [f xs]
  (when (seq xs)
    (reduce #(if (f %1 %2) %1 %2) xs)))

(defn least-date-by [f xs]
  (->> xs
       (map (partial t/date-str->obj false :day))
       (filter some?)
       (least-by f)))

(reg-sub
 ::min-date
 (fn [db [_ path]]
   (let [start-date (outils/normalize (get-in db (spath/start-date path)) :label)
         selected-date (get-in db (spath/selected-date path))
         min-date (first (get-in db (spath/options path)))]
     (-> (least-date-by t/before? [min-date start-date selected-date])
         (t/to-date)))))

(reg-sub
 ::max-date
 (fn [db [_ path]]
   (let [end-date (outils/normalize (get-in db (spath/end-date path)) :label)
         selected-date (get-in db (spath/selected-date path))
         max-date (last (get-in db (spath/options path)))]
     (-> (least-date-by t/after? [max-date end-date selected-date])
         (t/to-date)))))

(reg-sub
 ::date-range
 (fn [db [_ path]]
   (let [start-date (outils/normalize (get-in db (spath/start-date path))
                                      :label)
         end-date (outils/normalize (get-in db (spath/end-date path))
                                    :label)]
     [start-date end-date])))

(reg-sub
 ::ui-selection
 (fn [db [_ path]]
   (get-in db (spath/ui-selection path) [])))

(reg-sub
 ::day-ops
 (fn [db [_ path]]
   (tufte/profile
    {:when config/timelog-day-calc?}
    (let [d-vals (get-in db (spath/options path))]
      (if (and d-vals (not-empty d-vals))
      ;performance improvement for disabled Dates calc in Datepicker 
        (tufte/p ::day-ops-calc
                 (group-by (fn [d]
                             (t/get-month-year d))
                           (mapv (partial t/date-str->obj false :day)
                                 d-vals)))
        {})))))

(reg-sub
 ::month-ops
 (fn [db [_ path]]
   (if-let [m-vals (not-empty (get-in db (spath/options path)))]
     (sutil/month-options m-vals (i18n/current-language db))
     {})))

(reg-event-fx
 ::add-data-for-attr
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ path key value]]
   (let [newdb (if (nil? value)
                 (update-in db path dissoc key)
                 (assoc-in db (conj path key) value))]
     {:db newdb})))

(reg-event-fx
 ::delete-data-for-attr
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ path key]]
   {:db (update-in db path dissoc key)}))

(def dissoc-keys [:all-values? :empty-values? :ui-selection :from :to :advanced :cond :value :values :selected-date :start-date :end-date])

(reg-event-fx
 ::reset-values-from-attr
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ path]]
   (let [frame-id (spath/frame-id path)
         attr (spath/attr path)
         formdata-attr (get-in db path)]
     {:db         (assoc-in db path (apply dissoc
                                           formdata-attr
                                           dissoc-keys))
      :dispatch-n [[::options-backend/request-options frame-id attr true]]})))

(reg-sub
 ::formdata
 (fn [db [_ frame-id]]
   (get-in db (spath/frame-search-rows frame-id) {})))

(reg-event-db
 ::formdata
 [(fi/ui-interceptor)]
 (fn [db [_ frame-id formdata]]
   (cond-> db
     (and frame-id (map? formdata))
     (assoc-in (spath/frame-search-rows frame-id) formdata))))

(reg-sub
 ::search-changed?
 (fn [db [_ frame-id]]
   (get-in db (spath/search-frame-changed? frame-id) false)))

(reg-event-db
 ::search-changed
 (fn [db [_ frame-id search-changed?]]
   (assoc-in db (spath/search-frame-changed? frame-id) search-changed?)))

(reg-sub
 ::search-button-is-clicked?
 (fn [db [_ frame-id]]
   (get-in db [:search :is-clicked? frame-id] false)))

(reg-event-db
 ::search-button-is-clicked
 (fn [db [_ frame-id is-clicked?]]
   (-> db
       (assoc-in [:search :is-clicked? frame-id] is-clicked?))))

(reg-event-fx
 ::row-changed-wrapper
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ path has-changed? next-event]]
   {:db         (-> db
                    (assoc-in (conj path :has-changed?) has-changed?)
                    (assoc-in [:search :search-changed? (spath/frame-id path)] true))
    :dispatch-n [next-event]}))