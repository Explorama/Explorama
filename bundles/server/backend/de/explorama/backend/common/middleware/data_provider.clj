(ns de.explorama.backend.common.middleware.data-provider)

(defonce provider (atom {}))

(defn register-provider [identifier desc]
  (swap! provider assoc (keyword identifier) desc))

(defn data-tile-ref [identifier desc]
  ((get-in @provider [(keyword identifier)
                      :data-tile-ref])
   desc))