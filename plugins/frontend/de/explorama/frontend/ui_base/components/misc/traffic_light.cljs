(ns de.explorama.frontend.ui-base.components.misc.traffic-light
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:color {:type [:keyword :derefable]
           :characteristics [:red :yellow :green :grey]
           :desc "The light color of the traffic light"}
   :parent-class {:type :string
                  :desc "The class for parent div of bar"}
   :icon-params {:type :map
                 :desc "Parameters for icon"}
   :tooltip-params {:type :map
                    :desc "Parameters for tooltip"}
   :label {:type [:string :derefable]
           :desc "The label after the traffic light"}
   :hint-icon {:type [:keyword :string]
               :desc "Icon of the hint as css-class (string) or keyword from icon-collection"}
   :hint-text {:type [:string :derefable]
               :desc "Hint text which will be visible as hint-icon tooltip"}})
(def specification (parameters->malli parameter-definition nil))

;; ---------------------------------------------------------------------
;; Class knowledge stays PRIVATE here (chip.cljs pattern): each def below
;; folds the literal marker token that sibling, not-yet-migrated sheets
;; still select on (styles/src/scss/components/_prediction.scss:256,261-272
;; and _section.scss:93-101 both use `:has(.explorama__lights .lights--*)`
;; / `:has(.lights--*)`) together with the Tailwind utility stack that
;; reproduces the migrated styles/src/scss/components/_search.scss rules
;; (`.explorama__lights` container, `.lights__status` base,
;; `.lights--red/yellow/green/grey` bg-image SVGs, `.lights__message`).
;; The marker token is ALWAYS kept as a literal space-separated class in
;; these strings so the sibling selectors keep matching unchanged.
;;
;; IMPORTANT ancestor-scoping note: the old sheet's `.lights__status` base
;; sizing/position and `.lights__message` display/margin were declared
;; `.explorama__lights .lights__status` / `.explorama__lights .lights__message`
;; -- i.e. ONLY when the DEFAULT `parent-class` ("explorama__lights") is in
;; effect. When a caller overrides `:parent-class` (search's own
;; main_search/core.cljs passes "search__resultinfo"), that ancestor never
;; matched, so those declarations never applied there in the old CSS: the
;; message span rendered as a bare, unstyled inline span, and the light span
;; (when present) got ONLY what `.search__resultinfo span.lights__status`
;; declared (a separate, size-only rule kept as a remnant in _search.scss,
;; scoped for Task 5). A first attempt at this migration baked these
;; utilities unconditionally onto the marker classes and broke exactly this:
;; caught by an 0.14%, 1826px localized diff in the search app screenshot
;; gate (the message text shifted a few px from the leaked `mx-1`/
;; `align-middle`). Fixed by keeping `*-default-context-utils` OUT of the
;; marker-class strings and only appending them in the render fn when
;; `default-context?` (merged `:parent-class` still equals the unoverridden
;; default) -- reproducing the ancestor-scope with plain conditional logic
;; instead of a CSS selector. The colour `bg-[url(...)]` utility stays
;; UNCONDITIONAL (not ancestor-scoped): both old ancestor contexts declared
;; byte-identical bg-image SVGs for each colour (verified), so it is safe,
;; and correct, for the component to always supply it regardless of which
;; parent-class the caller chose.
;; ---------------------------------------------------------------------

(def ^:private parent-class-marker "explorama__lights")
(def ^:private default-parent-class
  (str parent-class-marker " flex items-center pl-2"))

(def ^:private light-class-marker "lights__status")
;; `.explorama__lights .lights__status` needs fixed 30px/25px/4px sizing
;; (display:inline-block; vertical-align:middle; background-size:contain;
;; background-position:center 30%; background-repeat:no-repeat). Those px
;; values are computed-identical to Tailwind v4's dynamic rem-based spacing
;; scale (w-7.5 = 7.5 * 0.25rem = 1.875rem = 30px, h-6.25 = 6.25 * 0.25rem =
;; 1.5625rem = 25px, mr-1 = 0.25rem = 4px) under this project's fixed 16px
;; root font-size (no html{font-size} override in base/normalize.css) --
;; kept rem-based for consistency with the rest of this project's spacing
;; tokens.
(def ^:private light-class-default-context-utils
  "inline-block align-middle w-7.5 h-6.25 mr-1 bg-contain bg-[center_30%] bg-no-repeat")

(def ^:private message-class-marker "lights__message")
;; old (`.explorama__lights .lights__message`): display:inline-block;
;; vertical-align:middle; margin:0 4px.
(def ^:private message-class-default-context-utils
  "inline-block align-middle my-0 mx-1")

(def ^:private red-class
  "lights--red bg-[url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D%22500%22%20height%3D%22500%22%20viewBox%3D%220%200%20500%20500%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Cg%20fill%3D%22none%22%20fill-rule%3D%22evenodd%22%3E%3Cpath%20d%3D%22M362%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2069%2030.938%2069%2069%200%2038.063-30.938%2069-69%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%23E42820%22%2F%3E%3Cpath%20d%3D%22M182%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69M1%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%23C6C6C5%22%2F%3E%3C%2Fg%3E%3C%2Fsvg%3E')]")
(def ^:private yellow-class
  "lights--yellow bg-[url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D%22500%22%20height%3D%22500%22%20viewBox%3D%220%200%20500%20500%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Cg%20fill%3D%22none%22%20fill-rule%3D%22evenodd%22%3E%3Cpath%20d%3D%22M362%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2069%2030.938%2069%2069%200%2038.063-30.938%2069-69%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%23C6C6C5%22%2F%3E%3Cpath%20d%3D%22M182%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%23F9D960%22%2F%3E%3Cpath%20d%3D%22M1%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%23C6C6C5%22%2F%3E%3C%2Fg%3E%3C%2Fsvg%3E')]")
(def ^:private green-class
  "lights--green bg-[url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D%22500%22%20height%3D%22500%22%20viewBox%3D%220%200%20500%20500%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Cg%20fill%3D%22none%22%20fill-rule%3D%22evenodd%22%3E%3Cpath%20d%3D%22M362%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2069%2030.938%2069%2069%200%2038.063-30.938%2069-69%2069-38.125%200-69-30.938-69-69M182%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%23C6C6C5%22%2F%3E%3Cpath%20d%3D%22M1%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69%22%20fill%3D%22%234ACC2D%22%2F%3E%3C%2Fg%3E%3C%2Fsvg%3E')]")
(def ^:private grey-class
  "lights--grey bg-[url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20width%3D%22500%22%20height%3D%22500%22%20viewBox%3D%220%200%20500%20500%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Cg%20fill%3D%22%23C6C6C5%22%20fill-rule%3D%22evenodd%22%3E%3Cpath%20d%3D%22M362%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2069%2030.938%2069%2069%200%2038.063-30.938%2069-69%2069-38.125%200-69-30.938-69-69M182%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69M1%20250c0-38.063%2030.875-69%2069-69%2038.063%200%2068.938%2030.938%2068.938%2069%200%2038.063-30.875%2069-68.938%2069-38.125%200-69-30.938-69-69%22%2F%3E%3C%2Fg%3E%3C%2Fsvg%3E')]")

(def default-parameters {:hint-icon "lights__info"
                         :parent-class default-parent-class})

(defn ^:export traffic-light [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "traffic-light" specification params)}
     (let [{:keys [color label hint-icon icon-params
                   tooltip-params hint-text parent-class]}
           params
           label (val-or-deref label)
           color (val-or-deref color)
           default-context? (= parent-class default-parent-class)
           color-class (case color
                         :red red-class
                         :yellow yellow-class
                         :green green-class
                         :grey grey-class
                         nil)]
       [:div {:class parent-class}
        (when color-class
          [:span {:class (cond-> [light-class-marker color-class]
                          default-context? (conj light-class-default-context-utils))}])
        (when label
          [:span {:class (cond-> [message-class-marker]
                          default-context? (conj message-class-default-context-utils))}
           label])
        (when (and hint-text hint-icon)
          [tooltip (assoc (or tooltip-params {})
                          :text hint-text)
           [icon (assoc (or icon-params {})
                        :icon hint-icon)]])])]))
