(ns de.explorama.frontend.common.fi.path)

(def ^:private root [:user-preferences])

(def user-preferences (conj root :values))

(def user-preferences-loaded (conj root :loaded?))

(defn user-preference [k]
  (conj user-preferences k))

(def user-preferences-watcher (conj root :watcher))

(defn user-preference-watcher [k]
  (conj user-preferences-watcher k))
