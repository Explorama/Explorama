(ns de.explorama.frontend.search.timeout
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defonce timeouts (reagent/atom {}))

(re-frame/reg-fx
 :timeout
 (fn [{:keys [id event time]}]
   (when-some [existing (get @timeouts id)]
     (js/clearTimeout existing)
     (swap! timeouts dissoc id))
   (when (some? event)
     (swap! timeouts assoc id
            (js/setTimeout
             (fn []
               (re-frame/dispatch event))
             time)))))
