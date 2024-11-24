(ns de.explorama.frontend.ui-base.components.formular.radio
  (:require
   [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
   [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
   [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:aria-label {:type [:string :derefable]
                :required :aria-label
                :desc "Aria label for the select component. Should be set if the label is a component. Takes priority over the label and placeholder."}
   :on-change {:type :function
               :required true
               :desc "Will be triggered when an this elements is selected."}
   :label {:type [:string :derefable]
           :required :aria-label
           :desc "Normal label text and fallback for aria-label. Only for (= label-variant :simple)."}
   :strong-label {:type [:string :derefable]
                  :desc "Additional label option. Creates a bold text next to the radio button. Only for (= label-variant :simple)."}
   :extra-class {:type :string
                 :desc "Classname which will be added to radio button."}
   :checked? {:type :boolean
              :required true
              :desc "Marks the radio button as selected."}
   :disabled? {:type :boolean
               :desc "Marks the radio button as disabled."}
   :id {:type :string
        :desc "Html id - will generate a id if not provided."}
   :tabindex {:type :number
              :desc "Defines the tabindex"}
   :name {:type :string
          :desc "Name of the radio button group. There can only be one selected radio button in a group."}
   :label-variant {:type :keyword
                   :desc ":simple (default) and :img. :img uses an images as label."}
   :src {:type :string
         :desc "Image source url for (= label-variant :img)."}
   :width {:type :number
           :desc "Image width for (= label-variant :img)."}
   :height {:type :number
            :desc "Image height url for (= label-variant :img)."}
   :alt {:type :string
         :desc "Image alt for (= label-variant :img)."}
   :tooltip-class {:type :string
                   :desc "Classes for the provided tooltip."}
   :tooltip {:type [:string :derefable]
             :desc "Tooltip label"}})

(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:disabled? false
                         :tabindex 0
                         :name :id
                         :label-variant :simple})

(def uuid-prefix "ui-base.formular.radio-")
(defn- make-uuid []
  (str uuid-prefix (random-uuid)))

(defn radio-button [{:keys [checked? disabled? label strong-label extra-class
                            id tabindex on-change aria-label label-variant
                            src width height alt tooltip-class tooltip name]}]
  (let [strong-label (val-or-deref strong-label)
        label (val-or-deref label)
        id (or id (make-uuid))]
    [:div (cond-> {:class "radio"}
            (= :img label-variant)
            (update :class str " align-items-center")
            extra-class
            (update :class str " " extra-class))
     [:input (cond-> {:type :radio
                      :aria-label (or (val-or-deref aria-label)
                                      (str (or strong-label "")
                                           (or label "")))
                      :name (or name id)
                      :id id
                      :tabIndex (or tabindex 0)
                      :checked (or checked? false)}
               on-change (assoc :on-change
                                #(when (not disabled?)
                                   (on-change (not checked?) %)))
               disabled? (assoc :disabled disabled?))]
     (let [label-variant (or label-variant :simple)]
       (case label-variant
         :simple
         (when (or label
                   strong-label)
           (cond-> [:label {:for id}]
             strong-label
             (conj [:strong strong-label])
             label
             (conj label)))
         :img
         [:label {:for id}
          [:img {:src src
                 :width width
                 :height height
                 :alt (or alt tooltip)}]]))
     (when tooltip
       [:span {:class tooltip-class
               :title (val-or-deref tooltip)}])]))

(defn  ^:export radio [props]
  [error-boundary {:validate-fn #(validate "radio" specification props)}
   [radio-button props]])
