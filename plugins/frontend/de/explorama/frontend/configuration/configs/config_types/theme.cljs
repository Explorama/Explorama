(ns de.explorama.frontend.configuration.configs.config-types.theme
  (:require [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]))

(def default :light)
(def config-type :theme)
(def theme-config-id :theme)

(def themes [{:value :light
              :label :theme-light}
             {:value :dark
              :label :theme-dark}
             {:value :system
              :label :theme-system}])

(defn add-system-theme-listener []
  (let [dark-match (.matchMedia js/window "(prefers-color-scheme: dark)")]
    (.addListener dark-match
                  (fn [e]
                    (let [current-theme @(re-frame/subscribe [::current-theme-raw])]
                      (when (#{:system nil} current-theme)
                        (re-frame/dispatch
                         [::persistence/save-and-commit
                          config-type
                          theme-config-id
                          {:value :system
                           :key theme-config-id}])))))))

(defn available-theme-options [db]
  (mapv (fn [{:keys [value label]}]
          {:value value
           :label (i18n/translate db label)})
        themes))

(re-frame/reg-sub
 ::available-theme-options
 (fn [db]
   (available-theme-options db)))

(defn current-theme [db]
  (let [db-value (get-in db (path/config-entry config-type theme-config-id))
        db-value (if (= :system db-value) nil db-value)]
    (cond
      db-value
      db-value
      (aget (.matchMedia js/window "(prefers-color-scheme: dark)") "matches")
      :dark
      (aget (.matchMedia js/window "(prefers-color-scheme: light)") "matches")
      :light
      :else
      default)))

(re-frame/reg-sub
 ::current-theme
 (fn [db]
   (current-theme db)))

(re-frame/reg-sub
 ::current-theme-raw
 (fn [db]
   (get-in db (path/config-entry config-type theme-config-id))))


(re-frame/reg-sub
 ::temporary-theme
 (fn [db]
   (let [theme (or
                (get-in db path/temporary-user-settings-theme)
                (get-in db (path/config-entry config-type theme-config-id)))]
     (some (fn [{:keys [value] :as theme-option}]
             (when (= value theme)
               theme-option))
           (available-theme-options db)))))

(defn theme-changed-sub? [db]
  (let [new-theme (get-in db path/temporary-user-settings-theme)
        old-theme (get-in db (path/config-entry config-type theme-config-id))
        theme-changed? (and new-theme (not= new-theme old-theme))]
    theme-changed?))

(re-frame/reg-sub
 ::theme-changed?
 (fn [db _]
   (theme-changed-sub? db)))

(re-frame/reg-event-db
 ::change-temporary-theme
 (fn [db [_ theme]]
   (assoc-in db path/temporary-user-settings-theme theme)))