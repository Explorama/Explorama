(ns de.explorama.frontend.ui-base.utils.timeout
  (:require [clojure.string :refer [includes?]]))

(defn ^:export handle-timeout
  "Will set a timeout of duration 'timeout' for the function 'func' in the atom 'timeout-state'. 
   If another timeout is set to the same atom, the prior one is cleared."
  [timeout-state timeout func]
  (when-let [t @timeout-state]
    (js/clearTimeout t))
  (reset! timeout-state
          (js/setTimeout func timeout)))