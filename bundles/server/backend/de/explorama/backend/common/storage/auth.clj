(ns de.explorama.backend.common.storage.auth
  "Helpers for authenticating and authorizing access to storage: Values stored
  by a particular identity (e.g., a client instance, a user, or a combination
  thereof) might only be read or modified by the same identity.

  This consists of two separte protocol, one for Authentication (i.e., turning
  credentials into an identity), one for Authorization (i.e., deciding what the
  given identity allows).

  Attention: Currently, this is only designed to be used within the
  `de.explorama.backend.storage hierarchy.")

(defprotocol AuthenticationStrategy
  "A strategy to turn credentials into an identity."
  (identify [_ credentials]))

(defprotocol AuthorizationStrategy
  "A strategy to decide what a given identity entails."
  (read? [_ credentials owner])
  (write? [_ credentials owner])
  (delete? [_ credentials owner]))

(defn- allows-identical? [owner credentials]
  (= owner credentials))

(defn- allows-client? [[o-client _] [c-client _]]
  (= o-client c-client))

(defrecord IdentityAuthorizationStrategy []
  AuthenticationStrategy
  (identify [_ credentials]
    credentials)
  AuthorizationStrategy
  (read? [_ credentials owner] (allows-identical? owner credentials))
  (write? [_ credentials owner] (allows-identical? owner credentials))
  (delete? [_ credentials owner] (allows-identical? owner credentials)))

(defrecord ClientUserAuthorizationStrategy []
  AuthenticationStrategy
  (identify [_ credentials]
    credentials)
  AuthorizationStrategy
  (read? [_ credentials owner] (allows-client? owner credentials))
  (write? [_ credentials owner] (allows-identical? owner credentials))
  (delete? [_ credentials owner] (allows-client? owner credentials)))

(defn make-identity-auth []
  (IdentityAuthorizationStrategy.))

(defn client-user-auth []
  (ClientUserAuthorizationStrategy.))