(ns de.explorama.profiling-tool.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def bench-report-wait
  (defconfig
    {:default 2000
     :env :explorama-benchmark-report-wait-ms
     :type :integer
     :doc "Thread sleep before the report gets retrieved from tufte."}))

(def test-frame-id {:frame-id "benchmark-frame"
                    :workspace-id "7298a7fb-0838-49b2-8435-b3101a549b20"})
