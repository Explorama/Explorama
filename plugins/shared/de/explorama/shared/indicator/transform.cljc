(ns de.explorama.shared.indicator.transform)

(declare traverse-)

(defn- traverse-seq [desc values validation-atom]
  (reduce (fn [acc value]
            (let [children (traverse- value values validation-atom)
                  {:keys [method]} (meta children)]
              (case method
                :into
                (into acc children)
                (conj acc children))))
          []
          desc))

(defn traverse-
  [desc values validation-atom]
  (cond (and (sequential? desc)
             (= :# (first desc)))
        (let [template (peek desc)
              normal (traverse-seq (-> desc pop rest) values validation-atom)]
          (cond-> []
            (seq normal)
            (into normal)
            :always
            (into (mapv (fn [iterate-desc]
                          (traverse- template (merge values iterate-desc) validation-atom))
                        (:# values)))
            :always
            (vary-meta assoc :method :into)))
        (sequential? desc)
        (traverse-seq desc values validation-atom)
        (map? desc)
        (reduce (fn [acc [key value]]
                  (assoc acc key (traverse- value values validation-atom)))
                {}
                desc)
        (and (keyword? desc)
             (get values desc))
        (do
          (when validation-atom
            (swap! validation-atom update desc (fn [value]
                                                 (if (number? value)
                                                   (inc value)
                                                   true))))
          (values desc))
        :else
        desc))

(defn- build-validation-structure [values {:keys [fixed generic] :as validation}]
  (when validation
    (atom (as-> (apply assoc {} (interleave fixed (repeat false))) $
            (apply assoc $ (interleave (keys generic) (repeat 0)))
            (assoc $ :#number (count (:# values)))
            (assoc $ :#frequency generic)))))

(defn- validate-result [validation-result]
  (let [{num :#number freq :#frequency} validation-result]
    (when-let [not-valid?
               (reduce (fn [result [key value]]
                         (cond (and (number? value)
                                    (< value (* num
                                                (get freq key))))
                               (assoc result key {:used value
                                                  :target (* num
                                                             (get freq key))})
                               (and (boolean? value)
                                    (not value))
                               (assoc result key {:used 0
                                                  :target 1})
                               :else result))
                       nil
                       (dissoc validation-result :#number :#frequency))]
      not-valid?)))

(defn traverse
  "This function traverses through the given ´desc´ and replaces all keywords with the provided ones
   from ´values´.
   
   Additionally it expandes the given desc if indicated by :# e.g. [:# ... template].
   It will provide for each template iteration one element of the values from :# in ´values´ and tries
   to replace all keywords in one template.
   
   Only the last element of the vector beginng with :# will be treated as template.
   
   The content the :# element will be conjoined to the parent or create a list in case of map values.
   
   This function will convert all sequential? datastructures to vectors.
   
   ´validation´ is a map consiting of :fixed and :generic with lists of all attributes the algorithm has to
   replace. If not provided then there will be no validation."
  ([desc values validation]
   (let [validation-atom (build-validation-structure values validation)
         result (traverse- desc values validation-atom)]
     (when-let [error-data (validate-result @validation-atom)]
       (throw (ex-info "Error during validation - not all necessary values were used!"
                       error-data)))
     result))
  ([desc values]
   (traverse- desc values nil)))
