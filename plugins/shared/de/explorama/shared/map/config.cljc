(ns de.explorama.shared.map.config
  (:require [clojure.string :as str]
            [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :map)
(def plugin-string (name plugin-key))

(defn- read-name-mappings [{:keys [overlayers] :as conf}]
  (assoc conf
         :overlayers
         (mapv (fn [{:keys [name-mapping id] :as overlayer}]
                 (if (seq name-mapping)
                   (assoc overlayer
                          :name-mapping
                          nil)
                          ;;TODO r1/config
                          ;; (try (edn/read-string (slurp name-mapping))
                          ;;      (catch Exception e
                          ;;        (timbre/error e (format "Error while reading name-mapping for overlayer %s" id)))))
                   overlayer))
               overlayers)))

(defn attr-string? [test-string]
  (and (string? test-string)
       (str/starts-with? test-string "#")))

(defn- gather-grouping-attributes [{:keys [overlayers] :as conf}]
  (assoc conf
         :overlayers
         (mapv (fn [{:keys [feature-properties] :as c}]
                 (if (seq feature-properties)
                   (assoc c
                          :grouping-attributes
                          (->> feature-properties
                               vals
                               (mapcat (fn [[_ & prop-values]]
                                         (->> prop-values
                                              (filter attr-string?)
                                              (mapv #(str/replace-first % "#" "")))))
                               set))
                   c))
               overlayers)))

(def default-layers
  {:base-layers [{:type "default"
                  :default true
                  :tilemap-server-url "https://a.tile.openstreetmap.de/{z}/{x}/{y}.png"
                  :attribution "Map data from OpenStreetMap."
                  :max-zoom 18
                  :name "Base german"}
                 {:type "default"
                  :tilemap-server-url "http://tile.osm.ch/switzerland/{z}/{x}/{y}.png"
                  :attribution "Map data from OpenStreetMap."
                  :max-zoom 18
                  :name "Swiss Standard"
                  :default false}
                 {:type "default"
                  :tilemap-server-url "http://tile.osm.ch/osm-swiss-style/{z}/{x}/{y}.png"
                  :attribution "Map data from OpenStreetMap."
                  :max-zoom 18
                  :name "Swiss Style"
                  :default false}]
   :overlayers [#_;Example
                  {:id "country-coloring"
                   :type "geojson"
                   :as-layer-base? true
                   :server-url "<server-url>"
                   :feature-properties {"COUNTRY" [:str "#country"]}
                   :main-feature-property "COUNTRY"
                   :name "Country"
                   :hint "Country Border."}]})

(def extern-config-state (atom {}))

(defn update-extern-config []
  (reset! extern-config-state
          (->> default-layers
               read-name-mappings
               gather-grouping-attributes)))

(update-extern-config)

(defn extern-config []
  @extern-config-state)

(def explorama-map-max-data-amount
  (defconfig
    {:env :explorama-map-max-data-amount
     :default 2000000
     :type :integer
     :doc "How much data can be visualized in the Client in one window.
         If the number of Events is reached, it will show a Message to the user and no data will be visualized."}))


(def explorama-map-max-events-data-amount
  (defconfig
    {:env :explorama-map-max-events-data-amount
     :default 200000
     :type :integer
     :doc "How much data can be visualized in the Client in one window.
         If the number of Events is reached, it will show a Message to the user and no marker and movement arrows will be visualized.
         The aggregation layers like Overlayers and Heatmap can still be visualized and used."}))


(def explorama-map-stop-filterview-amount
  (defconfig
    {:env :explorama-map-stop-filterview-amount
     :default 200000
     :type :integer
     :doc "This defines how much data can be filtered with the local-filter in each frame.
         If the number of events is larger than this, only a Message is shown when the user tries to use the filtering."}))


(def explorama-map-warn-filterview-amount
  (defconfig
    {:env :explorama-map-warn-filterview-amount
     :default 50000
     :type :integer
     :doc "This defines at what point a warning message should be shown to the user when he tries to use the local-filter."}))


(def explorama-max-popup-values
  (defconfig
    {:env :explorama-max-popup-values
     :default 10
     :type :integer
     :doc "Defines how many values should be shown in Popups when a Attribute has multiple values.
         If there are more then the configured number it will show the first x-values and then '...'."}))


(def explorama-marker-opacity
  (defconfig
    {:env :explorama-marker-opacity
     :default 70
     :type :integer
     :doc "Defines how strong the marker are visible on the map. Value ranges from 0-100."}))


(def explorama-marker-radius
  (defconfig
    {:env :explorama-marker-radius
     :default 5
     :type :integer
     :doc "Defines how big a marker should be."}))


(def explorama-map-max-marker-hover
  (defconfig
    {:env :explorama-map-max-marker-hover
     :default 10000
     :type :integer
     :doc "How many marker will be displayed when the user hovers over a cluster."}))


(def explorama-map-marker-cluster-threshold
  (defconfig
    {:env :explorama-map-marker-cluster-threshold
     :default 1000
     :type :integer
     :doc "Max number of events with which the user can still disable the clustering."}))


(def explorama-overlayer-opacity
  (defconfig
    {:env :explorama-overlayer-opacity
     :default 0.5
     :type :double
     :doc "Set the opacity for a area coloring overlayer. Ranges from 0 to 1."}))


(def explorama-map-cache-size
  (defconfig
    {:env :explorama-map-cache-size
     :type :integer
     :default 150000
     :doc "How big the cache for the data should be.
         If the size is reached older and not used keys will be removed to make space."}))


(def map-locations-external
  (defconfig
    {:name :map-locations-external
     :type :edn-string
     :default (or
            ;;TODO r1/config
            ;;  (try
            ;;    (slurp (str config-dir
            ;;                "/map/locations.edn")
            ;;           :encoding "UTF-8")
            ;;    (catch Exception _ nil))
            ;;  (try
            ;;    (slurp "locations.edn"
            ;;           :encoding "UTF-8")
            ;;    (catch Exception _ nil))
               "")
     :doc "Set the mapping from country name to a centroid position."}))
