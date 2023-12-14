(ns de.explorama.frontend.projects.project.post-processing
  (:require [re-frame.core :refer [reg-event-fx]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [taoensso.timbre :refer [debug]]))

;; Verticals can register an post-processing event which can supports the following registered descripion:
;; {:event-vec <vector> ; Will be dispatched by post-processing here
;;  :type <any> ; Optional: To decide if all post-processings from some type e.g. Layouts are done
;;  :order #{:pre :post}} ; Optional: To decide if it will be executed at the beginning or end for 
;;                         e.g checking if all post-processing of an type triggers some dialog in an later post-processing

;; The event-vec will get the following infos as last parameter:
;; {:type <any>
;;  :callback <event-vec>}  ; Callback-event which must be call by post-processing to continue

(reg-event-fx
 ::execute-next
 (fn [{db :db} [_ post-process-events {:keys [finish-callback read-only?] :as metas}]]
   (let [{next-event-vec :event-vec next-type :type :as next} (first post-process-events)]
     (if (and (vector? next-event-vec)
              (not read-only?))
       (let [next-params {:type next-type
                          :callback [::execute-next
                                     (rest post-process-events)
                                     metas]}]
         (debug "execute next" next)
         {:dispatch (conj next-event-vec next-params)})
       (do
         (debug "Post process done")
         {:dispatch-n [finish-callback]})))))

(reg-event-fx
 ::check-and-execute
 (fn [{db :db} [_ metas]]
   (let [post-process-events (fi/call-api :service-category-db-get db :project-post-processing-events)
         post-process-events (into []
                                   (sort-by (fn [{:keys [order type]}]
                                              (case order
                                                :pre (str type 0)
                                                :post (str type 2)
                                                (str type 1)))
                                            (filter #(vector? (:event-vec %))
                                                    (vals post-process-events))))]
     (debug "check-and-execute" {:post-process-events post-process-events
                                 :metas metas})
     {:dispatch [::execute-next post-process-events metas]})))

