(ns de.explorama.backend.rights-roles.attribute-infos.interface)

(def list-user-keys [:name :username :mail])

(defprotocol Attributes
  (refresh-loaded-user-infos [instance] "Used to refresh possible cached user-infos/roles.")
  (username->user-info [instance username] "Retrieving the user-info desc based on username")
  (list-users [instance] "Returning a list of user-infos (name, username, email)")
  (possible-user-attribute-vals [instance attrs] "Returning a list of all possible values for specific attrs"))