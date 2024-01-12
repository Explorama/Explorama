(ns de.explorama.frontend.aggregation-vpl.render.instance-interface)

(defprotocol AggregationRenderer
  (state [instance])
  (app [instance]))
