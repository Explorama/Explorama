(ns de.explorama.frontend.data-atlas.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.data-atlas.config :as config]
            [de.explorama.frontend.data-atlas.path :as db-path]
            [de.explorama.frontend.data-atlas.views.core :as views]
            [de.explorama.frontend.data-atlas.views.plugin-impl :as plugin-impl]
            [de.explorama.shared.data-atlas.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id config/tool-name
                                       :icon "atlas"
                                       :component :data-atlas
                                       :action [::data-atlas-open]
                                       :tooltip-text [::i18n/translate :menusection-data-atlas]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :vertical config/default-vertical-str
                                       :type :frame/management-type
                                       :tool-group :bar
                                       :bar-group :bottom
                                       :sort-order 1})]
           [:dispatch (init-done config/default-vertical-str)]
           [:dispatch (info (str config/default-vertical-str " arriving!"))]]})))

(re-frame/reg-event-fx
 ::init-event
 (fn [_ _]
   (let [{service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)]
     {:dispatch-n [[::arrive]
                   (service-register :modules config/tool-name views/main-panel)
                   (service-register :clean-workspace
                                     ::clean-workspace
                                     [::clean-workspace])
                   (service-register :logout-events :data-atlas-logout [::logout])
                   (papi-register config/default-vertical-str plugin-impl/desc)]})))

(defn clear-path [db path frames]
  (if (seq frames)
    (apply update-in db path dissoc frames)
    db))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (let [frames (fi/call-api :list-frames-vertical-db-get db config/default-vertical-str)
         dispatch-events (conj (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frames)
                               (conj follow-event ::clean-workspace))]
     {:db (-> db
              (clear-path [db-path/root] frames)
              (clear-path db-path/frame-container frames)
              (clear-path db-path/replay frames))
      :dispatch-n dispatch-events})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {}))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   (debug ::create-frame frame-id coords size)
   {:id frame-id
    :coords-in-pixel coords
    :size-in-pixel   size
    :size-min [600 500]
    :event ::view-event
    :module config/tool-name
    :vertical config/default-vertical-str
    :optional-class "explorama__datenatlas"
    :type :frame/management-type
    :legacy? false
    :resizable true}))

(re-frame/reg-sub
 ::project-is-loading
 (fn [db [_ frame-id]]
   (db-path/replay=? db frame-id)))

(re-frame/reg-event-fx
 ::data-atlas-open
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-open-event opts]]
   {:db         (assoc-in db [:de.explorama.frontend.data-atlas :frame-open-event] frame-open-event)
    :dispatch (fi/call-api :frame-create-event-vec (assoc (create-frame [100 200] [600 550])
                                                          :opts opts))}))

(re-frame/reg-event-fx
 ::view-event
 (fn [{db :db} [_ action params no-broadcast?]]
   (debug ::view-event action params)
   (let [{:keys [size coords frame-id frame-target-id callback-event min-h]} params
         user-info (fi/call-api :user-info-db-get db)
         sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (case action
       :frame/init (do
                     (when-not no-broadcast?
                       (sync-event-fn [::view-event action params true]))
                     {:db (assoc-in db (db-path/frame frame-id) {})
                      :dispatch [:de.explorama.frontend.data-atlas.views.core/set-loading frame-id true (.now js/Date)]
                      :backend-tube [ws-api/get-data-elements-route
                                     {:client-callback [ws-api/get-data-elements-result frame-id]}
                                     user-info frame-id nil nil (i18n/current-language db)]})
       :frame/close {:db (update-in db db-path/frame-container dissoc frame-id)
                     :dispatch callback-event}
       {}))))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(defn init []
  (register-init 0))
