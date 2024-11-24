(ns de.explorama.frontend.ui-base.components.common.label
  (:require [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.common.tooltip :refer [tooltip]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [form-hint-class input-parent-class]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def default-parameters {})

(def error-class "invalid")
(def caption-class "input-hint")

(defn ^:export label [params]
  [error-boundary
   (let [{:keys [label hint for-id extra-class extra-style] :as params} (merge default-parameters params)
         label (val-or-deref label)]
     [:label (cond-> {:class []}
               extra-class (update :class conj extra-class)
               extra-style (assoc :style extra-style)
               for-id (assoc :for for-id))
      label
      (when hint
        [tooltip {:text hint}
         [:div {:class form-hint-class}
          [:span]]])])])

(defn ^:export parent-wrapper [params element]
  [error-boundary
   (let [{lb :label :keys [id label-params hint caption invalid? extra-class]}
         params]
     [:div {:class (cond-> [input-parent-class]
                     invalid? (conj error-class)
                     (coll? extra-class) (into extra-class)
                     (and extra-class (not (coll? extra-class)))
                     (conj extra-class))}
      (when lb
        [label (assoc (or label-params {})
                      :label lb
                      :hint hint
                      :for-id id)])
      element
      (when caption
        (let [caption (val-or-deref caption)]
          [:div {:class caption-class}
           caption]))])])