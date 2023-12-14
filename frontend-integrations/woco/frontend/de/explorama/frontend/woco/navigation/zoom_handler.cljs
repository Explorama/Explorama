(ns de.explorama.frontend.woco.navigation.zoom-handler
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.config :as config]
            [taoensso.timbre :refer-macros [debug]]
            [de.explorama.frontend.woco.navigation.util :refer [z-factor]]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]))

(defonce valid-zoom-ids #{config/workspace-parent-id
                          config/frames-transform-id})

(defn zoom-handler [event]
  (let [src-element (aget event "srcElement")]
    (when (and src-element (valid-zoom-ids (aget src-element "id")))

      (.preventDefault event)
      (let [delta-x (aget event "wheelDeltaX")
            delta-y (aget event "wheelDeltaY")
            x? (not= delta-x 0)
            y? (not= delta-y 0)
            is-scroll? (< config/trackpad-scroll-offset (Math/abs delta-y))]

        (when x?
          (re-frame/dispatch [::navigation-control/pan delta-x]))

        (when (and y? (not is-scroll?))
          (re-frame/dispatch [::navigation-control/pan nil delta-y]))

        (when (and y? is-scroll?)
          (re-frame/dispatch [::navigation-control/point-zoom (z-factor delta-y)]))))))

(defn key-handler [event]
  (when (config/fullscreen-ignore-keys (aget event "which"))
    (let [keycode (aget event "which")]
      (.preventDefault event) ;//prevent browserzoom
      (cond
        (config/fullscreen-ignore-keys keycode)
        (re-frame/dispatch [:de.explorama.frontend.woco.navigation.fullscreen-handler/toggle-fullscreen])

        (config/zoom-in-keys keycode)
        (re-frame/dispatch [::navigation-control/point-zoom config/key-zoom-in-factor])

        (config/zoom-out-keys keycode)
        (re-frame/dispatch [::navigation-control/point-zoom config/key-zoom-out-factor])

        (config/panning-left-keys keycode)
        (re-frame/dispatch [::navigation-control/pan config/key-panning-horizontal])

        (config/panning-right-keys keycode)
        (re-frame/dispatch [::navigation-control/pan (- config/key-panning-horizontal)])

        (config/panning-top-keys keycode)
        (re-frame/dispatch [::navigation-control/pan nil config/key-panning-vertical])

        (config/panning-bottom-keys keycode)
        (re-frame/dispatch [::navigation-control/pan nil (- config/key-panning-vertical)])))))


(defn wheel-handler [event]
  (when (aget event "ctrlKey")
    (.preventDefault event))) ;//prevent browserzoom

(defn register-handler []
  (debug "Register zoom handler")
  (js/document.body.removeEventListener "wheel" wheel-handler)
  (js/document.body.removeEventListener "keydown" key-handler)
  (js/document.body.addEventListener "wheel"
                                     wheel-handler
                                     #js{:passive false})
  (js/document.body.addEventListener "keydown"
                                     key-handler))
