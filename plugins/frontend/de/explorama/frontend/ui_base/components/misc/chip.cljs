(ns de.explorama.frontend.ui-base.components.misc.chip
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]))

(def colors {:teal "chip-teal"
             :orange "chip-orange"
             :red "chip-red"
             :yellow "chip-yellow"
             :green "chip-green"
             :blue "chip-blue"})
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

(def ^:private light-class "light")
(def ^:private dark-class "dark")

(def ^:private chip-base-class "chip")
(def ^:private chip-outline-class "outline")
(def ^:private chip-small-class "small")
(def ^:private chip-extra-small-class "extra-small")
(def ^:private chip-big-class "large")
(def ^:private chip-full-width-class "full")

(defn- chip- [{:keys [variant full-width? size color brightness
                      label start-icon start-icon-params
                      button-icon button-icon-params on-click
                      button-aria-label]
               :as params}]
  (let [size-class (case size
                     :small chip-small-class
                     :extra-small chip-extra-small-class
                     :big chip-big-class
                     "")
        color-class (get colors color "")
        brightness-class (case brightness
                           :dark dark-class
                           :light light-class
                           "")]
    [:span {:class (cond-> [chip-base-class]
                     (= variant :secondary) (conj chip-outline-class)
                     full-width? (conj chip-full-width-class)
                     size (conj size-class)
                     color (conj color-class)
                     brightness (conj brightness-class))}
     (when start-icon
       [icon (assoc (or start-icon-params {})
                    :icon start-icon)])
     label
     (when button-icon
       [:button {:on-click on-click
                 :aria-label (translate-label button-aria-label)}
        [icon (assoc (or button-icon-params {})
                     :icon button-icon)]])]))

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
