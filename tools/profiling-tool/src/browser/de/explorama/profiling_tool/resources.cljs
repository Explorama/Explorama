(ns de.explorama.profiling-tool.resources
  (:require [de.explorama.profiling-tool.resource.data-a-1k :as a1k]
            [de.explorama.profiling-tool.resource.data-a-10k :as a10k]
            [de.explorama.profiling-tool.resource.data-b-1k :as b1k]
            [de.explorama.profiling-tool.resource.data-b-10k :as b10k]
            [de.explorama.profiling-tool.resource.dt-data-a-1k :as dt-a1k]
            [de.explorama.profiling-tool.resource.dt-data-a-10k :as dt-a10k]
            [de.explorama.profiling-tool.resource.dt-data-b-1k :as dt-b1k]
            [de.explorama.profiling-tool.resource.dt-data-b-10k :as dt-b10k]
            [de.explorama.profiling-tool.data.core :refer [data-a-1k data-a-10k
                                                           data-b-1k data-b-10k
                                                           data-a-ds-id data-b-ds-id
                                                           data-a-ds-id-value
                                                           data-b-ds-id-value
                                                           data-a-data-tiles-1k
                                                           data-a-data-tiles-10k
                                                           data-b-data-tiles-10k
                                                           data-b-data-tiles-1k]]))

(defonce results (atom {}))

(defn preload-resources [& _])

(defn load-test-resource [path]
  (condp = path
    data-a-1k a1k/content
    data-a-10k a10k/content
    data-b-1k b1k/content
    data-b-10k b10k/content
    data-a-data-tiles-1k dt-a1k/content
    data-a-data-tiles-10k dt-a10k/content
    data-b-data-tiles-1k dt-b1k/content
    data-b-data-tiles-10k dt-b10k/content
    data-a-ds-id data-a-ds-id-value
    data-b-ds-id data-b-ds-id-value))

(defn resource-available? [path]
  (contains? #{data-a-1k data-a-10k data-b-1k data-b-10k} path))

(defn save-test-result [path result]
  (swap! results assoc path result))
