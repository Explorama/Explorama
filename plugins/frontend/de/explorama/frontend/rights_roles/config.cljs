(ns de.explorama.frontend.rights-roles.config)

(def debug?
  ^boolean goog.DEBUG)

(def default-namespace :rights-roles)

(def access-token-lifetime (aget js/window "EXPLORAMA_ACCESS_TOKEN_LIFETIME"))
(def normalize-username (aget js/window "EXPLORAMA_WOCO_NORMALIZE_USERNAME"))