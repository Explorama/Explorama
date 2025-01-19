(ns de.explorama.backend.rights-roles.attribute-infos.adapter-no-credentials
  (:require [de.explorama.backend.rights-roles.attribute-infos.interface :as interface]))

(def ^:private dummy-user {:username "admin"
                           :role "admin"
                           :name "Admin"
                           :mail "test@test.de"})

(defn- username->user-info [] dummy-user)

(defn- list-users []
  [(select-keys dummy-user interface/list-user-keys)])

(defn- possible-user-attribute-vals [attrs]
  (let [all-attrs {:mail ["test@test.de"]}]
    (if (seq attrs)
      (select-keys all-attrs attrs)
      all-attrs)))

(deftype Attr-No-Credentials []
  interface/Attributes
  (refresh-loaded-user-infos [_])
  (username->user-info [_ _]
    (username->user-info))
  (list-users [_]
    (list-users))
  (possible-user-attribute-vals [_ attrs]
    (possible-user-attribute-vals attrs)))

(defn new-instance []
  (->Attr-No-Credentials))