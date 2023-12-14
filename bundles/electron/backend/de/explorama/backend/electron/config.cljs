(ns de.explorama.backend.electron.config
  (:require [cljs-node-io.core :as nio]
            [de.explorama.backend.electron.env :refer [get-env]]
            [de.explorama.backend.electron.file :refer [add-to-path
                                                        create-folder delete-file]]
            [de.explorama.shared.common.logging :as logging] ;; maybe we should switch the config to something else
            [de.explorama.shared.woco.config :as backend-config]
            [path]
            [taoensso.encore   :as enc]))

(def debug? ^boolean goog.DEBUG)
(def app-name "Explorama")
(def ^:private app-data-folder-name "app-data")

(def ^:private logs-folder-name "logs")
(def ^:private backend-log-file "backend.log")

;TODO r1/electron @2knu duplicate with de.explorama.main.config - why?
(defn- windows-app-data []
  (get-env "APPDATA"))

(defn- linux-app-data []
  (when-let [home (get-env "HOME")]
    (add-to-path home ".config")))

(defn- mac-app-data []
  ;TODO r1/mac-support not tested 
  (linux-app-data))

(def ^:private user-data-path (or
                               (windows-app-data)
                               (linux-app-data)
                               (mac-app-data)))

(def ^:private dev-user-folder (str app-name "-dev"))

(def root-path
  (add-to-path user-data-path
               (if debug?
                 dev-user-folder
                 app-name)))

(def logs-path
  (-> root-path
      (add-to-path logs-folder-name)))

(def backend-log-path
  (-> logs-path
      (add-to-path backend-log-file)))

(def app-data-path
  (-> root-path
      (add-to-path app-data-folder-name)))

(create-folder logs-path)
(create-folder app-data-path)

(defn node-spit-appender
  "Returns a simple `spit` file appender for `cljs-node-io`.
    Based on `taoensso.timbre.appenders.core/spit-appender`."
  [& [{:keys [fname append? delete-file-before?]
       :or   {fname "./timbre-spit.log"
              append? true}}]]
  (when delete-file-before?
    (delete-file backend-log-path))
  (enc/have? enc/nblank-str? fname)
  (logging/appender-template
   (fn [_data]
     (fn self [output & [retry?]]
       (try
         (nio/spit fname (str output enc/system-newline)
                   :append append?)
         (catch :default e
           (if retry?
             (throw e)
             (do
               (nio/make-parents fname)
               (self output true)))))))
   logging/to-str-output))

(def log-config
  {:appenders (cond-> {:spit (node-spit-appender {:fname backend-log-path
                                                  :delete-file-before? backend-config/dev-mode?})}
                backend-config/dev-mode?
                (assoc :console logging/devtools-appender))})
                      ;;  :println logging/println-appender))})
