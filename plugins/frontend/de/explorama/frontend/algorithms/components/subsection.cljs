(ns de.explorama.frontend.algorithms.components.subsection
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [reagent.core :as reagent]
            [taoensso.timbre :refer-macros [error]]))

(def default-parameters {:childs-on-root? false
                         :close-on-disabled? true})

(def section-base-class "settings__section__subsection")
(def section-100 "settings__section--100")
(def section-50 "settings__section--50")

(defn- collapsible [{:keys [label]}]
  [:h3 label])

(defn- add-childs [parent childs]
  (apply conj
         parent
         childs))

(defn- section-comp [{:keys [size] :as params}]
  [:div {:class [section-base-class
                 (case size
                   50 section-50
                   100 section-100
                   "")]}
   (when (:label params)
     [collapsible params])])

(defn section [params & childs]
  (reagent/create-class
   {:display-name "section"
    :component-did-catch (fn [_ e info]
                           (error "Section component crashed: " e info))
    :reagent-render
    (fn [params & childs]
      (let [{:keys [childs-on-root?] :as params} (merge default-parameters params)]
        (cond-> (if childs-on-root?
                  [:<> [section-comp params]]
                  (section-comp params))
          :always (add-childs childs))))}))
