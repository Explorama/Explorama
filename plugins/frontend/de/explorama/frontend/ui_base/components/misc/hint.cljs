(ns de.explorama.frontend.ui-base.components.misc.hint
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]))

(def parameter-definition
  {:variant {:type :keyword
             :characteristics [:default :info :warning :error]
             :required false
             :desc "Defines the styling variant of the hint."}
   :title {:type [:derefable :string :component]
           :desc "The title of your hint."}
   :content {:type [:derefable :string :component]
             :required true
             :desc "The content that will be displayed in your hint."}
   :icon {:type [:string :keyword]
          :required false
          :desc "There are default icons for :info, :warning and :error - but you can set any incon for any variant. The usage of keywords is suggested, see the icon component for more infos."}
   :icon-params {:type :map
                 :desc "Parameters for icon-component"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:variant :default})

;; tailwind: migrated from styles/src/scss/components/_hints.scss.
;; Whether the icon renders is known here in Clojure (`hint-display-icon`),
;; so the old `.hint:not(:has(> span[class^="icon-"]))` CSS state selector
;; becomes a plain `if` between two full layout class strings instead of a
;; `:has()` variant. Variant colors are likewise `case`-selected full literal
;; strings on `:variant`, never combined with each other, so no two `bg-*`/
;; `border-*` utilities for the same property ever land on one element.
;; The icon's background-color utility carries `!` because the old
;; `.hint > span[class^="icon-"] { background-color: ...; }` rule had higher
;; specificity than any `bg-*` class the icon component would add from a
;; caller-supplied `icon-params :color` -- i.e. it unconditionally won
;; before this migration too (a pre-existing quirk, not introduced here; no
;; current caller passes `icon-params :color` to `hint`, so this is
;; belt-and-braces parity, not an observed regression fix).
(def ^:private hint-with-icon-class "flex flex-row gap-2 p-2 rounded-sm border")
(def ^:private hint-without-icon-class "flex flex-col gap-1 p-2 rounded-sm border")
(def ^:private hint-default-color-class "bg-gray-50 border-gray-200")
(def ^:private hint-warning-color-class "bg-orange-50 text-orange-700 border-orange-200")
(def ^:private hint-error-color-class "bg-red-50 text-red-700 border-red-200")
(def ^:private hint-icon-structural-class "w-4 h-4 mt-0.5 flex-none")
(def ^:private hint-icon-default-color-class "bg-gray-900!")
(def ^:private hint-icon-warning-color-class "bg-orange!")
(def ^:private hint-icon-error-color-class "bg-red!")
(def ^:private hint-content-class "flex flex-col")
(def ^:private hint-title-class "font-bold")

(defn- hint-display-icon [{:keys [variant] ico :icon}]
  (or ico
      (case variant
        :info    :info-circle
        :warning :warning
        :error   :error
        nil)))

(defn- hint-icon [params]
  (let [{:keys [icon-params variant]} params
        display-icon (hint-display-icon params)]
    (when display-icon
      (let [icon-color-class (case variant
                                :warning hint-icon-warning-color-class
                                :error   hint-icon-error-color-class
                                hint-icon-default-color-class)
            structural-class (str hint-icon-structural-class " " icon-color-class)
            caller-extra-class (:extra-class icon-params)]
        [icon (merge icon-params
                     {:icon display-icon
                      :extra-class (cond
                                     (nil? caller-extra-class) structural-class
                                     (vector? caller-extra-class) (conj caller-extra-class structural-class)
                                     :else [caller-extra-class structural-class])})]))))

(defn- hint-content [params]
  (let [{:keys [title content]} params]
    (if title
      [:div {:class hint-content-class}
       [:div {:class hint-title-class}
        title]
       [:div content]]
      content)))

(defn- hint-parent [params]
  (let [{:keys [variant]} params
        layout-class (if (hint-display-icon params)
                       hint-with-icon-class
                       hint-without-icon-class)
        color-class (case variant
                      :warning hint-warning-color-class
                      :error   hint-error-color-class
                      hint-default-color-class)]
    [:div {:class (str layout-class " " color-class)}
     [hint-icon params]
     [hint-content params]]))

(defn ^:export hint [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "hint" specification params)}
     [hint-parent params]]))
