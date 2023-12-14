(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.core
  (:require [clojure.set :as set]
            [clojure.string :as cstr]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.cache :as scache]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.graph :as graph]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.selection-path :as sel-path]
            [de.explorama.backend.expdb.query.graph :as ngraph]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :as log :refer [debug]]
            [taoensso.tufte :as tufte]))

(def group-nodetypes #{attrs/feature-node attrs/context-node attrs/date-node})
(def type-blacklist #{"integer" "float" "boolean" "decimal" "double"})

(defn ensure-selection-val-string
  "Ensures that all values in the selection-vector are strings.
   Can/Must be removed in later version when it is not needed anymore."

  [[attr type val]]
  [(str attr)
   (str type)
   (str val)])

(def attribute-blacklist
  "Blacklist to define which ACs should not send to Clients. This collection
  contains [<nodetype> <attribute>] tuples."
  [[attrs/context-node attrs/location-attr]])
;["Date " "month"]
;["Date" "day"]])

(defn sort-attribute-descs [attributes]
  (vec
   (sort-by (fn [[element node-type]]
              (cond (#{"Year" attrs/year-attr} element)
                    "C"
                    (#{"Month" "month"} element)
                    "D"
                    (#{"Day" "day"} element)
                    "E"
                    (#{"Country" attrs/country-attr} element)
                    "A"
                    (#{attrs/datasource-attr} element)
                    "B"
                    (#{"datasource-temp"} element)
                    "BB"
                    (#{"datasource-ki"} element)
                    "BBB"
                    (#{attrs/datasource-node attrs/datasource-attr} node-type)
                    "BBBB"
                    (#{"Annotation" "annotation"} node-type)
                    "comment"
                    :else (cstr/lower-case (cstr/replace element #"[^a-zA-Z]" ""))))
            attributes)))

(defn calc-attributes [nodes]
  (into #{}
        (comp (map (fn [[nodetype attr]]
                     [attr nodetype]))
              (filter (fn [[attr nodetype]]
                        (and attr nodetype))))
        nodes))

(defn attr-type [nodes attr]
  (some (fn [[n a t]]
          (when (= a attr)
            [n a t]))
        nodes))

(defn characteristic-type [nodes attr]
  (let [[nodetype type charact] (attr-type nodes attr)
        at-type (cond
                  (= attrs/date-node nodetype) type
                  (= attrs/datasource-node nodetype) nil
                  (and (= attrs/context-node nodetype)
                       (= attrs/location-attr type)) type
                  (= attrs/context-node nodetype) nil
                  (= attrs/feature-node nodetype) nil
                  (= attrs/notes-node nodetype) type
                  (and (= attrs/fact-node nodetype)
                       (= charact "string")) "notes"
                  (and (= attrs/fact-node nodetype)
                       (not (type-blacklist charact))) nil
                  :else charact)]
    (if at-type
      at-type
      "string")))

(defn calc-attribute-types [nodes]
  (reduce (fn [acc attr]
            (assoc acc attr (characteristic-type nodes (first attr))))
          {}
          @graph/cached-all-attributes))

(defn all-attributes []
  (if (seq @graph/cached-all-attributes)
    @graph/cached-all-attributes
    (do
      (doseq [[_ {schema :schema}] config-expdb/explorama-bucket-config]
        (swap! graph/cached-all-attributes into (calc-attributes (ngraph/nodes schema))))

      (debug "Calculated cached-all-attributes:" @graph/cached-all-attributes)

      (reset! graph/cached-attribute-types
              (calc-attribute-types (apply concat
                                           (for [[_ {schema :schema}] config-expdb/explorama-bucket-config]
                                             (ngraph/nodes schema)))))
      (debug "Calculated cached-attribute-types")
      @graph/cached-all-attributes)))

(defn get-directsearch-attributes
  "Returns a vector with the names of the attributes which belong to one of the 4 given types."
  [attributes]
  (let [allowed-types #{attrs/feature-node attrs/date-node attrs/context-node attrs/datasource-node}]
    (filter
     (fn [[_ type]] (allowed-types type))
     attributes)))

(defn update-acs! []
  (debug "AC Update arrived")
  (scache/new-ac-cache)
  (reset! graph/directsearch-attributes (get-directsearch-attributes @graph/cached-all-attributes))
  (reset! graph/cached-all-attributes nil)
  (reset! graph/directsearch-attributes nil)
  (reset! graph/cached-attribute-types nil)
  (all-attributes)) ;; so we update the directsearch attributes after

(defn add-selection-nodes [nodes or-selection selections is-multi?]
  (if is-multi?
    (apply conj
           nodes
           (map (fn [[_ selection]]
                  (ensure-selection-val-string selection))
                selections))
    (let [[_ selection] or-selection]
      (conj nodes (ensure-selection-val-string selection)))))

(defn add-selection-path-nodes
  "Add the selection-path nodes in the nodes vector."
  [nodes selection-path]

  (set (reduce (fn [result [or-selection selections]]
                 (let [[op _] (when-not (keyword? or-selection)
                                or-selection)]
                   (cond
                     (and (= or-selection :or)
                          (or
                           (nil? op)
                           (= op :=)))
                     (add-selection-nodes result or-selection selections true)
                     (and (= or-selection :and)
                          (or
                           (nil? op)
                           (= op :=)))
                     (add-selection-nodes result or-selection selections true)
                     (= op :=)
                     (add-selection-nodes result or-selection selections false)
                     :else result)))
               nodes
               selection-path)))

(defn possible-characteristics
  [schema selection-path]
  (scache/update-cache schema
                       selection-path
                       (fn []
                         (as-> (if (empty? selection-path)
                                 (ngraph/nodes schema)
                                 (tufte/p ::reduce (reduce
                                                    clojure.set/intersection
                                                    (map #(set (graph/select-nodes schema %))
                                                         selection-path))))
                               $
                           (into [] $)
                           (add-selection-path-nodes $ selection-path)))))

(defn vector-merge [val1 val2]
  (cond
    (vector? val1) (-> val1
                       (concat val2)
                       set
                       vec)
    (set? val1) (clojure.set/union val1 val2)
    :else (merge-with vector-merge
                      val1
                      val2)))

;; **external**
;; de.explorama.backend.search.datainstance.core
(defn reduced-acs
  ([formdata]
   (reduce vector-merge
           (for [[_ {schema :schema}] config-expdb/explorama-bucket-config]
             (reduced-acs formdata schema))))
  ([formdata schema]
   (let [nodes (ngraph/nodes schema)
         [curr-selection-path] (sel-path/calc-selection-path [] formdata nodes (first formdata))]
     (possible-characteristics schema curr-selection-path))))

(defn attributes-formdata [formdata]
  (let [reduced-nodes (reduced-acs formdata)]
    (calc-attributes reduced-nodes)))

(defn acs-group-by [nodes]
  (reduce (fn [res [n a v]]
            (let [k [a n]]
              (update res k (fn [o] (conj (or o #{})
                                          v)))))
          {}
          nodes))

(defn attribute-options
  ([attributes formdata]
   (attribute-options attributes
                      formdata
                      (ngraph/all-nodes)
                      false))
  ([attributes formdata schema]
   (attribute-options attributes
                      formdata
                      (ngraph/all-nodes)
                      schema
                      false))
  ([attributes formdata nodes validate-options?]
   (apply
    merge-with
    vector-merge
    (for [[_ {schema :schema}] config-expdb/explorama-bucket-config]
      (attribute-options
       attributes
       formdata
       nodes
       schema
       validate-options?))))
  ([attributes formdata nodes schema validate-options?]
   (let [[selection-path relevant-formdata] (sel-path/calc-selection-path [] formdata nodes (first attributes))
         init-reduce {:result-options {}
                      :result-attributes []
                      :nodes nodes
                      :formdata relevant-formdata
                      :selection-path selection-path}
         node-vals (when validate-options?
                     (acs-group-by nodes))]
     (select-keys (reduce (fn [{:keys [nodes formdata selection-path] :as acc} attr-desc]
                            (let [[attr nodetype] attr-desc
                                  attrtype (characteristic-type nodes attr)]
                              (if (or (and (= nodetype attrs/fact-node)
                                           (type-blacklist attrtype))
                                      (sel-path/ignored-node-types nodetype)
                                      (and (= nodetype attrs/context-node)
                                           (= attr attrs/location-attr)))
                                (-> acc
                                    (assoc :formdata (filterv (fn [[attrd]] (= attrd attr-desc))
                                                              formdata)))
                                (let [[curr-selection-path relevant-formdata] (sel-path/calc-selection-path selection-path formdata nodes attr-desc)
                                      reduced-nodes (possible-characteristics schema curr-selection-path)
                                      options-for-attribute (vec (sort (sel-path/possible-attribute-values reduced-nodes attr)))
                                      options-for-attribute (if validate-options?
                                                              (filterv (get node-vals attr-desc #{}) options-for-attribute)
                                                              options-for-attribute)
                                      newacc (-> acc
                                                 (update :result-options assoc attr-desc options-for-attribute)
                                                 (assoc :nodes reduced-nodes
                                                        :selection-path curr-selection-path
                                                        :formdata relevant-formdata))]
                                  (if (empty? relevant-formdata)
                                    (assoc newacc :result-attributes (calc-attributes reduced-nodes))
                                    newacc)))))
                          init-reduce
                          attributes)
                  [:result-options :result-attributes]))))

(defn- neighborhood-filter [allow-list-fn block-list-fn [label? attr-name? value?] node]
  (when (and (allow-list-fn node)
             (not (block-list-fn node)))
    (let [[label attr-name attr-value] node]
      (cond-> []
        label?
        (conj label)
        attr-name?
        (conj attr-name)
        value?
        (conj attr-value)))))

(defn neighborhood [schema nodes pattern operation allow-list-fn block-list-fn sort-data-fn]
  (->> (if (empty? nodes)
         (into #{}
               (comp (map (fn [node]
                            (neighborhood-filter allow-list-fn block-list-fn pattern node)))
                     (filter identity))
               (ngraph/nodes schema))
         (let [op-fun (or (operation {:intersection set/intersection :union set/union}) set/intersection)]
           (apply disj
                  (->> (into []
                             (comp
                              (map (partial ngraph/node->dts schema))
                              (map (fn [dts]
                                     (into #{}
                                           (comp (mapcat (partial ngraph/dt->nodes schema))
                                                 (map (fn [node]
                                                        (neighborhood-filter allow-list-fn block-list-fn pattern node)))
                                                 (filter identity))
                                           dts))))
                             nodes)
                       (apply op-fun))
                  (map (fn [node]
                         (neighborhood-filter (constantly true)
                                              (constantly false)
                                              pattern
                                              node))
                       nodes))))
       sort-data-fn
       vec))
