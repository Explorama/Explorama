(ns de.explorama.frontend.mosaic.interaction.context-menu.shared
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [clojure.string :as string]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.di-acs :as di-acs]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.data.graph-acs :as gdgc]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [taoensso.timbre :refer [error]]))

(def aggregate-functions
  [{:name :contextmenu-operations-aggregate-sum
    :value :sum}
   {:name :contextmenu-operations-aggregate-max
    :value :max}
   {:name :contextmenu-operations-aggregate-min
    :value :min}
   {:name :contextmenu-operations-aggregate-average
    :value :average}])

(def generic-sort-functions
  [{:name :contextmenu-operations-sort-by-event-count
    :by :event-count}
   {:name :contextmenu-operations-sort-by-group-name
    :by :name}])

(defn item [{:keys [label click-fn icon sub-items visible? disabled?]}]
  (let [label (cond (string? label)
                    label
                    (keyword? label)
                    @(re-frame/subscribe [::i18n/translate label])
                    :else
                    (val-or-deref label))]
    (cond-> {:label label}
      click-fn
      (assoc :on-click click-fn)
      sub-items
      (assoc :sub-items sub-items)
      icon
      (assoc :icon icon)
      visible?
      (assoc :visible? visible?)
      disabled?
      (assoc :disabled? disabled?))))

(defn default-acc-fn [acc {:keys [name key by]} click-fn icon-fn]
  (conj acc
        (item {:label name
               :click-fn #(click-fn key name by)
               :icon (when icon-fn (icon-fn key))})))

(defn gen-nested-items [{:keys [label options click-fn icon-fn sort? acc-fn icon
                                visible? disabled?]
                         :or
                         {acc-fn default-acc-fn
                          sort? true}}]
  (let [sub-items (reduce
                   (fn [acc opt]
                     (acc-fn acc opt click-fn icon-fn))
                   []
                   (if sort?
                     (sort-by (fn [{:keys [name]}]
                                (string/lower-case name))
                              (if (vector? options)
                                options
                                @options))
                     options))]
    (item {:disabled? disabled?
           :visible? visible?
           :label label
           :icon icon
           :sub-items sub-items})))

(re-frame/reg-event-fx
 ::submenu-top-level-event
 (fn [_ [_ path attr event]]
   (let [attribute-vals @(re-frame/subscribe [::di-acs/attribute-vals path attr])
         attribute-vals (-> attribute-vals
                            sort
                            vec)]
     (when (vector? event)
       {:dispatch (conj event attr {attr (vec attribute-vals)})}))))

(defn- submenu-desc->options
  "Builds a list of contextmenu-elements desc with label and event.
  The list is based on the submenu type. Currently only :event-attributes is valid."
  [card {:keys [type event blacklist whitelist]} path is-top-level?]
  (let [event-attributes (cond is-top-level?
                               @(re-frame/subscribe [::di-acs/attributes path blacklist])
                               card (cond-> card
                                      blacklist (gdal/dissoc blacklist)
                                      whitelist (gdal/select-keys whitelist)
                                      :always gdal/keys)
                               :else {})
        labels @(fi/call-api [:i18n :get-labels-sub])]
    (cond
      (= type :event-attributes)
      (->> event-attributes
           (sort-by (fn [attr]
                      (string/lower-case
                       (cond
                         is-top-level? (get labels attr attr)
                         (keyword? attr) (name attr)
                         :else (get labels attr attr)))))
           (mapv
            (fn [attr]
              (if is-top-level?
                {:label (get labels attr attr)
                 :event [::submenu-top-level-event path attr event]}
                {:label (let [attr (if (keyword? attr)
                                     (name attr)
                                     attr)]
                          (get labels attr attr))
                 :event (conj event
                              [attr
                               (fn [datapoint]
                                 (let [date (when datapoint (gdal/get datapoint "date"))
                                       [year month] (when date
                                                      (string/split date #"-"))
                                       datapoint (cond
                                                   date (-> datapoint
                                                            (gdal/assoc "year" year)
                                                            (gdal/assoc "month" month)))]
                                   (-> datapoint
                                       (gdal/get attr)
                                       (gdal/g->))))])}))))
      :else
      [{:label (str "No valid submenu type " type)}])))

(re-frame/reg-sub
 ::canvas
 (fn [db [_ path]]
   (get-in db (conj (gp/top-level path)
                    :canvas
                    :interaction
                    :context-menu))))

(defn submenu->options
  "Creates the submenu hiccup based on the description."
  [path submenu is-top-level?]
  (let [{{:keys [card]} :payload} @(re-frame/subscribe [::canvas path])
        ;? this is usable because this function is used for comments on cards and information spaces
        date (when card (gdal/get card "date"))
        [year month] (when date
                       (string/split date #"-"))
        card (cond
               date (-> card
                        (gdal/assoc "year" year)
                        (gdal/assoc "month" month)))
        submenu-options (cond
                          (and submenu
                               (vector? submenu)) submenu
                          submenu (submenu-desc->options card submenu path is-top-level?)
                          :else nil)]
    submenu-options))

(defn- desc->item
  "Generates a contextmenu element based on the contextmenu-desc from the registry-service."
  [path is-top-level? {:keys [label icon submenu disabled? visible?] event-vec :event}]
  (let [label (if (vector? label)
                @(re-frame/subscribe label)
                label)
        {{:keys [card]} :payload} @(re-frame/subscribe [::canvas path])]
    (if submenu
      (gen-nested-items {:label label
                         :disabled? disabled?
                         :visible? visible?
                         :icon icon
                         :options (submenu->options path submenu is-top-level?)
                         :sort? false
                         :acc-fn (fn [acc {:keys [label event]}]
                                   (conj acc (item {:label label
                                                    :click-fn (when event
                                                                #(re-frame/dispatch (conj event card)))})))})
      (item {:label label
             :click-fn (when event-vec #(re-frame/dispatch event-vec))}))))

(defn desc-items
  "Generates a context-menu item vector for given descriptions"
  [acc path top-level? contextmenu-descs]
  (reduce (fn [acc [_id desc]]
            (conj acc
                  (desc->item path top-level? desc)))
          acc
          contextmenu-descs))

(defn sort-icon [current-desc]
  (cond (= :desc (:direction current-desc))
        :sort-desc
        (= :asc (:direction current-desc))
        :sort-asc
        :else nil))

(defn sort-by-action [sortby-key origin]
  (re-frame/dispatch [::tasks/execute-wrapper origin :sort-by {:by sortby-key}])
  (re-frame/dispatch (fi/call-api [:product-tour :next-event-vec] :mosaic :sort-by)))

(defn sort-by-items [origin {sort-desc gcp/sort-key} coupled?]
  (gen-nested-items {:label :contextmenu-top-level-sortby
                     :icon :sort-by
                     :sort? true
                     :options (re-frame/subscribe [::gdgc/sort-by origin])
                     :click-fn (fn [key]
                                 (sort-by-action key origin)
                                 (when coupled?
                                   (re-frame/dispatch (fi/call-api :couple-submit-action-vec
                                                               (gp/frame-id origin)
                                                               {:sort-by key}))))

                     :icon-fn (fn [key]
                                (if (= key (:by sort-desc))
                                  (sort-icon sort-desc)
                                  nil))}))

(defn sort-group-option [path attr-name by key {method-name :name method-value :value :as method} direction event-name]
  (let [label (cond (keyword? attr-name)
                    @(re-frame/subscribe [::i18n/translate attr-name])
                    method
                    (str attr-name " (" @(re-frame/subscribe [::i18n/translate method-name]) ")")
                    :else
                    attr-name)
        icon (sort-icon direction)]
    (item {:label label
           :icon icon
           :click-fn #(re-frame/dispatch [::tasks/execute-wrapper
                                      path
                                      event-name
                                      {:by by
                                       :attr key
                                       :method method-value}])})))

(re-frame/reg-sub
 ::layout-sorting-required
 (fn [db [_ path event-name]]
   (= "layout" (get-in db
                       (conj (gp/operation-desc path)
                             (case event-name
                               :sort-group-by :grp-key
                               :sort-sub-group-by :sub-grp-key
                               (error event-name "is unknown sorting event")))))))

(defn sort-group-items [origin {grp-direction-by :by
                                grp-direction-attr :attr
                                grp-direction-method :method
                                _ :directon
                                :as direction-params}
                        label
                        event-name]
  (let [labels @(fi/call-api [:i18n :get-labels-sub])
        layout? @(re-frame/subscribe [::layout-sorting-required origin event-name])]
    (gen-nested-items {:label label
                       :icon :sort-by
                       :options (into (if layout?
                                        (conj generic-sort-functions {:name  (get labels "layout" "layout") :by "layout"})
                                        generic-sort-functions)
                                      @(re-frame/subscribe [::gdgc/sort-by-group origin]))
                       :sort? false
                       :acc-fn (fn [acc {:keys [key name by]}]
                                 (let [gen-option (fn [by key method direction]
                                                    (sort-group-option origin name by key method direction event-name))]
                                   (cond (or (= by :event-count)
                                             (= by :name)
                                             (= by "layout"))
                                         (conj acc
                                               (gen-option by
                                                           nil
                                                           nil
                                                           (when (and (= by grp-direction-by)
                                                                      (= key grp-direction-attr)
                                                                      (nil? grp-direction-method))
                                                             direction-params)))
                                         :else
                                         (reduce (fn [parent method]
                                                   (conj parent
                                                         (gen-option :aggregate
                                                                     key
                                                                     method
                                                                     (when (and (= :aggregate grp-direction-by)
                                                                                (= key grp-direction-attr)
                                                                                (= (:value method)
                                                                                   grp-direction-method))
                                                                       direction-params))))
                                                 acc
                                                 aggregate-functions))))})))
