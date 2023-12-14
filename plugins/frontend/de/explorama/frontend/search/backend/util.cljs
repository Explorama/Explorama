(ns de.explorama.frontend.search.backend.util
  (:require [de.explorama.frontend.search.path :as path]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.frontend.search.data.topics :refer [is-topic-attr-desc? formdata-topics->datasource]]))

(def necessary-keys [:all-values? :empty-values? :value :values :from :to :selected-date :start-date :end-date :last-x :cond :advanced :timestamp :topic-selection?])

(def event-log-keys [:all-values? :empty-values? :value :values :from :to :selected-date :start-date :end-date :cond :last-x :ui-selection :active? :advanced :timestamp :topic-selection?])

(defn filter-unnecessary-rowdata [db frame-id attr-desc d keep-keys]
  (let [{:keys [values] :as filtered-map} (select-keys d keep-keys)
        valid? (validation/is-row-valid? db frame-id [attr-desc d])]
    (when (and (not-empty (dissoc filtered-map :timestamp))
               (or (not values)
                   (and values (not-empty values))))
      (assoc
       filtered-map
       :valid? valid?))))

(defn overwrite-with-search-row [db frame-id d attr]
  (let [overwrite-d (merge d (get-in db (path/search-row-data frame-id attr)))]
    (filter-unnecessary-rowdata db frame-id attr overwrite-d necessary-keys)))

(defn build-formdata [db frame-id fd d filtered-rowdata attr-desc filter-inactive?]
  (let [is-active? (get d :active?)
        is-filter-row? (boolean (get-in db (path/search-row-data frame-id attr-desc)))]
    (cond
      (and filtered-rowdata
           (or is-active? (is-topic-attr-desc? attr-desc)))
      (assoc fd attr-desc filtered-rowdata)
      (and is-filter-row? filtered-rowdata (not filter-inactive?))
      (let [overwrite (overwrite-with-search-row db frame-id d attr-desc)]
        (if overwrite
          (assoc fd attr-desc overwrite)
          fd))
      :else fd)))

(defn build-options-request-params
  ([db frame-id base-attr formdata filter-inactive?]
   (build-options-request-params db frame-id base-attr formdata necessary-keys {:filter-inactive? filter-inactive?
                                                                                :translate-topics? true}))
  ([db frame-id base-attr formdata necessary-keys {:keys [filter-inactive? translate-topics?]}]
   (let [formdata (vec (sort-by #(get-in % [1 :timestamp])
                                formdata))
         formdata (cond-> formdata
                    translate-topics?
                    (formdata-topics->datasource))
         [relevant-formdata req-attributes _]
         (reduce (fn [[fd attrs trigger] [attr d]]
                   (let [filtered-rowdata (filter-unnecessary-rowdata db frame-id attr d necessary-keys)
                         nfd (build-formdata db frame-id fd d filtered-rowdata attr filter-inactive?)]
                     (cond
                       (and (not trigger)
                            (= attr base-attr))
                       [nfd attrs true]
                       (and trigger filter-inactive?) [nfd attrs true]
                       trigger [nfd (conj attrs attr) true]
                       (not-empty d)
                       [nfd attrs trigger]
                       :else [fd attrs trigger])))
                 [{} [] (nil? base-attr)]
                 formdata)]
     {:attrs (not-empty req-attributes)
      :formdata (-> (sort-by #(get-in % [1 :timestamp])
                             (if (and (nil? base-attr) (not filter-inactive?))
                               (let [f
                                     (reduce (fn [fd [attr d]]
                                               (if d
                                                 (let [nd (filter-unnecessary-rowdata db frame-id attr d necessary-keys)]
                                                   (if (and nd (not-empty nd))
                                                     (assoc fd attr nd)
                                                     fd))
                                                 fd))
                                             {}
                                             formdata)]
                                 f)
                               relevant-formdata))
                    (vec))})))