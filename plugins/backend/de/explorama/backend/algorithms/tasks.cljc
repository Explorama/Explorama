(ns de.explorama.backend.algorithms.tasks
  (:require [clojure.set :as set]
            [data-format-lib.core :as dfl]
            [de.explorama.backend.algorithms.config :as config]
            [de.explorama.backend.algorithms.data-tiles :as data-tiles]
            [de.explorama.backend.algorithms.data.algorithm :as algo]
            [de.explorama.backend.algorithms.data.features :as feat]
            [de.explorama.backend.algorithms.data.redo :as redo]
            [de.explorama.backend.algorithms.error :refer [error-cases]]
            [de.explorama.backend.algorithms.explorama-adapter :refer [calculate-di-desc calculate-options data-tile-ref enhance-events make-di volatile-acs]]
            [de.explorama.backend.algorithms.prediction-registry.core :as prediction-registry]
            [de.explorama.backend.algorithms.predictions :as preds]
            [de.explorama.backend.algorithms.registry :as registry]
            [de.explorama.backend.algorithms.resources :as resources]
            [de.explorama.backend.common.environment.probe :as probe]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.data.data-tiles :as explorama-tiles]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.common.unification.time :refer [current-ms]]
            [taoensso.timbre :refer [debug error trace warn]]))

(defn available-algorithms []
  (select-keys resources/algorithms-config (registry/all-algorithms)))

(defn- available-problem-types []
  (let [all-algorithms (registry/all-algorithms)]
    (->> resources/problem-types-config
         (map (fn [[key {:keys [algorithms]
                         :as   desc}]]
                (let [available-algorithms (vec (set/intersection (set algorithms) all-algorithms))]
                  (when (seq available-algorithms)
                    [key (assoc desc :algorithms available-algorithms)]))))
         (filter identity)
         (into {}))))

(defn- retrieve-data [di filter-desc]
  (let [data (data-tiles/data-tiles-lookup di)]
    (if filter-desc (dfl/filter-data filter-desc data) data)))

(defn- not-too-much-data? [data data-count]
  (when data
    #_{:clj-kondo/ignore [:type-mismatch]}
    (<= data-count config/explorama-algorithms-max-data-amount)))

(defn- data-options* [di & [filter-desc]]
  (let [data (retrieve-data di filter-desc)
        data-count (count data)
        _ (debug "data-options: di" {:di di :data-count data-count})
        options (if-not (not-too-much-data? data data-count)
                  (throw (ex-info "Too much data" {:error :too-much-data
                                                   :data-count data-count
                                                   :max-data-amount config/explorama-algorithms-max-data-amount}))
                  (calculate-options data))]
    {:data    data
     :options options}))

(defn calculate-training-data [task data]
  (->> (feat/transform task data)
       (algo/transform task)))

(defn training-data-lookup [[{:keys [task di country] :as key}] _]
  (try
    (trace "training-data-lookup" task di country)
    (let [[_ _
           _ _
           _ _
           _ _
           validation]
          (feat/features (:attributes task) (:parameter task))]
      (feat/exceptions validation))
    {key
     (as-> (data-tiles/data-tiles-lookup di) $
       (if (not= country :ignore)
         (filter #(= (attrs/value % "country") country) $)
         $)
       (calculate-training-data task $))}
    (catch #?(:clj Exception :cljs :default) e
      (probe/rate-exception e)
      (warn e)
      {key {:train-error (ex-data e)}})))

(defn clean-task [task]
  (dissoc task :di :problem-type :algorithm))

(defn cached-training-data [task di country]
  (first (vals (preds/train-cache-lookup {:task (clean-task task)
                                          :di di
                                          :country country}))))

(defn countries- [data group-by-country?]
  (if group-by-country?
    (vec (set (map #(attrs/value % "country") data)))
    [:ignore]))

(defn countries [data task]
  (countries- data
              (get-in task [:parameter :group-by-country] false)))

(defn data-options [{:keys [client-callback]} [di operations-state]]
  (debug "data-options" {:di  di
                         :operations-state operations-state})
  (try
    (let [{:keys [options data]} (data-options* di)
          redo-check-result (when (and operations-state (seq operations-state))
                              (redo/check-redo (set (keys options))
                                               operations-state))]
      (client-callback
       options
       redo-check-result
       (calculate-di-desc data)))
    (catch #?(:clj Exception :cljs :default) e
      (probe/rate-exception e)
      (client-callback (ex-data e)))))

(defn training-data- [task di country]
  (debug "training-data-" task)
  (let [result (cached-training-data task di country)]
    (if (:train-error result)
      [(assoc (select-keys task [:problem-type :algorithm])
              :country country)
       result]
      (let [future-data (feat/future-data (assoc task :training-data result))
            result (assoc result :future-header (keys (first (:data future-data))))
            result (assoc result :header (keys (first (:data result))))
            result (assoc result :future-data (:data future-data))
            result (assoc result :mapping (into {}
                                                (map (fn [[[k kv] idx]]
                                                       [[k idx] kv])
                                                     (:mapping result))))
            result (assoc result :future-mapping (into {}
                                                       (map (fn [[[k kv] idx]]
                                                              [[k idx] kv])
                                                            (:mapping future-data))))]
        [(assoc (select-keys task [:problem-type :algorithm])
                :country country)
         result]))))

(defn training-data* [task di]
  (trace "calculating training-data" key)
  (let [data (vec (retrieve-data di nil))
        data-countries (countries data task)
        training-data
        (into {}
              (map (fn [country]
                     (training-data- task di country)))
              data-countries)]
    training-data))

(defn training-data [{:keys [client-callback]} [task di]]
  (try
    (let [training-data (training-data* task di)]
      (trace "training-data result" training-data)
      (client-callback training-data))
    (catch #?(:clj Exception :cljs :default) e
      (probe/rate-exception e)
      (error "task failed" task di)
      (when (= :unknown-error (:error (error-cases e)))
        (error e (ex-message e)))
      (client-callback (error-cases e)))))

(defn predict- [id options country di task]
  (let [training-data (cached-training-data task di country)
        _ (debug "predict-" id options country di task training-data)
        training (registry/make-model (:algorithm task)
                                      (assoc task
                                             :task-id id
                                             :training-data training-data
                                             :options options
                                             :country country))]
    (registry/execute-model (:algorithm task) training)))

(defn problem-type-wrapper [id options country di task]
  (mapv (fn [subtask]
          (predict- id options country di subtask))
        (if-let [problem-type (:problem-type task)]
          (mapv (fn [algorithm]
                  (debug "dispatch task" algorithm)
                  (assoc task :algorithm algorithm))
                (get-in (available-problem-types) [problem-type :algorithms]))
          [task])))

(defn predict* [task id options di data]
  (let [data-countries (countries data task)
        _ (debug "predict*" task id data-countries)
        predictions
        (mapv (fn [country]
                (problem-type-wrapper id options country di task))
              data-countries)
        predictions (enhance-events (flatten predictions) data-countries)
        volatile-acs (volatile-acs predictions)
        prediction-warnings (try
                              (->> predictions
                                   (mapcat :prediction-warning)
                                   (filter identity)
                                   set
                                   vec)
                              (catch #?(:clj Exception :cljs :default) e
                                (probe/rate-exception e)
                                (error e "Problem determining prediction-warning"
                                       predictions)))]
    [predictions
     volatile-acs
     prediction-warnings]))

(defn- lookup-prediction- [data-tile-ref]
  (let [[_
         [predictions
          volatile-acs
          prediction-warnings]]
        (first (preds/cache-lookup [data-tile-ref]))]
    [predictions
     volatile-acs
     prediction-warnings]))

(defn predict [{:keys [client-callback predicting-event]} [{task :task
                                                            di :source-di
                                                            pred-di :pred-di}]]
  (try
    (debug "predict" task di pred-di)
    (when predicting-event
      (predicting-event true))
    (let [pred-di (or pred-di (make-di di task))
          data-tile-ref (data-tile-ref pred-di)
          task (or task (-> (get pred-di :di/data-tile-ref)
                            vals
                            first
                            :task))
          [predictions
           volatile-acs
           prediction-warnings]
          (lookup-prediction- data-tile-ref)]
      (trace predictions volatile-acs prediction-warnings)
      (when client-callback
        (client-callback {:prediction-task task
                          :prediction-id   (cljc-uuid)
                          :predictions     predictions
                          :di              (assoc pred-di
                                                  :di/acs volatile-acs)
                          :warning         (not-empty prediction-warnings)})))
    (catch #?(:clj Exception :cljs :default) e
      (probe/rate-exception e)
      (error e (ex-message e))
      (client-callback (error-cases e)))
    (finally
      (when predicting-event
        (predicting-event false)))))

(defn load-predictions [{:keys [client-callback]} [_ username]]
  (client-callback (prediction-registry/list-predictions username false)))

(defn load-prediction [{:keys [client-callback]} [_ username {pred-id :pred-id} event]]
  (client-callback (prediction-registry/retrive-prediction username pred-id)))

(defn save-prediction [dispatch-to tube [_
                                         username
                                         prediction-name
                                         prediction-id
                                         prediction-task
                                         event]]
  (prediction-registry/store-prediction username
                                        (assoc prediction-task
                                               :prediction-id prediction-id
                                               :last-used (current-ms)
                                               :prediction-name prediction-name))
  (when event
    (dispatch-to tube (conj event prediction-id)))
  tube)

(defn delete-prediction [{:keys [client-callback]} [_ username prediction-id]]
  (try
    (client-callback
     (:prediction-name
      (prediction-registry/remove-prediction username
                                             prediction-id))
     false)
    (catch #?(:clj Exception :cljs :default) e
      (probe/rate-exception e)
      (error e (ex-message e))
      (client-callback
       prediction-id
       true))))

(defn init [{:keys [client-callback]} _]
  (client-callback {:problem-types (available-problem-types)
                    :procedures    (available-algorithms)}))

(defn miss-fn [tiles _]
  (mapv (fn [tile]
          (debug "prediction miss" tile)
          [tile
           (let [task (:task tile)
                 di (:di task)
                 data (data-tiles/data-tiles-lookup di)
                 options (calculate-options data)
                 result
                 (predict* task
                           (cljc-uuid)
                           options
                           di
                           data)]
             result)])
        tiles))

(defn data-lookup [data-tile-ref]
  (let [data-tile-ref (explorama-tiles/value data-tile-ref "data-tiles")]
    (debug "data-lookup" data-tile-ref)
    (map (fn [[tile-key [predictions]]]
           [tile-key (flatten (map :prediction-data predictions))])
         (preds/cache-lookup data-tile-ref))))
