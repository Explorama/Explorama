(ns de.explorama.frontend.algorithms.components.legend
  (:require [data-format-lib.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.utils.colors :refer [css-RGB-string
                                                    RGB->svg-color-matrix]]
            [goog.string :as gstring]
            [goog.string.format]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.algorithms.components.charts :as charts]
            [de.explorama.frontend.algorithms.components.result :as result]
            [de.explorama.frontend.algorithms.path.core :as path]))

(defn i18n-sub [i18n-key]
  (re-frame/subscribe [::i18n/translate i18n-key]))

(defn- first-indices
  ([coll] (first-indices identity coll))
  ([f coll]
   (loop [xs (seq coll), i 0, indices (transient {})]
     (if-let [[x & xs] xs]
       (let [k (f x)]
         (recur xs (inc i) (if (contains? indices k)
                             indices
                             (assoc! indices k i))))
       (persistent! indices)))))

(re-frame/reg-sub
 ::data-display-count
 (fn [db [_ frame-id]]
   (let [global-count (get-in db (conj (path/di-desc frame-id) :event-count))]
     {:all-data global-count})))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (let [desc (get-in db (path/di-desc frame-id))]
     (reduce (fn [m k] (assoc m k (first (get desc k))))
             {}
             [:years :countries :datasources]))))

(re-frame/reg-sub
 ::frame-datasource
 (fn [db [_ path]]
   (get-in db (path/data-instance-consuming path))))

(defn- sort-primitives [constraints]
  (let [indices (first-indices :data-format-lib.filter/prop constraints)]
    (sort-by (comp indices :data-format-lib.filter/prop) constraints)))

(re-frame/reg-sub
 ::simplified-di-desc
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::frame-datasource frame-id])
    (re-frame/subscribe [::di-desc frame-id])])
 (fn [[di-filter di-desc _]]
   {:base di-desc
    :local-filter? false
    :additional (-> (dflsv/simplified-filter di-filter nil)
                    (update :primitives sort-primitives))}))

(defn- get-legend [prediction procedures]
  (let [lang @(re-frame/subscribe [::i18n/current-language])
        attribute-labels @(fi/call-api [:i18n :get-labels-sub])
        {:keys [algorithm attributes prediction-data
                prediction-input-data backdated-forecast-data]}
        prediction]
    (charts/config->legend
     (result/prediction-data->chart-data
      procedures algorithm attributes prediction-data
      (fn [] lang)
      prediction-input-data backdated-forecast-data
      (fn [k] @(i18n-sub k))
      attribute-labels))))

(re-frame/reg-sub
 ::legend-description
 (fn [[_ frame-id]]
   [(re-frame/subscribe [:de.explorama.frontend.algorithms.components.main/result frame-id])
    (re-frame/subscribe [:de.explorama.frontend.algorithms.components.main/procedures])])
 (fn [[result procedures] _]
   (when result
     (into []
           (comp (mapcat #(get-legend % procedures))
                 (distinct))
           (:predictions result)))))

(def ^:private svg-shape
  #{:circle-line})

(defn- legend-element [{:keys [label color shape]}]
  [:li
   (if (svg-shape  shape)
     (let [id (str (random-uuid))]
       [:<>
        [:svg {:height 0, :width 0}
         [:filter {:id id}
          [:feColorMatrix {:in "SourceGraphic"
                           :type "matrix"
                           :values (RGB->svg-color-matrix color)}]]]
        [:span.legend__color {:style {:filter (gstring/format "url(#%s)" id)}
                              :class (cond-> shape
                                       (keyword? shape) name)}]])
     [:span.legend__color {:style {:background-color (css-RGB-string color)}
                           :class (cond-> shape
                                    (keyword? shape) name)}])
   [:span.legend__value label]])

(defn- chart-section [frame-id legend-description]
  [:div.section__content>div.panel__subsection.layouts>div.subsection__content>div.subsection__element
   (into [:ul]
         (map legend-element)
         legend-description)])

(defn edit-functionality-component [frame-id]
  (let [legend-description @(re-frame/subscribe [::legend-description frame-id])]
    (when (seq legend-description)
      [chart-section frame-id legend-description])))

(def legend-impl
  {:visible? true
   :disabled? (fn [frame-id]
                false)

   :data-display-count
   (fn [frame-id]
     (re-frame/subscribe [::data-display-count frame-id]))

   :di-desc-sub
   (fn [frame-id]
     (re-frame/subscribe [::simplified-di-desc frame-id]))})

