(ns de.explorama.frontend.indicator.views.elements.component
  (:require [de.explorama.frontend.indicator.views.elements.select-attributes :as select-attrs]
            [de.explorama.frontend.indicator.views.elements.select-aggregation :as select-aggre]
            [de.explorama.frontend.indicator.views.elements.custom-textarea :as c-textarea]
            [de.explorama.frontend.indicator.views.elements.range-number :as range-input]
            [taoensso.timbre :refer [warn]]))

(defonce attribute-selects
  #{:calc-attributes :time :group-attributes :all-attributes})

(defn comp-wrapper [indicator-id {:keys [label type content] :as desc}]
  (let [select? (= type :select)
        custom? (and (= type :textarea)
                     (= content :description))
        number? (= type :number)]
    (cond
      (and select?
           (attribute-selects content)) [select-attrs/element indicator-id desc]
      select? [select-aggre/element indicator-id desc]
      custom? [c-textarea/element indicator-id desc]
      number? [range-input/element indicator-id desc]
      :else (do
              (warn "No component for given description" desc)
              [:div "Missing Component defintion " label]))))