(ns de.explorama.frontend.configuration.i18n.translations
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [i18n.translations :refer [translations]]))


(defn available-languages [db]
  [{:display-name :lang-en-GB
    :name :en-GB}
   {:display-name :lang-de-DE
    :name :de-DE}])

(defn translate
  ([_db lang word-key]
   (let [translation-path (if (vector? word-key)
                            (conj word-key lang)
                            [word-key lang])]
     (get-in translations translation-path)))
  ([db word-key]
   (translate db (i18n/current-language db) word-key)))

(defn translate-multi [db word-keys]
  (let [lang (i18n/current-language db)]
    (reduce
     (fn [res word-key]
       (assoc res word-key (translate db lang word-key)))
     {}
     word-keys)))