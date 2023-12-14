(ns de.explorama.backend.common.storage.transient
  "Transient Key Value Storage

  The transient kv-storage is a credential-associated key-value-storage
  implementation of the de.explorama.backend.storage/KVStorage protocol with a grace period.
  A value can be associated to a key (key value storage), and can only be
  overriden or evicted from storage using the same credentials.

  A key can be evicted with a grace period, i.e. the value associated to that
  key stays within storage for the given amount of time. It will not be
  accessible after that grace period.

  If a new value is stored for a key using the same credentials, the old value
  is overriden. Storing a new value within the grace period will remove the
  grace period, keeping the value until eviction by consuming code.

  Create a new instance using the `make-instance` function."
  (:require [taoensso.timbre :as log]
            [de.explorama.backend.storage.auth :as auth]
            [de.explorama.backend.storage :as storage]))

(comment
  "In this namespace, key-value-tuples are stored in a map (usually named `m`,
  mapping from some arbitrary typed `key` to a map (named `value-container`,
  usually).

  That value-container contains a given arbitrary :auth object.

  Keys can be 'busy' (a value is stored) or 'free' (key is not associated a
  value), and as keys can be busy with a grace-period, they can be in an
  intermediate state of being busy, but marked to become 'free' at a certain
  timestamp.

  This information is given as `:evict-at` entry in the value-container map.

  However, to not grow storage indefinitely, keys need to be removed. In case of
  a no-grace period eviction, keys are removed immediately (i.e., dissoc'ed from
  the instance).

  Keys evicted with a grace period will be marked with a timestamp and
  'garbage collected' from time to time. In order to prevent growth even in
  the most bizzare usages, we need to garbage collect in two different cases:
  when we store new keys (to prevent memory leaks in long sequences of
  storing and evicting) and when we read values (to reduce memory consumption
  when we store a large number of keys and evict them afterwards in a second
  phase). Garbage collecting on read also prevents exposing key value tuples
  exceeding their grace period.

  To speed up garbage collection, we do not iterate all keys for the
  :evict-at entry, but cache this information as metadata to the map.
  Thus, we only need to iterate the `keys` in that set to find out
  whether a key evicted with grace period is really due removal.")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions.
;;

(defn- now
  "Wraps System/currentTimeMillis in order to redef it during tests"
  []
  (System/currentTimeMillis))

(defn- gc?
  "Checks whether a key/value marked for gc actually needs to be cleaned.
   It is not necessary if the grace period is not over yet or the key
   is stored again (lifting grace period).

   Returns true, if a key needs to be gc'ed, false if it does not need to
   be gc'ed (but will be after the grace period) or :undefined, if a key
   is stored again (signalled by the :evict-at information being undefined)."
  [m k]
  (if-let [evict-at
           (:evict-at (get m k))]
    (< evict-at (now))
    :undefined))

(defn- maybe-gc-key
  "Checks and performs a gc of a single key if necessary"
  [m k]
  (case (gc? m k)
    true (vary-meta (dissoc m k)
                    update :evicted disj k)
    false m
    :undefined (vary-meta m
                          update :evicted disj k)))

(defn- garbage-collect
  "Performs a gc on all keys if necessary.
  Relies on :evicted meta information."
  [m]
  (loop [m m evicted (:evicted (meta m))]
    (if (seq evicted)
      (recur
       (maybe-gc-key m (first evicted))
       (rest evicted))
      m)))

(defn- value [value-container]
  (:value value-container))

(defn- -owner [value-container]
  (:auth value-container))

(defn- no-value? [value-container]
  (empty? value-container))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pure functions: This is the basis for the stateful storage
;; implementation.
;;
;; `m` is the hash-map backing the key-value storage.
;; `k` is the access key
;;
;; `read?`, `write?`, and `delete?` are functions with a single argument (the
;; identity owning a key in the map), return true if it is ok to read, write or
;; delete the entry. Thus, these functions wrap authorization (what's allowed)
;; and, if necessary, authentication (who's asking).

(defn- retrieve* [m k read?]
  "Retrieves a value from map.
  If it is not allowed to read the value, returns nil"
  (when-let [value-container (get m k)]
    (when (read? (-owner value-container))
      (value value-container))))

(defn- store*
  "Stores a key/value tuple for given credentials.

  In order to prevent memory leaks (by storing/evicting huge numbers of
  keys), this garbage collection removes evicted keys beyond their grace
  period.

  Throws an exception if it is not ok to write? to a key.
  "
  [m k write? owner value]
  (garbage-collect
   (let [value-container (get m k)]
     (if (or (no-value? value-container)
             (write? (-owner value-container)))
       (assoc m k
                 ;; this will set a new value, also potentially getting
                 ;; rid of an existing :evict-at entry, i.e. re-store
                 ;; in the grace period.
              {:auth  owner ;; TODO: Move up. Do not use credentials here.
               :value value})
       (throw (ex-info "Key is busy already."
                       {:reason      ::busy-already
                        ::k          k
                        ::debug-info value-container}))))))

(defn- grace-time
  "Determins the new time to (really) evict a key/value tuple based upon a
   potentially already existing value and the new end of the grace period."
  [evict-at-old evict-at-new]
  (if evict-at-old
    (min evict-at-old evict-at-new)
    evict-at-new))

(defn- grace*
  "Adds grace period information to the key. This consists of both :evict-at
  in the map itself and evicted marker set metadata."
  [m k grace-ms]
  (vary-meta (update-in m [k :evict-at] grace-time grace-ms)
             update :evicted (fnil conj #{}) k))

(defn- evict*
  "evicts a key. If no grace-period-ms is given (i.e., arg is nil or not given),
   the key is evicted immediately. Otherwise, it stays in storage for the given
   amount of time (in milliseconds).

   Only the credentials used to store the key/value tuple may be used to evict
   the key. Using other credentials will thrown an Exception. Attention - this
   might leak information, so in case you do not want to expose this, catch the
   Exception and handle it appropriately."
  [m k delete? & [grace-period-ms]]
  (let [value-container (get m k)]
    (if (or (no-value? value-container)
            (delete? (-owner value-container)))
      (if grace-period-ms
        (grace* m k (+ (now) grace-period-ms))
        (dissoc m k))
      (throw (ex-info "Key is busy from other credentials."
                      {:reason      ::unauthorized
                       ::k          k
                       ::debug-info value-container})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Low-level functions for storage/MassEvictionStorage protocol implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- refresh-entry [write? owner [k value-container]]
  (let [auth (-owner value-container)]
    [k (if (write? auth)
         (if (= owner auth)
           {:auth  auth                                       ;; TODO: Move up. Do not use credentials here.
            :value (value value-container)}
           value-container)
         (throw (ex-info "Refreshing is not allowed."
                         {:reason      ::not-allowed
                          ::k          k
                          ::debug-info value-container})))]))

(defn- refresh-all* [m write? owner]
  (garbage-collect (with-meta (into {} (map (partial refresh-entry write? owner) m)) (meta m))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- grace-entry*
  "Adds grace period information to the key. This consists of both :evict-at
  in the map itself and evicted marker set metadata."
  [m k value-container grace-ms]
  (vary-meta (assoc m k (update value-container :evict-at grace-time grace-ms))
             update :evicted (fnil conj #{}) k))

(defn- evict-entry [delete? owner grace-period-ms m k value-container]
  (let [auth (-owner value-container)]
    (if (= owner auth)
      (if (delete? auth)
        (if grace-period-ms
          (grace-entry* m k value-container (+ (now) grace-period-ms))
          (vary-meta m update :evicted disj k))
        (throw (ex-info "Refreshing is not allowed."
                        {:reason      ::not-allowed
                         ::k          k
                         ::debug-info value-container})))
      (assoc m k value-container))))

(defn- evict-all*
  [m delete? owner & [grace-period-ms]]
  (reduce-kv (partial evict-entry delete? owner grace-period-ms) (with-meta {} (meta m)) m))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Stateful implementation: These functions operate on the state (`storage`),
;; which is an atom containing the map backing the key-value storage.
;;
;; Attention: These functions are marked private, so nobody uses these by
;; accident. Yet, they are totally viable and other implementations can and
;; actually *are* using them, namely `de.explorama.backend.projects.projects.locks`.
;; There are separate tests for these, etc.
;;
;; However, you should *not* consume this without a deep understanding of the
;; internals - unlike the TransitionStorage instance you can create using
;; the make-instance function.
;;
;; That's the only function from this namespace consuming code should use in
;; general. All other functions should be used through the
;; de.explorama.backend.storage/KVStorage protocol.

(defn- make-storage []
  (atom {}))

(defn- retrieve [storage k read?]
  (retrieve* (swap! storage garbage-collect) k read?))

(defn- store
  "Store a key-value tuple if the key was not in use before or associated to the
   same credentials.
   Return the last value, potentially transformed by the `transform` function.
   Return nil if key was not in use.

   Apply fn `apply-to-new!` to new storage if storage did contain old value.
   "
  [storage k write? owner v & {transform :transform
                               apply-to-new! :apply-to-new!
                               :or {transform value
                                    apply-to-new! nil}}]
  (log/trace "Storing key" {::key k
                            ;; do not leak information
                            #_#_::owner owner
                            #_#_::value v})
  (let [[old new] (swap-vals! storage store* k write? owner v)
        value-container (get old k)]
    (when apply-to-new!
      (apply-to-new! new))
    (when value-container
      (transform value-container))))

(defn- evict
  "Evicts a key, i.e. dissocs information from storage if called with
   the same credentials.
   Returns the \"old\" value of the tuple."
  [storage k delete? grace-period-ms & {transform :transform
                                        apply-to-new! :apply-to-new!
                                        :or {transform value
                                             apply-to-new! nil}}]
  (log/trace "Evicting key" {::key k})
  (let [[old new] (swap-vals! storage evict* k delete? grace-period-ms)
        value-container (get old k)]
    (when (and value-container apply-to-new!)
      (apply-to-new! new))
    (transform value-container)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State-based functions for storage/MassEvictionStorage protocol implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- refresh-all [storage write? owner & {apply-to-new! :apply-to-new!
                                            :or {apply-to-new! nil}}]
  (log/trace "Refresh all" {::owner owner})
  (let [[_ new] (swap-vals! storage refresh-all* write? owner)]
    (when apply-to-new!
      (apply-to-new! new)))
  true)

(defn- evict-all [storage delete? owner grace-period-ms & {apply-to-new! :apply-to-new!
                                                           :or {apply-to-new! nil}}]
  (log/trace "Evict all" {::owner owner})
  (let [[_ new] (swap-vals! storage evict-all* delete? owner grace-period-ms)]
    (when apply-to-new!
      (apply-to-new! new))
    true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; All wrapped up in a nice record implementation of the Storage protocol.

(defrecord TransientKVStorage [state auth]
  storage/KVStorage
  (retrieve [_ k credentials] (retrieve state k (partial auth/read? auth credentials)))
  (store [_ k credentials v] (store state k (partial auth/write? auth credentials) (auth/identify auth credentials)  v))
  (-evict [_ k credentials grace-period-ms] (evict state k (partial auth/delete? auth credentials) grace-period-ms))
  storage/MassEvictionStorage
  (refresh-all [instance credentials] (refresh-all state (partial auth/write? auth credentials) (auth/identify auth credentials)))
  (-evict-all [instance credentials grace-period-ms] (evict-all state (partial auth/delete? auth credentials) (auth/identify auth credentials) grace-period-ms)))

(defn create-authenticated-kv-storage
  "Given `auth`, which implements both
  `de.explorama.backend.storage.auth/AuthorizationStrategy` and
  `de.explorama.backend.storage.auth/AuthenticationStrategy`, create a new instance of the
  transient storage, to be used "
  [auth]
  {:pre [(satisfies? auth/AuthorizationStrategy auth)
         (satisfies? auth/AuthenticationStrategy auth)]
   :post [(satisfies? storage/KVStorage %)]}
  (->TransientKVStorage (make-storage) auth))
