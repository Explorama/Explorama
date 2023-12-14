(ns de.explorama.shared.common.configs.overlayers
  (:require [clojure.spec.alpha :as spec]
            [de.explorama.shared.common.configs.color-scheme]
            [data-format-lib.aggregations :as dfl-agg]))

;; Example
;; (def overlay-layout-1
;;   {:id (uuid)
;;    :name "Overlay Layout 1"
;;    :timestamp (System/currentTimeMillis)
;;    :default? true
;;    :color-scheme color-scheme-3
;;    :attributes [td/fact-1]
;;    :attribute-type "number"
;;    :value-assigned [[0 1]
;;                     [1 6]
;;                     [6 20]
;;                     [20 51]
;;                     [51 1000000]]
;;    :layer-type "feature"
;;    :grouping-attribute td/country
;;    :feature-layer-id "country-coloring"
;;    :aggregate-methode-name "max-matching-color"
;;    :heatmap-extrema "local"
;;    :source "location"
;;    :target td/country})

(spec/def :overlayer/id (spec/and string?
                                  #(seq %)))
(spec/def :overlayer/name (spec/and string?
                                    #(seq %)))
(spec/def :overlayer/timestamp number?)
(spec/def :overlayer/default? boolean?)
(spec/def :overlayer/color-scheme :color-scheme/desc)
(spec/def :overlayer/attributes (spec/and vector?
                                          #(and (seq %)
                                                (every? string? %))))
(spec/def :overlayer/attribute-type #{"integer" "decimal" "number" "string"})
(spec/def :overlayer/layer-type #{:feature :movement :heatmap})
(spec/def :overlayer/value-assigned (spec/and vector?
                                              #(seq %)))
(spec/def :feature/grouping-attribute (spec/and string?
                                                #(seq %)))
(spec/def :feature/feature-layer-id (spec/and string?
                                              #(seq %)))
(spec/def :feature/aggregate-method-name (spec/and string?
                                                   #(seq %)))

(spec/def :heatmap/extrema keyword?)

(spec/def :movement/source (spec/and string?
                                     #(seq %)))
(spec/def :movement/target (spec/and string?
                                     #(seq %)))

(spec/def :overlayer/all
  (spec/keys
   :req-un [:overlayer/id
            :overlayer/name
            :overlayer/timestamp
            :overlayer/color-scheme
            :overlayer/layer-type
            :overlayer/attributes
            :overlayer/attribute-type
            :overlayer/value-assigned
            :feature/grouping-attribute
            :feature/feature-layer-id
            :feature/aggregate-method-name
            :heatmap/extrema
            :movement/source
            :movement/target]
   :opt-un [:overlayer/default?]))

(defmulti overlayer :layer-type)

(defmethod overlayer :feature [_]
  (spec/keys
   :req-un [:overlayer/id
            :overlayer/name
            :overlayer/timestamp
            :overlayer/color-scheme
            :overlayer/layer-type
            :overlayer/attributes
            :overlayer/attribute-type
            :overlayer/value-assigned
            :feature/grouping-attribute
            :feature/feature-layer-id
            :feature/aggregate-method-name]
   :opt-un [:overlayer/default?]))

(defmethod overlayer :heatmap [_]
  (spec/keys
   :req-un [:overlayer/id
            :overlayer/name
            :overlayer/timestamp
            :overlayer/color-scheme
            :overlayer/layer-type
            :overlayer/attributes
            :overlayer/attribute-type
            :overlayer/value-assigned
            :heatmap/extrema]
   :opt-un [:overlayer/default?]))

(defmethod overlayer :movement [_]
  (spec/keys
   :req-un [:overlayer/id
            :overlayer/name
            :overlayer/timestamp
            :overlayer/color-scheme
            :overlayer/layer-type
            :overlayer/attributes
            :overlayer/attribute-type
            :overlayer/value-assigned
            :movement/source
            :movement/target]
   :opt-un [:overlayer/default?]))

(spec/def :overlayer/desc (spec/multi-spec overlayer :layer-type))

(defonce relevant-overlayer-keys-cache (atom nil))

(defn- relevant-overlayer-keys []
  (if-let [overlayer-keys @relevant-overlayer-keys-cache]
    overlayer-keys
    (reset! relevant-overlayer-keys-cache
            (reduce (fn [acc keys]
                      (apply conj acc (map #(keyword (name %))
                                           keys)))
                    #{}
                    (filter vector? (spec/form :overlayer/all))))))

(defn reduce-overlayer-desc
  "Ensures that only valid overlayer keys are used"
  [overlayer-desc]
  (select-keys overlayer-desc (relevant-overlayer-keys)))

(defn is-overlayer-valid?
  "Checks if a layout-desc is valid
   - layout-desc - the layout map
   - explain? - if map is invalid then show more informations why"
  ([layout-desc]
   (is-overlayer-valid? layout-desc false))
  ([layout-desc explain?]
   (let [{:keys [id name timestamp color-scheme
                 layer-type attributes value-assigned
                 source target extrema
                 aggregate-method-name feature-layer-id
                 grouping-attribute]} layout-desc
         base-valid? (and (string? id) (seq id)
                          (string? name) (seq name)
                          (number? timestamp))
         agg-desc (get dfl-agg/descs (keyword aggregate-method-name))
         attributes-valid? (cond
                             (nil? agg-desc) (and (vector? attributes)
                                                  (seq attributes)
                                                  (every? (fn [v]
                                                            (or (string? v)
                                                                (keyword? v)))
                                                          attributes))
                             (:need-attribute? agg-desc) (and (vector? attributes)
                                                              (seq attributes)
                                                              (every? (fn [v]
                                                                        (or (string? v)
                                                                            (keyword? v)))
                                                                      attributes))
                             :else true)
         color-scheme-valid? (spec/valid? :color-scheme/desc color-scheme)]
     (and base-valid?
          (case layer-type
            :feature (and color-scheme-valid?
                          attributes-valid?
                          (and (vector? value-assigned) (seq value-assigned))
                          (string? grouping-attribute) (seq grouping-attribute)
                          (string? feature-layer-id) (seq feature-layer-id)
                          (string? aggregate-method-name) (seq aggregate-method-name))
            :heatmap (or (= extrema :global)
                         (and (= extrema :local) attributes-valid?))
            :movement (and attributes-valid?
                           (string? source) (seq target)
                           (string? source) (seq target)
                           (not= source target))
            false)))))
