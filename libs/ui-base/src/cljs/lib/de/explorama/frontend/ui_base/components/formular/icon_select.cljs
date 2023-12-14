(ns de.explorama.frontend.ui-base.components.formular.icon-select
  (:require [clojure.string :as str]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary
                                                                 parent-wrapper]]
            [de.explorama.frontend.ui-base.components.formular.button :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli
                                                              validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-node]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class]]
            [react-dom :as react-dom]
            [reagent.core :as reagent]
            [reagent.dom :as rdom]
            [taoensso.timbre :refer-macros [error]]))

(def parameter-definition
  {:options {:type [:vector :derefable]
             :required true
             :desc "Icon options which will be visible in a grid."}
   :value {:type [:all :derefable]
           :required true
           :desc "Icon which is selected should be the value given by the on-change-fn."}
   :aria-label {:type [:string :derefable :keyword]
                :required :aria-label
                :desc "Aria label for the select component. Should be set if the label is a component. Takes priority over the label and placeholder."}
   :on-change {:type :function
               :desc "Will be triggered when an element will be selected or deselected. Also when the clean-all is called. First parameter is always the whole current selection. When is-multi is true then it's a vector otherwise a single map with the selected option"}
   :on-blur {:type :function
             :desc "Will be triggered when user clicks outside of component"}
   :label {:type [:string :component :derefable]
           :required :aria-label
           :desc "An label for select. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :ensure-visibility? {:type :boolean
                        :desc "Will automatically open the list updwards if it would be partially outside the brwoser window."}
   :disabled? {:type :boolean
               :desc "If true, select is disabled"}})

(def option-definition
  {:icon {:type [:string :keyword]
          :required true
          :desc "Icon name to be used for the option."}
   :value {:type :all
           :desc "Value for this option which gets passed on to the on-change-fn."}
   :extra-params {:type :map
                  :desc "Same options that can be used for icons."}
   :tooltip {:type [:string :derefable]
             :desc "Tooltip for the icon option."}})

(def default-parameters {:ensure-visibility? true})

(def sub-definitions {:option option-definition})
(def specification (parameters->malli parameter-definition sub-definitions))

(def open-list-upwards-class "open-above")
(def select-wrapper-class "select-wrapper")
(def value-element-class "select-option")
(def option-list-class "select-option-list")
(def grid-class "grid")
(def grid-cols-class "grid-cols-8-fr")
(def icons-per-row 8)
(def in-list-check-classes [value-element-class
                            grid-class
                            grid-cols-class])

(defn- calc-portal-props [{:keys [related-comp offset-x offset-y min-height width z-index menu-min-width flip-atom
                                  root-comp-atom]
                           :or {offset-x 0
                                offset-y 0
                                min-height 20}}]
  (let [rect (.getBoundingClientRect related-comp)
        top (if @flip-atom
              (when root-comp-atom
                (let [root-rect (.getBoundingClientRect @root-comp-atom)]
                  (- (aget rect "y")
                     (aget root-rect "height")
                     min-height)))
              (when rect
                (+ (aget rect "y")
                   offset-y)))
        left (when rect
               (+ (aget rect "x")
                  offset-x))]
    {:position :absolute
     :left left
     :top top
     :min-width menu-min-width
     :width (or width (when rect (aget rect "width")))
     :min-height min-height
     :z-index z-index}))

(defn- get-cursor [raw-state p]
  (reagent/cursor raw-state (if (vector? p)
                              p
                              [p])))

(defn- close-menu-state [raw-state]
  (swap! raw-state assoc
         :in-list? false
         :list-open? false
         :is-focused? false
         :select-idx 0
         :select-current? false))

(defn- add-close-on-select [{:keys [on-change] :as props} raw-state]
  (assoc props :on-change (fn [selections]
                            (close-menu-state raw-state)
                            (when on-change
                              (on-change selections)))))

(defn- add-is-single [{:keys [on-change] :as props}]
  (assoc props :on-change (fn [selection]
                            (when on-change (on-change selection)))))

(defn- apply-default-props [props raw-state]
  (let [{:keys [options value] :as props}
        (merge default-parameters props)]
    (cond-> props
      (or  (nil? options)
           (vector? options))
      (assoc :options (atom options))
      (or (vector? value)
          (map? value))
      (assoc :value (atom value))
      (nil? value)
      (assoc :value (atom {}))
      :always (add-close-on-select raw-state)
      :always (add-is-single))))

(defn- handle-click [e raw-state]
  (let [{:keys [root-comp list-open? in-list?]} @raw-state]
    (when (and (not in-list?)
               list-open?
               (not (.contains root-comp (aget e "target"))))
      (close-menu-state raw-state))))

(defn- observe-timer [exec-fn abbort-fn timeout]
  (js/window.setTimeout
   #(do
      (when-not (and abbort-fn (abbort-fn))
        (exec-fn)
        (observe-timer exec-fn abbort-fn timeout)))
   timeout))

(defn- portal-comp [props _]
  (let [ref (atom nil)
        styles (reagent/atom (calc-portal-props props))
        observe? (atom nil)
        check-interval 10
        exec-fn #(let [nstyles (calc-portal-props props)]
                   (when (and @ref (not= @styles nstyles))
                     (reset! styles nstyles)))]
    (reagent/create-class
     {:display-name "icon-select portal"
      :component-did-mount (fn [this]
                             (reset! ref this)
                             (reset! observe? true)
                             (observe-timer exec-fn
                                            #(not (true? @observe?))
                                            check-interval))
      :component-will-unmount #(reset! observe? false)
      :reagent-render
      (fn [{:keys [update-sub]} childs]
        (let [_ (when update-sub (val-or-deref update-sub))
              cstyles @styles]
          [:div {:style (or cstyles
                            {})}
           childs]))})))

(defn- portal
  "related-comp is the dom-element, where the portal should be placed"
  [{:keys [related-comp] :as props} childs]
  (when (and related-comp (instance? js/Element related-comp))
    (react-dom/createPortal
     (reagent/as-element
      [portal-comp props childs])
     js/document.body)))

(defn- find-option [searched-for options]
  (let [current-value (val-or-deref searched-for)]
    (first (filter (fn [{icon-name :icon
                         icon-val :value}]
                     (or (= icon-val current-value)
                         (= icon-name current-value)))
                   (val-or-deref options)))))

(defn- flip-menu?
  ([top menu-height]
   (let [{^number portal-height :height}
         (bounding-rect-node js/document.body)]
     (< portal-height (+ top menu-height)))))

(defn- list-menu-parent [{:keys [on-change disabled? value options ensure-visibility?]}
                         parent-comp flip-state]
  (let [current-value (val-or-deref value)
        options (val-or-deref options)
        menu-height (* (-> (count options)
                           (/ icons-per-row)
                           inc
                           int)
                       34) ;;TODO r1/css approximate row height
        flip? (if (and parent-comp ensure-visibility?)
                (flip-menu? (aget (.getBoundingClientRect @parent-comp) "y") menu-height)
                false)]
    (reset! flip-state flip?)
    (reduce (fn [acc {icon-name :icon
                      icon-tooltip :tooltip
                      icon-val :value
                      icon-extra-params :extra-params}]
              (conj acc
                    [:div {:class [value-element-class
                                   (when (or (= icon-val current-value)
                                             (= icon-name current-value))
                                     "selected")]
                           :on-click #(do (.stopPropagation %)
                                          (when (and on-change (not disabled?))
                                            (on-change (or icon-val icon-name))))}
                     [icon (cond-> (or icon-extra-params {})
                             :always (assoc :icon icon-name)
                             icon-tooltip (assoc :tooltip icon-tooltip))]]))
            [:div {:class (cond-> [toolbar-ignore-class option-list-class grid-class grid-cols-class]
                            flip?
                            (conj open-list-upwards-class))}]
            options)))

(defn- options-parent [{:keys [value menu-z-index menu-min-width]}
                       raw-state _]
  (let [parent-comp (reagent/atom nil)
        flip? (reagent/atom nil)
        click-handler (fn [e] (handle-click e raw-state))
        wheel-handler (fn [e] (handle-click e raw-state))]
    (reagent/create-class
     {:display-name "select options-parent"
      :component-did-mount #(do
                              (reset! parent-comp (rdom/dom-node %))
                              (js/document.addEventListener "wheel" wheel-handler true)
                              (js/document.addEventListener "mousedown" click-handler true))
      :component-will-unmount #(do
                                 (js/document.removeEventListener "wheel" wheel-handler true)
                                 (js/document.removeEventListener "mousedown" click-handler true))
      :reagent-render
      (fn [props raw-state {:keys [root-comp]}]
        [:div {:class "select__dropdown"
               :on-mouse-enter #(swap! raw-state assoc :in-list? true)
               :on-mouse-up #(let [event (aget % "nativeEvent")
                                   x (aget event "pageX")
                                   y (aget event "pageY")]
                             ;Workaround when clicking the last element of list, which reduces the menu size
                             ;then on-mouse leave is not triggered, although the mouse is no longer in menu
                             ;this causen an bug for closing menu and firing on-blur
                               (js/setTimeout
                                (fn []
                                ;checks if mouseposition is still in menu
                                  (let [elem (js/document.elementFromPoint x y)
                                        classes? (set (str/split (or (when elem
                                                                       (aget elem "className"))
                                                                     "")
                                                                 #" "))]
                                    (when (and elem (not (some classes? in-list-check-classes)))
                                      (swap! raw-state assoc :in-list? false))))
                                100))
               :on-mouse-leave #(swap! raw-state assoc :in-list? false)}
         [portal (cond-> {:related-comp @parent-comp
                          :update-sub value
                          :root-comp-atom root-comp
                          :flip-atom flip?}
                   menu-min-width (assoc :menu-min-width menu-min-width)
                   menu-z-index (assoc :z-index menu-z-index))
          [list-menu-parent props parent-comp flip?]]])})))

(defn- icon-extra-params [value options]
  (let [{:keys [tooltip extra-params]} (find-option value options)]
    (cond
      (and tooltip extra-params) (assoc extra-params :tooltip tooltip)
      extra-params extra-params
      :else {})))

(defn- current-selected-icon [value options]
  (let [found-option (find-option value options)]
    (:icon found-option)))

(defn- icon-select-parentless [_]
  (let [raw-state (reagent/atom {:root-comp false
                                 :list-open? false
                                 :in-list? false
                                 :is-focused? false
                                 :last-key nil
                                 :select-current? false
                                 :select-idx 0})
        acc-state {:root-comp (get-cursor raw-state :root-comp)
                   :list-open? (get-cursor raw-state :list-open?)
                   :last-key (get-cursor raw-state :last-key)
                   :in-list? (get-cursor raw-state :in-list?)
                   :is-focused? (get-cursor raw-state :is-focused?)
                   :select-current? (get-cursor raw-state :select-current?)
                   :select-idx (get-cursor raw-state :select-idx)}]
    (reagent/create-class
     {:display-name "icon-select"
      :component-did-catch (fn [_ e info]
                             (error "Select component crashed: " e info))
      :component-did-update (fn [])
      :reagent-render (fn [props]
                        (let [{:keys [disabled? aria-label value options] :as props}
                              (apply-default-props props raw-state)
                              {:keys [list-open?]} acc-state]
                          [error-boundary {:validate-fn #(validate "icon-select" specification props)}
                           [:div {:ref #(swap! raw-state assoc :root-comp %)
                                  :class [select-wrapper-class]}
                            [button (cond-> {:on-click #(swap! (get acc-state :list-open?) not)
                                             :variant :secondary
                                             :aria-label aria-label
                                             :start-icon (current-selected-icon value options)
                                             :icon-params (icon-extra-params value options)}
                                      (not (nil? disabled?)) (assoc :disabled? disabled?))]
                            (when (and @list-open? (not disabled?))
                              [options-parent props raw-state acc-state])]]))})))

(defn ^:export icon-select [props]
  [error-boundary
   [parent-wrapper props
    [icon-select-parentless props]]])

