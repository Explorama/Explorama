(ns de.explorama.frontend.ui-base.overview.formular.textarea
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.formular.textarea :refer [textarea default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Textarea"
   :desc "Standard formular component for multi-line inputs"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [textarea]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [textarea {:aria-label "on change example"
             :on-change (fn [val]
                          (js/console.log "Value changed:" val))}]
  {:title "On-change"})

(re-frame/reg-event-db
 :change-ta-value
 (fn [db [_ new-value]]
   (assoc db :value new-value)))

(re-frame/reg-sub
 :ta-value
 (fn [db]
   (get db :value "my-start-str")))

(defexample
  [textarea {:value (re-frame/subscribe [:ta-value])
             :aria-label "re-frame value example"
             :on-change (fn [val]
                          (re-frame/dispatch [:change-ta-value val]))}]
  {:title "Re-frame value"
   :code-before
   "(re-frame/reg-event-db
 :change-ta-value
 (fn [db [_ new-value]]
   (assoc db :value new-value)))

(re-frame/reg-sub
 :ta-value
 (fn [db]
   (get db :value \"my-start-str \")))   
   "})

(defexample
  [textarea {:default-value "You can't delete me"
             :aria-label "disabled example"
             :disabled? true}]
  {:title "Disabled"})

(defexample
  [textarea {:default-value "I'm seemless"
             :aria-label "borderless example"
             :borderless? true}]
  {:title "Borderless"})

(defexample
  [textarea {:read-only? true
             :aria-label "read-only example"
             :value "Readable"}]
  {:title "Read-Only"})

(defexample
  [textarea {:max-length 3
             :aria-label "max-length example"}]
  {:title "Max-Length"})

(defexample
  [textarea {:label "My Label"}]
  {:title "With label"})

(defexample
  [textarea {:label "Another label"
             :hint "Some hint"}]
  {:title "With hint"})

(defexample
  [textarea {:caption "I'll probably explain something"
             :aria-label "caption example"}]
  {:title "With caption"})

(defexample
  [textarea {:placeholder "Type in something"}]
  {:title "Placeholder"})

(defexample
  [textarea {:extra-class "input--w10"
             :aria-label "specific width example"}]
  {:title "Specific width"})

(defexample
  [:form
   [textarea {:required? true
              :aria-label "required with min-length example"
              :min-length 10}]
   [:button {:type :submit}
    "submit"]]
  {:title "Required with min-length"})