(ns de.explorama.frontend.knowledge-editor.core
  (:require [de.explorama.frontend.knowledge-editor.plugin-impl :as d42jack-plugin-impl]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.knowledge-editor.canvas :refer [reagent-canvas]]
            [de.explorama.frontend.knowledge-editor.config :as config]
            [de.explorama.frontend.knowledge-editor.main-view :refer [main-view]]
            de.explorama.frontend.knowledge-editor.markdown-helper ;TODO random ref just to make sure it gets loaded remove later
            [de.explorama.frontend.knowledge-editor.path :as path]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]))

(defn- create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   (debug ::create-frame frame-id coords size)
   {:id frame-id
    :coords-in-pixel coords
    :size-in-pixel   size
    :size-min [config/width config/height]
    :event ::view-event
    :module config/tool-name
    :vertical config/default-vertical-str
    :optional-class "optimist__datenatlas"
    :type :frame/management-type
    :legacy? false
    :resizable true}))

(re-frame/reg-event-fx
 ::open
 [fi/ui-interceptor]
 (fn [_ [_ frame-open-event opts]]
   {:dispatch (fi/call-api :frame-create-event-vec (assoc (create-frame [config/width config/height]
                                                                        [config/width config/height])
                                                          :opts opts))}))

(re-frame/reg-event-fx
 ::view-event
 (fn [{db :db} [_ action params]]
   (debug ::view-event action params)
   (let [{:keys [frame-id callback-event]} params
         user-info (fi/call-api :user-info-db-get db)]
     (case action
       :frame/init {:db (assoc-in db (path/frame frame-id) {})}
       :frame/close {:db (path/dissoc-in db (path/frame frame-id))
                     :dispatch callback-event}
       {}))))

(defn view [frame-id _]
  [:div {:style {:display :flex
                 :flex-direction :row
                 :justify-content :space-between
                 :flex-wrap :nowrap}}
   [main-view frame-id]
   [reagent-canvas frame-id]])


(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [user-info (fi/call-api :user-info-db-get db)
         {service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)]
     {:dispatch-n [[:data-atlas.tubes/init-tube user-info]
                   [:dispatch (tools-register {:id config/tool-name
                                               :icon "head-cogs"
                                               :component :d42jack
                                               :action [::open]
                                               :tooltip-text [:de.data42.i18n/translate :menusection-d42jack]
                                               :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                               :vertical config/default-vertical-str
                                               :type :frame/management-type
                                               :tool-group :bar
                                               :bar-group :bottom
                                               :sort-order 3})]
                   (service-register :modules config/tool-name view)
                   (service-register :clean-workspace
                                     ::clean-workspace
                                     [::clean-workspace])
                   (papi-register config/default-vertical-str d42jack-plugin-impl/desc)]})))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(register-init 0)