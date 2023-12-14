(ns de.explorama.backend.map.overlayers
  (:require [clojure.string :as string]
            [data-format-lib.aggregations :as dfl-agg]
            [data-format-lib.operations :as dfl-op]
            [de.explorama.shared.common.data.attributes :as dattrs]
            [de.explorama.shared.common.data.locations :as locations]
            [de.explorama.shared.map.config :as config-shared-map]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [taoensso.timbre :refer [debug error]]
            [de.explorama.backend.common.layout :as layout]))


(defn create-overlayers [_available-overlayer _ds-acs dim-info datasource-set]
  (let [ranges (layout/layout-ranges dim-info datasource-set)]
    (case (first ranges)
      :num
      (let  [[_ attribute attribute-range] ranges]
        [{:id (str "generated-default-overlay-feature-layout-" (cljc-uuid))
          :name "Overlayer Layout 1"
          :timestamp 1627662367930
          :default? true
          :layer-type :feature
          :color-scheme "colorscale3"
          :attributes [attribute]
          :attribute-type "number"
          :value-assigned attribute-range
          :grouping-attribute "country"
          :feature-layer-id "country-coloring"
          :aggregate-method-name "sum"}
         {:id (str "generated-default-overlay-movment-layout-" (cljc-uuid))
          :name "Overlayer Layout 3"
          :timestamp 1627662367930
          :default? true
          :layer-type :movement
          :color-scheme "colorscale3"
          :attributes [attribute]
          :attribute-type "number"
          :value-assigned attribute-range
          :source             "country"
          :target             "location"}])
      :datasources
      [{:id (str "generated-default-overlay-feature-layout-" (cljc-uuid))
        :name "Overlayer Layout 1"
        :timestamp 1627662367930
        :default? true
        :layer-type :feature
        :color-scheme "colorscale3"
        :attributes ["datasource"]
        :attribute-type "number"
        :value-assigned (second ranges)
        :grouping-attribute "country"
        :feature-layer-id "country-coloring"
        :aggregate-method-name "sum"}
       {:id (str "generated-default-overlay-movment-layout-" (cljc-uuid))
        :name "Overlayer Layout 3"
        :timestamp 1627662367930
        :default? true
        :layer-type :movement
        :color-scheme "colorscale3"
        :attributes ["datasource"]
        :attribute-type "number"
        :value-assigned (second ranges)
        :source             "country"
        :target             "location"}]
      :else
      [])))

(defn- location-valid? [location]
  (when (seq location)
    (if (number? (first location))
      (every? number? location)
      (every? location-valid? location))))

(defn- non-empty-attributes [& attributes]
  (into [:and]
        (map (fn [attr] {:data-format-lib.filter/op :non-empty,
                         :data-format-lib.filter/prop attr
                         :data-format-lib.filter/value nil}))
        attributes))

(defn- feature-layer-definiton [feature-id]
  (->> (:overlayers (config-shared-map/extern-config))
       (filter (fn [{:keys [id]}]
                 (= id feature-id)))
       first))

(defn- get-mapped-name [layer-id attribute input-name]
  (get-in (feature-layer-definiton layer-id)
          [:name-mapping attribute input-name]
          input-name))

(defn- attr-string? [test-string]
  (and (string? test-string)
       (string/starts-with? test-string "#")))

(defn- grouping-attributes [layer-id]
  (let [{:keys [grouping-attributes]} (feature-layer-definiton layer-id)]
    grouping-attributes))

(defn- feature-properties->feature-values [feature-layer-id mapped-attribute-values]
  (let [feature-properies (:feature-properties (feature-layer-definiton feature-layer-id))]
    (into {}
          (map (fn [[feature-prop [join-fn & feature-vals]]]
                 (let [feature-values (mapv (fn [feature-val]
                                              (if (config-shared-map/attr-string? feature-val)
                                                (get mapped-attribute-values
                                                     (string/replace-first feature-val "#" ""))
                                                feature-val))
                                            feature-vals)]
                   [feature-prop
                    (case join-fn
                      :str (string/join feature-values))])))
          feature-properies)))

(defn number-attr? [{:keys [result-type]}]
  (= result-type "number"))

(defn attribute-values [attribute country-data]
  (filter identity
          (map #(dattrs/value % attribute) country-data)))

(defn find-matching-attribute [attributes datapoint]
  (if (some #{"number-of-events"} attributes)
    "number-of-events"
    (let [data-attributes (keys datapoint)]
      (first (filter (fn [attr] (some #{attr} data-attributes)) attributes)))))

(defn- feature-attribute-vals->feature-key [feature-attribute-vals feature-layer-id]
  (let [mapped-attr-values (into {}
                                 (map (fn [[attr val]]
                                        [attr
                                         (get-mapped-name feature-layer-id
                                                          attr
                                                          val)]))
                                 feature-attribute-vals)]
    (feature-properties->feature-values feature-layer-id
                                        mapped-attr-values)))

(defn- aggregate-method->dfl-op [parent-op aggregate-method attribute]
  (let [{op-desc :dfl-op
         :as method-desc} (get dfl-agg/descs aggregate-method)]
    (cond
      (= attribute "number-of-events")
      (conj (get-in dfl-agg/descs [:number-of-events :dfl-op])
            {}
            parent-op)
      (number-attr? method-desc)
      (conj op-desc
            {:attribute attribute}
            parent-op)
      :else
      (let [op [:sort-by-frequencies {}
                [:select {:attribute attribute}
                 parent-op]]]
        (case aggregate-method
          :first-matching-color op
          :last-matching-color op
          :max-matching-color  [:take-first {} op]
          :min-matching-color  [:take-last {} op])))))

(defn- layout->dfl-op-desc [attribute
                            group-attrs
                            {:keys [aggregate-method] :as layout}]
  [:apply-layout {:layouts [layout]
                  :reverse-color? (= aggregate-method :last-matching-color)}
   [:heal-event
    {:policy :merge
     :descs [{:attribute attribute}]}
    (aggregate-method->dfl-op
     [:group-by {:attributes group-attrs}
      "di1"]
     aggregate-method
     attribute)]])

(defn- feature-coloring-data [data group-attrs attribute {:keys [feature-layer-id aggregate-method] :as layer}]
  (let [method-desc (case aggregate-method
                      (:first-matching-color
                       :last-matching-color
                       :max-matching-color
                       :min-matching-color) {:need-attribute? true}
                      (get dfl-agg/descs aggregate-method))
        attribute (if (:need-attribute? method-desc)
                    attribute
                    (name (:attribute method-desc)))
        aggregated-data (dfl-op/perform-operation {"di1" data}
                                                  {}
                                                  (layout->dfl-op-desc attribute group-attrs layer))]
    (persistent!
     (reduce (fn [acc d]
               (let [input-k (select-keys d group-attrs)
                     attribute-value (get d attribute)
                     value
                     #_{:clj-kondo/ignore [:type-mismatch]}
                     (cond (= aggregate-method :max-matching-color) attribute-value
                           (= aggregate-method :min-matching-color) attribute-value
                           (and (not (number-attr? method-desc))
                                (> (count attribute-value) config-shared-map/explorama-max-popup-values))
                           (string/join ", " (conj (vec (take config-shared-map/explorama-max-popup-values attribute-value)) "..."))
                           :else attribute-value)
                     color {:color (get-in d ["layout" "color"])
                            :opacity config-shared-map/explorama-overlayer-opacity
                            :value value}]
                 (if (and input-k
                          (not-empty input-k)
                          (every? identity (vals input-k))
                          (or (string? input-k)
                              (every? identity input-k))
                          val color)
                   (let [mapped-k (feature-attribute-vals->feature-key input-k
                                                                       feature-layer-id)]
                     (assoc! acc mapped-k (assoc color
                                                 :attribute attribute
                                                 :feature-names input-k)))
                   acc)))
             (transient {})
             aggregated-data))))

(defn feature-coloring-layer-calc [{:keys [attributes id name
                                           feature-layer-id] :as layer}
                                   data]
  (let [attribute (find-matching-attribute attributes (first data))
        grouping-attrs (->> (grouping-attributes feature-layer-id)
                            (mapv dattrs/access-key))
        layer-data (feature-coloring-data data grouping-attrs attribute layer)]
    (when (not-empty layer-data)
      {:layer-id id
       :feature-layer-id feature-layer-id
       :name name
       :data-set layer-data
       :type :feature})))

(defn- location-for-event [event]
  (let [location (dattrs/value event "location")
        country (dattrs/value event "country")]
    (cond
      (location-valid? location)
      location

      (and (string? country)
           (location-valid? (locations/center-for-name country)))
      [(locations/center-for-name country)]

      (and (vector? country)
           (seq country))
      (let [country-centers (->> country
                                 (map locations/center-for-name)
                                 (filterv location-valid?))]
        (when (seq country-centers)
          country-centers))
      :else nil)))

(defn marker-layer-calc [layouts
                         data]
  (let [not-found-default {:color "#5c5b5b"
                           :fillColor "#dddddd"
                           :fill true
                           :stroke true
                           :weight 3}
        opacity (double (/ config-shared-map/explorama-marker-opacity 100))
        applied-layout (dfl-op/perform-operation {"di1" (vec data)}
                                                 nil
                                                 [:apply-layout
                                                  {:layouts layouts}
                                                  [:filter "f1" "di1"]])
        base-style (merge
                    not-found-default
                    {:stroke false
                     :fill true
                     :radius config-shared-map/explorama-marker-radius
                     :fillOpacity opacity})]
    (into {}
          (comp (map (fn [cur-event]
                       (let [{ev-id "id"
                              ev-bucket "bucket"
                              {style-color "color"} "layout"} cur-event
                             location (location-for-event cur-event)]
                         (if location
                           [ev-id
                            [[ev-bucket ev-id]
                             location
                             (assoc base-style
                                    :fillColor style-color
                                    :color  style-color)]]
                           nil))))
                (filter identity))
          applied-layout)))

(defn get-attribute [attribute datapoint]
  (dattrs/value datapoint attribute))

(defn heatmap-layer-calc [{:keys [attributes extrema invalid? id name]} data]
  (when-not invalid?
    (let [attribute (find-matching-attribute attributes (first data))
          attribute-key (dattrs/access-key attribute)
          data-set (persistent!
                    (reduce (fn [acc d]
                              (let [locs (dattrs/value d "location")
                                    [center-lat center-lng] (locations/center-for-name (dattrs/value d "country"))
                                    attr-val (get-attribute attribute-key d)]
                                (cond
                                  (and (or (= :global extrema)
                                           attr-val)
                                       (location-valid? locs))
                                  (reduce (fn [acc [lat lon]]
                                            (if (location-valid? [lat lon])
                                              (conj! acc
                                                     (cond-> {:lat lat
                                                              :lng lon}
                                                       attribute-key (assoc attribute-key attr-val)))
                                              acc))
                                          acc
                                          locs)
                                  (and  (or (= :global extrema)
                                            attr-val)
                                        (empty? locs)
                                        (location-valid? [center-lat center-lng]))
                                  (conj! acc
                                         (cond-> {:lat center-lat
                                                  :lng center-lng}
                                           attribute-key (assoc attribute-key attr-val)))

                                  :else acc)))
                            (transient [])
                            data))]
      (when (not-empty data-set)
        {:layer-id id
         :name name
         :type :heatmap
         :data-set {:data data-set}
         :config {:value-field attribute-key
                  :use-local-extrema (= extrema :local)}}))))

(def range-min 1)
(def range-max 20)

(defn position-for-attribute [attribute datapoint]
  (let [attribute-val (get-attribute attribute datapoint)]
    (cond
      (vector? attribute-val) attribute-val
      (and attribute-val
           (string? attribute-val)) (locations/center-for-name attribute-val)
      :else nil)))

(defn- movement-arrow-operation [attribute source target]
  (let [aggr-op (if (= attribute "number-of-events")
                  (get-in dfl-agg/descs [:number-of-events :dfl-op])
                  (get-in dfl-agg/descs [:sum :dfl-op]))]
    [:normalize
     {:attribute attribute
      :result-name "weight"
      :range-min range-min
      :range-max range-max
      :all-data? false}
     [:heal-event
      {:policy :merge
       :descs [{:attribute attribute}]}
      (conj aggr-op
            {:attribute attribute}
            [:group-by
             {:attributes [source target]
              :mode :keep}
             [:filter "f1" "di1"]])]]))

(defn movement-layer-calc [{:keys [attributes source target id name] :as layer} data]
  (debug "movement-layer-calc" layer)
  (let [attribute (find-matching-attribute attributes (first data))
        data (dfl-op/perform-operation {"di1" data}
                                       {"f1" (non-empty-attributes source target)}
                                       (movement-arrow-operation attribute source target))
        data-set (persistent!
                  (reduce (fn [acc d]
                            (let [attr-val (get-attribute attribute d)
                                  weight (get d "weight")
                                  source-location (position-for-attribute source d)
                                  target-location (position-for-attribute target d)]
                              (if (and (location-valid? source-location)
                                       (location-valid? target-location))
                                (conj! acc
                                       {:id (str source-location "-" target-location)
                                        :attribute attribute
                                        :from source-location
                                        :to target-location
                                        :weight (double weight)
                                        :original attr-val})
                                acc)))
                          (transient [])
                          data))]
    (when (not-empty data)
      {:layer-id id
       :name name
       :type :movement
       :data-set data-set})))

(defn calc-layer [{:keys [type] :as layer} data not-too-much?]
  (cond
    (= type :feature) (feature-coloring-layer-calc layer data)
    (= type :heatmap) (heatmap-layer-calc layer data)
    (= type :movement) (when not-too-much? (movement-layer-calc layer data))
    :else (do
            (error "Unknown overlayer type: " type)
            nil)))

(defn layers-data [layers data not-too-much?]
  (into {}
        (comp (map (fn [{:keys [id] :as layer}]
                     [id (when layer
                           (try
                             (calc-layer layer data not-too-much?)
                             (catch #?(:clj Throwable :cljs :default) e
                               (error e)
                               nil)))]))
              (filter second))
        layers))

(def ?inc (fnil inc 0))

(def ?conj (fnil conj []))

(defn has-valid-location? [data-point]
  (or (-> (get data-point "location")
          (location-valid?)
          (boolean))
      (-> (get data-point "country")
          (locations/center-for-name)
          (location-valid?)
          (boolean))))

(defmulti can-be-rendered? (fn [type _ _ _]
                             type))

(defmethod can-be-rendered? :marker [_ attrs _ data-point]
  (and (has-valid-location? data-point)
       (or (nil? attrs)
           (boolean (some identity (map (partial dattrs/value data-point) attrs))))))

(defmethod can-be-rendered? :feature [_ attrs {:keys [feature-layer-id]} data-point]
  (let [grouping-attrs (->> (grouping-attributes feature-layer-id)
                            (mapv dattrs/access-key))
        attrs-val-fn (partial dattrs/value data-point)]
    (and
     (seq grouping-attrs)
     (every? attrs-val-fn grouping-attrs)
     (or (some identity (map attrs-val-fn attrs))
         (some #(= % "number-of-events") attrs)))))

(defmethod can-be-rendered? :heatmap [_ attrs {:keys [extrema]} data-point]
  (and (has-valid-location? data-point)
       (or (= :global extrema)
           (boolean (some identity (map (partial dattrs/value data-point) attrs))))))

(defmethod can-be-rendered? :movement [_ attrs {:keys [source target]} data-point]
  (let [attrs-val-fn (partial dattrs/value data-point)]
    (and
     (attrs-val-fn source)
     (position-for-attribute source data-point)
     (attrs-val-fn target)
     (position-for-attribute target data-point)
     (boolean (or (some identity (map attrs-val-fn attrs))
                  (some #(= % "number-of-events") attrs))))))

(defmethod can-be-rendered? :default [_ attrs _ data-point]
  (boolean (some identity (map (partial dattrs/value data-point) attrs))))

(defn renderable-layers [layers data]
  (let [att->lay (-> (reduce (fn [agg {:keys [attributes type feature-layer-id extrema id source target aggregate-method]}]
                               (update agg [attributes type {:feature-layer-id feature-layer-id
                                                             :extrema extrema
                                                             :source source
                                                             :target target
                                                             :aggregate-method aggregate-method}]
                                       ?conj id))
                             {}
                             layers))]
    (into #{}
          (comp
           (filter (fn [[attr-type count]]
                     (and attr-type (pos-int? count))))
           (mapcat (comp att->lay first)))
          (reduce (fn [agg data-point]
                    (reduce (fn [agg [attrs type extras]]
                              (if (can-be-rendered? type attrs extras data-point)
                                (assoc agg [attrs type extras] 1)
                                agg))
                            agg
                            (keys att->lay)))
                  {}
                  data))))

(defn calc-layers [data not-too-much? layers]
  (debug "Calc-layers" (vec layers))
  (let [renderable? (renderable-layers layers data)
        layer-data (layers-data (filter (comp renderable? :id)
                                        layers)
                                data
                                not-too-much?)]
    layer-data))

(defn displayable-data
  "Returns how many datapoints have a valid location (location or country attribute) to display on the map."
  [datapoints]
  (count (filterv location-for-event datapoints)))
