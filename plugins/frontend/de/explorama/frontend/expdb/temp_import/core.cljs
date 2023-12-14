(ns de.explorama.frontend.expdb.temp-import.core
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.expdb.path :as path]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button checkbox input-field select upload]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog
                                                                          loading-screen]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [hint]]
            [de.explorama.frontend.ui-base.utils.data-exchange :as data-exchange]
            [de.explorama.frontend.ui-base.utils.select :refer [to-option
                                                                vals->options]]
            [de.explorama.shared.expdb.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :refer [warn]]))

(re-frame/reg-event-fx
 ::upload-file
 (fn [{db :db} [_ meta-data result]]
   {:db (-> (update db path/root dissoc path/imports)
            (assoc-in path/raw-meta-data meta-data)
            (assoc-in path/load-screen true))
    :backend-tube [ws-api/upload-file
                   {:client-callback [ws-api/upload-file-result]
                    :failed-callback [ws-api/upload-file-failed]}
                   meta-data
                   result]}))

(re-frame/reg-event-fx
 ::update-options
 (fn [{db :db} [_ options-type options]]
   {:db (assoc-in db path/load-screen false)
    :backend-tube [ws-api/update-options
                   {:client-callback [ws-api/upload-file-result]
                    :failed-callback [ws-api/upload-file-failed]}
                   (get-in db path/raw-meta-data)
                   options-type
                   options]}))

(re-frame/reg-event-fx
 ::cancel-mapping
 (fn [{db :db} _]
   {:db (update db path/root dissoc path/imports)
    :backend-tube [ws-api/delete-file]
                  {}
                  (get-in db path/raw-meta-data)}))

(re-frame/reg-event-db
 ws-api/delete-file-result
 (fn [db [_ result]]
   (-> (assoc-in db path/load-screen false)
       (assoc-in path/done-screen (assoc result
                                         :type ws-api/delete-file)))))

(re-frame/reg-event-db
 ws-api/upload-file-failed
 (fn [db [_ result]]
   (warn ws-api/upload-file-failed result)
   (assoc-in db path/done-screen (assoc result
                                        :type ws-api/upload-file-failed))))

(defn- transform-types [[type & params]]
  ((case type
     :convert (fn [& _] nil)
     :id-generate (fn [[_ field?]]
                    (transform-types field?))
     :id-rand (fn [& _] nil)
     :fields (fn [[_ fields]]
               fields)
     :field (fn [[field]]
              [field])
     :value (fn [& _] nil)
     :date-schema (fn [[_ field]]
                    (transform-types field))
     :position (fn [[field?]]
                 (transform-types field?))
     :lat-lon (fn [[field-1 field-2]]
                [(first (transform-types field-1))
                 (first (transform-types field-2))]))
   params))

(defn- transform-mapping [mapping]
  (let [item (get-in mapping [:mapping :items 0])]
    (reduce-kv (fn [acc node values]
                 (reduce (fn [acc desc]
                           (let [row (transform-types (case node
                                                        :facts (:value desc)
                                                        :locations (:point desc)
                                                        :contexts (:name desc)
                                                        :dates (:value desc)
                                                        :texts desc))]
                             (cond (and (= :locations node)
                                        (= :lat-lon (first (:point desc))))
                                   (-> acc
                                       (assoc-in [:mapped (first row)]
                                                 {:desc desc
                                                  :node node
                                                  :lat-lon :lat
                                                  :include? true})
                                       (assoc-in [:mapped (second row)]
                                                 {:desc desc
                                                  :node node
                                                  :lat-lon :lon
                                                  :include? true}))
                                   (vector? row)
                                   (reduce (fn [acc row]
                                             (assoc-in acc [:mapped row] {:desc desc
                                                                          :node node
                                                                          :include? true}))
                                           acc
                                           row)
                                   :else
                                   (update acc :generated conj {:desc desc
                                                                :node node
                                                                :include? true}))))
                         acc
                         values))
               (if-let [row (transform-types (:global-id item))]
                 {:generated [{:desc (:global-id item)
                               :include? false
                               :node :global-id}]
                  :mapped {(first row) {:desc (:global-id item)
                                        :node :global-id
                                        :include? true}}}
                 {:generated [{:desc (:global-id item)
                               :include? true
                               :node :global-id}]
                  :mapped {}})
               (get-in item [:features 0]))))

(defn- generate-mapping [db data-source options]
  (let [{:keys [mapped generated]} (get-in db path/current-mapping)
        id? (seq (filter (fn [[_ v]]
                           (and (= :global-id (:node v))
                                (:include? v)))
                         mapped))
        date? (seq (filter (fn [[_ v]]
                             (and (= :dates (:node v))
                                  (:include? v)))
                           mapped))
        country? (seq (filter (fn [[_ v]]
                                (and (= :contexts (:node v))
                                     (= (get-in v [:desc :type 1]) "country")
                                     (:include? v)))
                              mapped))
        lat-lon? (let [result (reduce (fn [acc [row {:keys [node desc include? lat-lon]}]]
                                        (if (and (= node :locations)
                                                 include?
                                                 (= :lat-lon (get-in desc [:point 0])))
                                          (assoc acc row lat-lon)
                                          acc))
                                      {}
                                      mapped)]
                   (or  (= 0 (count result))
                        (and (= 2 (count result))
                             (= #{:lat :lon} (set (vals result))))))
        items (reduce (fn [acc {node :node
                                include? :include?
                                lat-lon :lat-lon
                                desc :desc}]
                        (cond (and include?
                                   (= node :locations)
                                   lat-lon)
                              (cond (not lat-lon?)
                                    acc
                                    (#{:lat :lon} lat-lon)
                                    (update-in acc [:features 0 :locations]
                                               (fn [locations]
                                                 (if (or (empty? locations)
                                                         (empty? (filter (fn [{[type] :point}]
                                                                           (= :lat-lon type))
                                                                         locations)))
                                                   (conj (or locations []) desc)
                                                   (mapv (fn [{[type] :point :as cdesc}]
                                                           (cond (and (= type :lat-lon)
                                                                      (= :lat lat-lon))
                                                                 (assoc-in cdesc [:point 1 1] (get-in desc [:point 1 1]))
                                                                 (and (= type :lat-lon)
                                                                      (= :lon lat-lon))
                                                                 (assoc-in cdesc [:point 2 1] (get-in desc [:point 2 1]))
                                                                 :else
                                                                 cdesc))
                                                         locations))))
                                    :else
                                    acc)
                              include?
                              (case node
                                :global-id (assoc-in acc [:global-id] desc)
                                :facts (update-in acc [:features 0 :facts] (fnil conj []) desc)
                                :locations (update-in acc [:features 0 :locations] (fnil conj []) desc)
                                :contexts (update-in acc [:features 0 :contexts] (fnil conj []) desc)
                                :dates (update-in acc [:features 0 :dates] (fnil conj []) desc)
                                :texts (update-in acc [:features 0 :texts] (fnil conj []) desc))
                              :else
                              acc))
                      {:features [{}]}
                      (cond-> (if id?
                                (map #(if (= :global-id (:node %))
                                        (assoc % :include? false)
                                        %)
                                     generated)
                                (map #(if (= :global-id (:node %))
                                        (assoc % :include? true)
                                        %)
                                     generated))
                        :always
                        (into (vals mapped))

                        (not date?)
                        (conj {:node :dates
                               :include? true
                               :desc {:value [:value (subs (.toISOString (js/Date. (.now js/Date))) 0 10)]
                                      :type [:value "occured-at"]}})
                        (not country?)
                        (conj {:node :contexts
                               :include? true
                               :desc {:name [:value "unspecified"]
                                      :global-id [:id-generate ["country" :text] :name]
                                      :type [:value "country"]}})))]
    {:tests [id? date? country? lat-lon?]
     :mapping {:meta-data (case (:type options)
                            :csv {:file-format :csv
                                  :csv {:separator (:separator options)
                                        :quote (:quote options)}})
               :mapping {:datasource {:name [:value data-source]
                                      :global-id [:value (str "source-" (str/lower-case data-source))]}
                         :items [items]}}}))

(re-frame/reg-event-db
 ws-api/upload-file-result
 (fn [db [_ mapping data]]
   (-> (assoc-in db path/current-mapping (transform-mapping mapping))
       (assoc-in path/current-header (-> data first (dissoc :row-number) keys))
       (assoc-in path/current-data data)
       (assoc-in path/raw-mapping mapping)
       (assoc-in path/meta-data (get mapping :meta-data))
       (assoc-in path/datasource (get-in mapping [:mapping :datasource]))
       (assoc-in path/load-screen false))))

(re-frame/reg-sub
 ::current-mapping?
 (fn [db _]
   (boolean (get-in db path/current-mapping))))

(re-frame/reg-sub
 ::uploaded-mapping?
 (fn [db _]
   (boolean (get-in db path/uploaded-mapping))))

(re-frame/reg-event-fx
 ::import-data-uploaded
 (fn [{db :db} _] ;TODO r1/mapping uploading a mapping
   {:db (assoc-in db path/load-screen true)
    :backend-tube [ws-api/import-file
                   {:client-callback [ws-api/import-file-result]}
                   (get-in db path/raw-meta-data)
                   (get-in db path/uploaded-mapping)]}))

(re-frame/reg-event-db
 ::cancel-uploaded
 (fn [db _]
   (assoc-in db path/uploaded-mapping nil)))

(re-frame/reg-event-db
 ::upload-mapping
 (fn [db [_ mapping]]
   (assoc-in db path/uploaded-mapping mapping)))

(re-frame/reg-event-fx
 ::download-mapping
 (fn [{db :db}]
   (data-exchange/download-content
    (str (.toISOString (js/Date. (.now js/Date))) "-current-mapping.edn")
    (:mapping (generate-mapping db
                                (get-in db path/datasource-name)
                                (get-in db path/options))))
   {}))

(re-frame/reg-sub
 ::current-mapping
 (fn [db _]
   (get-in db path/current-mapping)))

(re-frame/reg-sub
 ::current-header
 (fn [db _]
   (get-in db path/current-header)))

(re-frame/reg-sub
 ::current-data
 (fn [db _]
   (get-in db path/current-data)))

(re-frame/reg-sub
 ::meta-data
 (fn [db _]
   (get-in db path/meta-data)))

(re-frame/reg-sub
 ::datasource
 (fn [db _]
   (get-in db path/datasource)))

(re-frame/reg-sub
 ::show-dialog
 (fn [db _]
   (get-in db path/show-dialog)))

(re-frame/reg-event-db
 ::show-dialog
 (fn [db [_ message params]]
   (assoc-in db path/show-dialog [message params])))

(re-frame/reg-event-db
 ::hide-dialog
 (fn [db _]
   (assoc-in db path/show-dialog nil)))

(re-frame/reg-event-db
 ::hide-import-summary
 (fn [db _]
   (assoc-in db path/import-summary nil)))

(re-frame/reg-event-db
 ::options
 (fn [db [_ options]]
   (assoc-in db path/options options)))

(re-frame/reg-event-db
 ::datasource-name
 (fn [db [_ datasource-name]]
   (assoc-in db path/datasource-name datasource-name)))

(re-frame/reg-event-db
 ::hide-load-screen
 (fn [db _]
   (assoc-in db path/load-screen nil)))

(re-frame/reg-event-db
 ::hide-done-screen
 (fn [db _]
   (assoc-in db path/done-screen nil)))

(re-frame/reg-sub
 ::load-screen
 (fn [db _]
   (get-in db path/load-screen)))

(re-frame/reg-sub
 ::done-screen
 (fn [db _]
   (get-in db path/done-screen)))

(re-frame/reg-sub
 ::import-summary
 (fn [db _]
   (get-in db path/import-summary)))

(re-frame/reg-event-db
 ::update-field
 (fn [db [_ updates]]
   (reduce (fn [db [header-name field value]]
             (assoc-in db
                       (into (conj path/current-mapping
                                   :mapped
                                   header-name)
                             field)
                       value))
           db
           updates)))

(re-frame/reg-event-db
 ::include-col
 (fn [db [_ header-name value]]
   (assoc-in db
             (conj path/current-mapping
                   :mapped
                   header-name
                   :include?)
             value)))

(re-frame/reg-event-db
 ::change-col-type
 (fn [db [_ header-name col-type]]
   (let [{old-node :node :as old-value}
         (get-in db
                 (conj path/current-mapping
                       :mapped
                       header-name))]
     ;TODO r1/mapping define more custom transitions
     (assoc-in db
               (conj path/current-mapping
                     :mapped
                     header-name)
               (cond (and (= old-node :contexts)
                          (= col-type :facts))
                     {:node :facts
                      :include? true
                      :desc {:type [:value "string"]
                             :name [:value (get-in old-value [:desc :type 1])]
                             :value [:field header-name]}}
                     (and (= old-node :locations)
                          (= col-type :facts))
                     {:node :facts
                      :include? true
                      :desc {:type [:value "decimal"]
                             :name [:value header-name]
                             :value [:field header-name]}}
                     (= col-type :facts)
                     {:node :facts
                      :include? true
                      :desc {:type [:value "string"]
                             :name [:value header-name]
                             :value [:field header-name]}}
                     (= col-type :contexts)
                     {:node :contexts
                      :include? true
                      :desc {:name [:field header-name]
                             :global-id [:id-generate [(str/lower-case header-name) :text] [:field header-name]]
                             :type [:value (str/lower-case header-name)]}}
                     (= col-type :locations)
                     {:node :locations
                      :include? true
                      :desc {:point [:position
                                     [:field header-name]]}}
                     (= col-type :dates)
                     {:node :dates
                      :include? true
                      :desc {:value [:date-schema "YYYY-MM-dd" header-name]
                             :type [:value "occured-at"]}}
                     (= col-type :texts)
                     {:node :texts
                      :include? true
                      :desc [:field header-name ""]}
                     (= col-type :global-id)
                     {:node :global-id
                      :include? true
                      :desc [:field header-name]})))))

(re-frame/reg-event-fx
 ::import-mapping
 (fn [{db :db} [_ data-source options]]
   (let [raw-meta-data (get-in db path/raw-meta-data)
         {[id? date? country? lat-lon?] :tests
          :keys [mapping]}
         (generate-mapping db data-source options)
         warnings? (or (not id?) (not date?) (not country?))
         message (cond-> [:expdb-import-dialog-intro]
                   (not id?)
                   (conj :expdb-import-dialog-id)
                   (not date?)
                   (conj :expdb-import-dialog-date)
                   (not country?)
                   (conj :expdb-import-dialog-country)
                   (not lat-lon?)
                   (conj :expdb-import-dialog-lat-lon)
                   :always
                   (conj :expdb-import-dialog-outro))]
     (cond-> {}
       warnings?
       (assoc :dispatch [::show-dialog message [raw-meta-data mapping]])
       (not warnings?)
       (assoc :backend-tube [ws-api/import-file
                             {:client-callback [ws-api/import-file-result]}
                             raw-meta-data
                             mapping]
              :db (assoc-in db path/load-screen true))))))

(re-frame/reg-event-fx
 ::proceed
 (fn [{db :db} [_ [raw-meta-data mapping]]]
   {:db (assoc-in db path/load-screen true)
    :backend-tube [ws-api/import-file
                   {:client-callback [ws-api/import-file-result]}
                   raw-meta-data
                   mapping]}))

(re-frame/reg-event-fx
 ::cancel
 (fn [_ _]
   {}))

(re-frame/reg-event-db
 ws-api/import-file-result
 (fn [db [_ result]]
   (-> (assoc-in db path/import-summary result)
       (assoc-in path/load-screen nil))))

(re-frame/reg-event-db
 ws-api/commit-import-result
 (fn [db [_ result]]
   (-> (assoc-in db path/done-screen result)
       (assoc-in path/load-screen nil))))

(re-frame/reg-event-fx
 ::commit-import
 (fn [{db :db} _]
   {:db (assoc-in db path/load-screen true)
    :backend-tube [ws-api/commit-import
                   {:client-callback [ws-api/commit-import-result]}]}))

(re-frame/reg-event-fx
 ::abort-import
 (fn [{db :db} _]
   {:db (assoc-in db path/load-screen true)
    :backend-tube [ws-api/cancel-import
                   {:client-callback [ws-api/cancel-import-result]}]}))

(re-frame/reg-event-db
 ws-api/cancel-import-result
 (fn [db _]
   (assoc-in db path/load-screen nil)))

(defn- warning-dialog []
  (let [show-dialog @(re-frame/subscribe [::show-dialog])
        translations
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-import-dialog-intro :expdb-import-dialog-id :expdb-import-dialog-date
                              :expdb-import-dialog-country :expdb-import-dialog-outro :expdb-import-dialog-title])]
    [dialog
     {:show? (boolean show-dialog)
      :hide-fn #(re-frame/dispatch [::hide-dialog])
      :yes {:on-click #(re-frame/dispatch [::proceed (second show-dialog)])}
      :no {:on-click #(re-frame/dispatch [::cancel])}
      :title (:expdb-import-dialog-title translations)
      :message (reduce (fn [div message-part]
                         (conj div
                               (get translations message-part)
                               [:br]))
                       [:div]
                       (first show-dialog))}]))

(defn- csv-options [options]
  [:<>
   [input-field {:label "separator"
                 :value (:separator @options)
                 :on-change (fn [e]
                              (swap! options assoc :separator e)
                              (re-frame/dispatch [::update-options :csv @options]))
                 :on-blur (fn [_]
                            (re-frame/dispatch [::options @options]))}]
   [input-field {:label "quote"
                 :value (:quote @options)
                 :on-change (fn [e]
                              (swap! options assoc :quote e))
                 :on-blur (fn [_]
                            (re-frame/dispatch [::options @options])
                            (re-frame/dispatch [::update-options :csv @options]))}]])

(defn- col-view []
  (let [header @(re-frame/subscribe [::current-header])
        col-name-label @(re-frame/subscribe [::i18n/translate :expdb-import-table-column-name])]
    (into [:tr]
          (map (fn [header-name]
                 (if (= :head header-name)
                   [:th col-name-label]
                   [:th header-name]))
               (cons :head header)))))

(defn- include-view []
  (let [header @(re-frame/subscribe [::current-header])
        {mapped :mapped} @(re-frame/subscribe [::current-mapping])
        include-label @(re-frame/subscribe [::i18n/translate :expdb-import-table-include])]
    (into [:tr]
          (map (fn [header-name]

                 (if (= :head header-name)
                   [:th include-label]
                   [:th [checkbox {:as-toggle? true
                                   :checked? (get-in mapped [header-name :include?])
                                   :on-change (fn [new-state]
                                                (re-frame/dispatch [::include-col
                                                                    header-name
                                                                    new-state]))}]]))
               (cons :head header)))))

(defn- label-view []
  (let [header @(re-frame/subscribe [::current-header])
        {mapped :mapped} @(re-frame/subscribe [::current-mapping])
        {:keys [expdb-import-table-label expdb-import-table-context expdb-import-table-fact
                expdb-import-table-date expdb-import-table-text expdb-import-table-location
                expdb-import-table-global-id]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-import-table-label :expdb-import-table-context
                              :expdb-import-table-fact :expdb-import-table-date
                              :expdb-import-table-text :expdb-import-table-location
                              :expdb-import-table-global-id])]
    (into [:tr]
          (map (fn [header-name]
                 (if (= :head header-name)
                   [:th expdb-import-table-label]
                   (let [{:keys [node]} (get mapped header-name)]
                     [:th [select {:options (vals->options [[:contexts expdb-import-table-context]
                                                            [:facts expdb-import-table-fact]
                                                            [:dates expdb-import-table-date]
                                                            [:texts expdb-import-table-text]
                                                            [:locations expdb-import-table-location]
                                                            [:global-id expdb-import-table-global-id]])
                                   :values (if node
                                             (to-option node (case node
                                                               :facts "Fact"
                                                               :contexts "Context"
                                                               :dates "Date"
                                                               :texts "Text"
                                                               :locations "Location"
                                                               :global-id "Global ID"
                                                               nil))
                                             nil)
                                   :on-change (fn [{value :value}]
                                                (re-frame/dispatch [::change-col-type
                                                                    header-name
                                                                    value]))
                                   :extra-class "input--w10"}]])))
               (cons :head header)))))

(defn- name-view-field [header-name]
  (let [edit-name? (r/atom nil)]
    (fn []
      (let [{mapped :mapped} @(re-frame/subscribe [::current-mapping])
            {:keys [desc node]} (get mapped header-name)
            attribute (case node
                        :facts (get-in desc [:name 1])
                        :contexts (get-in desc [:type 1])
                        nil)]
        [:th (if attribute
               (if @edit-name?
                 [:div {:style {:display :flex
                                :flex-direction :row
                                :align-items :center}}
                  [input-field {:value @edit-name?
                                :on-change (fn [e]
                                             (reset! edit-name? e))}]
                  [button {:start-icon :check
                           :on-click (fn [_]
                                       (re-frame/dispatch [::update-field
                                                           [[header-name
                                                             (case node
                                                               :facts [:desc :name 1]
                                                               :contexts [:desc :type 1]
                                                               nil)
                                                             @edit-name?]]])
                                       (reset! edit-name? nil))}]]
                 [:span {:on-double-click (fn [_]
                                            (reset! edit-name? attribute))
                         :style {:cursor :pointer}}
                  attribute])
               "")]))))

(defn- name-view []
  (let [header @(re-frame/subscribe [::current-header])
        attribute-label @(re-frame/subscribe [::i18n/translate :expdb-import-table-attribute])]
    (into [:tr]
          (map (fn [header-name]
                 (if (= :head header-name)
                   [:th attribute-label]
                   [name-view-field header-name])))
          (cons :head header))))

(defn- type-view-field [header-name]
  (let [edit-name? (r/atom false)]
    (fn []
      (let [{mapped :mapped} @(re-frame/subscribe [::current-mapping])
            {:keys [desc node lat-lon]} (get mapped header-name)
            {:keys [expdb-import-table-string expdb-import-table-integer expdb-import-table-decimal
                    expdb-import-table-lat expdb-import-table-lon expdb-import-table-position]}
            @(re-frame/subscribe [::i18n/translate-multi
                                  :expdb-import-table-string :expdb-import-table-integer :expdb-import-table-decimal
                                  :expdb-import-table-lat :expdb-import-table-lon :expdb-import-table-position])]
        [:th (case node
               :facts [:<>
                       [select {:options (vals->options [["string" expdb-import-table-string]
                                                         ["integer" expdb-import-table-integer]
                                                         ["decimal" expdb-import-table-decimal]])
                                :values (to-option (get-in desc [:type 1])
                                                   (get {"string" expdb-import-table-string
                                                         "integer" expdb-import-table-integer
                                                         "decimal" expdb-import-table-decimal}
                                                        (get-in desc [:type 1])))
                                :extra-class "input--w10"
                                :on-change (fn [{value :value}]
                                             (re-frame/dispatch [::update-field
                                                                 [[header-name
                                                                   [:desc :type 1]
                                                                   value]]]))}]]
               :locations [select {:options (vals->options [[:lat expdb-import-table-lat]
                                                            [:lon expdb-import-table-lon]
                                                            [:position expdb-import-table-position]])
                                   :values (cond (= :position (get-in desc [:point 0]))
                                                 (to-option :position expdb-import-table-position)
                                                 (and (= :lat-lon (get-in desc [:point 0]))
                                                      (= lat-lon :lat))
                                                 (to-option :lat expdb-import-table-lat)
                                                 (and (= :lat-lon (get-in desc [:point 0]))
                                                      (= lat-lon :lon))
                                                 (to-option :lon expdb-import-table-lon))
                                   :extra-class "input--w10"
                                   :on-change (fn [{value :value}]
                                                (case value
                                                  :lat
                                                  (re-frame/dispatch [::update-field
                                                                      [[header-name
                                                                        [:desc :point 1 1]
                                                                        header-name]
                                                                       [header-name
                                                                        [:desc :point 0]
                                                                        :lat-lon]
                                                                       [header-name
                                                                        [:lat-lon]
                                                                        :lat]]])
                                                  :lon
                                                  (re-frame/dispatch [::update-field
                                                                      [[header-name
                                                                        [:desc :point 2 1]
                                                                        header-name]
                                                                       [header-name
                                                                        [:desc :point 0]
                                                                        :lat-lon]
                                                                       [header-name
                                                                        [:lat-lon]
                                                                        :lon]]])
                                                  :position
                                                  (re-frame/dispatch [::update-field
                                                                      [[header-name
                                                                        [:desc :point 1 1]
                                                                        header-name]
                                                                       [header-name
                                                                        [:desc :point]
                                                                        [:position [:field header-name]]]]])))}]
               :dates (if @edit-name?
                        [:div {:style {:display :flex
                                       :flex-direction :row
                                       :align-items :center}}
                         [input-field {:value @edit-name?}]
                         [button {:start-icon :check
                                  :on-click (fn [_]
                                              (reset! edit-name? nil)
                                              (re-frame/dispatch [::update-field
                                                                  [[header-name
                                                                    [:desc :value 1]
                                                                    @edit-name?]]]))}]]
                        [:span {:on-double-click (fn [_]
                                                   (reset! edit-name? (get-in desc [:value 1])))
                                :style {:cursor :pointer}}
                         (get-in desc [:value 1])])
               "")]))))

(defn- type-view []
  (let [header @(re-frame/subscribe [::current-header])
        type-label @(re-frame/subscribe [::i18n/translate :expdb-import-table-type])]
    (into [:tr]
          (map (fn [header-name]
                 (if (= :head header-name)
                   [:th type-label]
                   [type-view-field header-name])))
          (cons :head header))))


(defn- table-view []
  (let [header @(re-frame/subscribe [::current-header])
        data @(re-frame/subscribe [::current-data])]
    [:div {:style {:overflow :scroll}}
     [:table.explorama__table.explorama__table--bordered
      [:thead
       [col-view]
       [include-view]
       [label-view]
       [type-view]
       [name-view]]
      [:tbody
       (into [:<>]
             (map (fn [row]
                    (into [:tr]
                          (map (fn [header-name]
                                 (if (= :head header-name)
                                   [:td]
                                   [:td (get row header-name)]))
                               (cons :head header))))
                  data))]]]))

(defn- done-dialog []
  (let [done-screen @(re-frame/subscribe [::done-screen])
        {:keys [expdb-import-done-title
                expdb-import-done-success
                expdb-import-done-error]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-import-done-title
                              :expdb-import-done-success
                              :expdb-import-done-error])]
    [dialog
     {:show? (boolean done-screen)
      :hide-fn #(re-frame/dispatch [::hide-done-screen])
      :title expdb-import-done-title
      :message (if (:success done-screen)
                 (str expdb-import-done-success " " (get done-screen :data))
                 expdb-import-done-error)}]))

(defn- import-summary []
  (let [{success :success :as import-summary} @(re-frame/subscribe [::import-summary])
        {:keys [expdb-import-summary-title-success
                expdb-import-summary-title-failed
                expdb-import-summary-success
                expdb-import-summary-warning
                expdb-import-summary-error
                expdb-import-summary-import-report-new
                expdb-import-summary-mapping-report-ignored
                expdb-import-summary-mapping-report-ignored-download
                expdb-import-summary-procced]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-import-summary-title-success
                              :expdb-import-summary-title-failed
                              :expdb-import-summary-success
                              :expdb-import-summary-warning
                              :expdb-import-summary-error
                              :expdb-import-summary-import-report-new
                              :expdb-import-summary-mapping-report-ignored
                              :expdb-import-summary-mapping-report-ignored-download
                              :expdb-import-summary-procced])]
    [dialog
     {:show? (boolean import-summary)
      :hide-fn #(re-frame/dispatch [::hide-import-summary])
      :title (if success
               expdb-import-summary-title-success
               expdb-import-summary-title-failed)
      :message (cond-> [:<>]
                 (and success
                      (seq (:mapping-errors import-summary)))
                 (conj [:h3 expdb-import-summary-warning])
                 (not success)
                 (conj [:h3 expdb-import-summary-error])
                 (and success
                      (empty? (:mapping-errors import-summary)))
                 (conj [:h3 expdb-import-summary-success])
                 :always (conj [:br])
                 success
                 (conj (str expdb-import-summary-import-report-new ": " (get import-summary :events))
                       [:br])
                 (seq (:mapping-errors import-summary))
                 (conj [hint {:variant :info
                              :content [:div.flex.flex-row.align-items-center
                                        {:style {:color "#000000"}}
                                        (str expdb-import-summary-mapping-report-ignored ": " (count (:mapping-errors import-summary)))
                                        [button {:title expdb-import-summary-mapping-report-ignored-download
                                                 :extra-class "ml-6"
                                                 :start-icon :download
                                                 :size :small
                                                 :variant :tertiary
                                                 :icon-params {:color-important? true
                                                               :color :black}
                                                 :on-click (fn []
                                                             (data-exchange/download-content
                                                              (str (.toISOString (js/Date. (.now js/Date))) "-mapping-error-log.edn")
                                                              (:mapping-errors import-summary)))}]]}]
                       [:br])
                 success
                 (conj expdb-import-summary-procced))
      :yes {:disabled? (not success)
            :on-click #(re-frame/dispatch [::commit-import])}
      :cancel {:on-click #(re-frame/dispatch [::abort-import])}}]))

(defn- load-screen []
  (let [show? @(re-frame/subscribe [::load-screen])]
    ;TODO r1/mapping progress bar
    [loading-screen {:show? show?}]))

(defn- mapping-view []
  (let [meta-data @(re-frame/subscribe [::meta-data])
        datasource @(re-frame/subscribe [::datasource])
        data-source (r/atom (get-in datasource [:name 1]))
        options (r/atom (case (:file-format meta-data)
                          :csv {:type :csv
                                :separator (get-in meta-data [:csv :separator])
                                :quote (get-in meta-data [:csv :quote])}))]
    (re-frame/dispatch [::datasource-name @data-source])
    (re-frame/dispatch [::options @options])
    (fn []
      (let [meta-data @(re-frame/subscribe [::meta-data])
            {:keys [expdb-import-misc-datasource expdb-import-misc-import]}
            @(re-frame/subscribe [::i18n/translate-multi
                                  :expdb-import-misc-datasource
                                  :expdb-import-misc-import])]
        [:<>
         [:div.flex.flex-row.flex-nowrap.justify-evenly.align-items-start
          [input-field {:label expdb-import-misc-datasource
                        :value @data-source
                        :on-change (fn [e]
                                     (reset! data-source e))
                        :on-blur (fn [_]
                                   (re-frame/dispatch [::datasource-name @data-source]))}]
          (case (:type meta-data)
            :csv [csv-options options]
            [csv-options options])]
         [table-view]
         [:div.footer
          [button {:label expdb-import-misc-import
                   :variant :primary
                   :size :big
                   :on-click (fn []
                               (re-frame/dispatch [::import-mapping
                                                   @data-source
                                                   @options]))}]]]))))

(defn- upload-view []
  [upload {:multi-files? false
           :on-file-loaded (fn [result meta-data]
                             (re-frame/dispatch [::upload-file
                                                 (dissoc meta-data :file :last-modified)
                                                 result]))
           :file-type [".csv"]
           :local-read-as :string}])

(defn- uploaded-mapping-view []
  (let [{:keys [expdb-import-misc-import
                expdb-import-misc-cancel
                expdb-import-uploaded-hint]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-import-misc-import
                              :expdb-import-misc-cancel
                              :expdb-import-uploaded-hint])]
    [:div.flex.flex-column.flex-nowrap.justify-evenly.align-items-center
     [:h3 expdb-import-uploaded-hint]
     [:div.flex.flex-row
      {:style {:padding 5
               :gap 10}}
      [button {:label expdb-import-misc-import
               :on-click (fn [_]
                           (re-frame/dispatch [::import-data-uploaded]))}]
      [button {:label expdb-import-misc-cancel
               :on-click (fn [_]
                           (re-frame/dispatch [::cancel-uploaded]))}]]]))

(re-frame/reg-event-fx
 ::toggle-view
 (fn [{db :db} _]
   (let [is-active? (not (get-in db path/show-view?))]
     {:db (assoc-in db path/show-view? is-active?)
      :dispatch-n [(fi/call-api :overlayer-active-event-vec is-active?)]})))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get-in db path/show-view? false)))

(defn- section-title-bar [can-cancel?]
  (let [{:keys [expdb-import-misc-download-mapping expdb-import-misc-upload-mapping
                expdb-import-misc-import
                expdb-import-misc-cancel]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-import-misc-download-mapping
                              :expdb-import-misc-upload-mapping
                              :expdb-import-misc-import
                              :expdb-import-misc-cancel])]
    [:div.flex.justify-between
     [:h2
      expdb-import-misc-import]
     [:div.actions.flex.align-items-baseline
      (when can-cancel?
        [:<>
         [button {:label expdb-import-misc-download-mapping
                  :start-icon :download
                  :size :small
                  :variant :tertiary
                  :on-click (fn [_]
                              (re-frame/dispatch [::download-mapping]))}]
         [upload {:multi-files? false
                  :variant :button
                  :upload-button-params {:label expdb-import-misc-upload-mapping
                                         :start-icon :upload
                                         :variant :tertiary
                                         :size :small}
                  :on-file-loaded (fn [result _]
                                    (re-frame/dispatch [::upload-mapping (edn/read-string result)]))
                  :file-type [".edn"]
                  :local-read-as :string}]
         [button
          {:label expdb-import-misc-cancel
           :start-icon :reset
           :variant :secondary
           :on-click (fn [_]
                       (re-frame/dispatch [::cancel-mapping]))}]])
      [button {:variant :tertiary
               :start-icon :close
               :label @(re-frame/subscribe [::i18n/translate :close])
               :on-click #(re-frame/dispatch [::toggle-view])}]]]))

;;just for make recompiling more comfortable
(defn view-impl []
  (let [current-mapping? @(re-frame/subscribe [::current-mapping?])
        uploaded-mapping? @(re-frame/subscribe [::uploaded-mapping?])]
    [:div.welcome__page {:style {:z-index 3
                                 :position :absolute}}
     [:div.welcome__panel
      [:div.welcome__section.projects
       [section-title-bar (or uploaded-mapping? current-mapping?)]
       ;;TODO r1/css classes + structure more dynamic
       [:div.projects-container>div.projects-grid.flex
        {:style {:display :flex
                 :flex-direction :column
                 :height "-webkit-fill-available"}}
        [import-summary]
        [warning-dialog]
        [done-dialog]
        [load-screen]
        (cond uploaded-mapping?
              [uploaded-mapping-view]
              current-mapping?
              [mapping-view]
              :else
              [upload-view])]]]]))

(defn view []
  (when @(re-frame/subscribe [::is-active?])
    [view-impl]))
  
      