(ns de.explorama.frontend.ui-base.components.formular.button-group
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [add-class]]))

(def parameter-definition
  {:active-item {:type [:string :number :derefable]
                 :required false
                 :desc "The item-id which button is active."}
   :active-items {:type [:vector :derefable]
                  :required false
                  :desc "The item-ids which buttons are active."}
   :items {:type [:vector :derefable]
           :definition :button
           :required true
           :desc "A vector of items."}
   :disabled? {:type [:boolean :derefable]
               :desc "Disable the button-group."}})
(def button-definition
  {:label {:type [:string :derefable :component]
           :required true
           :desc "The label for <Item>."}
   :title {:type [:string :derefable]
           :desc "A tooltip for the button. A title should be given, if the label is a component."}
   :aria-label {:type [:string :derefable :keyword]
                :desc "The aria label for the button. If nothing is set the label or title will be used, otherwise this key always takes priority."}
   :compact? {:type :boolean
              :desc "Adapt item for optimal display of icon-only labels"}
   :id {:type [:keyword :string]
        :required true
        :desc "The item-id to identify."}
   :disabled? {:type [:boolean :derefable]
               :desc "Disables the item"}
   :on-click {:type :function
              :desc "Function thet should be executed."}
   :extra-props {:type :map
                 :desc "Extra Properties for the html-element."}})
(def sub-definitions {:button button-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {})

(def button-group-class "btn-group")
(def active-item-class "btn-toggled")
(def compact-item-class "btn-icon")

;; The `.btn-group` family styling (container + these raw <button> items +
;; toggled/not-toggled + the sibling-adjacency `:has(+)`/`+` rounding rules)
;; stays as a vendor/caller-DOM remnant in styles/src/tailwind.css rather
;; than becoming markup utilities here, because `.btn-group button` is a
;; contextual/descendant override that also reaches `button` COMPONENTS
;; placed inside a raw `.btn-group` div (woco/tools.cljs) -- which this ns
;; cannot reach -- and is consumed by unmigrated sibling sheets
;; (navbar_domain.css/temp_domain.css/table_domain.css) + base/themes.css
;; forced-colors. The marker classes above stay emitted so those rules keep
;; matching.

(defn- item [group-disabled? active-item active-items
             {:keys [label title id on-click extra-props disabled? compact? aria-label]}]
  (let [label (val-or-deref label)
        title (val-or-deref title)
        disabled? (or group-disabled?
                      (val-or-deref disabled?))
        aria-label (translate-label aria-label)
        active? (or (= id active-item)
                    (some #{id} active-items))]
    (cond->> [:button (cond-> (or extra-props {})
                        active? (update :class add-class active-item-class)
                        compact? (update :class add-class compact-item-class)
                        disabled? (assoc :disabled true)
                        (or (string? label) title)
                        (assoc :aria-label (or aria-label
                                               (cond-> ""
                                                 (string? label) (str label " ")
                                                 title (str title))))
                        (and (not disabled?) (fn? on-click))
                        (assoc :on-click on-click))
              label]
      title
      (conj [tooltip {:text title}]))))

(defn ^:export button-group [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "button-group" specification params)}
     (let [{:keys [active-item active-items items disabled?]}
           params
           active-item (val-or-deref active-item)
           active-items (val-or-deref active-items)
           items (val-or-deref items)
           disabled? (val-or-deref disabled?)]
       (reduce (fn [acc {:keys [id] :as item-desc}]
                 (conj acc
                       (with-meta
                         [item disabled? active-item active-items item-desc]
                         {:key (str "button-group-" id)})))
               [:div {:class [button-group-class]}]
               items))]))