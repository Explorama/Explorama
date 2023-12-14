(ns de.explorama.frontend.ui-base.components.formular.section
  (:require [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [form-hint-class]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [reagent.core :as reagent]))

(def parameter-definition
  {:label {:type [:derefable :string :component]
           :desc "An label or component which will be displayed as section title"}
   :disabled? {:type [:derefable :boolean]
               :desc "If true, open/closing is disabled"}
   :close-on-disabled? {:type :boolean
                        :desc "If true, section will be closed when :disabled is true"}
   :hint {:type [:derefable :string]
          :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
   :on-change {:type :function
               :desc "Triggered when section state is changing (open/close)"}
   :footer {:type :component
            :desc "Any combinations of buttons etc. which will be displayed at the bottom of an open section."}
   :default-open? {:type :boolean
                   :desc "If true, section will be open at mount"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:default-open? true
                         :disabled? false
                         :close-on-disabled? true})
(def collapsible-class "section")
(def collapsible-open-class "open")
(def collapsible-content-class "content")
(def collapsible-footer-class "footer")
(def collapsible-disabled-class "disabled")

(defn- section-header [{lb :label
                        :keys [hint disabled? on-change]
                        :as params}
                       is-open?]
  [:h2
   {:on-click #(when-not disabled?
                 (swap! is-open? not)
                 (when on-change
                   (on-change @is-open?)))}
   (val-or-deref lb)
   (when hint
     [tooltip {:text hint}
      [:div {:class form-hint-class}
       [:span]]])
   [:span.icon-collapse]])

(defn- can-open? [{:keys [close-on-disabled? disabled?]} is-open?]
  (and @is-open?
       (or (not close-on-disabled?)
           (and close-on-disabled? (not disabled?)))))

(defn- section-comp [{:keys [footer disabled?] :as params}
                     children is-open?]
  [:div {:class [collapsible-class
                 (when (can-open? params is-open?)
                   collapsible-open-class)
                 (when disabled?
                   collapsible-disabled-class)]}
   [section-header params is-open?]
   (into [:div {:class collapsible-content-class}]
         children)
   (when footer
     [:div {:class collapsible-footer-class}
      footer])])

(defn ^:export section [{:keys [default-open?]
                         :or {default-open? (get default-parameters :default-open?)}}
                        & _]
  (let [is-open? (reagent/atom (boolean default-open?))]
    (reagent/create-class
     {:display-name "section"
      :reagent-render
      (fn [params & children]
        (let [params (merge default-parameters params)]
          [error-boundary {:validate-fn #(validate "section" specification params)}
           (let [{:keys [disabled? close-on-disabled?]} params
                 disabled? (val-or-deref disabled?)
                 params (assoc params :disabled? disabled?)]
             (when (and close-on-disabled? disabled? @is-open?)
               (reset! is-open? false))
             [section-comp params children is-open?])]))})))
