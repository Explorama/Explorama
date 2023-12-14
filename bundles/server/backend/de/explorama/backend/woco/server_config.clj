(ns de.explorama.backend.woco.server-config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]
            [clojure.string :as str]))

(def explorama-scheme
  (defconfig
    {:env :explorama-scheme
     :default "http"
     :type :string
     :doc "The scheme (protocol) part of the base URL as which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def explorama-host
  (defconfig
    {:env :explorama-host
     :default "localhost"
     :type :string
     :doc "The host (domain) part of the base URL as which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def explorama-port
  (defconfig
    {:env :explorama-port
     :default 4001
     :type :integer
     :doc "The port on which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients, so port forwarding will likely fail."}))

(def explorama-bind-address
  (defconfig
    {:env :explorama-bind-address
     :default explorama-host
     :type :string
     :doc "The IP address of the interface the HTTP server should listen on."}))

(defn url-port-suffix [port]
  (when (or (and (string? port)
                 (not (str/blank? port)))
            (number? port))
    (str ":" port)))

(def explorama-base-url
  (defconfig
    {:name :explorama-base-url
     :default (str explorama-scheme
                   "://"
                   explorama-host
                   (url-port-suffix explorama-port))
     :doc "The base URL, generated from the configured scheme, host and port."}))

(def explorama-proxy-scheme
  (defconfig
    {:env :explorama-proxy-scheme
     :default explorama-scheme
     :type :string
     :doc "The scheme (protocol) part of the base URL as which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def explorama-proxy-host
  (defconfig
    {:env :explorama-proxy-host
     :default explorama-host
     :type :string
     :doc "The host (domain) part of the base URL as which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def explorama-proxy-port
  (defconfig
    {:env :explorama-proxy-port
     :default explorama-port
     :type :integer
     :doc "The port on which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients, so port forwarding will likely fail."}))

(def explorama-proxy-base-url
  (defconfig
    {:name :explorama-proxy-base-url
     :default (str explorama-proxy-scheme
                   "://"
                   explorama-proxy-host
                   (url-port-suffix explorama-proxy-port))
     :doc "The base URL, generated from the configured scheme, host and port."}))

(defn reverse-proxy? []
  (or explorama-proxy-scheme
      explorama-proxy-host
      explorama-proxy-port))

(def url-prefix* "search")

(defn js-path
  "relative path to the JS files, no leading or trailing slash"
  []
  (str (if (reverse-proxy?)
         (str url-prefix* "/")
         "")
       "js"))

(defn origin []
  (if (reverse-proxy?)
    (str explorama-proxy-base-url
         "/" url-prefix*)
    explorama-base-url))

(def explorama-thread-pool
  (defconfig
    {:env :explorama-thread-pool
     :type :integer
     :default 8
     :doc "Defines how many threads should be used for the thread-pool."}))

(def explorama-http-max-body
  (defconfig
    {:env :explorama-http-max-body
     :default 512
     :type :integer
     :doc "Set the http-max-body size for the server.
         Value is in mb."}))

(def explorama-liveness-exceptions-per-min
  (defconfig
    {:env :explorama-liveness-exceptions-per-min
     :type :integer
     :default 60
     :doc "Threshold for the number of exceptions per minute that are allowed before the liveness check fails."}))

