(ns de.explorama.frontend.reporting.views.dashboards.template-schema
  (:require [de.explorama.frontend.reporting.data.templates :as templates]
            [re-frame.core :refer [subscribe]]))

(defn schema-modules-grid [{:keys [template-id]}]
  (let [{:keys [grid tiles]} @(subscribe [::templates/template template-id])
        [gw gh] grid]
    (reduce (fn [acc [_tile-idx {[x y] :position
                                 [w h] :size}]]
              (let [x (inc x)
                    y (inc y)
                    tile-classes [(str "x" x)
                                  (str "y" y)
                                  (str "w" w)
                                  (str "h" h)]]
                (conj acc
                      [:div.dashboard__item {:class tile-classes}])))
            [:div.dashboard__layout (cond-> {:class ["preview"
                                                     (str "c" gw)
                                                     (str "r" gh)]})]
            (map-indexed (fn [idx itm] [idx itm]) tiles))))
