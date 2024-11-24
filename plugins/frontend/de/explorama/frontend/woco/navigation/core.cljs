(ns de.explorama.frontend.woco.navigation.core
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]))

(re-frame/reg-sub
 ::translate-size
 (fn [db [_ sizes]]
   (let [z (navigation-control/position db :z)]
     (cond->> sizes
       (map? sizes)
       (reduce (fn [acc [k v]]
                 (cond-> acc
                   (number? v) (assoc k (/ v z))))
               sizes)))))

(defn transform-str [props x y z]
  (assoc-in props
            [:style :transform]
            (str "translate3d(" x "px, " y "px, 0) "
                 "scale(" z ")")))

(defn transition-str [props]
  (assoc-in props
            [:style :transition]
            "transform 0.7s cubic-bezier(0.32, 0.57, 0.27, 1.02) 0s"))

(defn transform-container [{:keys [transform-active?]} & childs]
  (let [{:keys [x y z]} @(re-frame/subscribe [::navigation-control/position])
        activate-transition? @(re-frame/subscribe [::navigation-control/animation-activated?])]
    (apply conj
           [:div (cond-> {:id config/frames-transform-id
                          :style {:transform-origin "0px 0px 0px"}
                          :on-transition-end #(re-frame/dispatch [::navigation-control/deactivate-animation])}
                   (and transform-active?
                        x y z)
                   (transform-str x y z)
                   activate-transition?
                   (transition-str))]
           childs)))