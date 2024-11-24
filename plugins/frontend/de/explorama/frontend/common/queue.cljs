(ns de.explorama.frontend.common.queue
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.common.tracks :as tracks]))

(def ^:private event-blacklist #{:de.explorama.frontend.common.projects.core/next-replay-event :de.explorama.frontend.common.projects.core/project-loaded})

(def ^:private root :queue)

(defn- frame-desc [frame-id]
  [root frame-id])

(defn- last-job [frame-id]
  (conj (frame-desc frame-id)
        :last-job))

(defn- operations-queue [frame-id]
  (conj (frame-desc frame-id)
        :queue))

(defn- operations-queue-status [frame-id]
  (conj (frame-desc frame-id)
        :queue-status))

(defn- job [frame-id]
  (conj (frame-desc frame-id)
        :current-job))

(defn- queue-instance-done? [db frame-id]
  (and (empty? (get-in db (job frame-id)))
       (empty? (get-in db (operations-queue frame-id)))))

(defn- event-metas-root [frame-id]
  (conj (frame-desc frame-id)
        :event-meta))

(def ^:private event-callback-key :callback)

(defn- event-callback [frame-id]
  (conj (event-metas-root frame-id)
        :callback))

(def ^:private event-id-key :event-id)

(defn- event-id [frame-id]
  (conj (event-metas-root frame-id)
        event-id-key))

(defn is-current-job? [db frame-id task-id]
  (= (:task-id (get-in db (job frame-id)))
     task-id))



(re-frame/reg-sub
 ::finished?
 (fn [db [_ frame-id]]
   (queue-instance-done? db frame-id)))

(re-frame/reg-sub
 ::job
 (fn [db [_ frame-id]]
   (get-in db (job frame-id))))

(re-frame/reg-sub
 ::queue
 (fn [db [_ frame-id]]
   (get-in db (operations-queue frame-id))))

#_{:queue {<frame-id> {:init? true
                       :queue []
                       :current-job <job>}}}

(re-frame/reg-event-fx
 ::execute-callback-vec
 (fn [{db :db} [_ frame-id]]
   (let [event-callback-vec (get-in db (event-callback frame-id))]
     (cond event-callback-vec
           (do
             (debug "CALLBACK - " frame-id " - " event-callback-vec)
             {:db (update-in db
                             (event-metas-root frame-id)
                             dissoc
                             event-callback-key
                             event-id-key)
              :dispatch event-callback-vec})
           :else
           (do
             (debug "NO CALLBACK - " frame-id " - " event-callback-vec)
             {})))))

(defn set-event-callback [db frame-id callback-vec]
  (-> db
      (assoc-in (event-id frame-id) (random-uuid))
      (assoc-in (event-callback frame-id) callback-vec)))

(re-frame/reg-event-fx
 ::queue
 (fn [{db :db} [_ frame-id & values]]
   {:db (reduce (fn [db value]
                  (if (and (vector? value)
                           (not (event-blacklist (first value)))) ;FIXME only needed for replay-project
                    (update-in db
                               (operations-queue frame-id)
                               (fnil conj [])
                               {:dispatch value
                                :task-id (random-uuid)})
                    db))
                db
                values)
    :dispatch-n [[::next frame-id]
                 [::check-queue-status frame-id]]}))

(re-frame/reg-event-fx
 ::check-queue-status
 (fn [{db :db} [_ frame-id]]
   (let [queue-status (get-in db (operations-queue-status frame-id))]
     (cond (= :running queue-status) {}
           :else {:dispatch [::register-tracks frame-id]}))))

(re-frame/reg-event-fx
 ::next
 (fn [{db :db} [_ frame-id]]
   (if (empty? (get-in db (job frame-id)))
     (let [queue (get-in db (operations-queue frame-id))
           {:keys [dispatch task-id] :as task} (first queue)
           next-queue (next queue)
           _ (debug "NEXT - " frame-id queue task)
           result (cond
                    (and (empty? next-queue)
                         (not-empty task))
                    {:db (-> (assoc-in db (operations-queue frame-id) [])
                             (assoc-in (job frame-id) task))
                     :dispatch-later {:ms 50
                                      :dispatch (conj dispatch task-id)}}
                    (and (not-empty next-queue)
                         (not-empty task))
                    {:db (-> (assoc-in db (operations-queue frame-id) (vec next-queue))
                             (assoc-in (job frame-id) task))
                     :dispatch-later {:ms 50
                                      :dispatch (conj dispatch task-id)}}
                    (and (empty? next-queue)
                         (empty? task)
                         (get-in db frame-id))
                    {:db (assoc-in db (operations-queue frame-id) [])}
                    :else {})]
       result)
     {})))

(re-frame/reg-event-fx
 ::finish-task
 (fn [{db :db} [_ frame-id finished-task-id source]]
   (let [{:keys [task-id]} (get-in db (job frame-id))]
     (if (and (= finished-task-id task-id)
              task-id)
       (do
         (debug "finished task " frame-id " - " task-id " - " source)
         {:db (-> (assoc-in db (job frame-id) [])
                  (assoc-in (last-job frame-id) task-id))
          :dispatch [::next frame-id]})
       (do
         (debug "finished not current task " frame-id " - " task-id "/" finished-task-id " - " source)
         {})))))

(re-frame/reg-event-fx
 ::register-tracks
 (fn [{db :db} [_ frame-id & track-events]]
   (debug "register-tracks" frame-id)
   (if (not (get-in db (operations-queue-status frame-id)))
     {:db (assoc-in db (operations-queue-status frame-id) :running)
      :fx (into (mapv (fn [ev] [:dispatch ev]) track-events)
                [[:dispatch [::track-register-finished? frame-id]]
                 [:dispatch [::track-register-job frame-id]]
                 [:dispatch [::track-register-queue frame-id]]
                 [:dispatch [::next frame-id]]])}
     {})))

(re-frame/reg-event-fx
 ::dispose-tracks
 (fn [{db :db} [_ frame-id & dispose-events]]
   (debug "dispose-tracks" frame-id)
   (if (= :running (get-in db (operations-queue-status frame-id)))
     {:db (update db root dissoc frame-id)
      :fx (conj  (mapv (fn [ev] [:dispatch ev]) dispose-events)
                 [:dispatch [::track-dispose-finished? frame-id]]
                 [:dispatch [::track-dispose-job frame-id]]
                 [:dispatch [::track-dispose-queue frame-id]])}
     {})))

(re-frame/reg-event-fx
 ::track-register-finished?
 (fn [_ [_ frame-id]]
   {::tracks/register
    {:id [::close frame-id]
     :subscription [::finished? frame-id]
     :event-fn (fn [finished?]
                 (when finished?
                   [::execute-callback-vec frame-id]))}}))

(re-frame/reg-event-fx
 ::track-dispose-finished?
 (fn [_ [_ frame-id]]
   {::tracks/dispose
    {:id [::close frame-id]}}))

(re-frame/reg-event-fx
 ::track-register-job
 (fn [_ [_ frame-id]]
   {::tracks/register
    {:id [::job frame-id]
     :subscription [::job frame-id]
     :event-fn (fn [_]
                 [::next frame-id])}}))

(re-frame/reg-event-fx
 ::track-dispose-job
 (fn [_ [_ frame-id]]
   {::tracks/dispose
    {:id [::job frame-id]}}))

(re-frame/reg-event-fx
 ::track-register-queue
 (fn [_ [_ frame-id]]
   {::tracks/register
    {:id [::queue frame-id]
     :subscription [::queue frame-id]
     :event-fn (fn [_]
                 [::next frame-id])}}))

(re-frame/reg-event-fx
 ::track-dispose-queue
 (fn [_ [_ frame-id]]
   {::tracks/dispose
    {:id [::queue frame-id]}}))