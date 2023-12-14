(ns de.explorama.backend.configuration.persistence.i18n.api
  (:require [de.explorama.backend.configuration.persistence.i18n.core :as persistence]))

(defn get-labels [{:keys [client-callback failed-callback user-validation]}
                  [user-info lang]]
  (if (user-validation user-info)
    (client-callback
     (persistence/get-labels lang))
    (failed-callback)))

(defn set-labels [{:keys [client-callback failed-callback user-validation]} labels
                  [user-info labels]]
  (if (user-validation user-info)
    (client-callback (persistence/set-labels labels))
    (failed-callback)))

(defn get-translations [{:keys [client-callback failed-callback user-validation]}
                        [user-info lang]]
  (if (user-validation user-info)
    (client-callback
     (persistence/get-translations lang))
    (failed-callback)))