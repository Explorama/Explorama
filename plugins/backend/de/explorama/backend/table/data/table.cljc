(ns de.explorama.backend.table.data.table
  (:require [clojure.string :as string]
            [de.explorama.shared.data-format.filter]
            [de.explorama.backend.table.data.fetch :refer [di-data]]
            [de.explorama.shared.table.ws-api :as ws-api]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.misc :refer [cljc-max-int]]))

(defn- element-value [key element]
  (as-> (attrs/value element key) $
    (if (vector? $)
      (cond
        (string? (first $))
        (string/join "" $)
        (= key :month)
        (-> element #(attrs/value % "date") first (subs 5))
        (= key :year)
        (-> element #(attrs/value % "date") first (subs 0 4))
        :else (first $))
      (cond (= key :month)
            (-> element #(attrs/value % "date") (subs 5))
            (= key :year)
            (-> element #(attrs/value % "date") (subs 0 4))
            (= key "annotation")
            (get $ "content" "")
            :else $))))

(defn- sortby-key [raw-data key direction]
  (vec (if (#{:desc "desc"} direction)
         (sort-by (partial element-value key)
                  #(compare %2 %1)
                  raw-data)
         (sort-by
          (partial element-value key)
          raw-data))))

(defn- includes-case-insensitive?
  [s substr]
  (string/includes? (string/lower-case s) (string/lower-case substr)))

(defn filter-search-data [search-details raw-data]
  (filterv (fn [dataset]
             (every? (fn [{:keys [field value]}]
                       (let [svalue (str value)
                             field (attrs/access-key field)]
                         (if (= "pseudo-column-any" field)
                           (some #(includes-case-insensitive? % svalue)
                                 (vals dataset))
                           (includes-case-insensitive?
                            (attrs/value dataset field "") svalue))))
                     search-details))
           raw-data))

(defn- search-pure [raw-data search]
  (filter-search-data (map second search) raw-data))

(defn- sort-single [data sort-val]
  (sortby-key data
              (-> sort-val :attr attrs/access-key)
              (:direction sort-val)))

(defn sort-pure [raw-data sort-options]
  (reduce sort-single
          raw-data
          (reverse sort-options)))

(defn- get-row-index
  "Returns the zero based index of the row with ID `row-id` from `raw-data`.
   If there is no such row, nil is returned."
  [event-id raw-data]
  (first
   (keep-indexed (fn [index {id (attrs/access-key "id")}]
                   (when (= event-id id) index))
                 raw-data)))

(defn- get-page-index
  "Calculate the 1-based index of the page of size `size` in `raw-data` of the
   entry with ID `focus-event-id`.
   If there is no such entry, :page will be nil."
  [raw-data page-size focus-event-id]
  (when-let [index (get-row-index focus-event-id raw-data)]
    (let [new-page (inc (quot index page-size))]
      {:new-page (max 1 new-page)
       :focus-row-idx (-
                       index
                       (* (dec new-page)
                          page-size))})))

(defn- post-process [raw-data data-count page page-size focus-row-idx]
  (let [offset (* (dec page) page-size)
        end-range (min data-count (+ offset page-size))
        selected-data (if (= 0 (count raw-data))
                        raw-data
                        (subvec raw-data offset end-range))

        mdata {ws-api/focus-row-idx-key focus-row-idx
               ws-api/current-page-key page
               ws-api/last-page-key (int (Math/ceil (/ data-count page-size)))
               ws-api/row-count-key (count selected-data)
               ws-api/data-range-key [offset end-range]
               ws-api/data-key selected-data}]
    mdata))

(defn- build-result [{:keys [data filtered-count] :as data-infos}
                     {:keys [di task-id]
                      page-size ws-api/page-size-key
                      page ws-api/current-page-key
                      focus-event-id ws-api/focus-event-id-key
                      :or {page-size cljc-max-int
                           page 1}}]
  (let [{:keys [new-page focus-row-idx]}
        (or (when focus-event-id
              (get-page-index data page-size focus-event-id))
            {:new-page page})]
    (-> data-infos
        (merge (post-process data filtered-count new-page page-size focus-row-idx))
        (assoc :di di
               :task-id task-id))))

(defn table-data [{sorting ws-api/sorting-key
                   :as params}]
  (cond-> (di-data params)
    ;;  search (components/search-pure search)
    sorting (update :data #(sort-pure % sorting))
    :always (build-result params)))