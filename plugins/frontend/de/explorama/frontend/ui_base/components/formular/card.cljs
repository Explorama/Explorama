(ns de.explorama.frontend.ui-base.components.formular.card
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
            [taoensso.timbre :refer-macros [error]]
            [reagent.core :as r]))

(def parameter-definition
  {:type {:type :keyword
          :required true
          :characteristics [:text :button :carousel :childs]
          :desc "Type of the card. Due to the type different params are supported. For type :childs you can use the card component as parent component"}
   :items {:type [:derefable :vector]
           :desc "[:carousel] - Items for carousel. Datastructure must be a vector. A single item can be a component, string or map of :title and :content"}
   :auto-slide? {:type :boolean
                 :desc "[:carousel] - If true the items will be switch automatically related to defined :slide-direction and :slide-timeout-ms"}
   :slide-direction {:type :keyword
                     :characteristics [:left :right]
                     :desc "[:carousel] - Direction for auto-slide"}
   :aria-label-next {:type [:keyword :string :derefable]
                     :desc "[:carousel] - Label for the next button"}
   :aria-label-previous {:type [:keyword :string :derefable]
                         :desc "[:carousel] - Label for the previous butto"}
   :slide-timeout-ms {:type :number
                      :desc "[:carousel] - Timeout in ms for auto-slide"}
   :extra-class {:type [:vector :string]
                 :desc "[:childs] - Add some extra classes to the parent"}
   :extra-style {:type :map
                 :desc "[:childs] - Add some extra styling to the parent"}
   :content {:type [:derefable :string :component]
             :desc "[:button :text] - card content"}
   :show-divider? {:type :boolean
                   :desc "[:button] - If true divider is displayed between icon and title/content"}
   :title {:type [:derefable :string :component]
           :desc "[:button] - card title"}
   :aria-label {:type [:derefable :string :keyword]
                :desc "[:button] - The aria label, which is used if there is no title/content of type string. If both are stings, this parameter takes priority."}
   :orientation {:type :keyword
                 :characteristics [:horizontal :vertical]
                 :desc "[:button] - Defines the content orientation."}
   :full-height? {:type :boolean
                  :desc "If true uses 100% of available height. Currently only works for carousel type."}
   :disabled? {:type [:derefable :boolean]
               :required false
               :desc "[:button] - Only for :type :button - If true, the card will be grayed out and the on-click will not be triggered "}
   :icon {:type [:string :keyword]
          :desc "[:button] - Set an icon. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :icon-position {:type :keyword
                   :characteristics [:start :end]
                   :desc "[:button] - Defines the position of icon"}
   :icon-params {:type :map
                 :desc "[:button] - Parameters for icon-component"}
   :on-click {:type :function
              :default-fn-str "(fn [event])"
              :desc "[:button] - Will be triggered, if user clicks on card"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:type :text
                         :orientation :horizontal
                         :icon-params {:size :xxl}
                         :aria-label-next :aria-carousel-next
                         :aria-label-previous :aria-carousel-previous
                         :show-divider? true
                         :icon-position :start
                         :full-height? false
                         :auto-slide? false
                         :slide-direction :right
                         :slide-timeout-ms 10000
                         :disabled? false})

;; ---------------------------------------------------------------------
;; Tailwind migration of styles/src/scss/components/_card.scss's `%card`/
;; `.card`/`.btn-card` family (the `.card__list__ordered`/`.card__element`/
;; `.order__controls`/`.title__bar` family the same sheet defines has no
;; ui_base owner -- it is migrated at its two direct-builder sites,
;; woco/presentation/sidebar.cljs and configuration/views/data_management/
;; geographic_attributes.cljs, per Step 1).
;;
;; `card`/`btn-card` (the hiccup keyword classes below) stay as literal DOM
;; classes: base/_frames.scss (`.window-placement-overlay .card`,
;; `.window-handling-tour .card`), components/_projects.scss
;; (`.projects .card`), components/_welcome_page.scss
;; (`.help__section .btn-card.vertical`) and components/_search.scss
;; (`.search__card .btn-card`, `.search__card .btn-card .column`) key off
;; them via plain descendant selectors and are not migrated yet -- dropping
;; the marker classes would break those sheets' overrides.
(def ^:private card-base-class
  "rounded-xl shadow-md bg-(--bg-over-bg) no-underline text-(--text) border border-(--border-secondary) p-3")

;; `.btn-card { @extend %card; ... }` in the old sheet -- both land in this
;; same component, so the extended declarations are simply folded into one
;; stack (no cross-sheet inline+comment needed; that pattern is for a
;; DIFFERENT eventual owner, see Task 10's `_table.scss` finding).
;; Transition has mixed per-property durations (240ms bg, 120ms shadow/
;; transform) -- full arbitrary-property shorthand, not `transition-[props]
;; duration-<N>` (Task 7's tabs finding: that combo assumes one duration for
;; the whole property list).
(def ^:private btn-card-class
  (str card-base-class
       " flex items-center gap-3 text-left cursor-pointer "
       "[transition:background-color_.24s_ease,box-shadow_.12s_ease,transform_.12s_ease] "
       "has-[button]:cursor-default"))

(def ^:private btn-card-vertical-class "vertical flex-col")
;; `!` on every property: old CSS's `.btn-card.disabled` has real (0,2,0)
;; specificity, unconditionally beating the base `.btn-card` rule's
;; (0,1,0) bg/text/cursor/shadow declarations regardless of source order.
;; Flat Tailwind utility classes of equal (0,1,0) specificity are instead
;; tie-broken by Tailwind's own internal generation order, NOT by this
;; class list's order -- verified via compile-probe that, without `!`,
;; `bg-(--bg-over-bg)` (from `card-base-class`) and `cursor-pointer` (from
;; `btn-card-class`) silently won over this class's `bg-(--bg-hover)`/
;; `cursor-default` (caught by the harness diff gate: real background-color
;; and cursor regressions on every `card-button-*-disabled` instance).
(def ^:private btn-card-disabled-class
  "disabled bg-(--bg-hover)! text-(--text-disabled)! cursor-default! shadow-none!")

;; `&:is(a,button):not(.disabled)` states -- only ever applied below to the
;; real `:button.btn-card` this component renders (button-impl);
;; carousel-impl's `.btn-card` is a `<div>`, so it never gets these classes,
;; reproducing the old `:is(a,button)` restriction without encoding it in
;; the class string itself. `.disabled` has no `pointer-events:none` here
;; (unlike `.card__button.disabled`), so the old rule needs the explicit
;; `:not(.disabled)` guard -- translated as `not-[.disabled]`.
(def ^:private btn-card-interactive-class
  (str "hover:not-[.disabled]:bg-(--bg-hover) hover:not-[.disabled]:shadow-sm "
       "active:not-[.disabled]:scale-97 active:not-[.disabled]:shadow-xs "
       "focus-visible:not-[.disabled]:outline-2 "
       "focus-visible:not-[.disabled]:outline-(--border-focus) "
       "focus-visible:not-[.disabled]:outline-offset-2"))

(def ^:private btn-card-column-class "flex flex-col self-stretch")
(def ^:private btn-card-divider-class "self-stretch border border-(--divider)")

;; `:where(h1,h2,h3,h4,h5,h6,p) { margin:0; padding:0; }` -- applied
;; directly to the literal h4/p tags this component renders below instead of
;; reproduced as a selector (Tailwind has no zero-specificity utility
;; mechanism). No current card title/content is ever anything other than a
;; plain string wrapped in these tags with no other classes, so a plain
;; `m-0 p-0` is computed-identical everywhere it's used.
(def ^:private btn-card-heading-reset-class "m-0 p-0")

;; `:where(span[class^="icon-"]) { background-color: var(--icon); }` --
;; also zero-specificity in the old sheet (deliberately losing to any
;; per-instance icon `:color`). No current card call site ever passes a
;; custom icon `:color`/`:color-important?` (verified: harness + the one
;; production caller, woco/workspace/hints.cljs, both pass a bare `:icon`),
;; so threading a plain (real-specificity) class via icon-impl below is
;; computed-identical for every existing caller. See task report.
(def ^:private btn-card-icon-class "bg-(--icon)")
(def ^:private btn-card-icon-disabled-class "bg-(--icon-disabled)")

(defn- divider [{:keys [show-divider? title content]
                 icon-key :icon}]
  (when (and icon-key show-divider? (or title content))
    [:span.divider {:class btn-card-divider-class}]))

(defn- icon-tint-extra-class [caller-extra-class disabled?]
  (let [tint-class (if disabled? btn-card-icon-disabled-class btn-card-icon-class)]
    (cond
      (nil? caller-extra-class) tint-class
      (vector? caller-extra-class) (conj caller-extra-class tint-class)
      :else [caller-extra-class tint-class])))

(defn- icon-impl [{:keys [icon-params disabled?]
                   icon-key :icon}]
  (when icon-key
    [icon (assoc icon-params
                 :icon icon-key
                 :extra-class (icon-tint-extra-class (:extra-class icon-params) disabled?))]))

(defn- card-extra-class [extra-class]
  (cond
    (nil? extra-class) card-base-class
    (vector? extra-class) (conj extra-class card-base-class)
    :else [extra-class card-base-class]))

(defn- childs-impl [{:keys [extra-style extra-class]} childs]
  (apply conj
         [:div.card (cond-> {:class (card-extra-class extra-class)}
                      extra-style (assoc :style extra-style))]
         childs))

(defn- text-impl [{:keys [content]}]
  (let [content (val-or-deref content)]
    [:div.card {:class card-base-class}
     (if (string? content)
       [:p content]
       content)]))

(defn- button-impl [{:keys [title content disabled? orientation
                            icon-position aria-label
                            on-click]
                     :as params}]
  (let [disabled? (val-or-deref disabled?)
        title (val-or-deref title)
        aria-label (translate-label aria-label)
        content (val-or-deref content)
        vertical? (= orientation :vertical)
        show-content? (or title content)
        icon-start-position? (= icon-position :start)]
    [:button.btn-card {:type "button"
                       :class (cond-> [btn-card-class btn-card-interactive-class]
                                disabled? (conj btn-card-disabled-class)
                                vertical? (conj btn-card-vertical-class))
                       :aria-label (cond
                                     aria-label aria-label
                                     (string? title) title
                                     (string? content) content
                                     :else "")
                       :on-click (fn [e]
                                   (when (and (not disabled?)
                                              (fn? on-click))
                                     (on-click e)))}
     (when icon-start-position?
       [:<>
        [icon-impl (assoc params :disabled? disabled?)]
        [divider (assoc params :title title :content content)]])

     (when show-content?
       [(if vertical?
          :<>
          :div.column)
        (if vertical? {} {:class btn-card-column-class})
        (if (string? title)
          [:h4 {:class btn-card-heading-reset-class} title]
          title)
        content])
     (when-not icon-start-position?
       [:<>
        [divider (assoc params :title title :content content)]
        [icon-impl (assoc params :disabled? disabled?)]])]))

(defn- carousel-impl [{:keys [auto-slide? slide-direction slide-timeout-ms full-height?]}]
  (let [slide-idx (r/atom 0)
        slide-timer (r/atom nil)
        next-slide-fn #(swap! slide-idx (fn [idx] (inc idx)))
        prev-slide-fn #(swap! slide-idx (fn [idx] (dec idx)))
        auto-fn (if (= slide-direction :right)
                  next-slide-fn
                  prev-slide-fn)
        reset-timer #(swap! slide-timer (fn [timer]
                                          (js/clearInterval timer)
                                          (js/setInterval auto-fn slide-timeout-ms)))]
    (r/create-class
     {:component-did-mount #(when auto-slide? (reset-timer))
      :component-will-unmount #(swap! slide-timer js/clearInterval)
      :reagent-render
      (fn [{:keys [items aria-label-next aria-label-previous]}]
        (let [items (val-or-deref items)
              item-count (count items)
              current-idx (mod @slide-idx item-count)
              {:keys [title content] :as item} (get items current-idx)]
          [:div.btn-card.justify-between {:class (cond-> [btn-card-class]
                                                   (map? item)
                                                   (conj "col-start-2" "col-end-9")
                                                   full-height? (conj "h-full"))}
           [button {:variant :tertiary
                    :aria-label (translate-label aria-label-previous)
                    :start-icon :prev
                    :on-click (fn [_]
                                (reset-timer)
                                (prev-slide-fn))}]
           (cond
             (string? item)
             [:h4 {:class btn-card-heading-reset-class} item]
             (map? item)
             [:div.text-center
              ;; `.mb-2` already has real (0,1,0) specificity, which always
              ;; beat the old zero-specificity `:where(h4)` margin:0 reset --
              ;; so the 8px bottom margin was already visible in production.
              ;; Only the (previously zero-specificity-reset) top margin
              ;; needs an explicit `mt-0` now that `:where()` is gone.
              [:h4.mb-2 {:class "mt-0 p-0"} title]
              [:p.text-center {:class btn-card-heading-reset-class} content]]
             :else item)
           [button {:variant :tertiary
                    :aria-label (translate-label aria-label-next)
                    :start-icon :next
                    :on-click (fn [_]
                                (reset-timer)
                                (next-slide-fn))}]]))})))

(defn ^:export card [params & childs]
  (let [{card-type :type :as params}
        (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "card" specification params)}
     (case card-type
       :text [text-impl params]
       :childs [childs-impl params childs]
       :button [button-impl params]
       :carousel [carousel-impl params]
       (do (error "Card type not supported")
           "Error"))]))
