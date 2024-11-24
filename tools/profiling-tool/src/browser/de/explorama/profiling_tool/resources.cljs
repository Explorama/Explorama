(ns de.explorama.profiling-tool.resources
  (:require [de.explorama.profiling-tool.resource.data-a-1k-csv :as a1k]
            [de.explorama.profiling-tool.resource.data-a-10k-csv :as a10k]
            [de.explorama.profiling-tool.resource.data-b-1k-csv :as b1k]
            [de.explorama.profiling-tool.resource.data-b-10k-csv :as b10k]
            [de.explorama.profiling-tool.data :refer [data-a-1k data-a-10k
                                                      data-b-1k data-b-10k
                                                      data-a-1k-csv data-a-10k-csv
                                                      data-b-1k-csv data-b-10k-csv
                                                      data-a-ds-id data-b-ds-id
                                                      data-a-ds-id-value
                                                      data-b-ds-id-value]]))

(defonce results (atom {}))

(defn load-test-resource [path]
  (condp = path
    data-a-1k-csv a1k/content
    data-a-10k-csv a10k/content
    data-b-1k-csv b1k/content
    data-b-10k-csv b10k/content
    data-a-1k a1k/content
    data-a-10k a10k/content
    data-b-1k b1k/content
    data-b-10k b10k/content
    data-a-ds-id data-a-ds-id-value
    data-b-ds-id data-b-ds-id-value))

(defn resource-available? [path]
  (contains? #{data-a-1k data-a-10k data-b-1k data-b-10k} path))

(defn save-test-result [path result]
  (swap! results assoc path result))
