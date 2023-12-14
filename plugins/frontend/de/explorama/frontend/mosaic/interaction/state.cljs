(ns de.explorama.frontend.mosaic.interaction.state
  (:require [reagent.core :as reagent]))

(def state (reagent/atom nil))
(def allow-tooltips-state (atom {}))

(defn show-tooltip [new-state]
  (when-not (@allow-tooltips-state (:frame-id new-state))
    (reset! state new-state)))

(defn update-tooltip [frame-id updates]
  (when-not (@allow-tooltips-state frame-id)
    (swap! state (fn [val]
                   (if (= frame-id (:frame-id val))
                     (merge val updates)
                     val)))))

(defn hide-tooltip []
  (reset! state {}))

(defn block-tooltips [frame-id]
  (hide-tooltip)
  (swap! allow-tooltips-state assoc frame-id true))

(defn allow-tooltips [frame-id]
  (swap! allow-tooltips-state dissoc frame-id))