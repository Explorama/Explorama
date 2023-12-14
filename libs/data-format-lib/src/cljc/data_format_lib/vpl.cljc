(ns data-format-lib.vpl)

(defn function-metadata [general & kvs]
  (reduce (fn [acc [k meta-data v]]
            (let [base-meta (assoc meta-data :key k)
                  inherited-meta (apply merge
                                        (into []
                                              (comp (map (fn [[[path con] addition]]
                                                           (if (= con (get-in base-meta path))
                                                             addition
                                                             nil)))
                                                    (filter identity))
                                              (dissoc general [])))]
              (assoc acc k
                     (with-meta
                       v
                       (merge-with
                        (fn [a b]
                          (if (and (map? a)
                                   (map? b))
                            (merge a b)
                            b))
                        (get general [])
                        inherited-meta
                        base-meta)))))
          {}
          (partition-all 3 kvs)))

(comment
  (meta (get (function-metadata {:join? :boolean
                                 :join-fully? :boolean
                                 :attribute :attribute}
                                :heal-event
                                {:types {}
                                 :parameters {}}
                                (fn [instance _ {:keys [join? join-fully?]} di-groups]
                                  [instance _ {:keys [join? join-fully?]} di-groups]))
             :heal-event)))
