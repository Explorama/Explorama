(ns de.explorama.shared.common.configs.provider-impl)

(def ^:private config (let [config (aget js/window "EXPLORAMA_CLIENT_CONFIG")]
                        (if config
                          (js->clj config)
                          {})))

(defn lookup [key default]
  (get config key default))

(def config-dir "")

(defn load-logging-config [_])
