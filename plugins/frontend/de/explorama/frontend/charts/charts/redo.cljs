(ns de.explorama.frontend.charts.charts.redo
  (:require [de.explorama.frontend.ui-base.utils.select :refer [normalize]]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.charts.settings :as settings]))

(defn- build-chart-operations-state [db frame-id chart-index]
  (let [y-option (get-in db (path/y-option frame-id chart-index))
        x-option (get-in db (path/x-option frame-id))
        r-option (get-in db (path/r-option frame-id chart-index))
        sum-by-option (get-in db (path/sum-by-option frame-id chart-index))
        sum-by-values (get-in db (path/sum-by-values frame-id chart-index))
        use-nlp-attributes (get-in db (path/use-nlp-attributes frame-id chart-index))
        attributes (into #{} (map normalize (get-in db (path/attributes frame-id chart-index))))
        attributes (when-not (or (contains? attributes :all)
                                 (contains? attributes :characteristics))
                     attributes)
        chart-type (-> (get-in db (path/chart-type frame-id chart-index) settings/default-chart-desc)
                       normalize
                       (:cid))]
    (cond-> {}
      (and use-nlp-attributes (= :wordcloud chart-type))
      (assoc :use-nlp (into #{} (map normalize use-nlp-attributes)))
      (and attributes (= :wordcloud chart-type))
      (assoc :attributes attributes)
      (#{:bar :line :scatter :bubble :pie} chart-type)
      (assoc :y-option (normalize y-option))
      (#{:bar :line :scatter :bubble} chart-type)
      (assoc :x-option (normalize x-option))
      (#{:bubble} chart-type)
      (assoc :r-option (normalize r-option))
      (not= :wordcloud chart-type)
      (assoc :sum-by-option (normalize sum-by-option))
      (not= :wordcloud chart-type)
      (assoc :sum-by-values (into #{} (map normalize sum-by-values))))))

(defn build-operations-state
  "Represents all data-related operations/selections to check if some is not any more available after new data.
   (Invalid will be shown as notification for user)"
  [db frame-id]
  (let [x-option (get-in db (path/x-option frame-id))
        chart-operations-states (mapv (partial build-chart-operations-state db frame-id)
                                      (range (max 1 (count (get-in db (path/charts frame-id))))))
        any-chart-with-x? (some (fn [chart-desc]
                                  (#{:bar :line :scatter :bubble}
                                   (-> (get chart-desc path/chart-type-desc-key)
                                       normalize
                                       (:cid))))
                                (get-in db (path/charts frame-id)))]
    (cond-> {}
      any-chart-with-x?
      (assoc :x-option (normalize x-option))
      (seq chart-operations-states)
      (assoc :chart-operations chart-operations-states))))

(defn remove-invalid-operations
  "Removes invalid entries from db based on invalid-operations desc which is an collection
    ({:op <operation> :attribute <attribute> :value <value, when available>}) provided (and calculated)
   from server-side"
  [db frame-id invalid-operations]
  ;;server handles it currently 
  db)

(defn show-notification?
  "Checks if there is any invalid operation"
  [invalid-operations]
  (boolean (and invalid-operations
                (coll? invalid-operations)
                (not-empty invalid-operations))))
