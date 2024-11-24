(ns de.explorama.frontend.woco.navigation.fullscreen-handler
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.common.i18n :as i18n]
            [taoensso.timbre :refer-macros [debug]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]))

(defn toggle-fullscreen
  "Activates/Deactivates fullscreen if available"
  [flag]
  (let [elem js/document.documentElement
        fullscreen-active? (aget js/document "fullscreen")]
    (if flag
      (cond
        (and (not fullscreen-active?)
             (aget elem "requestFullscreen"))
        (.requestFullscreen elem)
        (and (not fullscreen-active?)
             (aget elem "webkitRequestFullscreen")) ;;Safari
        (.webkitRequestFullscreen elem)
        (and (not fullscreen-active?)
             (aget elem "msRequestFullscreen")) ;;IE11
        (.msRequestFullscreen elem))
      (cond
        (and fullscreen-active?
             (aget js/document "exitFullscreen"))
        (.exitFullscreen js/document)
        (and fullscreen-active?
             (aget js/document "webkitExitFullscreen")) ;;Safari
        (.webkitExitFullscreen js/document)
        (and fullscreen-active?
             (aget js/document "msExitFullscreen")) ;;IE11
        (.msExitFullscreen js/document)))))

(re-frame/reg-event-db
 ::toggle-fullscreen
 (fn [db [_ flag]]
   (let [old-state (get-in db path/fullscreen?)
         db (cond-> db
              (boolean? flag) (assoc-in path/fullscreen? flag)
              (nil? flag) (update-in path/fullscreen? not))
         new-state (get-in db path/fullscreen?)]
     (when (not= old-state new-state)
       (toggle-fullscreen new-state))
     db)))

(re-frame/reg-sub
 ::fullscreen?
 (fn [db]
   (get-in db path/fullscreen? false)))

(defn- fullscreenchange-handler
  "For detecting changes which are triggered from user without button click like ESC oder X"
  []
  (let [fullscreen? (aget js/document "fullscreen")]
    (re-frame/dispatch [::toggle-fullscreen fullscreen?])))

(defn register-handler []
  (debug "Register fullscreen handler")
  (js/document.removeEventListener "fullscreenchange" fullscreenchange-handler)
  (js/document.removeEventListener "mozfullscreenchange" fullscreenchange-handler)
  (js/document.removeEventListener "webkitfullscreenchange" fullscreenchange-handler)
  (js/document.removeEventListener "msfullscreenchange" fullscreenchange-handler)

  (js/document.addEventListener  "fullscreenchange" fullscreenchange-handler)
  ;firefox
  (js/document.addEventListener  "mozfullscreenchange" fullscreenchange-handler)
  ;Chrome, Safari and Opera
  (js/document.addEventListener  "webkitfullscreenchange" fullscreenchange-handler)
  ;IE, Edge
  (js/document.addEventListener  "msfullscreenchange" fullscreenchange-handler))

(defn toggle []
  (let [fullscreen? @(re-frame/subscribe [::fullscreen?])
        {:keys [navigation-fullscreen
                navigation-exit-fullscreen]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :navigation-fullscreen
                              :navigation-exit-fullscreen])]
    {:id "viewport-fullscreen"
     :title (if fullscreen?
              navigation-exit-fullscreen
              navigation-fullscreen)
     :icon (if fullscreen?
             :minimize
             :maximize)
     :on-click #(re-frame/dispatch [::toggle-fullscreen])
     :active? fullscreen?}))
