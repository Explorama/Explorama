(ns de.explorama.frontend.ui-base.components.common.error-boundary
  (:require [reagent.core :as r]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [taoensso.timbre :refer-macros [error]]))

(def parameter-definition
  {:show-error? {:type :boolean
                 :desc "If true, error message will be shown instead of nothing"}
   :error-message {:type [:string :component]
                   :desc "Overwrites the default error message"}
   :validate-fn {:type :function
                 :desc "Called once on mount to validate the input params of the comp."}
   :on-error {:type :function
              :default-fn-str "(fn [exception params])"
              :desc "Triggered, when component crashs"}
   :error-comp {:type :component
                :desc "Component which will be rendered when an error occurs"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:show-error? true
                         :error-message "Component crashed"})

(defn- error-component [{:keys [show-error? error-message]}]
  (when show-error?
    [:<>
     error-message]))

(defn ^:export error-boundary
  ([{:keys [on-error validate-fn] :as params} _]
   (let [error-state? (r/atom nil)]
     (r/create-class
      {:component-did-catch (fn [this e info])
       :component-did-mount #(do (validate "error-boundary" specification (merge default-parameters params))
                                 (when (fn? validate-fn) (validate-fn)))
       :component-did-update (fn [this argv]
                               (when (not= argv (r/argv this))
                                 (when (fn? validate-fn) (validate-fn))))
       :get-derived-state-from-error (fn [e]
                                       (error "Component crashed: " e)
                                       (when (fn? on-error)
                                         (on-error e params))
                                       (reset! error-state? true)
                                       #js {})
       :reagent-render (fn [params comp]
                         (let [{:keys [error-comp] :as params} (merge default-parameters params)]
                           (if @error-state?
                             (if error-comp
                               error-comp
                               [error-component params])
                             comp)))})))
  ([comp]
   [error-boundary default-parameters comp]))