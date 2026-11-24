(ns de.explorama.frontend.ui-base.components.common.virtualized-list
  (:require ["@tanstack/react-virtual" :refer [useVirtualizer]]
            ["react" :as react]
            [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.resize :refer [use-parent-size]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.virtual :as virtual]
            [reagent.core :as r]))

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

(def sizer-class "virtualized-list__sizer")

(defn- no-rows-renderer []
  (r/as-element [:div {:role "row"} [:div {:role "gridcell"} "No row"]]))

(defn- default-row-renderer [key index style row]
  ^{:key (str "r" key)}
  [:div {:style style :role "row"}
   [:div {:role "gridcell"}
    row]])

(def default-parameters {:height 100
                         :width 100
                         :row-height 20
                         :dynamic-height? false
                         :full-width? false
                         :full-height? false
                         :overscan-row-count 2
                         :row-renderer default-row-renderer
                         :no-rows-renderer no-rows-renderer})

(defn- internal-list [{:keys [rows row-height dynamic-height? overscan-row-count
                              width height full-width? full-height?
                              extra-class list-extra-style parent-extra-style
                              scroll-to-index row-renderer no-rows-renderer]}]
  (let [scroll-ref (react/useRef nil)
        parent-size (use-parent-size scroll-ref)
        rows (val-or-deref rows)
        row-count (count (or rows []))
        virtualizer (useVirtualizer
                     #js {:count row-count
                          :getScrollElement (fn [] (.-current scroll-ref))
                          :estimateSize (fn [_] row-height)
                          :overscan overscan-row-count})]
    (react/useEffect
     (fn []
       (when scroll-to-index
         (.scrollToIndex virtualizer scroll-to-index))
       js/undefined)
     #js [scroll-to-index (:height parent-size)])
    [:div {:ref scroll-ref
           :class extra-class
           :role "grid"
           :tab-index 0
           :style (merge {:overflow "auto"
                          :width (if full-width? (:width parent-size 0) width)
                          :height (if full-height? (:height parent-size 0) height)}
                         parent-extra-style
                         list-extra-style)}
     (if (zero? row-count)
       (no-rows-renderer)
       [:div {:class sizer-class
              :role "rowgroup"
              :style (virtual/sizer-style nil (.getTotalSize virtualizer))}
        (doall
         (for [virtual-row (.getVirtualItems virtualizer)]
           (let [index (.-index virtual-row)
                 item-key (.-key virtual-row)
                 row (get rows index)]
             (if dynamic-height?
               ^{:key item-key}
               [:div {:ref (.-measureElement virtualizer)
                      :data-index index
                      :style (virtual/row-style (.-start virtual-row) (.-size virtual-row) true)}
                (row-renderer item-key index {} row)]
               (row-renderer item-key index
                             (virtual/row-style (.-start virtual-row) (.-size virtual-row) false)
                             row)))))])]))

(defn ^:export virtualized-list [params]
  (let [params (merge default-parameters params)]
    [error-boundary {:validate-fn #(validate "virtualized-list" specification params)}
     [:f> internal-list params]]))