(ns de.explorama.frontend.abac.pap
  (:require [ajax.core :as ajax]))

(defn create-policy
  [hostname action-key policy roles]
  (let [body {:name policy
              :feature-id action-key
              :roles roles}
        pap-resp (ajax/POST (str hostname "/pap-register")
                   {:params body
                    :with-credentials? false
                    :format (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :handler (fn [_])
                    :error-handler (fn [err] (.log js/console err))})]
    (if (= (:status pap-resp)
           200)
      (get-in pap-resp [:body :result])
      false)))

(defn register-feature
  ([hostname action-key policy-name roles]
   (create-policy hostname
                  action-key
                  policy-name
                  roles))
  ([hostname action-key policy-name]
   (create-policy hostname
                  action-key
                  policy-name
                  nil)))
