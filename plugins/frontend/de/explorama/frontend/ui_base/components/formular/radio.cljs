(ns de.explorama.frontend.ui-base.components.formular.radio
  (:require
   [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
   [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
   [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:aria-label {:type [:string :derefable]
                :required :aria-label
                :desc "Aria label for the select component. Should be set if the label is a component. Takes priority over the label and placeholder."}
   :on-change {:type :function
               :required true
               :desc "Will be triggered when an this elements is selected."}
   :label {:type [:string :derefable]
           :required :aria-label
           :desc "Normal label text and fallback for aria-label. Only for (= label-variant :simple)."}
   :strong-label {:type [:string :derefable]
                  :desc "Additional label option. Creates a bold text next to the radio button. Only for (= label-variant :simple)."}
   :extra-class {:type :string
                 :desc "Classname which will be added to radio button."}
   :checked? {:type :boolean
              :required true
              :desc "Marks the radio button as selected."}
   :disabled? {:type :boolean
               :desc "Marks the radio button as disabled."}
   :id {:type :string
        :desc "Html id - will generate a id if not provided."}
   :tabindex {:type :number
              :desc "Defines the tabindex"}
   :name {:type :string
          :desc "Name of the radio button group. There can only be one selected radio button in a group."}
   :label-variant {:type :keyword
                   :desc ":simple (default) and :img. :img uses an images as label."}
   :src {:type :string
         :desc "Image source url for (= label-variant :img)."}
   :width {:type :number
           :desc "Image width for (= label-variant :img)."}
   :height {:type :number
            :desc "Image height url for (= label-variant :img)."}
   :alt {:type :string
         :desc "Image alt for (= label-variant :img)."}
   :tooltip-class {:type :string
                   :desc "Classes for the provided tooltip."}
   :tooltip {:type [:string :derefable]
             :desc "Tooltip label"}})

(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:disabled? false
                         :tabindex 0
                         :name :id
                         :label-variant :simple})

;; ---------------------------------------------------------------------
;; Tailwind migration of styles/src/scss/components/_checkbox.scss's
;; `%control` placeholder + `.radio` rule. `.radio` is kept as a literal
;; DOM class below (base/_themes.scss's forced-colors block selects it
;; directly) but `.radio-right` has zero usage sites anywhere in
;; plugins/bundles -- checked at Step 1 -- so it's dropped (candidate-dead,
;; :box-position isn't even a parameter of this component).
;;
;; `%control`'s shared declarations (`control-*`) are duplicated here --
;; NOT imported from checkbox.cljs -- because radio lives in its own ns
;; while checkbox/switch share checkbox.cljs (plan rule: duplicate per
;; component when the sheet's shared placeholder crosses ns boundaries,
;; don't create a new cross-ns shared-styles namespace for 2 call sites).
(def ^:private control-class "flex flex-row items-baseline gap-1.5")
(def ^:private control-input-class
  "appearance-none flex-none cursor-pointer peer disabled:cursor-default")
(def ^:private control-label-class
  "cursor-pointer w-auto leading-relaxed peer-disabled:cursor-default peer-disabled:text-(--text-disabled)")

;; `.radio input`. Same specificity reasoning as checkbox.cljs's
;; `checkbox-input-class`: `checked:hover:enabled:border-0` reproduces the
;; old sheet's `&:checked { &:hover:enabled {...} }` nesting (real
;; 3-pseudo-class specificity beating the plain 2-pseudo-class
;; `&:hover:enabled` sibling rule), and `disabled:bg-(--bg-hover)` needs no
;; `!` against `checked:bg-(--primary)` -- verified via compile-probe that
;; Tailwind generates `disabled:` utilities after `checked:` ones
;; regardless of class-list order, reproducing the old sheet's
;; later-block-wins tie between the two equal-specificity sibling rules.
(def ^:private radio-input-class
  (str "w-[1.15em] h-[1.15em] grid place-content-center "
       "border-2 border-(--border) rounded-full bg-(--bg) shadow-xs "
       "[transition:background_.12s_ease-in-out,border_.12s_ease-in-out] "
       "before:content-[''] before:w-[0.5em] before:h-[0.5em] before:rounded-full before:scale-0 "
       "before:[transition:transform_.12s_ease-out_.12s] "
       "before:shadow-[inset_1em_1em_var(--bg),var(--shadow-sm)] "
       "checked:bg-(--primary) checked:border-0 checked:before:scale-100 "
       "checked:hover:enabled:border-0 "
       "hover:enabled:border-2 hover:enabled:border-(--border-focus) "
       "focus-visible:outline-[max(2px,0.15em)] focus-visible:outline-(--border-focus) "
       "focus-visible:outline-offset-[max(2px,0.15em)] "
       "disabled:bg-(--bg-hover) disabled:shadow-none"))

(def uuid-prefix "ui-base.formular.radio-")
(defn- make-uuid []
  (str uuid-prefix (random-uuid)))

(defn radio-button [{:keys [checked? disabled? label strong-label extra-class
                            id tabindex on-change aria-label label-variant
                            src width height alt tooltip-class tooltip name]}]
  (let [strong-label (val-or-deref strong-label)
        label (val-or-deref label)
        id (or id (make-uuid))]
    [:div (cond-> {:class (str "radio " control-class)}
            (= :img label-variant)
            (update :class str "items-center")
            extra-class
            (update :class str " " extra-class))
     [:input (cond-> {:type :radio
                      :class (str control-input-class " " radio-input-class)
                      :aria-label (or (val-or-deref aria-label)
                                      (str (or strong-label "")
                                           (or label "")))
                      :name (or name id)
                      :id id
                      :tabIndex (or tabindex 0)
                      :checked (or checked? false)}
               on-change (assoc :on-change
                                #(when (not disabled?)
                                   (on-change (not checked?) %)))
               disabled? (assoc :disabled disabled?))]
     (let [label-variant (or label-variant :simple)]
       (case label-variant
         :simple
         (when (or label
                   strong-label)
           (cond-> [:label {:for id :class control-label-class}]
             strong-label
             (conj [:strong strong-label])
             label
             (conj label)))
         :img
         [:label {:for id :class control-label-class}
          [:img {:src src
                 :width width
                 :height height
                 :alt (or alt tooltip)}]]))
     (when tooltip
       [:span {:class tooltip-class
               :title (val-or-deref tooltip)}])]))

(defn  ^:export radio [props]
  [error-boundary {:validate-fn #(validate "radio" specification props)}
   [radio-button props]])
