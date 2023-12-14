(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.graph
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.conditions-utils :as cond-utils]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.selection-path :as sel-path]
            [de.explorama.backend.expdb.query.graph :as ngraph]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.misc :refer [cljc-parse-int]]))

(defonce cached-attribute-types (atom nil))

(defn get-attribute-types []
  @cached-attribute-types)

(defonce cached-all-attributes (atom nil))
(defonce directsearch-attributes (atom nil))

(defn- ensure-selection-val-string
  "Ensures that all values in the selection-vector are strings.
   Can/Must be removed in later version when it is not needed anymore."
  [[attr type val]]
  [(str attr) (str type) (str val)])

(defn- selection-impl-=
  "Finds all possible-characteristics that are connected with the selection.
  A selection is one node-tuple like [Date [year 2000]]."
  [schema selection]
  (let [all-nodes (ngraph/nodes schema)
        [node-type attribute value] selection
        value-type (get @cached-attribute-types [attribute node-type])
        selection (cond
                    (and (= "Fact" node-type)
                         (#{"integer" "decimal" "number" "double"} value-type))
                    [node-type [attribute value-type]]
                    (= "Fact" node-type)
                    (some (fn [[type attr val]]
                            (when (and
                                   (= node-type type)
                                   (= attribute attr)
                                   (= value val))
                              [type attr val]))
                          all-nodes)
                    :else
                    selection)]
    (ngraph/uncond-neighborhood schema [(ensure-selection-val-string selection)])))

(declare selection-impl-or)

(defn- selection-impl-not-and [schema selections]
  (let [selection-pred (mapv (fn [sel]
                               (str (get-in sel [1 1 1])))
                             selections)
        [[_ node-t attribute]] selections
        found-nodes (set (filterv (fn [[nodetype attr val]]
                                    (and
                                     (= node-t nodetype)
                                     (= attribute attr)
                                     (not (sel-path/in? selection-pred val))))
                                  (ngraph/nodes schema)))
        eq-selections (mapv (fn [node]
                              [:= node])
                            found-nodes)
        pos-charcs (selection-impl-or schema eq-selections)]
    (set/union pos-charcs found-nodes)))

(defn- selection-impl-loc [schema _]
  (ngraph/uncond-neighborhood schema [(ensure-selection-val-string [attrs/context-node attrs/location-attr
                                                                    attrs/location-attr])]))

(defn- selection-impl-or
  "Returns all nodes that are connected with the selections.
   The nodes described in selections are the same and will be viewed as or."
  [schema selections]
  (let [[[first-op]] selections
        result-set (case first-op
                     := (map (fn [[_ selection]]
                               (ngraph/uncond-neighborhood schema [(ensure-selection-val-string selection)]))
                             selections)
                     :not [(selection-impl-not-and schema selections)]
                     nil)]
    (apply set/union result-set)))

(defn- selection-impl-and [schema selections]
  (let [[[first-op]] selections
        result-set (case first-op
                     := (map (fn [[_ selection]]
                               (ngraph/uncond-neighborhood schema [(ensure-selection-val-string selection)]))
                             selections)
                     :not [(selection-impl-not-and schema selections)]
                     nil)]
    (apply set/intersection result-set)))

(defn- selection-impl-not=
  "Returns all nodes that are not connected with the selection.
  Example: selection is [Date [year 2000]]
  Means find all node from type Date and attribute year that are not 2000.
  For all found nodes find all nodes that are connected."
  [schema selection]
  (let [[node-t attribute value] selection
        found-nodes (set (filterv (fn [[nodetype attr val]]
                                    (and
                                     (= node-t nodetype)
                                     (= attribute attr)
                                     (not= val value)))
                                  (ngraph/nodes schema)))
        selections (mapv (fn [node]
                           [:= node])
                         found-nodes)
        pos-charcs (selection-impl-or schema selections)]
    (set/union pos-charcs found-nodes)))

(defn- range-impl [schema node-t attribute check-func]
  (let [all-nodes (ngraph/nodes schema)
        found-nodes (set (filterv (fn [[nodetype attr val]]
                                    (and
                                     (= node-t nodetype)
                                     (= attribute attr)
                                     (or (#{"integer" "decimal" "number" "double"} val)
                                         (check-func val))))
                                  all-nodes))
        indices (map #(ensure-selection-val-string %)
                     found-nodes)
        nodes (ngraph/uncond-neighborhood schema indices)]
    (set (concat nodes found-nodes))))

(defn- selection-impl-range
  "Example: selection is [[Date [year 2000]][Date [year 2010]]]"
  [schema selection]
  (let [[from to] selection
        [node-t attribute from-value] from
        [_ _ to-value] to
        compare-check (if (= attribute "day")
                        (fn [val] (and (cond-utils/compare-dates from-value val :>=)
                                       (cond-utils/compare-dates val to-value :>=)))
                        (fn [val] (<= from-value (cljc-parse-int val) to-value)))]
    (range-impl schema node-t attribute compare-check)))

(defn- selection-impl-not-range
  "Example: selection is [[Date year 2000][Date year 2010]]"
  [schema selection]
  (let [[from to] selection
        [node-t attribute from-value] from
        [_ _ to-value] to
        compare-check (if (= attribute "day")
                        (fn [val] (not (and (cond-utils/compare-dates from-value val :>=)
                                            (cond-utils/compare-dates val to-value :>=))))
                        (fn [val]
                          (not (<= from-value (cljc-parse-int val) to-value))))]
    (range-impl schema node-t attribute compare-check)))

(defn- selection-impl-compare
  "Example: selection is [Date year 2000]"
  [schema op selection]
  (let [[node-t attribute value] selection
        compare-func (cond-utils/condition-func op)
        compare-check (fn [val]
                        (if (= attribute "day")
                          (cond-utils/compare-dates value val op)
                          (compare-func (if (string? val)
                                          (cljc-parse-int val)
                                          val)
                                        value)))]
    (range-impl schema node-t attribute compare-check)))

(defn extend-to-neighbourhood [schema selection]
  (let [node-ids (set (map ensure-selection-val-string selection))]
    (ngraph/uncond-neighborhood schema node-ids)))

(defn- selection-impl-non-empty
  [schema [sel-type sel-attr _]]
  (->> (ngraph/nodes schema)
       (filter (fn [[type attr]]
                 (and
                  (= sel-type type)
                  (= sel-attr attr))))
       ((partial extend-to-neighbourhood schema))
       (set)))

(defn- selection-impl-empty [schema selection]
  (selection-impl-non-empty schema selection))

(defn select-nodes [schema [or-selection selections]]
  (case or-selection
    :and (selection-impl-and schema selections)
    :or (selection-impl-or schema selections)
    :range (selection-impl-range schema selections)
    :not-range (selection-impl-not-range schema selections)
    :location (selection-impl-loc schema selections)
    (let [[op selection] or-selection]
      (case op
        (:> :>= :< :<=) (selection-impl-compare schema op selection)
        := (selection-impl-= schema selection)
        :not (selection-impl-not= schema selection)
        :empty (selection-impl-empty schema selection)
        :non-empty (selection-impl-non-empty schema selection)))))

(defn get-directsearch-attributes-as-options
  "Converts a vector of strings into a vector of maps."
  []
  (->> @directsearch-attributes
       (map (fn [[attribute]] {:name attribute}))
       (sort-by (fn [{:keys [name]}] (string/lower-case name)))
       vec))

(defn extract-acs [result path]
  (conj result [path (get-directsearch-attributes-as-options)]))

(defn get-config-data [paths]
  (reduce extract-acs
          []
          paths))
