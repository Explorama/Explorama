(ns de.explorama.benchmarks.expdb.data)

(def ^:private modulus 2147483647)

(defn seeded-rng
  "Park-Miller minimal-standard PRNG; identical sequences on JVM and JS."
  [seed]
  (let [state (atom (inc (mod (Math/abs seed) (dec modulus))))]
    (fn []
      (swap! state (fn [s] (mod (* 16807 s) modulus))))))

(def ^:private alphabet "abcdefghijklmnopqrstuvwxyz0123456789")

(defn- rand-str [rng len]
  (apply str (map (fn [_] (nth alphabet (mod (rng) (count alphabet))))
                  (range len))))

(defn- pad2 [n]
  (if (< n 10) (str "0" n) (str n)))

(defn kv-pairs [seed n value-size]
  (let [rng (seeded-rng seed)]
    (mapv (fn [i]
            [(str "/de.explorama.backend.expdb/dt/default/data/"
                  (rand-str rng 16) "-" i)
             {:id i
              :name (rand-str rng 12)
              :count (mod (rng) 100000)
              :tags [(rand-str rng 6) (rand-str rng 6)]
              :payload (rand-str rng (max 0 (- value-size 120)))}])
          (range n))))

(defn import-payload [seed years countries events-per-tile]
  (let [rng (seeded-rng seed)
        contexts (mapv (fn [c] {:name (str "country" c)
                                :global-id (str "c" c)
                                :type "country"})
                       (range countries))
        items (vec (for [y (range years)
                         c (range countries)
                         e (range events-per-tile)]
                     (let [year (+ 1990 y)]
                       {:global-id (str "i-" year "-" c "-" e)
                        :features [{:global-id (str "f-" year "-" c "-" e)
                                    :facts [{:name "fact1"
                                             :type "integer"
                                             :value (mod (rng) 1000000)}
                                            {:name "fact2"
                                             :type "decimal"
                                             :value (+ 0.5 (mod (rng) 1000))}]
                                    :locations [{:lat (mod (rng) 90)
                                                 :lon (mod (rng) 180)}]
                                    :context-refs [{:global-id (str "c" c)}]
                                    :dates [{:type "occured-at"
                                             :value (str year "-"
                                                         (pad2 (inc (mod (rng) 12))) "-"
                                                         (pad2 (inc (mod (rng) 28))))}]
                                    :texts [(rand-str rng 40)]}]})))]
    {:payload {:contexts contexts
               :datasource {:name "bench-ds" :global-id "bench-ds"}
               :items items}
     :dt-keys (vec (for [y (range years)
                         c (range countries)]
                     {"year" (str (+ 1990 y))
                      "country" (str "country" c)
                      "datasource" "bench-ds"
                      "bucket" "default"
                      "identifier" "search"}))}))
