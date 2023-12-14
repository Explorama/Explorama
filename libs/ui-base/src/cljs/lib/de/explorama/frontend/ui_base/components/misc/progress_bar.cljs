(ns de.explorama.frontend.ui-base.components.misc.progress-bar
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:progress {:type [:number :derefable]
              :required true
              :desc "The progress in %, the range is 0-100"}
   :animated? {:type :boolean
               :desc "Disable the gradient animation"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:animated? true})

(def base-class "progress-bar")
(def bar-class "animation-gradient")

(defn ^:export progress-bar [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "progress-bar" specification params)}
     (let [{:keys [progress animated?]} params
           progress (val-or-deref progress)]
       (if progress
         [:div {:class base-class}
          [:span {:class (when animated? bar-class)
                  :style {:width (str (or progress 0)
                                      "%")}}]]
         [:<>]))]))