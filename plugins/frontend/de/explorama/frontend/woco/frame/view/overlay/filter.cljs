(ns de.explorama.frontend.woco.frame.view.overlay.filter
  (:require [clojure.string :as cljstr]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                            date-picker
                                                                            input-field select]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :refer [warn]]
            [de.explorama.frontend.woco.frame.filter.core :as fcore]
            [de.explorama.frontend.woco.frame.filter.util :as util]
            [de.explorama.frontend.woco.path :as path]))

(re-frame/reg-sub
 ::filter-ui-descs
 (fn [db [_ frame-id]]
   (let [filter-desc (get-in db (path/frame-filter frame-id))]
     (when (:show? filter-desc)
       filter-desc))))

(re-frame/reg-event-db
 ::update-filter-ui
 (fn [db [_ path value]]
   (assoc-in db path value)))

(re-frame/reg-sub
 ::filter-desc
 (fn [db [_ frame-id]]
   (get-in db (path/frame-filter frame-id))))

(re-frame/reg-sub
 ::data-acs
 (fn [db [_ frame-id]]
   (fcore/get-data-acs db frame-id)))

(re-frame/reg-sub
 ::filter-active?
 (fn [[_ frame-id]]
   (re-frame/subscribe [::filter-desc frame-id]))
 (fn [filter-desc _]
   (get filter-desc :active?)))

(re-frame/reg-sub
 ::data-acs-value
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::data-acs frame-id])])
 (fn [[data-acs] [_ _ attr constraint-key]]
   (get-in data-acs [attr constraint-key])))

(re-frame/reg-sub
 ::selected-ui
 (fn [[_ frame-id]]
   (re-frame/subscribe [::filter-desc frame-id]))
 (fn [filter-desc _]
   (get filter-desc :selected-ui)))

(re-frame/reg-sub
 ::selected-ui-value
 (fn [[_ frame-id]]
   (re-frame/subscribe [::filter-desc frame-id]))
 (fn [selected-ui [_ _ attr constraint-key]]
   (get-in selected-ui [:selected-ui attr constraint-key])))

(re-frame/reg-sub
 ::selected-ui-attr
 (fn [[_ frame-id]]
   (re-frame/subscribe [::filter-desc frame-id]))
 (fn [filter-desc [_ _ attr constraint-key]]
   (get-in filter-desc [:selected-ui attr constraint-key])))

(re-frame/reg-sub
 ::selected-ui-attributes
 (fn [[_ frame-id]]
   (re-frame/subscribe [::filter-desc frame-id]))
 (fn [filter-desc _]
   (get filter-desc :selected-ui-attributes [])))

(re-frame/reg-sub
 ::check-attribute-already-added
 (fn [[_ frame-id]]
   (re-frame/subscribe [::selected-ui-attributes frame-id]))
 (fn [selected-ui-attrs [_ _ attr constraint-key]]
   (boolean ((set selected-ui-attrs) [attr constraint-key]))))

(defn scroll-to-end [scroll-container-id]
  (.setTimeout js/window
               (fn [_]
                 (when-let [div-con (.getElementById js/document scroll-container-id)]
                   (aset div-con
                         "scrollTop"
                         (aget div-con "scrollHeight"))))
               100))


(re-frame/reg-event-db
 ::selected-ui-attribute
 (fn [db [_ frame-id attr constraint-key]]
   (let [data-acs (fcore/get-data-acs db frame-id)
         attr-type (get-in data-acs [attr constraint-key :type])]
     (cond-> db
       :always (update-in (vec (conj (path/frame-filter frame-id)
                                     :selected-ui-attributes))
                          (fn [o]
                            (-> (or o [])
                                (conj [attr constraint-key]))))
       (= attr-type :string) (assoc-in (vec (conj (path/frame-filter frame-id)
                                                  :selected-ui
                                                  attr
                                                  constraint-key))
                                       [])))))

(re-frame/reg-event-db
 ::delete-selected-ui-attr
 (fn [db [_ frame-id attr constraint-key]]
   (fcore/remove-constraint db frame-id attr constraint-key)))

(defn row-valid? [db frame-id attr constraint-key]
  (try
    (let [data-acs (fcore/get-data-acs db frame-id)
          {attr-type :type :as data-ac} (get-in data-acs [attr constraint-key])
          d (js->clj (get-in db (path/frame-filter-selected-ui-row frame-id attr constraint-key)))]
      (boolean
       (and d
            (or (and (vector? d)
                     (not-empty d)
                     ;Check number in correct range
                     (or (and (#{:number :year} attr-type)
                              (let [[min-ac max-ac] (:vals data-ac)]
                                (and
                                 (<= (first d) (second d)) ; min val not hihger then current max
                                 (<= min-ac (first d) max-ac) ; min-val between min/max from ac
                                 (<= min-ac (second d) max-ac)))) ; max-val between min/max from ac
                         (not (#{:number :year} attr-type))))
                (and (map? d)
                     (not-empty d))
                (and (string? d)
                     (not (cljstr/blank? d)))))))
    (catch :default e
      (warn e)
      false)))

(re-frame/reg-sub
 ::row-valid?
 (fn [db [_ frame-id attr constraint-key]]
   (row-valid? db frame-id attr constraint-key)))

(re-frame/reg-sub
 ::all-rows-valid?
 (fn [db [_ frame-id attributes]]
   (try
     (every? (fn [[attr constraint-key]] (row-valid? db frame-id attr constraint-key))
             attributes)
     (catch :default e
       (warn e)
       false))))

(defn on-change-func [path event]
  (re-frame/dispatch [::update-filter-ui path event]))

(defn combobox [name options selected-values value-change-path]
  (let [select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        opts (vec (sort-by :label options))]
    (with-meta
      [select {:autofocus? true
               :placeholder select-placeholder
               :extra-class "input--w100"
               :options opts
               :is-multi? true
               :close-on-select? false
               :values selected-values
               :on-change #(on-change-func value-change-path %)}]
      {:key (str value-change-path "-gcombo" name)})))

(defn number-is-int? [num]
  (try (js/Number.isInteger num)
       (catch :default _
         false)))

(defn count-decimal [num]
  (try
    (-> (cljstr/split (str num)
                      ".")
        second
        count)
    (catch :default _
      0)))

(defn calc-decimal-step [min-ac max-ac min-val max-val]
  (try
    (min 1
         (/ 1 (js/Math.pow 10
                           (max (count-decimal min-ac)
                                (count-decimal min-val)
                                (count-decimal max-ac)
                                (count-decimal max-val)))))
    (catch :default _
      1)))

(defn as-options [elements]
  (mapv #(hash-map :value % :label %) elements))

(defn year-range [frame-id attr constraint-key]
  (let [select-placeholder @(re-frame/subscribe [::i18n/translate :select-placeholder])
        value-change-path (conj (path/frame-filter frame-id) :selected-ui attr constraint-key)
        {[min-ac max-ac] :vals} @(re-frame/subscribe [::data-acs-value frame-id attr constraint-key])
        [min-ui max-ui] @(re-frame/subscribe [::selected-ui-value frame-id attr constraint-key])
        min-val (if (or (js/isNaN min-ui)
                        (string? min-ui))
                  min-ac
                  min-ui)
        max-val (if (or (js/isNaN max-ui)
                        (string? max-ui))
                  max-ac
                  max-ui)
        step-size (if (and (number-is-int? min-ac)
                           (number-is-int? min-val)
                           (number-is-int? max-ac)
                           (number-is-int? max-val))
                    1
                    (calc-decimal-step min-ac max-ac min-val max-val))
        min-options (as-options (range min-ac (inc (if max-val max-val max-ac)) step-size))
        max-options (as-options (range (if min-val min-val min-ac) (inc max-ac) step-size))]
    [:div.flex.gap-8
     [select
      {:placeholder select-placeholder
       :on-change   #(on-change-func (conj value-change-path 0) (:value %))
       :values      {:label min-val}
       :options     min-options}]
     [icon {:icon :minus}]
     [select
      {:placeholder select-placeholder
       :on-change   #(on-change-func (conj value-change-path 1) (:value %))
       :values      {:label max-val}
       :options     max-options}]]))

(defn num-range [frame-id attr constraint-key]
  (let [thousand-sep @(re-frame/subscribe [::i18n/translate :thousand-separator])
        decimal-sep @(re-frame/subscribe [::i18n/translate :decimal-separator])
        lang @(re-frame/subscribe [::i18n/current-language])
        value-change-path (conj (path/frame-filter frame-id) :selected-ui attr constraint-key)
        {[min-ac max-ac] :vals} @(re-frame/subscribe [::data-acs-value frame-id attr constraint-key])
        [min-ui max-ui] @(re-frame/subscribe [::selected-ui-value frame-id attr constraint-key])
        min-val (if (or (js/isNaN min-ui)
                        (string? min-ui))
                  min-ac
                  min-ui)
        max-val (if (or (js/isNaN max-ui)
                        (string? max-ui))
                  min-ac
                  max-ui)
        step-size (if (and (number-is-int? min-ac)
                           (number-is-int? min-val)
                           (number-is-int? max-ac)
                           (number-is-int? max-val))
                    1
                    (calc-decimal-step min-ac max-ac min-val max-val))]
    [:div.flex.gap-8
     [input-field
      {:on-change #(on-change-func value-change-path [% max-val])
       :on-clear #(on-change-func value-change-path [min-ac max-ac])
       :type :number
       :thousand-separator thousand-sep
       :decimal-separator decimal-sep
       :language lang
       :step step-size
       :max max-val
       :min min-ac
       :value min-val}]
     [icon {:icon :minus}]
     [input-field
      {:on-change #(on-change-func value-change-path [min-val %])
       :on-clear #(on-change-func value-change-path [min-ac max-ac])
       :type :number
       :thousand-separator thousand-sep
       :decimal-separator decimal-sep
       :language lang
       :step step-size
       :max max-ac
       :min min-val
       :value max-val}]]))

(defn date-range [min-date max-date start-date end-date start-change-path end-change-path]
  [error-boundary
   (let [lang (name @(re-frame/subscribe [::i18n/current-language]))
         dstr->date (fn [dstr]
                      (-> (util/date<- dstr)
                          (util/moment->date)))
         start-date (util/date<- start-date)
         end-date (util/date<- end-date)
          ;To prevent overlaps, when end-date is before start-date
         start-date (if (util/is-before? start-date end-date)
                      start-date
                      end-date)
          ;To prevent overlaps, when start-date is after end-date
         end-date (if (util/is-after? end-date start-date)
                    end-date
                    start-date)
         max-date (util/date<- max-date)
         min-date (util/date<- min-date)
          ;To prevent crashes, when default Value is not in Range
         min-date (if (util/is-before? start-date min-date)
                    start-date
                    min-date)
         max-date (if (util/is-after? end-date max-date)
                    end-date
                    max-date)]
     [date-picker (cond-> {:lang lang
                           :placeholder util/date-format
                           :range? true
                           :on-format-date (fn [dobj]
                                             (-> (util/date->moment dobj)
                                                 (util/date->)))
                           :on-parse-date (fn [dstr]
                                            (-> (util/date<- dstr)
                                                (util/moment->date)))
                           :on-change (fn [drange]
                                        (let [[sdate edate] (js->clj drange)]
                                          (when sdate
                                            (on-change-func start-change-path (util/date->moment sdate)))
                                          (when edate
                                            (on-change-func end-change-path (util/date->moment edate)))))}
                    (and start-date end-date)
                    (assoc :value [(dstr->date start-date)
                                   (dstr->date end-date)])
                    min-date (assoc :min-date (dstr->date min-date))
                    max-date (assoc :max-date (dstr->date max-date)))])])

(defn convert-to-string [element]
  (cond (aget element "label")
        (aget element "label")
        (implements? IMapEntry element)
        (let [[key value] element]
          (str (name key) ": " (let [date (util/date<- value)]
                                 (if (.isValid date)
                                   (util/date-> date) ;(.format date "YYYY-MM-DD"))
                                   value))))
        :else element))

(defn text-search [ui-val change-path]
  [input-field {:value ui-val
                :extra-class "input--w100"
                :on-change #(on-change-func change-path %)}])

(defn filter-row-component [data-acs frame-id [key constraint-key ui-value]]
  (let [{actype :type acvals :vals text-search? :text-search?}
        (get-in data-acs [key constraint-key])
        base-path (path/frame-filter frame-id)]
    (cond
      (nil? actype)
      [:<>
       (str (cljstr/join ", " (map convert-to-string (take 2 @ui-value)))
            (when (< 2 (count @ui-value))
              util/prune-char))]
      (= :number actype)
      [num-range frame-id
       key
       constraint-key]
      (= :year actype)
      [year-range frame-id
       key
       constraint-key]
      (and (= :string actype)
           text-search?)
      [text-search ui-value (conj base-path
                                  :selected-ui
                                  key
                                  constraint-key)]
      (#{:string} actype)
      [combobox (name key)
       (fcore/options<- acvals)
       ui-value
       (conj base-path
             :selected-ui
             key
             constraint-key)]
      (#{:date} actype)
      (let [[min-date max-date] acvals
            {:keys [start-date end-date]} @ui-value
            start-path (conj base-path
                             :selected-ui
                             key
                             constraint-key
                             :start-date)
            end-path (conj base-path
                           :selected-ui
                           key
                           constraint-key
                           :end-date)]
        [date-range min-date max-date start-date end-date start-path end-path])
      :else key)))

(defn search-attribute-row [frame-id attr constraint-key data-acs]
  (let [ui-value (re-frame/subscribe [::selected-ui-attr frame-id attr constraint-key])
        value-selected? @(re-frame/subscribe [::row-valid? frame-id attr constraint-key])
        labels @(fi/call-api [:i18n :get-labels-sub])
        attr->display-name (fn [attr] (get labels attr attr))]
    [error-boundary
     [:div {:class (cond-> "search__block"
                     (not value-selected?)
                     (str " search__block--error"))}
      [:div.search__block__label
       [:label {:for "input-select"
                :class "explorama__form__label"}
        (attr->display-name (if (= constraint-key :std)
                              attr
                              (name constraint-key)))]]
      [:div.search__block__input
       [filter-row-component data-acs frame-id [attr constraint-key ui-value]]]
      [:div.search__block_actions
       [button {:variant :tertiary
                :disabled? (empty? data-acs)
                :aria-label :delete-label
                :start-icon :trash
                :size :small
                :on-click #(re-frame/dispatch [::delete-selected-ui-attr frame-id attr constraint-key])}]]]]))

(defn search-attribute-element [frame-id scroll-container-id attr constraint-key constraint-label]
  (let [is-active @(re-frame/subscribe [::check-attribute-already-added frame-id attr constraint-key])
        class (when is-active "active")]
    [:li {:class class
          :on-click #(when (not is-active)
                       (re-frame/dispatch [::selected-ui-attribute frame-id attr constraint-key])
                       (scroll-to-end scroll-container-id))}
     constraint-label]))


(defn- sorted-attributes [attrs]
  (let [labels @(fi/call-api [:i18n :get-labels-sub])
        attr->display-name (fn [attr] (get labels attr attr))]
    (sort-by (fn [{:keys [constraint-label]}]
               (cljstr/lower-case constraint-label))
             (reduce (fn [acc [attr constraints]]
                       (reduce (fn [acc [constraint-key {constraint-name :name}]]
                                 (conj acc
                                       {:attribute attr
                                        :constraint-key constraint-key
                                        :constraint-label (attr->display-name (if constraint-name
                                                                                "year"
                                                                                (name attr)))}))
                               acc
                               constraints))
                     []
                     attrs))))

(defn attributes-selection-bar [frame-id scroll-container-id]
  (let [attributes-title @(re-frame/subscribe [::i18n/translate :constraints-attributes-title])
        attributes (sorted-attributes @(re-frame/subscribe [::data-acs frame-id]))]
    [:div {:class "search__sidebar"}
     [:h1 attributes-title]
     (reduce
      (fn [par {:keys [attribute constraint-key constraint-label]}]
        (conj par
              (with-meta
                [search-attribute-element frame-id scroll-container-id attribute constraint-key constraint-label]
                {:key (str scroll-container-id attribute constraint-key constraint-label)})))
      [:ul]
      attributes)]))

(defn constraints-creation-view [frame-id scroll-container-id]
  (let [attribute-select-hint @(re-frame/subscribe [::i18n/translate :constraints-attribute-select-hint])
        apply-button-label @(re-frame/subscribe [::i18n/translate :constraints-apply-button-label])
        remove-button-label @(re-frame/subscribe [::i18n/translate :constraints-remove-button-label])
        {:keys [selected-ui-attributes last-applied-filters]} @(re-frame/subscribe [::filter-desc frame-id])
        data-acs @(re-frame/subscribe [::data-acs frame-id])
        attributes (not-empty selected-ui-attributes)
        all-rows-valid? @(re-frame/subscribe [::all-rows-valid? frame-id attributes])
        apply-button-disabled?  (or (empty? data-acs)
                                    (empty? attributes)
                                    (not all-rows-valid?))]
    [:div {:class "search__main"}
     [:div {:id scroll-container-id
            :class "search__container"}
      (if attributes
        (reduce (fn [par [attr constraint-key]]
                  (conj par [search-attribute-row frame-id attr constraint-key data-acs]))
                [:div {:class "search__section"}]
                attributes)
        [:div.search__section
         [:div.explorama__search__block
          [:div.explorama__form__row]
          [:div.col-2]
          [:div.col-8 [:span attribute-select-hint]]]])]
     [:div.search__actions
      [:div.explorama__search__actions
       [button {:disabled? apply-button-disabled?
                :start-icon :search
                :label apply-button-label
                :on-click #(do
                             (.preventDefault %)
                             (re-frame/dispatch [::fcore/apply-filters-and-close frame-id]))}]
       "      "
       "      "
       [button {:variant :secondary
                :label remove-button-label
                :start-icon :trash
                :disabled?  (or (empty? data-acs)
                                (and (empty? attributes)
                                     (empty? last-applied-filters)))
                :on-click #(re-frame/dispatch [::fcore/clear-filters-and-close frame-id])}]]]]))

(defn frame-filter [frame-id]
  (let [opened (r/atom false)]
    (fn [frame-id]
      (when-let [show-desc @(re-frame/subscribe [::filter-ui-descs frame-id])]
        (let [data-acs @(re-frame/subscribe [::data-acs frame-id])]
          (if data-acs
            (let [scroll-container-id (str "woco_searchformend-" frame-id)]
              (when @opened ; TODO r11/filter hack solution to initialize the filter view when opened "too" early
                (re-frame/dispatch [:de.explorama.frontend.woco.frame.filter.core/init-filter frame-id])
                (reset! opened false))
              [:div.constrainview__overlay
               [:div.constrain__canvas.explorama__window--search {:key (str "woco-filter" frame-id)
                                                                  :id (str "woco-filter" frame-id)}
                [:a {:href "#"}
                 [:span.constrain__close
                  {:on-click (fn [e]
                               (.preventDefault e)
                               (re-frame/dispatch [::fcore/hide frame-id])
                               (re-frame/dispatch [::fcore/restore-filter-desc frame-id]))}]]
                [:div.window__wrapper__search
                 [attributes-selection-bar frame-id scroll-container-id]
                 [constraints-creation-view frame-id scroll-container-id]]]])
            (do
              (when-not @opened
                (reset! opened true))
              [:div.constrainview__overlay
               [:div.constrain__canvas.explorama__window--search {:key (str "woco-filter" frame-id)
                                                                  :id (str "woco-filter" frame-id)}
                [:a {:href "#"}
                 [:span.constrain__close
                  {:on-click (fn [e]
                               (.preventDefault e)
                               (re-frame/dispatch [::fcore/hide frame-id])
                               (re-frame/dispatch [::fcore/restore-filter-desc frame-id]))}]]
                [:div.window__wrapper__search
                 [loading-screen
                  {:show? true,
                   :message (re-frame/subscribe [::i18n/translate :loading-screen-message])
                   :tip (re-frame/subscribe [::i18n/translate :loading-screen-tip])
                   :tip-titel (re-frame/subscribe [::i18n/translate :loading-screen-tip-titel])}]]]])))))))
