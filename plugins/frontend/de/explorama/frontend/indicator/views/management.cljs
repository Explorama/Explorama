(ns de.explorama.frontend.indicator.views.management
  "Combines all access to the app-state regarding de.explorama.frontend.indicator.
   This is used by all namespaces inside of views."
  (:require [cljs.reader :as reader]
            [clojure.set :as set]
            [clojure.string :as str]
            [data-format-lib.data-instance :as dfl-di]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.path :as ip]
            [de.explorama.shared.indicator.transform :as transformer]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [warn]]))

(defn- indicator-ui-comp-disabled? [disable comp-values]
  (if (or (not disable)
          (empty? disable))
    false
    (every? (fn [[[_ comp-value] {current-comp-value :value}]]
              (= current-comp-value
                 comp-value))
            (zipmap disable comp-values))))

(defn new-indicator-id [db]
  (get-in db (conj ip/new-indicator :id)))

(defn indicator-exist? [db indicator-id]
  (or (get-in db (ip/indicator-desc indicator-id))
      (= (new-indicator-id db)
         indicator-id)))

(defn updated-indicator [indicator changes]
  (reduce (fn [o [comp-key change]]
            (let [assoc-fn (partial assoc o comp-key)]
              (case comp-key
                :name (assoc-fn change)
                :description (assoc-fn change)
                :di-desc (assoc-fn change)
                :dis (assoc-fn change)
                :indicator-type (assoc-fn (keyword change))
                :ui-desc (assoc-fn
                          (merge-with (fn [result-val input-val]
                                        (cond
                                          (map? result-val) (merge result-val input-val)
                                          (and (vector? result-val)
                                               (vector? input-val)) (concat result-val input-val)
                                          :else input-val))
                                      (get indicator :ui-desc)
                                      change)))))
          indicator
          changes))

(defn current-indicator-desc [db indicator-id]
  (let [indicator-desc (get-in db (ip/indicator-desc indicator-id))
        new-indicator-desc (get-in db ip/new-indicator)
        changes (get-in db (ip/indicator-changes indicator-id))]
    (updated-indicator (or indicator-desc
                           new-indicator-desc)
                       changes)))

(defn current-indicator-props [db indicator-id properties]
  (select-keys (current-indicator-desc db indicator-id)
               properties))

(defn current-indicator-prop [db indicator-id property]
  (get (current-indicator-desc db indicator-id)
       property))

(defn current-indicator-comp-value [db indicator-id comp-key]
  (let [{template-key :indicator-type
         current-ui-desc :ui-desc} (current-indicator-desc db indicator-id)]
    (get-in current-ui-desc [template-key comp-key])))

(defn template-ui-description [db indicator-id]
  (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
    (get-in db (ip/template-ui-desc template-key))))

(defn- datasets-sorted [db indicator-id filter-datasets]
  (let [datasets (apply dissoc
                        (get-in db (ip/indicator-data indicator-id))
                        filter-datasets)]
    (->> datasets
         vals
         (sort-by :timestamp))))

(defn- dataset-title [index]
  (str "Dataset " (inc index)))

(defn sorted-dataset-desc [db indicator-id]
  (let [filter-datasets (get-in db (ip/removed-indicator-data indicator-id))]
    (->> (datasets-sorted db indicator-id filter-datasets)
         (map-indexed
          (fn [index dataset]
            (assoc dataset
                   :title (dataset-title index))))
         vec)))

(defn all-comps-map [db indicator-id]
  (let [{:keys [definition-rows
                additional-attributes]
         :as template-desc} (template-ui-description db indicator-id)
        additional-comps-map (into {}
                                   (map (fn [{:keys [id] :as comp}]
                                          [id comp]))
                                   (:comps additional-attributes))]
    (reduce (fn [acc {:keys [id] :as comp}]
              (assoc acc id comp))
            {:additional additional-comps-map}
            (mapcat :comps definition-rows))))

(defn comp-desc-based-on-id [db indicator-id comp-id]
  (let [all-comps (all-comps-map db indicator-id)]
    (or (get all-comps comp-id)
        (get-in all-comps [:additional comp-id]))))

(defn template-calc-description [db template-key]
  (get-in db (ip/template-calc-desc template-key)))

(defn- comp-desc->replace-keys [{:keys [content replace default]
                                 {noe-op :op noe-key :key
                                  noe-default :default
                                  :as noe?} :number-of-events
                                 :as comp}
                                ui-value #_{:keys [value type di]}]
  (let [replace-keys
        (case content
          (:calc-attributes :all-attributes) (let [{:keys [value di]} ui-value
                                                   replace-name (name replace)
                                                   replace-attr-name (keyword
                                                                      (str replace-name
                                                                           "_attr_name"))
                                                   replace-heal-attr-name (keyword
                                                                           (str replace-name
                                                                                "_heal_attr_name"))
                                                   replace-di (keyword
                                                               (str replace-name
                                                                    "_di"))
                                                   di (dfl-di/ctn->sha256-id di)]
                                               {replace-attr-name value
                                                replace-heal-attr-name value
                                                replace-di di})
          :time {replace [(:value ui-value)]}
          :group-attributes {replace (mapv :value ui-value)}
          :aggregations {replace (:value ui-value)}
          :number {replace (js/parseInt (or ui-value default))}
          (do
            (warn "Unkown content-type to extract description-map" comp)
            nil))]
    (cond-> replace-keys
      (and noe?
           (= :number-of-events
              (:value ui-value)))
      (assoc noe-key noe-op)
      (and noe?
           (not= :number-of-events
                 (:value ui-value))
           noe-default)
      (assoc noe-key noe-default))))

(defn- remove-disable-comps- [all-components current-ui-desc]
  (reduce (fn [all-components [comp-key {disable :disable :as comp-desc}]]
            (if (and disable
                     (indicator-ui-comp-disabled? disable (map (fn [[key]]
                                                                 (get current-ui-desc key))
                                                               disable)))
              all-components
              (assoc all-components comp-key comp-desc)))
          {}
          all-components))

(defn- remove-disabled-comps [all-components current-ui-desc]
  (cond-> (remove-disable-comps- (dissoc all-components :additional)
                                 current-ui-desc)
    (get all-components :additional)
    (assoc :additional
           (remove-disable-comps- (get all-components :additional)
                                  current-ui-desc))))

(defn indicator->description-map
  "The generated map is a description with te replace-keys.
   This is used to generate the calculation description
   based on a template and given ui-selections."
  [db indicator-id]
  (let [{:keys [ui-desc indicator-type]
         indicator-name :name
         indicator-desc :description} (current-indicator-desc db indicator-id)
        all-components (all-comps-map db indicator-id)
        current-ui-desc (get ui-desc indicator-type)
        all-components (remove-disabled-comps all-components current-ui-desc)
        template-desc (get-in db (ip/template-desc indicator-type))]
    (reduce
     (fn [acc [comp-id comp-desc]]
       (let [ui-value (get current-ui-desc comp-id)]
         (if (= comp-id :additional)
           ;Additional is a special case and needs be handled diffrently
           (assoc acc :# (mapv (fn [components]
                                 (reduce (fn [res [comp-id- ui-value-]]
                                           (merge-with into
                                                       res
                                                       (comp-desc->replace-keys
                                                        (get-in all-components
                                                                [:additional comp-id-])
                                                        ui-value-)))
                                         {}
                                         components))
                               ui-value))
           (merge-with into
                       acc
                       (comp-desc->replace-keys
                        comp-desc
                        ui-value)))))
     {:replace_defaults_indicator_name "indicator"
      :replace-datasource-name indicator-name
      :replace-notes-value (or indicator-desc (cond-> (str indicator-name)
                                                indicator-type
                                                (str ", " (i18n/translate db (:label template-desc)))))
      :replace-indicator-type (when indicator-type (name indicator-type))}
     all-components)))

(defn- custom-indicator-description [db {:keys [id]
                                         {:keys [custom]} :ui-desc
                                         :as indicator-desc}]
  (let [input-desc (first (vals custom))
        all-datasets (into {}
                           (map (fn [{:keys [title di]}]
                                  [title di]))
                           (sorted-dataset-desc db id))]
    (reader/read-string (reduce (fn [desc [title di]]
                                  (str/replace desc title di))
                                input-desc
                                all-datasets))))

(defn- deduplicate-description [desc]
  (update desc :# (fn [add-attrs]
                    (let [add-attrs (mapv (fn [desc]
                                            (if (= (:replace_X_attr_name desc) :number-of-events)
                                              (assoc desc
                                                     :replace_X_attr_name "numberofevents"
                                                     :replace_X_heal_attr_name "numberofevents")
                                              desc))
                                          add-attrs)]
                      (mapv (fn [desc]
                              (if (not= "numberofevents" (:replace_X_heal_attr_name desc))
                                (assoc desc :replace_X_heal_attr_name
                                       (str (:replace_X_heal_attr_name desc)
                                            " (" (name (:replace_aggregation_# desc)) ")"))
                                desc))
                            add-attrs)))))

(defn indicator->final-description [db indicator-id]
  (let [{:keys [indicator-type]
         :as current-indicator-desc} (current-indicator-desc db indicator-id)
        description-map (-> (indicator->description-map db indicator-id)
                            deduplicate-description)
        removed-datasets (get-in db (ip/removed-indicator-data indicator-id) #{})
        transformed-calculation (if (not= indicator-type :custom)
                                  (-> db
                                      (template-calc-description indicator-type)
                                      (transformer/traverse description-map))
                                  (custom-indicator-description db current-indicator-desc))
        dis (into {}
                  (comp (filter (fn [[di-id]]
                                  (not (removed-datasets di-id))))
                        (map (fn [[di-id {:keys [di]}]]
                               [di-id di])))
                  (get-in db (ip/indicator-data indicator-id)))]
    (-> current-indicator-desc
        (update :ui-desc
                #(select-keys % [indicator-type]))
        (assoc :calculation-desc transformed-calculation
               :dis dis))))

(re-frame/reg-sub
 ::indicator-addon-rows
 (fn [db [_ indicator-id]]
   (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
     (get-in db (ip/indicator-addon-rows indicator-id template-key)))))

(defn current-indicator-addon-row-value [db indicator-id row-id attribute-id]
  (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
    (get-in db (ip/indicator-addon-row-value indicator-id template-key row-id attribute-id))))

(re-frame/reg-sub
 ::indicator-addon-row-value
 (fn [db [_ indicator-id row-id attribute-id]]
   (current-indicator-addon-row-value db indicator-id row-id attribute-id)))

(re-frame/reg-event-db
 ::indicator-addon-add-row
 (fn [db [_ indicator-id]]
   (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
     (update-in db (ip/indicator-addon-rows indicator-id template-key) (fnil conj []) {}))))

(defn vec-remove
  "remove elem in coll"
  [pos coll]
  (into (subvec coll 0 pos) (subvec coll (inc pos))))

(re-frame/reg-event-db
 ::indicator-addon-remove-row
 (fn [db [_ indicator-id row-id]]
   (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
     (update-in db (ip/indicator-addon-rows indicator-id template-key) #(vec-remove row-id %)))))

(re-frame/reg-event-db
 ::indicator-addon-row-value
 (fn [db [_ indicator-id row-id attribute-id value]]
   (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
     (assoc-in db (ip/indicator-addon-row-value indicator-id template-key row-id attribute-id) value))))

(re-frame/reg-sub
 ::indicator-props
 (fn [db [_ indicator-id properties]]
   (current-indicator-props db indicator-id properties)))

(re-frame/reg-sub
 ::indicator-prop
 (fn [db [_ indicator-id property]]
   (current-indicator-prop db indicator-id property)))

(defn- update-indicator-prop [db indicator-id prop-key prop-value]
  (assoc-in db
            (ip/indicator-prop-change indicator-id prop-key)
            prop-value))

(re-frame/reg-event-db
 ::update-indicator-prop
 (fn [db [_ indicator-id prop-key prop-value]]
   (update-indicator-prop db
                          indicator-id
                          prop-key
                          prop-value)))

(re-frame/reg-event-db
 ::update-indiactor-type
 (fn [db [_ indicator-id indicator-type]]
   (let [{current-ui-desc :ui-desc} (current-indicator-desc db indicator-id)
         comp-val (get-in current-ui-desc [indicator-type "time-granularity-select"])]
     (cond-> db
       :always (update-indicator-prop indicator-id :indicator-type indicator-type)
       (nil? comp-val) (assoc-in (ip/indicator-ui-desc-change indicator-id
                                                              indicator-type
                                                              "time-granularity-select")
                                 {:value "year"
                                  :label "year"
                                  :type :value})))))

(re-frame/reg-event-db
 ::update-indicator-ui-desc
 (fn [db [_ indicator-id comp-key changed-value]]
   (let [template-key (current-indicator-prop db indicator-id :indicator-type)]
     (assoc-in db
               (ip/indicator-ui-desc-change indicator-id
                                            template-key
                                            comp-key)
               changed-value))))

(re-frame/reg-sub
 ::indicator-ui-desc-comp-value
 (fn [db [_ indicator-id comp-key]]
   (current-indicator-comp-value db indicator-id comp-key)))

(re-frame/reg-sub
 ::indicator-ui-comp-disabled?
 (fn [[_ indicator-id disable]]
   (mapv (fn [[comp-key]]
           (re-frame/subscribe [::indicator-ui-desc-comp-value indicator-id comp-key]))
         disable))
 (fn [comp-values [_ _ disable]]
   (indicator-ui-comp-disabled? disable comp-values)))

(re-frame/reg-sub
 ::active-indicator
 (fn [db _]
   (get-in db ip/active-indicator)))

(re-frame/reg-event-fx
 ::change-active-indicator
 (fn [{db :db} [_ indicator-id project?]]
   (if (nil? indicator-id)
     {:db (assoc-in db
                    ip/active-indicator
                    nil)}
     (let [connected-dis (set (keys (get-in db (ip/indicator-data indicator-id))))
           {:keys [ui-desc indicator-type]
            di-in-desc :dis} (current-indicator-props db indicator-id [:dis :indicator-type :ui-desc])
           missing-dis (->> di-in-desc
                            (filter #(not (connected-dis (first %))))
                            (map second))
           additional-attributes (get-in ui-desc [indicator-type :additional])]
       {:db (cond-> db
              :always (assoc-in ip/active-indicator {:project? project?
                                                     :id indicator-id})
              additional-attributes (assoc-in (ip/indicator-addon-rows indicator-id indicator-type) additional-attributes))
        :fx (mapv (fn [di]
                    [:dispatch [:de.explorama.frontend.indicator.core/connect-to-frame-query nil false {:di di}]])
                  missing-dis)}))))

(re-frame/reg-sub
 ::creating-new?
 (fn [db _]
   (not-empty (get-in db ip/new-indicator))))

(re-frame/reg-sub
 ::new-indicator-id
 (fn [db _]
   (new-indicator-id db)))

(defn- copy-name [original-name current-indicators]
  (let [current-names (set (map (fn [[_ {:keys [name]}]]
                                  name)
                                current-indicators))]
    (loop [count 0
           new-name (str original-name " (copy)")
           unique? (not (current-names new-name))]
      (if unique?
        new-name
        (let [n-count (inc count)
              n-name (str original-name " (copy " n-count ")")]
          (recur n-count
                 n-name
                 (not (current-names n-name))))))))

(defn- unique-name [count current-indicators]
  (loop [count count
         unique? (not (some #(= (str "Indicator " count) (:name %))
                            current-indicators))]
    (if unique?
      (str "Indicator " count)
      (recur (inc count)
             (not (some #(= (str "Indicator " (inc count)) (:name %))
                        current-indicators))))))

(re-frame/reg-event-fx
 ::create-new-indicator
 (fn [{db :db} _]
   (let [{:keys [username]} (fi/call-api :user-info-db-get db)
         indicators (get-in db ip/indicators)
         new-indicator-id (str (random-uuid))
         indicator-name (unique-name (inc (count indicators)) indicators)]
     {:db (assoc-in db
                    ip/new-indicator
                    {:name indicator-name
                     :creator username
                     :id new-indicator-id
                     :write-access? true})
      :dispatch [::change-active-indicator new-indicator-id]})))

(re-frame/reg-event-fx
 ::copy-indicator
 (fn [{db :db} [_ indicator-id project?]]
   (let [user-info (fi/call-api :user-info-db-get db)
         indicator-desc (get-in db (if project?
                                     (ip/project-indicator-desc indicator-id)
                                     (ip/indicator-desc indicator-id)))
         new-indicator-id (str (random-uuid))
         indicators (get-in db ip/indicators)
         indicator-name (copy-name (:name indicator-desc)
                                   (get-in db ip/indicators))
         copied-indicator-desc (assoc indicator-desc
                                      :name indicator-name
                                      :id new-indicator-id)
         copied-data (get-in db (ip/indicator-data indicator-id))
         updated-db (-> db
                        (assoc-in (ip/indicator-desc new-indicator-id) copied-indicator-desc)
                        (assoc-in (ip/indicator-data new-indicator-id) copied-data))
         finalized-desc (indicator->final-description
                         updated-db
                         new-indicator-id)]
     {:db updated-db
      :backend-tube [ws-api/create-new-indicator
                     {:client-callback [ws-api/create-new-indicator-result finalized-desc]}
                     user-info finalized-desc]})))

(re-frame/reg-event-fx
 ::save-indicator
 (fn [{db :db} [_ indicator-id]]
   (let [user-info (fi/call-api :user-info-db-get db)
         is-new? (= indicator-id
                    (new-indicator-id db))
         finalized-desc (indicator->final-description db indicator-id)]
     {:backend-tube [(if is-new?
                       ws-api/create-new-indicator
                       ws-api/update-indicator-infos)
                     {:client-callback [(if is-new?
                                          ws-api/create-new-indicator-result
                                          ws-api/update-indicator-infos-result)
                                        finalized-desc]}
                     user-info
                     finalized-desc]})))

(re-frame/reg-event-fx
 ws-api/create-new-indicator-result
 (fn [{db :db} [_ finalized-desc {:keys [status msg data]}]]
   (if (= status :success)
     (let [indicator-id (:id data)]
       {:db (-> db
                (assoc-in (ip/indicator-desc indicator-id) finalized-desc)
                (update-in ip/indicators-changes dissoc indicator-id)
                (update-in ip/changed-indicators-data dissoc indicator-id)
                (update ip/root dissoc ip/new-indicator-key))
        :dispatch [::change-active-indicator indicator-id]})
     {:db db})))

(re-frame/reg-event-fx
 ws-api/update-indicator-infos-result
 (fn [{db :db} [_ finalized-desc {:keys [status msg data]}]]
   (if (= status :success)
     (let [indicator-id (:id data)
           removed-datasets (get-in db (ip/removed-indicator-data indicator-id))]
       {:db (-> db
                (assoc-in (ip/indicator-desc indicator-id) finalized-desc)
                (update-in ip/indicators-changes dissoc indicator-id)
                (update-in (ip/indicator-data indicator-id) (fn [val]
                                                              (apply dissoc val removed-datasets)))
                (update-in ip/changed-indicators-data dissoc indicator-id))
        :dispatch [::change-active-indicator indicator-id]})
     {:db db})))

(re-frame/reg-event-fx
 ::delete-indicator
 (fn [{db :db} [_ indicator-id]]
   (let [user (fi/call-api :user-info-db-get db)
         is-new? (= indicator-id (new-indicator-id db))]
     (if is-new?
       {:db (-> db
                (update ip/root dissoc ip/new-indicator-key)
                (update-in ip/indicators-changes dissoc indicator-id))
        :dispatch [::change-active-indicator nil]}
       {:backend-tube [ws-api/delete-indicator
                          {:client-callback [ws-api/delete-indicator-result]}
                          user {:id indicator-id}]}))))

(re-frame/reg-event-fx
 ws-api/delete-indicator-result
 (fn [{db :db} [_ {:keys [status msg data]}]]
   (when (= status :success)
     (let [indicator-id (second data)
           updated-db (-> db
                          (update-in ip/indicators dissoc indicator-id)
                          (update-in ip/indicators-changes dissoc indicator-id)
                          (update-in ip/indicators-data dissoc indicator-id)
                          (update-in ip/changed-indicators-data dissoc indicator-id))]
       {:db updated-db
        :dispatch [::change-active-indicator nil]}))))

(re-frame/reg-event-db
 ::discard-changes
 (fn [db [_ indicator-id]]
   (let [added-datasets (get-in db (ip/added-indicator-data indicator-id))
         {:keys [ui-desc indicator-type]} (get-in db (ip/indicator-desc indicator-id))
         additional-attributes (get-in ui-desc [indicator-type :additional])]
     (cond-> db
       :always (update-in ip/indicators-changes dissoc indicator-id)
       :always (update-in ip/changed-indicators-data dissoc indicator-id)
       additional-attributes (assoc-in (ip/indicator-addon-rows indicator-id indicator-type) additional-attributes)
       :always (update-in (ip/indicator-data indicator-id)
                          (fn [datasets]
                            (apply dissoc datasets added-datasets)))))))

(re-frame/reg-sub
 ::changed?
 (fn [db [_ indicator-id ignore-is-new?]]
   (let [is-new? (= indicator-id
                    (new-indicator-id db))
         saved-desc (get-in db (ip/indicator-desc indicator-id))
         current-desc (current-indicator-desc db indicator-id)
         added-datasets (get-in db (ip/added-indicator-data indicator-id))
         removed-datasets (get-in db (ip/removed-indicator-data indicator-id))]
     (or (and (not ignore-is-new?) is-new?)
         (and (not is-new?) (not= saved-desc current-desc))
         (not-empty added-datasets)
         (not-empty removed-datasets)))))

(re-frame/reg-sub
 ::all-indicators
 ;; Returns all indicators including changes and the one beeing created
 ;; Those are sorted by name and the new indicator is at the first position
 (fn [db _]
   (let [new-indicator-id (get-in db (conj ip/new-indicator :id))
         indicator-ids (->> (get-in db ip/indicators)
                            vals
                            (sort-by :name)
                            (map :id))]
     (mapv #(current-indicator-desc db %)
           (filter
            identity
            (conj indicator-ids new-indicator-id))))))

(re-frame/reg-sub
 ::project-indicators
 (fn [db _]
   (->> (get-in db ip/project-indicators)
        vals
        (sort-by :name)
        vec)))

(defn- intersected-options [datasets attribute-to-intersect]
  (->> datasets
       (map #(get-in % [:ui-options attribute-to-intersect]))
       (map set)
       (apply set/intersection)
       (sort-by (comp clojure.string/lower-case :label))
       vec))

(defn- time-attributes [db indicator-id]
  (let [removed-datasets (get-in db (ip/removed-indicator-data indicator-id) #{})]
    (intersected-options
     (datasets-sorted db indicator-id removed-datasets)
     :time-attributes)))

(re-frame/reg-sub
 ::time-attributes
 (fn [db [_ indicator-id]]
   (time-attributes db indicator-id)))

(defn- group-attributes [db indicator-id]
  (let [removed-datasets (get-in db (ip/removed-indicator-data indicator-id) #{})]
    (intersected-options
     (datasets-sorted db indicator-id removed-datasets)
     :group-attributes)))

(re-frame/reg-sub
 ::group-attributes
 (fn [db [_ indicator-id]]
   (group-attributes db indicator-id)))

(defn is-indicator-valid? [db indicator-id]
  (let [all-indicator-ids (conj (keys (get-in db ip/indicators))
                                (new-indicator-id db))
        {current-name :name
         creator :creator
         indicator-type :indicator-type
         ui-desc :ui-desc
         :as indicator-desc} (current-indicator-desc db indicator-id)
        {calc-desc :description
         validation-desc :validation-desc} (get-in db (ip/template-desc indicator-type))
        removed-datasets (get-in db (ip/removed-indicator-data indicator-id) #{})
        values-map (indicator->description-map db indicator-id)
        indicators (reduce (fn [res i]
                             (let [{c :creator
                                    :as description} (current-indicator-desc db i)]
                               (cond
                                 (= i indicator-id) (conj res indicator-desc)
                                 (= c creator) (conj res description)
                                 :else res)))
                           []
                           all-indicator-ids)
        empty-name? (str/blank? current-name)
        non-dupl? (not-any? (fn [{n :name
                                  i :id}]
                              (and (not= i indicator-id)
                                   (= n current-name)))
                            indicators)
        custom-desc-valid? (or (not= indicator-type :custom)
                               (and (= indicator-type :custom)
                                    (-> indicator-desc ;checks if custom is not empty
                                        (get-in [:ui-desc :custom])
                                        first
                                        second
                                        str/blank?
                                        not)))
        transform-validation (try
                               {:valid? true
                                :data (transformer/traverse calc-desc values-map validation-desc)}
                               (catch :default e
                                 {:valid? false
                                  :data (ex-data e)}))
        used-dis (loop [result #{}
                        key-val-pairs (vec values-map)]
                   (let [[k val] (first key-val-pairs)
                         is-di-key? (when k
                                      (-> k
                                          name
                                          (str/ends-with? "_di")))]
                     (cond
                       (nil? k) result
                       (vector? val) (recur result
                                            (apply conj
                                                   (rest key-val-pairs)
                                                   (mapcat vec val)))
                       is-di-key? (recur (conj result val)
                                         (rest key-val-pairs))
                       :else (recur result (rest key-val-pairs)))))
        deleted-dis-used (seq (set/intersection removed-datasets used-dis))

        grouping-options (->> (group-attributes db indicator-id)
                              (map :value)
                              set)
        current-selected-options (get-in ui-desc [indicator-type "grouping-select"])
        grouping-selection-valid? (or (empty? current-selected-options)
                                      (every? (fn [{:keys [value]}]
                                                (grouping-options value))
                                              current-selected-options))

        time-options (->> (time-attributes db indicator-id)
                          (map :value)
                          set)
        current-selected-time-options (get-in ui-desc [indicator-type "time-granularity-select"])
        time-granularity-valid? (or (= indicator-type :custom)
                                    (and (seq time-options)
                                         (time-options (:value current-selected-time-options))))

        is-valid? (and (not empty-name?)
                       (not deleted-dis-used)
                       non-dupl?
                       custom-desc-valid?
                       time-granularity-valid?
                       grouping-selection-valid?
                       (or (:valid? transform-validation)
                           (= indicator-type :custom)))]
    {:valid? is-valid?
     :reasons [(when empty-name?
                 [::i18n/translate :non-empty-indicator-name])
               (when deleted-dis-used
                 [::i18n/translate :deleted-di-used-for-attribute])
               (when-not custom-desc-valid?
                 [::i18n/translate :custom-indicator-not-valid])
               (when-not non-dupl?
                 [::i18n/translate :duplicate-indicator-name])
               (when-not grouping-selection-valid?
                 [::i18n/translate :optional-grouping-not-valid])
               (when (and (not time-granularity-valid?)
                          current-selected-time-options)
                 [::i18n/translate :time-granularity-not-valid])
               (when (and (not (:valid? transform-validation))
                          (seq (:data transform-validation)))
                 [::i18n/translate :transform-indicator-invalid])]}))

(re-frame/reg-sub
 ::valid?
 (fn [db [_ indicator-id]]
   (is-indicator-valid? db indicator-id)))

(defn- options-with-extra-infos [all-datasets filtered-attributes options-key options-type]
  (->> all-datasets
       (map (fn [{{attrs options-key} :ui-options
                  di :di}]
              (let [filter-attrs (get filtered-attributes di)]
                [di (mapv #(assoc %
                                  :option-type options-type
                                  :di di)
                          (if (empty? filter-attrs)
                            attrs
                            (filter #(filter-attrs (:value %))
                                    attrs)))])))
       (into {})))

(re-frame/reg-sub
 ::additional-attributes
 (fn [db [_ indicator-id filtered-attributes]] ;filtered-attributes {<di> #{<attr-name>}}
   (let [removed-datasets (get-in db (ip/removed-indicator-data indicator-id) #{})
         all-datasets (datasets-sorted db indicator-id removed-datasets)
         datasets-keys (mapv :di all-datasets)
         options-with-type-fn (partial options-with-extra-infos
                                       all-datasets
                                       filtered-attributes)
         time-attributes (options-with-type-fn :time-attributes :time)
         group-attributes (options-with-type-fn :group-attributes :group)
         calc-attributes (options-with-type-fn :calc-attributes :calc)]
     (vec
      (map-indexed (fn [index dataset-key]
                     (let [group-label (dataset-title index)
                           time-options (get time-attributes dataset-key)
                           group-options (get group-attributes dataset-key)
                           calc-options (get calc-attributes dataset-key)]
                       {:label group-label
                        :di dataset-key
                        :options (->> (conj (concat time-options group-options calc-options)
                                            {:label (i18n/translate db :number-of-events)
                                             :value :number-of-events
                                             :option-type :number-of-events
                                             :di dataset-key})
                                      (mapv #(update % :label i18n/attribute-label))
                                      (sort-by (comp clojure.string/lower-case :label))
                                      vec)}))
                   datasets-keys)))))

(re-frame/reg-sub
 ::calc-attributes
 (fn [db [_ indicator-id filter-datasets {:keys [hide?]}]] ; filter-datasets => [<di>], can be nil
   (let [removed-datasets (get-in db (ip/removed-indicator-data indicator-id) #{})]
     (->> (datasets-sorted db indicator-id ((fnil set/union #{}) filter-datasets removed-datasets))
          (map-indexed (fn [index {{attr-options :calc-attributes} :ui-options
                                   di :di}]
                         {:label (dataset-title index)
                          :di di
                          :options (cond-> (mapv #(assoc % :di di)
                                                 attr-options)
                                     (not hide?)
                                     (conj {:label (i18n/translate db :number-of-events)
                                            :value :number-of-events
                                            :di di}))}))
          (sort-by (comp clojure.string/lower-case :label))
          vec))))

(re-frame/reg-sub
 ::datasets-descriptions
 (fn [db [_ indicator-id]]
   (sorted-dataset-desc db indicator-id)))

(def conj-empty-set (fnil conj #{}))

(re-frame/reg-event-db
 ::remove-dataset
 (fn [db [_ indicator-id dataset-di]]
   (let [dataset-id (dfl-di/ctn->sha256-id dataset-di)
         all-added (get-in db (ip/added-indicator-data indicator-id))
         dataset-added? (contains? all-added dataset-id)]
     (if dataset-added?
       (-> db
           (update-in (ip/added-indicator-data indicator-id) disj dataset-id)
           (update-in (ip/indicator-data indicator-id) dissoc dataset-id))
       (update-in db
                  (ip/removed-indicator-data indicator-id)
                  conj-empty-set
                  dataset-id)))))

(re-frame/reg-event-db
 ::add-dataset
 (fn [db [_ indicator-id dataset-id]]
   (update-in db
              (ip/added-indicator-data indicator-id)
              conj-empty-set
              dataset-id)))

(re-frame/reg-sub
 ::indicator-templates
 (fn [db _]
   (get-in db ip/indicator-ui-templates)))

(re-frame/reg-sub
 ::indicator-template-options
 (fn [db _]
   (let [all-templates (get-in db ip/indicator-ui-templates)]
     (->> all-templates
          (map (fn [[template-key {:keys [label]}]]
                 {:label (i18n/translate db label)
                  :value template-key}))
          (sort-by (comp clojure.string/lower-case :label))
          vec))))

(re-frame/reg-sub
 ::indicator-template-ui-desc
 (fn [db [_ indicator-id]]
   (template-ui-description db indicator-id)))

(re-frame/reg-sub
 ::indicator-type-info
 (fn [[_ indicator-id :as in]]
   [(re-frame/subscribe [::indicator-templates])
    (re-frame/subscribe [::indicator-prop indicator-id :indicator-type])])
 (fn [[all-templates current-type]]
   (get-in all-templates [current-type :info])))

(re-frame/reg-event-fx
 ws-api/broadcast-indicator-updated
 (fn [{db :db} _]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:fx [[:backend-tube [ws-api/all-indicators {:client-callback [ws-api/all-indicators-result]} user-info]]
           [:backend-tube [ws-api/load-indicator-ui-descs {:client-callback [ws-api/loaded-indicator-ui-descs]}]]]})))

(re-frame/reg-event-fx
 ::send-copy
 (fn [{db :db} [_ user-info send-to-names indicator-id]]
   (let [indicator (indicator->final-description db
                                                 indicator-id)]
     {:fx (mapv (fn [user-name]
                  [:backend-tube [ws-api/share-indicator
                                  {:client-callback [ws-api/share-indicator-result]
                                   :broadcast-callback [ws-api/broadcast-indicator-updated]}
                                  user-info {:username (:value user-name)} indicator]])
                send-to-names)})))

(defn notify-vec [notification-desc]
  (fi/call-api :notify-event-vec notification-desc))

(re-frame/reg-event-fx
 ws-api/share-indicator-result
 (fn [{db :db} [_ {:keys [status msg data]}]]
   (let [success-msg  @(re-frame/subscribe [::i18n/translate :send-success-notification])
         error-msg @(re-frame/subscribe [::i18n/translate :send-error-notification])
         user-name (fi/call-api :name-for-user-db-get db (:creator data))]
     (if (= status :success)
       (re-frame/dispatch (notify-vec
                           {:message (-> success-msg
                                         (str/replace #"indicator-name" (:name data))
                                         (str/replace #"user-name" user-name))
                            :category {:indicator :send}
                            :type :success}))
       (re-frame/dispatch (notify-vec
                           {:message error-msg
                            :category {:indicator :send}
                            :type :error}))))))
