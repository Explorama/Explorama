(ns de.explorama.backend.concurrent
  (:require [taoensso.timbre :refer [error]])
  (:import java.util.concurrent.Executors
           java.util.concurrent.Callable))

(defn create-thread-pool [size]
  (Executors/newFixedThreadPool size))

(defn submit [thread-pool f]
  (.submit thread-pool ^Callable f))

(defmacro go [thread-pool & body]
  `(submit ~thread-pool
           (fn []
             (try
               ~@body
               (catch Exception e#
                 (error e#)
                 nil)))))