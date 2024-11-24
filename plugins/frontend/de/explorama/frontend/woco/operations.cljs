(ns de.explorama.frontend.woco.operations
  "This name space handles operations between windows/frames on the working
   context. The operation gets triggered by if both frames want to enchange
   contents that goes past simply connecting, currently set-operations, couple
   and override.
   The negotiations are processed by woco.frame.interaction.connection/connection-negotiation."
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.frame.interaction.move :refer [move-event-call moving-state]]
            [de.explorama.frontend.woco.path :as path]))

(defn- op-di [{:keys [source-di target-di op]}]
  (debug source-di target-di)
  (let [{source-dt-ref :di/data-tile-ref
         source-filter :di/filter
         source-ops :di/operations} source-di
        {target-dt-ref :di/data-tile-ref
         target-filter :di/filter
         target-ops :di/operations} target-di]
    #:di{:data-tile-ref (merge source-dt-ref target-dt-ref)
         :filter (merge source-filter target-filter)
         :operations (if (fn? op)
                       (op source-ops target-ops)
                       (conj (if (vector? op)
                               op
                               [op nil])
                             source-ops
                             target-ops))}))

(re-frame/reg-event-fx
 ::di-creation-done-replay
 (fn [_ [_ di callback-vec]]
   (debug di "created ->" callback-vec)
   {:dispatch-n [callback-vec]}))

(def intersection-by :intersection-by)
(def union :union)
(def difference :difference)
(def sym-difference :sym-difference)
(def couple :couple)
(def override :override)


(def op-behaviour :op-behaviour)
(def couple-behaviour :couple-behaviour)
(def override-behaviour :override-behaviour)

(def couple-behaviour-replay :couple-behaviour-replay)
(def override-behaviour-replay :override-behaviour-replay)

;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; ACTIONS ;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;

(defn map-action [action]
  (get
   {intersection-by ::di-operation
    union ::di-operation
    difference ::di-operation
    sym-difference ::di-operation}
   action
   action))

(defn di-operation-mapping [op params]
  (get
   {intersection-by [:intersection-by (:by params)]
    union [:union nil]
    difference (fn [source target]
                 [:difference nil target source])
    sym-difference [:sym-difference nil]}
   op))

(defmulti action
  (fn [task params source-desc target-desc db]
    task))

(defmethod action ::di-operation [woco-type
                                  {source-frame-id :source-frame-id
                                   target-frame-id :target-frame-id
                                   di :di
                                   callback :callback
                                   op :op}
                                  {:as params}
                                  {source-di :di}
                                  {target-di :di}]
  [op-behaviour
   nil
   {:di di
    :source-di source-di
    :target-di target-di
    :op (di-operation-mapping op params)
    :woco-type woco-type
    :params params
    :callback callback
    :source-frame-id source-frame-id
    :target-frame-id target-frame-id}])

(defmethod action couple [_ {source-frame-id :source-frame-id
                             target-frame-id :target-frame-id}
                          {by :by}
                          _
                          _
                          _]
  [couple-behaviour
   couple-behaviour-replay
   {:source-frame-id source-frame-id
    :target-frame-id target-frame-id
    :params {:by by}}])

(defmethod action override [_
                            {source-frame-id :source-frame-id
                             target-frame-id :target-frame-id}
                            _
                            _
                            _
                            db]
  (let [frame-info (frame-info/gather-information db source-frame-id)]
    [override-behaviour
     override-behaviour-replay
     {:source-frame-id source-frame-id
      :target-frame-id target-frame-id
      :payload frame-info}]))

;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; BEHAVIOUR ;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti behaviour (fn [task params]
                      task))

(re-frame/reg-event-fx
 ::set-op-done
 (fn [_ [_
         {source-frame-id :source-frame-id
          target-frame-id :target-frame-id}
         new-di]]
   {:dispatch-n [[:de.explorama.frontend.woco.frame.api/close source-frame-id]
                 [:de.explorama.frontend.woco.frame.api/recreate
                  target-frame-id
                  {:frame-id target-frame-id
                   :payload {:di new-di}}]]}))

(defmethod behaviour op-behaviour [_ payload]
  (debug op-behaviour payload)
  {:dispatch [::set-op-done
              payload
              (op-di payload)]})

(defmethod behaviour couple-behaviour [_ {source-frame-id :source-frame-id
                                          target-frame-id :target-frame-id
                                          params :params}]
  {:dispatch [:de.explorama.frontend.woco.api.couple/couple
              source-frame-id
              target-frame-id
              params]})

(defmethod behaviour override-behaviour [_
                                         {source-frame-id :source-frame-id
                                          target-frame-id :target-frame-id
                                          payload :payload}]
  {:dispatch [:de.explorama.frontend.woco.frame.api/override
              target-frame-id
              {:frame-source-id source-frame-id
               :frame-id target-frame-id
               :payload payload}]})

(defmethod behaviour couple-behaviour-replay [_ {source-frame-id :source-frame-id
                                                 target-frame-id :target-frame-id
                                                 params :params}]
  {:dispatch [:de.explorama.frontend.woco.api.couple/couple
              source-frame-id
              target-frame-id
              params
              false]})

(defmethod behaviour override-behaviour-replay [_
                                                {target-frame-id :target-frame-id}]
  {:dispatch [:de.explorama.frontend.woco.frame.api/override
              target-frame-id
              {:frame-id target-frame-id}]})

(defn source-or-target-response? [value source-or-target response]
  (cond (and (= :source source-or-target)
             (:target value))
        [response (:target value)]
        (and (= :target source-or-target)
             (:source value))
        [(:source value) response]
        :else []))

(re-frame/reg-event-fx
 ::process
 (fn [{db :db} [_ woco-type {:keys [connection-negotiation-id] :as ctx-infos} source-or-target params response]]
   (when-not (= woco-type :move)
     (debug ::pre-process source-or-target response)
     (let [value (get-in db (path/operation connection-negotiation-id))
           [{source-cancel? :cancel?
             :as source-response}
            {target-cancel? :cancel?
             :as target-response}]
           (source-or-target-response? value source-or-target response)]
       (debug ::process source-response target-response)
       (cond (and source-response target-response)
             (let [[behaviour-type _ params] (action (map-action woco-type)
                                                     (assoc ctx-infos :op woco-type)
                                                     params source-response
                                                     target-response db)
                   result (behaviour behaviour-type params)]
               (assoc result
                      :db
                      (path/dissoc-in db (path/operation connection-negotiation-id))))
             (and (or source-cancel?
                      target-cancel?)
                  source-response
                  target-response)
             (let [[behaviour-type _ params] (action (map-action woco-type)
                                                     (assoc ctx-infos :op woco-type)
                                                     params source-response
                                                     target-response db)
                   result (behaviour behaviour-type params)]
               (assoc result
                      :db (path/dissoc-in db (path/operation connection-negotiation-id))))
             :else
             {:db (assoc-in db (path/operation connection-negotiation-id source-or-target) response)})))))

(re-frame/reg-event-fx
 ::action
 (fn [{db :db} [_ drop-desc woco-type {:keys [source-event target-event connection-negotiation-id source-frame-id] :as ctx-infos} params]]
   (let [db (path/dissoc-in db (path/connection-negotiation connection-negotiation-id))]
     (cond (= :commit-move woco-type)
           (do
             (move-event-call source-frame-id
                              (:commit-move-data drop-desc)
                              #js{"nativeEvent" (:drop-event drop-desc)})
             {:db db})
           (= :reset-move woco-type)
           (do
             (moving-state false true)
             {:db db})
           :else
           {:dispatch-n [(conj source-event woco-type :source params [::process woco-type ctx-infos :source params])
                         (conj target-event woco-type :target params [::process woco-type ctx-infos :target params])]}))))