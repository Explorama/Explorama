(ns de.explorama.frontend.woco.navigation.panning-borders
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.frame.interaction.move :refer [moving-state]]
            [de.explorama.frontend.woco.config :as config]))

(def panning-area-class "autopanning__area")
(def main-class "autopanning__gradient__")
(def left-class (str main-class "left"))
(def top-class (str main-class "top"))
(def right-class (str main-class "right"))
(def bottom-class (str main-class "bottom"))
(def top-left-class (str main-class "top-left"))
(def top-right-class (str main-class "top-right"))
(def bottom-left-class (str main-class "bottom-left"))
(def bottom-right-class (str main-class "bottom-right"))
(def hover-class "hover")

(defn- panning-class [direction hover?]
  (cond-> (case direction
            :left left-class
            :top top-class
            :right right-class
            :bottom bottom-class
            :top-left top-left-class
            :top-right top-right-class
            :bottom-left bottom-left-class
            :bottom-right bottom-right-class
            nil)

    hover? (str " " hover-class)))

(defonce autopan-active? (atom {}))
(defonce current-timeout (atom {}))

(defn- autopan
  ([direction pan-x pan-y interval]
   (swap! current-timeout
          assoc
          direction
          (js/setTimeout #(do
                            (re-frame/dispatch [:de.explorama.frontend.woco.navigation.control/pan pan-x pan-y])
                            (when (get @autopan-active? direction)
                              (autopan direction pan-x pan-y)))
                         interval)))
  ([direction pan-x pan-y]
   (autopan direction pan-x pan-y config/autopan-interval)))

(defn- start-autopan [direction pan-x pan-y]
  (when-let [timeout (get @current-timeout direction)]
    (js/clearTimeout timeout))
  (swap! autopan-active? assoc direction true)
  (autopan direction pan-x pan-y config/autopan-start-delay))

(defn- end-autopan
  ([direction]
   (when-let [timeout (get @current-timeout direction)]
     (js/clearTimeout timeout)
     (swap! current-timeout dissoc direction))
   (swap! autopan-active? dissoc direction))
  ([]
   (reset! autopan-active? {})
   (reset! current-timeout {})))

(def top-directions #{:top :top-left :top-right})
(def left-direction #{:left :top-left :bottom-left})
(def bottom-direction #{:bottom :bottom-left :bottom-right})
(def right-direction #{:right :top-right :bottom-right})

(defn- pan-border [direction]
  (r/create-class
   {:display-name "woco panning area"
    :component-will-unmount #(end-autopan direction)
    :reagent-render (fn [direction]
                      [:div {:class (panning-class direction false)}
                       [:div {:class panning-area-class
                              :on-drag-leave (fn [e]
                                               (end-autopan direction))
                              :on-drag-enter (fn [e]
                                               (start-autopan direction
                                                              (cond
                                                                (left-direction direction)
                                                                (- config/panning-border-step)
                                                                (right-direction direction)
                                                                config/panning-border-step)
                                                              (cond
                                                                (top-directions direction)
                                                                (- config/panning-border-step)
                                                                (bottom-direction direction)
                                                                config/panning-border-step)))}]])}))


(defn- wrap-with-component-key [direction]
  (with-meta
    [pan-border direction]
    {:key (str "woco-panning-border" direction)}))

(defn panning-borders []
  (when config/enable-panning-borders?
    (r/create-class
     {:display-name "woco panning borders"
      :component-will-unmount #(end-autopan)
      :reagent-render (fn []
                        (when (moving-state)
                          [:<>
                           (wrap-with-component-key :left)
                           (wrap-with-component-key :top)
                           (wrap-with-component-key :right)
                           (wrap-with-component-key :bottom)
                           (wrap-with-component-key :top-left)
                           (wrap-with-component-key :top-right)
                           (wrap-with-component-key :bottom-left)
                           (wrap-with-component-key :bottom-right)]))})))

