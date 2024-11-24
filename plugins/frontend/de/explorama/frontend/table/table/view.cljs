(ns de.explorama.frontend.table.table.view
  (:require ["react-virtualized" :refer [Grid]]
            [clojure.string :refer [join]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button-group
                                                                            input-field
                                                                            select]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.select :as select-utils]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.timeout :as timeout]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.table.components.details :as details]
            [de.explorama.frontend.table.components.frame-header :as frame-header]
            [de.explorama.frontend.table.config :as config]
            [de.explorama.frontend.table.empty :as empty]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.frontend.table.table.state :as table-state]
            [de.explorama.shared.table.ws-api :as ws-api]
            [de.explorama.frontend.table.util.queue :as queue-util]))

(def ^:private virt-grid (r/adapt-react-class Grid))

(def body-row-height 30)
(def header-row-height 40)
(def table-footer-height 90)

(def ^:private normal-cell-color-class "table__normal_color")
(def ^:private second-cell-color-class "table__second_color")
(def ^:private selected-cell-class "table__selected")

(def ^:private table-header-scrollable-parent-class "table--header__scrollable__parent")
(def ^:private table-header-scrollable-cell-class "table--header__scrollable__cell")
(def ^:private table-header-scrollable-empty-cell-class "table-header-scrollable-empty-cell")

(def ^:private table-header-fixed-parent-class "table--header__fixed__parent")


(def ^:private table-body-scrollable-parent-class "table--body__scrollable__parent")
(def ^:private table-body-scrollable-cell-class "table--body__scrollable__cell")

(def ^:private table-footer-parent-class "table--footer__parent")

(defn- table-body-height [frame-height]
  (max (- frame-height header-row-height table-footer-height)
       0))

(defn- check-sorting-exists [current-sorting sort-by-attr]
  (some (fn [{:keys [attr] :as entry}]
          (when (= attr sort-by-attr)
            entry))
        current-sorting))

(defn- add-sorting [current-sorting sort-by-attr add?]
  (let [{existing-direction :direction :as existing-entry} (check-sorting-exists current-sorting sort-by-attr)
        new-sorting (when existing-entry
                      (mapv (fn [{:keys [attr direction] :as entry}]
                              (if (= attr sort-by-attr)
                                (assoc entry :direction (if (= direction "asc")
                                                          "desc"
                                                          "asc"))
                                entry))
                            current-sorting))]

    (cond->  (if add?
               (or new-sorting current-sorting)
               [])

      (or (not add?)
          (not existing-entry))
      (conj {:attr sort-by-attr :direction (if (= existing-direction "asc")
                                             "desc"
                                             "asc")}))))

(defn- header-operations [attribute {:keys [get-config-fn]}]
  (let [{:keys [direction] :as sort-entry} (check-sorting-exists (get-config-fn ws-api/sorting-key config/default-sorting)
                                                                 attribute)]
    (if sort-entry
      [:div.table__header__actions
       [icon {:icon (if (= direction "asc")
                      :sort-asc
                      :sort-desc)
              :size 13
              :color :blue}]]
      [:<>])))

(defn- header-cell-renderer [^string key style custom-class cell
                             {:keys [^boolean read-only?
                                     ^function get-config-fn ^function set-config-fn
                                     ^function request-data-fn]
                              :as params}]
  (let [^string display-attribute (config/attribute->display cell)]
    (r/as-element
     ^{:key (str ::header-cell-renderer key)}
     [:div {:style style
            :class custom-class
            :title display-attribute
            :on-click (fn [e]
                        (when (and cell (not read-only?))
                          (let [add? (aget e "ctrlKey")]
                            (set-config-fn ws-api/sorting-key
                                           (add-sorting (get-config-fn ws-api/sorting-key config/default-sorting)
                                                        cell
                                                        add?))
                            (set-config-fn ws-api/current-page-key 1)
                            (request-data-fn))))}
      [:div.table__header__label
       display-attribute]
      [header-operations cell params]])))


(defn- fill-header? [^number width ^number column-count]
  (< width (* column-count config/column-width)))

(defn- scrollable-header [state
                          {:keys [^number column-count
                                  ^function column-access-fn
                                  ^function delayed-log-fn
                                  ^function set-config-fn
                                  ^function get-scroll-fn]
                           [^number width] :size
                           :as params}]
  (let [^number scroll-x (get-scroll-fn :x 0)
        ^boolean fill-header? (fill-header? width column-count)
        ^number column-count (cond-> column-count
                               fill-header? (inc))]
    [virt-grid {:width width
                :height header-row-height
                :className table-header-scrollable-parent-class
                :columnWidth config/column-width
                :columnCount column-count
                :rowCount 1
                :rowHeight header-row-height
                :on-scroll #(let [^number new-scroll-x (aget % "scrollLeft")]
                              (when-not (= new-scroll-x scroll-x)
                                (set-config-fn ws-api/scroll-x-key new-scroll-x)
                                (delayed-log-fn)))
                :scroll-Left scroll-x
                :cellRenderer (fn [a]
                                (let [^string row-key (aget a "key")
                                      ^number column-index (aget a "columnIndex")
                                      style (aget a "style")
                                      cell-data (column-access-fn column-index)]

                                  (header-cell-renderer row-key
                                                        style
                                                        (if cell-data
                                                          table-header-scrollable-cell-class
                                                          table-header-scrollable-empty-cell-class)
                                                        cell-data
                                                        params)))}]))

(defn- prepare-content [cell]
  (cond
    (number? cell) (i18n/localized-number cell)
    ;; annotation/comment
    (and (map? cell)
         (get cell "author"))
    (let [{author "author" editor "editor" content "content"} cell
          author (when author @(fi/call-api :name-for-user-sub author))
          editor (when editor @(fi/call-api :name-for-user-sub editor))
          author-label @(re-frame/subscribe [::i18n/translate :comment-author-label])
          editor-label @(re-frame/subscribe [::i18n/translate :comment-editor-label])]
      (if (= author editor)
        {:tooltip (str content "\n(" editor-label ": " author ")")
         :content (str content)}
        {:tooltip (str content "\n(" author-label ": " author " ," editor-label ": " editor ")")
         :content (str content)}))
    (coll? cell)
    (join ", " (map prepare-content cell))
    :else
    (str cell)))

(defn- body-cell-renderer [^string key ^number row-index style custom-class
                           cell-data
                           {:keys [^function on-double-click ^function on-click
                                   ^function is-row-selected?]}]
  (let [content (prepare-content cell-data)
        {:keys [^string tooltip ^string content]
         :or {^string tooltip content
              ^string content content}} (when (map? content) content)]

    (r/as-element
     ^{:key (str ::body-cell-renderer custom-class key)}
     [:div (cond-> {:style style
                    :class (cond-> (if (even? row-index)
                                     [custom-class normal-cell-color-class]
                                     [custom-class second-cell-color-class])
                             (is-row-selected? row-index)
                             (conj selected-cell-class))
                    :title tooltip}
             on-click (assoc :on-click (partial on-click row-index))
             on-double-click (assoc :on-double-click (partial on-double-click row-index)))
      content])))

(defn- scrollable-body [state {:keys [^number column-count ^number row-count
                                      ^function attribute-value-access-fn
                                      ^function delayed-log-fn
                                      ^function set-config-fn ^function get-config-fn
                                      ^function get-scroll-fn]
                               [^number width ^number height] :size
                               :as params}]
  (let [^number scroll-x (get-scroll-fn :x 0)
        ^number scroll-y (get-scroll-fn :y 0)
        ^number focus-row-idx (get-config-fn ws-api/focus-row-idx-key)]
    [virt-grid (cond-> {:width width
                        :height (table-body-height height)
                        :className table-body-scrollable-parent-class
                        :columnWidth 120
                        :columnCount column-count
                        :rowCount row-count
                        :rowHeight body-row-height
                        :on-scroll #(let [^number new-scroll-x (aget % "scrollLeft")
                                          ^number new-scroll-y (aget % "scrollTop")]
                                      (when-not (= new-scroll-x scroll-x)
                                        (set-config-fn ws-api/scroll-x-key new-scroll-x)
                                        (delayed-log-fn))
                                      (when-not (= new-scroll-y scroll-y)
                                        (set-config-fn ws-api/scroll-y-key new-scroll-y)
                                        (delayed-log-fn)))
                        :scroll-Left scroll-x
                        :scroll-Top scroll-y
                        :cellRenderer (fn [a]
                                        (let [^string row-key (aget a "key")
                                              ^number column-index (aget a "columnIndex")
                                              ^number row-index  (aget a "rowIndex")
                                              style (aget a "style")
                                              cell-data (attribute-value-access-fn column-index row-index)]

                                          (body-cell-renderer row-key
                                                              row-index
                                                              style
                                                              table-body-scrollable-cell-class
                                                              cell-data
                                                              params)))}
                 ;must be set here, because otherwise scroll-top will be overwrited to 0 when focus-row-idx is nil
                 focus-row-idx (assoc :scroll-to-row focus-row-idx))]))

(defn- page-selection [state {:keys [set-config-fn get-config-fn request-data-fn read-only?]}]
  (let [^number last-page (get-config-fn ws-api/last-page-key 1)
        ^number current-page (get-config-fn ws-api/current-page-key 1)
        {:keys [thousand-separator decimal-separator of-label aria-label-current-page]}
        @(re-frame/subscribe [::i18n/translate-multi :of-label :thousand-separator :decimal-separator :aria-label-current-page])
        page-selection-timeout (atom nil)
        timeout-fn (partial timeout/handle-timeout page-selection-timeout 500)
        set-page-fn (fn [new-page]
                      (set-config-fn ws-api/current-page-key new-page)
                      (set-config-fn ws-api/scroll-y-key 0)
                      (request-data-fn))]
    [:div.paging__selection
     [button-group
      {:disabled? (or read-only? false)
       :items [{:id :previous
                :label [icon {:icon :prev
                              :color (if (= current-page 1)
                                       :gray
                                       :blue)}]
                :disabled? (= current-page 1)
                :on-click #(set-page-fn (dec current-page))}
               {:id :currentpage
                :extra-props {:class "paging__selection__page__parent"}
                :label [input-field
                        {:value current-page
                         :type :number
                         :aria-label aria-label-current-page
                         :disabled? read-only?
                         :decimal-separator decimal-separator
                         :thousand-separator thousand-separator
                         :extra-class "paging__selection__page__input__field"
                         :on-key-down (fn [e]
                                        (when (#{"." ","} (aget e "key"))
                                          (.preventDefault e)))
                         :on-input (fn [e]
                                     (timeout-fn
                                      (fn [_]
                                        (let [num-raw (aget e "nativeEvent" "srcElement" "value")
                                              num (try
                                                    (js/parseInt num-raw)
                                                    (catch :default _))]
                                          (cond
                                            (and (int? num)
                                                 (<= 1 num last-page))
                                            (set-page-fn num)
                                            (and (int? num)
                                                 (not (<= 1 num last-page)))
                                            (aset e "nativeEvent" "srcElement" "value" current-page))))))}]}
               {:id :last-page
                :extra-props {:class "paging__selection__page__limit"}
                :disabled? true
                :label (str of-label " " (i18n/localized-number last-page))}
               {:id :next
                :label [icon {:icon :next
                              :color (if (= current-page last-page)
                                       :gray
                                       :blue)}]
                :disabled? (= current-page last-page)
                :on-click #(set-page-fn (inc current-page))}]
       :active-item current-page}]]))

(defn- page-size-selection [state {:keys [read-only? set-config-fn get-config-fn request-data-fn]}]
  (let [^number page-size (get-config-fn ws-api/page-size-key config/default-page-size)
        page-size-label @(re-frame/subscribe [::i18n/translate :page-size])]
    [:div.page__size__selection
     [select {:label page-size-label
              :disabled? (or read-only? false)
              :on-change (fn [option]
                           (set-config-fn ws-api/page-size-key (select-utils/normalize option))
                           (set-config-fn ws-api/current-page-key 1)
                           (request-data-fn))
              :values (select-utils/to-option page-size (i18n/localized-number page-size))
              :is-multi? false
              :is-clearable? false
              :options (mapv (fn [possible-page-size]
                               {:value possible-page-size
                                :label (i18n/localized-number possible-page-size)})
                             config/available-page-sizes)
              :label-params {:extra-class "input--w5"}
              :extra-class "input--w5"}]]))

(defn- footer [state  params]
  [:div {:class table-footer-parent-class}
   [page-size-selection state params]
   [page-selection state params]])

(defn- column-access [columns ^number column-index]
  (attrs/access-key (get columns column-index)))

(defn- attribute-value-access [frame-id column-access-fn ^number column-index ^number row-index]
  (-> (table-data/frame-single-event-data frame-id row-index)
      (attrs/value (column-access-fn column-index))))

(defn- is-row-selected? [frame-id selected-event-ids ^number row-index]
  (boolean
   (selected-event-ids
    (-> (table-data/frame-single-event-data frame-id row-index)
        (attrs/value (attrs/access-key "id"))))))

(defn- table-content [state {:keys [frame-id size
                                    ^number column-count ^number row-count]
                             :as params}]
  (cond
    (and size
         column-count
         row-count
         (> row-count 0))
    [:div
     [scrollable-header state params]
     [scrollable-body state params]
     [footer state params]]

    :else
    [empty/empty-component frame-id {:counts-sub (re-frame/subscribe [::frame-header/get-counts frame-id])}]))

(defn- on-cell-click [double-click-timeout frame-id read-only?
                      ^number row-index e]
  (when-not read-only?
    (when-let [old-timeout @double-click-timeout]
      (js/clearTimeout old-timeout)
      (reset! double-click-timeout nil))
    (reset! double-click-timeout
            (js/setTimeout
             #(re-frame/dispatch
               [::table-state/click-select-handler frame-id (table-data/frame-single-event-data frame-id row-index)])
             config/double-click-timeout))))

(defn- on-cell-double-click [double-click-timeout frame-id read-only?
                             ^number row-index e]
  (when-not read-only?
    (when-let [old-timeout @double-click-timeout]
      (js/clearTimeout old-timeout)
      (reset! double-click-timeout nil))
    (when-let [event-data (table-data/frame-single-event-data frame-id row-index)]
      (re-frame/dispatch [::details/prepare-add-to-details-view
                          frame-id
                          [(attrs/value event-data (attrs/access-key "bucket"))
                           (attrs/value event-data (attrs/access-key "id"))]
                          (aget e "nativeEvent")]))
    (.preventDefault e)
    (.stopPropagation e)))

(defn- request-data [frame-id ^boolean read-only?]
  (when-not read-only?
    (table-data/set-frame-table-config frame-id ws-api/focus-row-idx-key nil)
    (table-data/set-frame-table-config frame-id ws-api/focus-event-id-key nil)
    (re-frame/dispatch (table-data/request-data-event-vec frame-id))))

(defn- log-table-state [frame-id logging-timeout-state read-only?]
  (when-let [old-timeout @logging-timeout-state]
    (js/clearTimeout old-timeout)
    (reset! logging-timeout-state nil))
  (when-not read-only?
    (reset! logging-timeout-state
            (js/setTimeout
             #(do
                (re-frame/dispatch (table-data/logging-event-vec frame-id)))
             config/table-delayed-logging-timeout))))

(defn- table-view-impl [{:keys [logging-timeout-state double-click-timeout] :as state} frame-id infos-sub vis-desc]
  (let [{:keys [size]} (val-or-deref infos-sub)
        columns (table-data/frame-columns frame-id)
        ^boolean read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                          {:frame-id frame-id})
        ^boolean project-loading? @(fi/call-api :project-loading-sub)
        ^number column-count (count columns)
        ^number available-data-count (table-data/frame-table-single-config frame-id ws-api/row-count-key)
        column-access-fn (partial column-access columns)
        attribute-value-access-fn (partial attribute-value-access frame-id column-access-fn)
        selected-event-ids @(re-frame/subscribe [::table-state/selected-ids frame-id])
        sub-component-params {:set-config-fn (partial table-data/set-frame-table-config frame-id)
                              :get-config-fn (partial table-data/frame-table-single-config frame-id)
                              :get-scroll-fn (fn [^keyword orientation default-value]
                                               (case orientation
                                                 :x (or @(table-data/frame-scroll-x frame-id) default-value)
                                                 :y (or @(table-data/frame-scroll-y frame-id) default-value)
                                                 nil))
                              :read-only? (or read-only? project-loading?)
                              :row-count available-data-count
                              :column-count column-count
                              :column-access-fn column-access-fn
                              :frame-id frame-id
                              :is-row-selected? (partial is-row-selected? frame-id selected-event-ids)
                              :size size
                              :attribute-value-access-fn attribute-value-access-fn
                              :on-double-click (when-not read-only?
                                                 (partial on-cell-double-click double-click-timeout frame-id read-only?))

                              :on-click (when-not read-only?
                                          (partial on-cell-click double-click-timeout frame-id read-only?))
                              :request-data-fn (partial request-data frame-id read-only?)
                              :delayed-log-fn (partial log-table-state frame-id logging-timeout-state (or read-only? project-loading?))}]
    [table-content state sub-component-params]))

(defn render-done [frame-id]
  (re-frame/dispatch
   (fi/call-api :render-done-event-vec frame-id (str config/default-namespace " - table"))))

(defn table-view [frame-id infos-sub vis-desc]
  (let [state {:double-click-timeout (atom nil)
               :logging-timeout-state (atom nil)}]
    (r/create-class
     {:component-did-mount (fn []
                             (let [row-count (table-data/frame-table-single-config frame-id ws-api/row-count-key)
                                   loading? @(re-frame/subscribe [::queue-util/loading? frame-id])]
                               (when (and vis-desc (not loading?) (not row-count))
                                 (re-frame/dispatch [:de.explorama.frontend.table.vis-state/restore-vis-desc frame-id vis-desc]))
                               (render-done frame-id)))

      :reagent-render
      (fn [frame-id infos-sub vis-desc]
        [table-view-impl state frame-id infos-sub vis-desc])})))