(ns de.explorama.frontend.woco.notes.api
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.notes.states :refer [get-bg-color get-instance
                                                             get-note-content get-text set-bg-color set-edit-mode
                                                             set-formatting set-instance-content set-text]]))

(re-frame/reg-event-fx
 ::save-annotation
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id new-color callback force?]]
   (let [old-color @(get-bg-color frame-id)
         new-color (or new-color old-color)
         content (get-note-content frame-id)
         text @(get-text frame-id)
         new-text (or content text)
         sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     {:fx [(when (vector? callback)
             [:dispatch callback])
           (when (or (not= new-color old-color)
                     (not= content text)
                     force?)
             (set-text frame-id new-text)
             (set-bg-color frame-id new-color)
             (sync-event-fn [::sync-notes frame-id new-text new-color])
             [:dispatch [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id]])]})))

(re-frame/reg-event-fx
 ::sync-notes
 (fn [_ [_ frame-id content new-color]]
   (set-instance-content frame-id content)
   (set-bg-color frame-id new-color)
   {}))

(defn- focus
  ([frame-id]
   (when-let [instance @(get-instance frame-id)]
     (focus instance frame-id)))
  ([instance frame-id]
   (.focus instance)
   (set-edit-mode frame-id)))

(defn update-formatting [^js instance frame-id]
  (when instance
    (try
      (set-formatting frame-id
                      (js->clj (.getFormat instance)))
      (catch :default _))))

(defn format-text
  ([frame-id op]
   (when-let [^js instance @(get-instance frame-id)]
     (.format instance
              op
              (not (boolean (aget (.getFormat instance)
                                  op))))
     (update-formatting instance frame-id)
     (focus instance frame-id)
     nil))
  ([frame-id op new-val]
   (when-let [^js instance @(get-instance frame-id)]
     (if-let [_already-applied? (boolean
                                 (= new-val
                                    (aget (.getFormat instance)
                                          op)))]
       (.format instance op false)
       (.format instance op new-val))
     (update-formatting instance frame-id)
     (focus instance frame-id)
     nil)))

(defn clean-format [frame-id _op]
  (when-let [^js instance @(get-instance frame-id)]
    (when-let [sel (.getSelection instance)]
      (.removeFormat instance sel))
    (update-formatting instance frame-id)
    (focus instance frame-id)))

(defn note-background [frame-id _ color]
  (focus frame-id)
  (re-frame/dispatch [::save-annotation frame-id color]))
