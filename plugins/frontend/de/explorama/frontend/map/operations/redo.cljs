(ns de.explorama.frontend.map.operations.redo
  (:require [de.explorama.frontend.map.paths :as geop]))

(defn build-operations-state
  "Represents all data-related operations/selections to check if some is not any more available after new data.
   (Invalid will be shown as notification for user)"
  [db frame-id]
  (let [active-layer-ids (reduce (fn [ids [id active?]]
                                   (cond-> ids
                                     (and active? id) (conj id)))
                                 #{}
                                 (get-in db (geop/frame-active-layers frame-id)))
        layers (get-in db geop/layers-path)
        active-layer-attributes (reduce (fn [r layer-id]
                                          (if-let [attr (some (fn [{:keys [id attribute]}]
                                                                (when (and (= layer-id id) attribute)
                                                                  attribute))
                                                              layers)]
                                            (update r attr #(conj (or % #{}) layer-id))
                                            r))
                                        {}
                                        active-layer-ids)]
    (cond-> {}
      active-layer-attributes
      (assoc :active-layer-attributes active-layer-attributes))))

(defn remove-invalid-operations
  "Removes invalid entries from db based on invalid-operations desc which is an collection
    ({:op <operation> :attribute <attribute> :value <value, when available>}) provided (and calculated)
   from server-side"
  [db frame-id invalid-operations]
  (let [db (reduce (fn [db {:keys [op layer-id]}]
                     (cond-> db
                       (and (= op :active-layer-attributes)
                            layer-id
                            (get-in db (geop/frame-active-layer frame-id layer-id)))
                       (assoc-in (geop/frame-active-layer frame-id layer-id) false)))
                   db
                   invalid-operations)
        layer-changes? (some #(= :active-layer-attributes (:op %)) invalid-operations)
        db (cond-> db
             (and layer-changes?
                  (every? #(false? (second %))
                          (get-in db (geop/frame-active-layers frame-id))))
             (assoc-in (geop/frame-active-layer frame-id "default-marker") true))]

    db))

(defn show-notification?
  "Checks if there is any invalid operation"
  [invalid-operations]
  (boolean (and invalid-operations
                (coll? invalid-operations)
                (not-empty invalid-operations))))
