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
(def parent-class "explorama__lights")
(def default-parameters {:hint-icon "lights__info"
                         :parent-class parent-class})

(def light-class "lights__status")
(def message-class "lights__message")
(def red-class "lights--red")
(def yellow-class "lights--yellow")
(def green-class "lights--green")
(def grey-class "lights--grey")

(defn ^:export traffic-light [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "traffic-light" specification params)}
     (let [{:keys [color label hint-icon icon-params
                   tooltip-params hint-text parent-class]}
           params
           label (val-or-deref label)
           color (val-or-deref color)
           color-class (case color
                         :red red-class
                         :yellow yellow-class
                         :green green-class
                         :grey grey-class
                         nil)]
       [:div {:class parent-class}
        (when color-class
          [:span {:class [light-class
                          color-class]}])
        (when label
          [:span {:class message-class}
           label])
        (when (and hint-text hint-icon)
          [tooltip (assoc (or tooltip-params {})
                          :text hint-text)
           [icon (assoc (or icon-params {})
                        :icon hint-icon)]])])]))
