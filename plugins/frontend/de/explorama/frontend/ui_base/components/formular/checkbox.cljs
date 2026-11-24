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

;; ---------------------------------------------------------------------
;; Tailwind migration of styles/src/scss/components/_checkbox.scss.
;; `.checkbox`/`.switch`/`.checkbox-right`/`.switch-right` are kept as
;; literal DOM classes above (`cb-variant-*`) because base/_themes.scss's
;; forced-colors block and the (not-yet-migrated) _login.scss/_search.scss/
;; _temp.scss sheets still select `.checkbox`/`.radio`/`.switch` directly.
;; `.checkbox-right`/`.switch-right`/`.radio-right` have zero such
;; dependents (grepped every sibling sheet) so their declarations are fully
;; replaced by `control-right-class` below, no literal marker needed.
;;
;; `%control`'s shared declarations (`control-*`) are duplicated here and in
;; radio.cljs's own `^:private` stack per the plan rule: switch lives in
;; THIS ns (the `:as-toggle?` variant of this same `checkbox` component),
;; radio lives in a separate ns, so the shared block is not hoisted into a
;; new cross-ns namespace for 2 call sites.
(def ^:private control-class "flex flex-row items-baseline gap-1.5")
(def ^:private control-right-class "flex-row-reverse text-end justify-end")
;; `peer` (on the input) + `peer-disabled:` (on the label) reproduce
;; `%control`'s `input:disabled, input:disabled + label {cursor:default}`
;; and `.checkbox/.radio/.switch input:disabled + label {color:...}`
;; sibling rules without needing the disabled? value resolved up here.
(def ^:private control-input-class
  "appearance-none flex-none cursor-pointer peer disabled:cursor-default")
(def ^:private control-label-class
  "cursor-pointer w-auto leading-relaxed peer-disabled:cursor-default peer-disabled:text-(--text-disabled)")

;; `.checkbox input`. `checked:hover:enabled:before:[clip-path:...]` and
;; `checked:hover:enabled:border-0` are NOT redundant with the plain
;; `checked:*`/`hover:enabled:*` rules despite same-looking values: the old
;; sheet nests `&:hover:enabled` *inside* `&:checked`, giving that combo
;; real (3-pseudo-class) specificity over the plain (2-pseudo-class)
;; `&:hover:enabled` sibling rule -- reproduced here via the 3-variant
;; chain, which Tailwind compiles to the same genuinely-higher-specificity
;; compound selector, so no `!` is needed anywhere in this stack (verified
;; via compile-probe: `checked:`/`disabled:`/`hover:enabled:` are all real
;; pseudo-classes, and Tailwind happens to generate `disabled:` utilities
;; after `checked:` ones regardless of class-list order, so the old sheet's
;; "later-wins" `:checked`/`:disabled` sibling-block tie -- disabled's
;; `background`/`box-shadow` beating checked's -- resolves the same way).
(def ^:private checkbox-input-class
  (str "w-[1.15em] h-[1.15em] grid place-content-center "
       "border-2 border-(--border) rounded-xxs bg-(--bg) shadow-xs "
       "[transition:background_.12s_ease-in-out,border_.12s_ease-in-out] "
       "before:content-[''] before:w-[0.75em] before:h-[0.75em] before:scale-0 "
       "before:origin-bottom-left "
       "before:[transition:transform_.12s_ease-out_.12s,clip-path_.12s] "
       "before:shadow-[inset_1em_1em_var(--bg),var(--shadow-sm)] "
       "before:[clip-path:polygon(0_50%,20%_30%,40%_50%,80%_10%,100%_30%,40%_90%,40%_90%,40%_90%,40%_90%,40%_90%,40%_90%,40%_90%)] "
       "checked:bg-(--primary) checked:border-0 checked:before:scale-100 "
       "checked:hover:enabled:border-0 "
       "checked:hover:enabled:before:[clip-path:polygon(0_20%,20%_0,50%_30%,80%_0%,100%_20%,70%_50%,100%_80%,80%_100%,50%_70%,20%_100%,0%_80%,30%_50%)] "
       "hover:enabled:border-2 hover:enabled:border-(--border-focus) "
       "focus-visible:outline-[max(2px,0.15em)] focus-visible:outline-(--border-focus) "
       "focus-visible:outline-offset-[max(2px,0.15em)] "
       "disabled:bg-(--bg-hover) disabled:shadow-none"))

;; `.switch input`. Unlike checkbox/radio, the old sheet DOES nest a
;; `&:checked { &:disabled {...} }` block, giving checked+disabled a real
;; (2-pseudo-class) specificity edge over the plain (1-pseudo-class)
;; `&:disabled` -- reproduced via `checked:disabled:bg-(--primary-muted)`.
(def ^:private switch-input-class
  (str "w-[2.15em] h-[1.15em] bg-(--bg-hover) rounded-full grid place-content-center shadow-xs "
       "[transition:background_.12s_ease-in-out] "
       "before:content-[''] before:w-[0.75em] before:h-[0.75em] before:rounded-full "
       "before:[transform:translateX(-67%)] "
       "before:[transition:transform_.12s_ease-in-out] "
       "before:shadow-[inset_1em_1em_var(--color-white),var(--shadow-sm)] "
       "checked:bg-(--primary) checked:before:[transform:translateX(67%)] "
       "checked:hover:enabled:bg-(--primary) checked:disabled:bg-(--primary-muted) "
       "hover:enabled:bg-(--primary-highlight) "
       "focus-visible:outline-[max(2px,0.15em)] focus-visible:outline-(--border-focus) "
       "focus-visible:outline-offset-[max(2px,0.15em)] "
       "disabled:bg-(--bg-section) disabled:shadow-none"))

(def uuid-prefix "ui-base.formular.cb-")
(defn- make-uuid []
  (str uuid-prefix (random-uuid)))

(defn- box [{:keys [id checked? on-change extra-class disabled? value read-only? label aria-label as-toggle?]}]
  (let [checked? (val-or-deref checked?)
        value (val-or-deref value)
        disabled? (val-or-deref disabled?)
        label (val-or-deref label)
        aria-label (translate-label aria-label)]
    [:input
     (cond->  {:class (cond-> [control-input-class
                               (if as-toggle? switch-input-class checkbox-input-class)]
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
                 params]
             [:div {:class (cond-> [(if as-toggle?
                                      cb-variant-toggle-class
                                      cb-variant-checkbox-class)
                                     control-class]
                             (= box-position :right) (conj control-right-class)
                             extra-class (conj extra-class))}
              [box params]
              (when lb
                [label (assoc (or label-params {})
                              :label lb
                              :for-id id
                              :extra-class (str control-label-class
                                                 (when-let [ec (:extra-class label-params)]
                                                   (str " " ec))))])
              (when hint
                [tooltip {:text hint}
                 [:div {:class form-hint-class}
                  [:span]]])])]))})))
