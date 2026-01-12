(ns de.explorama.main.windows.ui
  (:require [de.explorama.main.config :as config :refer [app-name dev-mode?]]
            [de.explorama.main.wrapper.env :refer [static-file-path]]
            [de.explorama.main.wrapper.window :as window]
            [de.explorama.main.wrapper.app :refer [quit-app]]
            [taoensso.timbre :as timbre :refer-macros [info error]]))

(defonce ^:private *ui-window (atom nil))

(def ^:private ui-window-props
  {:webPreferences {:nodeIntegration false
                    :contextIsolation true
                    :enableRemoteModule false
                    :worldSafeExecuteJavaScript true}
  ;;  :icon (static-file-path "app/icon_1024.png")
   ;; :autoHideMenuBar true
   ;  :titleBarStyle "customButtonsOnHover"
   ;:transparent true
   ;; :frame false 
   :resizable true
   :show false
   :title app-name})

(defn win-ref []
  @*ui-window)

(defn win-exists? []
  (boolean @*ui-window))

(defn post-message [api-key data]
  (when (win-exists?)
    (window/post-message @*ui-window api-key nil data)))

(defn close []
  (when (win-exists?)
    (window/close @*ui-window)
    (reset! *ui-window nil)))

(defn create [crash-handler]
  (let [prod? (not dev-mode?)
        {:keys [maximized? web-preferenes] :as win-config} (config/window-config :ui-window)
        ui-window (window/browser-window (cond-> (assoc-in ui-window-props
                                                           [:webPreferences :preload]
                                                           (static-file-path "_preloadUI.js"))
                                           prod?
                                           (assoc :autoHideMenuBar true)
                                           :always (merge (select-keys win-config
                                                                       [:autoHideMenuBar :center :width :height :x :y :minWidth :minHeight]))
                                           (map? web-preferenes)
                                           (update :webPreferences merge (select-keys web-preferenes [:v8CacheOptions :backgroundThrottling :devTools]))))]
    (reset! *ui-window ui-window)
    (window/load-url ui-window "index.html")
    (window/screenbased-listeners ui-window
                                  (fn [_action window-preferences]
                                    (config/save-preferences {:ui-window window-preferences})))
    (window/once ui-window "ready-to-show"
                 (fn []
                   (info "ui window created")
                   (when-not prod?
                     (.openDevTools ui-window))))
    (window/once ui-window "show"
                 (fn []
                   (let [bounds (select-keys win-config [:x :y :width :height])]
                     (when (seq bounds)
                       (.setBounds ui-window (clj->js bounds))))
                   (when maximized?
                     (.maximize ui-window))))
    (window/on ui-window
               "closed"
               #(do
                  (reset! *ui-window nil)
                  (when prod?
                    (quit-app))))
    ;; (window/on (aget ui-window "webContents")
    ;;            "console-message" (fn [e level message line source-id]
    ;;                                (println " console-message ---->>>" message e)
    ;;                                (info e message level line source-id (aget e "type"))
    ;;                                (if (= level 3)
    ;;                                  (timbre/with-config (dissoc config/frontend-log-config :output-fn)
    ;;                                    (error e message (str (js->clj (js/Object.getOwnPropertyNames e))) (type e) (aget e "args")))
    ;;                                  (timbre/with-config config/frontend-log-config
    ;;                                    (info e message)))))
    (window/on-crash ui-window
                     (fn [event-name e details]
                       (let [[reason exit-code]
                             (when details
                               [(aget details "reason")
                                (aget details "exitCode")])]
                         (error "UI crashed"
                                {:event-listener event-name
                                 :reason reason
                                 :exit-code exit-code}
                                (str "\n---- See crashdump for more details (" config/crash-dumps-path ") ----\n"))
                         (crash-handler))))))