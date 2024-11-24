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
                       extra-class (conj extra-class))
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
