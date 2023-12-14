(ns de.explorama.frontend.table.table.data
  "Holds data for each table frame. Only contains the current page data"
  (:require [clojure.string :refer [lower-case]]
            [reagent.core :as r]
            [de.explorama.frontend.table.config :as config]
            [de.explorama.shared.table.ws-api :as ws-api]
            [de.explorama.frontend.common.queue :as ddq]))

(defonce ^:private db (r/atom (. cljs.core/PersistentHashMap -EMPTY)))

(defn- frame-sub [frame-id]
  (r/cursor db [frame-id]))

;;For less delay
(defn frame-scroll-x [frame-id]
  (r/cursor db [frame-id :config ws-api/scroll-x-key]))

(defn frame-scroll-y [frame-id]
  (r/cursor db [frame-id :config ws-api/scroll-y-key]))

(defn frame-single-event-data
  "Returns the event-data at given index from available table page data"
  [frame-id index]
  (when-let [frame-data @(frame-sub frame-id)]
    (-> frame-data
        (get-in [:data index] {}))))

(defn remove-frame-data
  "Removes the data for table frame"
  [frame-id]
  (swap! db dissoc frame-id)
  nil)

(defn save-frame-data
  "Saves the data for table frame"
  [frame-id data]
  (swap! db assoc-in [frame-id :data] data)
  nil)

(defn set-frame-table-config
  "Set some configuration for the table like page-size"
  [frame-id config value]
  (swap! db assoc-in [frame-id :config config] value)
  nil)

(defn merge-frame-table-config
  "merge some configuration with the existing configs for the table"
  [frame-id configs]
  (swap! db update-in [frame-id :config] merge configs)
  nil)

(defn init-frame-table-config [frame-id configs]
  (swap! db assoc-in [frame-id :config] configs)
  nil)

(defn frame-table-config
  "Returns all configurations for table"
  [frame-id]
  (-> @(frame-sub frame-id)
      (get :config {})))

(defn frame-table-single-config
  "Returns a specific configuration for table"
  ([frame-id config default-value]
   (-> @(frame-sub frame-id)
       (get-in [:config config] default-value)))
  ([frame-id config]
   (frame-table-single-config frame-id config nil)))

(defn- columns
  [frame-id data-attributes]
  (let [freeze-attributes (vec (frame-table-single-config frame-id ws-api/freeze-key config/default-freeze-attributes))
        freeze-check? (set freeze-attributes)]
    (->> (filter #(not (config/ignore-attributes %))
                 data-attributes)
         (sort-by #(do
                     (cond
                       (freeze-check? %)
                       (str (.indexOf freeze-attributes %))
                       (= % "annotation")
                       (str (count freeze-attributes))
                       :else
                       (lower-case %))))
         (vec))))

(defn frame-columns
  "Returns the columns for table frame"
  [frame-id]
  (-> @(frame-sub frame-id)
      (get :columns [])))

(defn save-frame-columns
  "Saves the columns for table frame"
  [frame-id data-attributes]
  (swap! db assoc-in [frame-id :columns] (columns frame-id data-attributes))
  nil)

(defn request-data-event-vec
  ([frame-id request-params]
   [::ddq/queue frame-id
    [ws-api/table-data
     frame-id
     (merge (frame-table-config frame-id)
            request-params)]])
  ([frame-id]
   (request-data-event-vec frame-id nil)))

(defn- apply-request-defaults [request-params]
  (merge {ws-api/current-page-key 1
          ws-api/page-size-key config/default-page-size
          ws-api/sorting-key config/default-sorting}
         request-params))

(defn build-request-params [frame-id request-params]
  (-> (apply-request-defaults request-params)
      (select-keys ws-api/requesting-keys)
      (assoc :frame-id frame-id)))

(defn frame-request-params [frame-id]
  (build-request-params frame-id (frame-table-config frame-id)))

(defn logging-params [params]
  (-> (apply-request-defaults params)
      (select-keys ws-api/logging-keys)))

(defn copy-params [frame-id]
  (-> (frame-table-config frame-id)
      (logging-params)
      (dissoc ws-api/focus-event-id-key)))

(defn logging-event-vec [frame-id]
  [:de.explorama.frontend.table.event-logging/log-event
   frame-id
   "update-table"
   (logging-params (frame-table-config frame-id))])