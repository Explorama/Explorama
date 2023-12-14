(ns de.explorama.backend.configuration.ac-api
  (:require [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]))

;;-------------- Maybe do it better like this --------------
(defonce acs-state (atom nil))
(defonce ac-ranges (atom nil))
(defonce acs-types (atom nil))

(defn ac-service-url [service route]
  (str (get-in service [:ac-service :url]) route))

(defn- normalize-attr-type [attr-type]
  (case attr-type
    "decimal" :number
    "integer" :number
    "year" :date
    "day" :date
    "month" :date
    "string" :string
    "notes" :string
    "location" :location
    ;;external-ref, notes, etc. not handled
    nil))

(def type-chars #{"integer" "decimal"})

(def attribute-blacklist
  "Blacklist to define which ACs should not send to Clients. This collection
  contains [<nodetype> <attribute>] tuples."
  #{}) ;["location" "Context"]})

(defn- attr-values [attribute-label]
  (get @acs-state attribute-label))

(defn- check-characteristics [attrs]
  (reduce (fn [acc [attr-label :as attr]]
            (let [chars (get @acs-state attr)
                  chars-count (count chars)
                  characteristics? (boolean
                                    (and (vector? chars)
                                         (seq chars)
                                         (or (> chars-count 1)
                                             (and (= chars-count 1)
                                                  (not (type-chars (first chars)))))))]

              (assoc-in acc
                        [attr-label :std :characteristics?]
                        characteristics?)))
          attrs
          attrs))

(defn- calc-attribute-types [attrs]
  (reduce (fn [acc [[attribute-label node-type] attr-type]]
            (if-let [attr-type (normalize-attr-type attr-type)]
              (cond-> acc
                (and attribute-label attr-type)
                (assoc attribute-label
                       {:std {:type attr-type
                              :node-type node-type}}))
              acc))
          {"date" {:std {:type :date}}
           "location" {:std {:type :location}}
           "indicator" {:std {:characteristics? false, :node-type "Fact", :type :number}}}
          attrs))

(defn reset-acs []
  (let [attrs (-> (ac-api/attribute-types {:blocklist attribute-blacklist})
                  (calc-attribute-types))]
    (reset! acs-state
            (ac-api/attribute-values {:attributes attrs
                                      :blocklist attribute-blacklist}))
    (reset! acs-types (check-characteristics attrs))
    (reset! ac-ranges (ac-api/attribute-ranges {}))
    nil))

(defn init-acs []
  (reset-acs))

(defn attribute-types []
  @acs-types)

;;--------------------------------------------

(defn initialize [{:keys [client-callback]} _]
  (init-acs)
  (client-callback
   {:types (attribute-types)}))

(defn attr-characteristics [{:keys [client-callback]} [request-attrs]]
  (let [attrs-keys (select-keys @acs-types request-attrs)
        response (into {} (map (fn [[attr {{:keys [characteristics?] :as std} :std}]]
                                 (let [node-ranges (if characteristics?
                                                     (get @acs-state [attr {:std (select-keys std [:type :node-type])}])
                                                     (let [{:keys [min max]} (get @ac-ranges attr)]
                                                       [min max]))]
                                   [attr node-ranges]))
                               attrs-keys))]
    (client-callback response)))

(defn available-datasources [{:keys [client-callback]} [bucket]]
  (let [attributes [attrs/datasource-attr attrs/datasource-node]]
    (client-callback (-> (ac-api/attribute-values {:bucket bucket
                                                   :attributes [attributes]})
                         (get attributes)))))

