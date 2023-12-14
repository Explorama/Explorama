(ns de.explorama.backend.algorithms.error)

(defn error-cases [e]
  (let [msg (ex-message e)
        error-label (-> (ex-data e) :error-label)]
    {:error? true
     :error  (cond error-label
                   error-label
                   (= "No matching clause:" msg)
                   :invalid-selection-or-missing-attribute-error
                   :else :unknown-error)}))