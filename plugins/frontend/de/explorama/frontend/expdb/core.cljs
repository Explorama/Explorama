(ns de.explorama.frontend.expdb.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.expdb.config :as config]
            [de.explorama.frontend.expdb.temp-import.core :as view-core]
            [taoensso.timbre :as log :refer [error]]
            [re-frame.core :as re-frame]))

(def ^:private max-check-tries 100)

(re-frame/reg-event-fx
 ::init-event
 (fn [_ _]
   (let [{tools-register :tools-register-event-vec
          overlay-register :overlay-register-event-vec}
         (fi/api-definitions)]
     {:fx [[:dispatch (overlay-register :expdb-temp-import-view view-core/view)]
           [:dispatch (tools-register {:id "expdb-temp-import"
                                       :action [::view-core/toggle-view]
                                       :vertical config/default-vertical-str
                                       :tool-group :hidden})]]})))

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(defn init []
  (register-init 0))