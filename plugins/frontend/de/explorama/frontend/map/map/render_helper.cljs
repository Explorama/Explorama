(ns de.explorama.frontend.map.map.render-helper
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.utils :refer [rgb->hex]]
            [re-frame.core :as re-frame]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.frontend.common.i18n :as i18n]))

(defn show-event-popup-fn-wrapper [db frame-id event-id event-color clicked-position]
  (let [{:keys [attributes field-assignments]} (first (get-in db (geop/selected-marker-layouts frame-id)))
        render? (fi/call-api [:interaction-mode :render-db-get?]
                             db)]
    (when render?
      (map-api/show-popup frame-id
                          clicked-position
                          {:data (map-api/get-event-data frame-id event-id)
                           :title-color event-color
                           :title-attributes attributes
                           :display-attributes field-assignments}))
    {}))

(defn show-overlayer-popup-fn-wrapper [db frame-id overlayer-id feature-properties clicked-position]
  (let [{:keys [attributes]} (some (fn [{:keys [name] :as desc}]
                                     (when (= name overlayer-id)
                                       desc))
                                   (get-in db geop/overlayers))
        display-attrs (if (= attributes "all")
                        :all
                        attributes)
        render? (fi/call-api [:interaction-mode :render-db-get?]
                             db)]
    (when render?
      (map-api/show-popup frame-id
                          clicked-position
                          {:data feature-properties
                           :display-attributes display-attrs}))
    {}))

(defn show-feature-layer-popup-fn-wrapper [db frame-id area-feature-id feature-properties clicked-position]
  (let [{:keys [feature-layer-id aggregate-method]} (get-in db (conj (geop/selected-feature-layers frame-id)
                                                                     area-feature-id))
        {data-key-properties :feature-properties} (get-in db (geop/feature-color-layer feature-layer-id))
        data-key (select-keys feature-properties (keys data-key-properties))
        feature-data (map-api/get-feature-data frame-id area-feature-id)
        {data-base :feature-names
         data-attr :attribute
         data-val :value
         title-color :color} (get-in feature-data [:data-set data-key])
        render? (fi/call-api [:interaction-mode :render-db-get?]
                             db)
        color (if title-color
                title-color
                (rgb->hex config/default-area-color))
        data-attr (or (when-let [agg-label (get-in dfl-agg/descs [(keyword data-attr) :label])]
                        (i18n/translate db agg-label))
                      data-attr)]
    (when render?
      (map-api/show-popup frame-id
                          clicked-position
                          {:data (assoc data-base
                                        data-attr data-val)
                           :title-attributes [data-attr]
                           :title-color color
                           :display-attributes :all
                           :area-feature-id area-feature-id}))
    {}))

(defn add-render-done-listener [db frame-id]
  (let [{:keys [event-id event-color
                overlayer-id feature-properties
                area-feature-id clicked-position]
         {:keys [zoom center]} :view-position} (get-in db (geop/popup-desc frame-id))
        highlighted-markers (get-in db (geop/highlighted-markers frame-id))
        {last-highlighted-marker "id"} (when (seq highlighted-markers)
                                         (peek highlighted-markers))
        {logged-zoom :zoom
         logged-center :center} (get-in db (geop/view-position frame-id))
        clustering? (get-in db (geop/cluster-marker? frame-id))]
    (map-api/add-onetime-render-done-listener frame-id
                                              (fn []
                                                (cond
                                                  (and (seq event-id)
                                                       clustering?) (do (map-api/move-to frame-id zoom center)
                                                                        (map-api/select-cluster-with-marker frame-id (second event-id))
                                                                        (show-event-popup-fn-wrapper db frame-id event-id event-color clicked-position))
                                                  (and (seq last-highlighted-marker)
                                                       clustering?) (do (map-api/move-to frame-id logged-zoom logged-center)
                                                                        (map-api/select-cluster-with-marker frame-id last-highlighted-marker))
                                                  (seq event-id) (do
                                                                   (map-api/move-to frame-id zoom center)
                                                                   (show-event-popup-fn-wrapper db frame-id event-id event-color clicked-position))
                                                  (seq overlayer-id)
                                                  (do
                                                    (map-api/move-to frame-id zoom center)
                                                    (show-overlayer-popup-fn-wrapper db frame-id overlayer-id feature-properties clicked-position))
                                                  (seq area-feature-id)
                                                  (do
                                                    (map-api/move-to frame-id zoom center)
                                                    (show-feature-layer-popup-fn-wrapper db frame-id area-feature-id feature-properties clicked-position))
                                                  (and logged-zoom
                                                       logged-center) (map-api/move-to frame-id logged-zoom logged-center)
                                                  :else (map-api/move-to-data frame-id))
                                                (re-frame/dispatch (fi/call-api :render-done-event-vec
                                                                                frame-id config/default-namespace))))
    (map-api/render-map frame-id)))