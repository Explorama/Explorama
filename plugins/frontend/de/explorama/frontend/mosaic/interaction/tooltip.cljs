(ns de.explorama.frontend.mosaic.interaction.tooltip
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.interaction.state :refer [state]]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [re-frame.core :as re-frame]))

(defn- position [[left top] width height]
  (cond-> {}
    (< (* 0.70 width) left)
    (assoc :right (- width left 10))
    (>= (* 0.70 width) left)
    (assoc :left left)
    (< (* 0.85 height) top)
    (assoc :bottom (- height top (- 15)))
    (>= (* 0.85 height) top)
    (assoc :top (+ top 25))))

(defn ^:private raster-tooltip [frame-id]
  (let [{pos :pos
         {:keys [i18n-cards-count aggregated-value name grp-attr attr]} :text}
        @state
        width @(re-frame/subscribe [:de.explorama.frontend.mosaic.views.canvas/width frame-id])
        height @(re-frame/subscribe [:de.explorama.frontend.mosaic.views.canvas/height frame-id])
        pos (position pos width height)
        {:keys [by method]}
        (tasks/sort-desc-from-operations-desc @(re-frame/subscribe [::tasks/operations frame-id])
                                              grp-attr)
        labels @(fi/call-api [:i18n :get-labels-sub])]
    (cond-> [:div.react-tooltip-lite
             {:style (merge {:background-color "white"
                             :position :absolute}
                            pos)}
             (str name " (" i18n-cards-count " Events)")]
      (= :aggregate by)
      (into
       (let [translation @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate (case method
                                                                       :sum
                                                                       :canvas-tooltip-grp-sort-sum
                                                                       :average
                                                                       :canvas-tooltip-grp-sort-average
                                                                       :max
                                                                       :canvas-tooltip-grp-sort-max
                                                                       :min
                                                                       :canvas-tooltip-grp-sort-min)])]
         [[:br]
          (str translation
               " "
               (get labels attr attr)
               " (" aggregated-value ")")])))))

(defn ^:private treemap-tooltip [frame-id]
  (let [{pos :pos
         {:keys [i18n-cards-count name]
          {parent-i18n-cards-count :i18n-cards-count parent-name :name :as parent-title} :parent-title}
         :text}
        @state
        width @(re-frame/subscribe [:de.explorama.frontend.mosaic.views.canvas/width frame-id])
        height @(re-frame/subscribe [:de.explorama.frontend.mosaic.views.canvas/height frame-id])
        pos (position pos width height)]
    (cond-> [:div.react-tooltip-lite
             {:style (merge {:background-color "white"
                             :position :absolute}
                            pos)}
             (str name " (" i18n-cards-count " Events)")]
      parent-title
      (conj
       [:br]
       (str "in " parent-name " (" parent-i18n-cards-count " Events)")))))

(defn groups [frame-id]
  (let [{tool-tip-frame-id :frame-id
         type :type}
        @state]
    (when (= frame-id tool-tip-frame-id)
      (case type
        :treemap
        [treemap-tooltip frame-id]
        :raster
        [raster-tooltip frame-id]
        [raster-tooltip frame-id]))))
