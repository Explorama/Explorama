(ns de.explorama.backend.projects.abac)

(defn policy-id [_project-id _suffix] nil)

(defn fetch-policy [_policy-id]
  nil)

(defn create-policy-map [_project-id _project-name _creator _suffix]
  {})

(defn has-access? [_user _project-id _prefix]
  true)

(defn create-policy [_ _] nil)
(defn delete-policy [_] nil)
(defn edit-policy [_ _] nil)
