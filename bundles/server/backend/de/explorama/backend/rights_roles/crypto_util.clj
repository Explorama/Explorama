(ns de.explorama.backend.rights-roles.crypto-util
  (:require [clojure.java.io :as io])
  (:import java.security.SecureRandom
           javax.crypto.Cipher
           javax.crypto.KeyGenerator
           javax.crypto.spec.IvParameterSpec
           javax.crypto.spec.SecretKeySpec
           org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder))

(def ^:private s (char-array "XX8bD6!9.@Y8dEB_.77hy@sg9c3o.LA8P-R2ko9bkskob9kZ.kLh@n3N2jgzZ!QCu86*jGra@t6UdJpagzkmzz3VYKZabZU!MzpX"))
(def ^:private aes-algo "AES/CBC/PKCS5PADDING")
(def ^:private rsa-algo "RSA/ECB/PKCS1Padding")
;; The last parameter is optional and is only mandatory
;; if a private key is encrypted.

(java.security.Security/addProvider (org.bouncycastle.jce.provider.BouncyCastleProvider.))

(defn keydata [reader]
  (->> reader
       (org.bouncycastle.openssl.PEMParser.)
       (.readObject)))

(defn pem-string->key-pair
  [string]
  (let [kd (keydata (io/reader (.getBytes string)))
        dec-provider (.build (JcePEMDecryptorProviderBuilder.) s)]
    (.getKeyPair (org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter.)
                 (.decryptKeyPair kd dec-provider))))

(def ^:private priv-key (io/resource "keys/privkey_enc.pem")) ;TODO r1/rights this is super insecure
(def ^:private keypair (pem-string->key-pair (slurp priv-key)))
(def ^:private private-key (.getPrivate keypair))
(def ^:private public-key (.getPublic keypair))

(defn decode64 [str]
  (.decode (java.util.Base64/getDecoder) str))

(defn encode64 [bytes]
  (.encodeToString (java.util.Base64/getEncoder) bytes))

(defn unhexify [s]
  (let [bytes (into-array Byte/TYPE
                          (map (fn [[x y]]
                                 (unchecked-byte (Integer/parseInt (str x y) 16)))
                               (partition 2 s)))]
    bytes))

(defn hexify [coll]
  (let [hex [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f]]
    (letfn [(hexify-byte [b]
              (let [v (bit-and b 0xFF)]
                [(hex (bit-shift-right v 4)) (hex (bit-and v 0x0F))]))]
      (apply str (mapcat hexify-byte coll)))))

(defn decrypt
  [message]
  (let [cipher (doto (javax.crypto.Cipher/getInstance rsa-algo)
                 (.init javax.crypto.Cipher/DECRYPT_MODE private-key))]
    (->> message
         decode64
         (.doFinal cipher)
         (map char)
         (apply str))))

(defn encrypt
  [message]
  (encode64
   (let [cipher (doto (javax.crypto.Cipher/getInstance rsa-algo)
                  (.init javax.crypto.Cipher/ENCRYPT_MODE public-key))]
     (.doFinal cipher (.getBytes message)))))

(defn aes-decrypt [msg secret iv]
  (let [iv-bytes (unhexify iv)
        secret-bytes (unhexify secret)
        iv (IvParameterSpec. iv-bytes)
        secret-key (SecretKeySpec. secret-bytes "AES")
        cipher (Cipher/getInstance aes-algo)]
    (.init cipher Cipher/DECRYPT_MODE secret-key iv)
    (->> (.doFinal cipher (decode64 msg))
         (map char)
         (apply str))))

(defn- random-iv []
  (let [iv (byte-array 16)]
    (.nextBytes (SecureRandom.) iv)
    iv))

(defn- random-secret-key []
  (let [key-gen (KeyGenerator/getInstance "AES")]
    (.init key-gen 256 (SecureRandom/getInstanceStrong))
    (.generateKey key-gen)))

(defn aes-encrypt
  [msg]
  (let [iv-bytes (random-iv)
        secret (random-secret-key)
        cipher (Cipher/getInstance aes-algo)
        iv (IvParameterSpec. iv-bytes)]
    (.init cipher Cipher/ENCRYPT_MODE secret iv)
    {:content (->> (str msg)
                   (.getBytes)
                   (.doFinal cipher)
                   encode64)
     :secret (hexify (.getEncoded secret))
     :initv (hexify iv-bytes)}))
