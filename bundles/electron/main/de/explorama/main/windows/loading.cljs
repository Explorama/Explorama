(ns de.explorama.main.windows.loading
  (:require [de.explorama.main.wrapper.window :as window]
            [de.explorama.main.config :as config]
            [de.explorama.main.wrapper.env :refer [screen-config-exists? screen-center]]
            [taoensso.timbre :refer-macros [info error]]))

(defonce ^:private *loading-window (atom nil))

(def ^:private loading-window-props
  {:webPreferences {:nodeIntegration false}
;;    :icon (static-file-path "img/app/icon_1024.png")
   :frame false
   :show false
;;    :toolbar false
   :focusable false
   :resizable false
   :titleBarStyle "hidden"
   :alwaysOnTop true
   :skipTaskbar true})
;;    :transparent true})

(defn win-ref []
  @*loading-window)

(defn win-exists? []
  (boolean @*loading-window))

(defn post-message [api-key data]
  (when (win-exists?)
    (window/post-message @*loading-window api-key nil data)))

(defn close []
  (when (win-exists?)
    (window/close @*loading-window)
    (reset! *loading-window nil)))

(defn create [crash-handler]
  (let [window-config (config/window-config :loading-window {:force-default? true})
        {ui-screen-config :screen-config} (config/window-config :ui-window)
        window-config (cond-> window-config
                        (screen-config-exists?  ui-screen-config)
                        (merge (screen-center ui-screen-config window-config)))
        loading-window (window/browser-window (cond-> loading-window-props
                                                (map? window-config)
                                                (merge (select-keys window-config [:width :height :x :y]))))]

    (reset! *loading-window loading-window)
    (window/load-url loading-window "loading.html")
    (window/on loading-window "closed" #(reset! *loading-window nil))
    (window/once loading-window
                 "ready-to-show"
                 (fn []
                   (window/show loading-window)
                   (info "loading window created")))
    (window/on-crash loading-window
                     (fn [event-name e details]
                       (let [[reason exit-code]
                             (when details
                               [(aget details "reason")
                                (aget details "exitCode")])]
                         (error "Loading window crashed"
                                {:event-listener event-name
                                 :reason reason
                                 :exit-code exit-code}
                                (str "\n---- See crashdump for more details (" config/crash-dumps-path ") ----\n"))
                         (crash-handler))))))