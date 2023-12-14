(ns de.explorama.frontend.ui-base.components.misc.toolbar
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:id {:type :string
        :desc "The ID for the toolbar itself (not the wrapper which includes popups)"}
   :orientation {:type :keyword
                 :characteristics [:horizontal :vertical]
                 :desc "Horizontal to render the items from left to right and vertical to render them from top to bottom."}
   :tooltip-direction {:type :keyword
                       :characteristics [:up :down :left :right]
                       :desc "The direction all tooltips will open in."}
   :separator {:type :keyword
               :characteristics [:gap :symbol]
               :desc "Given a nested vector of items, decides how the segments are separated."}
   :extra-class {:type [:string :vector]
                 :desc "Additional classes to e.g., center the toolbar."}
   :extra-props {:type :map
                 :desc "Extra parameters for parent container"}
   :offset {:type :map
            :desc "Used to set the attributes :right, :left, :bottom and :top in css. Should only be used for moving toolbars (e.g. the viewport control, which moves with the sidebar). Otherwise, use classes like 'bottom-8'."}
   :z-index {:type :number
             :desc "Used to set the z-index directly if necessary."}
   :items {:type [:nested-vector :derefable]
           :required true
           :definition :toolbar-item
           :desc "A vector of items, which will be displayed according to the orientation. The vector can be nested once to separate the toolbar."}
   :on-click-toolbar-options {:type :function
                              :default-fn-str "(fn [event])"
                              :desc "Will be triggered when clicking on toolbar-options button"}
   :disabled-toolbar-options? {:type [:boolean :derefable]
                               :desc "Disables the toolbar-options button"}
   :toolbar-options-tooltip {:type [:string :derefable]
                             :desc "Tooltip for toolbar-options button"}
   :popouts {:type [:vector :derefable]
             :definition :popout
             :desc "Popouts, which can show additional content."}
   :popout-position {:type :keyword
                     :characteristics [:start :end]
                     :desc "The direction popouts will be opened, based on the DOM position. For verticals toolbars start translate to left of the toolbar and end translates to right. Horizontal toolbars will show them above for start and below for end."}})
(def toolbar-item-definition
  {:title {:type [:string :derefable]
           :required true
           :desc "The tooltip for <Item>."}
   :id {:type :string
        :required true
        :desc "The item-id to identify."}
   :type {:type :keyword
          :characteristics [:button :text :divider]
          :desc ":text will only show the label and ignore all other settings. :divider will show only a divider"}
   :label {:type [:string :derefable :component]
           :desc "The label for <Item>."}
   :icon {:type [:string :keyword]
          :desc "The icon that will be shown"}
   :icon-props {:type :map
                :desc "Properties for icons which will used as params for icon component"}
   :extra-class {:type [:string :vector]
                 :desc "Additional classes"}
   :disabled? {:type [:boolean :derefable]
               :desc "Disables the item"}
   :active? {:type [:boolean :derefable]
             :desc "Highlights the item. The active state will overwrite the disabled state."}
   :on-click {:type :function
              :desc "Function thet should be executed."}})

(def popout-definition
  {:id {:type :string
        :required true
        :desc "The item-id to identify."}
   :show? {:type [:boolean :derefable]
           :desc "Whether or not the popout is shown."}
   :content {:type :component
             :desc "What is shown in your popout"}})
(def sub-definitions {:toolbar-item toolbar-item-definition
                      :popout popout-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {:orientation :vertical
                         :tooltip-direction :right
                         :popout-position :end
                         :separator :gap})

(def item-default-parameters {:type :button})

(def ^:private toolbar-class "toolbar")
(def ^:private toolbar-options-class "toolbar-options")
(def ^:private section-class "toolbar-section")
(def ^:private horizontal-class "toolbar-horizontal")
(def ^:private divider-class "toolbar-divider")
(def ^:private active-button-class "active")
(def ^:private wrapper-class "toolbar-wrapper")
(def ^:private popout-class "toolbar-popout")
(def ^:private text-only-class ["text-bold"])

(defn- toolbar-segment [items tooltip-direction]
  (reduce
   (fn [res item]
     (let [{:keys [id active? disabled? title label on-click extra-class type icon-props] ic :icon
            :or {icon-props {}}}
           (merge item-default-parameters item)
           label (val-or-deref label)
           title (val-or-deref title)
           active? (val-or-deref active?)
           disabled? (val-or-deref disabled?)]
       (conj res
             (cond
               (= type :divider)
               [:span {:class divider-class}]
               (= type :text)
               [:span {:class text-only-class} label]
               :else
               [tooltip {:text title
                         :direction tooltip-direction}
                [:button
                 (cond-> {:aria-label title
                          :id id
                          :class []}
                   active? (update :class conj active-button-class)
                   disabled? (assoc :disabled true)
                   (and (fn? on-click) (not disabled?)) (assoc :on-click on-click)
                   (and extra-class (vector? extra-class)) (update :class concat extra-class)
                   (and extra-class (string? extra-class)) (update :class conj extra-class))
                 (when ic
                   [icon (assoc icon-props :icon ic)])
                 (when label
                   [:span.label label])]]))))
   [:<>]
   (val-or-deref items)))

(defn- toolbar-segmentation [{:keys [items tooltip-direction separator]}]
  (let [segments (if (every? vector? items)
                   (mapv #(vector toolbar-segment % tooltip-direction) items)
                   [[toolbar-segment items tooltip-direction]])]
    (if (= separator :symbol)
      (into [:div {:class section-class}] (interpose [:span {:class divider-class}] segments))
      (into [:<>] (mapv #(vector :div {:class section-class} %) segments)))))

(defn- toolbar-popout [{:keys [show? content id]}]
  (when (val-or-deref show?)
    [:div {:class popout-class
           :id id}
     [:span content]]))

(defn- toolbar-options [{:keys [on-click-toolbar-options disabled-toolbar-options? toolbar-options-tooltip]}]
  (let [disabled? (val-or-deref disabled-toolbar-options?)
        toolbar-options-tooltip (val-or-deref toolbar-options-tooltip)]
    (when (fn? on-click-toolbar-options)
      [:button (cond-> {:class toolbar-options-class}
                 disabled? (assoc disabled? true)
                 (not disabled?) (assoc :on-click on-click-toolbar-options))
       [icon (cond-> {:icon :collapse}
               (and toolbar-options-tooltip (not disabled?))
               (assoc :tooltip toolbar-options-tooltip))]])))

(defn- toolbar-wrapper [{:keys [orientation extra-class popouts popout-position offset z-index
                                extra-props id]
                         :or {extra-props {}}
                         :as params}]
  (let [style (cond-> (select-keys offset [:right :left :top :bottom])
                z-index (assoc :z-index z-index))]
    (if (empty? popouts)
      [:div
       (-> extra-props
           (assoc :class (cond-> []
                           :always
                           (conj toolbar-class)
                           (= orientation :horizontal)
                           (conj horizontal-class)
                           (vector? extra-class)
                           (concat extra-class)
                           (string? extra-class)
                           (conj extra-class)))
           (update :style (fn [o]
                            (merge (or o {})
                                   style))))
       [toolbar-segmentation params]
       [toolbar-options params]]
      [:div
       {:class (cond-> []
                 :always
                 (conj wrapper-class)
                 (vector? extra-class)
                 (concat extra-class)
                 (string? extra-class)
                 (conj extra-class))
        :style style}
       (let [wrapped-popouts (into [:<>] (mapv toolbar-popout popouts))
             wrapped-toolbar [:div
                              {:id id
                               :class (cond-> []
                                        :always
                                        (conj toolbar-class)
                                        (= orientation :horizontal)
                                        (conj horizontal-class))}
                              [toolbar-segmentation params]
                              [toolbar-options params]]]
         (if (= popout-position :start)
           [:<> wrapped-popouts wrapped-toolbar]
           [:<> wrapped-toolbar wrapped-popouts]))])))

(defn ^:export toolbar [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "toolbar" specification params)}
     [toolbar-wrapper params]]))

(def ^:export toolbar-divider {:type :divider
                               :id ""
                               :title ""})