(ns de.explorama.frontend.woco.api.notifications
  (:require ["react-toastify"]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.shared.woco.config :refer [explorama-enabled-notifications]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :as timbre :refer-macros [debug]]))

(def default-config {:draggable false
                     :container-id "woco-notification"
                     :limit 7
                     :pause-on-focus-loss false
                     :auto-close 15000 ;Delay in ms to close the notification
                     ;:close-on-click false
                     :position "bottom-right"})

(def notification-comp (reagent/adapt-react-class (aget js/ReactToastify "ToastContainer")))
(def notification-func (aget js/ReactToastify "toast"))

(defn- info [message notification-config]
  (if (map? notification-config)
    (.info notification-func message (clj->js notification-config))
    (.info notification-func message)))

(defn- success [message notification-config]
  (if (map? notification-config)
    (.success notification-func message (clj->js notification-config))
    (.success notification-func message)))

(defn- warn [message notification-config]
  (if (map? notification-config)
    (.warn notification-func message (clj->js notification-config))
    (.warn notification-func message)))

(defn- error [message notification-config]
  (if (map? notification-config)
    (.error notification-func message (clj->js notification-config))
    (.error notification-func message)))

(defn notification-container []
  [notification-comp default-config])

(defn- source-frame-icon [source-icon]
  [:div.global-notification-icon
   {:style {:display :inline-block
            :width "20px"
            :margin-right "3px"
            :margin-left "-5px"}}
   [icon {:icon source-icon
          :color :white
          :size 15}]])

(defn- frame-title [db {:keys [source-icon source-frame-id can-focus-source?]
                        :or {can-focus-source? true}}]
  (when-let [frame-title (when source-frame-id (get-in db (path/frame-title source-frame-id)))]
    [:<>
     (when source-icon
       [source-frame-icon source-icon])
     [:div.global-notification-title
      {:style {:display :inline-block
               :width "180px"
               :vertical-align :middle
               :overflow :hidden
               :text-overflow :ellipsis
               :white-space :nowrap}}
      frame-title]
     (when can-focus-source?
       [:div.global-notification-actions
        {:style {:display :inline-block
                 :margin-right "5px"
                 :float :right
                 :top 0}}
        [tooltip {:text (i18n/translate db :navigate-to-frame)}
         [button {:start-icon :focus
                  :variant :secondary
                  :extra-class "explorama__button--small"
                  :on-click (fn [e]
                              (.stopPropagation e)
                              (re-frame/dispatch [:de.explorama.frontend.woco.navigation.control/focus source-frame-id]))}]]])]))

(defn build-message [db {:keys [message source-frame-id] :as desc}]
  (let [message (cond->> message
                  (keyword? message) (i18n/translate db))]

    (if source-frame-id
      (reagent/as-element
       [error-boundary
        [:<>
         [frame-title db desc]
         [:br]
         message]])
      message)))

(defn show-notification?
  ([enabled-notifications {:keys [category]}]
   (boolean
    (and
     (map? category)
     (not-empty category)
     (or (#{true "*"} enabled-notifications)
         (and
          (map? enabled-notifications)
          (or (#{true "*"} (get enabled-notifications (first (keys category))))
              (and
               (coll? (get enabled-notifications (first (keys category))))
               ((set (get enabled-notifications (first (keys category))))
                (first (vals category))))))))))
  ([notification-desc]
   (show-notification? explorama-enabled-notifications notification-desc)))

(re-frame/reg-event-fx
 ::notify
 (fn [{db :db} [_ {:keys [type notification-config] :as desc}]]
   (when (show-notification? desc)
     (debug "Notification: " desc)
     (let [message (build-message db desc)]
       (case type
         (:i :info) (info message notification-config)
         (:s :succ :success) (success message notification-config)
         (:w :warn :warning) (warn message notification-config)
         (:e :err :error) (error message notification-config)
         (notification-func message notification-config))))
   {}))

(re-frame/reg-event-fx
 ::clear-notifications
 (fn [_ _]
   (.clearWaitingQueue notification-func)
   (.dismiss notification-func)))