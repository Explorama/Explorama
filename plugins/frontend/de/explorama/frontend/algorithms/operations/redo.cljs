(ns de.explorama.frontend.algorithms.operations.redo
  (:require [de.explorama.frontend.algorithms.path.core :as path]))

(defn- extract-problem-type 
  "if algorithm is there find out problem-type, else use given problem-type"
  [problem-types {:keys [problem-type algorithm]}]
  (or problem-type
      (some (fn [[problem-type {:keys [algorithms]}]]
              (when ((set algorithms)
                     algorithm)
                problem-type))
            problem-types)))

(defn build-operations-state
  "Represents all data-related operations/selections to check if some is not any more available after new data.
   (Invalid will be shown as notification for user)"
  [db frame-id]
  (let [problem-types (get-in db path/problem-types)
        [problem-def {:keys [future-header header] :as d}]
        (first (get-in db (path/training-data frame-id)))
        problem-type (extract-problem-type problem-types problem-def)]
    (cond-> {}
      problem-type
      (assoc :problem-type problem-type)
      (and header (coll? header) (not-empty header))
      (assoc :header (set header))
      (and future-header (coll? future-header) (not-empty future-header))
      (assoc :future-header (set future-header)))))

;; Currently unused, because vertical handle cleaning invalid itself. 
;; But still here for consistency of this part over all verticals
(defn remove-invalid-operations
  "Removes invalid entries from db based on invalid-operations desc which is an collection
    ({:op <operation> :attribute <attribute> :value <value, when available>}) provided (and calculated)
   from server-side"
  [db frame-id invalid-operations]
  (reduce (fn [db {:keys [op value]}]
            (case op
              db))
          db
          invalid-operations))

(defn show-notification?
  "Checks if there is any invalid operation"
  [invalid-operations]
  (boolean (and invalid-operations
                (coll? invalid-operations)
                (not-empty invalid-operations))))
