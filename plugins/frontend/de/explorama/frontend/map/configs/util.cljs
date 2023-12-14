(ns de.explorama.frontend.map.configs.util
  (:require [de.explorama.shared.common.data.attributes :as attrs]))


(defn feature-layer-config [basic-infos aggregate-method-name value-assigned attribute-type color-scheme feature-layer-id]
  (merge
   basic-infos
   {:color-scheme color-scheme
    :value-assigned value-assigned
    :feature-layer-id feature-layer-id
    :attribute-type attribute-type
    :aggregate-method (keyword aggregate-method-name)}))

(defn marker-layer-config [basic-infos value-assigned attribute-type color-scheme full-opacity? card-scheme field-assignments]
  (merge
   basic-infos
   {:color-scheme color-scheme
    :value-assigned value-assigned
    :full-opacity? full-opacity?
    :attribute-type attribute-type
    :card-scheme card-scheme 
    :field-assignments field-assignments}))

(defn translate-layer [layer]
  (let [{:keys [attributes source target
                layer-type aggregate-method-name
                attribute-type
                value-assigned color-scheme
                extrema name feature-layer-id datasources temporary?
                card-scheme field-assignments]
         overlayer-id :id} layer
        full-opacity? nil ;(not (get-in db (geop/disable-cluster-marker? frame-id)))
        layer-type (keyword (or layer-type :marker))
        basic-infos {:name            name
                     :id              overlayer-id
                     :type            layer-type
                     :attributes      (mapv attrs/access-key attributes)
                     :datasources datasources}
        feature? (#{:country :feature} layer-type)]
    (if layer-type ;assumption is when layer-type is not set, the layer is already translated 
      (cond-> basic-infos
        temporary? (assoc :temporary? true)
        feature? (feature-layer-config aggregate-method-name value-assigned attribute-type color-scheme feature-layer-id)
        (= layer-type :marker) (marker-layer-config value-assigned attribute-type color-scheme full-opacity? card-scheme field-assignments)
        (= layer-type :heatmap) (merge {:extrema extrema})
        (= layer-type :movement) (merge {:source source
                                         :target target}))
      layer)))

(defn translated-layer->raw [{:keys [type aggregate-method] :as layer}]
  (cond-> layer
    (vector? layer) second
    :always (assoc :timestamp (js/Date.now))
    ;type is then one overlayer (feature, heatmap, movement)
    (not= type :marker) (assoc :grouping-attribute "country"
                               :layer-type type
                               :aggregate-method-name (if (keyword? aggregate-method)
                                                        (name aggregate-method)
                                                        aggregate-method))))

(defn raw-layer->translate-layer [raw-layers]
  (mapv translate-layer raw-layers))