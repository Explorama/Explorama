(ns de.explorama.frontend.abac.pep
  (:require [ajax.core :as ajax]))

(defn check-pdp-call
  ([endpoint action-key user handler]
   (check-pdp-call endpoint action-key user nil handler))
  ([endpoint action-key user data handler]
   (let [body {:feature action-key
               :user user
               :data data}]
     (ajax/ajax-request {:uri endpoint
                         :method :post
                         :params body
                         :format (ajax/json-request-format)
                         :response-format (ajax/json-response-format {:keywords? true})
                         :handler handler
                         :error-handler (fn [err] (.log js/console err))}))))

(defn resp-handler [callback failed-callback [ok resp]]
  (if (and ok
           (get-in resp [:result :valid?]))
    (callback)
    (failed-callback (get-in resp [:result :failed]))))

(defn call-if-true [endpoint action-key user target-obj callback failed-callback]
  ;(println "check call")
  (let [handler (partial resp-handler callback failed-callback)]
    (check-pdp-call endpoint action-key user target-obj handler)))
