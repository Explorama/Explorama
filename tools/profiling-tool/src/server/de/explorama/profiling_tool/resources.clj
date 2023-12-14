(ns de.explorama.profiling-tool.resources
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [de.explorama.profiling-tool.data.core :refer [data-a-1m
                                                           data-a-500k data-a-50k
                                                           data-a-5k data-a-ds-id data-b-1m data-b-500k data-b-50k data-b-5k data-b-ds-id]]))

(defonce resources (atom {}))

(defn preload-resources [& resources]
  (doseq [path resources]
    (swap! resources assoc path (edn/read-string (slurp path)))))

(defn load-test-resource [path]
  (get @resources path path))

(defn resource-available? [path]
  (contains? #{data-a-5k data-a-50k
               data-a-500k data-a-1m
               data-b-5k data-b-50k
               data-b-500k data-b-1m
               data-a-ds-id data-b-ds-id}
             path))

(defn save-test-result [path result]
  (io/make-parents path)
  (spit (apply io/file path)
        result))
