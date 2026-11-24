(ns de.explorama.frontend.ui-base.components.misc.chip
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]))

(def colors {:teal "bg-teal text-white"
             :orange "bg-orange text-white"
             :red "bg-red text-white"
             :yellow "bg-yellow text-black"
             :green "bg-green text-white"
             :blue "bg-blue text-white"})
(def parameter-definition
  {:variant {:type :keyword
             :characteristics [:primary :secondary]
             :desc "The type of chip"}
   :size {:type :keyword
          :characteristics  [:extra-small :small :normal :big]
          :desc "Size of the icon."}
   :full-width? {:type :boolean
                 :desc "Whether or not to use the full available width. If false the chip will scale with its content."}
   :label {:type [:derefable :string :component]
           :required false
           :desc "An label or component which will be displayed as chip content"}
   :start-icon {:type [:string :keyword]
                :required false
                :desc "An icon which will be placed before label. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :start-icon-params {:type :map
                       :desc "Parameters for icon-component"}
   :button-icon {:type [:string :keyword]
                 :desc "A icon will be placed behind the label as a button invoking the on-click function given."}
   :button-icon-params {:type :map
                        :desc "Parameters for icon-component"}
   :button-aria-label {:type [:string :derefable :keyword]
                       :required true
                       :require-cond [:button-icon :*]
                       :desc "Short description of the button. Should be set for each button."}
   :on-click {:type :function
              :required false
              :default-fn-str "(fn [event])"
              :desc "Will be triggered, if user clicks on button"}
   :color {:type :keyword
           :characteristics (vec (sort (keys colors)))
           :desc "Color of the icon. Strings will be changed to keywords automatically"}
   :brightness {:type :keyword
                :characteristics [:light :dark]
                :desc "Brightness of color"}
   :tooltip {:type [:derefable :string]
             :desc "String which will be visibile if you hover over the chip"}
   :tooltip-extra-params {:type :map
                          :desc "Parameters for tooltip-component see tooltip for more information."}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:variant :primary
                         :size :normal
                         :full-width? false})

;; ---------------------------------------------------------------------
;; Structural + colour utility stacks. `gap` uses an arbitrary `em` value
;; (not `gap-2`) because it needs to be an em-relative gap that scales with
;; the chip's own font-size, which the `.extra-small`/`.small`/`.large` size
;; variants change.
(def ^:private chip-base-class
  "inline-flex whitespace-nowrap items-center justify-center gap-[0.5em] rounded-full leading-normal cursor-default")
(def ^:private chip-full-width-class "w-full")

;; Font-size per size variant. `:normal` (the default) sets no font-size in
;; the old sheet either -- it inherits from the surrounding context.
(def ^:private chip-font-size-extra-small-class "text-xxs")
(def ^:private chip-font-size-small-class "text-xs")
(def ^:private chip-font-size-large-class "text-lg")

;; Padding is two independently-overridden axes in the old sheet: vertical
;; padding comes purely from the size variant; horizontal padding comes from
;; the size variant UNLESS the chip contains a start-icon/button-icon, in
;; which case `&:has(button,span[class^="icon-"])` unconditionally forces
;; horizontal padding to size('10') (10px), regardless of size -- reproduced
;; here as an explicit branch rather than relying on cascade/generation order.
(def ^:private chip-py-normal-class "py-1.5")
(def ^:private chip-py-extra-small-class "pt-0 pb-px")
(def ^:private chip-py-small-class "py-1")
(def ^:private chip-py-large-class "py-3")

(def ^:private chip-px-normal-class "px-3")
(def ^:private chip-px-extra-small-class "px-1.5")
(def ^:private chip-px-small-class "px-2")
(def ^:private chip-px-large-class "px-6")
(def ^:private chip-px-has-icon-class "px-2.5")

;; Colour/brightness/secondary-variant class maps. Whenever variant is
;; :secondary, its background/outline-color/text-color must win over
;; brightness's -- brightness only continues to affect the icon colour (a
;; separate, non-competing property/element), which is why `secondary?` is
;; checked before `brightness` below and the icon colour is computed
;; independently.
(def ^:private chip-color-default-class "text-white bg-(--border)")

(def ^:private chip-light-default-class "bg-gray-100 text-gray-900")
(def ^:private chip-light-classes
  {:teal "bg-teal-100 text-black"
   :orange "bg-orange-100 text-black"
   :red "bg-red-100 text-black"
   :yellow "bg-yellow-100 text-black"
   :green "bg-green-100 text-black"
   :blue "bg-blue-100 text-black"})

(def ^:private chip-dark-default-class "bg-gray-800 text-white")
(def ^:private chip-dark-classes
  {:teal "bg-teal-800 text-white"
   :orange "bg-orange-800 text-white"
   :red "bg-red-800 text-white"
   :yellow "bg-yellow-800 text-white"
   :green "bg-green-800 text-white"
   :blue "bg-blue-800 text-white"})

;; The bare `outline` utility (1px solid) is load-bearing: the OLD utility
;; layer had `.outline { outline-width: 1px; outline-style: solid; }` and
;; chip.cljs has emitted that class since pre-phase-1 (77f6900) -- _chips.scss
;; setting only outline-COLOR was deliberate composition with it, so
;; secondary chips are designed to render a 1px solid coloured ring.
;; Tailwind v4's `outline` is computed-identical (outline-style solid via
;; --tw-outline-style default + outline-width 1px).
;; `outline-color` per colour has no colormap/theme token (the old rule used
;; Sass's HSL `darken($color, 15%)`, not the `shade()`-based -600/-700/-800
;; steps) -- precomputed once via dart-sass against the real `darken()`
;; function and inlined as arbitrary hex values.
(def ^:private chip-outline-default-class "bg-white outline outline-(--border) text-gray-500")
(def ^:private chip-outline-classes
  {:teal "bg-white outline outline-teal text-[#142c2f]"
   :orange "bg-white outline outline-orange text-[#bd4e04]"
   :red "bg-white outline outline-red text-[#c91212]"
   :yellow "bg-white outline outline-yellow text-[#e4ad01]"
   :green "bg-white outline outline-green text-[#095d34]"
   :blue "bg-white outline outline-blue text-[#09345d]"})

;; Icon descendant tinting (`@include icon-color(...)`): the icon child is
;; rendered by chip- itself, so it threads via `:extra-class`, same pattern
;; as hint.cljs/collapsible_list.cljs. `!` reproduces the old rule's
;; (0,2,1)/(0,3,1) specificity, which unconditionally beat any bg-* class the
;; icon component itself would add from a caller-supplied :color.
(def ^:private chip-icon-structural-class "w-[1em]! h-[1em]! mix-blend-hard-light")
(def ^:private chip-icon-color-default-class "bg-white!")
(def ^:private chip-icon-color-light-class "bg-gray-900!")
(def ^:private chip-button-icon-scale-class "scale-[0.8]")

;; The remove/close button: `&::before` draws the circular backdrop behind
;; the (blend-moded) icon; `content-['']` is required for Tailwind's
;; `before:` variant to render a pseudo-element at all.
(def ^:private chip-button-class
  (str "relative p-0 border-none bg-transparent mix-blend-multiply cursor-pointer "
       "before:content-[''] before:absolute before:block before:w-[1em] before:h-[1em] "
       "before:scale-[1.2] before:rounded-full before:bg-gray-500 hover:before:bg-gray-700"))

(defn- chip-font-size-class [size]
  (case size
    :extra-small chip-font-size-extra-small-class
    :small chip-font-size-small-class
    :big chip-font-size-large-class
    nil))

(defn- chip-padding-class [size has-icon?]
  (let [py (case size
             :extra-small chip-py-extra-small-class
             :small chip-py-small-class
             :big chip-py-large-class
             chip-py-normal-class)
        px (if has-icon?
             chip-px-has-icon-class
             (case size
               :extra-small chip-px-extra-small-class
               :small chip-px-small-class
               :big chip-px-large-class
               chip-px-normal-class))]
    (str py " " px)))

(defn- chip-color-class [color brightness secondary?]
  (cond
    secondary? (get chip-outline-classes color chip-outline-default-class)
    (= brightness :light) (get chip-light-classes color chip-light-default-class)
    (= brightness :dark) (get chip-dark-classes color chip-dark-default-class)
    :else (get colors color chip-color-default-class)))

(defn- chip-icon-extra-class [icon-params brightness button?]
  (let [color-class (if (= brightness :light)
                       chip-icon-color-light-class
                       chip-icon-color-default-class)
        structural-class (cond-> (str chip-icon-structural-class " " color-class)
                           button? (str " " chip-button-icon-scale-class))
        caller-extra-class (:extra-class icon-params)]
    (cond
      (nil? caller-extra-class) structural-class
      (vector? caller-extra-class) (conj caller-extra-class structural-class)
      :else [caller-extra-class structural-class])))

(defn- chip- [{:keys [variant full-width? size color brightness
                      label start-icon start-icon-params
                      button-icon button-icon-params on-click
                      button-aria-label]
               :as params}]
  (let [secondary? (= variant :secondary)
        has-icon? (boolean (or start-icon button-icon))
        font-size-class (chip-font-size-class size)]
    [:span {:class (cond-> [chip-base-class
                             (chip-color-class color brightness secondary?)
                             (chip-padding-class size has-icon?)]
                     font-size-class (conj font-size-class)
                     full-width? (conj chip-full-width-class))}
     (when start-icon
       [icon (assoc (or start-icon-params {})
                    :icon start-icon
                    :extra-class (chip-icon-extra-class start-icon-params brightness false))])
     label
     (when button-icon
       [:button {:class chip-button-class
                 :on-click on-click
                 :aria-label (translate-label button-aria-label)}
        [icon (assoc (or button-icon-params {})
                     :icon button-icon
                     :extra-class (chip-icon-extra-class button-icon-params brightness true))]])]))

(defn- with-tooltip [tooltip-params params]
  [tooltip tooltip-params
   [chip- params]])

(defn ^:export chip [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "chip" specification params)}
     (let [{:keys [tooltip-extra-params] ico :icon
            tooltip-text :tooltip}
           params
           contains-tooltip? (contains? params :tooltip)
           tooltip-params (merge tooltip-extra-params
                                 {:text tooltip-text})]
       (cond
         contains-tooltip? [with-tooltip tooltip-params params]
         :else [chip- params]))]))
