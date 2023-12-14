(ns de.explorama.backend.abac.user-attributes.core
  (:require [de.explorama.backend.abac.user-attributes.repository :as repo]
            [de.explorama.backend.abac.user-attributes.expdb-repo :as backend]))

;;;USER ATTRIBUTES
(def repo-backend (backend/new-instance))

(defn fetch-attr-values [attr]
  (repo/get-attribute repo-backend attr))

(defn add-attr-value [attr value]
  (repo/add-attribute repo-backend attr value))
