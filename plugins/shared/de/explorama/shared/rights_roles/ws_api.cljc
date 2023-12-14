(ns de.explorama.shared.rights-roles.ws-api)

(def logged-in :rights-roles/logged-in) ;Login success, returns user-info with a new token

(def validate-token ::validate-token) ;Checks the token and returns a new one
(def token-valid :rights-roles/token-valid) ;Validation valid result
(def token-invalid :rights-roles/token-invalid) ;Validation invalid result

(def token-payload ::token-payload)

(def all-user-roles ::all-user-roles)
(def all-user-roles-result :rights-roles/all-user-roles)

(def blacklist-role ::blacklist-role)
(def blacklist-failed ::blacklist-role-failed)
