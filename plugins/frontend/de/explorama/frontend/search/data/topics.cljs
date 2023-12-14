(ns de.explorama.frontend.search.data.topics
  "Support topics for searching"
  (:require [de.explorama.shared.common.data.attributes :as attrs]
            [re-frame.core :refer [reg-sub reg-event-fx]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.path :as path]))

(def topic-attr-desc [attrs/datasource-attr attrs/datasource-node])

(defn is-topic-attr-desc? [attr-desc]
  (= topic-attr-desc attr-desc))

(defn- get-with-fallback [m k]
  (if (< 1 (count m))
    (get m k)
    (first (vals m))))

(defn- translate-topic [lang {:keys [id title desc datasources]}]
  (let [title (get-with-fallback title lang)
        desc (get-with-fallback desc lang)
        tooltip (str title "\n" desc)]
    {:label title
     :tooltip tooltip
     :datasources datasources
     :value id}))

(defn- get-topics-from-config [db]
  (let [lang (i18n/current-language db)]
    (->> (fi/call-api [:config :get-config-db-get]
                      db
                      :topics)
         (vals)
         (mapv (fn [{:keys [datasources] :as topic-desc}]
                 (cond-> (translate-topic lang topic-desc)
                   (vector? (first datasources))
                   (update :datasources #(mapv second %))))))))

(defn get-topics-from-datasource [db datasource]
  (let [topics (get-topics-from-config db)]
    (reduce (fn [acc {:keys [datasources] :as topic-desc}]
              (cond-> acc
                ((set datasources) datasource)
                (conj topic-desc)))
            #{}
            topics)))

(defn get-topics [db available-datasources]
  (let [;for hierarchical filtering respect only available datasources
        topics (get-topics-from-config db)]
    (cond->> topics
      (seq available-datasources)
      (filterv (fn [{:keys [datasources]}]
                 (some available-datasources datasources))))))

(defn topic-values [ui-selection]
  ((comp sort set vec flatten conj)
   (map :datasources ui-selection)))

(defn formdata-topics->datasource [formdata]
  (let [update-fn (fn [{:keys [ui-selection topic-selection?] :as row-data}]
                    (cond-> row-data
                      topic-selection?
                      (assoc :topic-selection? false
                             :values (topic-values ui-selection))))]
    (cond
      (and (map? formdata)
           (get formdata topic-attr-desc))
      (update formdata topic-attr-desc
              (fn [row-data]
                (update-fn row-data)))
      (vector? formdata)
      (mapv (fn [[attr-desc row-data :as row]]
              (if (is-topic-attr-desc? attr-desc)
                [attr-desc (update-fn row-data)]
                row))
            formdata))))

(reg-sub
 ::topics
 (fn [db [_ frame-id datasource]]
   (if datasource
     (get-topics-from-datasource db datasource)
     (get-topics db (set (get-in db (path/search-row-options frame-id topic-attr-desc)))))))