(ns de.explorama.frontend.search.backend.direct-search
  (:require [de.explorama.shared.search.ws-api :as ws-api]
            [taoensso.timbre :refer-macros [error debug]]
            [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 ws-api/direct-search
 (fn [_ [_ search-config query]]
   (debug "Search for " search-config " with " query)
   {:backend-tube [ws-api/direct-search {:client-callback [ws-api/direct-search-result (:result-event query)]
                                         :failed-callback [ws-api/failed-handler :init-client]}
                   search-config query]}))

(reg-event-fx
 ws-api/direct-search-result
 (fn [_ [_ callback search-result]]
   (if-not callback
     (error "No callback for direct-search result defined")
     {:dispatch [callback search-result]})))