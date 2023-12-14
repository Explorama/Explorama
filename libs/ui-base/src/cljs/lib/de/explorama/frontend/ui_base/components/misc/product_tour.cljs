(ns de.explorama.frontend.ui-base.components.misc.product-tour
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [react-dom :as react-dom]
            [reagent.core :as reagent]
            [taoensso.timbre :refer-macros [error]]))

(def parameter-definition
  {:val-sub-fn {:type :function
                :required true
                :desc "Function that will be used for title, and description from the step.
                                    It will get either a vector (subscription) or a keyword (translation-key)."}
   :top {:type [:number :string :derefable]
         :desc "The top-position (absolute). Can be both a simple number or a string like '50%'."}
   :left {:type [:number :string :derefable]
          :desc "The left-position (absolute). Can be both a simple number or a string like '50%'."}
   :right {:type [:number :string :derefable]
           :desc "The right-position (absolute). Use this instead of :left if necessary, e.g., when the element is cut off at the right-hand side of the screen."}
   :bottom {:type [:number :string :derefable]
            :desc "The bottom-position (absolute). Use this instead of :top if necessary, e.g., when the element is cut off at the bottom side of the screen."}
   :offset-top {:type [:number :derefable]
                :desc "Relative top-position to the absolute."}
   :offset-left {:type [:number :derefable]
                 :desc "Relative left-position to the absolute."}
   :offset-right {:type [:number :derefable]
                  :desc "Relative right-position to the absolute."}
   :offset-bottom {:type [:number :derefable]
                   :desc "Relative bottom-position to the absolute."}
   :component {:type :keyword
               :required true
               :desc "The component that it used in.
                                   This is getting used to check if the current-step should be shown or not."}
   :additional-info {:type :keyword
                     :desc "Additional-info to be checked against the current-step. 
                                         If this is nil then all steps for the component will be shown here."}
   :prev-fn {:type :function
             :required true
             :desc "Click function for the back-button. Gets as parameter the current-step."}
   :next-fn {:type :function
             :required true
             :desc "Click function for the next-button. Gets as parameter the current-step."}
   :cancel-fn {:type :function
               :required true
               :desc "Click function for the close-button. Gets as parameter the current-step."}
   :cancel-aria-label {:type [:string :derefable :keyword]
                       :desc "Aria label for the close-button."}
   :steps-label {:type [:string :derefable]
                 :desc "The Steps label should be a formatted string containing two %s.
                                     The first %s is the current-step the second is the maximum of steps."}
   :language {:type [:string :derefable]
              :desc "The language to be used for number formating the steps."}
   :back-button-label {:type [:string :derefable]
                       :desc "Label for the back-button."}
   :show-back-button? {:type [:boolean :derefable]
                       :desc "If true the back-button will be shown when the step-num is > 1."}
   :next-button-label {:type [:string :derefable]
                       :desc "Label for the next-button."}
   :max-steps {:type [:number :derefable]
               :required true
               :desc "Number of maximum steps."}
   :current-step {:type [:map :derefable]
                  :required true
                  :definition :step
                  :desc "The current-step that will be shown. See Step for more details."}
   :portal-target {:type :string
                   :desc "ID for html-element to which the portal should be added."}
   :z-index {:type :number
             :desc "z-index of the step"}
   :parent-bounding-rect {:type [:map :derefable]
                          :desc "Representing the absolute position of the parent (left, right, top, bottom)."}})
(def step-definition
  {:component {:type :keyword
               :required true
               :desc "Describes the component that is involved."}
   :additional-info {:type :keyword
                     :required true
                     :desc "Describes what action this step is."}
   :step {:type :number
          :required true
          :desc "The current step number."}
   :auto-next? {:type :boolean
                :desc "Determins if the next-step gets called automatically.
                                    In this case no next Button will be shown."}
   :next-active-sub {:type :vector
                     :desc "Subscription to know if the next-step Button should be active."}
   :title {:type [:string :keyword]
           :required true
           :desc "Title for the string, if keyword it will be translated with the val-sub function."}
   :description {:type :vector
                 :required true
                 :desc "The description for the step. 
                                     Allowed values for the vector:
                                     [:translation <keyword>]
                                     [:img <img-src-string> <alt-desc>]
                                     [:msg <translation-vec|img-vec>+]"}
   :top-desc-img {:type :string
                  :desc "Image that will be shown above the title."}
   :top-desc-alt-img {:type :string
                      :required true
                      :require-cond [:top-desc-img :*]
                      :desc "Alternative text that will be shown."}})
(def specification (parameters->malli parameter-definition {:step step-definition}))
(def default-parameters {:offset-top 10
                         :steps-label "Step %s/%s"
                         :cancel-aria-label :aria-product-tour-cancel
                         :language "en-EN"
                         :show-back-button? false
                         :back-button-label "Back"
                         :next-button-label "Next"
                         :portal-target "workspace-root"
                         :z-index 50000})

(def main-class ["dialog" "dialog-compact" "dialog-auto-size" "product-tour" "absolute"])
(def header-class "dialog-header")
(def title-class ["flex" "justify-between" "align-items-center"])
(def body-class "dialog-body")
(def footer-class "dialog-footer")
(def steps-class ["text-xs" "text-secondary"])

(defn- format-number [language number-val]
  (when (and number-val
             (number? number-val)
             language)
    (.toLocaleString number-val language)))

(defn- step-img [img alt]
  [:img {:src img :alt alt}])

(defn- step-icon [val-sub-fn icon-params]
  [icon (update icon-params
                :tooltip
                (fn [tip]
                  (when tip
                    (val-sub-fn tip))))])

(defn- description-helper [val-sub-fn val]
  (reduce (fn [acc desc-part]
            (conj acc
                  (let [[type val desc] desc-part]
                    (case type
                      :msg (with-meta
                             [:<>
                              (description-helper val-sub-fn val)]
                             {:key (str desc-part "-msg")})
                      :translation (val-sub-fn val)
                      :icon (with-meta
                              [step-icon val-sub-fn val]
                              {:key (str desc-part "-icon-" val)})
                      :img (with-meta
                             [:img {:src val :alt desc}]
                             {:key (str desc-part "-" val)})
                      (do (error "Unknown description-type defined" desc-part)
                          nil)))))
          [:<>]
          val))

(defn- description [val-sub-fn active-desc]
  (reduce (fn [acc desc-part]
            (let [[type & vals] desc-part
                  val (first vals)
                  desc (second vals)]
              (case type
                :msg (conj
                      acc
                      (with-meta
                        [description-helper val-sub-fn vals]
                        {:key (str desc-part "-" vals)}))
                :translation (conj acc
                                   (with-meta
                                     [:<>
                                      (val-sub-fn val)]
                                     {:key (str desc-part "-" val)}))
                :icon (conj acc
                            (with-meta
                              [step-icon val-sub-fn val]
                              {:key (str desc-part "-icon-" val)}))
                :img (conj acc (with-meta
                                 [step-img val desc]
                                 {:key (str desc-part "-" val)}))
                (do (error "Unknown description-type defined" desc-part)
                    nil))))
          [:<>]
          active-desc))

(defn- portal-comp [props & childs]
  (reagent/create-class
   {:display-name "context-menu portal"
    :reagent-render
    (fn [{:keys [left top right bottom z-index
                 parent-bounding-rect]} & childs]
      (let [{parent-top :top
             parent-left :left
             parent-right :right
             parent-bottom :bottom} (val-or-deref parent-bounding-rect)
            left (val-or-deref left)
            top (val-or-deref top)
            right (val-or-deref right)
            bottom (val-or-deref bottom)
            z-index (val-or-deref z-index)]
        (apply conj
               [:div {:style {:position :absolute
                              :left (or parent-left left)
                              :top (or parent-top top)
                              :right (or parent-right right)
                              :bottom (or parent-bottom bottom)
                              :z-index z-index}}]
               childs)))}))

(defn- portal [{:keys [portal-target] :as props} childs]
  (let [portal-target (val-or-deref portal-target)
        portal-target-elem (if-let [ele (.getElementById js/document portal-target)]
                             ele
                             js/document.body)]
    (react-dom/createPortal
     (reagent/as-element
      [portal-comp props childs])
     portal-target-elem)))

(defn ^:export product-tour-step [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "product-tour" specification params)}
     (let [{:keys [offset-top offset-left offset-right offset-bottom
                   val-sub-fn
                   component additional-info
                   prev-fn next-fn cancel-fn
                   cancel-aria-label
                   steps-label language
                   back-button-label next-button-label
                   max-steps current-step show-back-button?]}
           params
           {active-comp :component
            active-info :additional-info
            active-step :step
            auto-next? :auto-next?
            active-sub? :next-active-sub
            active-title :title
            active-description :description
            top-img :top-desc-img
            top-alt-img :top-desc-alt-img
            :as current-step} (val-or-deref current-step)
           active-next? (if (vector? active-sub?)
                          (val-sub-fn active-sub?)
                          true)
           title (if (keyword? active-title)
                   (val-sub-fn active-title)
                   active-title)
           language (val-or-deref language)
           steps-label (val-or-deref steps-label)
           back-button-label (val-or-deref back-button-label)
           next-button-label (val-or-deref next-button-label)
           top (val-or-deref offset-top)
           left (val-or-deref offset-left)
           right (val-or-deref offset-right)
           bottom (val-or-deref offset-bottom)
           max-steps (val-or-deref max-steps)
           show-back-button? (val-or-deref show-back-button?)]
       (when (and (= component active-comp)
                  (or (nil? additional-info)
                      (= additional-info active-info)))
         [portal params
          [:div {:class main-class
                 :style {:width "250px"
                         :top top
                         :left left
                         :right right
                         :bottom bottom}}
           [:div {:class header-class}
            [:div {:class title-class} title
             (when (seq top-img)
               [step-img top-img top-alt-img])
             [button {:start-icon :close
                      :variant :tertiary
                      :size :small
                      :aria-label cancel-aria-label
                      :on-click #(cancel-fn current-step)}]]]
           [:div {:class body-class}
            [description val-sub-fn active-description]]
           [:div {:class footer-class}
            [:div {:class steps-class}
             (format steps-label
                     (format-number language active-step)
                     (format-number language max-steps))]
            (when (and show-back-button? (> active-step 1))
              [button {:label back-button-label
                       :on-click #(prev-fn current-step)}])
            (when-not auto-next?
              [button {:label next-button-label
                       :disabled? (not active-next?)
                       :variant :secondary
                       :on-click #(next-fn current-step)}])]]]))]))