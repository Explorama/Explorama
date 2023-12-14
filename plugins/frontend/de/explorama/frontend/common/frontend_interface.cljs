(ns de.explorama.frontend.common.frontend-interface
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]))

(defn api-definitions []
  (.-FIApi js/window))

(defn api-definition [api]
  (let [definitions (api-definitions)]
    (if (vector? api)
      (get-in definitions api)
      (get definitions api))))

(defn call-api [api & params]
  (when (api-definitions)
    (if-let [api-def (api-definition api)]
      (if (vector? api-def)
        (apply conj api-def params)
        (apply api-def params))
      (error "API definition not found for key" api))))

(def ^:private default-interceptor (re-frame/->interceptor :id ::do-nothing
                                                           :before (fn [ctx] ctx)))

(defn ui-interceptor []
  (if-let [inter (.-uiInterceptor js/window)]
    inter
    default-interceptor))