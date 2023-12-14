(ns de.explorama.frontend.ui-base.components.frames.notification
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [add-class]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:message {:type [:derefable :string :component]
             :desc "An message or component which will be displayed as notification content"}
   :actions {:type [:derefable :vector]
             :desc "Actions of notfication as vector of button-props [<first button props> <second button props> ...]."}
   :show? {:type [:boolean :derefable]
           :required true
           :desc "If true, notification is visible. Can be a boolean or derefable like an atom or re-frame subscription"}
   :extra-props {:type :map
                 :desc "Parameters for parent component"}
   :icon {:type [:keyword :string]
          :desc "An icon in notifications container"}
   :icon-params {:type :map
                 :desc "Parameters for icon-component"}
   :on-close {:type :function
              :desc "Will be triggered when user clicks on close icon"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:show? false
                         :icon :warning})

(def notification-parent-class "window__notification")
(def notification-close-class "notification__close")
(def notification-icon-class "notification__icon")
(def notification-message-class "notification__message")
(def notification-actions-class "notification__actions")
(def hidden-class "hidden")

(defn- icon-comp [{icon-key :icon :keys [icon-params]}]
  (when icon-key
    [:div {:class notification-icon-class}
     [icon (cond-> {:icon icon-key}
             (map? icon-params)
             (merge icon-params))]]))

(defn ^:export notification [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "notification" specification params)}
     (let [{:keys [show? message actions on-close extra-props]} params
           show? (val-or-deref show?)
           message (val-or-deref message)
           actions (val-or-deref actions)]
       [:div (cond-> (or extra-props {})
               :always (update :class add-class notification-parent-class)
               (not show?) (update :class add-class hidden-class))
        (when (fn? on-close)
          [:div {:class notification-close-class
                 :on-click #(on-close %)}])
        [icon-comp params]
        [:div {:class notification-message-class}
         message]
        (when (vector? actions)
          (reduce (fn [acc props]
                    (conj acc
                          [button props]))
                  [:div {:class notification-actions-class}]
                  actions))])]))