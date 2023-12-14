(ns de.explorama.frontend.rights-roles.path)

(def root :rights-roles)

(def login-form-key :login-form)
(def login-form [root login-form-key])
(defn login-value [value-key]
  (conj login-form value-key))

(def login-root [root :login])
(def logged-in? (conj login-root :logged-in?))
(def try-logout? (conj login-root :try-logout?))
(def user-info (conj login-root :user-info))

(def login-message (conj login-root :login-message :message))
(def ldap-available (conj login-root :login-message :ldap-available))