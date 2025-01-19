(ns de.explorama.main.core
  (:require [electron :refer [app crashReporter dialog]]
            [process]
            [de.explorama.shared.common.logging :as logging]
            [de.explorama.main.wrapper.app :refer [relaunch force-quit exec-when-ready relaunch quit-app on-app message-channel open-file]]
            [de.explorama.main.wrapper.file :refer [delete-folder]]
            [de.explorama.main.wrapper.window :refer [clear-cache clear-storage-data hide]]
            [de.explorama.main.config :as config]
            [de.explorama.main.wrapper.env :refer [app-env get-displays]]
            [de.explorama.main.windows.ui :as ui-window]
            [de.explorama.main.windows.worker :as worker-window]
            [de.explorama.main.windows.loading :as loading-window]
            [de.explorama.main.renderers-api :refer [init-renderers-api]]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [taoensso.timbre :refer-macros [info error]]))

(logging/set-log-level config/DEFAULT_LOG_LEVEL
                       {:appenders-conf (:appenders config/main-log-config)})

(defonce ^:private *ui-worker-channel (atom nil))

(defn- init-ui-worker-channel []
  (let [channel (message-channel)
        ui-message-port (aget channel "port1")
        worker-message-port (aget channel "port2")]
    (reset! *ui-worker-channel channel)
    (ui-window/post-message "workerPort" #js[worker-message-port])
    (worker-window/post-message "uiPort" #js[ui-message-port])))

(defn- reset-windows []
  (worker-window/close)
  (loading-window/close)
  (ui-window/close)
  (reset! *ui-worker-channel nil))

(defn- crash-handler []
  (error "Critical app crash - exit app")
  (hide (ui-window/win-ref))
  (hide (loading-window/win-ref))
  (hide (worker-window/win-ref))
  (go
    (let [;;for some reason its not possible to restart portable app
          can-restart? (or (not (:portable? app-env))
                           config/dev-mode?)
          actions [{:option "Close"
                    :action :close}
                   {:option "Restart app"
                    :show? can-restart?
                    :action :restart}
                   {:option "Open app content folder"
                    :action :open-folder}
                   {:option "Do a factory reset"
                    :action :factory-reset}]
          actions (reduce (fn [acc {:keys [show?] :as option}]
                            (cond-> acc
                              (or (nil? show?)
                                  (true? show?))
                              (conj option)))
                          []
                          actions)
          action
          (dialog.showMessageBoxSync
           #js{"title" config/app-name
               "message" (str "Critical app crash - Mabe a restart solves it\n\n"
                              "If this is not solving the problem, please send the following content to developer team:\n"
                              " - " config/crash-dumps-path "\n"
                              " - " config/log-path "\n"
                              "    (Hint: It might be that application data is visible through this files)")
               "type" "error"
               "buttons" (clj->js (mapv :option actions))})
          {:keys [action]} (get actions action)]
      (when (= action :restart)
        (relaunch))
      (when (= action :open-folder)
        (open-file config/root-path))
      (when (= action :factory-reset)
        (let [reset-action
              (dialog.showMessageBoxSync
               #js{"title" config/app-name
                   "message" "WARNING - All application data will be deleted - This can not be undone\n\nDo you really want to do a factory reset?"
                   "type" "warning"
                   "buttons" #js["Yes", "No"]})]
          (when (= reset-action 0)
            (info "Execute factory reset on" config/root-path)
            (delete-folder config/app-data-path)
            (<p! (clear-cache (worker-window/win-ref)))
            (<p! (clear-storage-data (worker-window/win-ref)))
            (<p! (clear-cache (ui-window/win-ref)))
            (<p! (clear-storage-data (ui-window/win-ref)))
            (<p! (clear-cache (loading-window/win-ref)))
            (<p! (clear-storage-data (loading-window/win-ref)))
            (dialog.showMessageBoxSync
             #js{"title" config/app-name
                 "message" (if can-restart?
                             "Factory reset done. Application will be restarted now"
                             "Factory reset done. Please restart Application")
                 "type" "info"})
            (when can-restart?
              (relaunch))))))
    (force-quit)))

(defn create-app []
  (when (and (not (ui-window/win-exists?))
             (not (worker-window/win-exists?)))
    (exec-when-ready (fn []
                       (init-renderers-api crash-handler)
                       (loading-window/create crash-handler)
                       (ui-window/create crash-handler)
                       (worker-window/create crash-handler)
                       (init-ui-worker-channel)))))

(defn reload []
  (reset-windows)
  (relaunch)
  (force-quit))

(defn- setup []
  (info "App env\n" (assoc app-env
                           :default-log-level config/DEFAULT_LOG_LEVEL
                           :runtime-mode config/RUNTIME_MODE))

  (info "app paths\n" {:userdata (app.getPath "userData")
                       :appData  (app.getPath "appData")
                       :sessionData  (app.getPath "sessionData")
                       :home (app.getPath "home")
                       :temp  (app.getPath "temp")
                       :module   (app.getPath "module")
                       :desktop   (app.getPath "desktop")
                       :documents   (app.getPath "documents")
                       :downloads   (app.getPath "downloads")
                       :logs   (app.getPath "logs")})
  (info "process paths\n" {:home (aget process "env" "HOME")
                           :appData (aget process "env" "APPDATA")})
  (.on process "uncaughtException"
       (fn [err]
         (error err "app crash - uncaughtException from main thread")
         (crash-handler)))
  (info "Set up folders" {:crashdumps config/crash-dumps-path
                          :logs config/log-path
                          :session-data config/root-path})
  (app.setPath "crashDumps" config/crash-dumps-path)
  (app.setPath "sessionData" config/root-path)
  (app.setAppLogsPath config/log-path)
  (crashReporter.start #js{"submitURL" ""
                           "uploadToServer" false}))

(defn ^:export -main []
  (info "Starting app..")
  (setup)
  (reset-windows)
  (on-app "ready" create-app)
  (on-app "activate" create-app)
  (on-app "window-all-closed" quit-app))

(set! *main-cli-fn* -main) ""