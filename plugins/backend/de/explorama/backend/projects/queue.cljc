(ns de.explorama.backend.projects.queue)

(defn queue
  #?(:clj ([] (clojure.lang.PersistentQueue/EMPTY))
     :cljs ([] #queue []))
  #?(:clj ([& colls] (reduce conj clojure.lang.PersistentQueue/EMPTY colls))
     :cljs ([& colls] (reduce conj #queue [] colls))))
