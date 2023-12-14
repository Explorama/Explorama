(ns de.explorama.frontend.ui-base.components.frames.loading-screen
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [button loading-message]]
            [de.explorama.frontend.ui-base.components.frames.dialog :refer [dialog]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [progress-bar]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [reagent.core :as reagent]))

(def parameter-definition
  {:show? {:type [:boolean :derefable]
           :required true
           :desc "If true, loading-screen is visible. Can be a boolean or derefable like an atom or re-frame subscription"}
   :progress {:type [:number :derefable]
              :desc "The progress in %, the range is 0-100. If no progress is given, a loading animation is shown instead."}
   :message {:type [:string :derefable]
             :desc "Main message which is visible on loading-screen. Can be a string or derefable like an atom or re-frame subscription"}
   :message-state {:type [:keyword]
                   :characteristics [:load :info :error]
                   :desc "State of the message. :info will hide the loading element and :error will additionally highlight the message in red."}
   :tip-title {:type [:string :derefable]
               :desc "Tip title which is visible on loading-screen. Can be a string or derefable like an atom or re-frame subscription"}
   :tip {:type [:string :derefable]
         :desc "Tip message which is visible on loading-screen. Can be a string or derefable like an atom or re-frame subscription"}
   :buttons {:type [:vector :derefable]
             :desc "A vector of button params, for specification see the button component."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:show? false
                         :message-state :load})

(def tip-class ["text-center" "text-secondary" "m-0"])
(def tip-title-class ["text-bold"])
(def error-class ["text-red" "mb-32"])
(def button-row-class ["flex" "gap-8"])

(defn- loading-screen-buttons [{:keys [buttons]}]
  (reduce
   (fn [res button-params]
     (conj res [button button-params]))
   [:div {:class button-row-class}]
   (val-or-deref buttons)))

(defn- loading-screen-tips [{:keys [tip tip-title]}]
  (let [tip-title (val-or-deref tip-title)
        tip (val-or-deref tip)]
    (when (or tip-title tip)
      (cond-> [:div {:class tip-class}]
        tip-title  (conj [:div {:class tip-title-class} tip-title])
        tip (conj tip)))))

(defn- loading-screen-error [{:keys [message] :as params}]
  [:<>
   [:div {:class error-class} (val-or-deref message)]
   [loading-screen-tips params]
   [loading-screen-buttons params]])

(defn- loading-screen-info [{:keys [message] :as params}]
  (let [message (val-or-deref message)]
    [:<>
     [:h2 message]
     [loading-screen-tips params]
     [loading-screen-buttons params]]))

(defn- loading-screen-load [{:keys [message progress] :as params}]
  (let [message (val-or-deref message)]
    [:<>
     (if progress
       [progress-bar {:progress progress}]
       [loading-message {:show? true
                         :size :large}])
     [:h2 message]
     [loading-screen-tips params]
     [loading-screen-buttons params]]))

(defn ^:export loading-screen [{:keys [show? message-state] :as params}]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "loading-screen" specification params)}
     [dialog {:show? show?
              :type :container
              :hide-fn #(do)
              :message (case message-state
                         :error [loading-screen-error params]
                         :info [loading-screen-info params]
                         [loading-screen-load params])}]]))