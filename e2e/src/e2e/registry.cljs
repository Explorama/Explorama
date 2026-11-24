(ns e2e.registry)

(defonce registry (atom []))

(defn defspec [spec-name run-fn]
  (swap! registry conj {:name spec-name :run run-fn}))

(defn export! []
  (set! (.-explorama_e2e js/global)
        #js {:specs (into-array (map (fn [{:keys [name run]}]
                                       #js {:name name :run run})
                                     @registry))}))
