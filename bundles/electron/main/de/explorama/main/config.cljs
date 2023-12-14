(ns de.explorama.main.config
  (:require [de.explorama.main.wrapper.file :refer [add-to-path write-edn
                                                    read-edn delete-file]]
            [de.explorama.main.wrapper.env :refer [os-name get-env screen-config-exists?]]
            [de.explorama.shared.common.logging :as logging]
            [taoensso.encore   :as enc]
            [cljs-node-io.core :as nio]))

(def app-name "Explorama")

(goog-define ^string DEFAULT_LOG_LEVEL "info")
(goog-define ^string RUNTIME_MODE "dev")

(def dev-mode? (= "dev" RUNTIME_MODE))

(def ^:private app-data-folder-name "app-data")

(def ^:private base-preference-file "base-preferences.edn")
(def ^:private logs-folder-name "logs")
(def ^:private main-log-file "main.log")
(def ^:private backend-log-file "backend.log")
(def ^:private frontend-log-file "frontend.log")

;TODO r1/electron @2knu duplicate with de.explorama.backend.electron.config - why?
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
               (if dev-mode?
                 dev-user-folder
                 app-name)))

(def base-preference-path
  (-> root-path
      (add-to-path app-data-folder-name)
      (add-to-path base-preference-file)))

(def preferences (atom nil))

(defn reload-preferences []
  (let [prefs (read-edn base-preference-path)]
    (when (map? prefs)
      (reset! preferences prefs))))

(defn get-preferences [& {:keys [pref-key]}]
  (when-not @preferences
    (reload-preferences))
  (let [preferences @preferences]
    (cond-> preferences
      (vector? pref-key) (get-in pref-key)
      (keyword? pref-key) (get pref-key))))

(defn save-preferences [preferences & {:keys [merge?]
                                       :or {merge? true}}]
  (when (map? preferences)
    (write-edn base-preference-path preferences merge?)
    (reload-preferences)))

(def crash-dumps-path (add-to-path root-path
                                   "crashDumps"))

(def log-path (-> root-path
                  (add-to-path logs-folder-name)))

(def main-log-path (-> log-path
                       (add-to-path main-log-file)))

(def app-data-path
  (-> root-path
      (add-to-path app-data-folder-name)))

(def mac? (= "Mac" (os-name)))

(def default-window-configs
  {:ui-window {:minWidth 800
               :width 1320
               :minHeight 600
               :height 850
               :maximized? true}
   :worker-window {:width 1200
                   :height 600
                   :show (when dev-mode? true)
                   :web-preferenes (cond-> {}
                                     dev-mode? (assoc :devTools true
                                                      :v8CacheOptions "none"))}

   :loading-window {:width 200
                    :height 200}})

(defn window-config [window-key & {:keys [force-default?]}]
  (let [{:keys [screen-config maximized? web-preferenes width height x y show]}
        (get-preferences {:pref-key window-key})
        default-conf (get default-window-configs window-key)]
    (if force-default?
      default-conf
      (cond-> default-conf
        (and show (= window-key :worker-window))
        (assoc :show true)
        (and (number? width)
             (number? height)
             (number? x)
             (number? y)
             (screen-config-exists? screen-config))
        (assoc :x x
               :y y
               :width width
               :height height
               :center false
               :maximized? maximized?
               :web-preferenes web-preferenes
               :screen-config screen-config)))))


(defn node-spit-appender
  "Returns a simple `spit` file appender for `cljs-node-io`.
    Based on `taoensso.timbre.appenders.core/spit-appender`."
  [& [{:keys [fname append? delete-file-before?]
       :or   {fname "./timbre-spit.log"
              append? true}}]]
  (when delete-file-before?
    (delete-file main-log-path))
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


(def main-log-config
  (when-not mac?
    {:appenders {:spit (node-spit-appender {:fname main-log-path
                                            :delete-file-before? dev-mode?})
                 :println logging/println-appender}}))

 ;; (defn- custom-output [{[mes] :vargs ts :timestamp_}]
;;   (str
;;    (when-let [ts (force ts)]
;;      (str ts " "))
;;    mes))

;; (def frontend-log-config
;;   (when-not mac?
;;     {:min-level (if (is-prod?)
;;                   :info
;;                   :debug)
;;      :output-fn custom-output
;;      :appenders {:spit (node-spit-appender {:fname frontend-log-file})}}))

;; (def backend-log-config
;;   (when-not mac?
;;     {:min-level (if (is-prod?)
;;                   :info
;;                   :debug)
;;      :output-fn custom-output
;;      :appenders {:spit (node-spit-appender {:fname backend-log-file})}}))