(ns de.explorama.profiling-tool.env
  (:import (java.lang.management ManagementFactory)))

(def ^{:doc "Maximum number of attempts to run finalisers and gc."
       :dynamic true
       :private true}
  *max-gc-attempts* 100)

;;; Memory management
(defn- heap-used
  "Report a (inconsistent) snapshot of the heap memory used."
  []
  (let [runtime (Runtime/getRuntime)]
    (- (.totalMemory runtime) (.freeMemory runtime))))

(defn force-gc
  "Force garbage collection and finalisers so that execution time associated
   with this is not incurred later. Up to max-attempts are made."
  ([] (force-gc *max-gc-attempts*))
  ([max-attempts]
   (loop [memory-used (heap-used)
          attempts 0]
     (System/runFinalization)
     (System/gc)
     (let [new-memory-used (heap-used)]
       (when (and (or (pos? (.. ManagementFactory
                                getMemoryMXBean
                                getObjectPendingFinalizationCount))
                      (> memory-used new-memory-used))
                  (< attempts max-attempts))
         (recur new-memory-used (inc attempts)))))))

(defn thread-wait [ms]
  (Thread/sleep ms))