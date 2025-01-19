(ns de.explorama.main.renderers-api
  (:require [de.explorama.main.windows.ui :as ui-window]
            [de.explorama.main.windows.loading :as loading-window]
            [de.explorama.main.api-util :refer [api-wrap]]
            [de.explorama.main.wrapper.window :as window :refer [send-to-window-webcontent]]
            [de.explorama.main.wrapper.app :refer [force-quit open-link open-file save-dialog]]
            [taoensso.timbre :refer-macros [info debug]]))

(def app-api {:select-directory "selectDirectory"
              :directory-selected "directorySelected"

              :save-dialog "saveDialog"
              :save-dialog-done "saveDialogDone"

              :open-file "openFile"

              :port-initalized "portInitalized"
              :worker-connection-established "workerConnectionEstablished"

              :quit-app "quitApp"
              :force-app-crash "forceAppCrash"
              :open-link "openLink"})

(defn wrap-api-call [api-fn callback-fn params]
  (-> (partial api-fn {:callback-fn callback-fn})
      (apply params)))

(defonce ^:private initialized (atom nil))
(defonce ^:private established (atom nil))

(defn init-renderers-api [crash-handler]
  (reset! initialized nil)
  (reset! established nil)
  (api-wrap (app-api :port-initalized)
            (app-api :port-initalized)
            (fn [_ renderer]
              (debug "Port initalized" renderer)
              (if (and (not @established)
                       (or (and (= renderer "ui")
                                (= @initialized "worker"))
                           (and (= renderer "worker")
                                (= @initialized "ui"))))
                (do
                  (debug "Connection between worker and ui established")
                  (reset! established true)
                  (send-to-window-webcontent (ui-window/win-ref)
                                             (app-api :worker-connection-established) "")
                  (window/show (ui-window/win-ref))
                  (window/focus (ui-window/win-ref))
                  (when (loading-window/win-exists?)
                    (loading-window/close)))
                (reset! initialized renderer)))
            :async)

  (api-wrap (app-api :quit-app)
            (app-api :quit-app)
            (fn [_]
              (force-quit))
            :async)

  (api-wrap (app-api :force-app-crash)
            (app-api :force-app-crash)
            (fn [_]
              (crash-handler))
            :async)

  (api-wrap (app-api :save-dialog)
            (app-api :save-dialog-done)
            (fn [{:keys [callback-fn]} options]
              (debug "SAVE DIALOG" options)
              (callback-fn {:path (save-dialog options)}))
            :async)

  (api-wrap (app-api :open-file)
            (app-api :open-file)
            (fn [_ file]
              (debug "OPEN FILE" file)
              (when (and file (seq file))
                (open-file file)))
            :async)

  (api-wrap (app-api :open-link)
            (app-api :open-link)
            (fn [_ url]
              (when (and url (seq url))
                (open-link url)))
            :async))
