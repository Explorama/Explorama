(ns de.explorama.backend.charts.data.redo)

(defn- value-check-fn [ui-options key]
  (if-let [entries (if (false? key)
                     ui-options
                     (get ui-options key))]
    (set (map :value entries))
    #{}))

(defn- check-sum-by-values
  "Helper function which checks all values with an given check-fn and write down the results in r.
   r is the current data-structe from check-operations-fn"
  [r chart-index sum-by-option sum-by-values check-fn]
  (reduce (fn [r value]
            (if (check-fn value)
              (update-in r
                         [:valid-operations chart-index sum-by-option :opst-paths]
                         (fn [o] (conj (or o #{}) [chart-index :sum-by-values value])))
              (update r :invalid-operations conj {:op :sum-by-values :attribute sum-by-option :value value})))

          r
          sum-by-values))

(defn- check-wordcloud-options
  "Helper function which updates valid/invalid for a given op.
   r is the current data-structe from check-operations-fn"
  [r op-state-options op check-fn]
  (reduce (fn [r value]
            (if (check-fn value)
              (-> r
                  (update-in [:valid-operations 0 value :op]
                             (fn [o] (conj (or o #{}) op)))
                  (update-in [:valid-operations 0 value :opst-paths]
                             (fn [o] (conj (or o #{}) [op value]))))
              (update r :invalid-operations
                      conj {:op op
                            :attribute value})))
          r
          op-state-options))

(defn- check-operations
  "Checks every operation from operations-state.
   Result is an map {:valid-operations <valid-operations> :invalid-operations <invalid-operations>}"
  [ui-options operations-state]
  (let [{:keys [x-option chart-operations]} operations-state
        x-options-check-fn (value-check-fn ui-options :x-options)
        contains-x? (contains? operations-state :x-option)
        use-nlp-options-check-fn (value-check-fn ui-options :wordcloud-attrs)
        attribtues-options-check-fn (value-check-fn ui-options :wordcloud-attrs)
        y-options-check-fn (value-check-fn (into []
                                                 (flatten
                                                  (map :options (get-in ui-options [:y-options :groups]))))
                                           false)
        sum-options-check-fn (value-check-fn ui-options :sum-options)

        update-fn (fn [key new old] (cond-> (or old {})
                                      (not (nil? new))
                                      (update key #(conj (or % #{}) new))))]
    (reduce (fn [acc [chart-index {:keys [y-option r-option sum-by-option sum-by-values use-nlp attributes] :as desc}]]
              (let [is-set-fn? #(contains? desc %)
                    characteristics-check-fn (value-check-fn ui-options sum-by-option)]
                (cond-> acc
                  (not (< (inc chart-index)
                          (count (get acc :valid-operations))))
                  (update :valid-operations conj {})
                  use-nlp
                  (check-wordcloud-options use-nlp :use-nlp use-nlp-options-check-fn)
                  attributes
                  (check-wordcloud-options attributes :attributes attribtues-options-check-fn)
                  (and (is-set-fn? :y-option)
                       (y-options-check-fn y-option))
                  (-> (update-in [:valid-operations chart-index y-option] (partial update-fn :op :y-option))
                      (update-in [:valid-operations chart-index y-option] (partial update-fn :opst-paths [chart-index :y-option])))
                  (and (is-set-fn? :y-option)
                       (or (nil? y-option)
                           (not (y-options-check-fn y-option))))
                  (update :invalid-operations conj {:op :y-option :attribute y-option :chart-index chart-index})

                  (and contains-x?
                       (x-options-check-fn x-option))
                  (-> (update-in [:valid-operations chart-index x-option] (partial update-fn :op :x-option))
                      (update-in [:valid-operations chart-index x-option] (partial update-fn :opst-paths [chart-index :x-option])))
                  (and contains-x?
                       (or (nil? x-option)
                           (not (x-options-check-fn x-option))))
                  (update :invalid-operations conj {:op :x-option :attribute x-option :chart-index chart-index})

                  (and (is-set-fn? :r-option)
                       (y-options-check-fn r-option))
                  (-> (update-in [:valid-operations chart-index r-option] (partial update-fn :op :r-option))
                      (update-in [:valid-operations chart-index r-option] (partial update-fn :opst-paths [chart-index :r-option])))
                  (and (is-set-fn? :r-option)
                       (or (nil? r-option)
                           (not (y-options-check-fn r-option))))
                  (update :invalid-operations conj {:op :r-option :attribute r-option :chart-index chart-index})

                  (and sum-by-option (sum-options-check-fn sum-by-option))
                  (-> (update-in [:valid-operations chart-index sum-by-option] (partial update-fn :op :sum-by-option))
                      (update-in [:valid-operations chart-index sum-by-option] (partial update-fn :opst-paths [chart-index :sum-by-option])))
                  (and sum-by-option (not (sum-options-check-fn sum-by-option)))
                  (update :invalid-operations conj {:op :sum-by-option :attribute sum-by-option :chart-index chart-index})

                  (and sum-by-option sum-by-values)
                  (check-sum-by-values chart-index sum-by-option sum-by-values characteristics-check-fn))))

            {:invalid-operations #{}
             :valid-operations []}
            (map-indexed vector chart-operations))))

(defn charts-check-redo
  "Checks attributes and values from Charts Operations/Selections based on ui-options
   Provides an map with the following keys:
   :valid-operations {<attribute> {:op <operations-set> :opst-paths <paths in operation-state>}]
   :invalid-operations #{{:op <operation> :attribute <attribute> :value <value, when available>}}
   (invalid-operations are used for notifications ui)"
  [ui-options operations-state]
  (let [{:keys [valid-operations invalid-operations] :as a} (check-operations ui-options operations-state)]
    {:valid-operations valid-operations
     :invalid-operations invalid-operations
     :valid-operations-state (reduce (fn [r opst-path]
                                       (cond
                                         (some #{:sum-by-values} opst-path)
                                         (update r :sum-by-values (fn [o] (conj (or o #{})
                                                                                (peek opst-path))))
                                         (some #{:use-nlp} opst-path)
                                         (update r :use-nlp (fn [o] (conj (or o #{})
                                                                          (peek opst-path))))
                                         (some #{:attributes} opst-path)
                                         (update r :attributes (fn [o] (conj (or o #{})
                                                                             (peek opst-path))))
                                         (some #{:x-option} opst-path)
                                         (assoc r :x-option (get operations-state (peek opst-path)))

                                         (get-in operations-state (into [:chart-operations] opst-path))
                                         (assoc-in r (vec (rest opst-path))
                                                   (get-in operations-state (into [:chart-operations] opst-path)))
                                         :else r))
                                     {}
                                     (reduce (fn [paths {:keys [opst-paths]}]
                                               (apply conj paths opst-paths))
                                             #{}
                                             (flatten (map vals valid-operations))))}))


