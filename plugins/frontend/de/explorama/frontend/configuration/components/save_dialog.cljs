(ns de.explorama.frontend.configuration.components.save-dialog
  (:require [re-frame.core :refer [subscribe dispatch]]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.common.i18n :as i18n]
            [reagent.core :as r]
            [clojure.string :refer [lower-case]]
            [de.explorama.frontend.configuration.components.dialog :as dialog :refer [confirm-dialog]]
            [de.explorama.frontend.configuration.configs.config-types.layout :as layout-configs]
            [de.explorama.frontend.configuration.configs.config-types.overlayer :as overlayer-configs]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-group collapsible-list]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.common.frontend-interface :as fi]))


(defn- names-selection [config-type existing-user-layout-names on-change-name]
  (let [user-layout-names (filter identity
                                  (mapv (fn [[k v]] (when-not (:default? v) k)) existing-user-layout-names))]
    [:div {:style {:height "150px"}}
     [collapsible-list
      {:items (mapv
               #(hash-map :label %1 :id %2 :collapsible? false)
               (sort-by #(lower-case (or % ""))
                        user-layout-names)
               (range (count user-layout-names)))
       :on-click #(on-change-name (:label %))}]]))

(defn save-dialog-impl [config-type new-name-state {:keys [show? can-edit? layout-desc existing-user-layout-names on-change-layout on-save]}]
  (let [{:keys [save-title save-button-label save-cancel-button-label save-existing-layouts save-default-warning layout-title-revert]}
        @(subscribe [::i18n/translate-multi
                     :save-title
                     :save-button-label
                     :save-cancel-button-label
                     :save-existing-layouts
                     :save-default-warning
                     :layout-title-revert])
        on-change-name #(reset! new-name-state %)
        reset-name-fn #(reset! new-name-state (:name layout-desc))
        new-name (val-or-deref new-name-state)
        default-names (filter identity
                              (mapv (fn [[k v]] (when (:default? v) k)) existing-user-layout-names))
        default-name? (some #{new-name} default-names)
        deref-can-edit? (val-or-deref can-edit?)
        form-error (when default-name? "explorama__form--error")]
    [dialog {:title save-title
             :show? show?
             :type :prompt
             :hide-fn (fn []
                        (reset! show? false)
                        (reset! can-edit? true)
                        (reset-name-fn))
             :ok {:label save-button-label
                  :disabled? (or (not deref-can-edit?)
                                 default-name?
                                 (not (seq new-name)))
                  :start-icon :save
                  :on-click (fn []
                              (let [{existing-id :id default? :default?} (get existing-user-layout-names new-name)]
                                (cond
                                  ;overwrite any existing layout = ask if layout should be overwrite
                                  (and (seq existing-id)
                                       (not default?))
                                  (on-save (-> layout-desc
                                               (dissoc :temporary? :default? :datasources)
                                               (assoc :name new-name)
                                               (assoc :id existing-id))
                                           (:id layout-desc)
                                           true)
                                  ;some new name or default layout = just save as new layout
                                  :else
                                  (on-save (-> layout-desc
                                               (dissoc :temporary? :default? :datasources)
                                               (assoc :name new-name)
                                               (assoc :id (str (random-uuid))))
                                           (:id layout-desc)))))}
             :cancel {:label save-cancel-button-label
                      :variant :secondary
                      :start-icon :close}
             :message
             [:<>
              [input-group
               {:items [{:type :input
                         :id "1"
                         :component-props {:value new-name
                                           :aria-label "Name"
                                           :disabled? (not deref-can-edit?)
                                           :on-change on-change-name}}
                        {:type :button
                         :id "2"
                         :component-props {:start-icon :reset
                                           :title layout-title-revert
                                           :disabled? (or (not deref-can-edit?)
                                                          (= new-name (:name layout-desc)))
                                           :on-click reset-name-fn}}]}]
              [:div.text-xs.text-bold.pl-4.mt-8 save-existing-layouts]
              [names-selection config-type existing-user-layout-names on-change-name]]}]))

(defn save-dialog [{:keys [config-type layout-desc]}]
  (let [new-name (r/atom nil)]
    (r/create-class
     {:display-name "layout-save-dialog"
      :component-did-mount #(reset! new-name (:name layout-desc))
      :component-did-update (fn [this argv]
                              (let [[_ {{o-name :name} :layout-desc}] argv
                                    [_ {{n-name :name} :layout-desc}] (r/argv this)]
                                (when-not (= o-name n-name)
                                  (reset! new-name n-name))))

      :reagent-render (fn [props]
                        [save-dialog-impl config-type new-name props])})))

(defn layout-overlayer-save-dialog [{:keys [on-close config-type-fn replace-fn handle-layout-change can-edit-save-dialog? save-dialog?
                                            frame-id edit-layout-desc] :as props}]
  (let [layout-desc (val-or-deref edit-layout-desc)
        ctype (config-type-fn layout-desc)
        existing-user-layout-names (cond (= ctype :layouts)
                                         @(subscribe [::layout-configs/existing-user-layout-names])
                                         (= ctype :overlayers)
                                         @(subscribe [::overlayer-configs/existing-user-overlayer-names]))

        {:keys [save-success-layout save-success-overlayer save-failure]}
        @(subscribe [::i18n/translate-multi
                     :save-success-layout
                     :save-success-overlayer
                     :save-failure])
        success-message (cond (= ctype :layouts)
                              save-success-layout
                              (= ctype :overlayers)
                              save-success-overlayer)
        dialog-key {:id (or frame-id :global)
                    :dialog-type (cond (= ctype :layouts)
                                       :overwrite-layout
                                       (= ctype :overlayers)
                                       :overwrite-overlayer)}]
    [:<>
     [save-dialog
      {:show? save-dialog?
       :can-edit? can-edit-save-dialog?
       :existing-user-layout-names  existing-user-layout-names
       :layout-desc layout-desc
       :config-type (config-type-fn layout-desc)
       :on-change-layout (fn [path new-val]
                           (handle-layout-change edit-layout-desc path new-val))
       :on-save (fn [layout-desc old-id overwrite?]
                  (let [config-desc (-> (or layout-desc
                                            (val-or-deref edit-layout-desc))
                                        (dissoc :default? :datasources))
                        config-type (config-type-fn config-desc)
                        config-id (:id config-desc)
                        save-event-vec [::persistence/save-and-commit
                                        config-type
                                        config-id
                                        config-desc
                                        ;;callbacks will be executed, when server response is there
                                        {:trigger-action :list-entries
                                         :success-callback
                                         (fn []
                                           (when replace-fn
                                             (replace-fn old-id layout-desc))
                                           (reset! save-dialog? false)
                                           (reset! can-edit-save-dialog? true)
                                           ; auto close after save; currently not wanted
                                           ;(reset! edit-layout-desc nil)
                                           ;(when (fn? on-close)
                                           ;    (let [updated-desc @(fi/call-api [:config :get-config-sub]
                                           ;                                     config-type config-id)]
                                           ;      (on-close updated-desc old-id)))
                                           (dispatch (fi/call-api :notify-event-vec
                                                                  {:type :success
                                                                   :category {:config :save}
                                                                   :message success-message})))
                                         :failed-callback
                                         (fn []
                                           (reset! save-dialog? false)
                                           (reset! can-edit-save-dialog? true)
                                           (reset! edit-layout-desc nil)
                                           (when (fn? on-close)
                                             (on-close))
                                           (dispatch (fi/call-api :notify-event-vec
                                                                  {:type :error
                                                                   :category {:config :save}
                                                                   :message save-failure})))}]]
                    (reset! can-edit-save-dialog? false)

                    (if overwrite?
                      (do
                        (dispatch [::dialog/set-data dialog-key {:overwrite-event save-event-vec
                                                                 :close-fn #(reset! can-edit-save-dialog? true)}])
                        (dispatch [::dialog/show-dialog dialog-key true (:dialog-type dialog-key)]))
                      (dispatch save-event-vec))))}]
     [confirm-dialog dialog-key]]))