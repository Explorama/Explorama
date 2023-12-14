(ns de.explorama.backend.expdb.legacy.search.direct-search
  (:require [de.explorama.backend.expdb.legacy.search.attribute-characteristics.graph :as graph]
            [clojure.string :as string]
            [de.explorama.shared.common.data.attributes :as attrs]))

(defn filter-func [lower-case-query group-query [_ [_ [_ node-value]]]]
  (or (= lower-case-query group-query)
      (and node-value (string/includes? (string/lower-case node-value) lower-case-query))))

(defn bucket-search [bucket node-type query group-key group-query]
  ;TODO r1/expdb reimplement this
  #_(->> (get (graph/ac-groups bucket)
              node-type)
         (#(get % group-key))
         (select-keys (graph/ac-nodes-map-inv bucket))
         (filter (partial filter-func
                          query
                          group-query))
         (map (fn [[_ node]] ;=> node [node-type [attribute-label node-value]]
                node))))

(defn grouped-search [node-type query group-key group-query]
  ;TODO r1/expdb reimplement this
  #_(let [lower-case-query (string/lower-case query)]
      (->> (reduce (fn [res bucket]
                     (apply conj
                            res
                            (bucket-search bucket node-type lower-case-query group-key group-query)))

                   #{}
                   (keys config/bucket-config))
           vec)))

(defn datasource-search [query]
  ;TODO r1/expdb reimplement this
  #_(let [lower-case-query (string/lower-case query)]
      (->> (graph/all-ac-nodes)
           (filterv (fn [[node-type [_ node-value]]]
                      (and (= node-type attrs/datasource-node)
                           (or (= lower-case-query attrs/datasource-attr)
                               (and node-value
                                    (string/includes? (string/lower-case node-value)
                                                      lower-case-query)))))))))
