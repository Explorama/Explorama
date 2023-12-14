(ns de.explorama.frontend.configuration.configs.config-types.language
  (:require [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]))

(def default i18n/default-language)
(def config-type :i18n)
(def lang-config-id :lang)


(defn available-languages-options [db]
  (when-let [langs (fi/call-api [:i18n :available-languages-db-get] db)]
    (into [] (set (mapv (fn [{lang :name disp-lang :display-name}]
                          {:value (keyword lang)
                           :label (i18n/translate db (keyword disp-lang))})
                        langs)))))
(re-frame/reg-sub
 ::available-languages-options
 (fn [db]
   (available-languages-options db)))

(re-frame/reg-sub
 ::current-language
 (fn [db]
   (or
    (get-in db (path/config-entry config-type lang-config-id))
    default)))

(re-frame/reg-sub
 ::temporary-language
 (fn [db]
   (let [lang (or
               (get-in db path/temporary-user-settings-language)
               (get-in db (path/config-entry config-type lang-config-id))
               default)]
     (some (fn [{:keys [value] :as lang-option}]
             (when (= value lang)
               lang-option))
           (available-languages-options db)))))

(defn language-changed-sub? [db]
  (let [new-lang (get-in db path/temporary-user-settings-language)
        old-language (get-in db (path/config-type  :i18n))
        language-changed? (and new-lang (not= new-lang (:lang old-language)))]
    language-changed?))

(re-frame/reg-sub
 ::language-changed?
 (fn [db _]
   (language-changed-sub? db)))

(re-frame/reg-event-db
 ::change-temporary-language
 (fn [db [_ language]]
   (assoc-in db path/temporary-user-settings-language language)))