(ns de.explorama.frontend.ui-base.overview.formular.button
  (:require [de.explorama.frontend.ui-base.components.formular.button :refer [button default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]
            [de.explorama.frontend.ui-base.utils.data-exchange :refer [download-content edn-content-type]]))

(defcomponent
  {:name "Button"
   :require-statement "[de.explorama.frontend.ui-base.components.formular.core :refer [button]]"
   :desc "Standard formular component for performing user actions like aborting something"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [button {:label "click-me"
           :on-click #(js/alert "clicked!")}]
  {:title "On-Click"})

(defexample
  [button {:label "I am primary"
           :variant :primary}]
  {:title "Primary-Variant"})

(defexample
  [button {:label "I am secondary"
           :variant :secondary}]
  {:title "Secondary-Variant"})

(defexample
  [button {:label "I am tertiary"
           :variant :tertiary}]
  {:title "Tertiary-Variant"})

(defexample
  [button {:label "I am big"
           :variant :primary
           :size :big}]
  {:title "Primary-Big"})

(defexample
  [button {:label "I am small"
           :variant :primary
           :size :small}]
  {:title "Primary-Small"})

(defexample
  [:<>
   [button {:label "Caution"
            :variant :primary
            :type :warning
            :start-icon :warning
            :size :small}]
   [:br]
   [button {:label "Wet Floor"
            :variant :secondary
            :start-icon :broom
            :type :warning
            :size :small}]
   [:br]
   [button {:label "Warning"
            :variant :tertiary
            :start-icon :warning
            :type :warning
            :size :small}]]
  {:title "Warning"})

(defexample
  [button {:label "Back"
           :variant :back
           :start-icon :previous
           :size :big}]
  {:title "Back"})

(defexample
  [button {:label "I am disabled"
           :variant :primary
           :disabled? true}]
  {:title "Primary-Disabled"})

(defexample
  [button {:label "my-label"
           :variant :primary
           :loading? true}]
  {:title "Loading"})

(defexample
  [button {:label "search"
           :start-icon :search}]
  {:title "Start-Icon"})

(defexample
  [button {:start-icon :search
           :title "Search"}]
  {:title "Icon Button"})

(defexample
  [button {:label "my-label"
           :extra-style {:cursor :default}}]
  {:title "Extra Style"})

(defexample
  [button {:label "Open Overview"
           :as-link "https://explorama.gitlab.io/utility/ui-base/#"}]
  {:title "As-Link"})

(defexample
  [button {:start-icon :save
           :title "Save"
           :on-click #(download-content "my-download.edn" {:my-key "test"} {:content-type edn-content-type})}]
  {:title "Download Button"
   :code-before "(:require [de.explorama.frontend.ui-base.utils.data-exchange :refer [download-content edn-content-type]])"})

(defexample
  [button {:start-icon :trash
           :title "Delete this."}]
  {:title "Button with title"})