(ns de.explorama.frontend.configuration.views.export-settings
  (:require [de.explorama.frontend.configuration.config :as config]
            [de.explorama.frontend.configuration.configs.config-types.export-settings :as export-configs]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.configuration.data.core :as data]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                   checkbox input-field]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [re-frame.core :refer [dispatch reg-event-fx subscribe]]
            [reagent.core :as r]))

(reg-event-fx
 ::apply-changes
 (fn [_ [_ new-configs]]
   {:fx (mapv (fn [[config-key val]]
                [:dispatch [::persistence/save-and-commit
                            export-configs/config-type
                            config-key
                            {:value val
                             :key config-key}]])
              new-configs)}))

(defn footer [init-state temporary-state]
  (let [apply-lable @(subscribe [::i18n/translate :save-settings-button])]
    [:div.footer
     [button {:start-icon :save
              :size :big
              :disabled? (= init-state @temporary-state)
              :variant :primary
              :on-click #(dispatch [::apply-changes (reduce (fn [acc [config-key val]]
                                                              (cond-> acc
                                                                (not= val (get init-state config-key))
                                                                (assoc config-key val)))
                                                            {}
                                                            @temporary-state)])
              :label apply-lable}]]))

(defn- config-checkbox [temporary-state config-key label]
  [checkbox
   {:checked? (r/cursor temporary-state [config-key])
    :label label
    :on-change (fn [new-state]
                 (swap! temporary-state assoc config-key new-state))}])

(defn ds-summary [temporary-state curr-mapping [_ ds]]
  (let [curr-val (get curr-mapping ds)
        max-len config/export-datasource-reference-max-length
        curr-len (if curr-val
                   (count curr-val)
                   0)]
    [:<>
     [:div.title.flex
      [tooltip {:text ds :direction :up}
       ds]]
     [:div
      [input-field
       {:value curr-val
        :aria-label "Export hint"
        :max-length max-len
        :caption (str curr-len "/" max-len)
        :on-change (fn [val]
                     (swap! temporary-state update export-configs/datasource-mapping assoc ds val))}]]]))

(defn ds-type-section [temporary-state curr-mapping ds-groups datasource-type]
  (let [label @(subscribe [::i18n/translate
                           (keyword (str "config-" (name datasource-type) "-datasources-label"))])]
    [:div
     [:h5 label]
     (into
      [:div.grid {:style {:grid-template-columns "1fr 2fr"
                          :gap "1rem"}}]
      (map (fn [ds]
             [ds-summary temporary-state curr-mapping ds])
           (sort-by second (get ds-groups datasource-type))))]))

(defn- datasource-mapping [temporary-state]
  (let [{:keys [config-export-datasource-mapping config-export-datasource-mapping-hint]}
        @(subscribe [::i18n/translate-multi :config-export-datasource-mapping :config-export-datasource-mapping-hint])
        ds-groups @(subscribe [::data/datasources])
        sorted-keys (sort (keys ds-groups))
        curr-mapping @(r/cursor temporary-state [export-configs/datasource-mapping])]
    [:div
     [:h3 config-export-datasource-mapping]
     config-export-datasource-mapping-hint
     (into
      [:div {:style {:margin-top 10}}]
      (map (fn [datasource-type] [ds-type-section temporary-state curr-mapping ds-groups datasource-type])
           sorted-keys))]))

(defn- custom-description [temporary-state]
  (let [{:keys [de-DE en-GB config-export-custom-description]}
        @(subscribe [::i18n/translate-multi :de-DE :en-GB :config-export-custom-description])
        max-length config/export-custom-description-max-length
        curr-len-en (if-let [v @(r/cursor temporary-state [export-configs/custom-description :en-GB])]
                      (count v)
                      0)
        curr-len-de (if-let [v @(r/cursor temporary-state [export-configs/custom-description :de-DE])]
                      (count v)
                      0)
        config-export-intro @(subscribe [::i18n/translate :config-export-intro])]
    [:div.flex.flex-column.gap-8
     config-export-intro
     [input-field
      {:value (r/cursor temporary-state [export-configs/custom-description :en-GB])
       :label (format config-export-custom-description en-GB)
       :max-length max-length
       :caption (str curr-len-en "/" max-length)
       :on-change (fn [val]
                    (swap! temporary-state assoc-in
                           [export-configs/custom-description :en-GB]
                           val))}]
     [input-field
      {:value (r/cursor temporary-state [export-configs/custom-description :de-DE])
       :label (format config-export-custom-description de-DE)
       :max-length max-length
       :caption (str curr-len-de "/" max-length)
       :on-change (fn [val]
                    (swap! temporary-state assoc-in
                           [export-configs/custom-description :de-DE]
                           val))}]]))

(defn- get-configs []
  {export-configs/show-date-flag @(fi/call-api [:config :get-config-sub] export-configs/config-type export-configs/show-date-flag)
   export-configs/show-user-flag @(fi/call-api [:config :get-config-sub] export-configs/config-type export-configs/show-user-flag)
   export-configs/custom-description (when-let [v @(fi/call-api [:config :get-config-sub] export-configs/config-type export-configs/custom-description)]
                                       (if (map? v)
                                         v
                                         {:en-GB ""
                                          :de-DE ""}))
   export-configs/show-datasources-flag @(fi/call-api [:config :get-config-sub] export-configs/config-type export-configs/show-datasources-flag)
   export-configs/datasource-mapping @(fi/call-api [:config :get-config-sub] export-configs/config-type export-configs/datasource-mapping)})

(defn view []
  (let [temporary-state (r/atom (get-configs))]
    (fn []
      (let [{:keys [config-export-show-date config-export-show-time config-export-show-user config-export-show-datasources]}
            @(subscribe [::i18n/translate-multi :config-export-show-date :config-export-show-time :config-export-show-user :config-export-show-datasources])
            curr-configs (get-configs)]
        [:<>
         [:div.content.settings
          [custom-description temporary-state]
          [:div
           [config-checkbox temporary-state export-configs/show-date-flag config-export-show-date]
           [config-checkbox temporary-state export-configs/show-time-flag config-export-show-time]
           [config-checkbox temporary-state export-configs/show-user-flag config-export-show-user]
           [config-checkbox temporary-state export-configs/show-datasources-flag config-export-show-datasources]]
          (when @(r/cursor temporary-state [export-configs/show-datasources-flag])
            [datasource-mapping temporary-state])]
         [footer curr-configs temporary-state]]))))