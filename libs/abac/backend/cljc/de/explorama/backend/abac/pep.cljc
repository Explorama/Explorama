(ns de.explorama.backend.abac.pep
  (:require [de.explorama.backend.abac.pdp :as pdp]
            [de.explorama.backend.abac.util :refer [normalize-username]]))

(defn checked-function-call [feature-action user target-obj callback failed-function]
  (let [user (if (map? user)
               (update user :username normalize-username)
               (normalize-username user))
        feature-action-key (if (keyword? feature-action)
                             feature-action
                             (keyword feature-action))
        pdp-result (pdp/call feature-action-key
                             user
                             target-obj)]
    (if (:valid? pdp-result)
      (try (callback (:policy pdp-result))
           (catch #?(:clj Throwable :cljs :default) _ (callback)))
      (failed-function (:failed pdp-result)))))
