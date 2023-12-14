(ns de.explorama.frontend.algorithms.components.main
  (:require [de.explorama.frontend.ui-base.components.frames.core :as frames]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.algorithms.components.prediction :as prediction]
            [de.explorama.frontend.algorithms.components.legend :as legend]
            [de.explorama.frontend.algorithms.components.reduced-result :as reduced-result]
            [de.explorama.frontend.algorithms.components.empty :as empty]
            [de.explorama.frontend.algorithms.operations.redo :as redo]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.algorithms.path.core :as paths]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.algorithms.config :as config]
            [de.explorama.frontend.algorithms.components.helper :as helper]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [taoensso.timbre :refer [debug info]]
            [cuerdas.core :as cuerdas]
            [clojure.set :as set]
            [clojure.string :as str]
            [de.explorama.shared.algorithms.ws-api :as ws-api]))

(re-frame/reg-sub
 ::procedures
 (fn [db]
   (get-in db paths/procedures)))

(re-frame/reg-sub
 ::prediction
 (fn [db [_ frame-id]]
   (get-in db (paths/load-prediction frame-id))))

(re-frame/reg-sub
 ::problem-types
 (fn [db]
   (get-in db paths/problem-types)))

(re-frame/reg-sub
 ::data-options
 (fn [db [_ frame-id]]
   (get-in db (paths/data-options frame-id))))

(re-frame/reg-sub
 ::training-data
 (fn [db [_ frame-id]]
   (get-in db (paths/training-data frame-id))))

(re-frame/reg-sub
 ::loading?
 (fn [db [_ frame-id]]
   (get-in db (paths/loading frame-id))))

(re-frame/reg-sub
 ::result
 (fn [db [_ frame-id]]
   (get-in db (paths/result frame-id))))

(re-frame/reg-sub
 ::export-states
 (fn [db [_ frame-id]]
   (get-in db (paths/export-states frame-id))))

(re-frame/reg-event-db
 ws-api/load-predictions-result
 (fn [db [_ predictions]]
   (assoc-in db paths/predictions predictions)))

(re-frame/reg-event-db
 ::problem-types
 (fn [db [_ problem-types]]
   (assoc-in db paths/problem-types problem-types)))

(re-frame/reg-event-db
 ::procedures
 (fn [db [_ procedures]]
   (assoc-in db paths/procedures procedures)))

(re-frame/reg-sub
 ::read-only?
 (fn [db [_ frame-id]]
   (let [intermode (fi/call-api [:interaction-mode :current-db-get?] db {:frame-id frame-id})]
     (= intermode :read-only))))

(def attr-name-blocklist #{"country" "id" "location" "annotation" "notes" "date" "datasource"})

(defn insert-newsearch-button [text]
  (try
    (if (str/includes? text "<!btn")
      (let [[beginning-text rest-text] (str/split text #"\<")
            [btn-text rest-text] (str/split rest-text #"\>")
            btn-text (str/capitalize (str/replace btn-text #"\!btn " ""))]
        [:<>
         beginning-text
         [button {:label btn-text
                  :start-icon :search
                  :extra-style {:cursor :default}
                  :variant :secondary}]
         rest-text])
      text)
    (catch :default _
      text)))

(defn stop-screen [{:keys [title message-1 message-2 stop ok-handler]}]
  (let [title-str @(re-frame/subscribe [::i18n/translate title])
        message-1-str @(re-frame/subscribe [::i18n/translate message-1])
        message-2-str @(re-frame/subscribe [::i18n/translate message-2])
        stop-str @(re-frame/subscribe [::i18n/translate stop])]
    [dialog
     {:show? true
      :title title-str
      :hide-fn #(do)
      :message  [:<>
                 [:p message-1-str]
                 [:p (insert-newsearch-button message-2-str)]]
      :ok {:on-click ok-handler
           :label stop-str}}]))

(defn client-options [options]
  (into {}
        (mapv (fn [[key [value]]]
                [key value])
              options)))

(re-frame/reg-event-fx
 ws-api/data-options-result
 (fn [{db :db} [_ frame-id replay? callback-vec task-id data-options {:keys [invalid-operations valid-operations-state]} di-desc]]
   (debug ::data-options frame-id replay? callback-vec data-options invalid-operations valid-operations-state)
   (when (= (get-in db (paths/connect-task-id frame-id))
            task-id)
     (if (:error data-options)
       (cond-> {:db (-> db
                        (assoc-in (paths/data-options frame-id) nil)
                        (assoc-in (paths/loading frame-id) false)
                        (assoc-in (paths/data-changed frame-id) true)
                        (assoc-in (paths/reset-view frame-id) true)
                        (assoc-in (paths/frame-dialog frame-id) {:show? :stop-view-too-much-data
                                                                 :type :stop-screen
                                                                 :details data-options}))}
         callback-vec
         (assoc :dispatch callback-vec))
       (let [old-data-options-set (set (remove (fn [[attr-name]]
                                                 (attr-name-blocklist attr-name))
                                               (get-in db (paths/data-options frame-id) data-options)))
             data-options-set (set (remove (fn [[attr-name]]
                                             (attr-name-blocklist attr-name))
                                           data-options))]
         (cond-> {:db (-> db
                          (assoc-in (paths/data-options frame-id) (client-options data-options))
                          (assoc-in (paths/data-options-server frame-id) data-options)
                          (assoc-in (paths/loading frame-id) false)
                          (assoc-in (paths/data-changed frame-id) true)
                          (assoc-in (paths/reset-view frame-id) (not (set/subset? data-options-set old-data-options-set)))
                          (assoc-in (paths/di-desc frame-id) di-desc))}
           callback-vec
           (assoc :dispatch-n [callback-vec])
           (redo/show-notification? invalid-operations)
           (update :dispatch-n conj [:de.explorama.frontend.algorithms.components.frame-notifications/not-supported-redo-ops frame-id invalid-operations valid-operations-state])))))))


(re-frame/reg-event-db
 ws-api/training-data-result
 (fn [db [_ frame-id training-id training-data]]
   (let [training-id-old (get-in db (paths/training-data-id frame-id))]
     (debug ::training-data frame-id training-id training-id-old training-data)
     (if (= training-id training-id-old)
       (-> (assoc-in db (paths/training-data frame-id) training-data)
           (assoc-in (paths/training-data-id frame-id) nil))
       db))))

(re-frame/reg-sub
 ::training-data-loading?
 (fn [db [_ frame-id]]
   (get-in db (paths/training-data-id frame-id))))

(defn merge-obj-or-color-acs [new-color-ac color-ac-old]
  (let [color-ac-old (into {} (map (fn [{:keys [name] :as ctn}] [name ctn]) color-ac-old))
        color-ac (into {} (map (fn [{:keys [name] :as ctn}] [name ctn]) new-color-ac))]
    (-> (merge-with (fn [a {:keys [info]}]
                      (update a :info (fn [a-info] (into '() (set/union (set a-info) (set info))))))
                    color-ac-old
                    color-ac)
        vals
        vec)))

(re-frame/reg-event-fx
 ws-api/predict-result
 (fn [{db :db} [_ frame-id replay? callback-vec {:keys [di error? prediction-task] :as result}]]
   (let [old-diid (get-in db (paths/data-instance-publishing frame-id))]
     {:db (-> (assoc-in db (paths/result frame-id) (assoc result :prediction-task prediction-task))
              (update-in paths/data-instances (fnil conj #{}) di)
              (assoc-in (paths/data-instance-publishing frame-id) di))
      :dispatch-n (cond-> []
                    (and di
                         (not replay?)
                         (not old-diid))
                    (conj (fi/call-api :frame-header-color-event-vec frame-id)
                          (fi/call-api :frame-set-publishing-event-vec frame-id true)
                          [:de.explorama.frontend.algorithms.event-logging/log-event frame-id "submit-task" {:di di}])

                    (and di
                         (not replay?)
                         old-diid)
                    (conj (fi/call-api :frame-update-children frame-id {:di di})
                          [:de.explorama.frontend.algorithms.event-logging/log-event frame-id "submit-task" {:di di}])

                    (and (or error?
                             replay?)
                         callback-vec)
                    (conj callback-vec))})))

(defn initialize-states [db frame-id goal settings parameter simple-parameter future-data]
  (-> db
      (assoc-in (paths/goal-state frame-id) goal)
      (assoc-in (paths/settings-state frame-id) settings)
      (assoc-in (paths/parameter-state frame-id) parameter)
      (assoc-in (paths/simple-parameter-state frame-id) simple-parameter)
      (assoc-in (paths/future-data-state frame-id) future-data)))

(re-frame/reg-event-fx
 ws-api/load-prediction-result
 (fn [{db :db} [_ frame-id replay? callback-vec prediction]]
   (cond-> {:db (let [procedures (get-in db paths/procedures)
                      problem-types (get-in db paths/problem-types)
                      translate-function (partial i18n/translate db)
                      [goal settings parameter simple-parameter future-data]
                      (helper/initialize-from-prediction procedures problem-types prediction translate-function)]
                  (initialize-states db frame-id goal settings parameter simple-parameter future-data))}
     callback-vec
     (assoc :dispatch callback-vec))))

(re-frame/reg-event-fx
 ::training-data-change
 (fn [{db :db} [_ frame-id task]]
   (let [id (str (random-uuid))]
     (debug "training-data-change" frame-id task)
     {:db (assoc-in db (paths/training-data-id frame-id) id)
      :backend-tube [ws-api/training-data
                     {:client-callback [ws-api/training-data-result frame-id id]}
                     (or task {})
                     (get-in db (paths/data-instance-consuming frame-id))]})))

(re-frame/reg-event-fx
 ::submit-task
 (fn [{db :db} [_ frame-id task replay? callback-vec]]
   (let [di (get-in db (paths/data-instance-consuming frame-id))]
     (debug "submit task" frame-id di task)
     {:db (assoc-in db (paths/prediction-task frame-id) task)
      :backend-tube [ws-api/predict
                     {:client-callback [ws-api/predict-result frame-id replay? callback-vec]
                      :custom {:predicting-event [::predicting frame-id]}}
                     {:task task
                      :source-di di}]})))

(re-frame/reg-event-fx
 ::save-prediction
 (fn [{db :db} [_ frame-id prediction-name prediction-task]]
   (debug "save prediction" prediction-name)
   (let [{:keys [username]}  (fi/call-api :user-info-db-get db)]
     {:backend-tube [ws-api/save-prediction
                     {:client-callback [ws-api/load-predictions-result]}
                     username
                     prediction-name
                     (str (random-uuid))
                     prediction-task]})))

(re-frame/reg-event-fx
 ::publish-data
 (fn [{db :db} [_ frame-id prediction-name]]
   (let [{:keys [prediction-id]} (get-in db (paths/result frame-id))]
     (info "publish prediction" prediction-name prediction-id)
     {:backend-tube [:de.explorama.frontend.algorithms.handler/publish
                     prediction-name
                     prediction-id]})))

(re-frame/reg-event-fx
 ::load-prediction
 (fn [{db :db} [_ frame-id pred-id replay? callback-vec]]
   (let [{:keys [username]}  (fi/call-api :user-info-db-get db)]
     {:db (-> db
              (update-in paths/predictions (fn [preds]
                                             (for [pred preds] (if (= pred-id (:prediction-id pred))
                                                                 (assoc pred :last-used (js/Date.now))
                                                                 pred))))
              (assoc-in (conj (paths/load-prediction frame-id) :load-prediction) false))
      :backend-tube [ws-api/load-prediction
                     {:client-callback [ws-api/load-prediction-result frame-id replay? callback-vec]}
                     username
                     {:pred-id pred-id}]})))

(re-frame/reg-event-fx
 ::load-predictions
 (fn [{db :db} _]
   (let [{:keys [username]}  (fi/call-api :user-info-db-get db)]
     {:backend-tube [ws-api/load-predictions
                     {:callback-event ws-api/load-predictions-result}
                     username]})))

(re-frame/reg-event-db
 ::predicting
 (fn [db [_ frame-id is-predicting?]]
   (assoc-in db (paths/is-predicting? frame-id) is-predicting?)))

(re-frame/reg-sub
 ::is-predicting?
 (fn [db [_ frame-id]]
   (get-in db (paths/is-predicting? frame-id) false)))

(re-frame/reg-event-db
 ::initialized-prediction
 (fn [db [_ frame-id]]
   (assoc-in db (conj (paths/load-prediction frame-id) :load-prediction) false)))

(re-frame/reg-sub
 ::goal-sate
 (fn [db [_ frame-id]]
   (get-in db (paths/goal-state frame-id))))

(re-frame/reg-sub
 ::settings-state
 (fn [db [_ frame-id]]
   (get-in db (paths/settings-state frame-id))))

(re-frame/reg-sub
 ::parameter-state
 (fn [db [_ frame-id]]
   (get-in db (paths/parameter-state frame-id))))

(re-frame/reg-sub
 ::simple-parameter-state
 (fn [db [_ frame-id]]
   (get-in db (paths/simple-parameter-state frame-id))))

(re-frame/reg-sub
 ::future-data-state
 (fn [db [_ frame-id]]
   (get-in db (paths/future-data-state frame-id))))

(re-frame/reg-event-db
 ::initialized-from-project
 (fn [db [_ frame-id]]
   (update-in db (paths/frame frame-id) dissoc paths/project-temp-key)))

(re-frame/reg-sub
 ::data-changed
 (fn [db [_ frame-id]]
   (get-in db (paths/data-changed frame-id))))

(re-frame/reg-event-db
 ::data-changed
 (fn [db [_ frame-id value]]
   (assoc-in db (paths/data-changed frame-id) value)))

(re-frame/reg-sub
 ::reset-view
 (fn [db [_ frame-id]]
   (get-in db (paths/reset-view frame-id))))

(re-frame/reg-event-db
 ::reset-view
 (fn [db [_ frame-id value]]
   (assoc-in db (paths/reset-view frame-id) value)))

(re-frame/reg-sub
 ::frame-dialog
 (fn [db [_ frame-id]]
   (get-in db (paths/frame-dialog frame-id))))

(re-frame/reg-sub
 ::show-stop-dialog
 (fn [db [_ frame-id]]
   (let [{:keys [type show?]} (get-in db (paths/frame-dialog frame-id))]
     (when (and show?
                (= :stop-screen type))
       show?))))

(re-frame/reg-event-db
 ::frame-dialog
 (fn [db [_ frame-id dialog-desc]]
   (assoc-in db (paths/frame-dialog frame-id) dialog-desc)))

(re-frame/reg-event-fx
 ::hide-result
 (fn [{db :db} [_ frame-id prediction-id username prediction-name error?]]
   (let [translate-function (partial i18n/translate db)]
     {:db
      (assoc-in db
                (paths/frame-dialog frame-id)
                (if-not error?
                  {:title (translate-function :hide-prediction-success-title)
                   :show? true
                   :message (str (translate-function :hide-prediction-success-message-1)
                                 " "
                                 prediction-name
                                 " "
                                 (translate-function :hide-prediction-success-message-2))
                   :hide-fn #(re-frame/dispatch [::close-dialog frame-id])}
                  {:title (translate-function :hide-prediction-error-title)
                   :show? true
                   :message (str (translate-function :hide-prediction-error-message-1)
                                 " "
                                 prediction-name
                                 " "
                                 (translate-function :hide-prediction-error-message-2))
                   :hide-fn #(re-frame/dispatch [::close-dialog frame-id])}))
      :backend-tube [ws-api/load-predictions
                     {:client-callback ws-api/load-predictions}
                     username]})))

(re-frame/reg-event-fx
 ::hide-prediction
 (fn [{db :db} [_ frame-id prediction-id]]
   (let [{:keys [username]}  (fi/call-api :user-info-db-get db)]
     {:backend-tube [:de.explorama.frontend.algorithms.handler/hide-prediction username prediction-id [::hide-result frame-id prediction-id username]]})))

(re-frame/reg-event-db
 ::close-dialog
 (fn [db [_ frame-id]]
   (assoc-in db (paths/frame-dialog frame-id) nil)))

(re-frame/reg-event-db
 ::save-last-prediction-task
 (fn [db [_ frame-id task]]
   (assoc-in db (paths/last-prediction-task frame-id) task)))

(defn comp-functions [procedures options problem-types prediction frame-id translate-function]
  {:submit
   (fn [goal-state settings-state parameter-state simple-parameter-state future-data-state]
     (let [task (helper/transform-inputs @procedures @options @problem-types @goal-state @settings-state @parameter-state @simple-parameter-state @future-data-state)]
       (re-frame/dispatch [::submit-task frame-id task])
       (re-frame/dispatch [::save-last-prediction-task frame-id task])))

   :logging-value-changed
   (fn [state-key path value]
     (re-frame/dispatch [:de.explorama.frontend.algorithms.event-logging/ui-wrapper frame-id "ui-value-changed" {:state-key state-key
                                                                                                                 :path path
                                                                                                                 :map? (map? value)
                                                                                                                 :value (if (map? value)
                                                                                                                          (:value value)
                                                                                                                          value)}]))
   :training-data-change
   (fn [goal-state settings-state parameter-state simple-parameter-state future-data-state]
     (re-frame/dispatch
      [::training-data-change
       frame-id
       (helper/transform-inputs @procedures @options @problem-types @goal-state @settings-state @parameter-state @simple-parameter-state @future-data-state)]))
   :load-prediction
   (fn [pred-id]
     (re-frame/dispatch [::load-prediction frame-id pred-id])
     (re-frame/dispatch [:de.explorama.frontend.algorithms.event-logging/ui-wrapper frame-id "load-prediction" {:pred-id pred-id}]))
   :hide-prediction
   (fn [pred-id]
     (re-frame/dispatch [::frame-dialog
                         frame-id
                         {:title (translate-function :hide-prediction-title)
                          :show? true
                          :header-type :warning
                          :message (translate-function :hide-prediction-message)
                          :yes {:on-click #(re-frame/dispatch [::hide-prediction frame-id pred-id])
                                :label (translate-function :delete-label)
                                :start-icon :trash
                                :type :warning}
                          :no {:variant :secondary
                               :label (translate-function :cancel-label)}
                          :hide-fn #(re-frame/dispatch [::close-dialog frame-id])}]))
   :data-changed-sub
   (re-frame/subscribe [::data-changed frame-id])
   :data-changed
   (fn [value] (re-frame/dispatch [::data-changed frame-id value]))
   :reset-view-sub
   (re-frame/subscribe [::reset-view frame-id])
   :reset-view
   (fn [value] (re-frame/dispatch [::reset-view frame-id value]))})

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (get-in db (paths/di-desc frame-id))))

(re-frame/reg-sub
 ::get-event-count
 (fn [db [_ frame-id]]
   (get-in db (conj (paths/di-desc frame-id) :event-count))))

(re-frame/reg-event-db
 ::set-custom-title
 [(fi/ui-interceptor)]
 (fn [db [_ frame-id title]]
   (assoc-in db (paths/custom-title frame-id) title)))

(re-frame/reg-sub
 ::custom-title
 (fn [db [_ frame-id]]
   (get-in db (paths/custom-title frame-id))))

(re-frame/reg-sub
 ::title-prefix
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::get-event-count frame-id])
    (re-frame/subscribe [::i18n/translate :algorithms-model-window])])
 (fn [[event-count
       vertical-label] [_ _ vertical-count-number]]
   (cond-> (str vertical-label " " vertical-count-number)
     (zero? event-count)
     (str " - 0 Events")
     (pos? event-count)
     (str " - "
          (i18n/localized-number event-count)
          " Events"))))

(re-frame/reg-sub
 ::title
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::di-desc frame-id])])
 (fn [[di-desc]]
   (let [{[years] :years, [countries] :countries, [datasources] :datasources} di-desc]
     (when (and (and datasources years countries)
                (not-empty datasources)
                (not-empty years)
                (not-empty countries))
       (str/join " " [" -" datasources years countries])))))

(def frame-header-impl
  {:frame-icon :head-cogs
   :frame-title-sub
   (fn [frame-id]
     (re-frame/subscribe [::title frame-id]))

   :frame-title-prefix-sub
   (fn [frame-id vertical-count-number]
     (re-frame/subscribe [::title-prefix frame-id vertical-count-number]))

   :can-change-title? true
   :on-minimize-event (fn [frame-id])
   :on-maximize-event (fn [frame-id])
   :on-normalize-event (fn [frame-id])
   :on-close-fn (fn [_ done-fn]
                  (done-fn))})

(defn loading-impl [frame-id]
  (re-frame/subscribe [::loading? frame-id]))

(defn initalize [{:keys [goal-state settings-state parameter-state simple-parameter-state future-data-state]}
                 {:keys [goal-sub settings-sub parameter-sub simple-parameter-sub future-data-sub initialized-from-project]}
                 {:keys [reset-view reset-view-sub]}]
  (let [goal-sub @goal-sub
        settings-sub @settings-sub
        parameter-sub @parameter-sub
        simple-parameter-sub @simple-parameter-sub
        future-data-sub @future-data-sub
        reset-view-sub @reset-view-sub]
    (cond
      reset-view-sub
      (do
        (reset-view false)
        (reset! settings-state {}))
      (or goal-sub settings-sub parameter-sub future-data-sub simple-parameter-sub)
      (do
        (reset! goal-state (or goal-sub {}))
        (reset! settings-state (or settings-sub {}))
        (reset! parameter-state (assoc (or parameter-sub {})
                                       :is-valid? {:def true}))
        (reset! simple-parameter-state (assoc (or simple-parameter-sub {})
                                              :is-valid? {:def true}))
        (reset! future-data-state (or future-data-sub {}))

        (initialized-from-project)))))

(defn export-states [states export-states]
  (when export-states
    (re-frame/dispatch (conj export-states
                             (into {}
                                   (map (fn [[state-key state-value]]
                                          [state-key @state-value]))
                                   states)))))

(defn container [frame-id infos-sub translate-function language-function]
  (let [states {:goal-state (reagent/atom {})
                :settings-state (reagent/atom {})
                :parameter-state (reagent/atom {})
                :simple-parameter-state (reagent/atom {})
                :future-data-state (reagent/atom {})
                :params-valid-state (reagent/atom {})}]
    (fn [frame-id infos-sub]
      (let [{is-minimized?   :is-minimized?} @infos-sub
            read-only? (re-frame/subscribe [::read-only? frame-id])
            procedures (re-frame/subscribe [::procedures])
            problem-types (re-frame/subscribe [::problem-types])
            options (re-frame/subscribe [::data-options frame-id])
            data (re-frame/subscribe [::training-data frame-id])
            loading (re-frame/subscribe [::loading? frame-id])
            result (re-frame/subscribe [::result frame-id])
            prediction (re-frame/subscribe [::prediction frame-id])
            is-predicting? (re-frame/subscribe [::is-predicting? frame-id])
            training-data-loading? (re-frame/subscribe [::training-data-loading? frame-id])
            init-subs {:goal-sub (re-frame/subscribe [::goal-sate frame-id])
                       :settings-sub (re-frame/subscribe [::settings-state frame-id])
                       :parameter-sub (re-frame/subscribe [::parameter-state frame-id])
                       :simple-parameter-sub (re-frame/subscribe [::simple-parameter-state frame-id])
                       :future-data-sub (re-frame/subscribe [::future-data-state frame-id])
                       :initialized-from-project (fn []
                                                   (re-frame/dispatch [::initialized-from-project frame-id]))}
            {dialog-title :title dialog-show :show?
             dialog-type :type
             dialog-header-type :header-type
             dialog-message :message
             dialog-yes :yes dialog-no :no
             dialog-hide-fn :hide-fn
             :as dialog}
            @(re-frame/subscribe [::frame-dialog frame-id])
            export-states? @(re-frame/subscribe [::export-states frame-id])]
        (initalize states init-subs (comp-functions procedures options problem-types prediction frame-id translate-function)) ;HACK
        (export-states states export-states?)
        (when-not is-minimized?
          [:<>
           [:div.window__body.flex
            {:id (config/frame-body-dom-id frame-id)
             :style {:overflow-y :auto
                     :display (when is-minimized?
                                "none")}}
            (when (and dialog (not= dialog-type :stop-screen))
              [frames/dialog
               (cond-> {:show? dialog-show
                        :hide-fn #(dialog-hide-fn false)
                        :title dialog-title
                        :message dialog-message}
                 dialog-header-type
                 (assoc :type dialog-header-type)
                 dialog-yes
                 (assoc :yes dialog-yes)
                 dialog-no
                 (assoc :no dialog-no))])
            (cond
              (and (not @options)
                   (not (:load-prediction @prediction)))
              [empty/empty-component frame-id]
              (or @options
                  (boolean? (:load-prediction @prediction)))
              [prediction/react-view
               frame-id
               procedures
               problem-types
               options
               data
               result
               {:publish (fn [prediction-name]
                           (re-frame/dispatch [::publish-data frame-id prediction-name]))}
               prediction
               is-predicting?
               training-data-loading?
               translate-function
               language-function
               (comp-functions procedures options problem-types prediction frame-id translate-function)
               states
               read-only?])]])))))

(defn react-view [frame-id {:keys [size] :as vis-desc}]
  (let [infos-sub (if vis-desc
                    (reagent/atom {:is-minimized? false
                                   :size (or size
                                             [140 140])})
                    (fi/call-api :frame-sub frame-id))]
    (reagent/create-class
     {:display-name (str "algorithms body" frame-id)
      :component-did-mount
      (fn []
        (re-frame/dispatch [::load-predictions frame-id])
        (when vis-desc (re-frame/dispatch [:de.explorama.frontend.algorithms.vis-state/restore-vis-desc frame-id vis-desc]))
        (re-frame/dispatch (fi/call-api :render-done-event-vec frame-id config/default-vertical)))
      :component-did-update (fn [this argv]
                              (let [[_ _ {old-size :size}] argv
                                    [_ _ {new-size :size}] (reagent/argv this)]
                                (when (and vis-desc
                                           (not= old-size new-size))
                                  (swap! infos-sub assoc :size new-size))))
      :reagent-render
      (fn [frame-id vis-desc]
        (let [translate-function
              (fn [key]
                @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate key]))
              language-function (fn []
                                  @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language]))]
          (if vis-desc
            [reduced-result/reduced-view
             frame-id
             infos-sub
             translate-function
             language-function]
            [container
             frame-id
             infos-sub
             translate-function
             language-function])))})))

(def ^:private stop-screens
  {:stop-view-display {:title :load-stop-screen-title
                       :message-1 :load-stop-screen-message-part-1
                       :message-2 :load-stop-screen-message-part-2
                       :stop :load-stop-screen-follow-recommendation}

   :stop-view-too-much-data {:title :too-much-data-title
                             :message-1 :too-much-data-message-part-1-min-max
                             :message-2 :too-much-data-message-part-2
                             :stop :too-much-data-follow-recommendation}})

(re-frame/reg-sub
 ::stopscreen-label
 (fn [db [_ frame-id k label]]
   (let [{:keys [details]} (get-in db (paths/frame-dialog frame-id))
         label (i18n/translate db (get-in stop-screens [k label]))]
     (cond-> label
       (= k :stop-view-too-much-data)
       (cuerdas/format {:data-count (i18n/localized-number (:data-count details))
                        :max-data-amount (i18n/localized-number (:max-data-amount details))})))))

(def stop-screen-impl
  {:show? (fn [frame-id]
            (re-frame/subscribe [::show-stop-dialog frame-id]))
   :title-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :title]))
   :message-1-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-1]))
   :message-2-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-2]))
   :stop-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :stop]))
   :ok-fn (fn [_ frame-id]
            (re-frame/dispatch [::close-dialog frame-id]))})

(def product-tour-impl
  {:component :algorithms})

(def loading-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::loading? frame-id]))
   :cancellable?
   (fn [_]
     (atom false)) ;! FIXME
   :cancel-fn
   (fn [frame-id _] nil)
   :loading-screen-message-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-message]))
   :loading-screen-tip-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip]))
   :loading-screen-tip-titel-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip-titel]))})
