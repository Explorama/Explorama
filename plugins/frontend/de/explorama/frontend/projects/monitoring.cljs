(ns de.explorama.frontend.projects.monitoring
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.tubes :as tubes]
            [re-frame.core :as re-frame]))

;; Log-Mode  ================================

(def monitoring-path
  [:projects :monitoring])

(def log-path
  (concat monitoring-path [:event-logs]))

(def log-mode-path
  (concat monitoring-path [:mode]))

(defn log-mode
  ([db]
   (get-in db log-mode-path))
  ([db value]
   (assoc-in db log-mode-path value)))

(defn log-time?
  [context]
  (= :event-time
     (get-in context (concat [:coeffects :db] log-mode-path))))

;; TODO we dont use it + causes an non critical exception
;; (re-frame/dispatch
;;  [:woco.button-42/register :on-click
;;   [::event-time-log-activation-switched]])

(re-frame/reg-event-db
 ::event-time-log-activation-switched
 (fn [db _]
   (let [current-state (log-mode db)
         new-state (if current-state nil :event-time)]
     (print "Changed log state to" new-state)
     (log-mode db new-state))))

(re-frame/reg-event-db
 ::event-time-log-activated
 (fn [db _] (log-mode db :event-time)))

(re-frame/reg-event-db
 ::event-time-log-deactivated
 (fn [db _] (log-mode db nil)))

(re-frame/reg-sub
 ::log-mode
 (fn [db _] (log-mode db)))

(def log-params-path
  (concat monitoring-path [:log-params]))

(defn log-params
  ([db]
   (get-in db log-params-path))
  ([db value]
   (assoc-in db log-params-path value)))

;; interceptor functionalilty ====================

(defn db-from-context
  [context]
  (or (get-in context [:effects :db])
      (get-in context [:coeffects :db])))

(defn update-effect-db
  "Returns context with a by db-update-fn updated db in [:effects :db],
  db-update-fn taking db and returning it updated."
  [context db-update-fn]
  (let [db (db-from-context context)
        db-updated (db-update-fn db)]
    (assoc-in context [:effects :db] db-updated)))

(defn maybe-intercept
  [interceptor-fn condition context]
  (if (condition context)
    (interceptor-fn context)
    context))

;; logging ========================================

(def log-buffer-path
  [:coeffects :log-buffer])

(defn log-fn [message db]
  (update-in db log-path
             conj message))

(defn log-interceptor-fn
  [context message]
  (update-effect-db context
                    (partial log-fn message)))

(defn reset-log-intercept-fn
  [context]
  (update-effect-db context
                    (fn [db] (assoc-in db log-path []))))

;; logging of events and their time of invokation ==

(defn current-time []
  (.getTime (js/Date.)))

(defn event-essentials
  [projects-event]
  (let [e (get projects-event 2)
        [p f] (str/split (str (get e 1)) #"_")]
    {:event (get e 2)
     :service (first e)
     :project p
     :frame f}))

(defn event-time-log
  [event time]
  (merge {:time time}
         (event-essentials event)))

(defn log-time-intercept-fn
  [context]
  (let [event (get-in context [:coeffects :event])
        time (get-in context log-buffer-path)
        message (event-time-log event time)]
    (log-interceptor-fn context message)))

(defn set-time
  [context]
  (assoc-in context log-buffer-path
            (current-time)))

(defn intercept-if-log
  [intercept-fn]
  (partial maybe-intercept
           intercept-fn
           log-time?))

(def log-time
  "Interceptor to log the times of event invokations,
  if event-time logging is enabled, otherwise do nothing."
  (re-frame/->interceptor
   :id ::log-time
   :before
   (intercept-if-log set-time)
   :after
   (intercept-if-log log-time-intercept-fn)))

(def reset-log
  (re-frame/->interceptor
   :id ::reset-log
   :after
   (intercept-if-log reset-log-intercept-fn)))

(defn print-intercept-fn
  [context]
  (print (str
          ::print-time-log "\n"
          (get-in context
                  (concat [:effects :db] log-path))))
  context)

(def print-time-log
  (re-frame/->interceptor
   :id ::print-time-log
   :after
   (intercept-if-log print-intercept-fn)))

;; log-report creation ========================================

(defn properties?
  [properties event]
  (let [relevant-keys (keys properties)
        conditions #(map
                     (fn [k] (= (get % k) (get properties k)))
                     relevant-keys)]
    (every? identity (conditions event))))

(defn filter-by
  [event-properties logs]
  (filter
   (partial properties? event-properties)
   logs))

(defn scan
  [f coll]
  (map f (rest coll)
       (butlast coll)))

(def deltas
  (partial scan -))

(def event-span-descr
  (partial scan #(str "Von " %2 ", bis " %1 ": ")))

(defn time-deltas
  [pillar-event logs]
  (->> logs
       (filter-by pillar-event)
       (map :time)
       deltas
       (map (comp #(/ % 10.) int #(/ % 100.)))))

(defn time-delta-report
  [pillar-event pillar-event-seq logs]
  (let [deltas (time-deltas pillar-event logs)
        deltas-descr (event-span-descr pillar-event-seq)
        rep (str "Time deltas between two subsequent pillar-event in seconds:\n"
                 (clojure.string/join "\n"
                                      (map str deltas-descr deltas)))]
    (if (= (count deltas) (count deltas-descr))
      rep
      (str "Error: Cannot create time-delta report. Number of pillar-events " (count deltas)
           " and descriptions " (count deltas-descr)
           " do not match.\nPillar event occurences: " (filter-by pillar-event logs)
           " ,\npillar-event " pillar-event
           ",\nall events: " logs))))

(defn first-moved-frame [logs]
  (->> logs
       (filter #(= (:event %) "move-frame"))
       first
       :frame))

(defn project-id [logs]
  (:project (first logs)))

(defn project
  [db id]
  (or (get-in db [:projects :projects :created-projects id])
      (get-in db [:projects :projects :allowed-projects id])
      (get-in db [:projects :projects :read-only-projects id])))

(defn save-report-on-server
  [report-name report]
  (tubes/dispatch-to-server
   [:de.explorama.frontend.projects.monitoring/save-as-file report-name report]))

(defn compute-and-persist-time-deltas-intercept-fn
  [context]
  (let [db (db-from-context context)
        logs (get-in db log-path)
        pillar-event {:event "move-frame"
                      :service "woco"
                      :frame (first-moved-frame logs)}
        p (project db (project-id logs))
        pillar-description (clojure.string/split (:description p) #",")
        report-name (:title p)]
    (if-let [report (time-delta-report pillar-event pillar-description logs)]
      (do
        (print "performance-report" report-name "written to server")
        (save-report-on-server report-name report))))
  context)

(def compute-and-persist-time-deltas
  (re-frame/->interceptor
   :id ::compute-and-persist-time-deltas
   :after
   (intercept-if-log
    compute-and-persist-time-deltas-intercept-fn)))
