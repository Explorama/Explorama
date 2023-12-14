(ns de.explorama.frontend.woco.config
  (:require [clojure.set :refer [union]]))

(def app-version "0.0.0")

(def debug?
  ^boolean goog.DEBUG)

(goog-define ^string DEFAULT_LOG_LEVEL "info")
(goog-define ^string RUNTIME_MODE "dev")

(def dev-mode? (= "dev" RUNTIME_MODE))

(def system-name "Explorama")

(def use-devtools? (aget js/window "EXPLORAMA_DEVTOOLS"))

(def available-plugins (js->clj (aget js/window "EXPLORAMA_PLUGIN_LIST")))

(def environment (aget js/window "EXPLORAMA_ENVIRONMENT"))

;; (when (or debug? use-devtools?)
;;   #_(ui-base-spec/enable-validation)
;;   (devtools/install! [:formatters :hints]))

(def default-namespace :woco)
(def default-vertical-str (name default-namespace))
(def details-view-vertical-namespace (str (name default-namespace)
                                          "-details-view"))
(def details-view-icon :iva-right)  ;;TODO r1/icons find better icon

(def frames-x-offset 30)
(def frames-y-offset 30)
(def header-height 32)   ;TODO r1/woco dynamic from somewhere
(def userbar-height 68)
(def ^number explorama-header-height 48) ;px

(def details-view-width 600)
(def details-view-height 550)
(def min-detail-view-size [(* details-view-width 0.7) (* details-view-height 0.9)])
(def details-view-tabs-width 160)
(def details-view-attr-col-width 120)
(def details-view-compare-width-percent 80)

(def legend-width 300) ;px

(def move-to-frame-offset 10)
(def workspace-root-id "workspace-root")
(def frames-transform-id (str ::frames-transform))
(def workspace-parent-id "frames-workspace")
(def navbar-id "woco-navi-bar")
(def note-id "tool-note")

(def window-class "frame")

(def welcome-check-delay 500)
(def welcome-tips-interval-ms 10000)
(def default-cursor "default")
(def copy-cursor "copy")
(def panning-cursor "move")
(def can-move-frame-cursor "grab")
(def move-frame-cursor "grabbing")
(def min-content-width 300)

(def direct-search-fetch-delay 500)

(def frame-id-prefix "woco_frame-")
(def frame-id-optimization-prefix "woco_optim-frame-")
(def frame-id-prefix-pattern (re-pattern frame-id-prefix))

(defn frame-dom-id [frame-id]
  (str frame-id-prefix (get frame-id :frame-id)))

(defn frame-optimization-dom-id [frame-id]
  (str frame-id-optimization-prefix (get frame-id :frame-id)))

;;---------- Panning/Zoom -----------

; Timeout in ms after panning/zoom to check if it should be logged
(def position-logging-timeout 3000)
; minimal zoom on workspace
(def min-zoom 0.1)
; maximal zoom on workspace
(def max-zoom 1.25)
; zoom on which interaction with frame is disabled and the whole frame can be dragged for moving
(def drag-frame-zoom 0.5)

; Speed which identifies the zoom-step size related to wheel delta
(def zoom-speed 0.1)
(def max-wheel-delta 720)
(def min-wheel-delta -720)

; Default position on workspace
(def default-position {:x 0
                       :y 0
                       :z 1})
; Multiply factor for zooming in with keyboard
(def key-zoom-in-factor 1.1)
; Multiply factor for zooming out with keyboard
(def key-zoom-out-factor 0.9)
; Multiply factor for panning horizontal with keyboard
(def key-panning-horizontal 20)
; Multiply factor for panning vertical with keyboard
(def key-panning-vertical 20)

; 107 Num Key +
; 187 Key +
(def zoom-in-keys #{107 187})

; 109 Num Key  -
; 189 Key -
(def zoom-out-keys #{109 189})
; 61 = key (resets browserzoom)

(def zooming-ignore-keys (union #{61 173}
                                zoom-in-keys
                                zoom-out-keys))

(def trackpad-scroll-offset 80) ;To decide if it's an scroll on mouse wheel or panning action on trackpad
; 37 = arrow left
(def panning-left-keys #{37})
; 38 = arrow top
(def panning-top-keys #{38})
; 39 = arrow right
(def panning-right-keys #{39})
; 40 = arrow bottom
(def panning-bottom-keys #{40})
; 61 = key (resets browserzoom)
(def panning-ignore-keys (union panning-left-keys
                                panning-top-keys
                                panning-right-keys
                                panning-bottom-keys))

;122 = F11 (fullscreen)
(def fullscreen-ignore-keys #{122})


(def framelist-order [:diid])
(def fit-to-content-padding-horizontal 100) ;px
(def fit-to-content-padding-vertical 50) ;px


(def minimap-width 360) ;px
(def minimap-height 135) ;px
(def minimap-resolution 3) ;high value = sharper rendering of minimap (= more pixels = maybe performance/memory implications)

(def minimap-render-padding-horizontal 1000) ;px
(def minimap-render-padding-vertical 500) ;px
(def minimap-render-min-width 1000);px
(def minimap-render-min-height 500);px

(def minimap-show-icon-min-width 140)
(def minimap-show-icon-min-height 140)

(def minimap-viewport-color [0 0 0]) ;rgb
(def minimap-viewport-alpha 0.15) ; 0-1

(def minimap-default-frame-color [200 200 200]) ;rgb
(def minimap-frame-border-size 12) ;px
(def minimap-frame-border-color [247 249 255]) ;rgb
(def minimap-frame-border-alpha 1) ; 0-1
(def minimap-icon-scale-factor 15)
(def minimap-render-icons? true)

(def enable-panning-borders? false)
(def panning-border-step 7) ;px
(def autopan-interval 10) ;ms
(def autopan-start-delay 200) ;ms

(def console-threshold 160) ; min-width of browser console
(def disable-default-context-menu? true) ; Enabled when Browser console is opened
(def sidebar-width 500) ;px

;; z-index ordering
(def z-index-min 100)
(def z-index-max 498)
(def maximized-index 503)

(def enable-connecting-edges? {:status false})

;; snapping
(def ^boolean enable-snapping? {:frame true
                                :grid true})
(def ^number snap-hitbox-x 12) ;; Marks the detection for snapping on x-axis
(def ^number snap-hitbox-y 12) ;; Marks the detection for snapping on y-axis
(def ^number snap-trap-x 2.52) ;; Marks where the mouse will trapped on the snappline for x-axis
(def ^number snap-trap-y 2.52) ;; Marks where the mouse will trapped on the snappline for y-axis

;; presentation-mode

;; When adding a slide: distance betweeen the outer edges of the viewport
;; and the edges of the slide. Measured as a ratio.
(def viewport-padding 0.08)
(def slideframe-padding 50) ;; when automatically surrounding frames

(def presentation-next-slide-keys
  #{" ", "n", "ArrowRight", "ArrowDown","Enter","PageDown"})

(def presentation-prev-slide-keys
  #{"p", "ArrowLeft", "ArrowUp","Backspace","PageUp"})

(def presentation-exit-keys
  #{"Escape"})

(def notes-vertical-str "notes")
(def note-min 100) ;; for both, width and height
(def note-width 350)
(def note-height 250)
(def custom-frame-z-index-min 500)
(def custom-frame-z-index-ontop 501)
(def note-default-bgcolor "#ffffff")
(def note-default-font-color "#1b1c1e")
(def note-minimap-dbgc "#aaaaaa")
(def bg-color-values [{:label :note-white
                       :hex "#ffffff"
                       :icon-color :white
                       :brightness 9}
                      {:label :note-gray
                       :hex "#e7e8ea"
                       :icon-color :gray
                       :brightness 9}
                      {:label :note-black
                       :hex "#1b1c1e"
                       :icon-color :black
                       :brightness 9}
                      {:label :note-red
                       :hex "#fbd8d8"
                       :icon-color :red
                       :brightness 9}
                      {:label :note-orange
                       :hex "#fee2d0"
                       :icon-color :orange
                       :brightness 9}
                      {:label :note-yellow
                       :hex "#fff5d6"
                       :icon-color :yellow
                       :brightness 9}
                      {:label :note-green
                       :hex "#cfedde"
                       :icon-color :green
                       :brightness 9}
                      {:label :note-teal
                       :hex "#cfeded"
                       :icon-color :teal
                       :brightness 9}
                      {:label :note-blue
                       :hex "#cfdeed"
                       :icon-color :blue
                       :brightness 9}])

(def font-color-values [{:label :note-white
                         :hex "#ffffff"
                         :icon-color :white
                         :brightness 9}
                        {:label :note-gray
                         :hex "#36393c"
                         :icon-color :gray
                         :brightness 2}
                        {:label :note-black
                         :hex "#1b1c1e"
                         :icon-color :black
                         :brightness 1}
                        {:label :note-red
                         :hex "#771e1e"
                         :icon-color :red
                         :brightness 1}
                        {:label :note-orange
                         :hex "#7d380a"
                         :icon-color :orange
                         :brightness 1}
                        {:label :note-yellow
                         :hex "#7f671a"
                         :icon-color :yellow
                         :brightness 1}
                        {:label :note-green
                         :hex "#08522d"
                         :icon-color :green
                         :brightness 1}
                        {:label :note-teal
                         :hex "#085252"
                         :icon-color :teal
                         :brightness 1}
                        {:label :note-blue
                         :hex "#082d52"
                         :icon-color :blue
                         :brightness 1}])

;; Frame toolbars
(def toolbar-mode-pref-key "window-toolbar-mode")
(def toolbar-full-mode-key "full")
(def toolbar-compact-mode-key "compact")
(def default-toolbar-mode toolbar-full-mode-key)
(def toolbar-portal-z-index 1500)
(def toolbar-context-menu-z-index 1501)
(def show-delay 10) ;;ms
(def hide-delay 1000) ;;ms

(def minimized-height 32)

(def ^number notes-max-length 5000)
