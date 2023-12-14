(ns de.explorama.frontend.woco.scale
  (:require
   [re-frame.core :as re-frame]
   [de.explorama.frontend.woco.path :as path]))


(defn scale-info []
  (let [dif ;calculates the scaling difference between the outer and inner window
        (- (aget js/window "devicePixelRatio")
           (/ (aget js/window "outerWidth")
              (aget js/window "innerWidth")))
        browser-zoom (aget js/window "devicePixelRatio")
        zoom-tip (cond ;sets limits that resulted from tests
                   (< browser-zoom 0.75) "zoom-in-tip"
                   (> browser-zoom 1.25) "zoom-out-tip"
                   :else "zoom-tip")
        scale-info {:inner-width (aget js/window "innerWidth")
                    :inner-height (aget js/window "innerHeight")
                    :zoom-tip zoom-tip}]
    scale-info))


(re-frame/reg-event-db
 ::update-scale-info
 (fn [db [_ _]]
   (assoc-in db path/scale-info (scale-info))))


(re-frame/reg-sub
 ::scale-info
 (fn [db [_ _]]
   (get-in db path/scale-info)))


(defn apply-user-browser-scale []
  (let [{:keys [inner-width inner-height zoom-tip]}
        @(re-frame/subscribe [::scale-info])]
    zoom-tip))
