(ns de.explorama.frontend.ui-base.overview.formular.input-field
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.components.formular.input-field :refer [input-field default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Input Field"
   :desc "Standard formular component for inputs"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [input-field]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [input-field {:on-change (fn [val]
                             (js/console.log "Value changed:" val))
                :aria-label "on-change example"}]
  {:title "On-change"})

(re-frame/reg-event-db
 :change-inp-value
 (fn [db [_ new-value]]
   (assoc db :value new-value)))

(re-frame/reg-sub
 :inp-value
 (fn [db]
   (get db :value "my-start-str")))

(defexample
  [input-field {:value (re-frame/subscribe [:inp-value])
                :aria-label "re-frame value example"
                :on-change (fn [val]
                             (re-frame/dispatch [:change-inp-value val]))}]
  {:title "Re-frame-value"
   :code-before
   "(re-frame/reg-event-db
  :change-inp-value
  (fn [db [_ new-value]]
   (assoc db :value new-value)))

(re-frame/reg-sub
 :inp-value
 (fn [db]
  (get db :value \"my-start-str\")))"})

(defexample
  [input-field {:on-clear #(js/alert "I could be doing something to clear the input!")
                :aria-label "on-clear example"}]
  {:title "On-clear"})

(defexample
  [input-field {:on-blur #(js/alert "Textfield focus lost!")
                :aria-label "on-blur example"}]
  {:title "On-blur"})

(defexample
  [input-field {:default-value "my-default"
                :aria-label "default value example"}]
  {:title "Default-value"})

(defexample
  [input-field {:default-value "You can't delete me"
                :aria-label "disabled example"
                :disabled? true}]
  {:title "Disabled"})

(defexample
  [input-field {:max-length 3
                :aria-label "max-length example"}]
  {:title "Max-Length"})

(defexample
  [input-field {:label "My Label"}]
  {:title "With-Label"})

(defexample
  [input-field {:label [:div {:style {:background-color "#d58b8b"}}
                        "My Label"]
                :aria-label "My label"}]
  {:title "With-Component-Label"})

(defexample
  [input-field {:placeholder "Type in something"}]
  {:title "Placeholder"})

(defexample
  [input-field {:type :number
                :step 2
                :label "Input a number"}]
  {:title "Number Input-Field"})

(defexample
  [input-field {:type :number
                :aria-label "number input-field no formatting example"
                :step 2
                :num-format? false
                :min 2
                :max 4000}]
  {:title "Number Input-Field no formatting"})

(defexample
  (let [value (reagent/atom "")]
    [input-field {:type :number
                  :aria-label "number positive value handling example"
                  :min 2
                  :max 20
                  :step 0.01
                  :value value
                  :on-change #(reset! value %)}])
  {:title "Number positive value handling"})

(defexample
  (let [value (reagent/atom nil)]
    [input-field {:type :number
                  :aria-label "number negative value handling example"
                  :min -20
                  :max -2
                  :value value
                  :on-change #(reset! value %)}])
  {:title "Number negative value handling"})

(defexample
  (let [value (reagent/atom -1)]
    [input-field {:type :number
                  :aria-label "no error hiding example"
                  :min -20
                  :max -2
                  :value value
                  :no-error-hiding? true
                  :on-change #(reset! value %)}])
  {:title "No error hiding: Show any errors right from the beginning"})

(defexample
  (let [value (reagent/atom -2)]
    [input-field {:type :number
                  :aria-label "manual invalid example"
                  :min -20
                  :max -2
                  :value value
                  :invalid? true
                  :no-error-hiding? true
                  :on-change #(reset! value %)}])
  {:title "Manually set the number invalid to true"})

(re-frame/reg-event-db
 :num-change-inp-value
 (fn [db [_ k new-value]]
   (assoc-in db [:num-value k] new-value)))

(re-frame/reg-sub
 :num-inp-value
 (fn [db [_ k]]
   (get-in db [:num-value k])))

(defexample
  (let [from (re-frame/subscribe [:num-inp-value :from])
        to (re-frame/subscribe [:num-inp-value :to])]
    [:div.flex.gap-8
     [input-field {:type :number
                   :max to
                   :value from
                   :placeholder "From"
                   :on-change #(re-frame/dispatch [:num-change-inp-value :from %])}]
     [icon {:icon :minus}]
     [input-field {:type :number
                   :min from
                   :value to
                   :placeholder "To"
                   :on-change #(re-frame/dispatch [:num-change-inp-value :to %])}]])
  {:title "Number range example re-frame"})

(defexample
  [input-field {:start-icon :search
                :aria-label "icon example"}]
  {:title "Icon"})

(defexample
  [input-field {:compact? true
                :aria-label "compact example"}]
  {:title "Compact"})

(defexample
  [input-field {:type :color
                :aria-label "color example"}]
  {:title "Color Input-Field"})

(defexample
  [input-field {:extra-class "input--w8"
                :aria-label "specific with example"}]
  {:title "Specific-Width"})

(defexample
  [:form
   [input-field {:required? true
                 :aria-label "required example"}]
   [:button {:type :submit}
    "submit"]]
  {:title "Required"})

(defexample
  [:form
   [input-field {:invalid? true
                 :aria-label "invalid example"
                 :caption "Some error message"}]]
  {:title "Invalid"})

(defexample
  [input-field {:read-only? true
                :aria-label "read-only example"
                :value "Readable"}]
  {:title "Read-Only"})