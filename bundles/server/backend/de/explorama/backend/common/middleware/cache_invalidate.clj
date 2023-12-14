(ns de.explorama.backend.common.middleware.cache-invalidate
  (:require [clojure.set :as set]))

(defonce functions (atom {}))

(defn register-invalidate [vertical invalidates]
  (swap! functions assoc vertical invalidates))

(defn send-invalidate
  ([sub params]
   (doseq [[vertical] @functions]
     (send-invalidate vertical sub params)))
  ([vertical sub params]
   (doseq [[condition func] (get @functions vertical)
           :when (set/intersection condition sub)]
     (func params))))
