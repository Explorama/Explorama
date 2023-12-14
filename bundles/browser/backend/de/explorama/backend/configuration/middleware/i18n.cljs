(ns de.explorama.backend.configuration.middleware.i18n
  (:require [de.explorama.backend.configuration.persistence.i18n.core :as persistence]))

(defn get-labels [language]
  (persistence/get-labels language))

(defn get-translations [language]
  (persistence/get-translations language))
