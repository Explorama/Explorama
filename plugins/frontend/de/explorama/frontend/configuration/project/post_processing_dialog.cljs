(ns de.explorama.frontend.configuration.project.post-processing-dialog
  (:require [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db reg-event-fx]]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.common.i18n :as i18n]
            [reagent.core :as r]
            [clojure.string :refer [lower-case]]
            [de.explorama.frontend.configuration.components.dialog :as dialog :refer [confirm-dialog]]
            [de.explorama.frontend.configuration.configs.config-types.layout :as layout-configs]
            [de.explorama.frontend.configuration.configs.config-types.overlayer :as overlayer-configs]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field]]
            [de.explorama.frontend.ui-base.components.frames.core :as frameui]
            [de.explorama.frontend.ui-base.components.common.core :refer [virtualized-list]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(reg-event-fx
 ::show-dialog
 (fn [{db :db} [_ callbacks]]
   {:db (assoc-in db path/post-process-dialog-callbacks callbacks)}))

(reg-event-fx
 ::execute-callback
 (fn [{db :db} [_ access-key]]
   (let [access-key (case access-key
                      :yes :yes-callback
                      :no :no-callback
                      nil)
         clean-up? (= :no access-key)
         callback (get-in db (conj path/post-process-dialog-callbacks
                                   access-key))]
     (when callback
       {:db (cond-> db
              (not clean-up?) (update-in path/project-post-checks-root
                                         dissoc
                                         path/post-process-dialog-callbacks-key)
              clean-up? (update-in path/root dissoc path/project-post-checks-root-key))
        :dispatch callback}))))

(reg-sub
 ::show-dialog?
 (fn [db]
   (boolean (get-in db path/post-process-dialog-callbacks))))

(defn dialog []
  (let [show? @(subscribe [::show-dialog?])
        {:keys [post-proc-dialog-title
                post-proc-dialog-headline
                post-proc-dialog-explanations
                post-proc-dialog-confirm
                confirm-dialog-no]}
        @(subscribe [::i18n/translate-multi
                     :post-proc-dialog-title
                     :post-proc-dialog-headline
                     :post-proc-dialog-explanations
                     :post-proc-dialog-confirm
                     :confirm-dialog-no])]
    [frameui/dialog {:title post-proc-dialog-title
                     :message
                     [:<>
                      [:h4 post-proc-dialog-headline]
                      [:div.explorama__form__input.explorama__form--info
                       post-proc-dialog-explanations]]
                     :show? show?
                     :hide-fn (fn []
                                (dispatch [::execute-callback :no]))
                     :no {:label confirm-dialog-no}
                     :yes {:label post-proc-dialog-confirm
                           :on-click (fn []
                                       (dispatch [::execute-callback :yes]))}}]))