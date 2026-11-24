(ns de.explorama.frontend.common.i18n
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [re-frame.db :as rf-db]
            [reagent.ratom :as ratom]
            [taoensso.timbre :refer [error]]))

(def default-language :en-GB)

(defn current-language [db]
  (or (fi/call-api [:config :get-config-db-get]
                   db
                   :i18n
                   :lang)
      default-language))

(defn- current-language-anywhere
  "Subscribes when called in a reactive context (render stays reactive to
  language switches), reads app-db otherwise."
  []
  (if (ratom/reactive?)
    (or @(fi/call-api [:config :get-config-sub] :i18n :lang)
        default-language)
    (current-language @rf-db/app-db)))

(def ^:private month-names (atom nil))

(defn month-name
  ([num lang]
   (when-not @month-names
     (reset! month-names (js->clj (aget js/window "EXPLORAMA_MONTH_NAMES"))))
   (get-in @month-names
           [(str num) (name lang)] num))
  ([num]
   (month-name num (current-language-anywhere))))

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
   (localized-number num (current-language-anywhere))))

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

(defn translate-anywhere
  "Subscribes when called in a reactive context (render stays reactive to
  language switches), reads app-db otherwise."
  [word-key]
  (if (ratom/reactive?)
    @(re-frame/subscribe [::translate word-key])
    (translate @rf-db/app-db word-key)))

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
   (attribute-label (if (ratom/reactive?)
                      @(fi/call-api [:i18n :get-labels-sub])
                      (fi/call-api [:i18n :get-labels-db-get] @rf-db/app-db))
                    attr)))

(defn unit-suffix [units attr]
  (let [attr-units (get units attr)]
    (when (= 1 (count attr-units))
      (str " (" (first attr-units) ")"))))

(defn attribute-units []
  (if (ratom/reactive?)
    (some-> (fi/call-api [:acs :attribute-units-sub]) deref)
    (fi/call-api [:acs :attribute-units-db-get] @rf-db/app-db)))

(defn attribute-label-with-unit
  ([labels units attr]
   (str (attribute-label labels attr) (unit-suffix units attr)))
  ([attr]
   (attribute-label-with-unit (if (ratom/reactive?)
                                @(fi/call-api [:i18n :get-labels-sub])
                                (fi/call-api [:i18n :get-labels-db-get] @rf-db/app-db))
                              (attribute-units)
                              attr)))

(defn labels-with-units
  ([labels units]
   (reduce (fn [acc attr]
             (assoc acc attr (attribute-label-with-unit labels units attr)))
           labels
           (keys units)))
  ([db]
   (labels-with-units (fi/call-api [:i18n :get-labels-db-get] db)
                      (fi/call-api [:acs :attribute-units-db-get] db))))