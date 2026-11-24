(ns de.explorama.frontend.ui-base.components.formular.button
  (:require [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.formular.loading-message :refer [loading-message]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [taoensso.timbre :refer [info]]))

(def parameter-definition
  {:variant {:type :keyword
             :characteristics [:primary :secondary :tertiary :back]
             :required false
             :desc "Defines the styling variant of button."}
   :type {:type :keyword
          :characteristics [:normal :warning]
          :required false
          :desc "Add special sub-types to your button."}
   :disabled? {:type [:derefable :boolean]
               :required false
               :desc "If true, the button will be grayed out and the on-click will not be triggered "}
   :size {:type :keyword
          :characteristics [:small :normal :big]
          :required false
          :desc "Defines the size type of a button."}
   :id {:type :string
        :desc "Adds :id to html-element. Should be unique"}
   :disabled-event-bubble? {:type [:derefable :boolean]
                            :desc "add prevent-default and stop-propagation to on-mouse-down, on-mouse-up and on-click"}
   :loading? {:type :boolean
              :required false
              :desc "If true, the label will be hidden, an loading indicator will be shown and buttons behavior is like an disabled button"}
   :as-link {:type :string
             :required false
             :desc "If set, the parent element will switch from button to link and set the given string as href. The link will be open as an extra browser tab"}
   :link-target {:type :string
                 :required false
                 :desc "Sets the target of link (e.g. current tab, extra tab, iframe,..). Only has an effect when :as-link is set"}
   :extra-class {:type :string
                 :desc "You should avoid it, because the most common cases this component handles by itself (use :variant, :diabled, :loading, :start-icon). But if its necessary to have an custom css class on component, you can add it here as a string."}
   :extra-style {:type [:map :string]
                 :desc "Style properties of element, which will be set. Normally you should use css classes instead of inline-css-code"}
   :start-icon {:type [:string :keyword]
                :required false
                :desc "An icon which will be placed before label. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :icon-params {:type :map
                 :desc "Parameters for icon-component"}
   :label {:type [:derefable :string :component]
           :required :aria-label
           :desc "An label or component which will be displayed as button content"}
   :aria-label {:type [:derefable :string :keyword]
                :required :aria-label
                :desc "When neither a label nor a tooltip can be used, an aria label can be set directly with this parameter. If multiple this are set, only this parameter will be used."}
   :on-click {:type :function
              :required false
              :default-fn-str "(fn [event])"
              :desc "Will be triggered, if user clicks on button"}
   :title {:type [:derefable :string]
           :required :aria-label
           :desc "Shows a build-in browser-tooltip on mouse-hover."}
   :tooltip-extra-params {:type :map
                          :required false
                          :desc "Parameters for tooltip-component see tooltip for more information."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:variant :primary
                         :disabled? false
                         :disabled-event-bubble? false
                         :loading? false
                         :link-target "_blank"})

(def primary-class "btn-primary")
(def secondary-class "btn-secondary")
(def tertiary-class "btn-tertiary")
(def btn-big-class "btn-large")
(def btn-small-class "btn-small")
(def btn-warning-class "btn-warning")
(def btn-back-class "btn-back")
(def btn-loading-class "btn-loading")
(def btn-icon-class "btn-icon")

(def btn-loader-class "loader")

;; ---------------------------------------------------------------------
;; Only the BOX-level styling (%btn + .btn-primary/.btn-secondary/.btn-tertiary
;; /.btn-back variants, warning bg/text, sizes) is expressed as utility stacks
;; here. The literal `.btn-*` marker classes above stay emitted -- sibling
;; sheets and remnant blocks (indicator_domain.css, the input/select remnants
;; in tailwind.css, navbar_domain.css, temp_domain.css), base/themes.css
;; forced-colors and direct-builders (woco/tools.cljs, server rights_roles
;; pages) key off them. The icon-tinting `span[class^="icon-"]` descendant
;; rules, the whole `.btn-group` family, `.btn-loading` and `.btn-link` stay
;; as vendor/caller-DOM remnants in styles/src/tailwind.css (see that file's
;; comment): they are descendant/contextual/direct-builder-consumed and
;; cannot become markup utilities without breaking specificity.
;;
;; `gap`/`padding` use arbitrary `em` values so they scale with the button's
;; font-size.
(def ^:private btn-base-util-class
  ;; %btn box shared by primary/secondary/tertiary (NOT back, which diverges).
  ;; Border-COLOR is per-variant so no two color utilities compete.
  (str "inline-flex justify-center items-center gap-[0.375em] border "
       "font-bold leading-none no-underline "
       "hover:enabled:cursor-pointer "
       "active:enabled:[transform:scale(0.97)] active:enabled:shadow-none "
       "focus-visible:outline-2 focus-visible:outline-purple-700 "
       "focus-visible:outline-offset-2 focus-visible:isolate "
       "disabled:opacity-50 disabled:shadow-none disabled:cursor-default"))

(def ^:private btn-back-util-class
  ;; .btn-back overrides %btn: justify-start, rounded-none, font-normal,
  ;; border-bottom only, active transform:none. Standalone stack (no base).
  ;; `[justify-content:start]` (not `justify-start`) so getComputedStyle reads
  ;; `start`, matching the old `justify-content: start` exactly (`justify-start`
  ;; emits `flex-start`). btn-back carries no box-shadow (none in every state),
  ;; so no `shadow-none` is emitted -- avoids the shadow-composition transparent
  ;; layers and keeps computed `box-shadow: none` like the old sheet.
  (str "inline-flex [justify-content:start] items-center gap-[0.375em] "
       "border border-transparent border-b-(--border) rounded-none "
       "font-normal leading-none no-underline bg-(--bg) text-(--text) "
       "[transition:background-color_120ms] "
       "hover:enabled:cursor-pointer hover:enabled:bg-(--bg-hover) "
       "active:enabled:[transform:none] "
       "focus-visible:outline-2 focus-visible:outline-purple-700 "
       "focus-visible:outline-offset-2 focus-visible:isolate "
       "disabled:opacity-50 disabled:cursor-default"))

(def ^:private btn-primary-util-class
  (str "bg-(--primary) text-gray-50 border-transparent shadow-sm "
       "[transition:background-color_240ms,transform_120ms] "
       "hover:enabled:bg-(--primary-highlight)"))
(def ^:private btn-primary-warning-util-class
  ;; .btn-warning.btn-primary: red-700 bg, hover red-800.
  (str "bg-red-700 text-gray-50 border-transparent shadow-sm "
       "[transition:background-color_240ms,transform_120ms] "
       "hover:enabled:bg-red-800"))

(def ^:private btn-secondary-util-class
  (str "bg-(--bg) text-(--text) border-(--border) shadow-sm "
       "[transition:border-color_120ms,transform_120ms,box-shadow_120ms,background-color_120ms,color_120ms] "
       "hover:enabled:bg-(--bg-hover) "
       "disabled:bg-gray-100 disabled:text-gray-600 disabled:border-gray-300"))
(def ^:private btn-secondary-warning-util-class
  ;; .btn-warning.btn-secondary: text red-700 (hover red-800); the disabled
  ;; text stays gray-600 because `disabled:` out-specifies the resting
  ;; `text-red-700` (matches the old (0,2,0)-tie resolving to :disabled).
  (str "bg-(--bg) text-red-700 border-(--border) shadow-sm "
       "[transition:border-color_120ms,transform_120ms,box-shadow_120ms,background-color_120ms,color_120ms] "
       "hover:enabled:bg-(--bg-hover) hover:enabled:text-red-800 "
       "disabled:bg-gray-100 disabled:text-gray-600 disabled:border-gray-300"))

(def ^:private btn-tertiary-util-class
  (str "bg-transparent text-(--primary) border-transparent "
       "[transition:background-color_120ms,color_120ms,transform_120ms] "
       "hover:enabled:bg-(--bg-hover) hover:enabled:text-(--text) "
       "disabled:bg-[rgba(242.6,244.1,244.8,0.5)] disabled:text-purple-700"))
(def ^:private btn-tertiary-warning-util-class
  ;; text red-700 (hover red-800); disabled text purple-700 wins by `disabled:`
  ;; specificity over the resting red-700 (old (0,2,0) tie -> :disabled later).
  (str "bg-transparent text-red-700 border-transparent "
       "[transition:background-color_120ms,color_120ms,transform_120ms] "
       "hover:enabled:bg-(--bg-hover) hover:enabled:text-red-800 "
       "disabled:bg-[rgba(242.6,244.1,244.8,0.5)] disabled:text-purple-700"))

(defn- btn-variant-util-class [variant warning?]
  (case variant
    :secondary (if warning? btn-secondary-warning-util-class btn-secondary-util-class)
    :tertiary  (if warning? btn-tertiary-warning-util-class btn-tertiary-util-class)
    :back      btn-back-util-class
    (if warning? btn-primary-warning-util-class btn-primary-util-class)))

(defn- btn-radius-util-class [variant icon-only?]
  ;; .btn-tertiary.btn-icon -> rounded-md; everything else -> rounded-full.
  ;; (:back carries its own rounded-none in btn-back-util-class.)
  (if (and (= variant :tertiary) icon-only?)
    "rounded-md"
    "rounded-full"))

(defn- btn-padding-util-class [size icon-only?]
  ;; padding axes: .btn-large (size 12 all + w-full) > .btn-icon (size 8, or
  ;; size 4 when also .btn-small) > .btn-small (4/8) > %btn default (8/12).
  (cond
    (= size :big)      "w-full p-[0.75em]"
    icon-only?         (if (= size :small) "p-[0.25em]" "p-[0.5em]")
    (= size :small)    "py-[0.25em] px-[0.5em]"
    :else              "py-[0.5em] px-[0.75em]"))

(defn- button-util-class [variant type size icon-only?]
  (let [warning? (= type :warning)
        back? (= variant :back)]
    (str (when-not back? (str btn-base-util-class " "))
         (btn-variant-util-class variant warning?)
         (when-not back? (str " " (btn-radius-util-class variant icon-only?)))
         " " (btn-padding-util-class size icon-only?))))

(defn- parent [as-link btn-params & childs]
  (apply conj
         (if as-link
           [:a btn-params]
           [:button btn-params])
         childs))

(defn- button- [params]
  (let [{:keys [variant disabled? loading?
                as-link link-target id
                start-icon icon-params
                extra-class extra-style
                size label on-click type
                disabled-event-bubble?
                title aria-label]
         :as input-p}
        (merge default-parameters params)
        disabled? (val-or-deref disabled?)
        disabled-event-bubble? (val-or-deref disabled-event-bubble?)
        title (val-or-deref title)
        label (val-or-deref label)
        aria-label (translate-label aria-label)
        disabled? (or disabled? loading?)]
    [parent
     as-link
     (cond-> {:href as-link
              :id id
              :target (when as-link link-target)
              :class (cond-> []
                       (not (some #{variant} [:secondary :tertiary :back])) (conj primary-class)
                       (= variant :secondary) (conj secondary-class)
                       (= variant :tertiary) (conj tertiary-class)
                       (= variant :back) (conj btn-back-class)
                       type (conj (case type :warning btn-warning-class ""))
                       loading? (conj btn-loading-class)
                       (and start-icon (not label)) (conj btn-icon-class)
                       size (conj (case size :big btn-big-class :small btn-small-class ""))
                       extra-class (conj extra-class)
                       ;; box-level utility stack (see defs above); the
                       ;; .btn-* markers above stay for sibling sheets/forced-
                       ;; colors/direct-builders + the tailwind.css remnants.
                       true (conj (button-util-class variant type size
                                                      (boolean (and start-icon (not label))))))
              :aria-label (or aria-label
                              (str label (when (and label title) " ") title))
              :style extra-style
              :disabled disabled?
              :on-click (fn [e]
                          (when-not disabled?
                            (when on-click
                              (on-click e)))
                          (when disabled-event-bubble?
                            (.stopPropagation e)
                            (.preventDefault e)))}
       disabled-event-bubble?
       (assoc :on-mouse-up (fn [e]
                             (.stopPropagation e)
                             (.preventDefault e))
              :on-mouse-down (fn [e]
                               (.stopPropagation e)
                               (.preventDefault e))))
     [:<>
      (when start-icon
        [icon (assoc (or icon-params {})
                     :icon start-icon)])
      (when loading?
        [:div {:class btn-loader-class} [:span] [:span] [:span]])
      label]]))

(defn- with-tooltip [tooltip-params params]
  [tooltip tooltip-params
   [button- params]])

(defn ^:export button [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "button" specification params)}
     (let [{:keys [title tooltip-extra-params]}
           params
           contains-title? (contains? params :title)
           title (val-or-deref title)
           tooltip-params (merge {:text title}
                                 tooltip-extra-params)]
       (if contains-title?
         [with-tooltip tooltip-params params]
         [button- params]))]))
