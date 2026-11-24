(ns de.explorama.frontend.ui-base.components.formular.textarea
  (:require [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.components.common.core :refer [parent-wrapper error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]))

(def parameter-definition
  {:id {:type :string
        :desc "Adds :id to html-element. Should be unique"}
   :name {:type :string
          :desc "Specifies the name of the textarea"}
   :label {:type [:string :component :derefable]
           :required :aria-label
           :desc "An label for textarea. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :aria-label {:type [:string :derefable :keyword]
                :required :aria-label
                :desc "Set the aria-label directly, will overwrite both label and placeholder. Use this only if the label is not a string."}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :default-value {:type [:string :number :derefable]
                   :desc "Specifies the inital content of textarea. It can be a string or a derefable like an atom or an re-frame event. It's recommanded to use an derefeable"}
   :value {:type [:string :number :derefable]
           :desc "Specifies the content of textarea. It can be a string or a derefable like an atom or an re-frame event. It's recommanded to use an derefeable"}
   :placeholder {:type [:string :derefable]
                 :required :aria-label
                 :desc "Content which is visible outgrayed when no value is set. It can be a string or a derefable like an atom or an re-frame event. It's recommanded to use an derefeable"}
   :borderless? {:type :boolean
                 :desc "If true, the textarea will be borderless."}
   :hint {:type [:derefable :string]
          :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
   :caption {:type [:derefable :string]
             :desc "An optional caption. It will be displayed underneath the element."}
   :prevent-dragging? {:type :boolean
                       :desc "If true, prevents from dragging"}
   :disabled? {:type [:derefable :boolean]
               :desc "If true, the textarea. will be grayed out and the on-blur will not be triggered"}
   :read-only? {:type :boolean
                :desc "If true, text can be selected but not deleted or changed"}
   :autofocus?  {:type :boolean
                 :desc "Flag for autofocusing textarea. Only works, when textarea is not disabled"}
   :required? {:type :boolean
               :desc "If true, it is required to send an formular when the textarea is in form-tag"}
   :extra-class {:type [:string :vector]
                 :desc "You should avoid it, because the most common cases this component handles by itself. But if its necessary to have an custom css class on component, you can add it here as a string."}
   :wrap {:type :keyword
          :characteristics [:hard :soft :off]
          :desc "Specifies how the text in a text area is to be wrapped when submitted in a form"}
   :max-length {:type :number
                :desc "The maximum lenght of content text"}
   :min-length {:type :number
                :desc "The minimum lenght of content text"}
   :on-change {:type :function
               :default-fn-str "(fn [new-value])"
               :desc "Will be triggered, if textarea text is changed"}
   :on-blur {:type :function
             :default-fn-str "(fn [event])"
             :desc "Will be triggered, if user clicks outside of textarea"}
   :on-key-down {:type :function
                 :default-fn-str "(fn [event])"
                 :desc "Will be triggered, if user is pressing a key"}
   :on-key-up {:type :function
               :default-fn-str "(fn [event])"
               :desc "Will be triggered, if user releases a key"}
   :on-key-press {:type :function
                  :default-fn-str "(fn [event])"
                  :desc "Will be triggered, if user presses a key"}
   :on-input {:type :function
              :default-fn-str "(fn [event])"
              :desc "Will be triggered, if textarea is changed. This event is similar to the :on-change. The difference is that the :on-input event occurs immediately after the value of an element has changed, while :on-change occurs when the element loses focus, after the content has been changed "}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:prevent-dragging? false
                         :required? false
                         :read-only? false
                         :autofocus? false
                         :disabled? false :wrap :soft
                         :borderless? false
                         :max-length 220
                         :on-change (fn [new-value])})

;; ---------------------------------------------------------------------
;; Tailwind migration of the `textarea` element rule in
;; styles/src/scss/components/_input.scss. `<textarea>` is rendered ONLY by
;; this component (it is the sole `[:textarea` builder in the frontend; no
;; backend emits raw <textarea> HTML), so the old GLOBAL `textarea {}` element
;; selector is effectively component-private and moves here as utility stacks.
;; Sibling sheets that also style textareas (_notes/_indicator/_importer) do so
;; through higher-specificity contextual selectors (`.class textarea`, (0,1,1))
;; that keep winning over these (0,1,0) utilities -> no cross-sheet flip; and
;; normalize's `textarea {}` sets only font/margin/overflow which these do not
;; touch, so those persist unchanged.
;;
;; `box-shadow: none` from the old `&:disabled` and `&.borderless` blocks is
;; intentionally OMITTED: a resting disabled/borderless textarea already
;; computes `box-shadow: none` (nothing sets one at rest) and a disabled
;; textarea is never focused, so the only shadow those `none`s overrode (the
;; `:focus` shadow) is unreachable -- omitting keeps the shadow-composition
;; gate floor untouched instead of emitting `shadow-none`'s transparent layers.
;; The old `.borderless` marker class is dropped (grepped: no sibling sheet or
;; base/_themes.scss forced-colors block selects it) and replaced by the
;; padding/radius branch below.
(def ^:private textarea-base-class
  ;; `[outline:none] [border:none]` (raw shorthands) rather than
  ;; `outline-none`/`border-none`: the Tailwind utilities only set
  ;; `*-style:none` (leaving the browser-default border-color and flipping the
  ;; registered `--tw-{border,outline}-style` vars solid->none), whereas the old
  ;; `outline:none; border:none` shorthands also reset border-color to
  ;; currentColor -- the arbitrary properties reproduce the shorthands exactly.
  (str "flex flex-row items-center [outline:none] [border:none] "
       "text-(--text) bg-(--bg-section) w-full resize-none cursor-auto "
       "leading-[1.375] [transition:border-color_120ms,box-shadow_120ms] "
       "hover:enabled:border-(--border) "
       "focus:border-(--border-focus) focus:caret-(--border-focus) "
       "focus:shadow-[var(--shadow-xs),inset_0_0_0_1px_var(--border-focus)] "
       "disabled:bg-(--bg-hover) disabled:text-(--text-secondary) disabled:cursor-default "
       "placeholder:text-(--text-secondary)"))
;; One value per property (no competing same-specificity utilities): the box
;; padding+radius is `p-0 rounded-none` when borderless, else the padded pill.
(def ^:private textarea-bordered-class "py-[0.375em] pr-[0.375em] pl-[0.75em] rounded-xl")
(def ^:private textarea-borderless-box-class "p-0 rounded-none")

(def uuid-prefix "ui-base.formular.ta-")
(defn- make-uuid []
  (str uuid-prefix (random-uuid)))

(defn- tarea [{:keys [value default-value id
                      placeholder max-length min-length
                      read-only? required?
                      disabled? autofocus? wrap
                      prevent-dragging? aria-label
                      on-change on-blur on-key-down
                      on-key-up on-key-press on-input
                      name borderless? label]}]

  (let [value (val-or-deref value)
        default-value (val-or-deref default-value)
        label (val-or-deref label)
        placeholder (val-or-deref placeholder)
        aria-label (translate-label aria-label)
        disabled? (val-or-deref disabled?)]
    [:textarea (cond->  {:class [textarea-base-class
                                 (if borderless?
                                   textarea-borderless-box-class
                                   textarea-bordered-class)]}
                 :always
                 (assoc :aria-label
                        (cond
                          aria-label aria-label
                          (string? label) label
                          placeholder placeholder
                          :else ""))
                 prevent-dragging?
                 (assoc :draggable     false
                        :on-drag-start #(.preventDefault %)
                        :on-drag       #(.preventDefault %)
                        :on-drag-end   #(.preventDefault %))
                 name (assoc :name name)
                 id (assoc :id id)
                 max-length (assoc :max-length max-length)
                 min-length (assoc :min-length min-length)
                 required? (assoc :required true)
                 read-only? (assoc :read-only true)
                 disabled? (assoc :disabled true)
                 placeholder (assoc :placeholder placeholder)
                 wrap (assoc :wrap wrap)
                 on-change (assoc :on-change (fn [e]
                                               (on-change (aget e "nativeEvent" "target" "value") e)))
                 on-key-down (assoc :on-key-down on-key-down)
                 on-key-up (assoc :on-key-up on-key-up)
                 on-key-press (assoc :on-key-press on-key-press)
                 on-input (assoc :on-input on-input)
                 on-blur (assoc :on-blur on-blur)
                 autofocus? (assoc :auto-focus true)
                 default-value (assoc :default-value default-value)
                 value (assoc :value value))]))

(defn ^:export textarea [{:keys [id]}]
  (let [id (or id (make-uuid))]
    (reagent/create-class
     {:display-name "textarea"
      :reagent-render
      (fn [params]
        (let [params (merge default-parameters
                            params)]
          [error-boundary {:validate-fn #(validate "textarea" specification params)}
           (let [params (merge params {:id id :element tarea})]
             [parent-wrapper params
              [tarea params]])]))})))