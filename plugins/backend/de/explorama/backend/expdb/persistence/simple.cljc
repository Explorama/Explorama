(ns de.explorama.backend.expdb.persistence.simple
  (:refer-clojure :exclude [get set]))

(defprotocol Simple
  (schema [instance])

  (dump [instance]) ;TODO r1/db make sure to not implement this for the server bundle
  (set-dump [instance data]) ;TODO r1/db make sure to not implement this for the server bundle

  (del [instance key])
  (del-bucket [instance])

  (get [instance key])
  (get+ [instance] [instance keys])

  (set [instance key value])
  (set+ [instance data]))
