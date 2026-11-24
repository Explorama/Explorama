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

;; ---------------------------------------------------------------------
;; Tailwind migration of styles/src/scss/components/_presentation.scss's
;; slide-overlay chrome (.slide__container / .slide__frame / .slide__title /
;; .slide__number + the drag & remove buttons and their icons). Only this file
;; renders this markup (verified via grep across plugins/, bundles/*/frontend),
;; so the class knowledge below stays private to this ns. The `slide__*` class
;; literals are still emitted on their elements: the residual :has()-state
;; overrides in _presentation_domain.scss select through them
;; (`.slide__container .slide__frame:has(button.slide__drag:hover) ...`).
;;
;; ZOOM / INLINE-STYLE COMPOSITION (unchanged by this migration):
;; `slideframe` still sets `box-shadow` and `border-width` INLINE on
;; `.slide__frame`, and `font-size` inline on the title/number -- all
;; zoom-compensated. An inline :style outranks any class (as it outranked the
;; old `.slide__container .slide__frame` SCSS rule), so the base values in the
;; stacks below are the unzoomed fallback and the inline :style keeps winning
;; at every zoom level -- exactly the prior cascade. The old class box-shadow
;; (var(--primary-muted)) and border-width (0.0625rem) were likewise always
;; overridden by that same inline :style; only the border colour/style and the
;; box-shadow-in-active-state actually rendered. The `:has()` drag/remove
;; state overrides live in _presentation_domain.scss: their `!important`
;; box-shadow still beats the inline style, and their higher-specificity
;; border-colour / background / title-number-colour still beat these base
;; utilities regardless of the utilities-loaded-last order.
(def ^:private slide-container-class
  "absolute z-[500000] pointer-events-none min-w-[300px] min-h-[300px]")

;; `[border:...]` / `[box-shadow:...]` are arbitrary-property stacks that
;; reproduce the old shorthand byte-for-byte; both are the unzoomed fallback
;; overridden at runtime by the inline :style (border-width / box-shadow). The
;; shadow keeps the old class's var(--primary-muted) form -- the inline :style
;; is what supplies the rendered rgba(0,145,145,0.2). border colour + solid
;; style DO render (inline only overrides the width).
(def ^:private slide-frame-class
  (str "relative w-full h-full bg-[rgba(255,255,255,0.1)] "
       "[border:0.0625rem_solid_var(--primary-muted)] rounded-sm "
       "[box-shadow:var(--primary-muted)_0_0_20px_0] "
       "[transition:background-color_200ms,box-shadow_200ms,border_200ms]"))

;; text-[20px] is the unzoomed fallback (inline :style font-size overrides it).
(def ^:private slide-title-class
  (str "absolute bottom-full w-full py-2 px-0.5 text-[20px] text-gray-400 "
       "overflow-hidden text-ellipsis whitespace-nowrap"))

;; text-[16px] is the unzoomed fallback (inline :style font-size overrides it).
(def ^:private slide-number-class
  "absolute bottom-2 w-full text-[16px] text-gray-400 font-bold text-center")

;; Shared base chrome for both slide buttons. [background:none]/[border:none]/
;; [box-shadow:none] mirror the old `button{background:none;border:none;
;; box-shadow:none}`; hover:[background:none] pins the hover background (the old
;; `button:hover{background:none}`, element-local, not a descendant-hover).
(def ^:private slide-button-class
  (str "absolute flex justify-center items-center m-0 p-0 "
       "[background:none] [border:none] [box-shadow:none] pointer-events-auto "
       "hover:[background:none]"))

;; `group` drives the icon's group-hover tint (slide-icon-class). The grab/
;; grabbing cursors are element-local (the button styling itself on its own
;; hover/active), so plain hover:/active: is correct -- not the
;; descendant-hover -> group-hover rule, which only applies when a parent's
;; hover styles a child (that case is the icon tint below).
(def ^:private slide-drag-class
  (str slide-button-class " group top-3 left-3 hover:cursor-grab active:cursor-grabbing"))

(def ^:private slide-remove-class
  (str slide-button-class " group top-3 right-3 cursor-pointer"))

;; Icon base colour gray-400 + the old descendant-hover tint gray-600 expressed
;; as group-hover: on the icon span (the button is `group`) -- reproducing the
;; old `button:hover span[class^="icon-"]{background-color:gray-600}` trigger
;; region (the whole button incl. its padding ring, same pattern as
;; sidebar.cljs's close button). w-6/h-6 mirror the old span sizing (overridden
;; at runtime by the icon component's numeric :size, as the old CSS was too).
(def ^:private slide-icon-class
  "w-6 h-6 bg-gray-400 group-hover:bg-gray-600")

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
           :className (str "slide__container " slide-container-class)}
          [:div.slide__frame
           {:class slide-frame-class
            :style {:box-shadow box-shadow :border-width border-width}}
           [:button.slide__drag
            {:class slide-drag-class
             :on-mouse-down #(do (.stopPropagation %)
                                 (re-frame/dispatch [::start-drag pos-store uid %]))
             :aria-label drag-label
             :disabled read-only?}
            [icon {:icon :drag-5 :size icon-size :extra-class slide-icon-class}]]
           [:button.slide__remove
            {:class slide-remove-class
             :on-mouse-down #(do (.stopPropagation %)
                                 (.preventDefault %)
                                 (re-frame/dispatch [::presentation/remove-slide-by-uid uid]))
             :aria-label close-label
             :disabled read-only?}
            [icon {:icon :close :size icon-size :extra-class slide-icon-class}]]
           [:div.slide__number {:class slide-number-class :style {:font-size font-size}} (str (inc index))]
           [:div.slide__title {:class slide-title-class :style {:font-size font-size}} name]]]]))))

(defn presentation-control-container []
  (let [curr-slide (+ @(re-frame/subscribe [::presentation/slide-sub]) 1)
        max-slides @(re-frame/subscribe [::presentation/max-slide-sub])
        presentation-of @(re-frame/subscribe [::i18n/translate :presentation-of])
        sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)]
    [toolbar {:orientation :horizontal
              :tooltip-direction :up
              :extra-class ["absolute" "center-x" "bottom-2"]
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