(ns de.explorama.frontend.configuration.components.share-dialog
  (:require [clojure.string :as string]
            [re-frame.core :refer [subscribe dispatch]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [select]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [reagent.core :as r]))

(defn- send-copy [{:keys [share-users] :as props} selected-users]
  (let [share-users (->> (val-or-deref share-users)
                         (mapv #(select-keys % [:label :value]))
                         (filterv #(not (some #{%} @selected-users)))
                         (sort-by (comp string/lower-case :label))
                         vec)]
    [:div.send__copy
     [select
      {:is-multi? true
       :is-clearable? true
       :on-change #(reset! selected-users %)
       :options share-users
       :values @selected-users
       :menu-row-height 35
       :extra-class "input--w100"}]]))

(defn share-dialog-impl [selected-users {:keys [config-type layout-desc] :as props}]
  (let [{config-id :id} (val-or-deref layout-desc)
        share-title @(subscribe [::i18n/translate :send-copy-label])
        show-dialog? (val-or-deref layout-desc)]
    [dialog {:title share-title
             :message [send-copy props selected-users]
             :show? (boolean show-dialog?)
             :hide-fn (fn []
                        (reset! layout-desc nil))
             :yes {:label @(subscribe [::i18n/translate :send-label])
                   :on-click #(do
                                (dispatch [:de.explorama.frontend.configuration.configs.persistence/copy-config config-type config-id @selected-users])
                                (reset! selected-users nil))}
             :cancel {:label @(subscribe [::i18n/translate :cancel-label])
                      :variant :secondary
                      :on-click #(do
                                   (reset! selected-users nil))}}]))

(defn share-dialog [_]
  (let [selected-users (r/atom nil)]
    (r/create-class
     {:display-name "layout-share-dialog"
      :reagent-render (fn [props]
                        [share-dialog-impl selected-users props])})))

(defn layout-overlayer-share-dialog [{:keys [config-type-fn can-edit-save-dialog? share-layout-desc share-users] :as props}]
  (let [ctype (config-type-fn share-layout-desc)]
    [:<>
     [share-dialog
      {:can-edit? can-edit-save-dialog?
       :layout-desc share-layout-desc
       :share-users share-users
       :config-type ctype
       :on-save #()}]]))