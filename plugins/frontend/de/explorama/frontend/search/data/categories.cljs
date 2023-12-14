(ns de.explorama.frontend.search.data.categories
  "Support categories of attributes for searching"
  (:require [clojure.set :refer [intersection union]]
            [clojure.string :as str]
            [cuerdas.core :refer [format]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :refer [reg-event-fx reg-sub]]
            [de.explorama.frontend.search.data.topics :refer [is-topic-attr-desc? topic-attr-desc]]
            [de.explorama.frontend.search.path :as path]
            [de.explorama.frontend.search.views.validation :as validation]
            [taoensso.timbre :refer-macros [warn]]
            [de.explorama.frontend.search.views.formdata :refer [can-define-values?]]))

(def topic-category-id :topic)
(def datasource-category-id :datasource)
(def geografic-category-id :geografic)
(def timeperiod-category-id :timeperiod)
(def attributes-category-id :attributes)

(def ^:private fixed-category-childs
  {geografic-category-id [[attrs/country-attr attrs/context-node]
                          [attrs/location-attr attrs/context-node]]
   timeperiod-category-id [["year" attrs/date-node]
                           ["month" attrs/date-node]
                           ["day" attrs/date-node]]})

(def ^:private category-config
  [{:label :topic-category ;"Topic"
    :desc :topic-category-desc
    :id topic-category-id
    :attr-desc topic-attr-desc
    :collapsible? false
    :icon-key :comment-text}
   {:label :datasource-category
    :desc :datasource-category-desc
    :id datasource-category-id
    :attr-desc [attrs/datasource-attr attrs/datasource-node]
    :collapsible? false
    :supported-modes #{:guided}
    :icon-key :database}
   {:label :geografic-category ;(str "Geographic (" (count (get category-childs :geo-loc)) ")")
    :desc :geografic-category-desc
    :id geografic-category-id
    :icon-key :language}
   {:label :timeperiod-category ;(str "Time period (" (count (get fixed-category-childs :time-period)) ")")
    :desc :timeperiod-category-desc
    :id timeperiod-category-id
    :icon-key :calender}
   {:label :attribute-category
    :desc :attribute-category-desc
    :id attributes-category-id
    :icon-key :info}])

(defn- sort-attribute-descs [attributes]
  (->> attributes
       (filter (fn [{:keys [label]
                     [element node-type] :attr-desc}]
                 (if (and element node-type label)
                   true
                   (do
                     (warn "Element/Node-Type/Label is nil for sorting attributes and will be ignored" [element node-type label])
                     nil))))
       (sort-by (fn [{:keys [label]
                      [element node-type] :attr-desc}]
                  (cond (#{"Year" attrs/year-attr} element)
                        "AAAAAA"
                        (#{"Month" "month"} element)
                        "AAAAAAA"
                        (#{"Day" "day"} element)
                        "AAAAAAAA"
                        :else (str/lower-case (str/replace label #"[^a-zA-Z]" "")))))
       vec))

(defn- calc-already-selected [db frame-id]
  (set (map first (keys (get-in db (path/frame-search-rows frame-id))))))

(defn- calc-available-attributes [db frame-id]
  (set (map first (get-in db (path/frame-attributes frame-id) []))))

(defn- filter-label
  ([filter-set filter-text label]
   (and (not (filter-set label))
        (or (str/blank? filter-text)
            (str/includes? (str/lower-case label)
                           filter-text))))
  ([filter-text label]
   (filter-label #{} filter-text label)))

(defn- prepare-childs [db filter-label-fn filter-attr-fn disable-check-fn sort? childs]
  (let [attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)]
    (cond->> childs
      :always (map (fn [[attr type]]
                     (cond-> {:label (i18n/attribute-label attribute-labels attr)
                              :attr-desc [attr type]}
                       (disable-check-fn attr)
                       (assoc :disabled? true))))
      sort? sort-attribute-descs
      (and (fn? filter-label-fn)
           (fn? filter-attr-fn))
      (filterv (fn [{:keys [label]
                     [attr] :attr-desc}]
                 (and (filter-attr-fn attr)
                      (filter-label-fn label)))))))

(defn- get-geographic-attributes-from-config [db]
  (let [geo-attrs (-> (fi/call-api [:config :get-config-db-get]
                                   db
                                   :geographic-attributes)
                      (vals)
                      (vec)
                      (get-in [0 :geographic-attributes]))]
    (if (and geo-attrs (seq geo-attrs))
      (mapv (fn [geo-attr]
              [geo-attr attrs/context-node])
            geo-attrs)
      (get fixed-category-childs geografic-category-id))))

(defn- calc-child-items [db frame-id category filter-text already-selected available-attributes]
  (let [filter-set (cond-> #{}
                     (= category attributes-category-id)
                     (union #{attrs/datasource-attr}
                            (set (map first (get-geographic-attributes-from-config db)))
                            (set (map first (get fixed-category-childs timeperiod-category-id)))))
        filter-label-fn (partial filter-label filter-text)
        filter-attr-fn (partial filter-label filter-set "")
        disable-fn (fn [attr]
                     (or (already-selected attr)
                         (not (available-attributes attr))))
        prepare-fn (partial prepare-childs db filter-label-fn filter-attr-fn disable-fn (not= category geografic-category-id))]
    (condp = category
      geografic-category-id (prepare-fn (get-geographic-attributes-from-config db))
      timeperiod-category-id (prepare-fn (get fixed-category-childs timeperiod-category-id))
      attributes-category-id (prepare-fn (get-in db (path/frame-attributes frame-id) []))
      nil)))

(defn- calc-items [db frame-id mode filter-text show-num?]
  (let [already-selected (calc-already-selected db frame-id)
        available-attributes (calc-available-attributes db frame-id)]
    (reduce (fn [acc {:keys [id label desc collapsible? attr-desc supported-modes]
                      :or {collapsible? true}
                      :as item}]
              (let [label (if (and (= id topic-category-id)
                                   (= mode :free))
                            :topic-datasource-category
                            label)
                    label (i18n/translate db label)
                    desc (i18n/translate db desc)
                    child-num (when collapsible? (count (calc-child-items db frame-id id filter-text already-selected available-attributes)))]
                (cond-> acc
                  (and
                   (or (not supported-modes)
                       (supported-modes mode))
                   (or (and collapsible? (> child-num 0))
                       (and (not collapsible?)
                            (filter-label filter-text label))))
                  (conj (cond-> item
                          :always (assoc :label label
                                         :desc desc)
                          (and collapsible? show-num?)
                          (update :label #(format (str % " ($child-num)")
                                                  {:child-num child-num}))
                          (and (not collapsible?)
                               attr-desc
                               (already-selected (first attr-desc)))
                          (assoc :disabled? true))))))
            []
            category-config)))

(reg-sub
 ::get-geographic-attributes-from-config
 (fn [db]
   (get-geographic-attributes-from-config db)))

(reg-sub
 ::child-items
 (fn [db [_ frame-id category filter-text]]
   (calc-child-items db frame-id category filter-text
                     (calc-already-selected db frame-id)
                     (calc-available-attributes db frame-id))))

(reg-sub
 ::items
 (fn [db [_ frame-id mode filter-text show-num?]]
   (let [items (calc-items db frame-id mode filter-text show-num?)]
     (if (seq items)
       items
       [{:label (i18n/translate db :no-matches)
         :disabled? true}]))))

(defn- unused-items [db frame-id mode]
  (let [items (calc-items db frame-id mode nil nil)
        already-selected (calc-already-selected db frame-id)
        available-attributes (calc-available-attributes db frame-id)]
    (reduce (fn [acc {:keys [collapsible? disabled?]
                      :or {collapsible? true}
                      category-id :id
                      :as item}]
              (let [childs (calc-child-items db frame-id category-id nil
                                             already-selected available-attributes)
                    childs-count (cond
                                   (and collapsible?
                                        (some #(already-selected (get-in % [:attr-desc 0]))
                                              childs))
                                   0
                                   collapsible?
                                   (count childs))]
                (cond-> acc
                  (or (and (not collapsible?)
                           (not disabled?))
                      (and collapsible? (< 0 childs-count)))
                  (conj item))))
            []
            items)))

(reg-sub
 ::unused-items
 (fn [db [_ frame-id mode]]
   (unused-items db frame-id mode)))

(reg-sub
 ::used-items
 (fn [db [_ frame-id mode]]
   (let [items (calc-items db frame-id mode nil nil)
         unused (set (map :id (unused-items db frame-id mode)))
         topic-datasource-row-exists? (boolean (get-in db (path/search-row-data frame-id topic-attr-desc)))
         topic-select? (boolean (get-in db (path/search-row-topic-select frame-id topic-attr-desc)))]
     (filterv (fn [{:keys [id]}]
                (and (not (unused id))
                     (or (and (not= id topic-category-id)
                              (not= id datasource-category-id))
                         (and (= id topic-category-id)
                              topic-datasource-row-exists?
                              topic-select?)
                         (and (= id datasource-category-id)
                              topic-datasource-row-exists?
                              (not topic-select?)))))
              items))))

(defn- str-representation [option]
  (cond-> option
    (and (map? option)
         (:label option))
    (:label option)))

(defn- conj-mulitple-helper [acc coll]
  (apply conj acc coll))

(defn- row->desc [db frame-id attr-filter lang acc
                  [attr-desc {:keys [value
                                     from to
                                     start-date end-date selected-date
                                     all-values? empty-values?
                                     advanced ui-selection]
                              condition :cond
                              :as row-data}]]
  (if-not (attr-filter attr-desc)
    acc
    (let [attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          valid? (validation/is-row-valid? db frame-id [attr-desc row-data])
          attribute-path (path/search-row-data frame-id attr-desc)
          attribute-define-values? (can-define-values? db attribute-path)
          [attr] attr-desc
          raw (if-not valid?
                (cond-> []
                  (not (is-topic-attr-desc? attr-desc))
                  (conj
                   (str (get attribute-labels attr attr)
                        ":")
                   (i18n/translate db :incomplete-definition)))
                (cond-> []
                  (not (is-topic-attr-desc? attr-desc))
                  (conj (cond-> (get attribute-labels attr attr)
                          (and (not condition)
                               (not (is-topic-attr-desc? attr-desc)))
                          (str ":")))
                  (and advanced condition attribute-define-values?)
                  (conj (str-representation condition))
                  (and advanced all-values?)
                  (conj (i18n/translate db :any-non-empty-values-label))
                  (and advanced empty-values?)
                  (conj (i18n/translate db :any-empty-values-label))
                  (and value (not ui-selection) attribute-define-values?)
                  (conj (str-representation value))
                  (and ui-selection attribute-define-values?)
                  (conj-mulitple-helper (map (fn [[idx sel]]
                                               (cond-> (if (= ["month" attrs/date-node] attr-desc)
                                                         (i18n/month-name (:value sel) lang)
                                                         (str-representation sel))
                                                 (and (< idx (dec (count ui-selection)))
                                                      (not (is-topic-attr-desc? attr-desc)))
                                                 (str ",")))
                                             (map-indexed vector ui-selection)))
                  (and selected-date attribute-define-values?)
                  (conj (str-representation selected-date))
                  (and from to attribute-define-values?)
                  (conj (str-representation from)
                        "-"
                        (str-representation to))
                  (and start-date end-date attribute-define-values?)
                  (conj (str-representation start-date)
                        "-"
                        (str-representation end-date))))]
      (cond
        (and (is-topic-attr-desc? attr-desc)
             (seq raw))
        (apply conj acc raw)
        (seq raw)
        (conj acc (str/join " " raw))
        :else
        (conj acc (i18n/translate db :incomplete-definition))))))

(reg-sub
 ::search-selection-desc
 (fn [db [_ frame-id category-id lang]]
   (let [search-rows (get-in db (path/frame-search-rows frame-id) [])
         child-items (if (or (= category-id datasource-category-id)
                             (= category-id topic-category-id))
                       [(some #(when (= category-id (:id %))
                                 %)
                              category-config)]
                       (calc-child-items db frame-id category-id nil
                                         #{}
                                         (calc-available-attributes db frame-id)))
         attr-filter (set (map :attr-desc child-items))]
     (reduce (partial row->desc db frame-id attr-filter lang)
             [] search-rows))))

(reg-event-fx
 ::remove-category
 (fn [{db :db} [_ frame-id category-id]]
   (let [selected-attrs (set (keys (get-in db (path/frame-search-rows frame-id))))
         child-items (if (or (= category-id datasource-category-id)
                             (= category-id topic-category-id))
                       [(some #(when (= category-id (:id %))
                                 %)
                              category-config)]
                       (calc-child-items db frame-id category-id nil
                                         #{}
                                         (calc-available-attributes db frame-id)))
         attr-descs (intersection selected-attrs
                                  (set (map :attr-desc child-items)))
         last-elem-idx (dec (count attr-descs))]
     {:fx (mapv (fn [[idx attr-desc]]
                  [:dispatch [:de.explorama.frontend.search.views.attribute-bar/delete-search-row
                              frame-id
                              attr-desc
                              (not= idx last-elem-idx)]])
                (map-indexed vector attr-descs))})))