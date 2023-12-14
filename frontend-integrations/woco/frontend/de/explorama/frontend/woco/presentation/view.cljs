(ns de.explorama.frontend.woco.presentation.view
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon toolbar]]
            [goog.events :as events]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.woco.frame.view.core :refer [resizable-comp]]
            [de.explorama.frontend.woco.navigation.control :as nav]
            [de.explorama.frontend.woco.presentation.core :as presentation
             :refer [get-slide slide-by-uid]])
  (:import [goog.events EventType]))

;; Mouse-event handling for dragging
(defn mouse-move-handler [offset pos-store]
  (fn [evt]
    (.stopPropagation evt)
    (.preventDefault evt)
    (let [{:keys [ox oy z]} offset
          x (- (/ (aget evt "clientX") z) ox)
          y (- (/ (aget evt "clientY") z) oy)]
      (swap! pos-store merge {:x x :y y}))))

(defn update-slide-from-pos-store [index pos-store]
  (let [{x :x y :y} @pos-store] (re-frame/dispatch [::presentation/update-slide index {:x x :y y}])))

(defn mouse-up-handler [on-move pos-store index]
  (fn me [evt]
    (events/unlisten js/window EventType.MOUSEMOVE on-move)
    (events/unlisten js/window EventType.MOUSEUP me)
    (update-slide-from-pos-store index pos-store)))

(re-frame/reg-fx
 ::register-move-handler
 (fn [[offset pos-store index]]
   (let [on-move (mouse-move-handler offset pos-store)]
     (events/listen js/window EventType.MOUSEMOVE on-move)
     (events/listen js/window EventType.MOUSEUP
                    (mouse-up-handler on-move pos-store index)))))

(re-frame/reg-event-fx
 ::start-drag
 (fn [{db :db} [_ pos-store index e]]
   (let [z (nav/position db :z)
         x (/ (aget e "clientX") z)
         y (/ (aget e "clientY") z)
         {px :x py :y} (get-slide db (slide-by-uid db index))
         offset             {:ox (- x px)
                             :oy (- y py)
                             :z z}]

     {::register-move-handler [offset pos-store index]})))

;; Resize Handlers
(defn- position-offset-operator [direction]
  (letfn [(_  [x1 x2] x1)]
    (case direction
      "bottom" [_ _]
      "bottomRight" [_ _]
      "right" [_ _]
      "topRight" [_ -]
      "top" [_ -]
      "topLeft" [- -]
      "left" [- _]
      "bottomLeft" [- _]
      [_ _])))

(defn- resize-stop-handler [direction delta slide]
  (let [[sx sy] (position-offset-operator direction)
        [dx dy]  [(aget delta "width") (aget delta "height")]
        {:keys [x y w h uid]} slide

        w (+ w dx)
        h (+ h dy)

        x (sx x dx)
        y (sy y dy)]
    (re-frame/dispatch [::presentation/update-slide uid {:x x :y y :w w :h h}])))

(defn- on-resize-handler [direction delta pos-store slide]
  (let [[sx sy] (position-offset-operator direction)
        [dx dy]  [(aget delta "width") (aget delta "height")]
        {:keys [x y w h]} slide
        x (sx x dx)
        y (sy y dy)
        w (+ w dx)
        h (+ h dy)]
    (reset! pos-store {:x x :y y :w w :h h})))

;; View containers
(defn slideframe [uid]
  (let [{x :x y :y
         w :w h :h} @(re-frame/subscribe [::presentation/slide-infos-by-uid uid])
        pos-store (r/atom {:x x :y y
                           :w w :h h})] ;local store for performance, storing in app-db caused flickering, only used during resizing and dragging

    (fn [uid]
      (let [{x :x y :y
             w :w h :h} @pos-store
            {:keys [index name #_w #_h] :as slide} @(re-frame/subscribe [::presentation/slide-infos-by-uid uid])
            {z :z} @(re-frame/subscribe [::nav/position])
            read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                     {:frame-id uid
                                      :component :presentation-mode
                                      :additional-info :slideframe-edit})
            ;styles that compensate for the zoom level
            font-size (str (/ 16 z) "px")
            icon-size (/ 25 z)
            box-shadow (str "rgba(0, 145, 145, 0.2) 0 0 " (/ 20 z) "px 0")
            border-width (str (/ 0.5 z) "px")
            close-label @(re-frame/subscribe [::i18n/translate :close])
            drag-label @(re-frame/subscribe [::i18n/translate :aria-label-drag-slide])]
        [error-boundary
         [resizable-comp
          {:size {:width w :height h}
           :enable (reduce #(assoc %1 %2 (not read-only?)) {} [:top, :right, :bottom, :left, :topRight, :bottomRight, :bottomLeft, :topLeft])
           :handleStyles (reduce #(assoc %1 %2 {:pointer-events "auto"}) {} [:top, :right, :bottom, :left, :topRight, :bottomRight, :bottomLeft, :topLeft])
           :scale z
           :on-resize-start (fn [ev _ _]
                              (.stopPropagation ev))
           :on-resize-stop (fn [ev direction ref delta]
                             (resize-stop-handler direction delta slide))
           :on-resize (fn [ev direction ref delta]
                        (on-resize-handler direction delta pos-store slide))
           :style {:position "absolute"
                   :transform (str "translate(" x "px, " y "px)")
                   :pointer-events "none"}
           :className "slide__container"}
          [:div.slide__frame
           {:style {:box-shadow box-shadow :border-width border-width}}
           [:button.slide__drag
            {:on-mouse-down #(do (.stopPropagation %)
                                 (re-frame/dispatch [::start-drag pos-store uid %]))
             :aria-label drag-label
             :disabled read-only?}
            [icon {:icon :drag-5 :size icon-size}]]
           [:button.slide__remove
            {:on-mouse-down #(do (.stopPropagation %)
                                 (.preventDefault %)
                                 (re-frame/dispatch [::presentation/remove-slide-by-uid uid]))
             :aria-label close-label
             :disabled read-only?}
            [icon {:icon :close :size icon-size}]]
           [:div.slide__number {:style {:font-size font-size}} (str (inc index))]
           [:div.slide__title {:style {:font-size font-size}} name]]]]))))

(defn presentation-control-container []
  (let [curr-slide (+ @(re-frame/subscribe [::presentation/slide-sub]) 1)
        max-slides @(re-frame/subscribe [::presentation/max-slide-sub])
        presentation-of @(re-frame/subscribe [::i18n/translate :presentation-of])
        sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)]
    [toolbar {:orientation :horizontal
              :tooltip-direction :up
              :extra-class ["absolute" "center-x" "bottom-8"]
              :z-index 200002
              :items [{:id "slideshow-prev"
                       :icon :prev
                       :on-click #(do (.stopPropagation %)
                                      (.preventDefault %)
                                      (re-frame/dispatch [::presentation/switch-slide -1]))}
                      {:id "slideshow-info"
                       :type :text
                       :label (str curr-slide " " presentation-of " " max-slides)}
                      {:id "slideshow-next"
                       :icon :next
                       :on-click #(do (.stopPropagation %)
                                      (.preventDefault %)
                                      (re-frame/dispatch [::presentation/switch-slide 1]))}
                      {:id "slideshow-close"
                       :icon :close
                       :on-click #(do (.stopPropagation %)
                                      (.preventDefault %)
                                      (when sync-event-fn
                                        (sync-event-fn [::presentation/switch-mode]))
                                      (re-frame/dispatch [::presentation/switch-mode]))}]}]))