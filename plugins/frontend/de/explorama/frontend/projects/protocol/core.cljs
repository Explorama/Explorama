(ns de.explorama.frontend.projects.protocol.core
  (:require [cljs-time.coerce :as date-coerce]
            [cljs-time.format :as date-format]
            [clojure.string :as string]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.projects.config :as config]
            [de.explorama.frontend.projects.path :as ppath]
            [de.explorama.frontend.projects.protocol.path :as path]
            [de.explorama.frontend.projects.subs :refer [loaded-project-id
                                                         project-by-id
                                                         project-loaded-infos]]
            [de.explorama.frontend.projects.utils.projects :as p-utils]
            [de.explorama.frontend.projects.views.tooltip :refer [step-action-button]]
            [de.explorama.frontend.ui-base.components.common.core :refer [virtualized-list]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field textarea]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog
                                                                          loading-screen]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.data-exchange :as data-exchange]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [goog.string :as goog-string]
            [re-frame.core :as re-frame]
            [re-frame.interop :as re-interop]
            [re-frame.utils :refer [dissoc-in]]
            [reagent.core :as r]
            [vimsical.re-frame.cofx.inject :as inject]))

(def ^:private init-store-desc
  {:id nil
   :based-events nil
   :protocol-steps nil
   :protocol-text ""})

(def ^:private protocol-store (r/atom init-store-desc))
(def ^:private protocol-context-id (r/cursor protocol-store [:id]))
(def ^:private protocol-based-events (r/cursor protocol-store [:based-events]))
(def ^:private protocol-steps-sub (r/cursor protocol-store [:protocol-steps]))
(def ^:private protocol-text-sub (r/cursor protocol-store [:protocol-text]))

(def ^:private init-dialog-store-desc
  {:id nil
   :based-events nil
   :protocol-steps nil
   :protocol-text ""})

(def ^:private dialog-protocol-store (r/atom init-dialog-store-desc))
(def ^:private dialog-protocol-context-id (r/cursor dialog-protocol-store [:id]))
(def ^:private dialog-protocol-steps-sub (r/cursor dialog-protocol-store [:protocol-steps]))

(defn sub-and-dispose [query-vec]
  (let [sub (re-frame/subscribe query-vec)
        val (deref sub)]
    (re-interop/dispose! sub)
    val))

(defn current-step [_]
  (first (peek (or @protocol-steps-sub []))))

(re-frame/reg-event-db
 ::activate-edit
 (fn [db [_ snap-desc]]
   (assoc-in db path/snapshot-editor (assoc snap-desc
                                            :active? true))))

(re-frame/reg-sub
 ::snapshot-active-edit
 (fn [db _]
   (get-in db path/snapshot-editor)))

(re-frame/reg-sub
 ::snapshot-edit-active?
 (fn [db _]
   (boolean (get-in db path/snapshot-edit-active? false))))

(re-frame/reg-event-db
 ::change-snapshot-title
 (fn [db [_ new-title]]
   (assoc-in db path/snapshot-edit-title new-title)))

(re-frame/reg-event-db
 ::change-snapshot-description
 (fn [db [_ new-description]]
   (assoc-in db path/snapshot-edit-description new-description)))

(re-frame/reg-sub
 ::save-edit-possible?
 (fn [db _]
   (let [{:keys [title]} (get-in db path/snapshot-editor)]
     (not (string/blank? title)))))

(re-frame/reg-event-fx
 ::save-snapshot-change
 (fn [{db :db} [_ project-id]]
   (let [{:keys [snapshot-id title description]} (get-in db path/snapshot-editor)]
     {:dispatch [::clean-edit]
      :backend-tube [ws-api/update-snapshot-desc-route
                     {}
                     project-id snapshot-id title description]})))

(re-frame/reg-event-db
 ::clean-edit
 (fn [db _]
   (dissoc-in db path/snapshot-editor)))

;;; UI part

(def date-formatter (date-format/formatters :date-hour-minute-second-fraction))

(defn timestamp->string [timestamp]
  (when timestamp
    (date-format/unparse date-formatter
                         (date-coerce/from-long timestamp))))

(defn protocol-action [icon-class click-event]
  [button {:variant :secondary
           :start-icon icon-class
           :on-click #(when click-event
                        (re-frame/dispatch click-event))}])

(re-frame/reg-event-fx
 ::start-loading-project-at-step
 (fn [{db :db} [_ project-to-load head]]
   {:db (assoc-in db path/step-read-only true)
    :fx [[:dispatch [:de.explorama.frontend.projects.protocol.core/open-at-step-dialog]]
         [:dispatch (fi/call-api :welcome-dismiss-page-event-vec
                                 [[:de.explorama.frontend.projects.views.overview/close-overview]
                                  [:de.explorama.frontend.projects.views.project-card/set-loading-project {:project project-to-load :head head}]
                                  [:de.explorama.frontend.projects.views.warning-dialog/handle-load-project-with-warning project-to-load head]])]]}))

(re-frame/reg-sub
 ::create-snapshot?
 (fn [db _]
   (boolean (get-in db [:projects :create-snapshot?]))))

(re-frame/reg-event-db
 ::create-snapshot
 (fn [db [_ project-id head]]
   (assoc-in db [:projects :create-snapshot?] [project-id head])))

(re-frame/reg-event-fx
 ::save-created-snapshot
 (fn [{db :db} [_ snapshot-id]]
   (let [user-info (fi/call-api :user-info-db-get db)
         [project-id head] (get-in db [:projects :create-snapshot?])
         title (get-in db path/snapshot-edit-title)
         description (get-in db path/snapshot-edit-description)]
     {:backend-tube [ws-api/create-snapshot-route
                       {} 
                       {:snapshot-id snapshot-id
                        :project-id project-id
                        :head head
                        :title title
                        :description description}
                       user-info]})))

(re-frame/reg-event-db
 ::clean-snapshot-creation
 (fn [db _]
   (-> db
       (update :projects dissoc :create-snapshot?)
       (dissoc-in path/snapshot-editor))))

(defn step-actions [snapshot? step-count {:keys [head]} project-id writable-project? project-to-load]
  (let [dialog-title @(re-frame/subscribe [::i18n/translate :loading-step-dialog-title])
        dialog-read-only-question @(re-frame/subscribe [::i18n/translate :loading-step-dialog-read-question])
        dialog-writeable-question @(re-frame/subscribe [::i18n/translate :loading-step-dialog-write-question])
        dialog-confirm-label @(re-frame/subscribe [::i18n/translate :loading-step-dialog-confirm-button])
        dialog-discard-label @(re-frame/subscribe [::i18n/translate :loading-step-dialog-discard-button])
        continue-anyway @(re-frame/subscribe [::i18n/translate :continue-anyway])
        dialog-title (format dialog-title step-count)]
    [:div.protocol__actions
     [step-action-button {:icon-class :eye
                          :size :small
                          :event [:de.explorama.frontend.projects.views.confirm-dialog/show-dialog true
                                  {:title dialog-title
                                   :infos dialog-read-only-question
                                   :confirm-label dialog-confirm-label
                                   :discard-label dialog-discard-label
                                   :confirm-event (if project-to-load
                                                    [::start-loading-project-at-step project-to-load {:plogs-id {:project-id (:project-id project-to-load)}
                                                                                                      :head head
                                                                                                      :read-only? true}]
                                                    [::start-loading-step head true])}]
                          :tooltip-text (re-frame/subscribe [::i18n/translate :step-open-read-only-tooltip])
                          :style {:display "inline"
                                  :margin-right "5px"}
                          :deactivated (empty? project-id)}]
     [step-action-button {:icon-class :edit
                          :size :small
                          :event [:de.explorama.frontend.projects.views.confirm-dialog/show-dialog true
                                  {:title dialog-title
                                   :infos dialog-writeable-question
                                   :dialog-type :warning
                                   :confirm-label continue-anyway
                                   :discard-label dialog-discard-label
                                   :confirm-event (if project-to-load
                                                    [::start-loading-project-at-step project-to-load {:plogs-id {:project-id (:project-id project-to-load)}
                                                                                                      :head head
                                                                                                      :read-only? false}]
                                                    [::start-loading-step head false])}]
                          :tooltip-text (re-frame/subscribe [::i18n/translate :step-open-writeable-tooltip])
                          :style {:display "inline"
                                  :margin-right "5px"}
                          :deactivated (not writable-project?)}]
     (when-not project-to-load
       [step-action-button {:icon-class (if snapshot?
                                          :star
                                          :star-o)
                            :size :small
                            :event (if snapshot?
                                     [::delete-snapshot head]
                                     [::create-snapshot project-id head])
                            :tooltip-text (if snapshot?
                                            (re-frame/subscribe [::i18n/translate :step-not-snapshot-tooltip])
                                            (re-frame/subscribe [::i18n/translate :step-as-snapshot-tooltip]))
                            :style {:display "inline"}
                            :deactivated (or (empty? project-id)
                                             (not writable-project?))}])]))

(defn snapshot-editor [{snapshot-title :title}]
  (let [new-val (r/atom snapshot-title)]
    (fn [{snapshot-title :title
          snapshot-description :description}]
      (let [title-label @(re-frame/subscribe [::i18n/translate :snapshot-title-label])
            description-label @(re-frame/subscribe [::i18n/translate :snapshot-title-description])]
        [:div.snapshot__editor
         [input-field {:label title-label
                       :default-value snapshot-title
                       :value @new-val
                       :on-clear (fn []
                                   (reset! new-val "")
                                   (re-frame/dispatch [::change-snapshot-title ""]))
                       :on-change (fn [val]
                                    (reset! new-val val)
                                    (re-frame/dispatch [::change-snapshot-title val]))}]
         [textarea {:label description-label
                    :default-value snapshot-description
                    :on-change #(re-frame/dispatch [::change-snapshot-description %])}]]))))

(defn snapshot-info [{snapshot-title :title
                      snapshot-description :description
                      :as snap-desc}]
  (let [title-label @(re-frame/subscribe [::i18n/translate :snapshot-title-label])
        description-label @(re-frame/subscribe [::i18n/translate :snapshot-title-description])]
    [:<>
     [:dt title-label]
     [:dd {:on-double-click #(re-frame/dispatch [::activate-edit snap-desc])}
      snapshot-title]
     [:dt description-label]
     [:dd {:on-double-click #(re-frame/dispatch [::activate-edit snap-desc])}
      snapshot-description]]))

(defn- translate-settings [translate-fn settings]
  (cond (and (vector? settings)
             (= :label (first settings)))
        (i18n/attribute-label (second settings))
        (and (vector? settings)
             (= :prefix (first settings)))
        (translate-fn (keyword (str (first settings) (second settings))))
        (vector? settings)
        (string/join " " (map (partial translate-settings translate-fn) settings))
        (string? settings)
        settings
        (keyword? settings)
        (translate-fn settings)))

(defn step-info [project-id
                 {:keys [window-id window-title action settings settings-sub]}
                 {snapshot-id :snapshot-id
                  :as snap-desc}
                 project-to-load]
  (let [window-title (or window-title
                         @(re-frame/subscribe (fi/call-api :frame-title-all-sub-vec window-id))
                         (fi/call-api :full-frame-title-raw window-id))
        window-string @(re-frame/subscribe [::i18n/translate :step-info-window])
        action-title @(re-frame/subscribe [::i18n/translate :step-info-action])
        settings-title @(re-frame/subscribe [::i18n/translate :step-info-settings])
        action-string (if (keyword? action)
                        @(re-frame/subscribe [::i18n/translate action])
                        action)
        settings-string (if (not-empty settings-sub)
                          @(re-frame/subscribe settings-sub)
                          (translate-settings (fn [k] @(re-frame/subscribe [::i18n/translate k]))
                                              settings))]
    [:dl.protocol__info
     [:<>
      (when snapshot-id
        [snapshot-info snap-desc])
      [:dt window-string]
      [:dd (if (string? window-title)
             (goog-string/truncate window-title config/max-protocol-entry-length)
             window-title)]
      [:dt action-title]
      [:dd action-string]
      (when settings-string
        [:<>
         [:dt settings-title]
         [:dd
          {:style {:white-space "pre-wrap"}
           :title settings-string}
          (goog-string/truncate settings-string config/max-protocol-entry-length)]])]]))

(defn step-id [step-count]
  (str "protocol-step-" step-count))

(defn- snapshot-creation-edit-dialog [_]
  (let [snapshot-id (r/atom (str (random-uuid)))]
    (fn [project-id]
      (let [create-snapshot? @(re-frame/subscribe [::create-snapshot?])
            snapshot-save-possible? @(re-frame/subscribe [::save-edit-possible?])
            edit-snapshot @(re-frame/subscribe [::snapshot-edit-active?])
            {:keys [create-snapshot-title
                    edit-snapshot-title
                    cancel-label
                    save-label]} @(re-frame/subscribe [::i18n/translate-multi
                                                       :create-snapshot-title
                                                       :edit-snapshot-title
                                                       :save-label :cancel-label])
            dialog-title (if create-snapshot?
                           create-snapshot-title
                           edit-snapshot-title)
            snapshot-desc (if create-snapshot?
                            {}
                            @(re-frame/subscribe [::snapshot-active-edit]))
            save-fn (if create-snapshot?
                      (fn []
                        (re-frame/dispatch [::save-created-snapshot @snapshot-id])
                        (re-frame/dispatch [::clean-snapshot-creation @snapshot-id])
                        (reset! snapshot-id (str (random-uuid))))
                      (fn []
                        (re-frame/dispatch [::save-snapshot-change project-id])))
            cancel-fn (if create-snapshot?
                        (fn []
                          (re-frame/dispatch [::clean-snapshot-creation]))
                        (fn []
                          (re-frame/dispatch [::clean-edit])))]
        [dialog
         {:show? (or create-snapshot? edit-snapshot),
          :hide-fn #()
          :title dialog-title
          :message [snapshot-editor snapshot-desc false],
          :yes {:label save-label
                :disabled? (not snapshot-save-possible?)
                :on-click save-fn},
          :cancel {:on-click cancel-fn
                   :label cancel-label}}]))))

(defn protocol-step [nr-total-step
                     step-count
                     {:keys [head] :as step-desc}
                     project-id
                     writable-project?
                     only-snapshots?
                     project-to-load]
  (let [snapshot-desc @(re-frame/subscribe [::snapshot-desc project-id head])
        language @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language])
        opened-step @(re-frame/subscribe [::opened-step])
        step-read-only? @(re-frame/subscribe [::step-read-only])]
    (when (or (and only-snapshots? snapshot-desc)
              (not only-snapshots?))
      [:li {:id (step-id step-count)
            :class [(when snapshot-desc "protocol__snapshot")
                    (when (and (not project-to-load)
                               step-read-only?
                               (or (= (:c head) (:c opened-step))
                                   (and (nil? (:c opened-step)) (= nr-total-step step-count))))
                      "current-prot-step")]}
       [:div.protocol__step
        (.toLocaleString step-count language)
        [:span.protocol__date
         (timestamp->string (:t head))]]
       [:div.protocol__container
        [step-info project-id step-desc snapshot-desc project-to-load]
        [step-actions snapshot-desc step-count step-desc project-id writable-project? project-to-load]]
       (when snapshot-desc
         [:div.protocol__star
          [icon {:icon :star}]])])))

(defn protocol-list-id [frame-id]
  (str "protocol-list-" frame-id))

(defn frame-header-items []
  (let [project-loaded? @(re-frame/subscribe [:de.explorama.frontend.projects.core/loaded-project-id])
        only-snapshots? @(re-frame/subscribe [::only-snapshots?])
        snapshot-only-tooltip @(re-frame/subscribe [::i18n/translate :snapshot-only-tooltip])
        snapshot-all-tooltip @(re-frame/subscribe [::i18n/translate :snapshot-all-tooltip])
        file-name (str (get @(re-frame/subscribe [:de.explorama.frontend.projects.core/project-loaded-infos])
                            :title
                            "workspace")
                       ".txt")]
    (cond-> []
      project-loaded?
      (conj {:title (if only-snapshots?
                      snapshot-all-tooltip
                      snapshot-only-tooltip)
             :tooltip-extra-params {:direction :left}
             :start-icon (if only-snapshots?
                           "star"
                           "star-o")
             :variant :tertiary
             :size :small
             :on-click #(re-frame/dispatch [::switch-only-snapshot])})
      :always
      (conj {:title (re-frame/subscribe [::i18n/translate :download-tooltip])
             :tooltip-extra-params {:direction :left}
             :start-icon :download
             :variant :tertiary
             :size :small
             :on-click (fn []
                         (data-exchange/download-content
                          (or file-name "protocol.txt")
                          (or @protocol-text-sub "")))}))))

(defn view [frame-id {:keys [project-to-load]}]
  (let [to-load-project-id (:project-id project-to-load)
        step-descs (if to-load-project-id
                     @dialog-protocol-steps-sub
                     @protocol-steps-sub)
        step-counts (count step-descs)
        project-id (or to-load-project-id
                       @(re-frame/subscribe [:de.explorama.frontend.projects.core/loaded-project-id]))
        only-snapshots? @(re-frame/subscribe [::only-snapshots?])
        writable-project? (if project-id
                            @(re-frame/subscribe [:de.explorama.frontend.projects.core/writable-project? project-id])
                            true)
        project-without-steps @(re-frame/subscribe [::i18n/translate :project-without-steps])
        dont-show-protocol? @(re-frame/subscribe [:de.explorama.frontend.projects.core/show-sync-tools?])
        no-protocol-while-sync-hint @(re-frame/subscribe [::i18n/translate :no-protocol-sync])]
    [:div.content {:style {:height "100%"}}
     [:div.window__body.window__body--scroll {:style {:height "100%"}}
      (cond
        dont-show-protocol? [:div no-protocol-while-sync-hint]
        (nil? step-descs) [loading-screen {:show? true}]
        :else [:<>
               [snapshot-creation-edit-dialog project-id]
               [virtualized-list {:extra-class "protocol"
                                  :dynamic-height? true
                                  :full-height? true
                                  :full-width? true
                                  :rows (vec (reverse step-descs))
                                  :no-rows-renderer (fn []
                                                      (if project-to-load
                                                        (r/as-element [:div project-without-steps])
                                                        (r/as-element [:div])))
                                  :row-renderer (fn [key idx style row]
                                                  (with-meta
                                                    [:div {:style (assoc style :overflow "auto")}
                                                     [protocol-step step-counts (- step-counts idx) row project-id writable-project? only-snapshots? project-to-load]]
                                                    {:key (str "protocol-" idx)}))}]])]]))

(defn- open-window-fx [db]
  (let [protocol-open? (get-in db path/protocol-window-open)]
    (cond
      (not protocol-open?)
      {:db (assoc-in db path/protocol-window-open true)
       :fx [[:dispatch (fi/call-api :sidebar-create-event-vec
                                    {:module "projects-protocol-window"
                                     :title "Protocol"
                                     :event ::view-event
                                     :id "protocol"
                                     :position :right
                                     :close-event [::close-action]
                                     :width 600
                                     :header-items-fn frame-header-items})]
            [:dispatch [::update-based-events]]]}
      protocol-open? (do
                       (reset! protocol-store init-store-desc)
                       {:db (assoc-in db path/protocol-window-open false)
                        :dispatch (fi/call-api :hide-sidebar-event-vec "protocol")})
      :else {})))

(re-frame/reg-event-fx
 ::open-window
 (fn [{db :db} _]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::open-window-sync])
     (open-window-fx db))))

(re-frame/reg-event-fx
 ::open-window-sync
 (fn [{db :db} _]
   (open-window-fx db)))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ _ close-event]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::close-sync close-event])
     (reset! protocol-store init-store-desc)
     {:db (update-in db path/protocol-window dissoc :open?)
      :dispatch-n [close-event]})))

(re-frame/reg-event-fx
 ::close-sync
 (fn [{db :db} [_ close-event]]
   (reset! protocol-store init-store-desc)
   {:db (update-in db path/protocol-window dissoc :open?)
    :fx [(when close-event [:dispatch close-event])
         [:dispatch (fi/call-api :hide-sidebar-event-vec "protocol")]]}))

(re-frame/reg-event-fx
 ::view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event]} params]
     (case action
       :frame/init
       {}
       :frame/close
       {:dispatch [::close-action frame-id callback-event]}
       {}))))

(re-frame/reg-event-db
 ::switch-only-snapshot
 (fn [db _]
   (update-in db path/protocol-window-only-snapshots not)))

(re-frame/reg-sub
 ::only-snapshots?
 (fn [db _]
   (get-in db path/protocol-window-only-snapshots false)))
;;; END UI

(re-frame/reg-event-fx
 ::update-based-events
 [(re-frame/inject-cofx ::inject/sub (with-meta [:de.explorama.frontend.projects.core/loaded-project-id] {:ignore-dispose true}))]
 (fn [{project-id :de.explorama.frontend.projects.core/loaded-project-id
       db :db} _]
   (when (get-in db path/protocol-window-open)
     (let [user-info (fi/call-api :user-info-db-get db)
           workspace-id (fi/call-api :workspace-id-db-get db)]
       {:backend-tube [ws-api/based-events-route
                         {:client-callback [ws-api/based-events-result]}
                         {:project-id project-id
                          :workspace-id workspace-id} user-info]}))))

(re-frame/reg-event-fx
 ::request-based-events-for-step-dialog
 (fn [{db :db} [_ project-id]]
   (let [user-info (fi/call-api :user-info-db-get db)
         workspace-id (fi/call-api :workspace-id-db-get db)]
     {:backend-tube [ws-api/based-events-route
                       {:client-callback [ws-api/based-events-result]}
                       {:project-id project-id
                        :workspace-id workspace-id} user-info]})))

(defn default-vertical-func [events _ _]
  (if config/use-default-protocol-func
    (map (fn [[index [counter [_ frame-id action]]]]
           [index
            {:window-id frame-id
             :action action
             :counter counter}])
         events)
    []))

(defn events->steps [protocol-services origins events snapshot-heads db]
  (reduce (fn [acc vertical]
            (let [vertical-func (or (get protocol-services vertical)
                                    default-vertical-func)
                  result (vertical-func events snapshot-heads db)]
              (if (map? result)
                (merge acc result)
                acc)))
          {}
          origins))

(defn- calc-steps [db based-events]
  (let [{:keys [snapshots]} (project-loaded-infos db)
        protocol-services (fi/call-api :service-category-db-get db :event-protocol)
        origins (->> (map (fn [[_ [origin]]]
                            origin)
                          based-events)
                     set)
        steps  (events->steps protocol-services
                              origins
                              based-events
                              (set (mapv :head
                                         snapshots))
                              db)]
    (->> steps
         (sort-by (fn [[idx]] idx))
         (map-indexed (fn [idx [_ desc]]
                        (assoc desc :counter idx)))
         vec)))

(defn- snapshot-desc [snapshots counter]
  (some #(when (= (:head %) counter)
           %)
        snapshots))

(defn- build-text-string [db steps]
  (let [{:keys [snapshots]} (project-loaded-infos db)
        snapshot-heads (set (map :head snapshots))
        window-string (i18n/translate db :step-info-window)
        action-title (i18n/translate db :step-info-action)
        settings-title (i18n/translate db :step-info-settings)
        snapshot-title-label (i18n/translate db :snapshot-title-label)
        snapshot-description-label (i18n/translate db :snapshot-title-description)]
    (string/join "\n---------------------------\n"
                 (reverse
                  (map-indexed
                   (fn [idx {:keys [window-id window-title action settings settings-sub counter]}]
                     (let [step-count (inc idx)
                           window-title (or window-title
                                            (fi/call-api :frame-title-all-db-get db window-id)
                                            (fi/call-api :full-frame-title-raw window-id))
                           action-string (if (keyword? action)
                                           (i18n/translate db action)
                                           action)
                           settings-string (if (not-empty settings-sub)
                                             (sub-and-dispose settings-sub)
                                             (translate-settings (partial i18n/translate db)
                                                                 settings))

                           {snapshot-title :title
                            snapshot-description :description} (snapshot-desc snapshots counter)]
                       (str step-count
                            (str (if (snapshot-heads counter)
                                   (str " * Snapshot *" "\n"
                                        snapshot-title-label "\t\t" snapshot-title "\n"
                                        snapshot-description-label "\t\t" snapshot-description)
                                   "")
                                 "\n"
                                 window-string "\t\t" window-title "\n"
                                 action-title "\t\t" action-string
                                 (if settings-string
                                   (str "\n"
                                        settings-title "\n"
                                        (string/join "\n"
                                                     (map #(str "- " %)
                                                          (string/split settings-string #"\n"))))
                                   "")))))
                   steps)))))

(defn- update-protocol-steps [db based-events]
  (let [new-proto-steps (calc-steps db based-events)
        proto-text (build-text-string db new-proto-steps)]
    (swap! protocol-store assoc
           :based-events based-events
           :protocol-steps new-proto-steps
           :protocol-text proto-text)))

(re-frame/reg-event-fx
 ws-api/based-events-result
 (fn [{db :db} [_ based-events]]
   (let [project-id (get-in db ppath/open-at-step-dialog)
         context-id (or
                     project-id
                     (loaded-project-id db)
                     (fi/call-api :workspace-id-db-get db))
         [store protocol-context-id] (if project-id
                                       [dialog-protocol-store @dialog-protocol-context-id]
                                       [protocol-store @protocol-context-id])]
     (if (= protocol-context-id context-id)
       (update-protocol-steps db based-events)
       (let [proto-steps (calc-steps db based-events)
             proto-text (build-text-string db proto-steps)]
         (reset! store {:id context-id
                        :based-events based-events
                        :protocol-steps proto-steps
                        :protocol-text proto-text}))))
   {}))

(re-frame/reg-sub
 ::snapshot-desc
 (fn [db [_ project-id counter]]
   (snapshot-desc (:snapshots (project-by-id db project-id)) counter)))

(re-frame/reg-event-fx
 ::delete-snapshot
 [(re-frame/inject-cofx ::inject/sub (with-meta [:de.explorama.frontend.projects.core/loaded-project-id] {:ignore-dispose true}))]
 (fn [{project-id :de.explorama.frontend.projects.core/loaded-project-id
       db :db} [_ counter]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:backend-tube [ws-api/delete-snapshot-route
                       {}
                       {:project-id project-id
                        :head counter}
                       user-info]})))

(re-frame/reg-event-fx
 ::start-loading-step
 [(re-frame/inject-cofx ::inject/sub (with-meta [:de.explorama.frontend.projects.core/loaded-project-id] {:ignore-dispose true}))]
 (fn [{db :db
       project-id :de.explorama.frontend.projects.core/loaded-project-id} [_ counter read-only?]]
   (let [creator-user (when project-id
                        (get-in (p-utils/all-projects db) [project-id :creator]))
         workspace-id (fi/call-api :workspace-id-db-get db)]
     (when-not read-only? ;Make sure on not read-only? to reset protocol
       (reset! protocol-store init-store-desc))
     {:db (assoc-in db path/step-loading true)
      :dispatch-n [[:de.explorama.frontend.projects.core/dont-cleanall-veto]
                   [::opened-step counter]
                   (fi/call-api :clean-workspace-event-vec
                                [::clean-workspace-done {:project-id project-id
                                                         :workspace-id workspace-id} counter read-only? creator-user false])]})))

(re-frame/reg-event-fx
 ::clean-workspace-done
 (fn [{db :db} [_ plogs-id counter read-only? creator-user]]
   (let [logged-in-user-info (fi/call-api :user-info-db-get db)
         user-info (if creator-user
                     creator-user
                     logged-in-user-info)]
     {:backend-tube [ws-api/load-head-route
                       {:client-callback [ws-api/load-head-result]}
                       {:plogs-id plogs-id
                        :head counter
                        :read-only? read-only?}
                       user-info]
      :fx [[:dispatch [:de.explorama.frontend.projects.core/clean-workspace-done]]
           [:dispatch [:de.explorama.frontend.projects.views.project-loading-screen/show-dialog plogs-id]]]})))

(re-frame/reg-event-fx
 ws-api/load-head-result
 (fn [{db :db} [_ step]]
   (let [logs (:logs step)
         plogs-id (get step :plogs-id)
         step-desc (get step :snapshot)
         read-only? (get step-desc :read-only?)
         project? (seq (get plogs-id :project-id))
         grouped-logs (p-utils/group-logs-by-origin logs)
         origins (set (keys grouped-logs))]
     (if (empty? logs)
       {:db (assoc-in db path/step-loaded-desc step-desc)
        :dispatch-n [[:de.explorama.frontend.projects.views.project-loading-screen/close-dialog plogs-id]
                     [::project-loaded plogs-id]
                     [::add-title-to-woco step]]}
       {:db (-> db
                (assoc-in (ppath/execute-events plogs-id) grouped-logs)
                (assoc-in (ppath/load-counter plogs-id) 0)
                (assoc-in (ppath/logs-to-load plogs-id) (count logs))
                (assoc-in path/step-loaded-desc step-desc)
                (assoc-in path/step-read-only read-only?)
                (ppath/loading-project-id plogs-id)
                (assoc-in ppath/origins-to-load origins))
        :dispatch-n [(fi/call-api [:interaction-mode :set-no-render-event])
                     [:de.explorama.frontend.projects.core/check-ready-replay]
                     [ws-api/request-projects-route]
                     (if read-only?
                       (fi/call-api [:interaction-mode :set-pending-read-only-event])
                       (fi/call-api [:interaction-mode :set-pending-normal-event]))
                     (when project?
                       [:de.explorama.frontend.projects.core/add-title-to-woco step])]}))))

(re-frame/reg-sub
 ::open-at-step-dialog
 (fn [db _]
   (get-in db ppath/open-at-step-dialog)))

(re-frame/reg-event-fx
 ::open-at-step-dialog
 (fn [{db :db} [_ desc]]
   {:db (assoc-in db ppath/open-at-step-dialog desc)
    :fx [(when (:project-id desc)
           [:dispatch [:de.explorama.frontend.projects.protocol.core/request-based-events-for-step-dialog (:project-id desc)]])]}))

(re-frame/reg-sub
 ::opened-step
 (fn [db _]
   (get-in db ppath/opened-step)))

(re-frame/reg-sub
 ::step-read-only
 (fn [db _]
   (get-in db path/step-read-only)))

(re-frame/reg-event-db
 ::opened-step
 (fn [db [_ n]]
   (assoc-in db ppath/opened-step n)))

(defn open-project-at-step-dialog []
  (let [cancel-label @(re-frame/subscribe [::i18n/translate :cancel-label])
        open-project-at-step @(re-frame/subscribe [::i18n/translate :open-project-at-step])
        project-desc @(re-frame/subscribe [::open-at-step-dialog])
        project-id (:project-id project-desc)]
    (when (seq project-desc)
      [:div
       [dialog
        {:show? project-id
         :hide-fn #(re-frame/dispatch [:de.explorama.frontend.projects.protocol.core/open-at-step-dialog])
         :title open-project-at-step
         :message
         [:div {:style {:height "300px" :width "350px"}}
          [view nil {:project-to-load project-desc}]]
         :cancel  {:variant :secondary
                   :label cancel-label
                   :on-click #(re-frame/dispatch [:de.explorama.frontend.projects.protocol.core/open-at-step-dialog])}}]])))