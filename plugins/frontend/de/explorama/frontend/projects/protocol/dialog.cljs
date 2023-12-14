(ns de.explorama.frontend.projects.protocol.dialog
  (:require [clojure.string :as string]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.projects.protocol.path :as path]))

(re-frame/reg-sub
 ::couldnt-open
 (fn [db _]
   (get-in db path/protocol-not-open)))

(re-frame/reg-event-db
 ::couldnt-open
 (fn [db [_ reasons]]
   (assoc-in db path/protocol-not-open reasons)))

(re-frame/reg-event-db
 ::close-couldnt-open
 (fn [db _]
   (assoc-in db path/protocol-not-open nil)))

(defn explain-couldnt-open
  [conditions-for-opening]
  (let [explanations
        {:writable
         @(re-frame/subscribe [::i18n/translate :protocol-project-read-only])
         :project-loaded
         @(re-frame/subscribe [::i18n/translate :protocol-no-project-loaded])
         :not-already-open
         @(re-frame/subscribe [::i18n/translate :protocol-already-open])}]
    (->> conditions-for-opening
         (filter (comp not true? second))
         (map first)
         (mapv #(get explanations %))
         (string/join "\n\n"))))

(defn couldnt-open-panel []
  (let [reasons @(re-frame/subscribe [::couldnt-open])
        explanation (explain-couldnt-open reasons)
        protocol-form-ok-label @(re-frame/subscribe [::i18n/translate :protocol-form-ok])]
    [dialog {:title @(re-frame/subscribe [::i18n/translate :protocol-couldnt-open])
             :message explanation
             :show? (boolean reasons)
             :hide-fn #(re-frame/dispatch [::close-couldnt-open])
             :ok {:label protocol-form-ok-label
                  :on-click #(re-frame/dispatch [::close-couldnt-open])}}]))