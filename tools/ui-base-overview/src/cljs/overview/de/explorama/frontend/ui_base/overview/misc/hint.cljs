(ns de.explorama.frontend.ui-base.overview.misc.hint
  (:require [de.explorama.frontend.ui-base.components.misc.hint :refer [default-parameters hint parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Hint"
   :desc "Component to display hints"
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [hint]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [:div
   {:style {:width :fit-content}}
   [hint {:content "some hint about anything"}]]
  {:title "Basic hint"})

(defexample
  [:div
   {:style {:width :fit-content}}
   [hint {:content "some hint about anything"
          :title "Attention!"}]]
  {:title "Hint title"})

(defexample
  [:div
   {:style {:width :fit-content}}
   [hint {:variant :info
          :content "some information to keep in mind"}]
   [hint {:variant :warning
          :content "be careful about these information"}]
   [hint {:variant :error
          :content "something went already wrong"}]]
  {:title "Special hints"})

(defexample
  [:div
   {:style {:width :fit-content}}
   [hint {:content "Some warning about geese I guess"
          :variant :warning
          :title "Geese"
          :icon :mosaic2}]]
  {:title "Custom icon"})
