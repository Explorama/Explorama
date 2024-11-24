(ns de.explorama.frontend.ui-base.components.formular.loading-message
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:show? {:type [:boolean :derefable]
           :required true
           :desc "If true, loading-message is visible. Can be a boolean or derefable like an atom or re-frame subscription"}
   :size {:type :keyword
          :characteristics [:small :medium :large]
          :desc "The size of the loading animation"}
   :orientation {:type :keyword
                 :characteristics [:left :right]
                 :desc "Defines where the message is shown beside the loading de.explorama.indicator."}
   :message {:type [:string :derefable]
             :desc "Message which will be shown. Can be a string or derefable like an atom or re-frame subscription"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:show? false
                         :size :medium
                         :orientation :right})

(def wrapper-classes ["flex" "align-items-center" "gap-8"])
(def size-class-small "loader-sm")
(def size-class-medium "loader-md")
(def size-class-large "loader-lg")

(defn- loading-animation [size]
  (let [gen-span-fn #(->> (repeat [:span]) (take %) (into [:<>]))]
    (case size
      :small [:div {:class size-class-small} [gen-span-fn 1]]
      :medium [:div {:class size-class-medium} [gen-span-fn 4]]
      :large [:div {:class size-class-large} [gen-span-fn 9]])))

(defn ^:export loading-message [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "loading-message" specification params)}
     (let [{:keys [show? orientation message size]} params
           show? (val-or-deref show?)
           message (val-or-deref message)]
       (cond
         (and show? (nil? message))
         [loading-animation size]
         (and show? message (= orientation :left))
         [:div {:class wrapper-classes} message [loading-animation size]]
         (and show? message (= orientation :right))
         [:div {:class wrapper-classes} [loading-animation size] message]
         :else [:<>]))]))