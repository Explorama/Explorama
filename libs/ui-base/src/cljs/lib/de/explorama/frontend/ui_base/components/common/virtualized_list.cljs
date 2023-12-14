(ns de.explorama.frontend.ui-base.components.common.virtualized-list
  (:require [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [reagent.core :as r]
            [cljsjs.react-virtualized]))

(def parameter-definition
  {:rows {:type [:vector :derefable]
          :desc "The rows which should be visible"}
   :width {:type :number
           :desc "Width of list in pixels"}
   :height {:type :number
            :desc "Height of list in pixels"}
   :full-width? {:type :boolean
                 :desc "If true uses 100% of available width (:width will be ignored)"}
   :full-height? {:type :boolean
                  :desc "If true uses 100% of available height (:height will be ignored)"}
   :dynamic-height? {:type :boolean
                     :desc "If true the height of every row will be calculated by its content"}
   :row-height {:type :number
                :desc "Row height of a list item in pixels"}
   :overscan-row-count {:type :number
                        :desc "Number of rows to render above/below the visible bounds of the list. This can help reduce flickering during scrolling"}
   :scroll-to-index {:type :number
                     :desc "Row index to ensure visible (by forcefully scrolling if necessary)"}
   :row-renderer {:type :function
                  :default-fn-str "(fn [key index style row]\n [:div {:style style}\n   row])))"
                  :desc "Renderer for a single row. Its important to use r/as-element and to include the necessary aria roles (e.g. row, rowgroup and gridcell)."}
   :no-rows-renderer {:type :function
                      :default-fn-str "(fn []\n (r/as-element\n  [:div \"No row\"])))"
                      :desc "Renderer when rows are empty. Its important to use r/as-element and the necessary aria roles (e.g. row, rowgroup and gridcell)."}
   :parent-extra-style {:type :map
                        :desc "Style which is applied to parent component if :full-width? or :full-height? is set."}
   :list-extra-style {:type :map
                      :desc "Style which is applied to list component"}
   :extra-class {:type :string
                 :desc "Class which will be added to list root"}})
(def specification (parameters->malli parameter-definition nil))

(def ^:private virt-list (r/adapt-react-class (aget js/ReactVirtualized "List")))
(def ^:private virt-autosizer (r/adapt-react-class (aget js/ReactVirtualized "AutoSizer")))
(def ^:private virt-cell-measurer (r/adapt-react-class (aget js/ReactVirtualized "CellMeasurer")))

(defn- no-rows-renderer []
  (r/as-element [:div {:role "row"} [:div {:role "gridcell"} "No row"]]))

(defn- default-row-renderer [key index style row]
  ^{:key (str "r" key)}
  [:div {:style style :role "row"}
   [:div {:role "gridcell"}
    row]])

(defn- default-dynamic-row-renderer [cache _row-renderer _key index _style _row measure]
  (r/create-class
   {:component-did-mount #(measure)
    :component-did-update (fn [this old-argv]
                            (let [[_ _ _ _ old-style old-row] (rest old-argv)
                                  [_ _ _ _ _ new-row] (rest (r/argv this))]
                              (when (and old-row old-style (not= old-row new-row))
                                (.clear cache index 0))))

    :reagent-render
    (fn [_ row-renderer key index style row _]
      [row-renderer key index style row])}))

(defn- autosize-row [cache row-renderer row-index key parent style row]
  [virt-cell-measurer {"rowIndex" row-index
                       "key" key
                       "parent" parent
                       "columnIndex" 0
                       "cache" cache}
   (fn [p]
     (r/as-element
      [default-dynamic-row-renderer cache row-renderer key row-index style row (aget p "measure")]))])

(def default-parameters {:height 100
                         :width 100
                         :row-height 20
                         :dynamic-height? false
                         :full-width? false
                         :full-height? false
                         :overscan-row-count 2
                         :row-renderer default-row-renderer
                         :no-rows-renderer no-rows-renderer})

(defn- internal-list [{:keys [dynamic-height? row-height]} _]
  (let [cell-measurer-cache (when dynamic-height?
                              (new (aget js/ReactVirtualized "CellMeasurerCache")
                                   #js{"fixedWidth" true
                                       "defaultHeight" row-height}))]
    (fn [{:keys [width height row-height overscan-row-count extra-class
                 scroll-to-index rows no-rows-renderer row-renderer list-extra-style]}
         {auto-width :width auto-height :height
          :or {auto-width width
               auto-height height}}]
      (let [rows (val-or-deref rows)
            row-count (count (or rows []))]
        [virt-list (cond-> {:width auto-width
                            :height auto-height
                            :rowCount row-count
                            :overscanRowCount overscan-row-count
                            :noRowsRenderer no-rows-renderer
                            :rowRenderer (fn [a]
                                           (let [{:keys [key index style parent]} (js->clj a :keywordize-keys true)]
                                             ^{:key (str "vr" key)}
                                             (r/as-element
                                              (if dynamic-height?
                                                (autosize-row cell-measurer-cache row-renderer index key parent style (get rows index))
                                                (row-renderer key index style (get rows index))))))}
                     scroll-to-index (assoc :scrollToIndex scroll-to-index)
                     (not dynamic-height?) (assoc :rowHeight row-height)
                     dynamic-height? (assoc :rowHeight (aget cell-measurer-cache "rowHeight"))
                     dynamic-height? (assoc :deferredMeasurementCache cell-measurer-cache)
                     list-extra-style (assoc :style list-extra-style)
                     extra-class (assoc :className extra-class))]))))

(defn ^:export virtualized-list [params]
  (let [{:keys [full-height? full-width? parent-extra-style] :as params}
        (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "virtualized-list" specification params)}
     (if (or full-width? full-height?)
       [virt-autosizer (cond-> {}
                         (not full-width?) (assoc :disable-width true)
                         (not full-height?) (assoc :disable-height true)
                         parent-extra-style (assoc :style parent-extra-style))
        (fn [autosizer-props]
          (r/as-element [internal-list params (js->clj autosizer-props :keywordize-keys true)]))]
       [internal-list params nil])]))