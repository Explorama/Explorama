(ns de.explorama.shared.cache.test-common
  (:require [de.explorama.shared.cache.data-tile.invalidate :as invalidate]))

(def query-match-fn #'invalidate/query-match-fn)

(defn should-cache-fn [prevent-caching-queries]
  (if (empty? prevent-caching-queries)
    (fn [_] true)
    (let [match-fns (mapv query-match-fn prevent-caching-queries)]
      (fn [tile-k]
        (or (empty? match-fns)
            (every? (fn [f]
                      (boolean (not (f tile-k))))
                    match-fns))))))