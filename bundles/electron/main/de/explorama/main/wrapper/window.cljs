(ns de.explorama.main.wrapper.window
  (:require [electron :refer [BrowserWindow]]
            [de.explorama.main.wrapper.env :refer [static-file-path get-window-display]]
            [taoensso.timbre :refer-macros [error]]
            [path]))

(defn- event->browser-window [event]
  (-> (aget event "sender")
      (BrowserWindow.fromWebContents)))

(defn clear-cache [win]
  (if (and win (aget win "webContents"))
    (-> (aget win "webContents" "session")
        (.clearCache))
    (js/Promise.resolve false)))

(defn clear-storage-data [win]
  (if (and win (aget win "webContents"))
    (-> (aget win "webContents" "session")
        (.clearStorageData [] #()))
    (js/Promise.resolve false)))

(defn on-crash [win callback-fn]
  (when (and win
             (fn? callback-fn)
             (aget win "webContents"))
    (-> (aget win "webContents")
        (.on "render-process-gone" (partial callback-fn "render-process-gone")))))

(defn send-to-window-webcontent [win api-key content]
  (when (and win (aget win "webContents"))
    (.send (aget win "webContents")
           api-key
           content)))

(defn send-to-window [event api-key content]
  (.send (aget event "sender")
         api-key
         content))

(defn post-message [win api-key _ data]
  (.postMessage (aget win "webContents")
                api-key
                nil
                data))

(defn get-url [filename]
  (str "file://" (static-file-path filename)))

(defn load-url
  [window filename]
  (let [file-url (get-url filename)]
    (.loadURL window file-url)))

(defn on
  [emitter event-name handler]
  (.on emitter event-name handler))

(defn once
  [window event function]
  (.once window event function))

(defn close
  [window]
  (when window
    (.close window)))

(defn set-always-on-top [window flag]
  (when window
    (.setAlwaysOnTop window flag)))

(defn show
  [window]
  (when window
    (.show window)))

(defn hide
  [window]
  (when window
    (.hide window)))

(defn focus
  [window]
  (when window
    (.focus window)))

(defn on-move [window callback]
  (on window "move" (partial callback :move)))

(defn on-resize [window callback]
  (on window "resize" (partial callback :resize)))

(defn on-maximize [window callback]
  (on window "maximize" (partial callback :maximize)))

(defn on-unmaximize [window callback]
  (on window "unmaximize" (partial callback :unmaximize)))

(defn screenbased-listeners [window callback]
  (let [timeout (atom nil)
        c (fn [action]
            (when-let [t @timeout]
              (js/clearTimeout t))
            (reset!
             timeout
             (js/setTimeout (fn []
                              (reset! timeout nil)
                              (try
                                (let [bounds (.getNormalBounds window)]
                                  (callback
                                   action
                                   (-> (js->clj bounds :keywordize-keys true)
                                       (assoc :maximized? (.isMaximized window))
                                       (assoc :screen-config (get-window-display window)))))
                                (catch :default e
                                  (error e "Failed to update screen preferences"))))
                            1000)))]
    (on-move window c)
    (on-resize window c)
    (on-maximize window c)
    (on-unmaximize window c)))

(defn browser-window
  [options]
  (let [ctor BrowserWindow]
    (ctor. (clj->js options))))
