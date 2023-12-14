(ns de.explorama.frontend.indicator.views.elements.select-aggregation
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [select]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.views.management :as management]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [warn]]))

(defonce possible-options
  {:time [{:label :indicator-distinct
           :value :distinct}
          #_{:label :indicator-aggregation-range
             :value :range}]
   :group [{:label :indicator-distinct
            :value :distinct}
           #_{:label :indicator-aggregation-top-x
              :value :top-x}]
   :calc [{:label :indicator-sum
           :value :sum}
          {:label :indicator-min
           :value :min}
          {:label :indicator-max
           :value :max}
          #_{:label :indicator-aggregation-range
             :value :range}]
   :number-of-events [{:label :number-of-events
                       :value :count-events}]})

(re-frame/reg-sub
 ::aggregation-options
 (fn [db [_ indicator-id depends-on row-id]]
   (let [translate-fn (partial i18n/translate db)
         {:keys [content] :as dependend-comp-desc}
         (management/comp-desc-based-on-id db
                                           indicator-id
                                           depends-on)
         dependend-selected-value (if row-id
                                    (management/current-indicator-addon-row-value
                                     db indicator-id row-id depends-on)
                                    (management/current-indicator-comp-value
                                     db indicator-id depends-on))
         options (case content
                   :calc-attributes (:calc possible-options)
                   :time (:time possible-options)
                   :group-attributes (:group possible-options)
                   :all-attributes (get possible-options
                                        (:option-type dependend-selected-value)
                                        [])
                   (do
                     (warn "no clause for content-type" {:depends-on-id depends-on
                                                         :depends-desc dependend-comp-desc})
                     []))]
     (->> options
          (map (fn [opt]
                 (update opt
                         :label
                         translate-fn)))
          (sort-by (comp clojure.string/lower-case :label))
          vec))))

(defn element [indicator-id {:keys [label hint depends-on id disable]}]
  (let [disabled? @(re-frame/subscribe [::management/indicator-ui-comp-disabled? indicator-id disable])
        label (if (keyword? label)
                @(re-frame/subscribe [::i18n/translate label])
                (str label))
        hint-text (cond
                    (keyword? hint) (re-frame/subscribe [::i18n/translate hint])
                    (not (nil? hint)) (str hint))
        hint-text (if disabled?
                    (str hint-text
                         (when hint-text " (")
                         @(re-frame/subscribe [::i18n/translate :disabled-number-of-events])
                         (when hint-text ")"))
                    hint-text)]
    [select {:label label
             :extra-class "input--w14"
             :disabled? disabled?
             :mark-invalid? true
             :options (re-frame/subscribe [::aggregation-options indicator-id depends-on])
             :values (re-frame/subscribe [::management/indicator-ui-desc-comp-value indicator-id id])
             :on-change (fn [changed-val]
                          (re-frame/dispatch [::management/update-indicator-ui-desc
                                          indicator-id id changed-val]))}]))
