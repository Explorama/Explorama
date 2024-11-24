(ns de.explorama.frontend.ui-base.components.formular.checkbox
  (:require [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.components.common.core :refer [label tooltip error-boundary]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [form-hint-class]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]))

(def parameter-definition
  {:checked? {:type [:boolean :derefable]
              :required true
              :desc "If true, checkbox is checked"}
   :disabled? {:type [:boolean :derefable]
               :desc "If true, the checkbox is disabled"}
   :id {:type :string
        :desc "The id for the input element"}
   :on-change {:type :function
               :required true
               :desc "Triggered when clicking on checkbox"}
   :box-position {:type :keyword
                  :characteristics [:left :right]
                  :desc "If set to :right, the box will placed at right position, otherwise at left"}
   :as-toggle? {:type :boolean
                :desc "If true, the checkbox will look like an toggle"}
   :extra-class {:type :string
                 :label {:type [:string :component :derefable]
                         :required true
                         :desc "An label for checkbox. Uses label from de.explorama.frontend.ui-base.components.common.label"}
                 :hint {:type [:derefable :string]
                        :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
                 :id {:type :string
                      :desc "Unique identifier for checkbox-input and linking for label."}
                 :disabled? {:type [:derefable :boolean]
                             :desc "If true, the checkbox will be grayed out and the on-change will not be triggered"}
                 :desc "You should avoid it, because the most common cases this component handles by itself. But if its necessary to have an custom css class on components parent, you can add it here as a string."}
   :label {:type [:string :derefable :component]
           :required :aria-label
           :desc "A label for the checkbox"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :aria-label {:type [:derefable :string :keyword]
                :required :aria-label
                :desc "When no label is given or the label is a compoennt, an aria-label must be given. If both are given, this attribute takes priority."}
   :read-only? {:type :boolean
                :desc "If true, checkbox can be checked/unchecked and on-change will be triggered"}
   :value {:type [:derefable :string :number]
           :desc "If set, checked? is true and checkbox is inside of a form-tag then will be transmitted as value on submitting"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:disabled? false
                         :read-only? false})

(def cb-variant-checkbox-class "checkbox")
(def cb-variant-toggle-class "switch")

(def cb-variant-checkbox-right-class "checkbox-right")
(def cb-variant-toggle-right-class "switch-right")

(def uuid-prefix "ui-base.formular.cb-")
(defn- make-uuid []
  (str uuid-prefix (random-uuid)))

(defn- box [{:keys [id checked? on-change extra-class disabled? value read-only? label aria-label]}]
  (let [checked? (val-or-deref checked?)
        value (val-or-deref value)
        disabled? (val-or-deref disabled?)
        label (val-or-deref label)
        aria-label (translate-label aria-label)]
    [:input
     (cond->  {:class (cond-> []
                        extra-class (conj extra-class))
               :type :checkbox
               :aria-label (or aria-label label)
               :id id
               :checked (or checked? false)}
       read-only? (assoc :read-only true)
       value (assoc :value value)
       on-change (assoc :on-change
                        #(when (and (not disabled?)
                                    (not read-only?))
                           (on-change (not checked?) %)))
       disabled? (assoc :disabled disabled?))]))

(defn ^:export checkbox [{:keys [id]}]
  (let [id (or id (make-uuid))]
    (reagent/create-class
     {:display-name "check-box"
      :reagent-render
      (fn [params]
        (let [params (merge default-parameters params {:id id})]
          [error-boundary {:validate-fn #(validate "checkbox" specification params)}
           (let [{lb :label :keys [extra-class label-params
                                   box-position as-toggle? hint]}
                 params
                 toggle-class
                 (if as-toggle?
                   cb-variant-toggle-right-class
                   cb-variant-checkbox-right-class)]
             [:div {:class (cond-> [(if as-toggle?
                                      cb-variant-toggle-class
                                      cb-variant-checkbox-class)]
                             (= box-position :right) (conj toggle-class)
                             extra-class (conj extra-class))}
              [box params]
              (when lb
                [label (assoc (or label-params {})
                              :label lb
                              :for-id id)])
              (when hint
                [tooltip {:text hint}
                 [:div {:class form-hint-class}
                  [:span]]])])]))})))
