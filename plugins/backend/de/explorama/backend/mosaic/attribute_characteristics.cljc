(ns de.explorama.backend.mosaic.attribute-characteristics
  (:require [clojure.string :as string]
            [de.explorama.backend.frontend-api :as fapi]
            [de.explorama.backend.mosaic.ac-api :as ac-api]
            [de.explorama.shared.mosaic.ws-api :refer [update-acs]]
            [taoensso.timbre :refer [debug]]))

(defonce ui-options (atom nil))

(def conj-empty (fnil conj []))
(def conj-empty-set (fnil conj #{}))

(defn- attribute-type [node-label attribute node-value]
  (cond (and (= node-label "Fact")
             (= (string/lower-case attribute) "notes"))
        "fulltext"
        (= node-label "Notes")
        "fulltext"
        (= node-label "Date")
        "date"
        (= node-label "External-ref")
        nil
        (and (= node-label "Fact")
             (#{"integer" "decimal" "double"} node-value)) "number"
        (and (= node-label "Fact")
             (= "boolean" node-value)) node-value
        :else
        "string"))

(defn- extract-characteristic-options [acc [node-label [attribute node-value]]]
  (if (and (not (and (= node-label "Fact")
                     (= (attribute-type node-label attribute node-value)
                        "number")))
           node-value)
    (update-in acc
               [:attr-options attribute]
               conj-empty-set
               node-value)
    acc))

(defn- ds-tree [acc datasource [node-label attribute]]
  (update-in acc
             [:ds-tree [node-label attribute]]
             conj-empty-set
             datasource))

(defn- client-acs [acc [node-label attribute value]]
  (let [type (attribute-type node-label attribute value)
        name (if (and (= node-label "Date")
                      (= attribute "day"))
               "date"
               attribute)
        ac-desc {:name name
                 :display-name (if (= type "number")
                                 (str name " (" type ")")
                                 name)
                 :type type
                 :node-label node-label
                 :attribute attribute}]
    (cond
      (and (#{"year" "month"} name)
           (= "Date" node-label)) acc
      (or (and (= name "notes")
               (= type "fulltext"))
          (and (= attribute "location")
               (= node-label "Context"))) (update acc :obj-ac conj-empty-set ac-desc)
      :else (-> acc
                (update :obj-ac conj-empty-set ac-desc)
                (update :color-ac conj-empty-set ac-desc)))))

(defn- extract-ac-node-infos [acc datasource ac-nodes]
  (reduce (fn [acc node]
            (-> acc
                (extract-characteristic-options node)
                (ds-tree datasource node)
                (client-acs node)))
          acc
          ac-nodes))

(defn- add-info [acs client-options]
  (mapv (fn [{:keys [node-label attribute] :as ac}]
          (-> ac
              (dissoc :node-label :attribute)
              (assoc :info (sort (into '()
                                       (get-in client-options [:ds-tree [node-label attribute]]))))))
        acs))

(defn- workaround-indicator
  "Adding extra ac-infos for indicator related attributes."
  [client-options]
  (-> client-options
      (update :obj-ac
              (fn [val]
                (conj val
                      {:name "indicator"
                       :display-name "indicator (number)"
                       :type "number"}
                      {:name "indicator-type"
                       :display-name "indicator-type"
                       :type "string"})))
      (update :color-ac
              (fn [val]
                (conj val
                      {:name "indicator"
                       :display-name "indicator (number)"
                       :type "number"}
                      {:name "indicator-type"
                       :display-name "indicator-type"
                       :type "string"})))
      (update-in [:attr-options "indicator-type"]
                 (fn [val]
                   (let [o-val (cond
                                 (vector? val) val
                                 val [val]
                                 :else [])]
                     (conj o-val
                           "average"
                           "division"
                           "sum"
                           "min"
                           "max"
                           "normalize"
                           "indicator-rank"))))))

(defn- create-ui-options []
  (let [all-datasources (mapcat (fn [[datasource-attr values]]
                                  (mapv #(vector datasource-attr %) values))
                                (ac-api/datasource-options))
        all-datasource-acs (reduce (fn [acc [datasource-attr datasource]]
                                     (update acc
                                             datasource
                                             (fn [val]
                                               (apply conj-empty
                                                      val
                                                      (ac-api/datasource->attributes datasource-attr datasource)))))
                                   {}
                                   all-datasources)
        client-options (reduce (fn [acc [datasource ac-nodes]]
                                 (-> acc
                                     (assoc-in
                                      [:ds-acs datasource]
                                      (->> (conj ac-nodes
                                                 ["Date" "date" "date"]
                                                 ["Notes" "notes" "fulltext"])
                                           (map (fn [[node-label attribute node-value]]
                                                  {:name attribute
                                                   :key attribute
                                                   :label node-label
                                                   :type (attribute-type node-label attribute node-value)}))
                                           set
                                           vec))
                                     (extract-ac-node-infos datasource
                                                            (conj ac-nodes ["Notes" "notes" "fulltext"]))))
                               {}
                               all-datasource-acs)]
    (-> client-options
        (update :obj-ac
                add-info
                client-options)
        (update :color-ac
                add-info
                client-options)
        (update :ds-tree
                (fn [nodes]
                  (mapv (fn [[[_ attribute] dss]]
                          {:name attribute
                           :childs (mapv #(hash-map :name %) dss)})
                        nodes)))
        workaround-indicator)))

(defn obj-acs []
  (get @ui-options :obj-ac))

(defn color-acs []
  (get @ui-options :color-ac))

(defn ds-acs []
  (get @ui-options :ds-acs))

(defn datasource-tree []
  (get @ui-options :ds-tree))

(defn possible-attribute-values [attr-name]
  (get-in @ui-options [:attr-options attr-name]))

(defn populate-ui-options! []
  (debug "creating ui-options based on current acs")
  (reset! ui-options (create-ui-options))
  (debug "broadcast acs")
  (fapi/broadcast [update-acs
                   {:acs (ds-acs)
                    :obj-acs (obj-acs)
                    :color-acs (color-acs)}])
  (debug "ui-options created."))
