(ns de.explorama.frontend.ui-base.components.formular.input-group
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [parent-wrapper error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.input-field :refer [input-parentless]]
            [de.explorama.frontend.ui-base.components.formular.select :refer [select-parentless]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.formular.icon-select :refer [icon-select]]))

(def parameter-definition
  {:items {:type [:vector :derefable]
           :required true
           :definition :input
           :desc "A vector of items."}
   :label {:type [:string :component :derefable]
           :desc "An label for input field. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :caption {:type [:string :component :derefable]
             :desc "Will desiplay a caption beneth the input element. Captions will be ignored for number inputs."}
   :invalid? {:type :boolean
              :desc "If true, both the input element and the caption will turn red. Will be ignored for number inputs."}
   #_#_:disabled? {:type [:boolean :derefable]
                   :desc "Disable the button-group."}})
(def input-definition
  {:type {:type :keyword
          :characteristics [:button :input :select :icon-select]
          :desc "The type of component used. Input will alway be a text input - num. inputs are not supported."}
   :id {:type :string
        :required true
        :desc "The item-id to identify."}
   :disabled? {:type [:boolean :derefable]
               :desc "Disables the item"}
   :component-props {:type :map
                     :desc "Properties for the component. See their individuel pages for more information."}})
(def sub-definitions {:input input-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {})

(def input-group-class "inputs-grouped")

(defn- item [group-disabled? {:keys [type component-props disabled?] :as params}]
  [(case type
     :button button
     :input input-parentless
     :select select-parentless
     :icon-select icon-select
     :div)
   component-props])

(defn ^:export input-group [{:keys [items disabled?] :as params}]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "input-group" specification params)}
     [parent-wrapper params
      (reduce
       (fn [acc {:keys [id] :as item-desc}]
         (conj acc
               (with-meta
                 [item disabled? item-desc]
                 {:key (str "input-group-" id)})))
       [:div {:class input-group-class}]
       items)]]))