(ns de.explorama.frontend.common.i18n
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]))

(def default-language :en-GB)

(defn current-language [db]
  (or (fi/call-api [:config :get-config-db-get]
                   db
                   :i18n
                   :lang)
      default-language))

(def ^:private month-names (atom nil))

(defn month-name
  ([num lang]
   (when-not @month-names
     (reset! month-names (js->clj (aget js/window "EXPLORAMA_MONTH_NAMES"))))
   (get-in @month-names
           [(str num) (name lang)] num))
  ([num]
   (let [lang @(fi/call-api [:config :get-config-sub] :i18n :lang)]
     (month-name num (or lang default-language)))))

(defn localized-number
  ([num lang]
   (let [lang (if (keyword? lang)
                (name lang)
                lang)]
     (if (and lang num)
       (try (.toLocaleString num lang)
            (catch :default e
              (error "failed to create localstring:" num "; lang" lang "; Exception:" e)
              (str num)))
       (str num))))
  ([num]
   (let [lang @(fi/call-api [:config :get-config-sub] :i18n :lang)]
     (localized-number num (or lang default-language)))))

(re-frame/reg-sub
 ::current-language
 (fn [db]
   (current-language db)))

(defn- not-found-str
  ([word-key custom-msg]
   (if (keyword? word-key)
     (str (or custom-msg "Key not found: ") word-key)
     (str (or custom-msg "Key not found: ") (keyword word-key)))))

(defn translate
  ([db word-key custom-msg]
   (or (fi/call-api [:i18n :translate-db-get]
                    db
                    word-key)
       (not-found-str word-key custom-msg)))
  ([db word-key]
   (translate db word-key nil)))

(re-frame/reg-sub
 ::translate
 (fn [db [_ word-key]]
   (translate db word-key)))

(defn translate-multi [db word-keys]
  (or (fi/call-api [:i18n :translate-multi-db-get]
                   db
                   word-keys)
      (reduce
       (fn [res word-key]
         (assoc res word-key ""))
       {}
       word-keys)))

(re-frame/reg-sub
 ::translate-multi
 (fn [db [_ & word-keys]]
   (translate-multi db word-keys)))

(defn attribute-label
  ([labels attr]
   (get labels attr attr))
  ([attr]
   (attribute-label @(fi/call-api [:i18n :get-labels-sub]) attr)))