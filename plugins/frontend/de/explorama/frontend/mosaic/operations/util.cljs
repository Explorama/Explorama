(ns de.explorama.frontend.mosaic.operations.util
  (:require [cljsjs.moment]
            [clojure.set :as set]
            [de.explorama.shared.data-format.data-instance :as dfl-di]
            [de.explorama.shared.data-format.dates :as dates]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.data.graph-acs :refer [attr->display-name]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.shared.mosaic.group-by-layout :as gbl]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]))

(defn- color-filter- [layout-id color-code lookup-table op-min op-max logical-op-1 logical-op-2 logical-op-3]
  (let [{[_ layout] layout-id} lookup-table
        characteristics
        (mapcat (fn [[_ characteristic]] characteristic)
                (get-in layout [:colors color-code]))]
    (if (= (:attribute-type layout) "number")
      (let [[incl-min-val excl-max-val] characteristics]
        (into [:or] (for [a (:attributes layout)]
                      [logical-op-1
                       #:de.explorama.shared.data-format.filter{:op op-min,
                                                :prop a :value incl-min-val}
                       #:de.explorama.shared.data-format.filter{:op op-max,
                                                :prop a :value excl-max-val}])))
      (into [logical-op-2] (for [a (:attributes layout)
                                 c characteristics]
                             #:de.explorama.shared.data-format.filter{:op logical-op-3,
                                                      :prop a :value c})))))

(defn- color-filter [layout-id color-code lookup-table]
  (color-filter- layout-id color-code lookup-table :>= :< :and :or :=))

(defn- negated-color-filter [layout-id color-code lookup-table]
  (color-filter- layout-id color-code lookup-table :< :>= :or :and :not=))

(defn- layout-colors-filter [lookup-table color-filter-fn op]
  (let [single-layout-unmatched (fn [acc layout-id]
                                  (let [color-codes (keys (get-in lookup-table [layout-id 1 :colors]))]
                                    (into acc (map (fn [color-code]
                                                     (color-filter-fn layout-id color-code lookup-table))
                                                   color-codes))))
        layout-ids (keys lookup-table)]
    (reduce single-layout-unmatched [op] layout-ids)))

(defn- layout-filter [layout-id  color-code lookup-table]
  (if (nil? layout-id)
    (layout-colors-filter lookup-table negated-color-filter :and)
    (color-filter layout-id color-code lookup-table)))

(defn- negated-layout-filter [layout-id  color-code lookup-table]
  (if (nil? layout-id)
    (layout-colors-filter lookup-table color-filter :or)
    (negated-color-filter layout-id color-code lookup-table)))

(defn build-filter-entry
  ([data-value data-grp-by layouts]
   (build-filter-entry data-value data-grp-by layouts :=))
  ([data-value data-grp-by layouts op]
   (if (not= data-grp-by "layout")
     (let [prop (cond
                  (= "year" data-grp-by) ::dates/year
                  (= "month" data-grp-by) ::dates/month
                  :else data-grp-by)]
       [:or {:de.explorama.shared.data-format.filter/op op
             :de.explorama.shared.data-format.filter/prop prop
             :de.explorama.shared.data-format.filter/value (cond (#{"year" "month"} data-grp-by)
                                                 (js/parseInt data-value)
                                                 (= data-value "undefined")
                                                 nil
                                                 :else
                                                 data-value)}])
     (let [layout-id (aget data-value "id")
           color-code (aget data-value "color")
           lookup-table (gbl/build-layout-lookup-table layouts)]
       (cond (= op :=)
             (layout-filter layout-id color-code lookup-table)
             (= op :not=)
             (negated-layout-filter layout-id color-code lookup-table)
             :else
             (error op "is not a valid operation for building group by layout filters"))))))

(defn add-filter-to-di [di filter]
  (let [new-filter-id (dfl-di/ctn->sha256-id filter)
        new-di (-> di
                   (update :di/operations (fn [ops]
                                            [:filter new-filter-id ops]))
                   (assoc-in [:di/filter new-filter-id] filter))]
    (if (empty? filter)
      di
      new-di)))

(defn- filter-grp-path [data-path data layouts op1]
  (let [group-key (gdal/first (gdal/get-in data data-path))]
    (build-filter-entry (gdal/get group-key "key")
                        (gdal/get group-key "attr")
                        layouts
                        op1)))

(defn- nested-filter-copy [data-path data layouts]
  (cond (= 2 (count data-path))
        (filter-grp-path data-path data layouts :=)
        (= 4 (count data-path))
        (let [filter-grp-1 (filter-grp-path (vec (take 2 data-path)) data layouts :=)
              filter-grp-2 (filter-grp-path data-path data layouts :=)]
          [:and
           filter-grp-1
           filter-grp-2])
        :else
        (error data-path "is not a valid path")))

(defn- copy-group-desc [data data-path operation-desc new-di layouts overwrite-behavior? lang]
  {:overwrite-behavior? overwrite-behavior?
   :operation-desc (-> (cond (and (= (count data-path) 2)
                                  (and (get operation-desc gcp/sub-grp-by-key)
                                       (get operation-desc gcp/sort-sub-grp-key)))
                             (set/rename-keys operation-desc
                                              {gcp/sub-grp-by-key gcp/grp-by-key
                                               gcp/sort-sub-grp-key gcp/sort-grp-key})
                             :else
                             (dissoc operation-desc
                                     gcp/sub-grp-by-key
                                     gcp/grp-by-key
                                     gcp/sort-sub-grp-key
                                     gcp/sort-grp-key))
                       (dissoc gcp/coupled-key gcp/couple-key))
   :source-action :copy-group
   :di new-di
   :group-title (let [group-data (-> (gdal/get-in data data-path)
                                     (gdal/first))
                      attribute (gdal/get group-data "attr")
                      group-name (gdal/get group-data "key")
                      labels @(fi/call-api [:i18n :get-labels-sub])]
                  (cond (= attribute "layout")
                        (gbl/get-group-text (gbl/build-layout-lookup-table layouts)
                                            (aget group-name "id")
                                            (aget group-name "color")
                                            attr->display-name
                                            labels
                                            i18n/localized-number)
                        (= attribute "month")
                        (str (i18n/month-name (js/parseInt group-name) lang) " (" (attr->display-name attribute labels) ")")
                        :else
                        (str group-name " (" (attr->display-name attribute labels) ")")))
   :layouts layouts
   :type :copy-group})

(defn- copy-group [db path data-path overwrite-behavior?]
  (let [frame-id (gp/frame-id path)
        operation-desc (get-in db (gp/operation-desc path))
        applied-filter (get-in db (gp/applied-filter path))
        data-instance (get-in db (gp/data-instance path))
        data (gdb/get-events path)
        layouts (get-in db (gp/selected-layouts (gp/frame-id path)))
        new-filter (nested-filter-copy data-path data layouts)
        new-di (cond-> data-instance
                 applied-filter
                 (add-filter-to-di applied-filter)
                 :always
                 (add-filter-to-di new-filter))
        lang (i18n/current-language db)]
    {:dispatch [:de.explorama.frontend.mosaic.core/create-copy-frame
                frame-id
                (copy-group-desc data data-path operation-desc new-di layouts overwrite-behavior? lang)]}))

(re-frame/reg-event-fx
 ::copy-group-ui-wrapper
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ path {data-path :data-path
                        overwrite-behavior? :overwrite-behavior?}]]
   (copy-group db path data-path overwrite-behavior?)))

(defn create-sub-group-filter [db {:keys [path data-path]}]
  (let [local-filter (fi/call-api :frame-filter-db-get db (gp/frame-id path))
        data (gdb/get-events path)
        layouts (get-in db (gp/selected-layouts (gp/frame-id path)))
        new-filters (nested-filter-copy data-path data layouts)
        new-filters (if (and local-filter (not-empty local-filter))
                      (conj new-filters local-filter)
                      new-filters)]
    new-filters))

(re-frame/reg-event-fx
 ::copy-card
 (fn [{db :db} [_ {path :source card :card-id}]]
   (let [frame-id (gp/frame-id path)
         di (get-in db (gp/data-instance path))
         new-filter
         [:or {:de.explorama.shared.data-format.filter/op :=
               :de.explorama.shared.data-format.filter/prop "id"
               :de.explorama.shared.data-format.filter/value (gdal/get card "id")}]]
     {:fx [[:dispatch [:de.explorama.frontend.mosaic.core/create-copy-frame
                       frame-id
                       {:operation-desc {}
                        :source-action :copy-card
                        :layouts (get-in db (gp/selected-layouts (gp/frame-id path)))
                        :di (add-filter-to-di di new-filter)
                        :group-title nil
                        :type :copy-group}]]]})))
