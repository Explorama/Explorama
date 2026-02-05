(ns de.explorama.main.wrapper.app
  (:require [electron :refer [app shell dialog MessageChannelMain]]
            [de.explorama.main.wrapper.env :refer [os-name]]
            [path]))

(defn relaunch []
  (.relaunch app))

(defn force-quit []
  (.exit app))

(defn- quit []
  (.quit app))

(defn quit-app []
  (when-not (= "Mac" (os-name))
    (quit)))

(defn exec-when-ready [callback]
  (-> (.whenReady app)
      (.then callback)))

(defn save-dialog [options]
  (.showSaveDialogSync dialog (clj->js options)))

(defn open-file [file]
  (.openPath shell file))

(defn open-link [link]
  (.openExternal shell link))

(defn get-path
  [path-id filename]
  (-> app
      (.getPath path-id)
      (path/join filename)))

;; (defn app-folder [folder-name]
;;   (-> (clj-str/split (app.getAppPath)
;;                      #"resources")
;;       (first)
;;       (add-to-path folder-name)))

(defn user-data-path
  "In Dev and Win: %APPDATA%/Electron
   In Prod and Win: %APPDATA%/Explorama"
  [filename]
  (get-path "userData" filename))

(def temp-path (.getPath app "temp"))

(def documents-path (.getPath app "documents"))

(defn on-app
  [event-name handler]
  (.on app event-name handler))

(defn message-channel []
  (MessageChannelMain.))