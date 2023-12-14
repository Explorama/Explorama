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

(def ^:private parent-class "hint")
(def ^:private warning-class "hint-warning")
(def ^:private error-class "hint-error")
(def ^:private content-class "hint-content")
(def ^:private title-class "hint-title")
(def ^:private desc-class "hint-desc")

(defn- hint-icon [params]
  (let [{:keys [icon-params variant] ico :icon} params
        default-icon (case variant
                       :info    :info-circle
                       :warning :warning
                       :error   :error
                       nil)
        display-icon (or ico default-icon)]
    (when display-icon
      [icon (merge icon-params {:icon display-icon})])))

(defn- hint-content [params]
  (let [{:keys [title content]} params]
    (if title
      [:div {:class content-class}
       [:div {:class title-class}
        title]
       [:div {:class desc-class}
        content]]
      content)))

(defn- hint-parent [params]
  (let [{:keys [variant]} params
        extra-class (case variant
                      :warning warning-class
                      :error   error-class
                      nil)]
    [:div {:class [parent-class extra-class]}
     [hint-icon params]
     [hint-content params]]))

(defn ^:export hint [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "hint" specification params)}
     [hint-parent params]]))
