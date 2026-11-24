(ns e2e.main
  (:require [e2e.registry :as registry]
            [e2e.specs.smoke]
            [e2e.specs.workspace-mechanics]
            [e2e.specs.core-data-journey]
            [e2e.specs.charts-render]
            [e2e.specs.prediction-render]
            [e2e.specs.data-import]
            [e2e.specs.fact-units]
            [e2e.specs.legend-layout]
            [e2e.specs.table-scroll]
            [e2e.specs.tooltip]))

(registry/export!)
