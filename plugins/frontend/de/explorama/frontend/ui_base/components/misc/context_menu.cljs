(ns de.explorama.frontend.ui-base.components.misc.context-menu
  (:require [reagent.core :as reagent]
            [react-dom :as react-dom]
            [reagent.dom :as rdom]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-node]]
            [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class]]
            [de.explorama.frontend.ui-base.utils.timeout :as timeout]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(def parameter-definition
  {:items {:type [:vector :derefable]
           :definition :list-entry
           :desc "Items which will be displayed. Empty items will be ignored. Data structure is [<Item> <Item>]. See Item for details"}
   :ensure-visibility? {:type :boolean
                        :desc "Recalculats position if context-menu is out of browser window."}
   :on-close {:type :function
              :desc "Will be triggered on click of context-menü close button"}
   :close-on-select? {:type :boolean
                      :desc "If true, click on item will close the menu automatically"}
   :openable? {:type :boolean
               :desc "If true, component can be open"}
   :show? {:type [:boolean :derefable]
           :required true
           :desc "If true, menu will open (except :openable is false)"}
   :position {:type [:map :derefable]
              :required true
              :desc "Fixed position as map {:left <number> :top <number>} of menu"}
   :child-x-offset {:type :number
                    :desc "Defines the horizontal offset of the child menu compared to the parent"}
   :child-y-offset {:type :number
                    :desc "Defines the vertical offset of child menus compared to the entry they belong to"}
   :show-delay {:type :number
                :desc "Delay to show child elements"}
   :menu-x-offset {:type :number
                   :desc "x-offset to control where the mouse is when menu opens"}
   :menu-y-offset {:type :number
                   :desc "y-offset to control where the mouse is when menu opens"}
   :menu-z-index {:type :number
                  :desc "z-index of context-menu"}
   :menu-max-height {:type :number
                     :desc "max height for the menu in pixel."}
   :extra-class {:type [:string :vector]
                 :desc "Extra classes for context-menu parent"}})
(def list-entry-definition
  {:label {:type [:derefable :string :component]
           :required true
           :require-cond [:type :entry]
           :desc "The label for entry"}
   :type {:type :keyword
          :characteristics [:entry :group]
          :desc ":entry will render the menu entry. :group will ignore everything except sub-items and draw dividers"}
   :value {:type :all
           :desc "Will be the second parameter for on-click function"}
   :icon {:type [:keyword :string]
          :desc "A item icon. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :icon-props {:type :map
                :desc "Properties for icons which will used as params for icon component"}
   :is-custom-content? {:type :boolean
                        :desc "If true, than specific context-menu classes will be ignored on leaf item"}
   :on-click {:type :function
              :required :function
              :desc "Will be triggered, if user clicks on the item. Provide [event label value] as parameters. Ignored if there are sub-items. Return false to prevent hiding of menu"}
   :sub-items {:type :vector
               :required :function
               :definition :list-entry
               :desc "A vector of items [<Item> <Item>], which will be in a submenu."}
   :disabled? {:type [:derefable :boolean]
               :desc "If true the item will be visually disabled, on-click action will be ignored and any sub-items will not be shown. Default value: false"}})
(def sub-definitions {:list-entry list-entry-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {:show-delay 500
                         :menu-x-offset 15
                         :menu-y-offset 5
                         :child-x-offset -8
                         :child-y-offset 8
                         :openable? true
                         :menu-z-index 50000
                         :menu-max-height 500
                         :close-on-select? true
                         :ensure-visibility? true})

(def context-menu-class "context-menu")
(def context-menu-group-class "context-menu-group")
(def context-menu-entry-class "context-menu-entry")
(def disabled-class "disabled")
(def icon-expand-extra-class "expand")

;;; ======= Util Functions =======

(defn- portal-comp [props & childs]
  (reagent/create-class
   {:display-name "context-menu portal"
    :reagent-render
    (fn [{:keys [left top menu-z-index]} & childs]
      (apply conj
             [:div {:style {:position :absolute
                            :left left
                            :top top
                            :z-index menu-z-index}}]
             childs))}))

(defn- portal [props childs]
  (react-dom/createPortal
   (reagent/as-element
    [portal-comp props childs])
   js/document.body))

(defn- handle-click [in-menu? menu-comps hide-fn e]
  (when (and (not @in-menu?)
             (not (some #(.contains % (aget e "target")) (vals @menu-comps))))
    (hide-fn)))

(defn- portal-target-rect [portal-target]
  (when-let [portal-target-elem (if-let [ele (.getElementById js/document portal-target)]
                                  ele
                                  js/document.body)]
    (bounding-rect-node portal-target-elem)))

(defn- ensure-visibility [{:keys [^boolean portal-target]} {^number init-left :left ^number init-top :top} menu-rect]
  (let [{:keys [^number width ^number height] ^number rect-left :left ^number rect-top :top} menu-rect
        {^number portal-width :width ^number portal-height :height} (portal-target-rect portal-target)
        ^number oversize-y (- portal-height (+ rect-top height))
        ^number oversize-x (- portal-width (+ rect-left width))
        ^number top (if (> 0 oversize-y)
                      (+ rect-top oversize-y)
                      rect-top)
        ^number left (if (> 0 oversize-x)
                       (+ rect-left oversize-x)
                       rect-left)
        ^boolean initialize? (and (nil? rect-left) (nil? rect-top))]
    {:left (if initialize? init-left left)
     :top (if initialize? init-top top)}))

;;; ======= Context Menu =======

(defn- entry [{:keys [label type sub-items disabled? on-click value icon-props is-custom-content?] ico :icon
               :or {icon-props {}}
               :as item}
              active-parents
              parent-rects
              level
              timeout-fn
              hide-fn]
  (if (= type :group)
    ;group
    (reduce
     #(conj %1 [entry %2 active-parents parent-rects level timeout-fn hide-fn])
     [:div {:class context-menu-group-class}]
     sub-items)
     ;single
    (let [label (val-or-deref label)
          id (str "context-menu-entry-" level "-" label)
          disabled? (val-or-deref disabled?)
          show-child? (and (not-empty (filter #(and % (not-empty %)) sub-items))
                           (not disabled?))]
      [:div {:class (cond-> [toolbar-ignore-class]
                      (not is-custom-content?) (conj context-menu-entry-class)
                      disabled? (conj disabled-class))
             :id id
             :on-mouse-enter (fn [_]
                               (timeout-fn
                                (fn [_]
                                  (when (not= item (get @active-parents level))
                                    (swap! active-parents #(cond-> %
                                                             :always
                                                             (select-keys (range level))
                                                             show-child?
                                                             (assoc level item)))
                                    (swap! parent-rects #(cond-> %
                                                           :always
                                                           (select-keys (range level))
                                                           show-child?
                                                           (assoc level (bounding-rect-node (.getElementById js/document id)))))))))
             :on-click #(when (and on-click (not disabled?))
                          (let [r (if value
                                    (on-click % label value)
                                    (on-click % label))]
                            (when-not (false? r)
                              (hide-fn))))}
       (when ico
         [icon (assoc icon-props
                      :icon ico)])
       label
       (when sub-items
         [icon {:icon :expand-list
                :color :gray
                :extra-class icon-expand-extra-class}])])))

(defn- entry-list [state _ level _ _]
  (let [{:keys [in-menu? menu-comps menu-rects active-parents parent-rects timeout]} state
        old-items (atom nil)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [node (rdom/dom-node this)]
          (swap! menu-comps assoc level node)))
      :component-did-update
      (fn [this]
        (let [node (rdom/dom-node this)]
          (swap! menu-rects assoc level (bounding-rect-node node))))
      :reagent-render
      (fn [state params level hide-fn]
        (let [{:keys [items menu-max-height menu-z-index child-x-offset child-y-offset show-delay ensure-visibility? extra-class]} params
              active-item (get @active-parents level)
              timeout-fn (partial timeout/handle-timeout timeout show-delay)
              first? (= level 0)
              {parent-width :width parent-top :top parent-left :left} (get @parent-rects (dec level))
              initial-pos {:top (if first? parent-top (+ parent-top child-y-offset))
                           :left (if first? parent-left (+ parent-left parent-width child-x-offset))}
              corrected-pos (if (and ensure-visibility?
                                     (= items @old-items))
                              (ensure-visibility params initial-pos (get @menu-rects level))
                              (do
                                (reset! old-items items)
                                initial-pos))]
          [:div (cond-> {:class [toolbar-ignore-class context-menu-class]
                         :style (merge {:z-index (+ menu-z-index level)
                                        :position :fixed
                                        :max-height menu-max-height}
                                       corrected-pos)}
                  first? (assoc :on-mouse-enter #(reset! in-menu? true)
                                :on-mouse-leave #(reset! in-menu? false))
                  extra-class (update :class (fn [o]
                                               (if (vector? extra-class)
                                                 (apply conj o extra-class)
                                                 (conj o extra-class)))))
           (when active-item
             (let [child-params (assoc params :items (get active-item :sub-items))]
               [entry-list state child-params (inc level) hide-fn]))
           (when items
             (reduce
              (fn [res item-desc]
                (if (and item-desc (not-empty item-desc))
                  (conj res [entry item-desc active-parents parent-rects level timeout-fn hide-fn])
                  res))
              [:<>]
              items))]))})))

(defn- menu [{:keys [on-close]}]
  (let [state {:timeout (atom nil)
               :in-menu? (atom nil)
               :menu-comps (atom {})
               :menu-rects (reagent/atom {})
               :active-parents (reagent/atom {})
               :parent-rects (reagent/atom {})}
        start-level 0
        hide-fn #(when on-close (on-close %))
        {:keys [in-menu? menu-comps parent-rects]} state
        check-hide-click (partial handle-click in-menu? menu-comps hide-fn)]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [node (rdom/dom-node this)]
          (swap! parent-rects assoc -1 (bounding-rect-node node))
          (js/document.addEventListener "mousedown" check-hide-click true)))
      :component-will-unmount
      #(js/document.removeEventListener "mousedown" check-hide-click true)
      :reagent-render
      (fn [params]
        (let [{:keys [close-on-select?]} params
              hide-fn #(when close-on-select? (hide-fn %))]
          [portal params
           [entry-list state params start-level hide-fn]]))})))

;;; ======= Exported Funcitons =======

(defn ^:export context-menu [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "context-menu" specification params)}
     (let [{:keys [openable? show? position]} params
           openable? (val-or-deref openable?)
           {:keys [left top]} (val-or-deref position)
           show? (val-or-deref show?)]
       [:<>
        (when (and show?
                   openable?
                   (and left top))
          [menu (assoc params
                       :left left
                       :top top)])])]))

(defn ^:export calc-menu-position
  ([mouse-event params]
   (let [{:keys [menu-x-offset menu-y-offset portal-target]} (merge default-parameters params)
         portal-target-elem (if-let [ele (.getElementById js/document portal-target)]
                              ele
                              js/document.body)
         rect (.getBoundingClientRect portal-target-elem)
         portal-left (if rect (aget rect "left") 0)
         portal-top (if rect (aget rect "top") 0)
         scroll-left (max 0 (aget portal-target-elem "scrollLeft"))
         scroll-top  (max 0 (aget portal-target-elem "scrollTop"))
         x (-> (if mouse-event (aget mouse-event "pageX") 0)
               (- portal-left)
               (+ scroll-left))
         y (-> (if mouse-event (aget mouse-event "pageY") 0)
               (- portal-top)
               (+ scroll-top))]
     {:top (- y (or menu-y-offset 0))
      :left (- x (or menu-x-offset 0))}))
  ([mouse-event]
   (calc-menu-position mouse-event {})))