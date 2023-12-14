(ns de.explorama.backend.search.direct-search
  (:require [clojure.string :as string]
            [de.explorama.backend.search.attribute-characteristics.api :as ac-api]
            [de.explorama.backend.search.config :as config-search]
            [de.explorama.backend.common.config :as config-backend]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.regex :refer [search-pattern]]
            [taoensso.timbre :refer [debug error]]))

(defn pol-filter
  "Used to filter the policies for the rights/roles config-tab.
   false => needs to be filtered out
   true => can be shown in the ui"
  [[pol-id _]]
  (not (string/starts-with? (name pol-id) "Directsearch-")))

(defn click-event [group-key label]
  (str [:de.explorama.backend.search.api.core/open-with-row group-key label]))

(defn- sort-by-label [results]
  (sort-by (fn [{:keys [label]}]
             (string/lower-case label))
           results))

(defn grouped-search [node-type query group-key group-query]
  (debug "Grouped-Search for" group-key)
  (->> (ac-api/grouped-search node-type query group-key group-query)
       (map (fn [[_ [_ label]]]
              {:label label
               :event (click-event [(name group-key) node-type] label)}))
       sort-by-label
       vec))

(defn datasource-search [query]
  (debug "Datasource-Search")
  (->> (ac-api/datasource-search query)
       (map (fn [[node-type [attr label]]]
              {:label label
               :event (click-event [attr node-type] label)}))
       sort-by-label
       vec))

(defn result-picker
  "According to the type, perform the corresponding search."
  [[search-key search-type] query limit datasources]
  (debug "Search-key" search-key)
  (let [results (cond
                  (#{attrs/context-node attrs/date-node attrs/feature-node} search-type)
                  (grouped-search search-type
                                  query
                                  (name search-key)
                                  (string/lower-case search-key))
                  (= attrs/datasource-node search-type)
                  (filter (fn [{:keys [label]}]
                            (or (not config-backend/explorama-datasource-access-control-enabled)
                                (and config-backend/explorama-datasource-access-control-enabled
                                     (contains? (set datasources) label))))
                          (datasource-search query))
                  :else (error "Search-key not valid:" search-key))
        search-attr (if (= search-key attrs/datasource-attr)
                      nil ;R5 workaround for #6735: dont show error and show-all button for datasource
                      [search-key attrs/context-node])]
    ;; returns the data structure expected by Autosuggestion in Woco
    {:title        (string/capitalize (name search-key))
     :event        (when (and search-key search-attr)
                     (str [:de.explorama.backend.search.direct-search/show-all [search-key search-type]]))
     :ui-results   (take limit results)
     :results      results
     :result-count (count results)}))

(defn- search-attributes [formdata list-type search-keys]
  (let [search-keys-filter (if (= list-type "Whitelist")
                             #(search-keys %)
                             #(not (search-keys %)))]
    (filter (fn [[attr]]
              (and (search-keys-filter attr)
                   (not (config-search/direct-search-ignore-attributes attr))))
            (ac-api/directsearch-attributes formdata))))

(defn search
  "Surveys the query in the given attributes (search-keys)"
  [formdata datasources [list-type search-keys] {:keys [query limit]}]
  (let [attributes (search-attributes formdata list-type search-keys)
        query (string/lower-case query)
      ;; extracts the results from each of the attributes
        search-result (->> attributes
                           (map #(result-picker % query limit datasources))
                           (filterv #(> (:result-count %) 0)))]
   ;; here the trick: returns "Unified search" as title,
   ;; so Woco knows the vector of results is actually embedded in :results
    {:title "Unified search"
     :results search-result}))

(defn- gen-label-with-highlight [search-term label-translation]
  (let [pattern (search-pattern search-term)]
    (fn [string-value]
      (try
        (let [string-value (label-translation string-value)
              index (string/index-of (string/lower-case string-value)
                                     search-term)
              end-index (+ index (count search-term))
              highlight-part (subs string-value index end-index)
              [start-str end-str] (string/split string-value pattern 2)]
          {:prefix start-str
           :highlight highlight-part
           :suffix end-str})
        (catch #?(:clj Throwable :cljs :default) _
          nil)))))

(defn search-elements
  [callback-fn frame-id labels lang org-search-term formdata datasources [list-type search-keys] task-id]
  (let [selected-attributes (set (map #(get-in % [0 0]) formdata))
        attributes (sort-by first
                            (filter #(not (selected-attributes (first %)))
                                    (search-attributes formdata "blacklist" #{"month"})))
        search-term (string/lower-case org-search-term)
        label-fn (gen-label-with-highlight search-term (fn [string-value]
                                                         (get labels string-value string-value)))]
    (callback-fn frame-id
                 :attributes
                 (->> (ac-api/search-attributes formdata search-term lang)
                      (map (fn [[attribute n-label]]
                             (when-let [label (label-fn attribute)]
                               {:on-click-value [attribute n-label]
                                :sort-val (string/lower-case attribute)
                                :label label})))
                      (filter identity)
                      (sort-by :sort-val)
                      set)
                 task-id)

    (let [label-fn (gen-label-with-highlight search-term identity)]
      (doseq [attr attributes]
        (let [all-vals (if (= attr [attrs/datasource-attr attrs/datasource-node])
                         {attr (filter (fn [source] (string/includes? (string/lower-case source) search-term)) datasources)}
                         (ac-api/search-values formdata attr search-term))
              res (->> (get all-vals attr)
                       (map (fn [attr-value]
                              (when-let [label (label-fn attr-value)]
                                {:on-click-value attr-value
                                 :sort-val (string/lower-case attr-value)
                                 :label label})))
                       (filter identity)
                       (sort-by :sort-val)
                       set)]
          (callback-fn frame-id
                       attr
                       res
                       task-id))
        (debug "send result for " attr)))
    (callback-fn frame-id :done nil task-id)))
