(ns de.explorama.shared.cache.util)

(defn single-destructuring [result]
  (-> result first second))

(defn single-return-type [key result]
  {(first key) result})

(defn data-tile-limit-exceeded [events]
  (throw (ex-info "Data-tile limited exceeded"
                  {:events events
                   :reason :data-tile-limit})))