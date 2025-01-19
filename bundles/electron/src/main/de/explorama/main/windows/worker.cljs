(ns de.explorama.main.windows.worker
  (:require [de.explorama.main.wrapper.env :refer [static-file-path]]
            [de.explorama.main.wrapper.window :as window]
            [de.explorama.main.config :as config :refer [dev-mode?]]
            [taoensso.timbre :as timbre :refer-macros [info error]]))

(defonce ^:private *worker-window (atom nil))

(def ^:private worker-window-props
  (cond-> {:webPreferences {:nodeIntegration true
                            :contextIsolation false}
                    ;; :enableRemoteModule false
                    ;; :worldSafeExecuteJavaScript true}
          ;;  :icon (static-file-path "app/icon_1024.png")
           :show false}
    dev-mode?
    (assoc :resizable true
           :title "Explorama (Worker) - only visible in dev mode")))

(defn win-ref []
  @*worker-window)

(defn win-exists? []
  (boolean @*worker-window))

(defn post-message [api-key data]
  (when (win-exists?)
    (window/post-message @*worker-window api-key nil data)))

(defn close []
  (when (win-exists?)
    (window/close @*worker-window)
    (reset! *worker-window nil)))

(defn create [crash-handler]
  (let [win-config (config/window-config :worker-window)
        {:keys [maximized? web-preferenes] :as win-config} (when (or dev-mode? (:show win-config))
                                                             win-config)
        worker-window (window/browser-window (cond-> (assoc-in worker-window-props
                                                               [:webPreferences :preload]
                                                               (static-file-path "_preloadWorker.js"))
                                               (map? win-config)
                                               (merge (select-keys win-config
                                                                   [:show :autoHideMenuBar :center :width :height :x :y]))
                                               dev-mode? (assoc :show true)
                                               (map? web-preferenes)
                                               (update :webPreferences merge (select-keys web-preferenes [:v8CacheOptions :backgroundThrottling :devTools]))))]
    (reset! *worker-window worker-window)
    (window/load-url worker-window "worker.html")
    (when dev-mode?
      (window/screenbased-listeners worker-window
                                    (fn [_action window-preferences]
                                      (config/save-preferences {:worker-window window-preferences}))))
    (window/once worker-window "ready-to-show"
                 (fn []
                   (info "worker window created")
                   (when win-config
                     (let [bounds (select-keys win-config [:x :y :width :height])]
                       (when (seq bounds)
                         (.setBounds worker-window (clj->js bounds)))))
                   (when maximized?
                     (.maximize worker-window))
                   (when dev-mode?
                     (.openDevTools worker-window))))
    (window/on worker-window "closed" #(reset! *worker-window nil))
    ;; (window/on (aget worker-window "webContents")
    ;;            "console-message" (fn [e level message line source-id]
    ;;                                (timbre/with-config config/backend-log-config
    ;;                                  (info message))))
    (window/on-crash worker-window
                     (fn [event-name e details]
                       (let [[reason exit-code]
                             (when details
                               [(aget details "reason")
                                (aget details "exitCode")])]
                         (error "Worker crashed"
                                {:event-listener event-name
                                 :reason reason
                                 :exit-code exit-code}
                                (str "\n---- See crashdump for more details (" config/crash-dumps-path ") ----\n"))
                         (crash-handler))))))