(ns de.explorama.frontend.woco.notes.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [re-frame.db]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.woco.notes.view]
            [de.explorama.frontend.woco.api.interaction-mode :as interaction-mode]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.notes.states :as states]))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   {:id             frame-id
    :coords-in-pixel coords
    :size-in-pixel   size
    :size-min       [config/note-min config/note-min]
    :event          ::note-view-event
    :module         config/note-id
    :vertical       config/notes-vertical-str
    :type           :frame/custom-type
    :resizable      true
    :stick-to-frames? true
    :optional-class "note-card"}))

(re-frame/reg-event-fx
 ::note-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event query]
          {:keys [source-frame-id]} :opts}
         params]
     (debug "note-view-event" action params)
     (case action
       :frame/init
       (do (if source-frame-id
             (states/copy-note-state source-frame-id frame-id)
             (states/init-note-state frame-id))
           {:dispatch [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id]})
       :frame/close {:fx [[:dispatch callback-event]]}
       {}))))

(re-frame/reg-event-fx
 ::spawn-new-note
 [interaction-mode/ro-interceptor]
 (fn [{db :db} [_]]
   {:dispatch-n [(fi/call-api :frame-create-event-vec
                              (create-frame [100 200]
                                            [config/note-width config/note-height]))]}))

(re-frame/reg-event-fx
 ::duplicate-note-frame
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ source-frame-id]]
   (let [{source-coords :coords source-size :size} (fi/call-api :frame-db-get db source-frame-id)]
     {:dispatch-n [(fi/call-api :frame-create-event-vec
                                (-> (create-frame source-coords source-size)
                                    (assoc :opts {:source-frame-id source-frame-id
                                                  :publishing-frame-id source-frame-id})))]})))


(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (states/clean-states)
   (let [frames (fi/call-api :list-frames-vertical-db-get db config/notes-vertical-str)]
     {:dispatch-n (cond-> (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frames)
                    follow-event (conj (conj follow-event ::clean-workspace)))})))
