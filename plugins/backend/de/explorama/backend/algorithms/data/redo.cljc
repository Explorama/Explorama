(ns de.explorama.backend.algorithms.data.redo
  (:require [clojure.data :as clj-data]))

(defn- values-check-fn
  "Checks diff of two sets
   Returns [<only in check-vals> <only in attributes-set> <in both>]"
  [attributes-set check-vals]
  (if (and (set? attributes-set)
           (set? check-vals))
    (clj-data/diff check-vals attributes-set)
    [#{} #{} #{}]))

(defn- update-conj
  "Helper to update or create and conj an entry to an set"
  [key new old]
  (cond-> (or old {})
    (not (nil? new))
    (update key #(conj (or % #{}) new))))

(defn- add-valid
  "Helper to add multiple valid entries to acc (map)"
  [acc valid-entries op]
  (reduce (fn [acc entry]
            (-> acc
                (update-in [:valid-operations entry] (partial update-conj :op op))
                (update-in [:valid-operations entry] (partial update-conj :opst-paths [op entry]))))
          acc
          valid-entries))

(defn- add-invalid
  "Helper to add multiple invalid entries to acc (map)"
  [acc invalid-entries op]
  (reduce (fn [acc entry]
            (update acc :invalid-operations conj {:op op :attribute entry}))
          acc
          invalid-entries))

(defn- check-operations
  "Checks every operation from operations-state.
   Result is an map {:valid-operations <valid-operations> :invalid-operations <invalid-operations>}"
  [attributes-set operations-state]
  (let [{:keys [problem-type header future-header]} operations-state
        [invalid-header _ valid-header] (values-check-fn attributes-set header)
        [invalid-future-header _ valid-future-header] (values-check-fn attributes-set future-header)]

    (cond-> {:invalid-operations #{}
             :valid-operations {}}
      problem-type
      (-> (update-in [:valid-operations problem-type] (partial update-conj :op :problem-type))
          (update-in [:valid-operations problem-type] (partial update-conj :opst-paths [:problem-type])))
      (and header (not-empty valid-header))
      (add-valid valid-header :header)
      (and header (not-empty invalid-header))
      (add-invalid invalid-header :header)

      (and future-header (not-empty valid-future-header))
      (add-valid valid-future-header :future-header)
      (and future-header (not-empty invalid-future-header))
      (add-invalid invalid-future-header :future-header))))

(defn check-redo
  "Checks attributes and values from Operations/Selections based on possible attributes
   Provides an map with the following keys:
   :valid-operations {<attribute> {:op <operations-set> :opst-paths <paths in operation-state>}]
   :invalid-operations #{{:op <operation> :attribute <attribute> :value <value, when available>}}
   (invalid-operations are used for notifications ui)"
  [attributes-set operations-state]
  (let [{:keys [valid-operations invalid-operations]} (check-operations attributes-set operations-state)]
    {:valid-operations valid-operations
     :invalid-operations invalid-operations
     :valid-operations-state (reduce (fn [r opst-path]
                                       (cond
                                         (some #{:header} opst-path)
                                         (update r :header (fn [o] (conj (or o #{})
                                                                         (peek opst-path))))
                                         (some #{:future-header} opst-path)
                                         (update r :future-header (fn [o] (conj (or o #{})
                                                                                (peek opst-path))))
                                         (get-in operations-state opst-path)
                                         (assoc-in r opst-path (get-in operations-state opst-path))
                                         :else r))
                                     {}
                                     (reduce (fn [paths {:keys [opst-paths]}]
                                               (apply conj paths opst-paths))
                                             #{}
                                             (vals valid-operations)))}))


