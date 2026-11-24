(ns de.explorama.frontend.ui-base.components.formular.collapsible-list
  (:require [reagent.core :as r]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.specification :refer [parameters->malli validate]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary virtualized-list]]))

(def parameter-definition
  {:row-height {:type [:number]
                :desc "Height of every row in px. Must be the same for all rows"}
   :default-open-id {:type [:any]
                     :desc "Id(s) of item(s) which should be open at mounting"}
   :multiple-open? {:type :boolean
                    :desc "If true multiple items can be collapse"}
   :open-all? {:type [:derefable :boolean]
               :desc "If true all items will be collapse"}
   :items {:type [:derefable :vector]
           :required true
           :desc "(Parent) Items which will be displayed"}
   :collapse-items-fn {:type [:function]
                       :default-fn-str "(fn [parent-id])"
                       :desc "Function to get childs from parent item. The function can return an derefable like re-frame subscription"}
   :on-click {:type [:function]
              :default-fn-str "(fn [item])"
              :desc "Fired when a child (or parent which is not collapsable)"}
   :disabled? {:type [:derefable :boolean]
               :desc "If true, open/closing is disabled"}})
(def specification (parameters->malli parameter-definition nil))
(def default-parameters {:row-height 40
                         :disabled? false
                         :multiple-open? false
                         :open-all? false})

(def ^:private disabled-class "disabled")

;; tailwind: migrated from styles/src/scss/components/_collapsible_list.scss.
;; `list-item`/`disabled` stay literal DOM classes: base/_themes.scss's
;; forced-colors block (`@media (forced-colors: active)`) still selects
;; `.list-item:not(.disabled)` directly and hasn't been migrated yet (cross-
;; sheet marker, same situation as tooltip.cljs's `tooltip-wrapper`). `open`/
;; `child` had zero external referrers (grepped styles/src/scss/,
;; plugins/**, bundles/*/frontend), so -- like hint.cljs's `.hint-error`/
;; `.hint-warning` -- they're folded into full literal utility-class picks
;; below instead of staying as DOM marker classes.
;; `flex!` (not `flex`): Tailwind ships its own `list-item` utility
;; (`display: list-item`, one of the CSS `display` keywords) which the
;; kept-literal `list-item` marker class above makes Tailwind's scanner
;; treat as a genuine utility candidate. Both `.flex`/`.list-item` land in
;; the same unlayered utilities sheet at equal specificity (0,1,0); `.list-item`
;; is generated after `.flex` (verified: dist/css/5_utilities.css line 322 vs
;; 337), so on source order alone it silently wins and the row collapses out
;; of flex layout. `!important` makes the real intent unconditional.
(def ^:private list-item-class
  "flex! items-center gap-1 relative p-2.5 border-b border-b-(--border-secondary) bg-(--bg) text-sm transition-[background-color] duration-[120ms] ease-[ease] hover:not-[.disabled]:bg-(--bg-hover) hover:not-[.disabled]:cursor-pointer active:not-[.disabled]:scale-97")
(def ^:private list-item-disabled-class "text-(--text-disabled)")
(def ^:private list-item-open-class "shadow-sm z-1")
(def ^:private list-item-child-class "border-l-4 border-l-(--border)")
(def ^:private item-label-class "truncate")
(def ^:private item-icon-class "ml-auto")
(def ^:private item-icon-span-class "bg-(--icon)")
(def ^:private item-icon-span-disabled-class "bg-(--icon-disabled)")

(defn- entry-icon [open? disabled?]
  [icon {:icon (if open?
                 :collapse-open
                 :collapse-closed)
         :extra-class (if disabled?
                        item-icon-span-disabled-class
                        item-icon-span-class)}])

(defn- parent-entry [style
                     {:keys [open? idx id label collapsible?]
                      :or {collapsible? true}
                      disable-item? :disabled?
                      :as item}
                     {:keys [disabled? on-open on-click]}]
  (let [disabled? (val-or-deref (or disable-item? disabled?))]
    [:div.list-item (cond-> {:style style
                             :role "row"
                             :class [list-item-class]}
                      (and collapsible? (not disabled?))
                      (assoc :on-click #(on-open idx id))
                      (and (not collapsible?)
                           (not disabled?)
                           (fn? on-click))
                      (assoc :on-click #(on-click (dissoc item :open? idx)))
                      open? (update :class conj list-item-open-class)
                      disabled? (update :class conj disabled-class list-item-disabled-class))
     [:div {:class item-label-class :title label :role "gridcell"}
      label]
     (when collapsible?
       [:div {:class item-icon-class :role "gridcell"}
        [entry-icon open? disabled?]])]))

(defn- entry [style
              {:keys [label]
               disable-item? :disabled?
               :as item}
              {:keys [disabled? on-click]}]
  (let [disabled? (val-or-deref (or disable-item? disabled?))]
    [:div.list-item (cond-> {:style style
                             :class [list-item-class list-item-child-class]}
                      (and (not disabled?)
                           (fn? on-click))
                      (assoc :on-click #(on-click (dissoc item :idx)))
                      disabled?
                      (update :class conj disabled-class list-item-disabled-class))
     [:div {:class item-label-class :title label}
      label]]))

(defn- calc-rows [open-item childs items multiple-open? open-all?]
  (if (not open-item)
    (mapv (fn [[idx item]]
            (assoc item
                   :idx idx
                   :is-parent? true))
          (map-indexed vector items))
    (-> (reduce (fn [{:keys [global-idx] :as acc} {item-val :id :as item}]
                  (-> acc
                      (update :global-idx
                              (fn [^number old-idx]
                                (let [old-idx (inc old-idx)]
                                  (cond
                                    open-all?
                                    (+ old-idx (count (get childs item-val [])))
                                    (and multiple-open? (open-item item-val))
                                    (+ old-idx (count (get childs item-val [])))
                                    :else old-idx))))
                      (update :items
                              (fn [acc]
                                (cond
                                  (or (open-item item-val)
                                      (and open-all? (get childs item-val)))
                                  (apply conj!
                                         (conj! acc
                                                (assoc item
                                                       :idx global-idx
                                                       :is-parent? true
                                                       :open? true))
                                         (map #(assoc % :parent item-val)
                                              (get childs item-val)))
                                  (not (open-item item-val))
                                  (conj! acc (assoc item
                                                    :idx global-idx
                                                    :is-parent? true))
                                  :else acc)))))
                {:items (transient [])
                 :global-idx 0}
                items)
        (get :items)
        (persistent!))))

(defn- list-impl [open-state scroll-to-idx
                  {:keys [open-all? multiple-open?
                          row-height collapse-items-fn items]
                   :as params}]
  (let [open-elems @open-state
        open-all? (val-or-deref open-all?)
        items (val-or-deref items)
        scroll-to-idx @scroll-to-idx
        parent? (set (map :id items))
        childs (when (and (or open-elems open-all?)
                          (fn? collapse-items-fn))
                 (reduce (fn [acc open-elem-id]
                           (assoc acc open-elem-id
                                  (val-or-deref (collapse-items-fn open-elem-id))))
                         {}
                         (if open-all?
                           (map :id items)
                           open-elems)))
        rows (calc-rows open-elems childs items multiple-open? open-all?)]
    [virtualized-list
     (cond-> {:row-height row-height
              :full-width? true
              :full-height? true
              :rows rows
              :extra-class "collapsible-list"
              :row-renderer (fn [key idx style item]
                              (r/as-element
                               (if (parent? (:id item))
                                 (with-meta
                                   [parent-entry style item params]
                                   {:key (str ::list-parent key idx)})
                                 (with-meta
                                   [entry style item params]
                                   {:key (str ::list-child key idx)}))))}

       scroll-to-idx
       (assoc :scroll-to-index scroll-to-idx))]))

(defn ^:export collapsible-list [{:keys [default-open-id]}]
  (let [open-state (r/atom (cond
                             (coll? default-open-id) (set default-open-id)
                             default-open-id #{default-open-id}
                             :else #{}))
        scroll-to-idx (r/atom nil)]
    (r/create-class
     {:display-name "collapsible-list"
      :reagent-render
      (fn [params]
        (let [{:keys [multiple-open?] :as params}
              (merge default-parameters params)]
          [error-boundary {:validate-fn #(validate "collapsible-list" specification params)}
           [list-impl
            open-state
            scroll-to-idx
            (assoc params
                   :on-open (fn [idx value]
                              (swap! open-state (fn [old-state]
                                                  (cond
                                                    (old-state value)
                                                    (disj old-state value)
                                                    multiple-open?
                                                    (conj old-state value)
                                                    :else #{value})))
                              (reset! scroll-to-idx idx)))]]))})))
