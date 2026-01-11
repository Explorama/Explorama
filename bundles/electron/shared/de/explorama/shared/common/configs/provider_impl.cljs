(ns de.explorama.shared.common.configs.provider-impl
  (:require [cljs.reader :as edn]))

(goog-define ^string CONFIG "{}")
(def ^:private config (edn/read-string CONFIG))

(defn lookup [key default]
  (get config key default))

(def config-dir "")

(defn load-logging-config [_])
