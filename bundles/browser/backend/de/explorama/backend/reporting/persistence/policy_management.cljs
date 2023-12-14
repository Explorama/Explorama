(ns de.explorama.backend.reporting.persistence.policy-management)

(defn write-access? [_ _] true)

(defn read-access? [_ _] true)

(defn policies-user-attributes [_] {})

(defn create-init-dr-policies [_ _ _ _ _] true)

(defn delete-dr-policies [_] true)

(defn policies-exist? [_] true)

(defn update-dr-policies [_ _ _ _ _ _] true)
