(ns de.explorama.frontend.ui-base.components.formular.select
  (:require
   [clojure.core.reducers :as r]
   [clojure.string :as str]
   ["react-virtualized" :refer [AutoSizer List]]
   [react-dom :as react-dom]
   [reagent.core :as reagent]
   [reagent.dom :as rdom]
   [de.explorama.frontend.ui-base.components.misc.icon :refer [icon]]
   [de.explorama.frontend.ui-base.utils.interop :refer [format]]
   [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-node]]
   [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
   [de.explorama.frontend.ui-base.components.common.core :refer [parent-wrapper error-boundary]]
   [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class export-ignore-class]]
   [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref translate-label]]
   [taoensso.timbre :refer-macros [error]]))

(def parameter-definition
  {:options {:type [:vector :derefable]
             :required true
             :desc "Options which will be visible as list when clicking on the component. Data structure is [<Option> <Option>] which can be provided directly as a vector or as an derefable like an atom or re-frame subscription"}
   :values {:type [:map :vector :derefable]
            :required true
            :desc "Values which are selected. On single select it's an map which is a <Option>, on multi select it's an vector of options [<Option> <Option>]. Data structure can be provided directly as a vector/map or as an derefable like an atom or re-frame subscription"}
   :aria-label {:type [:string :derefable :keyword]
                :required :aria-label
                :desc "Aria label for the select component. Should be set if the label is a component. Takes priority over the label and placeholder."}
   :clear-aria-label {:type [:string :derefable :keyword]
                      :desc "Aria label for the clear button"}
   :remove-aria-label {:type [:string :derefable :keyword]
                       :desc "Aria label for the remove button on multi select items"}
   :open-aria-label {:type [:string :derefable :keyword]
                     :desc "Aria label for the drop down arrow"}
   :search-aria-label {:type [:string :derefable :keyword]
                       :desc "Aria label for the search input field"}
   :value-render-fn {:type :function
                     :default-fn-str "(fn [selection] (get selection label-key)"
                     :desc "Render component for selected values to provide a different selection than in menu"}
   :on-change {:type :function
               :desc "Will be triggered when an element will be selected or deselected. Also when the clean-all is called. First parameter is always the whole current selection. When is-multi is true then it's a vector otherwise a single map with the selected option"}
   :on-blur {:type :function
             :desc "Will be triggered when user clicks outside of component"}
   :label {:type [:string :component :derefable]
           :required :aria-label
           :desc "An label for select. Uses label from de.explorama.frontend.ui-base.components.common.label"}
   :label-params {:type :map
                  :desc "Parameters for label component"}
   :hint {:type [:derefable :string]
          :desc "An optional hint. It will be displayed as info bubble with mouse-over."}
   :caption {:type [:string :component :derefable]
             :desc "Will desiplay a caption beneth the select element."}
   :start-icon {:type [:string :keyword]
                :desc "An icon which will be placed before the selection. Its recommanded to use a keyword. If its a string it has to be an css class, if its a keyword the css-class will get from icon-collection. See icon-collection to which are provided"}
   :label-key {:type :keyword
               :desc "The keyword in Option-Map which represents the label. The label will be displayed"}
   :value-key {:type :keyword
               :desc "The keyword in Option-Map which represents the value. The value represents its element"}
   :tooltip-key {:type :keyword
                 :desc "The keyword in Option-Map which represents the tooltip string"}
   :filter-key {:type :keyword
                :desc "The keyword in Option-Map by which the typeahead will filter"}
   :check-key {:type :keyword
               :desc "The keyword in Option-Map by which the invalid items will be checked when mark-invalid or remove-invalid is true"}
   :menu-z-index {:type :number
                  :desc "Z-Index of menu portal component"}
   :menu-height  {:type :number
                  :desc "Height of the menu/list in pixels"}
   :menu-min-width {:type :number
                    :desc "Minimal width of menu/list list in pixels"}
   :menu-row-height {:type :number
                     :desc "Height of a row in menu/list in pixels"}
   :char-width {:type :number
                :desc "Width of a single character in typeahead input. Is needed for calculation of input width"}
   :overscan-row-count {:type :number
                        :desc "Number of preloaded rows in menu/list. A high number may results in performance issues, a low number may results in lagging when scrolling"}
   :autofocus? {:type :boolean
                :desc "Flag for autofocusing input. Only works, when select is not disabled"}
   :is-searchable? {:type :boolean
                    :desc "If true, typeahead is available"}
   :logging? {:type :boolean
              :desc "If true then warnings will be printed when required parameters are not set"}
   :is-multi? {:type :boolean
               :desc "If true, multiple values can be selected"}
   :visible-selections {:type :integer
                        :desc "Max number of visible selections, the remaining ones will be hidden behind a '+ x' button."}
   :select-on-tab {:type :boolean
                   :desc "If true, focused options can be select with the tab key"}
   :show-more {:type :string
               :desc "Message for hidden multi-select selections. Should contain a '%d', which will be replaced with the count."}
   :is-grouped? {:type :boolean
                 :desc "If true, groups can be defined as options. Options structure is: [{:label <group-label :options [<Option> <Option>]} ..]"}
   :keep-selected? {:type :boolean
                    :desc "By default, selected elements disappear from the drop down. This option will keep them, highlights them and allows you to de-select them from the list."}
   :group-selectable? {:type :boolean
                       :desc "If true and :is-grouped or :show-all-group is true, groups are visible in menu/list, when :is-multi is true they also are selectable"}
   :group-value-key {:type :keyword
                     :desc "The key where the unique group value is set. This needs to set in group-definition and in each group-option. Only used for mark-invalid?/remove-invalid?."}
   :show-all-group? {:type :boolean
                     :desc "If true, displays an group \"All\" in menu, when no groups are defined"}
   :all-group-label {:type :string
                     :desc "Label of \"All\" group"}
   :disabled? {:type :boolean
               :desc "If true, select is disabled"}
   :ignore-case? {:type :boolean
                  :desc "If true, typeahead filter will ignore capitalization"}
   :close-on-select? {:type :boolean
                      :desc "If true, menu/list will be closed when selecting an element. Defaults to true on single select and to false on multi select."}
   :show-options-tooltip? {:type :boolean
                           :desc "If true, an tooltip on each option in menu/list with the label will be visible when hovering over it"}
   :option-show-title? {:type :boolean
                        :desc "If true, an tooltip on each selected option with label will be visible when hovering over it"}
   :placeholder {:type [:string :keyword]
                 :required :aria-label
                 :desc "Placeholder of typeahead, when selection is empty"}
   :ensure-visibility? {:type :boolean
                        :desc "Will automatically open the list updwards if it would be partially outside the brwoser window."}
   :select-key {:type :integer
                :desc "Keycode for selecting current option with keyboard. For example 13 is Enter-Keycode"}
   :no-options-placeholder {:type :string
                            :desc "Placeholder of menu/list when no options are available. For example options is [ ] or all options are selected"}
   :show-clean-all? {:type :boolean
                     :desc "If true, an x will be visible to delete-all selected entries"}
   :clear-all-tooltip {:type :string
                       :desc "Tooltip which are visible on hovering over clean-all x"}
   :is-clearable? {:type :boolean
                   :desc "If true, the selections can be deleted. When it's false also clean-all x is hidden"}
   :remove-invalid? {:type :boolean
                     :desc "Checks every selected entry if there are in options list. When some is not, it will be hidden"}
   :mark-invalid? {:type :boolean
                   :desc "Checks every selected entry if there are in options list. When some is not, it will be marked"}
   :disabled-class {:type :string
                    :desc "Can be set to use a custom class on disabled elements"}
   :invalid-class {:type :string
                   :desc "Can be set to use a custom class on invalid selections"}
   :invalid-hint {:type :string
                  :desc "Hint that is visible on hovering over an invalid entry"}
   :extra-class {:type :string
                 :desc "Classname which will be added to select root. For example adding a class for managing the with of select"}})
(def option-definition
  {:label {:type [:string :component]
           :required true
           :desc "The label for <Option> Can be changed by :label-key of select component. As default it is the content by which options will be filtered by typeahead input. Can be changed by :filter-key of select component"}
   :value {:type [:string :number :keyword]
           :required true
           :desc "The value for <Option>, which represents the options. For example: internal representation is :sort-op for triggering a sorting operation, but Sort By should be visible for the user. Can be changed by :value-key of select component"}
   :fixed? {:type :boolean
            :desc "If true, <Option> is not removable (only for :is-multi? true when selected)"}
   :options {:type :vector
             :desc "Only used when :is-grouped? is true. Represents options of an group. Data structure is like :options [<Option> <Option>]"}
   :disabled? {:type :boolean
               :desc "[Only menu] If true, <Option> is disabled"}
   :disabled-hint {:type :string
                   :desc "[Only menu] Tooltip when option :disabled? true"}})
(def sub-definitions {:option option-definition})
(def specification (parameters->malli parameter-definition sub-definitions))
(def default-parameters {:label-key :label
                         :value-key :value
                         :tooltip-key :label
                         :filter-key :label
                         :check-key :value
                         :menu-z-index 50000
                         :menu-height 250
                         :menu-min-width 70
                         :menu-row-height 30
                         :overscan-row-count 2
                         :is-searchable? true
                         :keep-selected? false
                         :visible-selections 3
                         :ensure-visibility? true
                         :close-on-select? true
                         :show-clean-all? true
                         :is-clearable? true
                         :clear-aria-label :aria-select-clear
                         :remove-aria-label :aria-select-remove
                         :open-aria-label :aria-select-open
                         :search-aria-label :aria-select-search
                         :clear-all-tooltip "Clear all"
                         :show-options-tooltip? true
                         :option-show-title? true
                         :ignore-case? true
                         :remove-invalid? false ;May cost performance
                         :mark-invalid? false ;May cost performance
                         :char-width 10
                         :autofocus? false
                         :placeholder :select-placeholder
                         :show-more "+%d"
                         :select-key 13 ;enter
                         :select-on-tab false
                         :no-options-placeholder "No Options"
                         :disabled-class "disabled"
                         :invalid-class "invalid"
                         :invalid-hint "This Option is invalid"
                         :is-multi? false
                         :logging? true
                         :disabled? false
                         ;----- groups -----
                         :is-grouped? false
                         :group-selectable? true
                         :show-all-group? false ;shows an all group, when no groups are defined
                         :all-group-label "All"})

(def default-parameters-multi {:close-on-select? false})

(def virt-autosizer (reagent/adapt-react-class AutoSizer))
(def virt-list (reagent/adapt-react-class List))

(def tab-keycode 9)
(def esc-keycode 27)
(def backspace-keycode 8)
(def up-arrow-keycode 38)
(def down-arrow-keycode 40)
(def menu-border-size 2)

(def caption-class "input-hint")
(def basic-select-class "select-input")
(def single-select-class "single-select")
(def multi-select-class "multi-select")
(def select-button-group-class "select-buttons")
(def select-button-class "btn-select")
(def clear-button-class "btn-clear")
(def open-list-upwards-class "open-above")
(def option-list-class "select-option-list")
(def drop-down-value-selected-class "active")
(def truncate-text-class "text-truncate")
(def group-element-class "select-option-group")
(def group-count-class "select-option-count")
(def value-element-class "select-option")
(def multi-select-holder-class "multi-select-values")
(def multi-select-value-base-class "value")
(def multi-select-group-value-class "value-group")
(def multi-select-value-class "value-label")
(def selected-class "selected")
(def selectable-group-class "selectable")
(def show-all-selections-class "expanded")
(def select-wrapper-class "select-wrapper")
(def inactive-class "disabled")

(def react-virt-grid-class "ReactVirtualized__Grid")
(def react-virt-list-class "ReactVirtualized__List")
(def react-virt-innerscroll-class "ReactVirtualized__Grid__innerScrollContainer")
(def in-list-check-classes [value-element-class
                            group-element-class
                            group-count-class
                            truncate-text-class
                            react-virt-grid-class
                            react-virt-list-class
                            react-virt-innerscroll-class])

; -------------------- Extra Component to position options list absolute ----------------------------
; It's only here to maintain the select component easier, later it should be an extra file

(defn- calc-portal-props [{:keys [related-comp offset-x offset-y min-height width z-index menu-min-width
                                  root-comp-atom flip-atom]
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

(defn- observe-timer [exec-fn abbort-fn timeout]
  (js/window.setTimeout
   #(do
      (when-not (and abbort-fn (abbort-fn))
        (exec-fn)
        (observe-timer exec-fn abbort-fn timeout)))
   timeout))

(defn- portal-comp [props childs]
  (let [ref (atom nil)
        styles (reagent/atom (calc-portal-props props))
        observe? (atom nil)
        check-interval 10
        exec-fn #(let [nstyles (calc-portal-props props)]
                   (when (and @ref (not= @styles nstyles))
                     (reset! styles nstyles)))]
    (reagent/create-class
     {:display-name "select portal"
      :component-did-mount (fn [this]
                             (reset! ref this)
                             (reset! observe? true)
                             (observe-timer exec-fn
                                            #(not (true? @observe?))
                                            check-interval))
      :component-will-unmount #(reset! observe? false)
      :reagent-render
      (fn [{:keys [update-sub]} childs]
        (let [_ (when update-sub @update-sub)
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

; ------------------------ State Cursors -------------------------------

(defn- get-cursor [raw-state p]
  (reagent/cursor raw-state (if (vector? p)
                              p
                              [p])))

; ------------------------ Utils to edit options and selection ------------------------- 
(defn- delete-element [old-vec element use-index?]
  (if (or (nil? element)
          (empty? element))
    old-vec
    (let [idx (get element :i)
          i (if (and idx use-index?)
              idx
              0)
          i (.indexOf old-vec (dissoc element :i :type) i) ;starts at i
          is-in-back-area? (> 1.1 (/ (count old-vec)
                                     (max i 1)))]
      (cond
        (or (empty? old-vec)
            (empty? element)
            (= i -1))
        old-vec
        ;Into more performant with small sized vec as second second parameter
        is-in-back-area?
        (into (subvec old-vec 0 i)
              (subvec old-vec (inc i)))
        ;r/flatten more performant with high or medium sized vec as second parameter
        :else
        (vec (r/flatten [(subvec old-vec 0 i)
                         (subvec old-vec (inc i))]))))))

(defn- delete-multi [old-vec elements is-reduced-vec? use-index?]
  (cond
    (empty? elements)
    old-vec
    ;indeces stimmen nicht mehr -> performanter mit remove
    is-reduced-vec?
    (vec (r/remove (set elements) old-vec))
    ;indeces vorhanden -> delete-element = performant
    :else
    (reduce (fn [v sel]
              (delete-element v sel use-index?))
            old-vec
            elements)))

(defn- add-element
  ([old-vec element] (add-element old-vec element nil))
  ([old-vec element sortby]
   (cond
     (empty? element) old-vec
     sortby (vec (sort-by sortby (conj old-vec
                                       element)))
     :else (vec (conj old-vec
                      element)))))

(defn- close-menu-state [raw-state]
  (swap! raw-state assoc
         :in-list? false
         :list-open? false
         :is-focused? false
         :select-idx 0
         :select-current? false))

; -----------------------------------------------------------------------------------
(defn- set-input-width
  ([props acc-state]
   (set-input-width props acc-state  (or @(:filterval acc-state)
                                         "")))
  ([{:keys [char-width]} acc-state val]
   (when-let [input-comp @(:input-comp acc-state)]
     (if (empty? (or val ""))
       (aset input-comp
             "style"
             (clj->js {}))
       (aset input-comp
             "style"
             "width"
             (str (max 0
                       (* char-width (+ 2 (count val))))
                  "px"))))))

(defn- add-close-on-select [{:keys [on-change] :as props} raw-state]
  (assoc props :on-change (fn [selections]
                            (close-menu-state raw-state)
                            (when on-change
                              (on-change selections)))))

(defn- unselect-all
  ([is-multi? on-change]
   (unselect-all is-multi? on-change nil))
  ([is-multi? on-change selected]
   (on-change (cond
                (and (vector? selected)
                     (not-empty selected))
                selected
                is-multi?
                []
                :else nil))))

(defn- unselect-last [{:keys [is-multi? on-change values]}]
  (let [vals @values]
    (cond (and is-multi?
               (not-empty vals)
               (not (get (peek vals) :fixed?)))
          (on-change (pop vals))
          (and (not is-multi?)
               (not-empty vals))
          (on-change nil))))

(defn- clear-input [props raw-state acc-state focus?]
  (swap! raw-state assoc :filterval "")
  (when-let [input-comp @(:input-comp acc-state)]
    (aset input-comp "value" "")
    (when focus?
      (.focus input-comp)))
  (set-input-width props acc-state))

(defn- clear-input-on-blur [{:keys [on-blur] :as props} raw-state acc-state raw-state]
  (assoc props :on-blur (fn []
                          (close-menu-state raw-state)
                          (clear-input props raw-state acc-state false)
                          (when on-blur (on-blur)))))

(defn- clear-input-on-change [{:keys [on-change] :as props} raw-state acc-state]
  (assoc props :on-change (fn [selections]
                            (clear-input props raw-state acc-state true)
                            (when on-change (on-change selections)))))

(defn- add-is-single [{:keys [on-change] :as props}]
  (assoc props :on-change (fn [selection]
                            (when on-change (on-change selection)))))

(defn- apply-default-props [{:keys [is-multi?] :as props} raw-state acc-state]
  (let [{:keys [close-on-select? is-multi? options values] :as props}
        (cond-> default-parameters
          is-multi? (merge default-parameters-multi)
          :always (merge props))]
    (cond-> props
      (or  (nil? options)
           (vector? options))
      (assoc :options (atom options))
      (or (vector? values)
          (map? values))
      (assoc :values (atom values))
      (nil? values)
      (assoc :values (atom (if is-multi? [] {})))
      close-on-select? (add-close-on-select raw-state)
      (not is-multi?) (add-is-single)
      :always (clear-input-on-blur raw-state acc-state raw-state)
      :always (clear-input-on-change raw-state acc-state))))

; ------------------------ Typeahead Filter  -------------------------

(defn- filter-input [{:keys [filter-key ignore-case?]} data input]
  (let [input (cond-> input
                (not (string? input)) (str)
                ignore-case? (str/lower-case))
        filterfunc (if ignore-case?
                     (fn [v] (str/includes? (str/lower-case (str v))
                                            input))
                     (fn [v] (str/includes? (str v)
                                            input)))]

    (filterv (fn [row]
               (filterfunc (get row filter-key "")))
             data)))

(defn- filter-options [{:keys [is-multi? keep-selected?] :as props} options selections input use-index?]
  (cond
    keep-selected?
    options
    (and (not is-multi?)
         (= input ""))
    (delete-element options selections use-index?)

    (and is-multi?
         (= input ""))
    (delete-multi options selections false use-index?)

    (not is-multi?)
    (delete-element (filter-input props options input)
                    selections
                    use-index?)
    :else
    (delete-multi (filter-input props options input)
                  (mapv #(dissoc % :i :type) selections)
                  true
                  use-index?)))

(defn- filter-groups [{:keys [label-key keep-selected?]} groups selections]
  (let [selections (reduce (fn [r {:keys [type] :as s}]
                             (if (= type :group)
                               (conj r (get s label-key))
                               r))
                           #{}
                           (if (vector? selections)
                             selections
                             [selections]))]
    (if keep-selected?
      groups
      (filterv (fn [g]
                 (not (selections (get g label-key))))
               groups))))

(defn- typeahead-filter [{:keys [values label-key
                                 all-group-label
                                 is-grouped? show-all-group?] :as props}
                         options input]
  (let [selections @values]
    (cond
      (and (not is-grouped?)
           show-all-group?)
      (let [filtered-options (filter-options props options selections input true)
            filtered-group-count (count filtered-options)]
        (filter-groups (assoc props :filter-key label-key)
                       (cond-> []
                         (> filtered-group-count 0)
                         (conj {label-key all-group-label
                                :gidx 0
                                :group-count (count options)
                                :filtered-count filtered-group-count
                                :options filtered-options
                                :all-options options}))
                       selections))

      is-grouped?
      (:grouped-options
       (reduce (fn [{:keys [grouped-options gidx] :as r} {:keys [options fixed?] :as g}]
                 (let [filtered-options (filter-options props options selections input false)
                       filtered-group-count (count filtered-options)
                       group-count (count options)]
                   (if (> filtered-group-count 0)
                     {:grouped-options (conj grouped-options
                                             (cond-> {label-key (get g label-key)
                                                      :gidx gidx
                                                      :group-count group-count
                                                      :filtered-count filtered-group-count
                                                      :options filtered-options
                                                      :all-options options}
                                               fixed? (assoc :fixed? fixed?)))
                      :gidx (+ filtered-group-count gidx 1)}
                     r)))
               {:grouped-options []
                :gidx 0}
               (filter-groups (assoc props :filter-key label-key)
                              options
                              selections)))

      :else (filter-options props options selections input true))))

; ------------------------ Options-List  -------------------------

(defn- ensure-scroll
  ([component]
   (ensure-scroll component 100))
  ([component timeout]
   (when component
     (js/setTimeout
      #(.scrollIntoView component (clj->js {:block "nearest" :inline "nearest"}))
      timeout))))

(defn- delete-selection [values is-multi? selection]
  (if is-multi?
    (delete-multi @values [selection] true true)
    {}))

(defn- add-selection [{:keys [label-key value-key is-multi? keep-selected?]}
                      values
                      {:keys [gidx type all-options value] :as selection}]
  (cond
    (and keep-selected?
         (some #{value} (mapv :value @values)))
    (delete-selection values is-multi? selection)
    (and is-multi?
         (or gidx (= type :group))
         (vector? all-options))
    (let [filter-fn (reduce (fn [r v]
                              (let [l (get v label-key)
                                    v (get v value-key)]
                                (cond-> r
                                  (and l v)
                                  (conj {label-key l
                                         value-key v}))))
                            #{}
                            all-options)]
      (-> (filterv (fn [v]
                     (not (filter-fn {label-key (get v label-key)
                                      value-key (get v value-key)})))
                   @values)
          (add-element selection)
          distinct
          vec))
    is-multi?
    (vec (distinct (add-element @values selection)))
    :else selection))

(defn- group-count-label [group-count]
  (cond (> group-count 1000000)
        "1M+"
        (>= group-count 1000)
        (str (int (/ group-count 1000))
             "K")
        :else group-count))

(defn- row-renderer [{:keys [on-change values
                             is-multi? label-key tooltip-key group-selectable?
                             is-grouped? show-all-group?
                             show-options-tooltip? keep-selected?] :as props}
                     key index style
                     {row-value :value :keys [gidx group-count filtered-count disabled? disabled-hint] :as rowdata}
                     raw-state select-idx last-key]
  (when (and (= index select-idx)
             (and gidx (or (not is-multi?)
                           (not group-selectable?))))
    (if (and (= :up last-key)
             (not= gidx 0))
      (swap! raw-state update :select-idx dec)
      (swap! raw-state update :select-idx inc)))
  (reagent/as-element
   [:div {:key key
          :on-click #(do (.stopPropagation %)
                         (when (and on-change
                                    (or (not gidx)
                                        (and is-multi? gidx group-selectable?))
                                    (not disabled?))
                           (on-change (add-selection props values
                                                     (assoc rowdata
                                                            :i (cond
                                                                 gidx gidx
                                                                 (or is-grouped? show-all-group?)
                                                                 (- index gidx 1)
                                                                 :else index)
                                                            :type (if gidx :group :value))))))
          :on-mouse-enter (fn [e]
                            (when (and (not= index select-idx)
                                       (not (and gidx (or (not is-multi?)
                                                          (not group-selectable?))))
                                       (not disabled?))
                              (swap! raw-state assoc :select-idx index)))
          :class (cond-> []
                   gidx (conj group-element-class)
                   (and is-multi? group-selectable?) (conj selectable-group-class)
                   (not gidx) (conj value-element-class)
                   (some #{rowdata} (flatten (mapv :all-options @values)))
                   (conj inactive-class)
                   (and (not gidx)
                        keep-selected?
                        (some #{row-value} (mapv :value @values)))
                   (conj selected-class)
                   disabled? (conj inactive-class)
                   (and
                    (= index select-idx)
                    (or (not gidx)
                        (and group-selectable?
                             is-multi?
                             (or is-grouped? show-all-group?))))
                   (conj drop-down-value-selected-class))
          :title (cond
                   (and disabled? disabled-hint) disabled-hint
                   show-options-tooltip? (get rowdata tooltip-key))
          :style style}
    [:span {:class truncate-text-class}
     (get rowdata label-key)]
    (when group-count
      [:span {:class group-count-class
              :title (if (not= group-count filtered-count)
                       (cond-> (str "Unfiltered: " group-count
                                    "\nFiltered: " filtered-count)
                         (and is-multi? group-selectable?)
                         (str "\nSelect will applied to unfiltered values"))
                       group-count)}
       (cond-> ""
         (not= group-count filtered-count) (str (group-count-label filtered-count) "/")
         :always (str (group-count-label group-count)))])]))

(defn- get-list-item [{:keys [is-grouped? show-all-group?]} idx rows]
  (cond
    (or is-grouped? show-all-group?)
    (some (fn [{:keys [gidx group-count] :as group}]
            (cond
              (= gidx idx)
              group
              (and (< gidx idx)
                   (>= (+ gidx group-count)
                       idx))
              (get-in group [:options (- idx gidx 1)])))
          rows)

    :else
    (get rows idx)))

(defn- list-menu [autoprops
                  {:keys [no-options-placeholder menu-height
                          menu-row-height overscan-row-count]
                   :as props}
                  rows-length rows raw-state acc-state]
  (let [select-idx @(:select-idx acc-state)
        last-key @(:last-key acc-state)]
    [virt-list {:width (aget autoprops "width")
                :height menu-height
                :rowCount rows-length
                :rowHeight menu-row-height
                :overscanRowCount overscan-row-count
                ;:onRowsRendered (fn [e]
                ;                  (let [idx (min (aget e "startIndex")
                ;                                 0)]
                ;                   (swap! state assoc 
                ;                         :select-idx idx)))
                :scrollToIndex select-idx
                :noRowsRenderer #(reagent/as-element [:div {:class [value-element-class inactive-class]}
                                                      no-options-placeholder])
                :rowRenderer (fn [a]
                               (let [{:keys [key index style]} (js->clj a :keywordize-keys true)]
                                 (row-renderer props key index style
                                               (get-list-item props index rows)
                                               raw-state select-idx last-key)))}]))

(defn- flip-menu?
  ([top menu-height]
   (let [{^number portal-height :height}
         (bounding-rect-node js/document.body)]
     (< portal-height (+ top menu-height)))))

(defn- list-menu-parent [{:keys [menu-height menu-row-height on-change
                                 values options is-multi? group-selectable?
                                 is-grouped? show-all-group? ensure-visibility?]
                          :as props}
                         raw-state acc-state parent-comp flip-state]
  (let [{:keys [select-current? filterval input-comp]} acc-state
        select-idx @(:select-idx acc-state) ;Workaround for trigger updating
        rows (typeahead-filter props @options @filterval)
        rows-length (cond
                      (and rows (or is-grouped? show-all-group?))
                      (reduce (fn [c {:keys [filtered-count]}]
                                (cond-> (inc c) ;for group label
                                  filtered-count (+ filtered-count)))
                              0
                              rows)
                      (and rows (not (or is-grouped? show-all-group?)))
                      (count rows)
                      :else 0)
        menu-height (min menu-height
                         (+ menu-border-size
                            (* rows-length
                               menu-row-height)))
        menu-height (max menu-height (+ menu-border-size menu-row-height))
        props (assoc props :menu-height menu-height)
        flip? (if (and parent-comp ensure-visibility?)
                (flip-menu? (aget (.getBoundingClientRect @parent-comp) "y") menu-height)
                false)]
    (reset! flip-state flip?)
    (swap! raw-state assoc :displayed-rows rows-length)
    (when @select-current?
      (swap! raw-state assoc
             :select-idx 0
             :select-current? false)
      (let [idx (or select-idx 0)
            {:keys [gidx] :as row} (get-list-item props idx rows)]
        (when (and on-change row (or (and is-multi? gidx group-selectable?)
                                     (not gidx)))
          (ensure-scroll @input-comp)
          (on-change (add-selection props
                                    values
                                    (if gidx
                                      (assoc row :type :group)
                                      (assoc row
                                             :i (if (or is-grouped? show-all-group?)
                                                  (- idx 1)
                                                  idx)
                                             :type :value)))))))

    [:div {:class (cond-> [toolbar-ignore-class option-list-class]
                    flip?
                    (conj open-list-upwards-class))}
     [virt-autosizer {:disableHeight true}
      (fn [autoprops]
        (reagent/as-element [list-menu autoprops props rows-length rows raw-state acc-state]))]]))

(defn- handle-click [e raw-state acc-state check-contains? scroll?]
  (let [{:keys [root-comp input-comp list-open? in-list?]} @raw-state]
    (when scroll?
      (ensure-scroll input-comp))
    (cond
      (and (not in-list?)
           list-open?
           (or (not check-contains?)
               (not (.contains root-comp (aget e "target")))))
      (close-menu-state raw-state)
      (and (or in-list?
               list-open?)
           (or (not check-contains?)
               (.contains root-comp (aget e "target")))
           input-comp)
      (.focus input-comp))))

(defn- options-parent [{:keys [values menu-z-index menu-min-width is-multi?] :as props} raw-state {:keys [input-comp root-comp] :as acc-state}]
  (let [parent-comp (reagent/atom nil)
        flip? (reagent/atom nil)
        click-handler (fn [e] (handle-click e raw-state acc-state true true))
        wheel-handler (fn [e] (handle-click e raw-state acc-state is-multi? false))]
    (reagent/create-class
     {:display-name "select options-parent"
      :component-did-mount #(do
                              (ensure-scroll @input-comp)
                              (reset! parent-comp (rdom/dom-node %))
                              (js/document.addEventListener "wheel" wheel-handler true)
                              (js/document.addEventListener "mousedown" click-handler true))
      :component-will-unmount #(do
                                 (js/document.removeEventListener "wheel" wheel-handler true)
                                 (js/document.removeEventListener "mousedown" click-handler true))
      :reagent-render
      (fn [props raw-state acc-state]
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
                          :root-comp-atom root-comp
                          :update-sub values
                          :flip-atom flip?}
                   menu-min-width (assoc :menu-min-width menu-min-width)
                   menu-z-index (assoc :z-index menu-z-index))
          [list-menu-parent props raw-state acc-state parent-comp flip?]]])})))

; ------------------------ Header: Selections + Input -------------------------
(defn- input [{:keys [on-input-change input-id disabled? is-multi?
                      autofocus? select-on-tab is-clearable?
                      extra-class search-aria-label]
               :as props}
              raw-state acc-state]
  [:input {:ref #(swap! raw-state assoc :input-comp %)
           :class (cond-> []
                    is-multi? (conj export-ignore-class)
                    (string? extra-class) (conj extra-class))
           :size 1
           :id input-id
           :auto-focus autofocus?
           :type :text
           :disabled disabled?
           :aria-label (translate-label search-aria-label)
           :on-key-down (fn [e]
                          (let [filterval @(:filterval acc-state)]
                            (cond (and is-clearable?
                                       (or (nil? filterval)
                                           (= filterval ""))
                                       (= (aget e "keyCode") backspace-keycode))
                                  (unselect-last props)
                                  (or (and (not select-on-tab)
                                           (= (aget e "keyCode") tab-keycode))
                                      (= (aget e "keyCode") esc-keycode))
                                  (do (swap! raw-state (fn [oldstate]
                                                         (assoc oldstate
                                                                :in-list? false
                                                                :list-open? false)))
                                      (clear-input props raw-state acc-state false)))))
           :on-change (fn [e]
                        (let [val (aget e "target" "value")]
                          (swap! raw-state (fn [oldstate]
                                             (assoc oldstate
                                                    :filterval val
                                                    :list-open? true)))
                          (set-input-width props acc-state val)
                          (when on-input-change
                            (on-input-change val))))}])

(defn- selected-elm [{:keys [invalid fixed? gidx] :as selection}
                     {:keys [remove-invalid? mark-invalid? on-change value-render-fn
                             invalid-class values label-key
                             options check-key invalid-hint
                             option-show-title? tooltip-key
                             value-key is-multi? disabled?
                             is-grouped? group-value-key
                             remove-aria-label] :as props}
                     deleted-sel?]
  (let [selection-group (get selection group-value-key)
        filter-options (cond->> @options
                         (and is-grouped? group-value-key) (filter #(= selection-group
                                                                       (get % group-value-key)))
                         is-grouped? (mapcat :options))

        invalid (if (and (not invalid) (or remove-invalid? mark-invalid?))
                  (boolean (empty? (filter-input (assoc props :filter-key check-key)
                                                 filter-options
                                                 (get selection check-key))))
                  invalid)]
    (when (or (and remove-invalid? (not invalid))
              (not remove-invalid?))
      [:div {:class (cond-> multi-select-value-base-class
                      invalid (str " " invalid-class)
                      gidx (str " " multi-select-group-value-class))
             :key (get value-key selection)}
       [:div (cond-> {:class multi-select-value-class}
               option-show-title? (assoc :title (get selection tooltip-key))
               (and invalid mark-invalid?) (assoc :title invalid-hint))
        (if (fn? value-render-fn)
          [value-render-fn selection]
          (get selection label-key))]
       (when (and is-multi? (not (or disabled? fixed?)))
         [(let [over-del? (reagent/atom false)
                remove-aria-label (translate-label remove-aria-label)]
            (fn []
              [:button {:class clear-button-class
                        :aria-label remove-aria-label
                        :on-mouse-down (fn [e]
                                         (.stopPropagation e)
                                         (reset! deleted-sel? true)
                                         (reset! over-del? true))
                        :on-mouse-leave (fn []
                                          (reset! deleted-sel? false)
                                          (reset! over-del? false))
                        :on-click (fn [e]
                                    (.stopPropagation e)
                                    (when on-change
                                      (on-change (delete-selection values is-multi? selection)))
                                    (when-not @over-del?
                                      (reset! deleted-sel? false)))}
               [:span.icon-close]]))])])))

(defn- in-box [box pos]
  (and box pos
       (<= (.-left box) (:x pos) (.-right box))
       (<= (.-top box) (:y pos) (.-bottom box))))

(defn- typeahead-container [{:keys [autofocus? is-clearable?]} raw-state acc-state]
  (let [leaved-comp? (reagent/atom autofocus?)
        deleted-sel? (reagent/atom false)
        container-div (atom nil)
        last-mouse-pos (atom nil)]
    (fn [{:keys [values placeholder disabled? show-more
                 is-multi? on-change on-blur visible-selections
                 show-clean-all? is-searchable? disabled-class
                 clear-aria-label open-aria-label start-icon
                 aria-label label]
          :as props}
         raw-state acc-state]
      (let [{:keys [filterval in-list? is-focused? input-comp list-open?]} acc-state
            selected @values
            label (val-or-deref label)
            placeholder (translate-label placeholder)
            aria-label (translate-label aria-label)]
        [:div {:ref #(reset! container-div %)
               :class (cond-> basic-select-class
                        is-multi? (str " " multi-select-class)
                        (not is-multi?) (str " " single-select-class)
                        disabled? (str " " disabled-class))
               :aria-label (or aria-label label placeholder)
               :on-mouse-over #(reset! leaved-comp? false) ;;needed to detect if mouse is in on mount
               :on-mouse-move #(reset! last-mouse-pos {:x (.-clientX %), :y (.-clientY %)})
               :on-mouse-down (fn [_]
                                (when-not disabled?
                                  (reset! leaved-comp? false)
                                  (swap! raw-state assoc
                                         :in-list? false
                                         :select-idx 0
                                         :is-focused? true
                                         :select-current? false
                                         :list-open? (not @list-open?))))
               :on-mouse-up #(when (and (not disabled?)
                                        is-searchable?)
                               (.focus @input-comp))
               :on-mouse-enter #(reset! leaved-comp? false)
               :on-mouse-leave (fn [_]
                                 (reset! leaved-comp? true)
                                 (reset! deleted-sel? false))
               :on-blur (fn [_]
                          (when (and on-blur
                                     (not disabled?)
                                     (not @in-list?)
                                     (or (and (not @deleted-sel?)
                                              @leaved-comp?)
                                         (and @last-mouse-pos
                                              (not (in-box (when-let [div @container-div]
                                                             (.getBoundingClientRect div))
                                                           @last-mouse-pos)))))
                            (on-blur)))}
         [:<>
          (when start-icon
            [icon {:icon start-icon}])
          (cond
             ;placeholder
            (and (= 0 (count selected))
                 (= "" @filterval)
                 (not @is-focused?))
            [:div.select-placeholder placeholder]
             ;multi value
            is-multi?
            [:div {:class (cond-> multi-select-holder-class
                            @list-open? (str " " show-all-selections-class))
                   :on-mouse-down #(when @list-open?
                                     (.stopPropagation %))}
             (cond-> (reduce (fn [parent selection]
                               (conj parent [selected-elm selection props deleted-sel?]))
                             [:<>]
                             (if @list-open?
                               selected
                               (take visible-selections selected)))
               (and (not @list-open?)
                    (> (count selected) visible-selections))
               (conj [:span {:class "overflow-count"}
                      (format show-more (- (count selected) visible-selections))]))
             (when is-searchable?
               [input (assoc props :extra-class "min-w-40") raw-state acc-state])]
            ;empty select  
            (or (= 0 (count selected))
                (not= "" @filterval))
            [:div]
            :else
            ;single value
            [selected-elm selected props])
          (when (and is-searchable?
                     (not is-multi?))
            [input (assoc props :extra-class "min-w-0") raw-state acc-state])
          [:div {:class select-button-group-class}
           (when (and is-clearable? show-clean-all? (not disabled?) selected (not-empty selected))
             [:button {:class clear-button-class
                       :aria-label (translate-label clear-aria-label)
                       :on-mouse-enter #(reset! deleted-sel? true)
                       :on-mouse-leave #(reset! deleted-sel? false)
                       :on-mouse-down #(.stopPropagation %)
                       :on-click (fn [e]
                                   (let [selected (filterv (fn [{:keys [fixed?]}] fixed?)
                                                           selected)]
                                     (.stopPropagation e)
                                     (when is-searchable?
                                       (aset input-comp "value" "")
                                       (swap! raw-state assoc :filterval ""))
                                     (reset! deleted-sel? false)
                                     (unselect-all is-multi? on-change selected)
                                     (when is-searchable?
                                       (.focus @input-comp)
                                       (swap! raw-state assoc :is-focused? true))))}
              [:span.icon-close]])
           [:button {:class select-button-class
                     :aria-label (translate-label open-aria-label)}
            [:span.icon-chevron-down]]]]]))))

;options: [{}]
;values:  for multi [{}], for single {} 
; ------------------------ Component -------------------------   
(defn select-parentless [props]
  (let [raw-state (reagent/atom {:filterval ""
                                 :root-comp false
                                 :list-open? false
                                 :in-list? false
                                 :is-focused? false
                                 :last-key nil
                                 :select-current? false
                                 :select-idx 0
                                 :input-comp nil
                                 :displayed-rows 0})
        acc-state {:filterval (get-cursor raw-state :filterval)
                   :root-comp (get-cursor raw-state :root-comp)
                   :list-open? (get-cursor raw-state :list-open?)
                   :last-key (get-cursor raw-state :last-key)
                   :in-list? (get-cursor raw-state :in-list?)
                   :is-focused? (get-cursor raw-state :is-focused?)
                   :select-current? (get-cursor raw-state :select-current?)
                   :select-idx (get-cursor raw-state :select-idx)
                   :input-comp (get-cursor raw-state :input-comp)
                   :displayed-rows (get-cursor raw-state :displayed-rows)}]
    (reagent/create-class
     {:display-name "select"
      :component-did-catch (fn [_ e info]
                             (error "Select component crashed: " e info))
      :component-did-update (fn []
                              (let [{:keys [list-open? input-comp]} acc-state]
                                (when (and @list-open? @input-comp)
                                  (.focus @input-comp))))
      :reagent-render (fn [props]
                        (let [{:keys [select-key select-on-tab is-multi?
                                      disabled? disabled-class is-searchable?]
                               lb :label
                               :as props}
                              (apply-default-props props raw-state acc-state)
                              {:keys [list-open? input-comp displayed-rows]} acc-state]
                          [error-boundary {:validate-fn #(validate "select" specification props)}
                           [:div {:ref #(swap! raw-state assoc :root-comp %)
                                  :class select-wrapper-class
                                  :on-key-down (fn [e]
                                                 (when (and (not disabled?)
                                                            (= up-arrow-keycode (aget e "keyCode")))
                                                   (if @list-open?
                                                     (do
                                                       (.preventDefault e)
                                                       (.stopPropagation e)
                                                       (swap! raw-state assoc :last-key :up)
                                                       (swap! raw-state update :select-idx (fn [old]
                                                                                             (max (dec old)
                                                                                                  0))))
                                                     (swap! raw-state assoc :list-open? true)))
                                                 (when (and (not disabled?)
                                                            (= down-arrow-keycode (aget e "keyCode")))
                                                   (if @list-open?
                                                     (do
                                                       (.preventDefault e)
                                                       (.stopPropagation e)
                                                       (swap! raw-state assoc :last-key :down)
                                                       (swap! raw-state update :select-idx (fn [old]
                                                                                             (min (inc old)
                                                                                                  (dec @displayed-rows)))))
                                                     (swap! raw-state assoc :list-open? true)))
                                                  ;select
                                                 (when (and (not disabled?)
                                                            (and @list-open?
                                                                 (or (= select-key (aget e "keyCode"))
                                                                     (and select-on-tab
                                                                          (= tab-keycode (aget e "keyCode"))))))
                                                   (.preventDefault e)
                                                   (.stopPropagation e)
                                                   (if (and @input-comp (= @displayed-rows 0))
                                                     (close-menu-state raw-state)
                                                     (swap! raw-state assoc :select-current? true))
                                                   (when is-searchable?
                                                     (.focus @input-comp))))}
                            [typeahead-container props raw-state acc-state]
                            (when (and @list-open? (not disabled?))
                              [options-parent props raw-state acc-state])]]))})))

(defn  ^:export select [props]
  [error-boundary
   [parent-wrapper props
    [select-parentless props]]])

       ; For debugging
       ;   :component-will-unmount #(js/console.log "WILL UNMOUNT")})))) 