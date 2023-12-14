(ns de.explorama.frontend.woco.frame.filter.core
  (:require [clojure.set :as set]
            [clojure.string :as clj-str]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.filter.util :as util]
            [de.explorama.frontend.woco.path :as path]))

(defn options<- [string-list]
  (->> string-list
       (sort-by #(clj-str/lower-case %))
       (mapv
        (fn [val]
          {:value val :label val}))))

(defn handle-string [elem-v val]
  (if (set? val)
    (if (vector? elem-v)
      (set/union val
                 (set elem-v))
      (conj val elem-v))
    (if (vector? elem-v)
      (set elem-v)
      #{elem-v})))

(defn handle-min-max [[min max] elem-v val]
  (if (vector? val)
    (if (vector? elem-v)
      [(apply min (concat elem-v val))
       (apply max (concat elem-v val))]
      [(apply min (conj val elem-v))
       (apply max (conj val elem-v))])
    (if (vector? elem-v)
      [(apply min elem-v)
       (apply max elem-v)]
      [(apply min [elem-v])
       (apply max [elem-v])])))

(defn conditions-handler [result key val]
  (cond
    (= :date key) (conj result [key {:std {:func (partial handle-min-max
                                                          [util/date-min util/date-max])
                                           :type :date}
                                     :year {:func (partial handle-min-max
                                                           [util/year-min util/year-max])
                                            :type :year
                                            :name :contraints-year-filter}}])
    (string? val) (conj result [key {:std {:func handle-string
                                           :type :string}}])
    (number? val) (conj result [key {:std {:func (partial handle-min-max
                                                          [min max])
                                           :type :number}}])
    :else (conj result [key {:std {:func handle-string
                                   :type :string}}])))

(defn handler-map-builder [event]
  (reduce (fn [result [key val]]
            (if (vector? val)
              (conditions-handler result key (first val))
              (conditions-handler result key val)))
          []
          event))

(defn update-func [handler-map key fkey val cval]
  ((get-in handler-map [key fkey :func]) val cval))

(defn get-data-acs [db frame-id]
  (let [data-ac-path
        (get-in db (path/vertical-plugin-api frame-id :filter :data-acs-path))]
    (get-in db (data-ac-path frame-id))))

(defn data-acs
  [data]
  (let [handler-vec (handler-map-builder (as-> data $
                                           (first $)))
        handler-map (into {} handler-vec)]
    (reduce (fn [result elem]
              (reduce (fn [result [key val]]
                        (reduce (fn [result [fkey fval]]
                                  (update-in result
                                             [key fkey :vals]
                                             (partial update-func handler-map key fkey val)))
                                result
                                (get result key)))
                      result
                      elem))
            (reduce (fn [result [key val]]
                      (reduce (fn [result [fkey val]]
                                (assoc-in result [key fkey] (select-keys val [:type :name])))
                              result
                              val))
                    {}
                    handler-vec)
            data)))

(defn default-ui-value [type vals text-search?]
  (cond
    (= type :number) (clj->js vals)
    (and text-search? (= type :string)) {:text-search? true}
    (= type :string) (options<- vals)
    (= type :date) (let [[min-date max-date] vals]
                     {:start-date (util/date<- min-date)
                      :end-date (util/date<- max-date)})
    :else vals))

(defn default-ui-selection [data-acs]
  (reduce (fn [result [key contraints]]
            (reduce (fn [result [contraint-key {ftype :type fvals :vals text-search? :text-search?}]]
                      (assoc-in result
                                [key contraint-key]
                                (default-ui-value ftype fvals text-search?)))
                    result
                    contraints))
          {}
          data-acs))

(defn in? [{data-acs-type :type data-acs-vals :vals text-search? :text-search? :as data-acs-value}
           ui-value]
  (cond
    (nil? data-acs-type)
    [false ui-value]
    (#{:number :year} data-acs-type)
    (let [{[ac-min ac-max] :vals} data-acs-value
          [ui-min ui-max] (js->clj ui-value)
          start-bounds (<= ac-min ui-min)
          end-bounds (>= ac-max ui-max)]
      [(and start-bounds
            end-bounds)
       [(when-not start-bounds
          [ac-min ui-min])
        (when-not end-bounds
          [ac-max ui-max])]])
    (and (= :string data-acs-type)
         text-search?)
    [(not (clj-str/blank?  ui-value)) ui-value]
    (#{:string} data-acs-type)
    (let [{not-in false}
          (group-by #(boolean ((into #{} data-acs-vals) %))
                    (mapv #(get % :value)
                          ui-value))]
      [(empty? not-in) not-in])
    (#{:date} data-acs-type)
    (let [{:keys [start-date end-date]} ui-value
          {[ac-min ac-max] :vals} data-acs-value
          start-bounds (.isBetween (util/date<- start-date)
                                   ac-min ac-max nil "[]")
          end-bounds (.isBetween (util/date<- end-date)
                                 ac-min ac-max nil "[]")]
      [(and
        start-bounds
        end-bounds)
       [(when-not start-bounds
          start-date)
        (when-not end-bounds
          end-date)]])
    :else [true]))

(defn update-filter [{:keys [selected-ui-attributes] selected-ui-old :selected-ui :as filter-desc}
                     data-acs]
  (let [selected-ui (if selected-ui-old selected-ui-old (default-ui-selection data-acs))
        {user-definied true
         not-user-user-definied false}
        (group-by (fn [element]
                    (boolean ((set selected-ui-attributes)
                              element)))
                  (reduce (fn [result [attr constraints]]
                            (reduce (fn [result [contraint-key _]]
                                      (conj result [attr contraint-key]))
                                    result
                                    constraints))
                          []
                          selected-ui))]
    (reduce (fn [filter-desc [attribute contraint-key]]
              (assoc-in filter-desc
                        [:selected-ui attribute contraint-key]
                        (default-ui-value (get-in data-acs [attribute contraint-key :type])
                                          (get-in data-acs [attribute contraint-key :vals])
                                          (get-in data-acs [attribute contraint-key :text-search?]))))
            filter-desc
            not-user-user-definied)))

(defn events-for-apply-filter [frame-id submit-event local-filter log?]
  [[::check-active frame-id]
   (when log?
     [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id])
   (submit-event frame-id local-filter)
   [::hide frame-id]])

(defn filter-desc [db frame-id]
  (get-in db (path/frame-filter frame-id)))

(defn update-last-applied-filters [db frame-id]
  (assoc-in db
            (path/frame-last-applied-filters frame-id)
            (util/ui-app-state->filter-desc
             (filter-desc db frame-id)
             (get-data-acs db frame-id))))

(defn- init-filterview
  ([db frame-id show?]
   (update-in db
              (path/frame-filter frame-id)
              (fn [old]
                (let [data-acs (get-data-acs db frame-id)]
                  (merge old
                         (cond-> {:selected-ui (default-ui-selection data-acs)
                                  :selected-ui-attributes []}
                           (boolean show?)
                           (assoc :show? show?)))))))
  ([db frame-id]
   (init-filterview db frame-id nil)))

(defn reload-last-applied-filters [db frame-id]
  (if-let [last-applied-filters (get-in db (path/frame-last-applied-filters frame-id))]
    (-> db
        (init-filterview frame-id)
        (update-in
         (path/frame-filter frame-id)
         (fn [{:keys [data-acs] :as old}]
           (let [{:keys [selected-ui selected-ui-attributes]} (util/filter-desc->ui-desc last-applied-filters)]
             (-> old
                 (assoc :selected-ui-attributes selected-ui-attributes)
                 (update :selected-ui merge (util/ensure-dates selected-ui data-acs)))))))

    (init-filterview db frame-id)))

(defn hide-filtering-view [db frame-id]
  (assoc-in db
            (conj (path/frame-filter frame-id) :show?)
            false))

(defn prepare-and-update-filter [db frame-id]
  (let [data-acs (get-data-acs db frame-id)]
    (update-in db
               (path/frame-filter frame-id)
               (fn [filter-desc]
                 (update-filter filter-desc data-acs)))))

(defn remove-constraint
  ([db frame-id]
   (remove-constraint db
                      frame-id
                      []))
  ([db frame-id attr constraint-key]
   (remove-constraint db
                      frame-id
                      (filterv #(not= % [attr constraint-key])
                               (get-in db (conj (path/frame-filter frame-id) :selected-ui-attributes)))))
  ([db frame-id selected-ui-attributes]
   (-> db
       (assoc-in (conj (path/frame-filter frame-id) :selected-ui-attributes) selected-ui-attributes)
       (prepare-and-update-filter frame-id))))

(re-frame/reg-event-db
 ::check-active
 (fn [db [_ frame-id]]
   (assoc-in db (conj (path/frame-filter frame-id)
                      :active?)
             (boolean (not-empty (get-in db
                                         (conj (path/frame-filter frame-id)
                                               :selected-ui-attributes)
                                         []))))))

(re-frame/reg-event-fx
 ::apply-filters-and-close
 (fn [{db :db} [_ frame-id]]
   (let [submit-event (get-in db (path/vertical-plugin-api frame-id :filter :submit-event))
         old-filter (get-in db (path/frame-last-applied-filters frame-id))
         db (update-last-applied-filters db frame-id)
         new-filter (get-in db (path/frame-last-applied-filters frame-id))]
     (cond-> {:db db}
       (not= old-filter new-filter)
       (assoc :dispatch-n (events-for-apply-filter frame-id submit-event new-filter true))
       (= old-filter new-filter)
       (assoc :dispatch-n [[::check-active frame-id]
                           [::hide frame-id]])))))

(re-frame/reg-event-fx
 ::clear-filters-and-close
 (fn [{db :db} [_ frame-id]]
   (let [submit-event (get-in db (path/vertical-plugin-api frame-id :filter :submit-event))
         old-filter (get-in db (path/frame-last-applied-filters frame-id))
         db (-> db
                (remove-constraint frame-id)
                (update-last-applied-filters frame-id))
         new-filter (get-in db (path/frame-last-applied-filters frame-id))]

     (cond-> {:db db}
       (not= old-filter new-filter)
       (assoc :dispatch-n (events-for-apply-filter frame-id submit-event new-filter true))
       (= old-filter new-filter)
       (assoc :dispatch-n [[::check-active frame-id]
                           [::hide frame-id]])))))

(re-frame/reg-event-db
 ::remove-filter
 (fn [db [_ frame-id]]
   (assoc-in db (path/frame-filter frame-id) {})))

(re-frame/reg-event-db
 ::set-filter
 (fn [db [_ frame-id filter]]
   (assoc-in db (path/frame-filter frame-id)
             (util/filter-desc->ui-desc filter))))

(re-frame/reg-event-db
 ::update-filter
 (fn [db [_ frame-id]]
   (prepare-and-update-filter db frame-id)))

(re-frame/reg-event-db
 ::init-filter
 (fn [db [_ frame-id]]
   (init-filterview db frame-id false)))

(re-frame/reg-event-fx
 ::show
 (fn [{db :db} [_ frame-id]]
   (let [desc-path (path/frame-filter frame-id)
         last-applied-filters (get-in db (path/frame-last-applied-filters frame-id))]
     (if (and last-applied-filters
              (not-empty last-applied-filters))
       {:db (-> (update-in db desc-path assoc :show? true)
                (reload-last-applied-filters frame-id)
                (prepare-and-update-filter frame-id))}
       {:db (init-filterview db frame-id true)}))))

(re-frame/reg-event-db
 ::hide
 (fn [db [_ frame-id]]
   (hide-filtering-view db frame-id)))

(re-frame/reg-event-db
 ::restore-filter-desc
 (fn [db [_ frame-id]]
   (reload-last-applied-filters db frame-id)))

(re-frame/reg-sub
 ::last-applied-filters
 (fn [db [_ frame-id]]
   (get-in db (path/frame-last-applied-filters frame-id))))

(defn set-external-filter [db frame-id local-filter]
  (-> (assoc-in db
                (path/frame-last-applied-filters frame-id)
                local-filter)
      (assoc-in (conj (path/frame-filter frame-id) :active?)
                (boolean local-filter))))
