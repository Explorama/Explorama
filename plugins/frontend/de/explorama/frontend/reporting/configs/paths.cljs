(ns de.explorama.frontend.reporting.configs.paths)

(def configs-root [:reporting :configs])

(def share-infos (conj configs-root :share))
(def possible-users (conj share-infos :users))
(def possible-roles (conj share-infos :roles))

(defn config-dialog [dialog-key]
  (conj configs-root :dialog dialog-key))

(defn config-dialog-is-active? [dialog-key]
  (conj (config-dialog dialog-key)
        :is-active?))

(defn config-dialog-tag [dialog-key]
  (conj (config-dialog dialog-key)
        :tag))

(defn config-dialog-data [dialog-key]
  (conj (config-dialog dialog-key)
        :data))

(def colors (conj configs-root
                  :colors))