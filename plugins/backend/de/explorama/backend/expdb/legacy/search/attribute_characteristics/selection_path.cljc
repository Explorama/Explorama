(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.selection-path
  (:require [clojure.string :as cstr]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.conditions-utils :as cond-utils]
            [de.explorama.shared.common.data.attributes :as attrs]))

(def ignore-node-types #{"fulltext" "fultext" "external-ref" "notes" "annotation"})

(defn ignored-node-types [nodetype]
  (when (and nodetype (string? nodetype))
    (boolean (ignore-node-types (cstr/lower-case nodetype)))))

(defn- build-selection [condi all-values? empty-values? nodetype attr value]
  (cond
    all-values? [:non-empty [nodetype attr value]]
    empty-values? [:empty [nodetype attr value]]
    (nil? value) nil
    :else
    (case condi
      "not=" [:not [nodetype attr value]]
      "not in range" [:not [nodetype attr value]]
      (let [op (if (cond-utils/compare-ops condi)
                 (keyword condi)
                 :=)]
        [op [nodetype attr value]]))))

(defn possible-attribute-values [nodes attr]
  (reduce (fn [acc [_ a val]]
            (if (= a attr)
              (conj acc val)
              acc))
          []
          nodes))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn- prepare-multi-vals [attr condi multi-values nodes]
  (cond (= attr "month")
        (reduce (fn [res year-month]
                  (let [ends-with? #(cstr/ends-with? year-month (str %))
                        match? (if (and condi (cstr/includes? condi "not"))
                                 (nil? (some ends-with? multi-values))
                                 (not (nil? (some ends-with? multi-values))))]
                    (if match?
                      (conj res year-month)
                      res)))
                []
                (possible-attribute-values nodes attr))
        :else multi-values))

(defn- calc-selection-elements [nodes [attr nodetype] values]
  (let [multi-values (:values values)
        range-from (or
                    (get-in values [:from :value])
                    (:from values))
        range-to (or
                  (get-in values [:to :value])
                  (get values :to range-from))
        date-from (get values :start-date)
        date-to (get values :end-date)
        value (or
               (get-in values [:value :value])
               (:value values)
               (:selected-date values))
        condi (or
               (get-in values [:cond :value])
               (:cond values))
        all-values? (:all-values? values)
        empty-values? (:empty-values? values)
        prepared-multi-vals (prepare-multi-vals attr condi multi-values nodes)
        multi-val-selection (mapv (fn [value]
                                    (build-selection condi false false nodetype attr value))
                                  prepared-multi-vals)
        multi-val-selection-filtered (filterv (fn [val]
                                                (not (nil? val)))
                                              multi-val-selection)]
    (cond
      (and (= attrs/location-attr attr)
           (= attrs/context-node nodetype))
      [:location [values]]
      all-values?
      [(build-selection condi true false nodetype attr value)]
      empty-values?
      [(build-selection condi false true nodetype attr value)]
      prepared-multi-vals (if (empty? multi-val-selection-filtered)
                            nil
                            (cond
                              (= 1 (count prepared-multi-vals))
                              multi-val-selection-filtered
                              (and condi (cstr/includes? condi "not"))
                              [:and multi-val-selection-filtered]
                              :else
                              [:or multi-val-selection-filtered]))
      (and date-from date-to condi (cstr/includes? condi "not"))
      [:not-range [[nodetype attr date-from] [nodetype attr date-to]]]
      (and date-from date-to)
      [:range [[nodetype attr date-from] [nodetype attr date-to]]]
      (and range-from range-to condi (cstr/includes? condi "not"))
      [:not-range [[nodetype attr range-from] [nodetype attr range-to]]]
      (and range-from range-to)
      [:range [[nodetype attr range-from] [nodetype attr range-to]]]
      :else
      ;; This is the branch where my "notes" facet is constructed.
      (let [selection-entry (build-selection condi all-values? empty-values? nodetype attr value)]
        (if (nil? selection-entry)
          nil
          [selection-entry])))))

;; **entrypoint** (entrypoint into this namespace)
(defn calc-selection-path [selection-path formdata nodes attr-desc]
  (reduce (fn [[selection-path formdata trigger] [at-key desc]]
            (cond

              (or trigger
                  (ignored-node-types (second at-key)))
              [selection-path formdata trigger]

              (and (not trigger)
                   (= at-key attr-desc))
              [selection-path formdata true]

              :else
              (let [result (calc-selection-elements nodes at-key desc)
                    sel-path (cond-> selection-path
                               result (conj result))]
                [sel-path
                 (filterv (fn [[attrd]]
                            (not= attrd at-key))
                          formdata)
                 false])))
          [selection-path formdata false]
          formdata))

(defn selection-path*
  "This function turns an external, API dependent version of a search/filter
  expression into an internal data structure (which, I think, will later on
  be handed over to the data-format-lib to compile a filter expression from.)"
  [formdata nodes]
  (for [[at-key desc] formdata]
    (calc-selection-elements nodes at-key desc)))
