(ns de.explorama.backend.search.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :search)
(def plugin-string (name plugin-key))

(def direct-search-ignore-attributes #{"day"})

(def explorama-traffic-lights-threshold-red
  (defconfig
    {:env :explorama-traffic-lights-threshold-red
     :default 400000
     :type :integer
     :doc "Number of events when the traffic light shows red in the search."}))

(def explorama-traffic-lights-threshold-yellow
  (defconfig
    {:env :explorama-traffic-lights-threshold-yellow
     :default 50000
     :type :integer
     :doc "Number of events when the traffic light shows yellow in the search."}))

(def explorama-threshold-count-events-data-tiles
  (defconfig
    {:env :explorama-threshold-count-events-data-tiles
     :default 2500000
     :type :integer
     :doc "Maximum number of events pulled for counting."}))

(def explorama-threshold-count-events-filter
  (defconfig
    {:env :explorama-threshold-count-events-filter
     :default 2000000
     :type :integer
     :doc "Result limit for a data-retrival (count only)"}))

(def explorama-threshold-count-events-chunk-size
  (defconfig
    {:env :explorama-threshold-count-events-chunk-size
     :default 250000
     :type :integer
     :doc "Chunk size for data-retrival (count only)"}))

(def search-geo-config
  (defconfig
    {:name :search-geo-config
     :fallback {:source {:url "http://tile.openstreetmap.org/{z}/{x}/{y}.png"}
                :maxZoom 18}
     :default [:config-dir "/search-geo.edn"]
     :type :edn-file
     :doc "Configuration for the location attribute map."}))

(def explorama-direct-search-request-delay
  (defconfig
    {:env :explorama-direct-search-request-delay
     :default 500
     :type :integer
     :doc "Delay in ms between last user input and request to server for the result."}))

(def explorama-search-parameter-config
  (defconfig
    {:env :explorama-search-parameter-config
     :default {:annotation {:required-attributes-num 2}
               :notes {:required-attributes-num 2}
               :fulltext {:required-attributes-num 2}
               :location {:required-attributes #{"year" "datasource" "country"}}
               :year {}
               :datasource {}
               :country {}
               :default {}}
     :type :edn-string
     :doc "Defines required number of attributes or attributes for specific attributes."}))
