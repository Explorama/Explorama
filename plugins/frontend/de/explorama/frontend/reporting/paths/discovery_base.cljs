(ns de.explorama.frontend.reporting.paths.discovery-base
  (:require [taoensso.timbre :refer [error]]))

(def root-key :reporting)
(def root [root-key])

(def available-size (conj root :available-size))

(def plugins-init-done-key :plugins-init-done)
(def plugins-init-done (conj root plugins-init-done-key))

(def hide-loading-overlay? (conj root :hide-loading-overlay?))
(def global-loadingscreen? (conj root :global-loadingscreen?))

(defn workspace-id [] [root-key :workspace-id])

(def client-id [root-key :client-id])

(def interaction-mode [root-key :interaction-mode])
(def pending-interaction-mode [root-key :pending-interaction-mode])

(def registry [root-key :registry])

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(def login-form-key :login-form)
(def login-form (conj root login-form-key))
(defn login-value [k]
  (conj login-form k))

(def rights-and-roles-key :rights-and-roles)
(def login-root [rights-and-roles-key])

(def logged-in (conj login-root :logged-in?))
(def user-info (conj login-root :user-info))
(def login-message (conj login-root :login-message))
(def ldap-available (conj login-root :ldap-available))

(def init-events [:init-events])

(def tool-descs
  (conj root
        :tools))

(defn tool-desc [tool-id]
  (conj tool-descs
        tool-id))

(def vertical-plugin-apis
  (conj root :plugin-api))

(defn vertical-plugin-api
  ([frame-id]
   (conj vertical-plugin-apis (:vertical frame-id)))
  ([frame-id endpoint]
   (conj vertical-plugin-apis (:vertical frame-id) endpoint))
  ([frame-id endpoint aspect]
   (conj vertical-plugin-apis (:vertical frame-id) endpoint aspect)))

(def active-tab
  (conj root :active-tab))

(def maximized-frame
  (conj root :maximized-frame))

(def module-legend-root [root :frame-legend])

(defn open-legend [frame-id]
  (conj module-legend-root
        frame-id))