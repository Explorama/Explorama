(ns de.explorama.backend.abac.jwt
  (:require [buddy.core.hash :as hash]
            [buddy.sign.jwt :as jwt]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [de.explorama.shared.abac.config :as config]
            [de.explorama.shared.abac.util :as util :refer [user-info-role-fix]]
            [taoensso.timbre :refer [error]]))

(def secret (hash/sha256 config/explorama-shared-secret-key))
(def encryption {:alg :dir :enc :a128cbc-hs256})

(defn token-payload
  "Decrypt the token to get the data."
  [token]
  (jwt/decrypt token secret encryption))

(defn token-valid?
  "Checks if the token is valid."
  [token]
  (if (or (= token "NO-TOKEN")
          (= token "null")
          (nil? token)
          (not (seq token)))
    {:valid? false
     :reason ["no token given"]}
    (try
      (let [claim (token-payload token)]
        (try
          (let [username (:username claim)
                user-role (:role claim)
                exp  (-> (:exp claim)
                         (long)
                         (c/from-long))
                current-time (t/now)
                payload-valid? (and username
                                    user-role
                                    exp)
                time-exp-valid? (or (t/equal? exp
                                              current-time)
                                    (t/before? current-time
                                               exp))]
            {:valid? (and payload-valid?
                          time-exp-valid?)
             :reason (cond-> []
                       (not payload-valid?) (conj "user/role/experation might be missing")
                       (not time-exp-valid?) (conj "the given token is expired. need to login again."))})
          (catch Throwable e
            (error e
                   "Exception while checking validity for token."
                   {:claim claim
                    :exp (:exp claim)
                    :exp-type (type (:exp claim))})
            {:valid? false
             :reason ["invalid token"]})))
      (catch Throwable e
        (error e
               "Exception while decrypting token.")
        {:valid? false
         :reason ["invalid token"]}))))

(defn user-valid?
  ([user-info]
   (user-valid? user-info (:token user-info)))
  ([user-info token]
   (when (seq token)
     (let [claim (token-payload token)
           claimed-infos (select-keys claim [:username :role])
           user-info (-> user-info
                         (select-keys [:username :role])
                         user-info-role-fix)]
       (and (:valid? (token-valid? token))
            (= claimed-infos user-info))))))

(defn- is-admin-role? [role]
  (or (and (vector? role) (some #(= (util/role-lookup "admin") %) role))
      (and (string? role) (= (util/role-lookup "admin") role))))

(defn admin-claim-valid? [token]
  (when (seq token)
    (let [{:keys [role] :as claim} (token-payload token)]
      (and (:valid? (token-valid? token))
           (:admin? claim)
           (is-admin-role? role)))))

(defn admin-token [user-info]
  (when (seq user-info)
    (let [exp
          #_{:clj-kondo/ignore [:type-mismatch]}
          (-> (t/now)
              (t/plus (t/hours config/explorama-admin-token-experation-hours))
              (c/to-long))]
      (when (is-admin-role? (:role user-info))
        (jwt/encrypt (assoc user-info
                            :exp exp
                            :admin? true)
                     secret
                     encryption)))))

(defn user-token
  "Encrpyt the user-info with a experation-time."
  [user-info]
  (let [exp-time
        #_{:clj-kondo/ignore [:type-mismatch]}
        (-> (t/now)
            (t/plus (t/hours config/explorama-token-experation-hours))
            (c/to-long))]
    (jwt/encrypt (assoc user-info :exp exp-time)
                 secret
                 encryption)))

(defn new-user-token
  "Generate a new user-token when the given token is valid."
  ([user-info]
   (new-user-token user-info (:token user-info)))
  ([user-info token]
   (when (user-valid? user-info token)
     (user-token user-info))))