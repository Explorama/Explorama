(ns de.explorama.backend.rights-roles.authorization.interface)

(defn login-header-image []
  "/asset/img/explorama_logo_white.svg")

(defprotocol Authorization
  (login-page [intance login-class login-target response input]
    "Returning a html hiccup for the login-page")
  (login-user-form [instance login-info]
    "Map with valid?, response and token 
                    token => valid? = true
                    respones => valid? = false")
  (validate-user-token [instance cookies]
    "Validate if the given cookies are valid.
     Returns user-info, token, expires-in (seconds) and maybe a session (right now only keycloak).")

  (admin-token [instance login-info]
    "Return a admin-token when the given login-info is valid.")
  (logout-user [instance req] "Return a ring-redirect")

  (list-roles [instance]))
