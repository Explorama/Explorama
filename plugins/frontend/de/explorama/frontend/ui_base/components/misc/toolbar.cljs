(ns de.explorama.frontend.ui-base.components.misc.toolbar
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:id {:type :string
        :desc "The ID for the toolbar itself (not the wrapper which includes popups)"}
   :orientation {:type :keyword
                 :characteristics [:horizontal :vertical]
                 :desc "Horizontal to render the items from left to right and vertical to render them from top to bottom."}
   :tooltip-direction {:type :keyword
                       :characteristics [:up :down :left :right]
                       :desc "The direction all tooltips will open in."}
   :separator {:type :keyword
               :characteristics [:gap :symbol]
               :desc "Given a nested vector of items, decides how the segments are separated."}
   :extra-class {:type [:string :vector]
                 :desc "Additional classes to e.g., center the toolbar."}
   :extra-props {:type :map
                 :desc "Extra parameters for parent container"}
   :offset {:type :map
            :desc "Used to set the attributes :right, :left, :bottom and :top in css. Should only be used for moving toolbars (e.g. the viewport control, which moves with the sidebar). Otherwise, use classes like 'bottom-8'."}
   :z-index {:type :number
             :desc "Used to set the z-index directly if necessary."}
   :items {:type [:nested-vector :derefable]
           :required true
           :definition :toolbar-item
           :desc "A vector of items, which will be displayed according to the orientation. The vector can be nested once to separate the toolbar."}
   :on-click-toolbar-options {:type :function
                              :default-fn-str "(fn [event])"
                              :desc "Will be triggered when clicking on toolbar-options button"}
   :disabled-toolbar-options? {:type [:boolean :derefable]
                               :desc "Disables the toolbar-options button"}
   :toolbar-options-tooltip {:type [:string :derefable]
                             :desc "Tooltip for toolbar-options button"}
   :popouts {:type [:vector :derefable]
             :definition :popout
             :desc "Popouts, which can show additional content."}
   :popout-position {:type :keyword
                     :characteristics [:start :end]
                     :desc "The direction popouts will be opened, based on the DOM position. For verticals toolbars start translate to left of the toolbar and end translates to right. Horizontal toolbars will show them above for start and below for end."}})
(def toolbar-item-definition
  {:title {:type [:string :derefable]
           :required true
           :desc "The tooltip for <Item>."}
   :id {:type :string
        :required true
        :desc "The item-id to identify."}
   :type {:type :keyword
          :characteristics [:button :text :divider]
          :desc ":text will only show the label and ignore all other settings. :divider will show only a divider"}
   :label {:type [:string :derefable :component]
           :desc "The label for <Item>."}
   :icon {:type [:string :keyword]
          :desc "The icon that will be shown"}
   :icon-props {:type :map
                :desc "Properties for icons which will used as params for icon component"}
   :extra-class {:type [:string :vector]
                 :desc "Additional classes"}
   :disabled? {:type [:boolean :derefable]
               :desc "Disables the item"}
   :active? {:type [:boolean :derefable]
             :desc "Highlights the item. The active state will overwrite the disabled state."}
   :on-click {:type :function
              :desc "Function thet should be executed."}})

(def popout-definition
  {:id {:type :string
        :required true
        :desc "The item-id to identify."}
   :show? {:type [:boolean :derefable]
           :desc "Whether or not the popout is shown."}
   :content {:type :component
             :desc "What is shown in your popout"}})
(def sub-definitions {:toolbar-item toolbar-item-definition
                      :popout popout-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {:orientation :vertical
                         :tooltip-direction :right
                         :popout-position :end
                         :separator :gap})

(def item-default-parameters {:type :button})

;; Structural / forced-colors marker classes kept as literals in the DOM.
;; `.toolbar button.active` is selected directly by base/themes.css's
;; `@media (forced-colors: active)` block, `.toolbar-wrapper` by
;; welcome_page_domain.css (`&~.toolbar-wrapper{display:none}`), and
;; `.toolbar` is the ancestor those forced-colors `.toolbar button.active`
;; rules need. The `.toolbar-*` chrome is expressed as the utility stacks
;; below; the two rules that can't become owner markup (Pixi
;; `.toolbar-popout canvas` and the dead `:has(> .toolbar-options)
;; .toolbar-section:last-child`) stay in components/toolbar_domain.css.
(def ^:private toolbar-class "toolbar")
(def ^:private toolbar-options-class "toolbar-options")
(def ^:private section-class "toolbar-section")
(def ^:private horizontal-class "toolbar-horizontal")
(def ^:private divider-class "toolbar-divider")
(def ^:private active-button-class "active")
(def ^:private wrapper-class "toolbar-wrapper")
(def ^:private popout-class "toolbar-popout")
(def ^:private label-class "label")
(def ^:private text-only-class ["font-bold"])

;; ---- migrated _toolbar.scss chrome as Tailwind utility stacks ----------------
;; Standard classes are fully-spelled static literals; theme colours use the
;; `bg-(--var)` css-var shorthand; descendant `span[class^=icon-]` rules and the
;; button state selectors are arbitrary variants, all verified to compile
;; byte-identically to the old declarations in dist/css/5_utilities.css.

(defn- toolbar-util-class
  "`.toolbar` base (+ .toolbar-horizontal direction). transition + delay merged
   into the transition shorthand (opacity 120ms ease-in 500ms)."
  [horizontal?]
  (str "flex " (if horizontal? "flex-row" "flex-col")
       " gap-1 [transition:opacity_120ms_ease-in_500ms]"))

(defn- wrapper-util-class
  "`.toolbar-wrapper`; :has(.toolbar-horizontal) column-flip resolved from the
   known orientation instead of a :has() variant."
  [horizontal?]
  (str "flex " (if horizontal? "flex-col" "flex-row") " gap-1"))

(defn- section-util-class
  "`.toolbar-section` (+ .toolbar-horizontal row direction)."
  [horizontal?]
  (str "flex " (if horizontal? "flex-row" "flex-col")
       " items-center gap-0.5 p-2 rounded-xl bg-(--bg) shadow-lg"))

(defn- divider-util-class
  "`.toolbar-divider` (vertical default vs .toolbar-horizontal variant)."
  [horizontal?]
  (str (if horizontal? "w-0.5 h-4/5 mx-1" "w-4/5 h-0.5 my-1")
       " rounded-full bg-(--divider)"))

(def ^:private item-button-base-class
  ;; `.toolbar button` chrome independent of active/resting state. No flex-
  ;; direction here: default row; icon+label buttons add flex-col below,
  ;; matching `button:has(> .label)`.
  (str "flex justify-center items-center p-2 [border:none] rounded-lg font-bold "
       "cursor-pointer [transition:color_120ms,background-color_120ms] disabled:cursor-default "
       "active:enabled:[transform:scale(0.95)] "
       "focus-visible:[outline:2px_solid_var(--border-focus)] "
       "[&_span[class^=icon-]]:w-5 [&_span[class^=icon-]]:h-5 "
       "[&_span[class^=icon-]]:[transition:background-color_120ms]"))

(def ^:private item-button-resting-class
  ;; `.toolbar button` resting colours + hover/disabled variants.
  (str "text-(--text) bg-(--bg) hover:text-(--link) disabled:text-(--text-disabled) "
       "[&_span[class^=icon-]]:bg-(--icon) [&:hover_span[class^=icon-]]:bg-(--icon-hover) "
       "[&:disabled_span[class^=icon-]]:bg-(--icon-disabled)"))

(def ^:private item-button-active-class
  ;; `.toolbar button.active` (+ its :hover). Swapped in for the resting colours
  ;; so the old `.active` (0,2,1) specificity win is reproduced without a tie;
  ;; the disabled colour variants are dropped when active, matching the old
  ;; "active state overwrites disabled" cascade.
  (str "text-(--link) bg-(--bg-hover) hover:text-(--link-hover) "
       "[&_span[class^=icon-]]:bg-(--icon-hover) [&:hover_span[class^=icon-]]:bg-(--link-hover)"))

(defn- item-button-util-class [active? has-label?]
  (str item-button-base-class
       (when has-label? " flex-col gap-0.5")
       " " (if active? item-button-active-class item-button-resting-class)))

;; `.label:not(:only-child)` (icon + label buttons). font-size is `text-xs`;
;; in this theme `--text-xs--line-height` is `initial`, so `.text-xs` emits
;; only `font-size: var(--text-xs)` (0.75rem) with no line-height — identical
;; to the old single-property rule.
(def ^:private item-label-extra-class "w-max font-normal text-xs")

(def ^:private options-button-util-class
  ;; `.toolbar-options` union: the `.toolbar button` chrome it inherited as a
  ;; descendant PLUS its own absolute-position/padding/radius/transparent-bg
  ;; overrides. Icon is smaller (w-3), tinted --icon-secondary, rotated -45deg.
  (str "absolute bottom-0 right-0 flex justify-center items-center [border:none] font-bold "
       "cursor-pointer p-0.5 rounded-md bg-transparent text-(--text) "
       "[transition:color_120ms,background-color_120ms] hover:text-(--link) "
       "disabled:text-(--text-disabled) disabled:cursor-default "
       "active:enabled:[transform:scale(0.95)] "
       "focus-visible:[outline:2px_solid_var(--border-focus)] "
       "[&_span[class^=icon-]]:w-3 [&_span[class^=icon-]]:h-3 "
       "[&_span[class^=icon-]]:bg-(--icon-secondary) "
       "[&_span[class^=icon-]]:[transform:rotate(-45deg)] "
       "[&_span[class^=icon-]]:[transition:background-color_120ms] "
       "[&:hover_span[class^=icon-]]:bg-(--icon-hover) "
       "[&:disabled_span[class^=icon-]]:bg-(--icon-disabled)"))

(def ^:private popout-util-class
  ;; `.toolbar-popout` (border:1px = `border`; border-style solid is global).
  "flex p-2 border border-(--border) rounded-md bg-(--bg) shadow-md")

(defn- popout-span-util-class
  "`.toolbar-popout > span`: vertical keeps flex:1/height:0; horizontal (the old
   `:has(.toolbar-horizontal)` case) sets width:0/height:auto, flex:1 retained."
  [horizontal?]
  (str "flex-1 " (if horizontal? "w-0 h-auto" "h-0")))

(defn- toolbar-segment [items tooltip-direction horizontal?]
  (reduce
   (fn [res item]
     (let [{:keys [id active? disabled? title label on-click extra-class type icon-props] ic :icon
            :or {icon-props {}}}
           (merge item-default-parameters item)
           label (val-or-deref label)
           title (val-or-deref title)
           active? (val-or-deref active?)
           disabled? (val-or-deref disabled?)]
       (conj res
             (cond
               (= type :divider)
               [:span {:class [divider-class (divider-util-class horizontal?)]}]
               (= type :text)
               [:span {:class text-only-class} label]
               :else
               [tooltip {:text title
                         :direction tooltip-direction}
                [:button
                 (cond-> {:aria-label title
                          :id id
                          :class (cond-> [(item-button-util-class active? (some? label))]
                                   active? (conj active-button-class))}
                   disabled? (assoc :disabled true)
                   (and (fn? on-click) (not disabled?)) (assoc :on-click on-click)
                   (and extra-class (vector? extra-class)) (update :class concat extra-class)
                   (and extra-class (string? extra-class)) (update :class conj extra-class))
                 (when ic
                   [icon (assoc icon-props :icon ic)])
                 (when label
                   [:span {:class (cond-> [label-class]
                                    (and ic label) (conj item-label-extra-class))}
                    label])]]))))
   [:<>]
   (val-or-deref items)))

(defn- toolbar-segmentation [{:keys [items tooltip-direction separator orientation]}]
  (let [horizontal? (= orientation :horizontal)
        segments (if (every? vector? items)
                   (mapv #(vector toolbar-segment % tooltip-direction horizontal?) items)
                   [[toolbar-segment items tooltip-direction horizontal?]])]
    (if (= separator :symbol)
      (into [:div {:class [section-class (section-util-class horizontal?)]}]
            (interpose [:span {:class [divider-class (divider-util-class horizontal?)]}] segments))
      (into [:<>] (mapv #(vector :div {:class [section-class (section-util-class horizontal?)]} %) segments)))))

(defn- toolbar-popout [{:keys [show? content id]} horizontal?]
  (when (val-or-deref show?)
    [:div {:class [popout-class popout-util-class]
           :id id}
     [:span {:class (popout-span-util-class horizontal?)} content]]))

(defn- toolbar-options [{:keys [on-click-toolbar-options disabled-toolbar-options? toolbar-options-tooltip]}]
  (let [disabled? (val-or-deref disabled-toolbar-options?)
        toolbar-options-tooltip (val-or-deref toolbar-options-tooltip)]
    (when (fn? on-click-toolbar-options)
      [:button (cond-> {:class [toolbar-options-class options-button-util-class]}
                 disabled? (assoc disabled? true)
                 (not disabled?) (assoc :on-click on-click-toolbar-options))
       [icon (cond-> {:icon :collapse}
               (and toolbar-options-tooltip (not disabled?))
               (assoc :tooltip toolbar-options-tooltip))]])))

(defn- toolbar-wrapper [{:keys [orientation extra-class popouts popout-position offset z-index
                                extra-props id]
                         :or {extra-props {}}
                         :as params}]
  (let [horizontal? (= orientation :horizontal)
        style (cond-> (select-keys offset [:right :left :top :bottom])
                z-index (assoc :z-index z-index))]
    (if (empty? popouts)
      [:div
       (-> extra-props
           (assoc :class (cond-> [toolbar-class (toolbar-util-class horizontal?)]
                           horizontal?
                           (conj horizontal-class)
                           (vector? extra-class)
                           (concat extra-class)
                           (string? extra-class)
                           (conj extra-class)))
           (update :style (fn [o]
                            (merge (or o {})
                                   style))))
       [toolbar-segmentation params]
       [toolbar-options params]]
      [:div
       {:class (cond-> [wrapper-class (wrapper-util-class horizontal?)]
                 (vector? extra-class)
                 (concat extra-class)
                 (string? extra-class)
                 (conj extra-class))
        :style style}
       (let [wrapped-popouts (into [:<>] (mapv #(toolbar-popout % horizontal?) popouts))
             wrapped-toolbar [:div
                              {:id id
                               :class (cond-> [toolbar-class (toolbar-util-class horizontal?)]
                                        horizontal?
                                        (conj horizontal-class))}
                              [toolbar-segmentation params]
                              [toolbar-options params]]]
         (if (= popout-position :start)
           [:<> wrapped-popouts wrapped-toolbar]
           [:<> wrapped-toolbar wrapped-popouts]))])))

(defn ^:export toolbar [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "toolbar" specification params)}
     [toolbar-wrapper params]]))

(def ^:export toolbar-divider {:type :divider
                               :id ""
                               :title ""})