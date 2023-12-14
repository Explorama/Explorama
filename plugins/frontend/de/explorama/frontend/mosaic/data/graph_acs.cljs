(ns de.explorama.frontend.mosaic.data.graph-acs
  "Acces to graph-acs, which are a reduced form of Graph-ACs (calculated on import)"
  (:require [re-frame.core :as re-frame]
            [clojure.set :as set]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.tracks]
            [clojure.string :as string]
            [de.explorama.shared.mosaic.ws-api :refer [update-acs]]))


(def ac-path gp/ac-path)
(def ac-obj-path gp/ac-obj-path)
(def ac-color-path gp/ac-color-path)

(defn retrieve-volatile-acs [db]
  (->>  (get-in db gp/volatile-acs)
        vals))

(defn merge-obj-or-color-acs [new-color-ac color-ac-old]
  (let [color-ac-old (into {} (map (fn [{:keys [name] :as ctn}] [name ctn]) color-ac-old))
        color-ac (into {} (map (fn [{:keys [name] :as ctn}] [name ctn]) new-color-ac))]
    (-> (merge-with (fn [a {:keys [info]}]
                      (update a :info (fn [a-info] (into '() (set/union (set a-info) (set info))))))
                    color-ac-old
                    color-ac)
        vals
        vec)))

(defn obj-or-color-acs [db key]
  (->> (retrieve-volatile-acs db)
       (reduce (fn [acc {ac key}]
                 (merge-obj-or-color-acs acc (set ac)))
               (set (get-in db (case key
                                 :obj-ac ac-obj-path
                                 :color-ac ac-color-path
                                 ac-obj-path))))
       vec))

(defn ac [db]
  (->> (retrieve-volatile-acs db)
       (map :ac)
       (apply merge (get-in db ac-path))))

(defn gather-attributes [db datasources]
  (let [acs (ac db)]
    (reduce (fn [r datasource]
              (apply conj r (get acs datasource)))
            #{}
            datasources)))

(defn- sort-acs [acs]
  (sort-by (fn [{:keys [name]}]
             (string/lower-case name))
           acs))

(re-frame/reg-sub
 ::obj-acs
 (fn [db _]
   (->> :obj-ac
        (obj-or-color-acs db)
        sort-acs
        vec)))

(re-frame/reg-sub
 ::color-acs
 (fn [db _]
   (->> :color-ac
        (obj-or-color-acs db)
        sort-acs
        vec)))

(re-frame/reg-event-db
 update-acs
 (fn [db [_ {:keys [acs obj-acs color-acs]}]]
   (-> db
       (assoc-in ac-path acs)
       (assoc-in ac-obj-path obj-acs)
       (assoc-in ac-color-path color-acs))))


(defn attr->display-name [attr labels]
  (get labels attr attr))


(defn calc-datasource-by
  ([db path op]
   (assert (fn? op) "is not a function")
   (let [datasources (get-in db (gp/instance-datasources path))
         acs (ac db)]
     (reduce op (map
                 (fn [[_ options]]
                   (set options))
                 (select-keys acs datasources)))))
  ([db path blacklist op]
     ;;removes all option combinations defined in the blacklist
   (remove
    (fn [element]
      (not-empty (filter (fn [black]
                           (= black
                              (select-keys element (keys black))))
                         blacklist)))
    (calc-datasource-by db path op))))

(defn update-labels [db elements]
  (let [attr-labels (fi/call-api [:i18n :get-labels-db-get] db)]
    (mapv (fn [e] (update e :name #(attr->display-name % attr-labels)))
          elements)))

;! Deadcode?
(defn intersection-by-options [db path]
  (update-labels db
                 (conj (calc-datasource-by db
                                           path
                                           [{:label "Fact"}
                                            {:label "External-ref"}
                                            {:label "Date"}
                                            {:label "Notes"}
                                            {:label "Context"
                                             :name "location"}]
                                           set/intersection)
                       {:label "Feature"
                        :name "Identifier"
                        :key "id"})))

(defn couple-by [db path]
  (update-labels db
                 (calc-datasource-by db
                                     path
                                     [{:label "Fact"}
                                      {:label "External-ref"}
                                      {:label "Context"}
                                      {:label "Datasource"}
                                      {:label "Feature"}
                                      {:label "Notes"}
                                      {:label "Date"
                                       :name "day"}
                                      {:label "Date"
                                       :name "date"}]
                                     set/intersection)))

(re-frame/reg-sub
 ::group-by
 (fn [db [_ path]]
   (let [layouts (seq (get-in db (gp/selected-layouts (gp/frame-id path))))
         res (update-labels db
                            (cond-> (calc-datasource-by db
                                                        path
                                                        [{:label "Fact"}
                                                         {:label "External-ref"}
                                                         {:label "Context"
                                                          :name "location"}
                                                         {:label "Notes"}
                                                         {:label "Date"
                                                          :name "date"}
                                                         {:label "Date"
                                                          :name "day"}]
                                                        set/union)
                              layouts (conj
                                       {:name  "layout", :key "layout", :label "Layout", :type "map"})))]
     res)))

(re-frame/reg-sub
 ::sort-by
 (fn [db [_ path]]
   (update-labels db
                  (calc-datasource-by db
                                      path
                                      [{:label "Fact"
                                        :type "boolean"}
                                       {:label "External-ref"}
                                       {:label "Context"
                                        :name "location"}
                                       {:label "Notes"}
                                       {:label "Date"
                                        :name "day"}
                                       {:label "Date"
                                        :name "year"}
                                       {:label "Date"
                                        :name "month"}]
                                      set/union))))

(re-frame/reg-sub
 ::sort-by-group
 (fn [db [_ path]]
   (update-labels db
                  (calc-datasource-by db
                                      path
                                      [{:label "Context"}
                                       {:label "External-ref"}
                                       {:label "Date"}
                                       {:label "Fact"
                                        :type "string"}
                                       {:label "Fact"
                                        :type "boolean"}
                                       {:label "Datasource"}
                                       {:label "Feature"}
                                       {:label "Notes"}]
                                      set/union))))

;! deadcode
(re-frame/reg-sub
 ::intersect-by
 (fn [db [_ path]]
   (intersection-by-options db path)))

(re-frame/reg-sub
 ::submenu-option-event-attributes
 (fn [db [_ path blacklist]]
   (update-labels db
                  (calc-datasource-by db
                                      path
                                      (conj (mapv (fn [b]
                                                    {:name (name b)})
                                                  blacklist)
                                            {:label "Fact"
                                             :type "number"}
                                            {:label "External-ref"}
                                            {:label "Fact"
                                             :type "boolean"}
                                            {:label "Date"
                                             :name "day"}
                                            {:label "Date"
                                             :name "date"})
                                      set/intersection))))
(re-frame/reg-sub
 ::couple-by
 (fn [db [_ path]]
   (couple-by db path)))

(re-frame/reg-sub
 ::scatter-axis
 (fn [db [_ path]]
   (update-labels db
                  (calc-datasource-by db
                                      path
                                      [{:label "Context"
                                        :name "location"}
                                       {:label "External-ref"}
                                       {:label "Notes"}
                                       {:label "Date"
                                        :name "year"}
                                       {:label "Date"
                                        :name "day"}
                                       {:label "Date"
                                        :name "month"}]
                                      set/union))))
